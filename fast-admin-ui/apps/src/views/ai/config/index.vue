<script lang="ts" setup>
import type { AiConfigApi } from '#/api/ai/config';
import type { AiRagApi } from '#/api/ai/rag';

import { computed, onMounted, reactive, ref } from 'vue';

import { AccessControl } from '@vben/access';
import { Page } from '@vben/common-ui';
import { RotateCw } from '@vben/icons';

import {
  Button,
  Form as AForm,
  FormItem,
  Input,
  InputNumber,
  Space,
  Spin,
  Switch,
  Tag,
  message,
} from 'ant-design-vue';

import { getAiConfig, updateAiConfig } from '#/api/ai/config';
import { getAiRagVectorStoreStatus } from '#/api/ai/rag';

const loading = ref(false);
const saving = ref(false);
const statusLoading = ref(false);
const vectorStatus = ref<AiRagApi.VectorStoreStatus>();
const activeSection = ref('assistant');

const sections = [
  { key: 'assistant', title: '助手配置' },
  { key: 'sql', title: 'SQL 工具' },
  { key: 'mcp', title: 'MCP' },
  { key: 'knowledge', title: '知识库' },
  { key: 'embedding', title: 'Embedding' },
];

const form = reactive<AiConfigApi.AiConfig>({
  assistantEnabled: true,
  assistantMaxToolIterations: 8,
  assistantRequirePermission: false,
  assistantSystemPrompt:
    '你是 Fast Admin 后台的 AI 运维助手。\n回答要简洁、准确；当你无法确认后台事实时，明确说明需要工具或数据支持。\n当前版本仅支持对话，不得声称已经执行后台写操作。',
  chatHistoryWindow: 20,
  executeSqlEnabled: false,
  executeSqlMaxRows: 100,
  executeSqlPermissionCode: 'ai:sql:execute',
  mcpClientEnabled: true,
  rag: {
    collectionName: 'fast_admin_rag',
    embeddingApiKey: '',
    embeddingBaseUrl: '',
    embeddingModel: 'text-embedding-3-small',
    embeddingTimeoutMs: 20_000,
    enabled: true,
    qdrantApiKey: '',
    qdrantTimeoutMs: 5000,
    qdrantUrl: 'http://100.115.97.59:6333',
  },
  readonlySqlEnabled: true,
  readonlySqlMaxRows: 100,
  readonlySqlPermissionCode: 'ai:sql:readonly',
  schemaToolEnabled: true,
  schemaToolPermissionCode: 'ai:sql:readonly',
});

function applyConfig(data: AiConfigApi.AiConfig) {
  Object.assign(form, data);
  form.rag = { ...form.rag, ...data.rag };
}

async function load() {
  loading.value = true;
  try {
    applyConfig(await getAiConfig());
  } finally {
    loading.value = false;
  }
}

async function save() {
  saving.value = true;
  try {
    await updateAiConfig(form);
    message.success('保存成功');
    await load();
  } finally {
    saving.value = false;
  }
}

async function refreshVectorStatus() {
  statusLoading.value = true;
  try {
    vectorStatus.value = await getAiRagVectorStoreStatus();
  } finally {
    statusLoading.value = false;
  }
}

const qdrantTag = computed(() => {
  if (!vectorStatus.value) return { color: 'default', text: '-' };
  return vectorStatus.value.connected
    ? { color: 'success', text: '已连接' }
    : { color: 'warning', text: '未连接' };
});

function scrollToSection(key: string) {
  activeSection.value = key;
  document.getElementById(`ai-config-${key}`)?.scrollIntoView({
    behavior: 'smooth',
    block: 'start',
  });
}

onMounted(async () => {
  await load();
  refreshVectorStatus();
});
</script>

<template>
  <Page auto-content-height>
    <Spin :spinning="loading">
      <div class="ai-config-page">
        <div class="ai-config-header">
          <div>
            <div class="ai-config-title">AI 配置</div>
            <div class="ai-config-subtitle">运行参数</div>
          </div>
          <Space>
            <Button :loading="statusLoading" @click="refreshVectorStatus">
              <RotateCw class="size-4" />
              刷新向量库状态
            </Button>
            <AccessControl type="code" :codes="['ai:config:edit']">
              <Button type="primary" :loading="saving" @click="save">
                保存配置
              </Button>
            </AccessControl>
          </Space>
        </div>

        <div class="ai-config-status">
          <div class="status-item">
            <span>Qdrant</span>
            <Tag :color="qdrantTag.color">{{ qdrantTag.text }}</Tag>
          </div>
          <div class="status-item">
            <span>版本</span>
            <strong>{{ vectorStatus?.version || '-' }}</strong>
          </div>
          <div class="status-item">
            <span>延时</span>
            <strong>{{ vectorStatus?.latencyMs == null ? '-' : `${vectorStatus.latencyMs}ms` }}</strong>
          </div>
          <div class="status-item">
            <span>集合</span>
            <strong>{{ form.rag.collectionName || '-' }}</strong>
          </div>
          <div class="status-item">
            <span>知识库</span>
            <Tag :color="form.rag.enabled ? 'success' : 'default'">
              {{ form.rag.enabled ? '启用' : '禁用' }}
            </Tag>
          </div>
        </div>

        <div class="ai-config-shell">
          <aside class="ai-config-nav">
            <button
              v-for="item in sections"
              :key="item.key"
              class="nav-item"
              :class="{ active: activeSection === item.key }"
              type="button"
              @click="scrollToSection(item.key)"
            >
              {{ item.title }}
            </button>
          </aside>

          <AForm :model="form" class="ai-config-form" layout="vertical">
            <section id="ai-config-assistant" class="config-section">
              <div class="section-head">
                <h3>助手配置</h3>
              </div>
              <div class="section-body">
                <div class="field-grid cols-4">
                  <FormItem label="启用助手">
                    <div class="switch-field">
                      <Switch v-model:checked="form.assistantEnabled" />
                    </div>
                  </FormItem>
                  <FormItem label="使用权限校验">
                    <div class="switch-field">
                      <Switch v-model:checked="form.assistantRequirePermission" />
                    </div>
                  </FormItem>
                  <FormItem label="最大工具轮次">
                    <InputNumber
                      v-model:value="form.assistantMaxToolIterations"
                      class="w-full"
                      :min="1"
                      :max="20"
                    />
                  </FormItem>
                  <FormItem label="历史窗口">
                    <InputNumber
                      v-model:value="form.chatHistoryWindow"
                      class="w-full"
                      :min="2"
                      :max="100"
                    />
                  </FormItem>
                  <FormItem class="span-4" label="系统提示词">
                    <Input.TextArea
                      v-model:value="form.assistantSystemPrompt"
                      :auto-size="{ minRows: 4, maxRows: 10 }"
                    />
                  </FormItem>
                  <FormItem label="表结构工具">
                    <div class="switch-field">
                      <Switch v-model:checked="form.schemaToolEnabled" />
                    </div>
                  </FormItem>
                  <FormItem label="表结构工具权限码">
                    <Input v-model:value="form.schemaToolPermissionCode" />
                  </FormItem>
                </div>
              </div>
            </section>

            <section id="ai-config-sql" class="config-section">
              <div class="section-head">
                <h3>SQL 工具</h3>
              </div>
              <div class="section-body">
                <div class="field-grid cols-3">
                  <FormItem label="只读 SQL">
                    <div class="switch-field">
                      <Switch v-model:checked="form.readonlySqlEnabled" />
                    </div>
                  </FormItem>
                  <FormItem label="只读 SQL 最大行数">
                    <InputNumber
                      v-model:value="form.readonlySqlMaxRows"
                      class="w-full"
                      :min="1"
                      :max="100"
                    />
                  </FormItem>
                  <FormItem label="只读 SQL 权限码">
                    <Input v-model:value="form.readonlySqlPermissionCode" />
                  </FormItem>
                  <FormItem label="执行 SQL">
                    <div class="switch-field">
                      <Switch v-model:checked="form.executeSqlEnabled" />
                    </div>
                  </FormItem>
                  <FormItem label="执行 SQL 最大行数">
                    <InputNumber
                      v-model:value="form.executeSqlMaxRows"
                      class="w-full"
                      :min="1"
                      :max="500"
                    />
                  </FormItem>
                  <FormItem label="执行 SQL 权限码">
                    <Input v-model:value="form.executeSqlPermissionCode" />
                  </FormItem>
                </div>
              </div>
            </section>

            <section id="ai-config-mcp" class="config-section">
              <div class="section-head">
                <h3>MCP</h3>
              </div>
              <div class="section-body">
                <div class="field-grid cols-4">
                  <FormItem label="启用 MCP 客户端">
                    <div class="switch-field">
                      <Switch v-model:checked="form.mcpClientEnabled" />
                    </div>
                  </FormItem>
                </div>
              </div>
            </section>

            <section id="ai-config-knowledge" class="config-section">
              <div class="section-head">
                <h3>知识库</h3>
              </div>
              <div class="section-body">
                <div class="field-grid cols-4">
                  <FormItem label="启用知识库">
                    <div class="switch-field">
                      <Switch v-model:checked="form.rag.enabled" />
                    </div>
                  </FormItem>
                  <FormItem label="Qdrant 集合名">
                    <Input v-model:value="form.rag.collectionName" />
                  </FormItem>
                  <FormItem class="span-2" label="Qdrant URL">
                    <Input v-model:value="form.rag.qdrantUrl" />
                  </FormItem>
                  <FormItem
                    class="span-2"
                    label="Qdrant API Key"
                    extra="已配置时显示 ******；需要修改时输入新的 Key"
                  >
                    <Input.Password
                      v-model:value="form.rag.qdrantApiKey"
                      placeholder="未开启鉴权可留空"
                    />
                  </FormItem>
                  <FormItem label="Qdrant 超时毫秒">
                    <InputNumber
                      v-model:value="form.rag.qdrantTimeoutMs"
                      class="w-full"
                      :min="1000"
                      :max="120000"
                      :step="1000"
                    />
                  </FormItem>
                </div>
              </div>
            </section>

            <section id="ai-config-embedding" class="config-section">
              <div class="section-head">
                <h3>Embedding</h3>
              </div>
              <div class="section-body">
                <div class="field-grid cols-4">
                  <FormItem class="span-2" label="Embedding Base URL">
                    <Input v-model:value="form.rag.embeddingBaseUrl" />
                  </FormItem>
                  <FormItem
                    class="span-2"
                    label="Embedding API Key"
                    extra="已配置时显示 ******；需要修改时输入新的 Key"
                  >
                    <Input.Password
                      v-model:value="form.rag.embeddingApiKey"
                      placeholder="保存后不回显明文"
                    />
                  </FormItem>
                  <FormItem class="span-3" label="Embedding 模型">
                    <Input v-model:value="form.rag.embeddingModel" />
                  </FormItem>
                  <FormItem label="Embedding 超时毫秒">
                    <InputNumber
                      v-model:value="form.rag.embeddingTimeoutMs"
                      class="w-full"
                      :min="1000"
                      :max="120000"
                      :step="1000"
                    />
                  </FormItem>
                </div>
              </div>
            </section>
          </AForm>
        </div>
      </div>
    </Spin>
  </Page>
</template>

<style scoped>
.ai-config-page {
  width: 100%;
}

.ai-config-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  padding: 2px 0;
}

.ai-config-title {
  font-size: 18px;
  font-weight: 600;
  line-height: 28px;
}

.ai-config-subtitle {
  color: hsl(var(--muted-foreground));
  font-size: 13px;
}

.ai-config-status {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 12px;
}

.status-item {
  display: flex;
  min-width: 0;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  min-height: 44px;
  padding: 9px 12px;
  border: 1px solid hsl(var(--border));
  border-radius: 6px;
  background: hsl(var(--card));
}

.status-item span {
  flex: none;
  color: hsl(var(--muted-foreground));
  font-size: 12px;
}

.status-item strong {
  min-width: 0;
  overflow: hidden;
  font-size: 13px;
  font-weight: 500;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ai-config-shell {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 12px;
  align-items: start;
}

.ai-config-nav {
  position: sticky;
  top: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px;
  border: 1px solid hsl(var(--border));
  border-radius: 6px;
  background: hsl(var(--card));
}

.nav-item {
  width: 100%;
  height: 34px;
  padding: 0 10px;
  border: 0;
  border-radius: 4px;
  background: transparent;
  color: hsl(var(--muted-foreground));
  cursor: pointer;
  font-size: 13px;
  text-align: left;
  transition:
    background-color 0.2s ease,
    color 0.2s ease;
}

.nav-item:hover,
.nav-item.active {
  background: hsl(var(--accent));
  color: hsl(var(--foreground));
}

.ai-config-form {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 12px;
}

.config-section {
  scroll-margin-top: 12px;
  overflow: hidden;
  border: 1px solid hsl(var(--border));
  border-radius: 6px;
  background: hsl(var(--card));
}

.section-head {
  display: flex;
  min-height: 44px;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid hsl(var(--border));
}

.section-head h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
}

.section-body {
  padding: 16px;
}

.field-grid {
  display: grid;
  gap: 12px;
}

.field-grid.cols-3 {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.field-grid.cols-4 {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.span-2 {
  grid-column: span 2;
}

.span-3 {
  grid-column: span 3;
}

.span-4 {
  grid-column: span 4;
}

.switch-field {
  display: flex;
  min-height: 32px;
  align-items: center;
}

:deep(.ant-form-item) {
  margin-bottom: 0;
}

@media (max-width: 768px) {
  .ai-config-header {
    align-items: stretch;
    flex-direction: column;
  }

  .ai-config-status {
    grid-template-columns: 1fr;
  }

  .ai-config-shell {
    grid-template-columns: 1fr;
  }

  .ai-config-nav {
    position: static;
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .field-grid.cols-3,
  .field-grid.cols-4 {
    grid-template-columns: 1fr;
  }

  .span-2,
  .span-3,
  .span-4 {
    grid-column: span 1;
  }
}

@media (min-width: 769px) and (max-width: 1180px) {
  .ai-config-status {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .field-grid.cols-3,
  .field-grid.cols-4 {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .span-3 {
    grid-column: span 2;
  }

  .span-4 {
    grid-column: span 2;
  }
}
</style>
