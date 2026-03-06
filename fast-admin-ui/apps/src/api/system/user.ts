import type { Recordable } from '@vben/types';

import { requestClient } from '#/api/request';

export namespace SysUserApi {
  export interface SysUser {
    [key: string]: any;
    id: string;
    name: string;
    permissions: string[];
    remark?: string;
    status: 0 | 1;
  }
}
const BaseUrl = '/system/user';

/**
 * 获取用户列表数据
 */
async function list(params: Recordable<any>) {
  return requestClient.get<Array<SysUserApi.SysUser>>(BaseUrl, {
    params,
  });
}

/**
 * 创建数据
 * @param data 用户数据
 */
async function add(data: Omit<SysUserApi.SysUser, 'id'>) {
  return requestClient.post(BaseUrl, data);
}

/**
 * 更新角色
 *
 * @param id 角色 ID
 * @param data 用户数据
 */
async function edit(data: Omit<SysUserApi.SysUser, 'id'>) {
  return requestClient.put(`${BaseUrl}`, data);
}

/**
 * 删除用户
 * @param id 用户 ID
 */
async function del(id: string) {
  return requestClient.delete(`${BaseUrl}/${id}`);
}

export { add, del, edit, list };
