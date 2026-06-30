package cc.oofo.flow.form.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cc.oofo.flow.form.entity.FlowForm;
import cc.oofo.flow.form.entity.query.FlowFormQuery;
import cc.oofo.framework.core.service.BaseService;
import cc.oofo.framework.exception.BizException;

/**
 * 自定义表单服务。
 *
 * @author Sir丶雨轩
 */
@Service
@Transactional
public class FlowFormService extends BaseService<FlowForm> {

    public Page<FlowForm> page(FlowFormQuery query) {
        query.getQueryWrapper().orderByDesc("created_at");
        return page(query.getMPPage(), query.getQueryWrapper());
    }

    public void add(FlowForm data) {
        if (!StringUtils.hasText(data.getName())) {
            throw new BizException("表单名称不能为空");
        }
        if (!StringUtils.hasText(data.getFormKey())) {
            throw new BizException("表单标识不能为空");
        }
        if (keyExists(null, data.getFormKey())) {
            throw new BizException("表单标识已存在");
        }
        save(data);
    }

    public void edit(FlowForm data) {
        if (!StringUtils.hasText(data.getId())) {
            throw new BizException("ID 不能为空");
        }
        if (StringUtils.hasText(data.getFormKey()) && keyExists(data.getId(), data.getFormKey())) {
            throw new BizException("表单标识已存在");
        }
        updateById(data);
    }

    public void del(String id) {
        removeById(id);
    }

    /** 按 formKey 取表单（供发起 / 审批渲染）。 */
    public FlowForm getByKey(String formKey) {
        if (!StringUtils.hasText(formKey)) {
            return null;
        }
        return query().eq("form_key", formKey).last("limit 1").one();
    }

    private boolean keyExists(String excludeId, String key) {
        return query().ne(StringUtils.hasText(excludeId), "id", excludeId)
                .eq("form_key", key).exists();
    }
}
