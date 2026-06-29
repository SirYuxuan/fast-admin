import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridOptions } from '#/adapter/vxe-table';

import type { AiModelApi } from '#/api/ai/model';

export const PROVIDER_OPTIONS = [
  { label: 'Anthropic', value: 'anthropic' },
  { label: 'OpenAI', value: 'openai' },
  { label: 'OpenAI 兼容', value: 'openai-compatible' },
];

const PROVIDER_TAGS = [
  { label: 'Anthropic', value: 'anthropic', color: 'purple' },
  { label: 'OpenAI', value: 'openai', color: 'green' },
  { label: 'OpenAI 兼容', value: 'openai-compatible', color: 'blue' },
];

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    { component: 'Input', fieldName: 'name', label: '配置名称' },
    {
      component: 'Select',
      componentProps: { allowClear: true, options: PROVIDER_OPTIONS },
      fieldName: 'provider',
      label: '提供方',
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

export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'name',
      label: '配置名称',
      rules: 'required',
    },
    {
      component: 'Select',
      componentProps: { class: 'w-full', options: PROVIDER_OPTIONS },
      fieldName: 'provider',
      label: '提供方',
      rules: 'selectRequired',
    },
    {
      component: 'Input',
      fieldName: 'baseUrl',
      label: 'Base URL',
      help: 'OpenAI 兼容模型填写，例如 DeepSeek/Qwen 网关地址；Anthropic 可留空',
    },
    {
      component: 'InputPassword',
      fieldName: 'apiKey',
      label: 'API Key',
      rules: 'required',
    },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        class: 'w-full',
        options: [],
        placeholder: '可点击下方「获取模型列表」拉取后选择',
        showSearch: true,
      },
      fieldName: 'model',
      label: '模型名称',
      rules: 'required',
    },
    {
      component: 'InputNumber',
      componentProps: {
        class: 'w-full',
        max: 2,
        min: 0,
        step: 0.1,
        style: { width: '100%' },
      },
      defaultValue: 0.7,
      fieldName: 'temperature',
      label: 'Temperature',
    },
    {
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 1, style: { width: '100%' } },
      fieldName: 'maxTokens',
      label: 'Max Tokens',
    },
    {
      component: 'Switch',
      componentProps: { class: 'w-auto' },
      defaultValue: true,
      fieldName: 'enabled',
      label: '启用',
    },
    {
      component: 'Switch',
      componentProps: { class: 'w-auto' },
      fieldName: 'active',
      label: '设为当前模型',
      help: '保存后会取消其它模型的当前状态',
    },
    {
      component: 'Textarea',
      componentProps: { rows: 2 },
      fieldName: 'remark',
      label: '备注',
    },
  ];
}

export function useColumns<T = AiModelApi.ModelConfig>(
  onActionClick: OnActionClickFn<T>,
  onEnabledChange?: (newVal: any, row: T) => PromiseLike<boolean | undefined>,
  onActiveChange?: (newVal: any, row: T) => PromiseLike<boolean | undefined>,
): VxeTableGridOptions['columns'] {
  return [
    { field: 'name', title: '配置名称', minWidth: 160 },
    {
      cellRender: { name: 'CellTag', options: PROVIDER_TAGS },
      field: 'provider',
      title: '提供方',
      width: 130,
    },
    { field: 'model', title: '模型名称', minWidth: 180 },
    { field: 'baseUrl', title: 'Base URL', minWidth: 220, showOverflow: true },
    {
      cellRender: {
        attrs: { beforeChange: onActiveChange },
        name: onActiveChange ? 'CellSwitch' : 'CellTag',
        options: [
          { label: '当前', value: true, color: 'green' },
          { label: '备用', value: false, color: 'default' },
        ],
        props: { checkedValue: true, unCheckedValue: false },
      },
      field: 'active',
      title: '当前',
      width: 90,
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
    {
      field: 'lastLatencyMs',
      title: '上次测试延时',
      width: 130,
      slots: { default: 'latency' },
    },
    { field: 'createdAt', title: '创建时间', width: 180 },
    {
      align: 'center',
      cellRender: {
        attrs: { onClick: onActionClick },
        name: 'CellOperation',
        options: [
          { code: 'edit', authCode: 'ai:model:edit' },
          { code: 'delete', authCode: 'ai:model:delete' },
        ],
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 140,
    },
  ];
}
