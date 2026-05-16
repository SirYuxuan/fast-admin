package cc.oofo.system.job.mapper;

import org.apache.ibatis.annotations.Mapper;

import cc.oofo.framework.core.mapper.BaseMapper;
import cc.oofo.system.job.entity.SysJob;

@Mapper
public interface SysJobMapper extends BaseMapper<SysJob> {
}
