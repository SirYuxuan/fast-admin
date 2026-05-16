import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridOptions } from '#/adapter/vxe-table';

import type { SysConfigApi } from '#/api/system/config';

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    { component: 'Input', fieldName: 'configName', label: '参数名称' },
    { component: 'Input', fieldName: 'configKey', label: '参数键名' },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: '系统内置', value: 1 },
          { label: '自定义', value: 0 },
        ],
      },
      fieldName: 'configType',
      label: '类型',
    },
  ];
}

export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'configName',
      label: '参数名称',
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'configKey',
      label: '参数键名',
      rules: 'required',
      help: '系统全局唯一，例如 sys.user.initPassword',
    },
    {
      component: 'Textarea',
      componentProps: { rows: 3 },
      fieldName: 'configValue',
      label: '参数值',
      rules: 'required',
    },
    {
      component: 'RadioGroup',
      componentProps: {
        options: [
          { label: '自定义', value: 0 },
          { label: '系统内置', value: 1 },
        ],
      },
      defaultValue: 0,
      fieldName: 'configType',
      label: '类型',
    },
    {
      component: 'Textarea',
      componentProps: { rows: 2 },
      fieldName: 'remark',
      label: '备注',
    },
  ];
}

export function useColumns<T = SysConfigApi.Config>(
  onActionClick: OnActionClickFn<T>,
): VxeTableGridOptions['columns'] {
  return [
    { field: 'configName', title: '参数名称', minWidth: 180 },
    { field: 'configKey', title: '参数键名', minWidth: 200 },
    { field: 'configValue', title: '参数值', minWidth: 200, showOverflow: true },
    {
      field: 'configType',
      title: '类型',
      width: 100,
      cellRender: {
        name: 'CellTag',
        options: [
          { label: '系统内置', value: 1, color: 'blue' },
          { label: '自定义', value: 0, color: 'default' },
        ],
      },
    },
    { field: 'remark', title: '备注', minWidth: 160 },
    { field: 'createdAt', title: '创建时间', width: 180 },
    {
      align: 'center',
      cellRender: {
        attrs: { onClick: onActionClick },
        name: 'CellOperation',
        options: [
          { code: 'edit', authCode: 'system:config:edit' },
          { code: 'delete', authCode: 'system:config:delete' },
        ],
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 140,
    },
  ];
}
