import { requestClient } from '#/api/request';

export namespace FlowProcessApi {
  export interface Instance {
    [key: string]: any;
    id: string;
    name?: string;
    processDefinitionKey?: string;
    startUserName?: string;
    startTime?: string;
    endTime?: string;
    finished?: boolean;
  }
}

const Url = '/flow/process';

/** 发起流程 */
export function startProcess(processKey: string, variables: Record<string, any>) {
  return requestClient.post<string>(`${Url}/start`, { processKey, variables });
}

/** 我发起的 */
export function getMyInitiated(params: Record<string, any>) {
  return requestClient.get(`${Url}/initiated`, { params });
}

/** 实例详情（含变量） */
export function getInstanceDetail(instanceId: string) {
  return requestClient.get(`${Url}/${instanceId}`);
}

/** 撤销实例 */
export function cancelInstance(instanceId: string, reason?: string) {
  return requestClient.post(`${Url}/${instanceId}/cancel`, null, {
    params: { reason },
  });
}
