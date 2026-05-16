import { requestClient } from '#/api/request';

export namespace SysMonitorApi {
  export interface ServerInfo {
    cpu?: Record<string, any>;
    memory?: Record<string, any>;
    server?: Record<string, any>;
    disks?: Array<Record<string, any>>;
    jvm?: Record<string, any>;
    redis?: Record<string, any>;
    datasource?: Record<string, any>;
  }
}

/** 获取服务器监控全量信息 */
export function getServerMonitor() {
  return requestClient.get<SysMonitorApi.ServerInfo>('/system/monitor/server');
}
