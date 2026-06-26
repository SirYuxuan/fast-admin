import { requestClient } from '#/api/request';

export namespace AiModelApi {
  export type Provider = 'anthropic' | 'openai' | 'openai-compatible';

  export interface ModelConfig {
    [key: string]: any;
    id: string;
    name: string;
    provider: Provider;
    model: string;
    baseUrl?: string;
    apiKey?: string;
    enabled: boolean;
    active: boolean;
    temperature?: number;
    maxTokens?: number;
    remark?: string;
    lastLatencyMs?: number;
    lastTestOk?: boolean;
    lastTestedAt?: string;
    createdAt?: string;
  }

  export interface TestResult {
    success: boolean;
    latencyMs: number;
    message?: string;
  }
}

const Url = '/ai/model';

export function getAiModelPage(params: Record<string, any>) {
  return requestClient.get(Url, { params });
}

export function createAiModel(data: Partial<AiModelApi.ModelConfig>) {
  return requestClient.post(Url, data);
}

export function updateAiModel(data: Partial<AiModelApi.ModelConfig>) {
  return requestClient.put(Url, data);
}

export function fetchAiModelList(data: Partial<AiModelApi.ModelConfig>) {
  return requestClient.post<string[]>(`${Url}/fetch-models`, data);
}

export function testAiModel(data: Partial<AiModelApi.ModelConfig>) {
  return requestClient.post<AiModelApi.TestResult>(`${Url}/test`, data);
}

export function activateAiModel(id: string) {
  return requestClient.post(`${Url}/${id}/activate`);
}

export function changeAiModelEnabled(id: string, enabled: boolean) {
  return requestClient.post(`${Url}/${id}/enabled?enabled=${enabled}`);
}

export function deleteAiModel(id: string) {
  return requestClient.delete(`${Url}/${id}`);
}
