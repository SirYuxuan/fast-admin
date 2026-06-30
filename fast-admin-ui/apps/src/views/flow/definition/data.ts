import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridOptions } from '#/adapter/vxe-table';

import type { FlowDefinitionApi } from '#/api/flow/definition';

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    { component: 'Input', fieldName: 'name', label: '流程名称' },
    { component: 'Input', fieldName: 'key', label: '流程标识' },
  ];
}

export function useColumns<T = FlowDefinitionApi.Definition>(
  onActionClick: OnActionClickFn<T>,
): VxeTableGridOptions['columns'] {
  return [
    { field: 'name', title: '流程名称', minWidth: 160 },
    { field: 'key', title: '流程标识', minWidth: 160 },
    { field: 'version', title: '版本', width: 90, formatter: ({ row }) => `v${row.version}` },
    { field: 'category', title: '分类', width: 120 },
    {
      field: 'suspended',
      title: '状态',
      width: 100,
      cellRender: {
        name: 'CellTag',
        options: [
          { label: '激活', value: false, color: 'green' },
          { label: '挂起', value: true, color: 'red' },
        ],
      },
    },
    {
      align: 'center',
      cellRender: {
        attrs: { onClick: onActionClick },
        name: 'CellOperation',
        options: [
          { code: 'versions', text: '历史版本' },
          {
            code: 'suspend',
            text: '挂起',
            authCode: 'flow:definition:suspend',
            disabled: (row: FlowDefinitionApi.Definition) => row.suspended,
          },
          {
            code: 'activate',
            text: '激活',
            authCode: 'flow:definition:suspend',
            disabled: (row: FlowDefinitionApi.Definition) => !row.suspended,
          },
          {
            code: 'delete',
            authCode: 'flow:definition:delete',
          },
        ],
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 280,
    },
  ];
}
