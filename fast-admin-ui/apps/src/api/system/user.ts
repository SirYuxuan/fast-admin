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

  /** 个人中心 - 用户信息 */
  export interface UserInfo {
    id: string;
    username: string;
    nickname?: string;
    email?: string;
    phone?: string;
  }

  /** 个人中心 - 基本信息更新 */
  export interface ProfileUpdate {
    nickname?: string;
    email?: string;
    phone?: string;
  }

  /** 个人中心 - 修改密码 */
  export interface PasswordChange {
    oldPassword: string;
    newPassword: string;
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

/**
 * 获取当前登录用户的信息
 */
async function getUserProfile() {
  return requestClient.get<SysUserApi.UserInfo>(`${BaseUrl}/info`);
}

/**
 * 更新当前登录用户的个人信息（昵称/邮箱/手机号）
 */
async function updateUserProfile(data: SysUserApi.ProfileUpdate) {
  return requestClient.put(`${BaseUrl}/profile`, data);
}

/**
 * 修改当前登录用户的密码
 */
async function changeUserPassword(data: SysUserApi.PasswordChange) {
  return requestClient.put(`${BaseUrl}/password`, data);
}

export {
  add,
  changeUserPassword,
  del,
  edit,
  getUserProfile,
  list,
  updateUserProfile,
};
