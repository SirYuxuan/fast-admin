package cc.oofo.framework.core.query.resolver;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import cc.oofo.framework.core.query.annotation.Operator;
import cc.oofo.framework.core.query.annotation.QueryField;
import cc.oofo.framework.core.query.annotation.ValueType;

/**
 * 解析 `@QueryField` 注解并把对应条件应用到 MyBatis-Plus 的 `QueryWrapper` 上。
 *
 * 设计要点：
 * - 反射读取子类字段（包含 private）并根据注解判断是否跳过空值
 * -
 * 支持常见操作符：EQ/NE/GT/GE/LT/LE/LIKE/LEFT_LIKE/RIGHT_LIKE/IN/NOT_IN/BETWEEN/IS_NULL/IS_NOT_NULL
 * - 支持简单的日期字符串解析（当 `type = ValueType.DATE` 且给出 `dateFormat`）
 * - 缓存类元数据以减少反射开销
 */
public final class QueryAnnotationResolver {

    private static final ConcurrentMap<Class<?>, List<FieldMeta>> CACHE = new ConcurrentHashMap<>();

    private QueryAnnotationResolver() {
    }

    public static void apply(Object queryObject, QueryWrapper<?> wrapper) {
        if (queryObject == null || wrapper == null)
            return;
        Class<?> cls = queryObject.getClass();
        List<FieldMeta> metas = CACHE.computeIfAbsent(cls, QueryAnnotationResolver::inspect);
        for (FieldMeta m : metas) {
            try {
                Object raw = m.field.get(queryObject);
                if (m.ann.ignoreNull()) {
                    if (raw == null)
                        continue;
                    if (raw instanceof String && ((String) raw).trim().isEmpty())
                        continue;
                    if (raw instanceof Collection && ((Collection<?>) raw).isEmpty())
                        continue;
                    if (raw.getClass().isArray() && Arrays.asList((Object[]) raw).isEmpty())
                        continue;
                }
                Object value = convertIfNeeded(raw, m.ann);
                String prop = m.propName.isEmpty() ? m.field.getName() : m.propName;
                applyCondition(wrapper, prop, m.ann, value);
            } catch (IllegalAccessException e) {
                // 忽略不可访问字段（理论上已设可访问）
            }
        }
    }

    /**
     * 将 query 对象中被 {@link cc.oofo.framework.core.query.annotation.QueryField}
     * 注解的字段解析并
     * 应用到传入的 {@link QueryWrapper} 上。
     *
     * 说明：
     * - 方法会读取缓存（按 query 对象 Class 缓存已解析的被注解字段）以减少反射开销。
     * - 若注解设置了 ignoreNull 且字段值为空/空集合/空字符串，则会跳过该字段。
     * - 对于日期类型的字符串，会根据注解提供的 dateFormat 做简单解析。
     *
     * @param queryObject 包含查询条件的对象（通常继承自 BaseQuery）
     * @param wrapper     MyBatis-Plus 的 {@link QueryWrapper}，解析器会直接在其上追加条件
     */

    private static Object convertIfNeeded(Object raw, QueryField ann) {
        if (raw == null)
            return null;
        if (ann.type() == ValueType.DATE) {
            String fmt = ann.dateFormat();
            DateTimeFormatter dtf = fmt == null || fmt.isEmpty() ? null : DateTimeFormatter.ofPattern(fmt);
            // 只做常见字符串到 LocalDate/LocalDateTime 的转换，返回原值其余情况
            if (raw instanceof String) {
                String s = ((String) raw).trim();
                try {
                    if (dtf != null) {
                        // 尝试 LocalDateTime 再 LocalDate
                        try {
                            return LocalDateTime.parse(s, dtf);
                        } catch (Exception ignored) {
                        }
                        return LocalDate.parse(s, dtf);
                    } else {
                        // 无格式时尝试 ISO
                        try {
                            return LocalDateTime.parse(s);
                        } catch (Exception ignored) {
                        }
                        try {
                            return LocalDate.parse(s);
                        } catch (Exception ignored) {
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            // 如果是数组（例如 BETWEEN 的范围），递归转换每个元素
            if (raw.getClass().isArray()) {
                Object[] arr = (Object[]) raw;
                List<Object> out = new ArrayList<>(arr.length);
                for (Object o : arr)
                    out.add(convertIfNeeded(o, ann));
                return out;
            }
        }
        return raw;
    }

    /**
     * 根据注解的操作符把条件追加到 {@link QueryWrapper} 上。
     *
     * 注意：
     * - 当注解的 {@code or=true} 时，使用 wrapper.or() 将该条件与之前条件用 OR 连接。
     * - 对于集合/数组类型的 IN/NOT_IN，方法会正确调用相应的 wrapper 方法。
     *
     * @param wrapper QueryWrapper 实例
     * @param prop    属性名（或列名，取决于注解中的 prop 字段约定）
     * @param ann     注解实例，包含 operator/or 等属性
     * @param value   要比较的值（可能已被 convertIfNeeded 转换）
     */

    private static void applyCondition(QueryWrapper<?> wrapper, String prop, QueryField ann, Object value) {
        Operator op = ann.operator();
        boolean useOr = ann.or();
        switch (op) {
            case EQ:
                if (useOr)
                    wrapper.or().eq(prop, value);
                else
                    wrapper.eq(prop, value);
                break;
            case NE:
                if (useOr)
                    wrapper.or().ne(prop, value);
                else
                    wrapper.ne(prop, value);
                break;
            case GT:
                if (useOr)
                    wrapper.or().gt(prop, value);
                else
                    wrapper.gt(prop, value);
                break;
            case GE:
                if (useOr)
                    wrapper.or().ge(prop, value);
                else
                    wrapper.ge(prop, value);
                break;
            case LT:
                if (useOr)
                    wrapper.or().lt(prop, value);
                else
                    wrapper.lt(prop, value);
                break;
            case LE:
                if (useOr)
                    wrapper.or().le(prop, value);
                else
                    wrapper.le(prop, value);
                break;
            case LIKE:
                if (value != null) {
                    if (useOr)
                        wrapper.or().like(prop, value.toString());
                    else
                        wrapper.like(prop, value.toString());
                }
                break;
            case LEFT_LIKE:
                if (value != null) {
                    if (useOr)
                        wrapper.or().likeLeft(prop, value.toString());
                    else
                        wrapper.likeLeft(prop, value.toString());
                }
                break;
            case RIGHT_LIKE:
                if (value != null) {
                    if (useOr)
                        wrapper.or().likeRight(prop, value.toString());
                    else
                        wrapper.likeRight(prop, value.toString());
                }
                break;
            case IN:
                if (value instanceof Collection) {
                    Collection<?> c = (Collection<?>) value;
                    if (useOr)
                        wrapper.or().in(prop, c);
                    else
                        wrapper.in(prop, c);
                } else if (value != null && value.getClass().isArray()) {
                    Object[] arr = (Object[]) value;
                    if (useOr)
                        wrapper.or().in(prop, arr);
                    else
                        wrapper.in(prop, arr);
                }
                break;
            case NOT_IN:
                if (value instanceof Collection) {
                    Collection<?> c = (Collection<?>) value;
                    if (useOr)
                        wrapper.or().notIn(prop, c);
                    else
                        wrapper.notIn(prop, c);
                } else if (value != null && value.getClass().isArray()) {
                    Object[] arr = (Object[]) value;
                    if (useOr)
                        wrapper.or().notIn(prop, arr);
                    else
                        wrapper.notIn(prop, arr);
                }
                break;
            case BETWEEN:
                if (value instanceof List) {
                    List<?> l = (List<?>) value;
                    if (l.size() >= 2 && Objects.nonNull(l.get(0)) && Objects.nonNull(l.get(1))) {
                        if (useOr)
                            wrapper.or().between(prop, l.get(0), l.get(1));
                        else
                            wrapper.between(prop, l.get(0), l.get(1));
                    }
                } else if (value != null && value.getClass().isArray()) {
                    Object[] arr = (Object[]) value;
                    if (arr.length >= 2 && Objects.nonNull(arr[0]) && Objects.nonNull(arr[1])) {
                        if (useOr)
                            wrapper.or().between(prop, arr[0], arr[1]);
                        else
                            wrapper.between(prop, arr[0], arr[1]);
                    }
                }
                break;
            case IS_NULL:
                if (useOr)
                    wrapper.or().isNull(prop);
                else
                    wrapper.isNull(prop);
                break;
            case IS_NOT_NULL:
                if (useOr)
                    wrapper.or().isNotNull(prop);
                else
                    wrapper.isNotNull(prop);
                break;
            default:
                break;
        }
    }

    private static List<FieldMeta> inspect(Class<?> cls) {
        List<FieldMeta> list = new ArrayList<>();
        Class<?> cur = cls;
        while (cur != null && !"java.lang.Object".equals(cur.getName())) {
            Field[] fields = cur.getDeclaredFields();
            for (Field f : fields) {
                QueryField ann = f.getAnnotation(QueryField.class);
                if (ann == null)
                    continue;
                try {
                    f.setAccessible(true);
                } catch (Exception ignored) {
                }
                String prop = ann.prop() == null ? "" : ann.prop();
                list.add(new FieldMeta(f, ann, prop));
            }
            cur = cur.getSuperclass();
        }
        return list;
    }

    /**
     * 扫描类及其父类的字段，收集所有带有 {@link QueryField} 注解的字段元信息。
     * 此方法仅用于构建缓存条目，返回的 FieldMeta 列表会被存入 CACHE。
     *
     * @param cls 要扫描的类
     * @return 被注解字段的元信息列表
     */

    private static class FieldMeta {
        final Field field;
        final QueryField ann;
        final String propName;

        FieldMeta(Field field, QueryField ann, String propName) {
            this.field = field;
            this.ann = ann;
            this.propName = propName == null ? "" : propName;
        }
    }
}
