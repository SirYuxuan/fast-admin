package cc.oofo.ai.mcp.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import cc.oofo.system.config.service.SysConfigService;
import lombok.RequiredArgsConstructor;

/**
 * MCP 运行期配置。
 */
@Service
@RequiredArgsConstructor
public class AiMcpSettingService {

    public static final String MCP_CLIENT_ENABLED = "ai.mcp.client.enabled";

    private static final boolean DEFAULT_MCP_CLIENT_ENABLED = true;

    private final SysConfigService sysConfigService;

    public boolean isClientEnabled() {
        String value = sysConfigService.getValue(MCP_CLIENT_ENABLED);
        if (!StringUtils.hasText(value)) {
            return DEFAULT_MCP_CLIENT_ENABLED;
        }
        return switch (value.trim().toLowerCase()) {
            case "1", "true", "yes", "on" -> true;
            case "0", "false", "no", "off" -> false;
            default -> DEFAULT_MCP_CLIENT_ENABLED;
        };
    }
}
