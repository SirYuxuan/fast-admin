import type { ModelType } from './data-types';

import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridOptions } from '#/adapter/vxe-table';

import { z } from '@vben/common-ui';

import { crudHelper, useCrud } from '#/api';

import { SysUserSex, SysUserStatus } from './data-types';

/**
 * API 基础路径
 */
export const BaseUrl = '/system/user';
/**
 * CRUD API 方法
 */
export const crud = useCrud<ModelType>(BaseUrl);
/**
 * 获取编辑表单的字段配置。
 * @returns 编辑表单的字段配置
 */
export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'username',
      label: '用户名',
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'nickname',
      label: '昵称',
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'email',
      label: '邮箱',
      rules: z.string().email('请输入有效的邮箱地址').optional(),
    },
    {
      component: 'Input',
      fieldName: 'phone',
      label: '手机号',
      rules: z
        .string()
        .regex(/^1[3-9]\d{9}$/, '请输入有效的手机号')
        .optional(),
    },
    {
      component: 'ApiTreeSelect',
      componentProps: {
        api: () => crudHelper.get('system/dept'),
        class: 'w-full',
        labelField: 'name',
        valueField: 'id',
        childrenField: 'children',
      },
      fieldName: 'deptId',
      label: '所属部门',
      rules: 'selectRequired',
    },
    {
      component: 'ApiSelect',
      componentProps: {
        api: () => crudHelper.get('system/role/select'),
        class: 'w-full',
        mode: 'multiple',
      },
      fieldName: 'roles',
      label: '角色',
      rules: 'selectRequired',
    },
    {
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        options: [
          { label: '男', value: SysUserSex.MALE },
          { label: '女', value: SysUserSex.FEMALE },
          { label: '未知', value: SysUserSex.UNKNOWN },
        ],
        optionType: 'button',
      },
      defaultValue: SysUserSex.UNKNOWN,
      fieldName: 'sex',
      label: '性别',
    },
    {
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        options: [
          { label: '正常', value: SysUserStatus.NORMAL },
          { label: '冻结', value: SysUserStatus.FROZEN },
          { label: '锁定', value: SysUserStatus.LOCKED },
        ],
        optionType: 'button',
      },
      defaultValue: SysUserStatus.NORMAL,
      fieldName: 'status',
      label: '状态',
    },
  ];
}

/**
 * 搜索表单字段配置
 * @returns 搜索表单字段配置
 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'username',
      label: '用户名',
    },
    {
      component: 'Input',
      fieldName: 'nickname',
      label: '昵称',
    },
    {
      component: 'Input',
      fieldName: 'email',
      label: '邮箱',
    },
    {
      component: 'Input',
      fieldName: 'phone',
      label: '手机号',
    },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: '男', value: SysUserSex.MALE },
          { label: '女', value: SysUserSex.FEMALE },
          { label: '未知', value: SysUserSex.UNKNOWN },
        ],
      },
      fieldName: 'sex',
      label: '性别',
    },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: '正常', value: SysUserStatus.NORMAL },
          { label: '冻结', value: SysUserStatus.FROZEN },
          { label: '锁定', value: SysUserStatus.LOCKED },
        ],
      },
      fieldName: 'status',
      label: '状态',
    },
    {
      component: 'RangePicker',
      fieldName: 'createTime',
      label: '创建时间',
    },
  ];
}

/**
 * 表格列配置
 * @param onActionClick  操作点击事件
 *
 * @returns 表格列配置
 */
export function useColumns<T = ModelType>(
  onActionClick: OnActionClickFn<T>,
): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'username',
      title: '用户名',
      width: 120,
    },
    {
      field: 'nickname',
      title: '昵称',
      width: 120,
    },
    {
      field: 'email',
      title: '邮箱',
      width: 180,
    },
    {
      field: 'phone',
      title: '手机号',
      width: 120,
    },
    {
      field: 'deptName',
      title: '部门名称',
      width: 150,
    },
    {
      cellRender: {
        name: 'CellTag',
        options: [
          { label: '男', value: SysUserSex.MALE, color: 'blue' },
          { label: '女', value: SysUserSex.FEMALE, color: 'pink' },
          { label: '未知', value: SysUserSex.UNKNOWN, color: 'default' },
        ],
      },
      field: 'sex',
      title: '性别',
      width: 100,
    },
    {
      cellRender: {
        name: 'CellTag',
        options: [
          { label: '正常', value: SysUserStatus.NORMAL, color: 'success' },
          { label: '冻结', value: SysUserStatus.FROZEN, color: 'warning' },
          { label: '锁定', value: SysUserStatus.LOCKED, color: 'error' },
        ],
      },
      field: 'status',
      title: '状态',
      width: 100,
    },
    {
      field: 'loginIp',
      title: '登录IP',
      width: 120,
    },
    {
      field: 'loginCity',
      title: '登录城市',
      width: 100,
    },
    {
      field: 'loginTime',
      title: '最后登录',
      width: 160,
    },
    {
      field: 'createTime',
      title: '创建时间',
      width: 160,
    },
    {
      align: 'center',
      cellRender: {
        attrs: {
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: [
          {
            code: 'edit',
          },
          {
            code: 'delete',
          },
        ],
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 130,
    },
  ];
}
