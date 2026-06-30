package cc.oofo.flow.record.mapper;

import org.apache.ibatis.annotations.Mapper;

import cc.oofo.flow.record.entity.FlowTaskRecord;
import cc.oofo.framework.core.mapper.BaseMapper;

/**
 * 流程审批记录 Mapper。
 *
 * @author Sir丶雨轩
 */
@Mapper
public interface FlowTaskRecordMapper extends BaseMapper<FlowTaskRecord> {
}
