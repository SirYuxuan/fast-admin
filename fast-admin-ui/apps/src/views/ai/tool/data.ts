import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridOptions } from '#/adapter/vxe-table';

import type { AiToolApi } from '#/api/ai/tool';

export const TOOL_TYPE_OPTIONS = [
  { label: 'SQL', value: 'sql' },
  { label: 'HTTP', value: 'http' },
];

const TOOL_TYPE_TAGS = [
  { label: 'SQL', value: 'sql', color: 'blue' },
  { label: 'HTTP', value: 'http', color: 'cyan' },
];

const HTTP_METHOD_OPTIONS = ['GET', 'POST', 'PUT', 'PATCH', 'DELETE'].map((value) => ({
  label: value,
  value,
}));

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    { component: 'Input', fieldName: 'name', label: '工具名称' },
    { component: 'Input', fieldName: 'toolCode', label: '工具编码' },
    {
      component: 'Select',
      componentProps: { allowClear: true, options: TOOL_TYPE_OPTIONS },
      fieldName: 'type',
      label: '类型',
    },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: '启用', value: true },
          { label: '禁用', value: false },
        ],
      },
      fieldName: 'enabled',
      label: '状态',
    },
  ];
}

export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'name',
      label: '工具名称',
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'toolCode',
      label: '工具编码',
      rules: 'required',
      help: '暴露给模型的 tool name，使用英文字母、数字、下划线，并以字母开头',
    },
    {
      component: 'Select',
      componentProps: { class: 'w-full', options: TOOL_TYPE_OPTIONS },
      fieldName: 'type',
      label: '类型',
      rules: 'selectRequired',
    },
    {
      component: 'Textarea',
      componentProps: { rows: 3 },
      fieldName: 'description',
      label: '工具说明',
      rules: 'required',
      help: '模型会根据这段说明判断何时调用工具',
    },
    {
      component: 'Input',
      fieldName: 'permissionCode',
      label: '调用权限',
      help: '为空表示登录用户均可调用；敏感工具建议填写菜单权限码',
    },
    {
      component: 'Textarea',
      componentProps: { rows: 5, placeholder: 'select * from sys_user where username = :username' },
      dependencies: {
        rules: (values: Record<string, any>) => (values.type === 'sql' ? 'required' : null),
        show: (values: Record<string, any>) => values.type === 'sql',
        triggerFields: ['type'],
      },
      fieldName: 'sqlText',
      label: 'SQL 模板',
      help: '使用 :param 命名参数，模型只能传参，不能临时拼 SQL',
    },
    {
      component: 'Switch',
      componentProps: { class: 'w-auto' },
      defaultValue: true,
      dependencies: {
        show: (values: Record<string, any>) => values.type === 'sql',
        triggerFields: ['type'],
      },
      fieldName: 'readOnly',
      label: '只读',
      help: '关闭后允许执行 update/insert/delete，务必配合权限码',
    },
    {
      component: 'Select',
      componentProps: { class: 'w-full', options: HTTP_METHOD_OPTIONS },
      defaultValue: 'GET',
      dependencies: {
        rules: (values: Record<string, any>) => (values.type === 'http' ? 'selectRequired' : null),
        show: (values: Record<string, any>) => values.type === 'http',
        triggerFields: ['type'],
      },
      fieldName: 'method',
      label: '请求方法',
    },
    {
      component: 'Input',
      dependencies: {
        rules: (values: Record<string, any>) => (values.type === 'http' ? 'required' : null),
        show: (values: Record<string, any>) => values.type === 'http',
        triggerFields: ['type'],
      },
      fieldName: 'url',
      label: '请求地址',
      help: '可使用 {{param}} 占位，例如 https://api.example.com/users/{{id}}',
    },
    {
      component: 'Textarea',
      componentProps: { rows: 3, placeholder: '{"Authorization":"Bearer {{token}}"}' },
      dependencies: {
        show: (values: Record<string, any>) => values.type === 'http',
        triggerFields: ['type'],
      },
      fieldName: 'headersJson',
      label: '请求头',
      help: '必须是 JSON 对象',
    },
    {
      component: 'Textarea',
      componentProps: { rows: 4 },
      dependencies: {
        show: (values: Record<string, any>) => values.type === 'http',
        triggerFields: ['type'],
      },
      fieldName: 'bodyTemplate',
      label: '请求体',
    },
    {
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 1000, max: 60000, step: 1000 },
      defaultValue: 10000,
      fieldName: 'timeoutMs',
      label: '超时毫秒',
    },
    {
      component: 'Switch',
      componentProps: { class: 'w-auto' },
      defaultValue: true,
      fieldName: 'enabled',
      label: '启用',
    },
    {
      component: 'Textarea',
      componentProps: { rows: 2 },
      fieldName: 'remark',
      label: '备注',
    },
  ];
}

export function useColumns<T = AiToolApi.ToolConfig>(
  onActionClick: OnActionClickFn<T>,
): VxeTableGridOptions['columns'] {
  return [
    { field: 'name', title: '工具名称', minWidth: 150 },
    { field: 'toolCode', title: '工具编码', minWidth: 160 },
    {
      cellRender: { name: 'CellTag', options: TOOL_TYPE_TAGS },
      field: 'type',
      title: '类型',
      width: 90,
    },
    { field: 'description', title: '说明', minWidth: 220, showOverflow: true },
    { field: 'permissionCode', title: '调用权限', minWidth: 160, showOverflow: true },
    {
      cellRender: {
        name: 'CellTag',
        options: [
          { label: '启用', value: true, color: 'blue' },
          { label: '禁用', value: false, color: 'default' },
        ],
      },
      field: 'enabled',
      title: '状态',
      width: 90,
    },
    { field: 'createdAt', title: '创建时间', width: 180 },
    {
      align: 'center',
      cellRender: {
        attrs: { onClick: onActionClick },
        name: 'CellOperation',
        options: [
          { code: 'edit', authCode: 'ai:tool:edit' },
          { code: 'delete', authCode: 'ai:tool:delete' },
        ],
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 140,
    },
  ];
}
