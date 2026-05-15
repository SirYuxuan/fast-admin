package cc.oofo.biz.demo.service;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cc.oofo.biz.demo.entity.Demo;
import cc.oofo.biz.demo.entity.dto.DemoSaveDto;
import cc.oofo.biz.demo.entity.query.DemoQuery;
import cc.oofo.framework.core.service.BaseService;
import cc.oofo.framework.exception.BizException;

/**
 * 示例 Service
 *
 * @author Sir丶雨轩
 */
@Service
@Transactional
public class DemoService extends BaseService<Demo> {

    public Page<Demo> page(DemoQuery query) {
        return page(query.getMPPage(), query.getQueryWrapper());
    }

    public List<Demo> list(DemoQuery query) {
        return list(query.getQueryWrapper());
    }

    public void add(DemoSaveDto dto) {
        Demo entity = new Demo();
        BeanUtils.copyProperties(dto, entity);
        if (entity.getIsEnabled() == null) {
            entity.setIsEnabled(true);
        }
        save(entity);
    }

    public void update(DemoSaveDto dto) {
        Demo existing = getById(dto.getId());
        if (existing == null) {
            throw new BizException("数据不存在");
        }
        Demo entity = new Demo();
        BeanUtils.copyProperties(dto, entity);
        updateById(entity);
    }

    public void del(String id) {
        if (getById(id) == null) {
            throw new BizException("数据不存在");
        }
        removeById(id);
    }

}
