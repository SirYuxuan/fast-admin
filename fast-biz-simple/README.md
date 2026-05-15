# fast-biz-simple

业务模块模板。复制本模块作为新业务的起点。

## 复制步骤

1. 拷贝整个 `fast-biz-simple` 目录，重命名为 `fast-biz-<your-name>`。
2. 修改新模块 `pom.xml` 中的 `<artifactId>`。
3. 在根 `pom.xml` 的 `<modules>` 与 `dependencyManagement` 中注册新模块。
4. 在 `fast-application/pom.xml` 中添加依赖。
5. 重命名包路径：`cc.oofo.biz.demo` → `cc.oofo.biz.<your-name>`，并把 `Demo*` 类和 `biz_demo` 表名按业务改掉。

## 标准结构

```
cc.oofo.biz.demo/
├── controller/  REST 接口
├── service/     业务逻辑（继承 BaseService）
├── mapper/      数据访问（继承 BaseMapper）
└── entity/      实体 + dto/ + query/
```

实体继承 `BaseEntity` 自动获得 id、审计字段、逻辑删除；查询实体继承 `BaseQuery` 配合 `@QueryField` 注解自动拼装 `QueryWrapper`。
