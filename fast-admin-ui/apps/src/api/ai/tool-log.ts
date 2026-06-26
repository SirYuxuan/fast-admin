import { requestClient } from '#/api/request';

export namespace AiToolLogApi {
  export interface ToolCallLog {
    [key: string]: any;
    id: string;
    sessionId?: string;
    operatorId?: string;
    toolName: string;
    source: string;
    argumentsJson?: string;
    resultJson?: string;
    success: boolean;
    errorMsg?: string;
    costMs?: number;
    createdAt?: string;
  }
}

const Url = '/ai/tool-log';

export function getAiToolLogPage(params: Record<string, any>) {
  return requestClient.get(Url, { params });
}

export function getAiToolLogDetail(id: string) {
  return requestClient.get<AiToolLogApi.ToolCallLog>(`${Url}/${id}`);
}
