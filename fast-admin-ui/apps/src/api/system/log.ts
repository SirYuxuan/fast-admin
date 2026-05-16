import { requestClient } from '#/api/request';

export namespace SysLogApi {
  export interface OperationLog {
    [key: string]: any;
    id: string;
    title?: string;
    businessType?: string;
    method?: string;
    requestMethod?: string;
    username?: string;
    url?: string;
    ip?: string;
    requestParams?: string;
    responseResult?: string;
    status: number;
    errorMsg?: string;
    costTime?: number;
    createdAt: string;
  }

  export interface LoginLog {
    [key: string]: any;
    id: string;
    userId?: string;
    username?: string;
    ip?: string;
    location?: string;
    browser?: string;
    os?: string;
    device?: string;
    status: number;
    msg?: string;
    type?: string;
    createdAt: string;
  }
}

// 操作日志
export function getOperationLogPage(params: Record<string, any>) {
  return requestClient.get('/system/log/operation', { params });
}

export function getOperationLogDetail(id: string) {
  return requestClient.get<SysLogApi.OperationLog>(`/system/log/operation/${id}`);
}

export function deleteOperationLog(id: string) {
  return requestClient.delete(`/system/log/operation/${id}`);
}

export function cleanOperationLog() {
  return requestClient.delete('/system/log/operation/clean');
}

// 登录日志
export function getLoginLogPage(params: Record<string, any>) {
  return requestClient.get('/system/log/login', { params });
}

export function deleteLoginLog(id: string) {
  return requestClient.delete(`/system/log/login/${id}`);
}

export function cleanLoginLog() {
  return requestClient.delete('/system/log/login/clean');
}
