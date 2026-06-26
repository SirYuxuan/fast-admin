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

const CONNECTION_TAGS = [
  { label: '已连接', value: true, color: 'green' },
  { label: '未连接', value: false, color: 'red' },
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

export function useFormSchema(
  onTransportChange?: (value: AiMcpApi.Transport) => void,
): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'name',
      label: '服务名称',
      rules: 'required',
    },
    {
      component: 'Select',
      componentProps: {
        class: 'w-full',
        onChange: onTransportChange,
        options: TRANSPORT_OPTIONS,
      },
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
      component: 'Switch',
      componentProps: { class: 'w-auto' },
      defaultValue: false,
      dependencies: {
        show: (values: Record<string, any>) => values.transport === 'sse',
        triggerFields: ['transport'],
      },
      fieldName: 'keepAlive',
      help: '仅 SSE：定时发送 ping 保持长连接，避免被反向代理/网关空闲断开',
      label: '连接保活',
    },
    {
      component: 'InputNumber',
      componentProps: {
        addonAfter: '秒',
        class: 'w-full',
        max: 3600,
        min: 5,
        precision: 0,
      },
      defaultValue: 30,
      dependencies: {
        rules: (values: Record<string, any>) =>
          values.transport === 'sse' && values.keepAlive ? 'required' : null,
        show: (values: Record<string, any>) =>
          values.transport === 'sse' && values.keepAlive === true,
        triggerFields: ['transport', 'keepAlive'],
      },
      fieldName: 'keepAliveInterval',
      help: '建议 15~60 秒，需小于中间链路的空闲超时',
      label: '保活间隔',
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
  onEnabledChange?: (newVal: any, row: T) => PromiseLike<boolean | undefined>,
): VxeTableGridOptions['columns'] {
  return [
    { field: 'name', title: '服务名称', minWidth: 160 },
    {
      cellRender: { name: 'CellTag', options: TRANSPORT_TAGS },
      field: 'transport',
      title: '传输类型',
      width: 150,
    },
    {
      cellRender: { name: 'CellTag', options: CONNECTION_TAGS },
      field: 'connected',
      title: '连接状态',
      width: 100,
    },
    { field: 'toolCount', title: '工具数', width: 80 },
    { field: 'promptCount', title: '提示词', width: 80 },
    { field: 'resourceCount', title: '资源数', width: 80 },
    {
      field: 'contextTokenCount',
      title: '上下文',
      width: 90,
      formatter: ({ cellValue }) => `${cellValue || 0} token`,
    },
    {
      cellRender: {
        attrs: { beforeChange: onEnabledChange },
        name: onEnabledChange ? 'CellSwitch' : 'CellTag',
        options: [
          { label: '启用', value: true, color: 'blue' },
          { label: '禁用', value: false, color: 'default' },
        ],
        props: { checkedValue: true, unCheckedValue: false },
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
          { code: 'detail', text: '详情' },
          {
            authCode: 'ai:mcp:edit',
            code: 'reload',
            disabled: (row: AiMcpApi.McpServer) => row.refreshing,
            loading: (row: AiMcpApi.McpServer) => row.refreshing,
            text: (row: AiMcpApi.McpServer) => row.refreshing ? '刷新中' : '刷新',
          },
          { code: 'edit', authCode: 'ai:mcp:edit' },
          { code: 'delete', authCode: 'ai:mcp:delete' },
        ],
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 220,
    },
  ];
}
