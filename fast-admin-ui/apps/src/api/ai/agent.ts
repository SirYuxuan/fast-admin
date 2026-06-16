import { useAppConfig } from '@vben/hooks';
import { useAccessStore } from '@vben/stores';

export namespace AiAgentApi {
  export interface ChatRequest {
    message: string;
    sessionId?: string;
  }

  export interface ChatEvent {
    message?: string;
    messageId?: string;
    sessionId?: string;
    text?: string;
    type: 'delta' | 'done' | 'error' | 'session';
  }
}

const { apiURL } = useAppConfig(import.meta.env, import.meta.env.PROD);

function resolveApiUrl(path: string) {
  return `${apiURL.replace(/\/$/, '')}${path}`;
}

// 后端返回标准 text/event-stream；这里只解析 data 行，兼容多行 data。
function parseSseBlock(block: string) {
  const data = block
    .split(/\r?\n/)
    .filter((line) => line.startsWith('data:'))
    .map((line) => line.slice(5).trimStart())
    .join('\n');

  if (!data) {
    return;
  }
  return JSON.parse(data) as AiAgentApi.ChatEvent;
}

/**
 * 通过 fetch POST 消费 SSE。原生 EventSource 不支持携带 body，不适合当前接口。
 */
export async function streamAiAgentChat(
  data: AiAgentApi.ChatRequest,
  onEvent: (event: AiAgentApi.ChatEvent) => void,
  signal?: AbortSignal,
) {
  const accessStore = useAccessStore();
  const response = await fetch(resolveApiUrl('/ai/agent/chat'), {
    body: JSON.stringify(data),
    headers: {
      'Accept': 'text/event-stream',
      'Authorization': accessStore.accessToken || '',
      'Content-Type': 'application/json',
    },
    method: 'POST',
    signal,
  });

  if (!response.ok || !response.body) {
    throw new Error(`AI 请求失败：${response.status}`);
  }

  const decoder = new TextDecoder();
  const reader = response.body.getReader();
  let buffer = '';

  // SSE 按网络分片到达，buffer 用来暂存尚未凑齐的事件块。
  while (true) {
    const { done, value } = await reader.read();
    if (done) {
      break;
    }

    buffer += decoder.decode(value, { stream: true });
    const blocks = buffer.split(/\r?\n\r?\n/);
    buffer = blocks.pop() || '';

    for (const block of blocks) {
      const event = parseSseBlock(block);
      if (event) {
        onEvent(event);
      }
    }
  }

  // 兼容服务端结束前没有额外空行的场景。
  if (buffer.trim()) {
    const event = parseSseBlock(buffer);
    if (event) {
      onEvent(event);
    }
  }
}
