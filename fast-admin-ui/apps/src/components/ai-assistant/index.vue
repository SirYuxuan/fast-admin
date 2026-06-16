<script lang="ts" setup>
import type { AiAgentApi } from '#/api';

import { nextTick, ref } from 'vue';

import { LoaderCircle, MessageSquareCode, X } from '@vben/icons';

import { App, Button, Drawer, FloatButton, Input } from 'ant-design-vue';

import { streamAiAgentChat } from '#/api';

type ChatMessage = {
  content: string;
  id: string;
  role: 'assistant' | 'user';
  status?: 'error' | 'streaming';
};

const { TextArea } = Input;
const { message } = App.useApp();

const open = ref(false);
const sending = ref(false);
const input = ref('');
const sessionId = ref<string>();
const listRef = ref<HTMLElement>();
const messages = ref<ChatMessage[]>([
  {
    content: '你好，我是 AI 运维助手。当前版本先支持对话，后续会逐步接入后台工具。',
    id: crypto.randomUUID(),
    role: 'assistant',
  },
]);

let abortController: AbortController | undefined;

function scrollToBottom() {
  nextTick(() => {
    if (listRef.value) {
      listRef.value.scrollTop = listRef.value.scrollHeight;
    }
  });
}

function appendAssistantDelta(text: string) {
  const current = messages.value.at(-1);
  if (current?.role === 'assistant' && current.status === 'streaming') {
    current.content += text;
  }
  scrollToBottom();
}

function handleEvent(event: AiAgentApi.ChatEvent) {
  // 后端先返回 session，后续多轮对话沿用这个 ID。
  if (event.type === 'session') {
    sessionId.value = event.sessionId;
    return;
  }

  if (event.type === 'delta') {
    appendAssistantDelta(event.text || '');
    return;
  }

  if (event.type === 'error') {
    const current = messages.value.at(-1);
    if (current?.role === 'assistant' && current.status === 'streaming') {
      current.content = event.message || 'AI 请求失败';
      current.status = 'error';
    }
    return;
  }

  if (event.type === 'done') {
    const current = messages.value.at(-1);
    if (current?.role === 'assistant') {
      current.status = undefined;
    }
  }
}

async function sendMessage() {
  const content = input.value.trim();
  if (!content || sending.value) {
    return;
  }

  // 每次发送都创建新的 AbortController，方便用户主动停止当前流式响应。
  input.value = '';
  sending.value = true;
  abortController = new AbortController();

  messages.value.push({
    content,
    id: crypto.randomUUID(),
    role: 'user',
  });
  messages.value.push({
    content: '',
    id: crypto.randomUUID(),
    role: 'assistant',
    status: 'streaming',
  });
  scrollToBottom();

  try {
    await streamAiAgentChat(
      { message: content, sessionId: sessionId.value },
      handleEvent,
      abortController.signal,
    );
  } catch (error) {
    const current = messages.value.at(-1);
    if (current?.role === 'assistant') {
      current.content =
        error instanceof Error ? error.message : 'AI 请求失败，请稍后再试';
      current.status = 'error';
    }
    message.error('AI 请求失败');
  } finally {
    sending.value = false;
    abortController = undefined;
  }
}

function stopMessage() {
  abortController?.abort();
  sending.value = false;
}
</script>

<template>
  <FloatButton
    class="ai-assistant-entry"
    tooltip="AI 运维助手"
    @click="open = true"
  >
    <template #icon>
      <MessageSquareCode class="size-5" />
    </template>
  </FloatButton>

  <Drawer
    v-model:open="open"
    class="ai-assistant-drawer"
    placement="right"
    title="AI 运维助手"
    width="420"
  >
    <div class="ai-assistant-shell">
      <div ref="listRef" class="ai-assistant-messages">
        <div
          v-for="item in messages"
          :key="item.id"
          :class="[
            'ai-assistant-message',
            `ai-assistant-message--${item.role}`,
            item.status === 'error' ? 'is-error' : '',
          ]"
        >
          <div class="ai-assistant-bubble">
            <LoaderCircle
              v-if="item.status === 'streaming' && !item.content"
              class="ai-assistant-loading"
            />
            <span>{{ item.content }}</span>
          </div>
        </div>
      </div>

      <div class="ai-assistant-input">
        <TextArea
          v-model:value="input"
          :auto-size="{ minRows: 2, maxRows: 5 }"
          :disabled="sending"
          placeholder="输入运维问题或操作意图"
          @press-enter.exact.prevent="sendMessage"
        />
        <Button
          v-if="sending"
          class="ai-assistant-send"
          danger
          type="text"
          @click="stopMessage"
        >
          <template #icon>
            <X class="size-4" />
          </template>
        </Button>
        <Button
          v-else
          class="ai-assistant-send"
          type="primary"
          @click="sendMessage"
        >
          发送
        </Button>
      </div>
    </div>
  </Drawer>
</template>

<style scoped>
.ai-assistant-entry {
  inset-block-end: 32px;
  inset-inline-end: 32px;
}

.ai-assistant-shell {
  display: flex;
  height: calc(100vh - 112px);
  flex-direction: column;
  gap: 12px;
}

.ai-assistant-messages {
  flex: 1;
  overflow-y: auto;
  padding: 2px 4px 8px;
}

.ai-assistant-message {
  display: flex;
  margin-block-end: 12px;
}

.ai-assistant-message--user {
  justify-content: flex-end;
}

.ai-assistant-bubble {
  max-width: min(82%, 320px);
  border: 1px solid hsl(var(--border));
  border-radius: 8px;
  background: hsl(var(--background));
  color: hsl(var(--foreground));
  line-height: 1.7;
  overflow-wrap: anywhere;
  padding: 10px 12px;
  white-space: pre-wrap;
}

.ai-assistant-message--user .ai-assistant-bubble {
  border-color: hsl(var(--primary) / 35%);
  background: hsl(var(--primary));
  color: hsl(var(--primary-foreground));
}

.ai-assistant-message.is-error .ai-assistant-bubble {
  border-color: hsl(var(--destructive) / 35%);
  color: hsl(var(--destructive));
}

.ai-assistant-loading {
  animation: ai-assistant-spin 1s linear infinite;
}

.ai-assistant-input {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
}

.ai-assistant-send {
  align-self: end;
}

@keyframes ai-assistant-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
