package cc.oofo.framework.web.response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.metadata.IPage;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分页数据返回
 * 
 * @author Sir丶雨轩
 * @since 2025/11/14
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Ps<T> extends Rs<T> {

    /**
     * 成功响应
     * 
     * @param <T>  数据类型
     * @param page 分页数据
     * @return 统一响应对象
     */
    public static <T> Ps<T> ok(IPage<T> page) {
        Ps<T> ps = new Ps<>();
        ps.setCode(HttpCode.SUCCESS);
        ps.setMessage("success");
        Map<String, Object> pageData = new HashMap<>();
        pageData.put("total", page.getTotal());
        pageData.put("items", page.getRecords());
        ps.setData(pageData);
        return ps;
    }

    /**
     * 成功响应
     * 
     * @param <T>   数据类型
     * @param items 数据列表
     * @param total 总数
     * @return 统一响应对象
     */
    public static <T> Ps<T> ok(List<T> items, long total) {
        Ps<T> ps = new Ps<>();
        ps.setCode(HttpCode.SUCCESS);
        ps.setMessage("success");
        Map<String, Object> pageData = new HashMap<>();
        pageData.put("total", total);
        pageData.put("items", items);
        ps.setData(pageData);
        return ps;
    }
}
