import type { VbenFormSchema } from '#/adapter/form';

export function useTypeFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'dictName',
      label: '字典名称',
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'dictType',
      label: '字典类型',
      rules: 'required',
      help: '英文编码，如 sys_user_sex',
    },
    {
      component: 'RadioGroup',
      componentProps: {
        options: [
          { label: '启用', value: 1 },
          { label: '禁用', value: 0 },
        ],
      },
      defaultValue: 1,
      fieldName: 'status',
      label: '状态',
    },
    {
      component: 'Textarea',
      componentProps: { rows: 2 },
      fieldName: 'remark',
      label: '备注',
    },
  ];
}

export function useDataFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'dictType',
      label: '字典类型',
      componentProps: { disabled: true },
    },
    {
      component: 'Input',
      fieldName: 'dictLabel',
      label: '字典标签',
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'dictValue',
      label: '字典键值',
      rules: 'required',
    },
    {
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0 },
      defaultValue: 0,
      fieldName: 'dictSort',
      label: '排序',
    },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: '默认', value: 'default' },
          { label: '成功', value: 'success' },
          { label: '处理中', value: 'processing' },
          { label: '警告', value: 'warning' },
          { label: '错误', value: 'error' },
        ],
      },
      fieldName: 'listClass',
      label: '回显样式',
    },
    {
      component: 'RadioGroup',
      componentProps: {
        options: [
          { label: '启用', value: 1 },
          { label: '禁用', value: 0 },
        ],
      },
      defaultValue: 1,
      fieldName: 'status',
      label: '状态',
    },
    {
      component: 'Textarea',
      componentProps: { rows: 2 },
      fieldName: 'remark',
      label: '备注',
    },
  ];
}
