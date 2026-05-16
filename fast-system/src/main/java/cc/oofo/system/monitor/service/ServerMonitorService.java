package cc.oofo.system.monitor.service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.zaxxer.hikari.HikariDataSource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.TickType;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

/**
 * 服务器监控数据采集
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServerMonitorService {

    private final DataSource dataSource;
    private final StringRedisTemplate redisTemplate;

    /** 取所有监控数据 */
    public Map<String, Object> summary() {
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            SystemInfo si = new SystemInfo();
            HardwareAbstractionLayer hal = si.getHardware();
            OperatingSystem os = si.getOperatingSystem();
            data.put("cpu", buildCpu(hal.getProcessor()));
            data.put("memory", buildMemory(hal.getMemory()));
            data.put("server", buildServer(os));
            data.put("disks", buildDisks(os.getFileSystem()));
        } catch (Exception e) {
            log.error("collect oshi metrics failed", e);
        }
        data.put("jvm", buildJvm());
        data.put("redis", buildRedis());
        data.put("datasource", buildDataSource());
        return data;
    }

    // ---------- CPU ----------
    private Map<String, Object> buildCpu(CentralProcessor processor) {
        long[] prev = processor.getSystemCpuLoadTicks();
        Util.sleep(500);
        long[] curr = processor.getSystemCpuLoadTicks();

        long nice = curr[TickType.NICE.getIndex()] - prev[TickType.NICE.getIndex()];
        long irq = curr[TickType.IRQ.getIndex()] - prev[TickType.IRQ.getIndex()];
        long softirq = curr[TickType.SOFTIRQ.getIndex()] - prev[TickType.SOFTIRQ.getIndex()];
        long steal = curr[TickType.STEAL.getIndex()] - prev[TickType.STEAL.getIndex()];
        long cSys = curr[TickType.SYSTEM.getIndex()] - prev[TickType.SYSTEM.getIndex()];
        long user = curr[TickType.USER.getIndex()] - prev[TickType.USER.getIndex()];
        long iowait = curr[TickType.IOWAIT.getIndex()] - prev[TickType.IOWAIT.getIndex()];
        long idle = curr[TickType.IDLE.getIndex()] - prev[TickType.IDLE.getIndex()];
        long total = user + nice + cSys + idle + iowait + irq + softirq + steal;
        if (total <= 0) total = 1;

        Map<String, Object> cpu = new LinkedHashMap<>();
        cpu.put("name", processor.getProcessorIdentifier().getName());
        cpu.put("physicalCores", processor.getPhysicalProcessorCount());
        cpu.put("logicalCores", processor.getLogicalProcessorCount());
        cpu.put("usage", percent(total - idle, total));
        cpu.put("system", percent(cSys, total));
        cpu.put("user", percent(user, total));
        cpu.put("idle", percent(idle, total));
        cpu.put("ioWait", percent(iowait, total));
        return cpu;
    }

    // ---------- Memory ----------
    private Map<String, Object> buildMemory(GlobalMemory memory) {
        long total = memory.getTotal();
        long available = memory.getAvailable();
        long used = total - available;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("totalGB", toGB(total));
        map.put("usedGB", toGB(used));
        map.put("freeGB", toGB(available));
        map.put("usage", percent(used, total == 0 ? 1 : total));
        return map;
    }

    // ---------- Server ----------
    private Map<String, Object> buildServer(OperatingSystem os) {
        Map<String, Object> map = new LinkedHashMap<>();
        Properties props = System.getProperties();
        map.put("osName", props.getProperty("os.name"));
        map.put("osVersion", props.getProperty("os.version"));
        map.put("osArch", props.getProperty("os.arch"));
        try {
            InetAddress addr = InetAddress.getLocalHost();
            map.put("hostName", addr.getHostName());
            map.put("hostIp", addr.getHostAddress());
        } catch (Exception ignored) {
            map.put("hostName", "unknown");
            map.put("hostIp", "unknown");
        }
        map.put("osFamily", os.getFamily());
        map.put("userDir", props.getProperty("user.dir"));
        return map;
    }

    // ---------- Disks ----------
    private List<Map<String, Object>> buildDisks(FileSystem fs) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (OSFileStore store : fs.getFileStores()) {
            long total = store.getTotalSpace();
            long free = store.getUsableSpace();
            long used = total - free;
            // 跳过总容量 0 的虚拟分区
            if (total <= 0) continue;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", store.getName());
            map.put("mount", store.getMount());
            map.put("type", store.getType());
            map.put("totalGB", toGB(total));
            map.put("usedGB", toGB(used));
            map.put("freeGB", toGB(free));
            map.put("usage", percent(used, total));
            list.add(map);
        }
        return list;
    }

    // ---------- JVM ----------
    private Map<String, Object> buildJvm() {
        Map<String, Object> map = new LinkedHashMap<>();
        Properties props = System.getProperties();
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memBean.getHeapMemoryUsage();
        MemoryUsage nonHeap = memBean.getNonHeapMemoryUsage();
        ThreadMXBean tb = ManagementFactory.getThreadMXBean();

        map.put("javaVersion", props.getProperty("java.version"));
        map.put("javaHome", props.getProperty("java.home"));
        map.put("javaVendor", props.getProperty("java.vendor"));
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        map.put("uptime", formatUptime(uptime));
        map.put("startTime", ManagementFactory.getRuntimeMXBean().getStartTime());
        // 堆
        Map<String, Object> heapMap = new LinkedHashMap<>();
        heapMap.put("maxMB", toMB(heap.getMax()));
        heapMap.put("usedMB", toMB(heap.getUsed()));
        heapMap.put("committedMB", toMB(heap.getCommitted()));
        heapMap.put("usage", percent(heap.getUsed(), Math.max(heap.getMax(), 1)));
        map.put("heap", heapMap);
        // 非堆
        Map<String, Object> nhMap = new LinkedHashMap<>();
        nhMap.put("maxMB", nonHeap.getMax() < 0 ? -1 : toMB(nonHeap.getMax()));
        nhMap.put("usedMB", toMB(nonHeap.getUsed()));
        nhMap.put("committedMB", toMB(nonHeap.getCommitted()));
        map.put("nonHeap", nhMap);
        // 线程
        map.put("threads", tb.getThreadCount());
        map.put("daemonThreads", tb.getDaemonThreadCount());
        map.put("peakThreads", tb.getPeakThreadCount());
        return map;
    }

    // ---------- Redis ----------
    private Map<String, Object> buildRedis() {
        Map<String, Object> map = new LinkedHashMap<>();
        try {
            Properties info = redisTemplate.execute((RedisCallback<Properties>) c -> c.serverCommands().info());
            if (info != null) {
                map.put("version", info.getProperty("redis_version"));
                map.put("mode", info.getProperty("redis_mode"));
                map.put("uptime", formatUptime(parseLong(info.getProperty("uptime_in_seconds")) * 1000));
                map.put("connectedClients", info.getProperty("connected_clients"));
                map.put("usedMemoryHuman", info.getProperty("used_memory_human"));
                map.put("usedMemoryPeakHuman", info.getProperty("used_memory_peak_human"));
                map.put("commandsProcessed", info.getProperty("total_commands_processed"));
                map.put("hitRate", calcHitRate(info));
            }
            Long dbSize = redisTemplate.execute((RedisCallback<Long>) c -> c.serverCommands().dbSize());
            map.put("dbSize", dbSize);
        } catch (Exception e) {
            log.warn("get redis info failed: {}", e.getMessage());
            map.put("error", e.getMessage());
        }
        return map;
    }

    private String calcHitRate(Properties info) {
        long hits = parseLong(info.getProperty("keyspace_hits"));
        long misses = parseLong(info.getProperty("keyspace_misses"));
        long total = hits + misses;
        if (total == 0) return "0.00%";
        return new DecimalFormat("0.00").format(hits * 100.0 / total) + "%";
    }

    private long parseLong(String s) {
        try { return Long.parseLong(s); } catch (Exception e) { return 0; }
    }

    // ---------- DataSource ----------
    private Map<String, Object> buildDataSource() {
        Map<String, Object> map = new LinkedHashMap<>();
        if (dataSource instanceof HikariDataSource hk) {
            map.put("poolName", hk.getPoolName());
            map.put("jdbcUrl", hk.getJdbcUrl());
            map.put("driver", hk.getDriverClassName());
            map.put("maxPoolSize", hk.getMaximumPoolSize());
            map.put("minIdle", hk.getMinimumIdle());
            try {
                var mx = hk.getHikariPoolMXBean();
                map.put("active", mx.getActiveConnections());
                map.put("idle", mx.getIdleConnections());
                map.put("total", mx.getTotalConnections());
                map.put("waiting", mx.getThreadsAwaitingConnection());
            } catch (Exception e) {
                log.warn("get hikari pool stats failed: {}", e.getMessage());
            }
        } else {
            Map<String, Object> info = new HashMap<>();
            info.put("type", dataSource.getClass().getSimpleName());
            return info;
        }
        return map;
    }

    // ---------- helpers ----------
    private double toGB(long bytes) {
        return round(bytes / 1024.0 / 1024.0 / 1024.0, 2);
    }

    private double toMB(long bytes) {
        return round(bytes / 1024.0 / 1024.0, 2);
    }

    private double percent(long used, long total) {
        if (total <= 0) return 0;
        return round(used * 100.0 / total, 2);
    }

    private double round(double v, int scale) {
        double pow = Math.pow(10, scale);
        return Math.round(v * pow) / pow;
    }

    private String formatUptime(long ms) {
        long days = TimeUnit.MILLISECONDS.toDays(ms);
        long hours = TimeUnit.MILLISECONDS.toHours(ms) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60;
        return String.format("%d天 %02d:%02d:%02d", days, hours, minutes, seconds);
    }
}
