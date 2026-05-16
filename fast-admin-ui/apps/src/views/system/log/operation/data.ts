import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridOptions } from '#/adapter/vxe-table';

import type { SysLogApi } from '#/api/system/log';

export const BUSINESS_TYPE_OPTIONS = [
  { label: '其它', value: 'OTHER' },
  { label: '新增', value: 'CREATE' },
  { label: '修改', value: 'UPDATE' },
  { label: '删除', value: 'DELETE' },
  { label: '查询', value: 'QUERY' },
  { label: '授权', value: 'GRANT' },
  { label: '导入', value: 'IMPORT' },
  { label: '导出', value: 'EXPORT' },
  { label: '强退', value: 'FORCE_LOGOUT' },
  { label: '清空', value: 'CLEAN' },
];

const TYPE_COLORS: Record<string, string> = {
  CREATE: 'green',
  UPDATE: 'blue',
  DELETE: 'red',
  QUERY: 'cyan',
  GRANT: 'purple',
  IMPORT: 'gold',
  EXPORT: 'orange',
  FORCE_LOGOUT: 'volcano',
  CLEAN: 'magenta',
  OTHER: 'default',
};

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    { component: 'Input', fieldName: 'title', label: '操作模块' },
    {
      component: 'Select',
      componentProps: { allowClear: true, options: BUSINESS_TYPE_OPTIONS },
      fieldName: 'businessType',
      label: '业务类型',
    },
    { component: 'Input', fieldName: 'username', label: '操作人' },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: '成功', value: 1 },
          { label: '失败', value: 0 },
        ],
      },
      fieldName: 'status',
      label: '状态',
    },
  ];
}

export function useColumns<T = SysLogApi.OperationLog>(
  onActionClick: OnActionClickFn<T>,
): VxeTableGridOptions['columns'] {
  return [
    { field: 'title', title: '操作模块', minWidth: 120 },
    {
      field: 'businessType',
      title: '业务类型',
      width: 100,
      cellRender: {
        name: 'CellTag',
        options: BUSINESS_TYPE_OPTIONS.map((o) => ({
          ...o,
          color: TYPE_COLORS[o.value] || 'default',
        })),
      },
    },
    { field: 'method', title: '方法', minWidth: 200 },
    { field: 'requestMethod', title: 'HTTP', width: 80 },
    { field: 'url', title: '请求 URL', minWidth: 200 },
    { field: 'userId', title: '用户ID', width: 220 },
    { field: 'ip', title: 'IP', width: 140 },
    {
      field: 'status',
      title: '状态',
      width: 80,
      cellRender: {
        name: 'CellTag',
        options: [
          { label: '成功', value: 1, color: 'success' },
          { label: '失败', value: 0, color: 'error' },
        ],
      },
    },
    {
      field: 'costTime',
      title: '耗时',
      width: 90,
      formatter: ({ cellValue }) => (cellValue == null ? '-' : `${cellValue} ms`),
    },
    { field: 'createdAt', title: '操作时间', width: 180 },
    {
      align: 'center',
      cellRender: {
        attrs: { onClick: onActionClick },
        name: 'CellOperation',
        options: [
          { code: 'detail', text: '详情' },
          { code: 'delete', authCode: 'system:log:operation:delete' },
        ],
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 140,
    },
  ];
}
