import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridOptions } from '#/adapter/vxe-table';

import type { FlowModelApi } from '#/api/flow/model';

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    { component: 'Input', fieldName: 'name', label: '模型名称' },
    { component: 'Input', fieldName: 'modelKey', label: '流程标识' },
    { component: 'Input', fieldName: 'category', label: '分类' },
  ];
}

export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'name',
      label: '模型名称',
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'modelKey',
      label: '流程标识',
      rules: 'required',
      help: '部署后即流程定义 key，建议英文，全局唯一，例如 leave_apply',
    },
    {
      component: 'Input',
      fieldName: 'category',
      label: '分类',
    },
    {
      component: 'Textarea',
      componentProps: { rows: 3 },
      fieldName: 'description',
      label: '描述',
    },
  ];
}

export function useColumns<T = FlowModelApi.Model>(
  onActionClick: OnActionClickFn<T>,
): VxeTableGridOptions['columns'] {
  return [
    { field: 'name', title: '模型名称', minWidth: 160 },
    { field: 'modelKey', title: '流程标识', minWidth: 160 },
    { field: 'category', title: '分类', width: 120 },
    {
      field: 'latestDefinitionId',
      title: '部署状态',
      width: 110,
      cellRender: {
        name: 'CellTag',
        options: [
          { label: '已部署', value: 'deployed', color: 'green' },
          { label: '未部署', value: 'draft', color: 'default' },
        ],
      },
      formatter: ({ row }: { row: FlowModelApi.Model }) =>
        row.latestDefinitionId ? 'deployed' : 'draft',
    },
    { field: 'description', title: '描述', minWidth: 160, showOverflow: true },
    { field: 'createdAt', title: '创建时间', width: 180 },
    {
      align: 'center',
      cellRender: {
        attrs: { onClick: onActionClick },
        name: 'CellOperation',
        options: [
          { code: 'design', text: '设计', authCode: 'flow:model:design' },
          { code: 'deploy', text: '部署', authCode: 'flow:model:deploy' },
          { code: 'edit', authCode: 'flow:model:edit' },
          { code: 'delete', authCode: 'flow:model:delete' },
        ],
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 220,
    },
  ];
}
