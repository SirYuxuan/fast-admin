import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridOptions } from '#/adapter/vxe-table';

import type { SysJobApi } from '#/api/system/job';

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    { component: 'Input', fieldName: 'jobName', label: '任务名称' },
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

export function useColumns<T = SysJobApi.JobLog>(
  onActionClick: OnActionClickFn<T>,
): VxeTableGridOptions['columns'] {
  return [
    { field: 'jobName', title: '任务名称', minWidth: 160 },
    { field: 'jobGroup', title: '分组', width: 110 },
    { field: 'beanName', title: 'Bean', minWidth: 140 },
    { field: 'methodName', title: '方法', width: 110 },
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
    { field: 'errorMsg', title: '错误信息', minWidth: 200, showOverflow: true },
    { field: 'createdAt', title: '执行时间', width: 180 },
    {
      align: 'center',
      cellRender: {
        attrs: { onClick: onActionClick },
        name: 'CellOperation',
        options: [{ code: 'delete' }],
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 100,
    },
  ];
}
