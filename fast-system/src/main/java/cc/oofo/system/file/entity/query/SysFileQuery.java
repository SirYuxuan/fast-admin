package cc.oofo.system.file.entity.query;

import cc.oofo.framework.core.entity.BaseQuery;
import cc.oofo.framework.core.query.annotation.Operator;
import cc.oofo.framework.core.query.annotation.QueryField;
import cc.oofo.system.file.entity.SysFile;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件查询
 *
 * @author Sir丶雨轩
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysFileQuery extends BaseQuery<SysFile> {

    @QueryField(operator = Operator.LIKE, prop = "original_name")
    private String name;

    private String storageType;
    private String bizType;
    private String bizId;
    private String ext;

}
