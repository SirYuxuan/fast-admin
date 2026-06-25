<script lang="ts" setup>
import type { AiAgentApi } from '#/api';

import { computed, h, nextTick, onBeforeUnmount, ref, watch } from 'vue';

import {
  Copy,
  LoaderCircle,
  Maximize,
  MessageSquareCode,
  Minimize,
  PanelRight,
  Plus,
  X,
} from '@vben/icons';

import { App, Button, Drawer, FloatButton, Modal } from 'ant-design-vue';
import { Bubble, Conversations, Sender } from 'ant-design-x-vue';
import MarkdownIt from 'markdown-it';

import {
  deleteAiAgentSession,
  getAiAgentSessionMessages,
  getAiAgentSessions,
  streamAiAgentChat,
} from '#/api';

type ChatMessage = {
  content: string;
  createdAt?: string;
  id: string;
  modelCode?: string;
  modelName?: string;
  modelProvider?: string;
  process?: ProcessItem[];
  role: 'assistant' | 'user';
  status?: 'error' | 'streaming';
};

type ProcessItem = {
  costMs?: number;
  detail?: string;
  id: string;
  source?: string;
  status: 'error' | 'info' | 'running' | 'success';
  title: string;
  toolName?: string;
  type: 'thought' | 'tool';
};

const { message } = App.useApp();
const ASSISTANT_STATE_KEY = 'fast-admin-ai-assistant-state';
const welcomeMessage = '你好，我是 AI 运维助手。当前版本先支持对话，后续会逐步接入后台工具。';
const markdown = new MarkdownIt({
  breaks: true,
  html: false,
  linkify: true,
});

const savedAssistantState = loadAssistantState();
const open = ref(false);
const expanded = ref(savedAssistantState.expanded);
const detached = ref(savedAssistantState.detached);
const dragging = ref(false);
const resizing = ref(false);
const sending = ref(false);
const sessionLoading = ref(false);
const switchingSession = ref(false);
const input = ref('');
const sessionId = ref<string>();
const listRef = ref<HTMLElement>();
const floatingRef = ref<HTMLElement>();
const sessions = ref<AiAgentApi.ChatSession[]>([]);
const messages = ref<ChatMessage[]>([createWelcomeMessage()]);
const drawerWidth = computed(() => (expanded.value ? 'min(960px, 96vw)' : '420'));
const conversationItems = computed(() =>
  sessions.value.map((session) => ({
    key: session.sessionId,
    label: h('div', { class: 'ai-assistant-session-label' }, [
      h('span', { class: 'ai-assistant-session-title' }, session.title),
      h('span', { class: 'ai-assistant-session-time' }, formatDate(session.createdAt)),
    ]),
    session,
    timestamp: session.createdAt ? new Date(session.createdAt).getTime() : undefined,
  })),
);
const shellClass = computed(() => ({
  'ai-assistant-shell': true,
  'is-expanded': expanded.value || detached.value,
}));
const floatingStyle = ref({
  height: savedAssistantState.floatingStyle.height,
  left: savedAssistantState.floatingStyle.left,
  top: savedAssistantState.floatingStyle.top,
  width: savedAssistantState.floatingStyle.width,
});

let abortController: AbortController | undefined;
let dragOffsetX = 0;
let dragOffsetY = 0;
let floatingFrame = 0;
let pendingFloatingStyle: Partial<typeof floatingStyle.value> | undefined;
let pendingUserMessage = '';
let resizeStartHeight = 0;
let resizeStartWidth = 0;
let resizeStartX = 0;
let resizeStartY = 0;

function loadAssistantState() {
  const defaultStyle = {
    height: '640px',
    left: 'calc(100vw - 760px)',
    top: '96px',
    width: '720px',
  };
  if (typeof window === 'undefined') {
    return {
      detached: false,
      expanded: false,
      floatingStyle: defaultStyle,
    };
  }
  try {
    const raw = window.localStorage.getItem(ASSISTANT_STATE_KEY);
    const state = raw ? JSON.parse(raw) : {};
    return {
      detached: Boolean(state.detached),
      expanded: Boolean(state.expanded),
      floatingStyle: {
        ...defaultStyle,
        ...(state.floatingStyle || {}),
      },
    };
  } catch {
    return {
      detached: false,
      expanded: false,
      floatingStyle: defaultStyle,
    };
  }
}

function saveAssistantState() {
  if (typeof window === 'undefined') {
    return;
  }
  window.localStorage.setItem(
    ASSISTANT_STATE_KEY,
    JSON.stringify({
      detached: detached.value,
      expanded: expanded.value,
      floatingStyle: floatingStyle.value,
    }),
  );
}

function createWelcomeMessage(): ChatMessage {
  return {
    content: welcomeMessage,
    id: crypto.randomUUID(),
    role: 'assistant',
  };
}

function formatDate(value?: string) {
  if (!value) {
    return '未知时间';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleString('zh-CN', {
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    month: '2-digit',
  });
}

function renderMarkdown(content: string) {
  return markdown.render(content || '');
}

async function loadSessions() {
  sessionLoading.value = true;
  try {
    sessions.value = await getAiAgentSessions();
  } catch {
    message.warning('会话记录加载失败');
  } finally {
    sessionLoading.value = false;
  }
}

function getSessionMenu(item: Record<string, unknown>) {
  const session = item.session as AiAgentApi.ChatSession | undefined;
  return {
    items: [
      {
        danger: true,
        key: 'delete',
        label: '删除会话',
      },
    ],
    onClick: ({ domEvent }: { domEvent: Event }) => {
      domEvent.stopPropagation();
      if (session) {
        confirmDeleteSession(session);
      }
    },
    selectable: false,
  };
}

function confirmDeleteSession(session: AiAgentApi.ChatSession) {
  Modal.confirm({
    content: `确定删除会话“${session.title}”及其聊天记录吗？`,
    okText: '删除',
    okType: 'danger',
    onOk: () => deleteSession(session.sessionId),
    title: '删除会话',
  });
}

async function deleteSession(targetSessionId: string) {
  stopMessage();
  try {
    await deleteAiAgentSession(targetSessionId);
    sessions.value = sessions.value.filter((item) => item.sessionId !== targetSessionId);
    if (sessionId.value === targetSessionId) {
      newSession();
    }
    message.success('会话已删除');
  } catch {
    message.error('会话删除失败');
  }
}

async function selectSession(targetSessionId: string) {
  if (targetSessionId === sessionId.value) {
    return;
  }
  stopMessage();
  sessionId.value = targetSessionId;
  switchingSession.value = true;
  try {
    const history = await getAiAgentSessionMessages(targetSessionId);
    messages.value = history.length
      ? history.map((item) => ({
          content: item.content,
          createdAt: item.createdAt,
          id: crypto.randomUUID(),
          modelCode: item.modelCode,
          modelName: item.modelName,
          modelProvider: item.modelProvider,
          process: parseProcessJson(item.processJson),
          role: item.role,
        }))
      : [createWelcomeMessage()];
    scrollToBottom();
  } catch {
    message.error('会话消息加载失败');
  } finally {
    switchingSession.value = false;
  }
}

function parseProcessJson(processJson?: string) {
  if (!processJson) {
    return;
  }
  try {
    const process = JSON.parse(processJson);
    return Array.isArray(process) ? (process as ProcessItem[]) : undefined;
  } catch {
    return;
  }
}

function openAssistant() {
  open.value = true;
}

function detachAssistant() {
  detached.value = true;
  expanded.value = false;
  open.value = true;
}

function attachAssistant() {
  detached.value = false;
  open.value = true;
}

function closeAssistant() {
  open.value = false;
}

function startDrag(event: MouseEvent) {
  if (!floatingRef.value || event.button !== 0) {
    return;
  }
  dragging.value = true;
  const rect = floatingRef.value.getBoundingClientRect();
  dragOffsetX = event.clientX - rect.left;
  dragOffsetY = event.clientY - rect.top;
  window.addEventListener('mousemove', moveFloating);
  window.addEventListener('mouseup', stopDrag);
}

function moveFloating(event: MouseEvent) {
  const width = floatingRef.value?.offsetWidth || 720;
  const height = floatingRef.value?.offsetHeight || 640;
  const left = Math.min(Math.max(8, event.clientX - dragOffsetX), window.innerWidth - width - 8);
  const top = Math.min(Math.max(8, event.clientY - dragOffsetY), window.innerHeight - height - 8);
  scheduleFloatingStyle({
    left: `${left}px`,
    top: `${top}px`,
  });
}

function stopDrag() {
  dragging.value = false;
  window.removeEventListener('mousemove', moveFloating);
  window.removeEventListener('mouseup', stopDrag);
}

function startResize(event: MouseEvent) {
  if (!floatingRef.value || event.button !== 0) {
    return;
  }
  resizing.value = true;
  const rect = floatingRef.value.getBoundingClientRect();
  resizeStartWidth = rect.width;
  resizeStartHeight = rect.height;
  resizeStartX = event.clientX;
  resizeStartY = event.clientY;
  window.addEventListener('mousemove', resizeFloating);
  window.addEventListener('mouseup', stopResize);
}

function resizeFloating(event: MouseEvent) {
  const minWidth = 520;
  const minHeight = 420;
  const left = floatingRef.value?.getBoundingClientRect().left || 0;
  const top = floatingRef.value?.getBoundingClientRect().top || 0;
  const maxWidth = window.innerWidth - left - 8;
  const maxHeight = window.innerHeight - top - 8;
  const width = Math.min(
    Math.max(minWidth, resizeStartWidth + event.clientX - resizeStartX),
    maxWidth,
  );
  const height = Math.min(
    Math.max(minHeight, resizeStartHeight + event.clientY - resizeStartY),
    maxHeight,
  );
  scheduleFloatingStyle({
    height: `${Math.round(height)}px`,
    width: `${Math.round(width)}px`,
  });
}

function stopResize() {
  resizing.value = false;
  window.removeEventListener('mousemove', resizeFloating);
  window.removeEventListener('mouseup', stopResize);
}

function scheduleFloatingStyle(style: Partial<typeof floatingStyle.value>) {
  pendingFloatingStyle = {
    ...pendingFloatingStyle,
    ...style,
  };
  if (floatingFrame) {
    return;
  }
  floatingFrame = requestAnimationFrame(() => {
    floatingStyle.value = {
      ...floatingStyle.value,
      ...pendingFloatingStyle,
    };
    pendingFloatingStyle = undefined;
    floatingFrame = 0;
  });
}

function newSession() {
  stopMessage();
  sessionId.value = undefined;
  input.value = '';
  messages.value = [createWelcomeMessage()];
  scrollToBottom();
}

watch(open, (visible) => {
  if (visible) {
    loadSessions();
    scrollToBottom();
  }
});

watch([detached, expanded, floatingStyle], saveAssistantState, { deep: true });

onBeforeUnmount(() => {
  if (floatingFrame) {
    cancelAnimationFrame(floatingFrame);
  }
  stopDrag();
  stopResize();
});

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

function ensureLocalSession(targetSessionId?: string) {
  if (!targetSessionId || sessions.value.some((item) => item.sessionId === targetSessionId)) {
    return;
  }
  const title = pendingUserMessage.trim() || '新会话';
  sessions.value = [
    {
      createdAt: new Date().toISOString(),
      sessionId: targetSessionId,
      title: title.length > 28 ? `${title.slice(0, 28)}...` : title,
      updatedAt: new Date().toISOString(),
    },
    ...sessions.value,
  ];
}

function currentAssistantMessage() {
  const current = messages.value.at(-1);
  return current?.role === 'assistant' ? current : undefined;
}

function hasMessageHeader(item: ChatMessage) {
  return Boolean(item.createdAt || item.modelName || item.modelCode || item.process?.length);
}

function messageMetaText(item: ChatMessage) {
  const parts: string[] = [];
  if (item.role === 'user') {
    parts.push('我');
  } else {
    const modelParts = [item.modelName, item.modelCode].filter(Boolean);
    parts.push(modelParts.length ? modelParts.join(' / ') : 'AI 助手');
  }
  if (item.createdAt) {
    parts.push(formatDate(item.createdAt));
  }
  return parts.join(' · ');
}

function applyAssistantMeta(event: AiAgentApi.ChatEvent) {
  const current = currentAssistantMessage();
  if (!current) {
    return;
  }
  current.modelName = event.modelName;
  current.modelProvider = event.modelProvider;
  current.modelCode = event.modelCode;
}

function formatToolArgs(args?: string) {
  if (!args) {
    return;
  }
  try {
    const parsed = JSON.parse(args);
    args = JSON.stringify(parsed, null, 2);
  } catch {
    // Keep the original tool input if it is not JSON.
  }
  return args.length > 360 ? `${args.slice(0, 360)}...` : args;
}

function appendProcess(item: Omit<ProcessItem, 'id'>) {
  const current = currentAssistantMessage();
  if (!current) {
    return;
  }
  current.process ||= [];
  current.process.push({
    ...item,
    id: crypto.randomUUID(),
  });
  scrollToBottom();
}

function updateToolProcess(event: AiAgentApi.ChatEvent) {
  const current = currentAssistantMessage();
  if (!current) {
    return;
  }
  current.process ||= [];

  const source = event.source || 'builtin';
  const toolName = event.toolName || 'unknown_tool';
  if (event.phase === 'start') {
    current.process.push({
      detail: formatToolArgs(event.args),
      id: crypto.randomUUID(),
      source,
      status: 'running',
      title: `${source === 'mcp' ? 'MCP' : '工具'}调用：${toolName}`,
      toolName,
      type: 'tool',
    });
    scrollToBottom();
    return;
  }

  const item = [...current.process]
    .reverse()
    .find(
      (process) =>
        process.type === 'tool' &&
        process.toolName === toolName &&
        process.source === source &&
        process.status === 'running',
    );
  if (item) {
    item.status = event.ok ? 'success' : 'error';
    item.costMs = event.costMs;
  } else {
    current.process.push({
      costMs: event.costMs,
      id: crypto.randomUUID(),
      source,
      status: event.ok ? 'success' : 'error',
      title: `${source === 'mcp' ? 'MCP' : '工具'}调用：${toolName}`,
      toolName,
      type: 'tool',
    });
  }
  scrollToBottom();
}

function handleEvent(event: AiAgentApi.ChatEvent) {
  // 后端先返回 session，后续多轮对话沿用这个 ID。
  if (event.type === 'session') {
    sessionId.value = event.sessionId;
    applyAssistantMeta(event);
    ensureLocalSession(event.sessionId);
    return;
  }

  if (event.type === 'delta') {
    appendAssistantDelta(event.text || '');
    return;
  }

  if (event.type === 'thought') {
    appendProcess({
      status: 'info',
      title: event.text || '正在思考',
      type: 'thought',
    });
    return;
  }

  if (event.type === 'tool') {
    updateToolProcess(event);
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

async function sendMessage(value?: string) {
  const content = (value ?? input.value).trim();
  if (!content || sending.value) {
    return;
  }

  // 每次发送都创建新的 AbortController，方便用户主动停止当前流式响应。
  pendingUserMessage = content;
  input.value = '';
  nextTick(() => {
    input.value = '';
  });
  sending.value = true;
  abortController = new AbortController();

  messages.value.push({
    content,
    createdAt: new Date().toISOString(),
    id: crypto.randomUUID(),
    role: 'user',
  });
  messages.value.push({
    content: '',
    createdAt: new Date().toISOString(),
    id: crypto.randomUUID(),
    process: [],
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
      current.content = error instanceof Error ? error.message : 'AI 请求失败，请稍后再试';
      current.status = 'error';
    }
    message.error('AI 请求失败');
  } finally {
    input.value = '';
    pendingUserMessage = '';
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
  <FloatButton class="ai-assistant-entry" tooltip="AI 运维助手" @click="openAssistant">
    <template #icon>
      <MessageSquareCode class="size-5" />
    </template>
  </FloatButton>

  <Drawer
    v-if="!detached"
    v-model:open="open"
    class="ai-assistant-drawer"
    placement="right"
    title="AI 运维助手"
    :width="drawerWidth"
  >
    <template #extra>
      <Button title="分离窗口" type="text" @click="detachAssistant">
        <template #icon>
          <Copy class="size-4" />
        </template>
      </Button>
      <Button :title="expanded ? '还原' : '放大'" type="text" @click="expanded = !expanded">
        <template #icon>
          <Minimize v-if="expanded" class="size-4" />
          <Maximize v-else class="size-4" />
        </template>
      </Button>
    </template>

    <div :class="shellClass">
      <aside class="ai-assistant-sessions">
        <div class="ai-assistant-sessions-head">
          <span>会话记录</span>
          <Button size="small" type="text" @click="newSession">
            <template #icon>
              <Plus class="size-4" />
            </template>
            新会话
          </Button>
        </div>
        <div v-if="sessionLoading" class="ai-assistant-session-empty">加载中...</div>
        <Conversations
          v-else
          :active-key="sessionId"
          class="ai-assistant-conversations"
          :items="conversationItems"
          :menu="getSessionMenu"
          :on-active-change="selectSession"
        />
      </aside>

      <section class="ai-assistant-chat">
        <div ref="listRef" :class="['ai-assistant-messages', switchingSession ? 'is-loading' : '']">
          <div v-if="switchingSession" class="ai-assistant-chat-loading">
            <LoaderCircle class="ai-assistant-process-spinner" />
            <span>正在加载会话...</span>
          </div>
          <Bubble
            v-for="item in messages"
            :key="item.id"
            class="ai-assistant-message"
            :content="item.content"
            :loading="item.status === 'streaming' && !item.content"
            :placement="item.role === 'user' ? 'end' : 'start'"
            :root-class-name="item.status === 'error' ? 'is-error' : ''"
            :class-names="{ content: 'ai-assistant-bubble' }"
            shape="corner"
            :variant="item.role === 'user' ? 'filled' : 'outlined'"
          >
            <template #header v-if="hasMessageHeader(item)">
              <div :class="['ai-assistant-message-header', `is-${item.role}`]">
                <div class="ai-assistant-message-meta">{{ messageMetaText(item) }}</div>
              </div>
              <div v-if="item.role === 'assistant' && item.process?.length" class="ai-assistant-process">
                <div class="ai-assistant-process-title">思考与执行</div>
                <div
                  v-for="process in item.process"
                  :key="process.id"
                  :class="['ai-assistant-process-item', `is-${process.status}`]"
                >
                  <div class="ai-assistant-process-row">
                    <LoaderCircle
                      v-if="process.status === 'running'"
                      class="ai-assistant-process-spinner"
                    />
                    <span v-else class="ai-assistant-process-dot"></span>
                    <span class="ai-assistant-process-text">
                      {{ process.title }}
                    </span>
                    <span v-if="process.costMs" class="ai-assistant-process-cost">
                      {{ process.costMs }}ms
                    </span>
                  </div>
                  <pre v-if="process.detail" class="ai-assistant-process-detail">{{
                    process.detail
                  }}</pre>
                </div>
              </div>
            </template>
            <template #message="{ content }">
              <!-- eslint-disable vue/no-v-html -->
              <div class="ai-assistant-markdown" v-html="renderMarkdown(content)"></div>
            </template>
          </Bubble>
        </div>

        <Sender
          v-model:value="input"
          :auto-size="{ minRows: 2, maxRows: 5 }"
          class="ai-assistant-sender"
          :disabled="sending"
          :loading="sending"
          :on-cancel="stopMessage"
          :on-submit="sendMessage"
          placeholder="输入运维问题或操作意图"
          submit-type="enter"
        />
      </section>
    </div>
  </Drawer>

  <Teleport to="body">
    <div
      v-if="open && detached"
      ref="floatingRef"
      :class="[
        'ai-assistant-floating',
        dragging ? 'is-dragging' : '',
        resizing ? 'is-resizing' : '',
      ]"
      :style="floatingStyle"
    >
      <div class="ai-assistant-floating-head" @mousedown="startDrag">
        <span>AI 运维助手</span>
        <div class="ai-assistant-floating-actions" @mousedown.stop>
          <Button title="收回抽屉" type="text" @click="attachAssistant">
            <template #icon>
              <PanelRight class="size-4" />
            </template>
          </Button>
          <Button title="关闭" type="text" @click="closeAssistant">
            <template #icon>
              <X class="size-4" />
            </template>
          </Button>
        </div>
      </div>

      <div :class="shellClass">
        <aside class="ai-assistant-sessions">
          <div class="ai-assistant-sessions-head">
            <span>会话记录</span>
            <Button size="small" type="text" @click="newSession">
              <template #icon>
                <Plus class="size-4" />
              </template>
              新会话
            </Button>
          </div>
          <div v-if="sessionLoading" class="ai-assistant-session-empty">加载中...</div>
          <Conversations
            v-else
            :active-key="sessionId"
            class="ai-assistant-conversations"
            :items="conversationItems"
            :menu="getSessionMenu"
            :on-active-change="selectSession"
          />
        </aside>

        <section class="ai-assistant-chat">
          <div ref="listRef" :class="['ai-assistant-messages', switchingSession ? 'is-loading' : '']">
            <div v-if="switchingSession" class="ai-assistant-chat-loading">
              <LoaderCircle class="ai-assistant-process-spinner" />
              <span>正在加载会话...</span>
            </div>
            <Bubble
              v-for="item in messages"
              :key="item.id"
              class="ai-assistant-message"
              :content="item.content"
              :loading="item.status === 'streaming' && !item.content"
              :placement="item.role === 'user' ? 'end' : 'start'"
              :root-class-name="item.status === 'error' ? 'is-error' : ''"
              :class-names="{ content: 'ai-assistant-bubble' }"
              shape="corner"
              :variant="item.role === 'user' ? 'filled' : 'outlined'"
            >
              <template #header v-if="hasMessageHeader(item)">
                <div :class="['ai-assistant-message-header', `is-${item.role}`]">
                  <div class="ai-assistant-message-meta">{{ messageMetaText(item) }}</div>
                </div>
                <div v-if="item.role === 'assistant' && item.process?.length" class="ai-assistant-process">
                  <div class="ai-assistant-process-title">思考与执行</div>
                  <div
                    v-for="process in item.process"
                    :key="process.id"
                    :class="['ai-assistant-process-item', `is-${process.status}`]"
                  >
                    <div class="ai-assistant-process-row">
                      <LoaderCircle
                        v-if="process.status === 'running'"
                        class="ai-assistant-process-spinner"
                      />
                      <span v-else class="ai-assistant-process-dot"></span>
                      <span class="ai-assistant-process-text">
                        {{ process.title }}
                      </span>
                      <span v-if="process.costMs" class="ai-assistant-process-cost">
                        {{ process.costMs }}ms
                      </span>
                    </div>
                    <pre v-if="process.detail" class="ai-assistant-process-detail">{{
                      process.detail
                    }}</pre>
                  </div>
                </div>
              </template>
              <template #message="{ content }">
                <!-- eslint-disable vue/no-v-html -->
                <div class="ai-assistant-markdown" v-html="renderMarkdown(content)"></div>
              </template>
            </Bubble>
          </div>

          <Sender
            v-model:value="input"
            :auto-size="{ minRows: 2, maxRows: 5 }"
            class="ai-assistant-sender"
            :disabled="sending"
            :loading="sending"
            :on-cancel="stopMessage"
            :on-submit="sendMessage"
            placeholder="输入运维问题或操作意图"
            submit-type="enter"
          />
        </section>
      </div>
      <div
        class="ai-assistant-floating-resize"
        title="拖动调整大小"
        @mousedown.stop.prevent="startResize"
      ></div>
    </div>
  </Teleport>
</template>

<style scoped>
.ai-assistant-entry {
  inset-block-end: 32px;
  inset-inline-end: 32px;
}

.ai-assistant-floating {
  position: fixed;
  z-index: 1001;
  display: flex;
  box-sizing: border-box;
  min-width: 520px;
  min-height: 420px;
  max-width: calc(100vw - 16px);
  max-height: calc(100vh - 16px);
  flex-direction: column;
  overflow: hidden;
  border: 1px solid hsl(var(--border));
  border-radius: 8px;
  background: hsl(var(--background));
  box-shadow: 0 18px 48px hsl(var(--foreground) / 18%);
}

.ai-assistant-floating.is-dragging,
.ai-assistant-floating.is-resizing {
  user-select: none;
}

.ai-assistant-floating-head {
  display: flex;
  flex: none;
  align-items: center;
  justify-content: space-between;
  border-block-end: 1px solid hsl(var(--border));
  cursor: move;
  font-size: 14px;
  font-weight: 600;
  padding: 8px 10px 8px 14px;
}

.ai-assistant-floating-actions {
  display: flex;
  align-items: center;
  gap: 2px;
}

.ai-assistant-floating .ai-assistant-shell {
  height: auto;
  min-height: 0;
  flex: 1;
  padding: 12px;
}

.ai-assistant-floating-resize {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 18px;
  height: 18px;
  cursor: nwse-resize;
}

.ai-assistant-floating-resize::after {
  position: absolute;
  right: 4px;
  bottom: 4px;
  width: 8px;
  height: 8px;
  border-right: 2px solid hsl(var(--muted-foreground));
  border-bottom: 2px solid hsl(var(--muted-foreground));
  content: '';
  opacity: 0.65;
}

.ai-assistant-shell {
  display: grid;
  height: calc(100vh - 112px);
  gap: 14px;
  grid-template-rows: auto minmax(0, 1fr);
}

.ai-assistant-shell.is-expanded {
  grid-template-columns: 248px minmax(0, 1fr);
  grid-template-rows: minmax(0, 1fr);
}

.ai-assistant-sessions {
  display: flex;
  min-width: 0;
  min-height: 0;
  max-height: 188px;
  flex-direction: column;
  gap: 10px;
  border: 1px solid hsl(var(--border));
  border-radius: 8px;
  background: hsl(var(--background));
  box-shadow: 0 1px 2px hsl(var(--foreground) / 4%);
  padding: 10px;
}

.ai-assistant-shell:not(.is-expanded) .ai-assistant-sessions {
  max-height: 220px;
  background: hsl(var(--muted) / 18%);
  padding: 8px;
}

.ai-assistant-shell.is-expanded .ai-assistant-sessions {
  max-height: none;
}

.ai-assistant-sessions-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  color: hsl(var(--foreground));
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0;
  padding-inline: 2px;
}

.ai-assistant-sessions-head :deep(.ant-btn) {
  display: inline-flex;
  align-items: center;
  height: 26px;
  gap: 4px;
  padding-inline: 6px;
  font-size: 12px;
  line-height: 1;
}

.ai-assistant-sessions-head :deep(.ant-btn .ant-btn-icon) {
  display: inline-flex;
  align-items: center;
}

.ai-assistant-session-empty {
  color: hsl(var(--muted-foreground));
  font-size: 12px;
  padding: 10px 2px;
}

:deep(.ai-assistant-conversations) {
  min-width: 0;
  overflow: auto;
  border: 1px solid hsl(var(--border));
  border-radius: 7px;
  background: hsl(var(--background));
  padding: 0;
}

:deep(.ai-assistant-conversations .ant-conversations-item) {
  position: relative;
  height: auto;
  min-width: 0;
  border: 1px solid transparent;
  border-radius: 0;
  background: transparent;
  border-block-end-color: hsl(var(--border) / 72%);
  margin-block: 0;
  padding: 10px 8px 10px 13px;
  transition:
    background-color 0.16s ease,
    border-color 0.16s ease,
    box-shadow 0.16s ease;
}

:deep(.ai-assistant-conversations .ant-conversations-item:first-child) {
  border-start-start-radius: 7px;
  border-start-end-radius: 7px;
}

:deep(.ai-assistant-conversations .ant-conversations-item:last-child) {
  border-end-start-radius: 7px;
  border-end-end-radius: 7px;
  border-block-end-color: transparent;
}

:deep(.ai-assistant-conversations .ant-conversations-item::before) {
  position: absolute;
  top: 9px;
  bottom: 9px;
  left: 4px;
  width: 3px;
  border-radius: 999px;
  background: transparent;
  content: '';
}

:deep(.ai-assistant-conversations .ant-conversations-item:hover) {
  border-color: transparent;
  background: hsl(var(--muted) / 28%);
}

:deep(.ai-assistant-conversations .ant-conversations-item-active) {
  border-color: transparent;
  background: hsl(var(--primary) / 6%);
  box-shadow: none;
}

:deep(.ai-assistant-conversations .ant-conversations-item-active::before) {
  background: hsl(var(--primary));
}

:deep(.ai-assistant-conversations .ant-conversations-item-active:hover) {
  background: hsl(var(--primary) / 8%);
}

:deep(.ai-assistant-conversations .ant-conversations-label) {
  min-width: 0;
  flex: 1;
}

:deep(.ai-assistant-conversations .ant-conversations-menu-icon) {
  color: hsl(var(--muted-foreground));
}

:deep(.ai-assistant-session-label) {
  display: grid;
  min-width: 0;
  gap: 3px;
  line-height: 1.25;
}

:deep(.ai-assistant-session-title) {
  overflow: hidden;
  color: hsl(var(--foreground));
  font-size: 13px;
  font-weight: 500;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.ai-assistant-session-time) {
  overflow: hidden;
  color: hsl(var(--muted-foreground));
  font-size: 11px;
  font-weight: 400;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.ai-assistant-conversations .ant-conversations-item-active .ai-assistant-session-title) {
  color: hsl(var(--foreground));
}

.ai-assistant-shell:not(.is-expanded) :deep(.ai-assistant-conversations) {
  max-height: 156px;
}

.ai-assistant-shell:not(.is-expanded) :deep(.ai-assistant-conversations .ant-conversations-item) {
  min-height: 48px;
  padding: 7px 6px 7px 14px;
}

.ai-assistant-shell:not(.is-expanded) :deep(.ai-assistant-conversations .ant-conversations-item::after) {
  position: absolute;
  right: 36px;
  bottom: 0;
  left: 14px;
  height: 1px;
  background: hsl(var(--border) / 78%);
  content: '';
}

.ai-assistant-shell:not(.is-expanded)
  :deep(.ai-assistant-conversations .ant-conversations-item:last-child::after) {
  display: none;
}

.ai-assistant-shell:not(.is-expanded) :deep(.ai-assistant-session-label) {
  gap: 2px;
}

.ai-assistant-shell:not(.is-expanded) :deep(.ai-assistant-session-title) {
  font-size: 12px;
}

.ai-assistant-shell:not(.is-expanded) :deep(.ai-assistant-session-time) {
  font-size: 10px;
}

.ai-assistant-chat {
  display: flex;
  min-height: 0;
  flex-direction: column;
  gap: 12px;
}

.ai-assistant-messages {
  position: relative;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 2px 4px 8px;
}

.ai-assistant-messages.is-loading {
  overflow: hidden;
}

.ai-assistant-chat-loading {
  position: absolute;
  z-index: 2;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 8px;
  background: hsl(var(--background) / 78%);
  color: hsl(var(--muted-foreground));
  font-size: 13px;
  backdrop-filter: blur(2px);
}

.ai-assistant-message {
  display: flex;
  margin-block-end: 12px;
}

:deep(.ai-assistant-bubble) {
  max-width: min(82%, 320px);
  line-height: 1.7;
  overflow-wrap: anywhere;
  white-space: normal;
}

:deep(.ai-assistant-shell.is-expanded .ai-assistant-bubble) {
  max-width: min(78%, 640px);
}

:deep(.is-error .ai-assistant-bubble) {
  border-color: hsl(var(--destructive) / 35%);
  color: hsl(var(--destructive));
}

.ai-assistant-message-header {
  margin-block-end: 6px;
}

.ai-assistant-message-header.is-user {
  text-align: right;
}

.ai-assistant-message-meta {
  color: hsl(var(--muted-foreground));
  font-size: 11px;
  line-height: 1.4;
}

.ai-assistant-markdown {
  min-width: 0;
}

:deep(.ai-assistant-markdown > :first-child) {
  margin-block-start: 0;
}

:deep(.ai-assistant-markdown > :last-child) {
  margin-block-end: 0;
}

:deep(.ai-assistant-markdown p),
:deep(.ai-assistant-markdown ul),
:deep(.ai-assistant-markdown ol),
:deep(.ai-assistant-markdown blockquote),
:deep(.ai-assistant-markdown pre),
:deep(.ai-assistant-markdown table) {
  margin-block: 0 10px;
}

:deep(.ai-assistant-markdown ul),
:deep(.ai-assistant-markdown ol) {
  padding-inline-start: 20px;
}

:deep(.ai-assistant-markdown code) {
  border-radius: 4px;
  background: hsl(var(--muted) / 70%);
  font-family:
    ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
    monospace;
  font-size: 12px;
  padding: 1px 4px;
}

:deep(.ai-assistant-markdown pre) {
  overflow: auto;
  border-radius: 6px;
  background: hsl(var(--muted) / 70%);
  padding: 10px;
}

:deep(.ai-assistant-markdown pre code) {
  background: transparent;
  padding: 0;
}

:deep(.ai-assistant-markdown blockquote) {
  border-inline-start: 3px solid hsl(var(--border));
  color: hsl(var(--muted-foreground));
  padding-inline-start: 10px;
}

:deep(.ai-assistant-markdown table) {
  display: block;
  max-width: 100%;
  overflow: auto;
  border-collapse: collapse;
}

:deep(.ai-assistant-markdown th),
:deep(.ai-assistant-markdown td) {
  border: 1px solid hsl(var(--border));
  padding: 4px 8px;
}

.ai-assistant-process {
  display: grid;
  gap: 6px;
  margin-block-end: 8px;
  border-block-end: 1px solid hsl(var(--border));
  padding-block-end: 8px;
}

.ai-assistant-process-title {
  color: hsl(var(--muted-foreground));
  font-size: 12px;
  font-weight: 600;
}

.ai-assistant-process-item {
  display: grid;
  gap: 4px;
  border-radius: 6px;
  background: hsl(var(--muted) / 45%);
  padding: 6px 8px;
}

.ai-assistant-process-row {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 6px;
}

.ai-assistant-process-spinner {
  width: 14px;
  height: 14px;
  flex: none;
  animation: ai-assistant-spin 1s linear infinite;
  color: hsl(var(--primary));
}

.ai-assistant-process-dot {
  width: 7px;
  height: 7px;
  flex: none;
  border-radius: 999px;
  background: hsl(var(--muted-foreground));
}

.ai-assistant-process-item.is-success .ai-assistant-process-dot {
  background: hsl(var(--success, 142 71% 45%));
}

.ai-assistant-process-item.is-error .ai-assistant-process-dot {
  background: hsl(var(--destructive));
}

.ai-assistant-process-text {
  min-width: 0;
  flex: 1;
  font-size: 12px;
  overflow-wrap: anywhere;
}

.ai-assistant-process-cost {
  flex: none;
  color: hsl(var(--muted-foreground));
  font-size: 11px;
}

.ai-assistant-process-detail {
  max-height: 120px;
  overflow: auto;
  margin: 0;
  color: hsl(var(--muted-foreground));
  font-family:
    ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
    monospace;
  font-size: 11px;
  line-height: 1.5;
  white-space: pre-wrap;
}

@keyframes ai-assistant-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
