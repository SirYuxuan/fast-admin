import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridOptions } from '#/adapter/vxe-table';

import type { SysLogApi } from '#/api/system/log';

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    { component: 'Input', fieldName: 'username', label: '用户名' },
    { component: 'Input', fieldName: 'ip', label: 'IP' },
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
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: '登录', value: 'LOGIN' },
          { label: '登出', value: 'LOGOUT' },
        ],
      },
      fieldName: 'type',
      label: '类型',
    },
  ];
}

export function useColumns<T = SysLogApi.LoginLog>(
  onActionClick: OnActionClickFn<T>,
): VxeTableGridOptions['columns'] {
  return [
    { field: 'username', title: '用户名', width: 140 },
    { field: 'ip', title: 'IP', width: 140 },
    { field: 'location', title: '地点', width: 140 },
    { field: 'browser', title: '浏览器', width: 100 },
    { field: 'os', title: '操作系统', width: 130 },
    { field: 'device', title: '设备', width: 80 },
    {
      field: 'type',
      title: '类型',
      width: 80,
      cellRender: {
        name: 'CellTag',
        options: [
          { label: '登录', value: 'LOGIN', color: 'blue' },
          { label: '登出', value: 'LOGOUT', color: 'default' },
        ],
      },
    },
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
    { field: 'msg', title: '描述', minWidth: 180 },
    { field: 'createdAt', title: '时间', width: 180 },
    {
      align: 'center',
      cellRender: {
        attrs: { onClick: onActionClick },
        name: 'CellOperation',
        options: [{ code: 'delete', authCode: 'system:log:login:delete' }],
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 100,
    },
  ];
}
