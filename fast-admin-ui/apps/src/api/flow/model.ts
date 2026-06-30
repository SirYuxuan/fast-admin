import { requestClient } from '#/api/request';

export namespace FlowModelApi {
  export interface Model {
    [key: string]: any;
    id: string;
    name: string;
    modelKey: string;
    category?: string;
    bpmnXml?: string;
    description?: string;
    latestDefinitionId?: string;
    status?: number;
    createdAt?: string;
  }
}

const Url = '/flow/model';

export function getModelPage(params: Record<string, any>) {
  return requestClient.get(Url, { params });
}

export function getModelDetail(id: string) {
  return requestClient.get<FlowModelApi.Model>(`${Url}/${id}`);
}

export function createModel(data: Partial<FlowModelApi.Model>) {
  return requestClient.post(Url, data);
}

export function updateModel(data: Partial<FlowModelApi.Model>) {
  return requestClient.put(Url, data);
}

export function deleteModel(id: string) {
  return requestClient.delete(`${Url}/${id}`);
}

/** 保存设计器产出的 BPMN XML */
export function saveModelBpmn(id: string, bpmnXml: string) {
  return requestClient.put(`${Url}/${id}/bpmn`, { bpmnXml });
}

/** 部署模型，返回新流程定义 ID */
export function deployModel(id: string) {
  return requestClient.post<string>(`${Url}/${id}/deploy`);
}
