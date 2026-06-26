package cc.oofo.ai.mcp.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cc.oofo.ai.mcp.dto.AiMcpServerSaveDto;
import cc.oofo.ai.mcp.entity.AiMcpServer;
import cc.oofo.ai.mcp.entity.query.AiMcpServerQuery;
import cc.oofo.framework.core.service.BaseService;
import cc.oofo.framework.exception.BizException;
import cc.oofo.system.job.entity.SysJob;
import cc.oofo.system.job.service.SysJobService;
import lombok.RequiredArgsConstructor;

/**
 * MCP 服务配置服务。
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AiMcpServerService extends BaseService<AiMcpServer> {

    private static final Set<String> TRANSPORTS = Set.of("stdio", "sse", "streamable-http");
    private static final int MIN_KEEP_ALIVE_SECONDS = 5;
    private static final int MAX_KEEP_ALIVE_SECONDS = 3600;
    private static final int DEFAULT_KEEP_ALIVE_SECONDS = 30;
    private static final String KEEP_ALIVE_JOB_GROUP = "MCP_KEEPALIVE";

    private final ObjectMapper objectMapper;
    private final SysJobService sysJobService;

    public Page<AiMcpServer> page(AiMcpServerQuery query) {
        query.getQueryWrapper().orderByDesc("enabled").orderByDesc("created_at");
        return page(query.getMPPage(), query.getQueryWrapper());
    }

    public AiMcpServer add(AiMcpServerSaveDto dto) {
        validate(dto, null);
        if (nameExists(null, dto.getName())) {
            throw new BizException("MCP 服务名称已存在");
        }

        AiMcpServer entity = new AiMcpServer();
        copyToEntity(dto, entity);
        entity.setEnabled(dto.getEnabled() == null || dto.getEnabled());
        save(entity);
        syncKeepAliveJob(entity);
        updateById(entity);
        return entity;
    }

    public void update(AiMcpServerSaveDto dto) {
        AiMcpServer entity = getByIdOrThrow(dto.getId());
        validate(dto, entity.getId());
        if (nameExists(entity.getId(), dto.getName())) {
            throw new BizException("MCP 服务名称已存在");
        }

        copyToEntity(dto, entity);
        syncKeepAliveJob(entity);
        updateById(entity);
    }

    public void changeEnabled(String id, boolean enabled) {
        AiMcpServer entity = getByIdOrThrow(id);
        entity.setEnabled(enabled);
        // 启停时同步保活任务（禁用→删除保活 job，启用→按配置重建）
        syncKeepAliveJob(entity);
        updateById(entity);
    }

    public void del(String id) {
        AiMcpServer entity = getByIdOrThrow(id);
        removeKeepAliveJob(entity.getKeepAliveJobId());
        removeById(id);
    }

    @Transactional(readOnly = true)
    public List<AiMcpServer> listEnabled() {
        return list(new LambdaQueryWrapper<AiMcpServer>().eq(AiMcpServer::getEnabled, true));
    }

    public AiMcpServer getByIdOrThrow(String id) {
        AiMcpServer entity = getById(id);
        if (entity == null) {
            throw new BizException("MCP 服务不存在");
        }
        return entity;
    }

    private void validate(AiMcpServerSaveDto dto, String id) {
        if (dto == null) {
            throw new BizException("MCP 服务配置不能为空");
        }
        if (!StringUtils.hasText(dto.getName())) {
            throw new BizException("MCP 服务名称不能为空");
        }
        if (!StringUtils.hasText(dto.getTransport()) || !TRANSPORTS.contains(dto.getTransport())) {
            throw new BizException("MCP 传输类型不支持");
        }
        if ("stdio".equals(dto.getTransport()) && !StringUtils.hasText(dto.getCommand())) {
            throw new BizException("stdio 模式命令不能为空");
        }
        if (!"stdio".equals(dto.getTransport()) && !StringUtils.hasText(dto.getUrl())) {
            throw new BizException("远程 MCP 地址不能为空");
        }
        validateJson(dto.getArgsJson(), true, "命令参数 JSON 必须是数组");
        validateJson(dto.getHeadersJson(), false, "请求头 JSON 必须是对象");
        if ("sse".equals(dto.getTransport()) && Boolean.TRUE.equals(dto.getKeepAlive())) {
            Integer interval = dto.getKeepAliveInterval();
            if (interval == null || interval < MIN_KEEP_ALIVE_SECONDS || interval > MAX_KEEP_ALIVE_SECONDS) {
                throw new BizException("保活间隔需在 " + MIN_KEEP_ALIVE_SECONDS + "~" + MAX_KEEP_ALIVE_SECONDS + " 秒之间");
            }
        }
    }

    private void validateJson(String json, boolean array, String message) {
        if (!StringUtils.hasText(json)) {
            return;
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            if (array && !node.isArray() || !array && !node.isObject()) {
                throw new BizException(message);
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(message);
        }
    }

    private boolean nameExists(String excludeId, String name) {
        LambdaQueryWrapper<AiMcpServer> wrapper = new LambdaQueryWrapper<AiMcpServer>().eq(AiMcpServer::getName, name);
        if (StringUtils.hasText(excludeId)) {
            wrapper.ne(AiMcpServer::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    private void copyToEntity(AiMcpServerSaveDto dto, AiMcpServer entity) {
        entity.setName(dto.getName());
        entity.setTransport(dto.getTransport());
        entity.setCommand(dto.getCommand());
        entity.setUrl(dto.getUrl());
        entity.setArgsJson(dto.getArgsJson());
        entity.setHeadersJson(dto.getHeadersJson());
        if (dto.getEnabled() != null) {
            entity.setEnabled(dto.getEnabled());
        }
        // 保活仅对 sse 生效，其余传输类型强制关闭，保持数据干净
        boolean keepAlive = "sse".equals(dto.getTransport()) && Boolean.TRUE.equals(dto.getKeepAlive());
        entity.setKeepAlive(keepAlive);
        entity.setKeepAliveInterval(dto.getKeepAliveInterval() == null
                ? DEFAULT_KEEP_ALIVE_SECONDS : dto.getKeepAliveInterval());
        entity.setRemark(dto.getRemark());
    }

    /**
     * 根据实体当前的保活配置同步对应的 sys_job：
     * 采用「先删后建」策略，保证任务名/间隔与服务始终一致；
     * 关闭保活、非 sse 或服务被禁用时仅删除任务。回填 keepAliveJobId 到实体（调用方负责持久化）。
     */
    private void syncKeepAliveJob(AiMcpServer entity) {
        removeKeepAliveJob(entity.getKeepAliveJobId());
        entity.setKeepAliveJobId(null);

        boolean shouldKeepAlive = "sse".equals(entity.getTransport())
                && Boolean.TRUE.equals(entity.getKeepAlive())
                && Boolean.TRUE.equals(entity.getEnabled());
        if (!shouldKeepAlive) {
            return;
        }

        int interval = entity.getKeepAliveInterval() == null
                ? DEFAULT_KEEP_ALIVE_SECONDS : entity.getKeepAliveInterval();
        SysJob job = new SysJob();
        job.setJobName("MCP保活-" + entity.getName());
        job.setJobGroup(KEEP_ALIVE_JOB_GROUP);
        job.setBeanName("aiMcpKeepAliveJob");
        job.setMethodName("ping");
        job.setMethodParams(entity.getId());
        job.setCronExpression(intervalToCron(interval));
        job.setConcurrent(0);
        job.setMisfirePolicy(2);
        job.setStatus(1); // 显式启动；SysJobService.defaults 默认会置为 0（暂停）
        job.setRemark("SSE MCP 连接保活，由 MCP 配置自动维护");
        sysJobService.add(job);
        entity.setKeepAliveJobId(job.getId());
    }

    private void removeKeepAliveJob(String jobId) {
        if (StringUtils.hasText(jobId)) {
            sysJobService.del(jobId);
        }
    }

    /**
     * 将保活间隔（秒）换算为 Quartz Cron。秒级用步进表达式，分钟级退化为分步进。
     */
    private String intervalToCron(int seconds) {
        int value = Math.min(Math.max(seconds, MIN_KEEP_ALIVE_SECONDS), MAX_KEEP_ALIVE_SECONDS);
        if (value < 60) {
            return "0/" + value + " * * * * ?";
        }
        int minutes = Math.min(value / 60, 59);
        return "0 0/" + minutes + " * * * ?";
    }
}
