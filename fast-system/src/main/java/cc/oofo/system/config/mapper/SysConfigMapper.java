package cc.oofo.system.config.mapper;

import org.apache.ibatis.annotations.Mapper;

import cc.oofo.framework.core.mapper.BaseMapper;
import cc.oofo.system.config.entity.SysConfig;

@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {
}
