import type { Recordable } from '@vben/types';

import { requestClient } from '#/api/request';

/**
 * CRUD 操作接口
 */
export interface CrudInterface<T = any> {
  /**
   * 获取列表数据
   */
  list: (params?: Recordable<any>) => Promise<T[]>;
  /**
   * 创建数据
   */
  add: (data: Omit<T, 'id'>) => Promise<any>;
  /**
   * 更新数据
   */
  edit: (data: Partial<T> & { id: string }) => Promise<any>;
  /**
   * 删除数据
   */
  del: (id: string) => Promise<any>;
  /**
   * 根据ID获取详情
   */
  getById: (id: string) => Promise<T>;
  /**
   * 获取数据
   * @param path 路径
   * @param params 查询参数
   */
  get: <R = T[]>(path: string, params?: Recordable<any>) => Promise<R>;
}

/**
 * 通用 CRUD Hook
 * @param baseUrl API 基础路径
 * @returns CRUD 操作对象
 */
export function useCrud<T = any>(baseUrl: string): CrudInterface<T> {
  return {
    /**
     * 获取列表数据
     */
    async list(params?: Recordable<any>) {
      return requestClient.get<T[]>(baseUrl, {
        params,
      });
    },

    /**
     * 创建数据
     * @param data 创建数据
     */
    async add(data: Omit<T, 'id'>) {
      return requestClient.post(baseUrl, data);
    },

    /**
     * 更新数据
     * @param data 包含id的更新数据
     */
    async edit(data: Partial<T> & { id: string }) {
      return requestClient.put(`${baseUrl}`, data);
    },

    /**
     * 删除数据
     * @param id 数据 ID
     */
    async del(id: string) {
      return requestClient.delete(`${baseUrl}/${id}`);
    },

    /**
     * 根据ID获取详情
     * @param id 数据 ID
     */
    async getById(id: string) {
      return requestClient.get<T>(`${baseUrl}/${id}`);
    },
    /**
     * 获取数据（支持自定义返回类型）
     * @param path 路径
     * @param params 查询参数
     * @returns 返回指定类型的数据
     */
    async get<R = T[]>(path: string, params?: Recordable<any>): Promise<R> {
      return requestClient.get<R>(`${baseUrl}/${path}`, {
        params,
      });
    },
  };
}

/**
 * CRUD 辅助工具
 */
export const crudHelper = {
  /**
   * 获取数据（支持自定义返回类型）
   * @param url 完整的 API 路径
   * @param params 查询参数
   * @returns 返回指定类型的数据
   */
  async get<R = any>(url: string, params?: Recordable<any>): Promise<R> {
    return requestClient.get<R>(url, {
      params,
    });
  },
};

// 使用示例：
// export namespace SysUserApi {
//   export interface SysUser {
//     [key: string]: any;
//     id: string;
//     name: string;
//     permissions: string[];
//     remark?: string;
//     status: 0 | 1;
//   }
// }

// 使用方式：
// const userCrud = useCrud<SysUserApi.SysUser>('/system/user');
// userCrud.list({ page: 1, pageSize: 10 });
// userCrud.add({ name: 'test', status: 1 });
// userCrud.edit('123', { name: 'updated' });
// userCrud.del('123');
