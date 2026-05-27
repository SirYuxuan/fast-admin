package cc.oofo.system.user.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cc.oofo.framework.excel.ExcelUtil;
import cc.oofo.framework.excel.ImportResult;
import cc.oofo.framework.web.response.Ps;
import cc.oofo.framework.web.response.Rs;
import cc.oofo.system.log.annotation.OperationLog;
import cc.oofo.system.log.enums.BusinessType;
import cc.oofo.system.user.entity.SysUser;
import jakarta.servlet.http.HttpServletResponse;
import cc.oofo.system.user.dto.SysUserInfoDto;
import cc.oofo.system.user.dto.SysUserPasswordDto;
import cc.oofo.system.user.dto.SysUserProfileDto;
import cc.oofo.system.user.entity.dto.SysUserDto;
import cc.oofo.system.user.entity.query.SysUserQuery;
import cc.oofo.system.user.service.SysUserService;
import lombok.RequiredArgsConstructor;

/**
 * 系统用户控制器
 * 
 * @author Sir丶雨轩
 * @since 2025/11/13
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/system/user")
public class SysUserController {

    private final SysUserService userService;

    /**
     * 用户列表
     * 
     * @param query 查询参数
     * @return 结果
     */
    @GetMapping
    public Ps<SysUserDto> list(SysUserQuery query) {
        return Ps.ok(userService.listUsers(query), userService.countUsers(query));
    }

    /**
     * 添加用户
     * 
     * @param sysUserDto 用户信息
     * @return 结果
     */
    @PostMapping
    public Rs<Void> add(@RequestBody SysUserDto sysUserDto) {
        userService.add(sysUserDto);
        return Rs.ok();
    }

    /**
     * 修改用户
     * 
     * @param sysUserDto 用户信息
     * @return 结果
     */
    @PutMapping
    public Rs<Void> edit(@RequestBody SysUserDto sysUserDto) {
        userService.edit(sysUserDto);
        return Rs.ok();
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 结果
     */
    @DeleteMapping("/{id}")
    public Rs<Void> del(@PathVariable String id) {
        userService.del(id);
        return Rs.ok();
    }

    /**
     * 获取用户信息
     *
     * @return 结果
     */
    @GetMapping(path = "/info")
    public Rs<SysUserInfoDto> info() {
        return Rs.ok(userService.info());
    }

    /**
     * 更新当前登录用户的个人信息
     *
     * @param dto 个人信息
     * @return 结果
     */
    @PutMapping(path = "/profile")
    public Rs<Void> updateProfile(@RequestBody SysUserProfileDto dto) {
        userService.updateProfile(dto);
        return Rs.ok();
    }

    /**
     * 修改当前登录用户的头像
     *
     * @param avatar 头像 URL
     * @return 结果
     */
    @PutMapping(path = "/avatar")
    public Rs<Void> changeAvatar(@RequestParam String avatar) {
        userService.changeAvatar(avatar);
        return Rs.ok();
    }

    /**
     * 修改当前登录用户的密码
     *
     * @param dto 密码 DTO
     * @return 结果
     */
    @PutMapping(path = "/password")
    public Rs<Void> changePassword(@RequestBody SysUserPasswordDto dto) {
        userService.changePassword(dto);
        return Rs.ok();
    }

    // ============================================================
    // Excel 导入导出
    // ============================================================

    /** 导出用户列表 */
    @GetMapping("/export")
    @OperationLog(title = "用户管理", type = BusinessType.EXPORT)
    public void export(HttpServletResponse response, SysUserQuery query) {
        List<SysUser> data = userService.listForExport(query);
        ExcelUtil.export(response, "用户列表", SysUser.class, data);
    }

    /** 下载导入模板 */
    @GetMapping("/import/template")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtil.exportTemplate(response, "用户导入模板", SysUser.class);
    }

    /** 导入用户 */
    @PostMapping("/import")
    @OperationLog(title = "用户管理", type = BusinessType.IMPORT)
    public Rs<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) {
        ImportResult<SysUser> r = ExcelUtil.importData(file, SysUser.class);
        int added = userService.batchImport(r);
        Map<String, Object> resp = new HashMap<>();
        resp.put("totalRows", r.getTotalRows());
        resp.put("successCount", added);
        resp.put("parsedSuccessCount", r.getSuccessCount());
        resp.put("errorCount", r.getErrorCount());
        resp.put("addedCount", added);
        resp.put("errors", r.getErrors());
        return Rs.ok(resp);
    }

}
