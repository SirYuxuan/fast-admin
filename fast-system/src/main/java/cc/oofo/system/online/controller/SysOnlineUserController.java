package cc.oofo.system.online.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cc.oofo.framework.web.response.Rs;
import cc.oofo.system.log.annotation.OperationLog;
import cc.oofo.system.log.enums.BusinessType;
import cc.oofo.system.online.dto.OnlineUserDto;
import cc.oofo.system.online.service.SysOnlineUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "在线用户")
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/online")
public class SysOnlineUserController {

    private final SysOnlineUserService service;

    @Operation(summary = "在线用户列表")
    @GetMapping
    public Rs<List<OnlineUserDto>> list(@RequestParam(required = false) String keyword) {
        return Rs.ok(service.list(keyword));
    }

    @Operation(summary = "强制下线")
    @OperationLog(title = "在线用户", type = BusinessType.FORCE_LOGOUT)
    @DeleteMapping("/{tokenValue}")
    public Rs<Void> kickout(@PathVariable String tokenValue) {
        service.kickout(tokenValue);
        return Rs.ok();
    }
}
