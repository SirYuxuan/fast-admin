import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridOptions } from '#/adapter/vxe-table';

import type { AiToolLogApi } from '#/api/ai/tool-log';

const SOURCE_OPTIONS = [
  { label: '内置工具', value: 'builtin' },
  { label: 'MCP', value: 'mcp' },
];

const SOURCE_TAGS = [
  { label: '内置工具', value: 'builtin', color: 'blue' },
  { label: 'MCP', value: 'mcp', color: 'cyan' },
];

const SUCCESS_TAGS = [
  { label: '成功', value: true, color: 'green' },
  { label: '失败', value: false, color: 'red' },
];

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    { component: 'Input', fieldName: 'toolName', label: '工具名称' },
    {
      component: 'Select',
      componentProps: { allowClear: true, options: SOURCE_OPTIONS },
      fieldName: 'source',
      label: '来源',
    },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: '成功', value: true },
          { label: '失败', value: false },
        ],
      },
      fieldName: 'success',
      label: '结果',
    },
  ];
}

export function useColumns<T = AiToolLogApi.ToolCallLog>(
  onActionClick: OnActionClickFn<T>,
): VxeTableGridOptions['columns'] {
  return [
    { field: 'toolName', title: '工具名称', minWidth: 180 },
    {
      cellRender: { name: 'CellTag', options: SOURCE_TAGS },
      field: 'source',
      title: '来源',
      width: 110,
    },
    {
      cellRender: { name: 'CellTag', options: SUCCESS_TAGS },
      field: 'success',
      title: '结果',
      width: 90,
    },
    {
      field: 'costMs',
      title: '耗时',
      width: 100,
      formatter: ({ cellValue }) => (cellValue == null ? '-' : `${cellValue} ms`),
    },
    { field: 'errorMsg', title: '错误信息', minWidth: 160 },
    { field: 'operatorId', title: '操作人', width: 140 },
    { field: 'sessionId', title: '会话 ID', minWidth: 180 },
    { field: 'createdAt', title: '调用时间', width: 180 },
    {
      align: 'center',
      cellRender: {
        attrs: { onClick: onActionClick },
        name: 'CellOperation',
        options: [{ code: 'detail', text: '详情' }],
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 90,
    },
  ];
}
