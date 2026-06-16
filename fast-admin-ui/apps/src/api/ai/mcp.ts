import { requestClient } from '#/api/request';

export namespace AiMcpApi {
  export type Transport = 'sse' | 'stdio' | 'streamable-http';

  export interface McpServer {
    [key: string]: any;
    id: string;
    name: string;
    transport: Transport;
    command?: string;
    url?: string;
    argsJson?: string;
    headersJson?: string;
    enabled: boolean;
    remark?: string;
    createdAt?: string;
  }
}

const Url = '/ai/mcp/server';

export function getAiMcpServerPage(params: Record<string, any>) {
  return requestClient.get(Url, { params });
}

export function createAiMcpServer(data: Partial<AiMcpApi.McpServer>) {
  return requestClient.post(Url, data);
}

export function updateAiMcpServer(data: Partial<AiMcpApi.McpServer>) {
  return requestClient.put(Url, data);
}

export function deleteAiMcpServer(id: string) {
  return requestClient.delete(`${Url}/${id}`);
}
