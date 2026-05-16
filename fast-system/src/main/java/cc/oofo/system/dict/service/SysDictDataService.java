package cc.oofo.system.dict.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cc.oofo.framework.core.service.BaseService;
import cc.oofo.framework.exception.BizException;
import cc.oofo.system.dict.entity.SysDictData;
import cc.oofo.system.dict.entity.query.SysDictDataQuery;

/**
 * 字典数据服务
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Service
@Transactional
public class SysDictDataService extends BaseService<SysDictData> {

    public Page<SysDictData> page(SysDictDataQuery query) {
        query.getQueryWrapper().orderByAsc("dict_sort");
        return page(query.getMPPage(), query.getQueryWrapper());
    }

    /** 按 dictType 取所有可用字典（前端下拉用） */
    public List<SysDictData> listByType(String dictType) {
        return query().eq("dict_type", dictType)
                .eq("status", 1)
                .orderByAsc("dict_sort")
                .list();
    }

    public void add(SysDictData data) {
        if (!StringUtils.hasText(data.getDictType())) {
            throw new BizException("字典类型不能为空");
        }
        save(data);
    }

    public void edit(SysDictData data) {
        if (!StringUtils.hasText(data.getId())) {
            throw new BizException("ID 不能为空");
        }
        updateById(data);
    }
}
