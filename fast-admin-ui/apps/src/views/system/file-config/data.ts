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

const STORAGE_TYPE_COLORS: Record<string, string> = {
  FTP: 'orange',
  LOCAL: 'default',
  OSS: 'blue',
  S3: 'cyan',
  SFTP: 'purple',
};

const STORAGE_TYPE_TAGS = STORAGE_TYPE_OPTIONS.map((o) => ({
  ...o,
  color: STORAGE_TYPE_COLORS[o.value] || 'default',
}));

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    { component: 'Input', fieldName: 'name', label: '配置名' },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: STORAGE_TYPE_OPTIONS,
      },
      fieldName: 'type',
      label: '存储类型',
    },
  ];
}

/**
 * 表单 schema:
 * - 基础字段（name/type/urlPrefix/remark）始终显示
 * - 每个存储类型的字段通过 dependencies.show 按 type 切换
 */
export function useFormSchema(): VbenFormSchema[] {
  const showWhen = (...types: string[]) => ({
    dependencies: {
      show: (values: Record<string, any>) => types.includes(values.type),
      triggerFields: ['type'],
    },
  });
  return [
    {
      component: 'Input',
      fieldName: 'name',
      label: '配置名',
      rules: 'required',
    },
    {
      component: 'Select',
      componentProps: {
        class: 'w-full',
        options: STORAGE_TYPE_OPTIONS,
        getPopupContainer: () => document.body,
      },
      fieldName: 'type',
      label: '存储类型',
      rules: 'selectRequired',
    },
    {
      component: 'Input',
      fieldName: 'urlPrefix',
      label: '访问地址前缀',
      help: '如 https://files.example.com（不含末尾斜杠）',
      rules: 'required',
    },

    // Endpoint：OSS 必填；S3 选填（兼容服务才填）
    {
      component: 'Input',
      componentProps: {
        placeholder: 'OSS 必填；S3 兼容服务（如 MinIO）才填，标准 AWS 留空',
      },
      dependencies: {
        rules: (values: Record<string, any>) =>
          values.type === 'OSS' ? 'required' : null,
        show: (values: Record<string, any>) =>
          ['OSS', 'S3'].includes(values.type),
        triggerFields: ['type'],
      },
      fieldName: 'config.endpoint',
      label: 'Endpoint',
    },
    {
      component: 'Input',
      fieldName: 'config.region',
      label: 'Region',
      ...showWhen('S3'),
    },
    {
      component: 'Input',
      fieldName: 'config.bucket',
      label: 'Bucket',
      rules: 'required',
      ...showWhen('OSS', 'S3'),
    },
    {
      component: 'Input',
      fieldName: 'config.accessKey',
      label: 'AccessKey',
      rules: 'required',
      ...showWhen('OSS', 'S3'),
    },
    {
      component: 'InputPassword',
      fieldName: 'config.secretKey',
      label: 'SecretKey',
      rules: 'required',
      ...showWhen('OSS', 'S3'),
    },
    {
      component: 'Switch',
      componentProps: { class: 'w-auto' },
      fieldName: 'config.pathStyleAccess',
      label: 'Path-style',
      help: 'MinIO 等需要开启',
      ...showWhen('S3'),
    },

    // FTP / SFTP
    {
      component: 'Input',
      fieldName: 'config.host',
      label: 'Host',
      rules: 'required',
      ...showWhen('FTP', 'SFTP'),
    },
    {
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 1, max: 65_535 },
      fieldName: 'config.port',
      label: 'Port',
      ...showWhen('FTP', 'SFTP'),
    },
    {
      component: 'Input',
      fieldName: 'config.username',
      label: '用户名',
      rules: 'required',
      ...showWhen('FTP', 'SFTP'),
    },
    {
      component: 'InputPassword',
      fieldName: 'config.password',
      label: '密码',
      ...showWhen('FTP', 'SFTP'),
    },

    // basePath：LOCAL/FTP/SFTP 必填；OSS/S3 选填（bucket 内前缀）
    {
      component: 'Input',
      dependencies: {
        componentProps: (values: Record<string, any>) => ({
          placeholder:
            values.type === 'LOCAL'
              ? '本地根目录，如 /data/uploads'
              : values.type === 'FTP' || values.type === 'SFTP'
                ? '远端根目录'
                : 'Bucket 内前缀（可选）',
        }),
        rules: (values: Record<string, any>) =>
          ['FTP', 'LOCAL', 'SFTP'].includes(values.type) ? 'required' : null,
        show: (values: Record<string, any>) =>
          ['FTP', 'LOCAL', 'OSS', 'S3', 'SFTP'].includes(values.type),
        triggerFields: ['type'],
      },
      fieldName: 'config.basePath',
      label: '根目录',
    },

    // FTP 专属
    {
      component: 'Switch',
      componentProps: { class: 'w-auto' },
      defaultValue: true,
      fieldName: 'config.passive',
      label: '被动模式',
      ...showWhen('FTP'),
    },

    // SFTP 专属
    {
      component: 'Textarea',
      componentProps: { rows: 4, placeholder: 'PEM 私钥内容（与密码二选一）' },
      fieldName: 'config.privateKey',
      label: '私钥',
      ...showWhen('SFTP'),
    },
    {
      component: 'InputPassword',
      fieldName: 'config.passphrase',
      label: '私钥口令',
      ...showWhen('SFTP'),
    },

    {
      component: 'Textarea',
      componentProps: { rows: 2 },
      fieldName: 'remark',
      label: '备注',
    },
  ];
}

export function useColumns<T = SystemFileApi.FileConfig>(
  onActionClick: OnActionClickFn<T>,
): VxeTableGridOptions['columns'] {
  return [
    { field: 'name', title: '配置名', minWidth: 160 },
    {
      cellRender: { name: 'CellTag', options: STORAGE_TYPE_TAGS },
      field: 'type',
      title: '存储类型',
      width: 120,
    },
    { field: 'urlPrefix', title: '访问地址前缀', minWidth: 240 },
    {
      cellRender: {
        name: 'CellTag',
        options: [
          { label: '激活', value: true, color: 'success' },
          { label: '未激活', value: false, color: 'default' },
        ],
      },
      field: 'isActive',
      title: '状态',
      width: 100,
    },
    { field: 'remark', title: '备注', minWidth: 160 },
    { field: 'createdAt', title: '创建时间', width: 180 },
    {
      align: 'center',
      cellRender: {
        attrs: { onClick: onActionClick },
        name: 'CellOperation',
        options: [
          { code: 'activate', text: '激活', authCode: 'system:file:config:activate' },
          { code: 'edit', authCode: 'system:file:config:edit' },
          { code: 'delete', authCode: 'system:file:config:delete' },
        ],
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 180,
    },
  ];
}
