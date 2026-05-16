package cc.oofo.system.dict.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cc.oofo.framework.core.service.BaseService;
import cc.oofo.framework.exception.BizException;
import cc.oofo.system.dict.entity.SysDictData;
import cc.oofo.system.dict.entity.SysDictType;
import cc.oofo.system.dict.entity.query.SysDictTypeQuery;
import cc.oofo.system.dict.mapper.SysDictDataMapper;
import lombok.RequiredArgsConstructor;

/**
 * 字典类型服务
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Service
@Transactional
@RequiredArgsConstructor
public class SysDictTypeService extends BaseService<SysDictType> {

    private final SysDictDataMapper sysDictDataMapper;

    public Page<SysDictType> page(SysDictTypeQuery query) {
        query.getQueryWrapper().orderByDesc("created_at");
        return page(query.getMPPage(), query.getQueryWrapper());
    }

    public void add(SysDictType data) {
        if (!StringUtils.hasText(data.getDictType())) {
            throw new BizException("字典类型不能为空");
        }
        if (typeExists(null, data.getDictType())) {
            throw new BizException("字典类型已存在");
        }
        save(data);
    }

    public void edit(SysDictType data) {
        if (!StringUtils.hasText(data.getId())) {
            throw new BizException("ID 不能为空");
        }
        if (typeExists(data.getId(), data.getDictType())) {
            throw new BizException("字典类型已存在");
        }
        updateById(data);
    }

    public void del(String id) {
        SysDictType t = getById(id);
        if (t == null) return;
        // 同时删除字典数据
        sysDictDataMapper.delete(new QueryWrapper<SysDictData>().eq("dict_type", t.getDictType()));
        removeById(id);
    }

    private boolean typeExists(String excludeId, String type) {
        return query().ne(StringUtils.hasText(excludeId), "id", excludeId)
                .eq("dict_type", type).exists();
    }
}
