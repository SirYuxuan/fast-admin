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
    connected?: boolean;
    toolCount?: number;
    promptCount?: number;
    resourceCount?: number;
    contextTokenCount?: number;
    statusMessage?: string;
    remark?: string;
    createdAt?: string;
  }

  export interface McpInspect {
    server: McpServer;
    runtime: {
      capabilities?: Record<string, any>;
      connected?: boolean;
      contextTokenCount?: number;
      instructions?: string;
      promptCount?: number;
      resourceCount?: number;
      serverInfo?: Record<string, any>;
      statusMessage?: string;
      toolCount?: number;
    };
    tools: Array<{
      description?: string;
      inputSchema?: Record<string, any>;
      name: string;
      outputSchema?: Record<string, any>;
      title?: string;
    }>;
    prompts: Array<{
      arguments?: Array<{
        description?: string;
        name: string;
        required?: boolean;
      }>;
      description?: string;
      name: string;
      title?: string;
    }>;
    resources: Array<{
      description?: string;
      mimeType?: string;
      name?: string;
      size?: number;
      title?: string;
      uri: string;
    }>;
  }
}

const Url = '/ai/mcp/server';

export function getAiMcpServerPage(params: Record<string, any>) {
  return requestClient.get(Url, { params });
}

export function getAiMcpServerDetail(id: string) {
  return requestClient.get(`${Url}/${id}`);
}

export function inspectAiMcpServer(id: string) {
  return requestClient.get<AiMcpApi.McpInspect>(`${Url}/${id}/inspect`);
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

export function reloadAiMcpServer(id: string) {
  return requestClient.post(`${Url}/${id}/reload`);
}
