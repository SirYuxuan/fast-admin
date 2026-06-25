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
import lombok.RequiredArgsConstructor;

/**
 * MCP 服务配置服务。
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AiMcpServerService extends BaseService<AiMcpServer> {

    private static final Set<String> TRANSPORTS = Set.of("stdio", "sse", "streamable-http");

    private final ObjectMapper objectMapper;

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
        return entity;
    }

    public void update(AiMcpServerSaveDto dto) {
        AiMcpServer entity = getByIdOrThrow(dto.getId());
        validate(dto, entity.getId());
        if (nameExists(entity.getId(), dto.getName())) {
            throw new BizException("MCP 服务名称已存在");
        }

        copyToEntity(dto, entity);
        updateById(entity);
    }

    public void del(String id) {
        getByIdOrThrow(id);
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
        entity.setRemark(dto.getRemark());
    }
}
