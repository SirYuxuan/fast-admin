package cc.oofo.system.file.controller;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cc.oofo.framework.exception.BizException;
import cc.oofo.framework.web.response.Ps;
import cc.oofo.framework.web.response.Rs;
import cc.oofo.system.file.entity.SysFile;
import cc.oofo.system.file.entity.query.SysFileQuery;
import cc.oofo.system.file.service.SysFileService;
import lombok.RequiredArgsConstructor;

/**
 * 文件控制器
 *
 * @author Sir丶雨轩
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/system/file")
public class SysFileController {

    private final SysFileService service;

    @GetMapping
    public Ps<SysFile> page(SysFileQuery query) {
        return Ps.ok(service.page(query));
    }

    @PostMapping(path = "/upload")
    public Rs<SysFile> upload(@RequestPart("file") MultipartFile file,
                              @RequestParam(required = false) String bizType,
                              @RequestParam(required = false) String bizId) {
        return Rs.ok(service.upload(file, bizType, bizId));
    }

    @GetMapping(path = "/{id}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable String id) {
        SysFile f = service.getById(id);
        if (f == null) {
            throw new BizException("文件不存在");
        }
        InputStream in = service.download(id);
        String filename = URLEncoder.encode(f.getOriginalName(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(in));
    }

    @DeleteMapping(path = "/{id}")
    public Rs<Void> del(@PathVariable String id) {
        service.del(id);
        return Rs.ok();
    }

}
