import { requestClient } from '#/api/request';

export namespace AiConfigApi {
  export interface RagConfig {
    collectionName: string;
    embeddingApiKey?: string;
    embeddingBaseUrl?: string;
    embeddingModel: string;
    embeddingTimeoutMs: number;
    enabled: boolean;
    qdrantApiKey?: string;
    qdrantTimeoutMs: number;
    qdrantUrl: string;
  }

  export interface AiConfig {
    assistantEnabled: boolean;
    assistantMaxToolIterations: number;
    assistantRequirePermission: boolean;
    assistantSystemPrompt: string;
    chatHistoryWindow: number;
    executeSqlEnabled: boolean;
    executeSqlMaxRows: number;
    executeSqlPermissionCode?: string;
    mcpClientEnabled: boolean;
    rag: RagConfig;
    readonlySqlEnabled: boolean;
    readonlySqlMaxRows: number;
    readonlySqlPermissionCode?: string;
    schemaToolEnabled: boolean;
    schemaToolPermissionCode?: string;
  }
}

const Url = '/ai/config';

export function getAiConfig() {
  return requestClient.get<AiConfigApi.AiConfig>(Url);
}

export function updateAiConfig(data: AiConfigApi.AiConfig) {
  return requestClient.put(Url, data);
}
