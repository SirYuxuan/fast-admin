import { requestClient } from '#/api/request';

export namespace FlowDefinitionApi {
  export interface Definition {
    [key: string]: any;
    id: string;
    key: string;
    name?: string;
    version: number;
    category?: string;
    deploymentId: string;
    suspended: boolean;
  }
}

const Url = '/flow/definition';

export function getDefinitionPage(params: Record<string, any>) {
  return requestClient.get(Url, { params });
}

export function getDefinitionXml(definitionId: string) {
  return requestClient.get<string>(`${Url}/${definitionId}/xml`);
}

/** 某流程的全部历史版本 */
export function getDefinitionVersions(key: string) {
  return requestClient.get<FlowDefinitionApi.Definition[]>(`${Url}/versions`, {
    params: { key },
  });
}

/** 可发起的流程（每个 key 最新激活版本） */
export function getStartableProcesses(name?: string) {
  return requestClient.get(`${Url}/startable`, { params: { name } });
}

/** 取流程发起表单 */
export function getStartForm(definitionId: string) {
  return requestClient.get<{ form?: string; formKey?: string }>(
    `${Url}/${definitionId}/start-form`,
  );
}

export function suspendDefinition(definitionId: string) {
  return requestClient.put(`${Url}/${definitionId}/suspend`);
}

export function activateDefinition(definitionId: string) {
  return requestClient.put(`${Url}/${definitionId}/activate`);
}

export function deleteDeployment(deploymentId: string, cascade = false) {
  return requestClient.delete(`${Url}/deployment/${deploymentId}`, {
    params: { cascade },
  });
}
