package cc.oofo.system.log.mapper;

import org.apache.ibatis.annotations.Mapper;

import cc.oofo.framework.core.mapper.BaseMapper;
import cc.oofo.system.log.entity.SysLoginLog;

@Mapper
public interface SysLoginLogMapper extends BaseMapper<SysLoginLog> {
}
