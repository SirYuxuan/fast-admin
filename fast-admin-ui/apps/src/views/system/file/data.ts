import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridOptions } from '#/adapter/vxe-table';

import type { SystemFileApi } from '#/api/system/file';

export const STORAGE_TYPE_OPTIONS = [
  { label: '本地', value: 'LOCAL' },
  { label: '阿里云 OSS', value: 'OSS' },
  { label: 'AWS S3', value: 'S3' },
  { label: 'FTP', value: 'FTP' },
  { label: 'SFTP', value: 'SFTP' },
];

const STORAGE_TYPE_TAGS = STORAGE_TYPE_OPTIONS.map((o) => ({ ...o }));

export function formatSize(size?: number): string {
  if (size == null) return '-';
  if (size < 1024) return `${size} B`;
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
  if (size < 1024 * 1024 * 1024)
    return `${(size / 1024 / 1024).toFixed(1)} MB`;
  return `${(size / 1024 / 1024 / 1024).toFixed(2)} GB`;
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    { component: 'Input', fieldName: 'name', label: '文件名' },
    { component: 'Input', fieldName: 'ext', label: '扩展名' },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: STORAGE_TYPE_OPTIONS,
      },
      fieldName: 'storageType',
      label: '存储类型',
    },
    { component: 'Input', fieldName: 'bizType', label: '业务类型' },
  ];
}

export function useColumns<T = SystemFileApi.FileRecord>(
  onActionClick: OnActionClickFn<T>,
): VxeTableGridOptions['columns'] {
  return [
    { field: 'originalName', title: '文件名', minWidth: 220 },
    {
      field: 'size',
      title: '大小',
      width: 110,
      formatter: ({ cellValue }) => formatSize(cellValue as number),
    },
    { field: 'ext', title: '扩展名', width: 90 },
    { field: 'contentType', title: 'MIME', width: 160 },
    {
      cellRender: { name: 'CellTag', options: STORAGE_TYPE_TAGS },
      field: 'storageType',
      title: '存储',
      width: 110,
    },
    { field: 'bizType', title: '业务类型', width: 120 },
    { field: 'bizId', title: '业务ID', width: 140 },
    { field: 'createdAt', title: '上传时间', width: 180 },
    {
      align: 'center',
      cellRender: {
        attrs: { onClick: onActionClick },
        name: 'CellOperation',
        options: [
          { code: 'preview', text: '预览' },
          { code: 'download', text: '下载', authCode: 'system:file:download' },
          { code: 'delete', authCode: 'system:file:delete' },
        ],
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 200,
    },
  ];
}
