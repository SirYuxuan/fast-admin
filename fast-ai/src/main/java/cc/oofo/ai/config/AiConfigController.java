package cc.oofo.ai.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cc.oofo.framework.web.response.Rs;
import cc.oofo.system.log.annotation.OperationLog;
import cc.oofo.system.log.enums.BusinessType;
import lombok.RequiredArgsConstructor;

/**
 * AI 运维配置接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/config")
public class AiConfigController {

    private final AiConfigService service;

    @GetMapping
    public Rs<AiConfigDto> current() {
        return Rs.ok(service.current());
    }

    @PutMapping
    @OperationLog(title = "AI 配置", type = BusinessType.UPDATE)
    public Rs<Void> save(@RequestBody AiConfigDto dto) {
        service.save(dto);
        return Rs.ok();
    }
}
