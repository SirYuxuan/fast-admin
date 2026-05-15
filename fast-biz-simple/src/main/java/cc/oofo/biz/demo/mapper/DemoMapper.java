package cc.oofo.biz.demo.mapper;

import org.apache.ibatis.annotations.Mapper;

import cc.oofo.biz.demo.entity.Demo;
import cc.oofo.framework.core.mapper.BaseMapper;

/**
 * 示例 Mapper
 *
 * @author Sir丶雨轩
 */
@Mapper
public interface DemoMapper extends BaseMapper<Demo> {
}
