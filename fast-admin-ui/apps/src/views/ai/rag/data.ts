import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridOptions } from '#/adapter/vxe-table';

import type { AiRagApi } from '#/api/ai/rag';

export const DOC_STATUS_TAGS = [
  { label: '待处理', value: 'pending', color: 'default' },
  { label: '索引中', value: 'indexing', color: 'blue' },
  { label: '已索引', value: 'indexed', color: 'green' },
  { label: '失败', value: 'failed', color: 'red' },
];

export function formatSize(size?: number): string {
  if (size == null) return '-';
  if (size < 1024) return `${size} B`;
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
  return `${(size / 1024 / 1024).toFixed(1)} MB`;
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    { component: 'Input', fieldName: 'name', label: '知识库名称' },
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
      label: '知识库名称',
      rules: 'required',
    },
    {
      component: 'Textarea',
      componentProps: { rows: 3 },
      fieldName: 'description',
      label: '描述',
    },
    {
      component: 'InputNumber',
      componentProps: { class: 'w-full', max: 4000, min: 100, style: { width: '100%' } },
      defaultValue: 800,
      fieldName: 'chunkSize',
      label: '切片长度',
    },
    {
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0, style: { width: '100%' } },
      defaultValue: 100,
      fieldName: 'chunkOverlap',
      label: '切片重叠',
    },
    {
      component: 'Input',
      componentProps: {
        placeholder: '例如 \\n\\n，支持 \\n、\\t',
      },
      defaultValue: '\\n\\n',
      fieldName: 'chunkDelimiter',
      label: '切片分隔符',
    },
    {
      component: 'Switch',
      componentProps: { class: 'w-auto' },
      defaultValue: true,
      fieldName: 'enabled',
      label: '启用',
    },
    {
      component: 'Textarea',
      componentProps: { rows: 2 },
      fieldName: 'remark',
      label: '备注',
    },
  ];
}

export function useColumns<T = AiRagApi.KnowledgeBase>(
  onActionClick: OnActionClickFn<T>,
  onEnabledChange?: (newVal: any, row: T) => PromiseLike<boolean | undefined>,
): VxeTableGridOptions['columns'] {
  return [
    { field: 'name', title: '知识库名称', minWidth: 160 },
    { field: 'description', title: '描述', minWidth: 220, showOverflow: true },
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
    { field: 'documentCount', title: '文档数', width: 90 },
    { field: 'chunkCount', title: '切片数', width: 90 },
    { field: 'lastIndexedAt', title: '最后索引', width: 180 },
    {
      align: 'center',
      cellRender: {
        attrs: { onClick: onActionClick },
        name: 'CellOperation',
        options: [
          { code: 'detail', text: '进入' },
          { code: 'edit', authCode: 'ai:rag:edit' },
          { code: 'delete', authCode: 'ai:rag:delete' },
        ],
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 180,
    },
  ];
}
