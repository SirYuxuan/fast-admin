import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridOptions } from '#/adapter/vxe-table';

import type { AiMcpApi } from '#/api/ai/mcp';

export const TRANSPORT_OPTIONS = [
  { label: 'stdio', value: 'stdio' },
  { label: 'SSE', value: 'sse' },
  { label: 'Streamable HTTP', value: 'streamable-http' },
];

const TRANSPORT_TAGS = [
  { label: 'stdio', value: 'stdio', color: 'default' },
  { label: 'SSE', value: 'sse', color: 'blue' },
  { label: 'Streamable HTTP', value: 'streamable-http', color: 'cyan' },
];

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    { component: 'Input', fieldName: 'name', label: '服务名称' },
    {
      component: 'Select',
      componentProps: { allowClear: true, options: TRANSPORT_OPTIONS },
      fieldName: 'transport',
      label: '传输类型',
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
      label: '服务名称',
      rules: 'required',
    },
    {
      component: 'Select',
      componentProps: { class: 'w-full', options: TRANSPORT_OPTIONS },
      fieldName: 'transport',
      label: '传输类型',
      rules: 'selectRequired',
    },
    {
      component: 'Input',
      dependencies: {
        rules: (values: Record<string, any>) =>
          values.transport === 'stdio' ? 'required' : null,
        show: (values: Record<string, any>) => values.transport === 'stdio',
        triggerFields: ['transport'],
      },
      fieldName: 'command',
      label: '启动命令',
    },
    {
      component: 'Input',
      dependencies: {
        rules: (values: Record<string, any>) =>
          values.transport && values.transport !== 'stdio' ? 'required' : null,
        show: (values: Record<string, any>) =>
          values.transport && values.transport !== 'stdio',
        triggerFields: ['transport'],
      },
      fieldName: 'url',
      label: '服务地址',
    },
    {
      component: 'Textarea',
      componentProps: { rows: 3, placeholder: '例如 [\"--port\", \"3000\"]' },
      fieldName: 'argsJson',
      label: '命令参数',
      help: 'stdio 模式使用，必须是 JSON 数组',
    },
    {
      component: 'Textarea',
      componentProps: { rows: 3, placeholder: '例如 {\"Authorization\":\"Bearer xxx\"}' },
      fieldName: 'headersJson',
      label: '请求头',
      help: '远程 MCP 使用，必须是 JSON 对象',
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

export function useColumns<T = AiMcpApi.McpServer>(
  onActionClick: OnActionClickFn<T>,
): VxeTableGridOptions['columns'] {
  return [
    { field: 'name', title: '服务名称', minWidth: 160 },
    {
      cellRender: { name: 'CellTag', options: TRANSPORT_TAGS },
      field: 'transport',
      title: '传输类型',
      width: 150,
    },
    { field: 'command', title: '启动命令', minWidth: 180, showOverflow: true },
    { field: 'url', title: '服务地址', minWidth: 220, showOverflow: true },
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
    { field: 'remark', title: '备注', minWidth: 160 },
    { field: 'createdAt', title: '创建时间', width: 180 },
    {
      align: 'center',
      cellRender: {
        attrs: { onClick: onActionClick },
        name: 'CellOperation',
        options: [
          { code: 'edit', authCode: 'ai:mcp:edit' },
          { code: 'delete', authCode: 'ai:mcp:delete' },
        ],
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 140,
    },
  ];
}
