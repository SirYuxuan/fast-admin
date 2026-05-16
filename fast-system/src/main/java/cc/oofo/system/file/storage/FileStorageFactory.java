package cc.oofo.system.file.storage;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import cc.oofo.framework.exception.BizException;
import cc.oofo.system.file.entity.SysFileConfig;
import cc.oofo.system.file.event.FileConfigChangedEvent;
import cc.oofo.system.file.service.SysFileConfigService;
import cc.oofo.system.file.storage.config.StorageConfig;
import lombok.RequiredArgsConstructor;

/**
 * 文件存储工厂
 *
 * <p>负责：
 * <ul>
 *   <li>把策略实现按 {@link StorageType} 索引化</li>
 *   <li>读取当前激活配置 + JSON 反序列化为类型化的 {@link StorageConfig}</li>
 *   <li>暴露上传/下载/删除入口</li>
 *   <li>监听配置变更事件清缓存</li>
 * </ul>
 *
 * @author Sir丶雨轩
 */
@Component
@RequiredArgsConstructor
public class FileStorageFactory {

    private final List<FileStorage> storages;
    private final SysFileConfigService configService;
    private final ObjectMapper objectMapper;

    private Map<StorageType, FileStorage> registry;

    /** 缓存当前激活配置的解析结果 */
    private volatile Resolved active;

    private record Resolved(SysFileConfig config, StorageConfig parsed, FileStorage storage) {
    }

    private Map<StorageType, FileStorage> registry() {
        if (registry == null) {
            Map<StorageType, FileStorage> m = new EnumMap<>(StorageType.class);
            for (FileStorage s : storages) {
                m.put(s.type(), s);
            }
            registry = m;
        }
        return registry;
    }

    /**
     * 取指定类型的策略实例
     */
    public FileStorage get(StorageType type) {
        FileStorage s = registry().get(type);
        if (s == null) {
            throw new BizException("未注册的存储类型: " + type);
        }
        return s;
    }

    /**
     * 把 JSON 配置反序列化为类型化对象
     */
    public StorageConfig parseConfig(StorageType type, String json) {
        try {
            return objectMapper.readValue(json, get(type).configClass());
        } catch (Exception e) {
            throw new BizException("解析存储配置失败: " + e.getMessage());
        }
    }

    /**
     * 取当前激活配置（含解析后的对象 + 对应策略）。无激活配置则抛错。
     */
    public Resolved current() {
        Resolved r = active;
        if (r == null) {
            synchronized (this) {
                r = active;
                if (r == null) {
                    SysFileConfig cfg = configService.getActiveOrThrow();
                    StorageConfig parsed = parseConfig(StorageType.valueOf(cfg.getType()), cfg.getConfig());
                    r = new Resolved(cfg, parsed, get(StorageType.valueOf(cfg.getType())));
                    active = r;
                }
            }
        }
        return r;
    }

    /**
     * 用当前激活配置上传
     */
    public UploadResult upload(java.io.InputStream in, FileMeta meta) {
        Resolved r = current();
        return r.storage().upload(r.parsed(), r.config().getUrlPrefix(), in, meta);
    }

    /**
     * 当前激活配置的 SysFileConfig 实体（含 id、type、urlPrefix）
     */
    public SysFileConfig currentEntity() {
        return current().config();
    }

    @EventListener
    public void onConfigChanged(FileConfigChangedEvent event) {
        active = null;
    }

}
