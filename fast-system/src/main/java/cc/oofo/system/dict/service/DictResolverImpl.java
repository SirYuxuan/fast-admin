package cc.oofo.system.dict.service;

import java.util.List;

import org.springframework.stereotype.Component;

import cc.oofo.framework.excel.DictResolver;
import cc.oofo.system.dict.entity.SysDictData;
import lombok.RequiredArgsConstructor;

/**
 * DictResolver 的实现（桥接 SysDictDataService）。
 *
 * 让 fast-framework 的 ExcelUtil 能用上字典转换，又不让 fast-framework 反向依赖 fast-system。
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Component
@RequiredArgsConstructor
public class DictResolverImpl implements DictResolver {

    private final SysDictDataService dictDataService;

    @Override
    public String resolveLabel(String dictType, String value) {
        if (dictType == null || value == null) return null;
        List<SysDictData> list = dictDataService.listByType(dictType);
        return list.stream()
                .filter(d -> value.equals(d.getDictValue()))
                .map(SysDictData::getDictLabel)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String resolveValue(String dictType, String label) {
        if (dictType == null || label == null) return null;
        List<SysDictData> list = dictDataService.listByType(dictType);
        return list.stream()
                .filter(d -> label.equals(d.getDictLabel()))
                .map(SysDictData::getDictValue)
                .findFirst()
                .orElse(null);
    }
}
