import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridOptions } from '#/adapter/vxe-table';

import type { FlowFormApi } from '#/api/flow/form';

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    { component: 'Input', fieldName: 'name', label: '表单名称' },
    { component: 'Input', fieldName: 'formKey', label: '表单标识' },
  ];
}

export function useColumns<T = FlowFormApi.Form>(
  onActionClick: OnActionClickFn<T>,
): VxeTableGridOptions['columns'] {
  return [
    { field: 'name', title: '表单名称', minWidth: 160 },
    { field: 'formKey', title: '表单标识', minWidth: 160 },
    { field: 'remark', title: '备注', minWidth: 180, showOverflow: true },
    { field: 'createdAt', title: '创建时间', width: 180 },
    {
      align: 'center',
      cellRender: {
        attrs: { onClick: onActionClick },
        name: 'CellOperation',
        options: [
          { code: 'edit', text: '设计', authCode: 'flow:form:edit' },
          { code: 'delete', authCode: 'flow:form:delete' },
        ],
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 140,
    },
  ];
}
