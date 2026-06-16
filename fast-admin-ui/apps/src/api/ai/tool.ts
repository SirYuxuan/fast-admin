import { requestClient } from '#/api/request';

export namespace AiToolApi {
  export type ToolType = 'http' | 'sql';

  export interface ToolConfig {
    [key: string]: any;
    id: string;
    name: string;
    toolCode: string;
    type: ToolType;
    description: string;
    enabled: boolean;
    permissionCode?: string;
    method?: string;
    url?: string;
    headersJson?: string;
    bodyTemplate?: string;
    sqlText?: string;
    readOnly?: boolean;
    timeoutMs?: number;
    remark?: string;
    createdAt?: string;
  }
}

const Url = '/ai/tool';

export function getAiToolPage(params: Record<string, any>) {
  return requestClient.get(Url, { params });
}

export function createAiTool(data: Partial<AiToolApi.ToolConfig>) {
  return requestClient.post(Url, data);
}

export function updateAiTool(data: Partial<AiToolApi.ToolConfig>) {
  return requestClient.put(Url, data);
}

export function deleteAiTool(id: string) {
  return requestClient.delete(`${Url}/${id}`);
}
