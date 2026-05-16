import { requestClient } from '#/api/request';

export namespace SysConfigApi {
  export interface Config {
    [key: string]: any;
    id: string;
    configName: string;
    configKey: string;
    configValue?: string;
    configType?: number; // 1系统内置 0自定义
    remark?: string;
    createdAt?: string;
  }
}

const Url = '/system/config';

export function getConfigPage(params: Record<string, any>) {
  return requestClient.get(Url, { params });
}

export function getConfigDetail(id: string) {
  return requestClient.get<SysConfigApi.Config>(`${Url}/${id}`);
}

export function createConfig(data: Partial<SysConfigApi.Config>) {
  return requestClient.post(Url, data);
}

export function updateConfig(data: Partial<SysConfigApi.Config>) {
  return requestClient.put(Url, data);
}

export function deleteConfig(id: string) {
  return requestClient.delete(`${Url}/${id}`);
}
