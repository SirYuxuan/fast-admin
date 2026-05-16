package cc.oofo.system.config.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cc.oofo.framework.core.service.BaseService;
import cc.oofo.framework.exception.BizException;
import cc.oofo.system.config.entity.SysConfig;
import cc.oofo.system.config.entity.query.SysConfigQuery;

/**
 * 系统参数服务
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Service
@Transactional
public class SysConfigService extends BaseService<SysConfig> {

    public Page<SysConfig> page(SysConfigQuery query) {
        query.getQueryWrapper().orderByDesc("created_at");
        return page(query.getMPPage(), query.getQueryWrapper());
    }

    public void add(SysConfig data) {
        if (!StringUtils.hasText(data.getConfigKey())) {
            throw new BizException("参数键名不能为空");
        }
        if (keyExists(null, data.getConfigKey())) {
            throw new BizException("参数键名已存在");
        }
        save(data);
    }

    public void edit(SysConfig data) {
        if (!StringUtils.hasText(data.getId())) {
            throw new BizException("ID 不能为空");
        }
        if (keyExists(data.getId(), data.getConfigKey())) {
            throw new BizException("参数键名已存在");
        }
        updateById(data);
    }

    public void del(String id) {
        SysConfig cfg = getById(id);
        if (cfg == null) {
            return;
        }
        if (cfg.getConfigType() != null && cfg.getConfigType() == 1) {
            throw new BizException("系统内置参数不可删除");
        }
        removeById(id);
    }

    /** 根据 key 取参数值 */
    public String getValue(String key) {
        SysConfig cfg = query().eq("config_key", key).one();
        return cfg == null ? null : cfg.getConfigValue();
    }

    private boolean keyExists(String excludeId, String key) {
        return query().ne(StringUtils.hasText(excludeId), "id", excludeId)
                .eq("config_key", key).exists();
    }
}
