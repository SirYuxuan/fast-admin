<script lang="ts" setup>
import type { AiMcpApi } from '#/api/ai/mcp';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Button, Input, message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { createAiMcpServer, updateAiMcpServer } from '#/api/ai/mcp';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<AiMcpApi.McpServer>();
const currentTransport = ref<AiMcpApi.Transport>();
const argRows = ref<RowValue[]>([createValueRow()]);
const headerRows = ref<HeaderRow[]>([createHeaderRow()]);
const getTitle = computed(() =>
  formData.value?.id ? '编辑 MCP 服务' : '新增 MCP 服务',
);
const showArgs = computed(() => currentTransport.value === 'stdio');
const showHeaders = computed(
  () => !!currentTransport.value && currentTransport.value !== 'stdio',
);

type HeaderRow = {
  id: string;
  name: string;
  value: string;
};

type RowValue = {
  id: string;
  value: string;
};

function createValueRow(value = ''): RowValue {
  return { id: crypto.randomUUID(), value };
}

function createHeaderRow(name = '', value = ''): HeaderRow {
  return { id: crypto.randomUUID(), name, value };
}

function parseArgsJson(value?: string) {
  if (!value) {
    return [createValueRow()];
  }
  try {
    const parsed = JSON.parse(value);
    if (Array.isArray(parsed)) {
      return parsed.length
        ? parsed.map((item) => createValueRow(String(item ?? '')))
        : [createValueRow()];
    }
  } catch {
    // Keep invalid legacy content visible so the user can fix it in place.
  }
  return [createValueRow(value)];
}

function parseHeadersJson(value?: string) {
  if (!value) {
    return [createHeaderRow()];
  }
  try {
    const parsed = JSON.parse(value);
    if (parsed && typeof parsed === 'object' && !Array.isArray(parsed)) {
      const entries = Object.entries(parsed);
      return entries.length
        ? entries.map(([name, headerValue]) => createHeaderRow(name, String(headerValue ?? '')))
        : [createHeaderRow()];
    }
  } catch {
    // Keep invalid legacy content visible so the user can fix it in place.
  }
  return [createHeaderRow('', value)];
}

function addArgRow() {
  argRows.value.push(createValueRow());
}

function removeArgRow(id: string) {
  argRows.value = argRows.value.filter((row) => row.id !== id);
  if (!argRows.value.length) {
    addArgRow();
  }
}

function addHeaderRow() {
  headerRows.value.push(createHeaderRow());
}

function removeHeaderRow(id: string) {
  headerRows.value = headerRows.value.filter((row) => row.id !== id);
  if (!headerRows.value.length) {
    addHeaderRow();
  }
}

function buildArgsJson() {
  const args = argRows.value.map((row) => row.value.trim()).filter(Boolean);
  return args.length ? JSON.stringify(args) : undefined;
}

function buildHeadersJson() {
  const headers: Record<string, string> = {};
  for (const row of headerRows.value) {
    const name = row.name.trim();
    const value = row.value.trim();
    if (!name && !value) {
      continue;
    }
    if (!name) {
      message.warning('请求头名称不能为空');
      return false;
    }
    headers[name] = value;
  }
  return Object.keys(headers).length ? JSON.stringify(headers) : undefined;
}

function resetRows(data?: AiMcpApi.McpServer) {
  currentTransport.value = data?.transport;
  argRows.value = parseArgsJson(data?.argsJson);
  headerRows.value = parseHeadersJson(data?.headersJson);
}

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    labelWidth: 110,
  },
  layout: 'horizontal',
  schema: useFormSchema((value) => {
    currentTransport.value = value;
  }),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  class: 'w-[760px]',
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    modalApi.lock();
    try {
      const data = await formApi.getValues();
      if (data.transport === 'stdio') {
        data.argsJson = buildArgsJson();
        data.headersJson = undefined;
      } else {
        const headersJson = buildHeadersJson();
        if (headersJson === false) {
          return;
        }
        data.argsJson = undefined;
        data.headersJson = headersJson;
      }
      if (formData.value?.id) {
        await updateAiMcpServer({ ...data, id: formData.value.id });
      } else {
        await createAiMcpServer(data);
      }
      modalApi.close();
      emit('success');
    } finally {
      modalApi.lock(false);
    }
  },
  onOpenChange(isOpen) {
    if (isOpen) {
      const data = modalApi.getData<AiMcpApi.McpServer>();
      formData.value = data ?? undefined;
      formApi.resetForm();
      resetRows(data);
      if (data) formApi.setValues(data);
    }
  },
});
</script>

<template>
  <Modal :title="getTitle">
    <Form class="mx-4" />
    <div v-if="showArgs" class="mcp-dynamic-field">
      <div class="mcp-dynamic-label">命令参数</div>
      <div class="mcp-dynamic-body">
        <div v-for="row in argRows" :key="row.id" class="mcp-row">
          <Input v-model:value="row.value" placeholder="例如 --port" />
          <Button danger type="text" @click="removeArgRow(row.id)">删除</Button>
        </div>
        <Button size="small" type="dashed" @click="addArgRow">新增参数</Button>
      </div>
    </div>
    <div v-if="showHeaders" class="mcp-dynamic-field">
      <div class="mcp-dynamic-label">请求头</div>
      <div class="mcp-dynamic-body">
        <div v-for="row in headerRows" :key="row.id" class="mcp-row">
          <Input v-model:value="row.name" class="mcp-header-name" placeholder="Header 名称" />
          <Input v-model:value="row.value" placeholder="Header 值" />
          <Button danger type="text" @click="removeHeaderRow(row.id)">删除</Button>
        </div>
        <Button size="small" type="dashed" @click="addHeaderRow">新增请求头</Button>
      </div>
    </div>
  </Modal>
</template>

<style scoped>
.mcp-dynamic-field {
  display: grid;
  grid-template-columns: 110px minmax(0, 1fr);
  gap: 12px;
  margin: 0 16px 18px;
}

.mcp-dynamic-label {
  color: hsl(var(--foreground));
  font-size: 14px;
  line-height: 32px;
  text-align: end;
}

.mcp-dynamic-body {
  display: grid;
  min-width: 0;
  gap: 8px;
}

.mcp-row {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 8px;
}

.mcp-header-name {
  width: 180px;
  flex: none;
}
</style>
