<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { AiModelApi } from '#/api/ai/model';

import { AccessControl } from '@vben/access';
import { Page, useVbenModal } from '@vben/common-ui';
import { Plus, RotateCw } from '@vben/icons';

import { Button, message, Modal as AModal, Tooltip } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  activateAiModel,
  changeAiModelEnabled,
  deleteAiModel,
  getAiModelPage,
  testAiModel,
} from '#/api/ai/model';

import { useColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

function onActionClick({ code, row }: OnActionClickParams<AiModelApi.ModelConfig>) {
  if (code === 'activate') {
    onActivate(row);
  } else if (code === 'delete') {
    onDelete(row);
  } else if (code === 'edit') {
    formModalApi.setData(row).open();
  } else if (code === 'test') {
    onTest(row);
  }
}

async function onTest(row: AiModelApi.ModelConfig) {
  if (row.testingLatency) {
    return;
  }
  const hide = message.loading(`正在测试「${row.name}」...`, 0);
  row.testingLatency = true;
  try {
    const result = await testAiModel({
      id: row.id,
      provider: row.provider,
      model: row.model,
      baseUrl: row.baseUrl,
    });
    message.success(`连接成功，延时 ${result.latencyMs} ms`);
  } finally {
    row.testingLatency = false;
    hide();
    refreshGrid();
  }
}

function onActivate(row: AiModelApi.ModelConfig) {
  if (row.active) {
    message.info('该模型已是当前模型');
    return;
  }
  AModal.confirm({
    content: `确认将「${row.name}」设为当前模型？`,
    title: '切换确认',
    onOk: () =>
      activateAiModel(row.id).then(() => {
        message.success('切换成功');
        refreshGrid();
      }),
  });
}

function onDelete(row: AiModelApi.ModelConfig) {
  AModal.confirm({
    content: `确认删除模型配置「${row.name}」？`,
    title: '删除确认',
    onOk: () =>
      deleteAiModel(row.id).then(() => {
        message.success('删除成功');
        refreshGrid();
      }),
  });
}

async function onEnabledChange(
  newVal: boolean,
  row: AiModelApi.ModelConfig,
): Promise<boolean> {
  try {
    await changeAiModelEnabled(row.id, newVal);
    message.success(newVal ? '已启用' : '已禁用');
    return true;
  } catch {
    return false;
  }
}

async function onActiveChange(
  newVal: boolean,
  row: AiModelApi.ModelConfig,
): Promise<boolean> {
  if (!newVal) {
    message.info('至少保留一个当前模型');
    return false;
  }
  if (row.active) {
    return false;
  }
  try {
    await activateAiModel(row.id);
    message.success('切换成功');
    refreshGrid();
    return true;
  } catch {
    return false;
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
    submitOnEnter: true,
    collapsed: true,
  },
  gridOptions: {
    columns: useColumns(onActionClick, onEnabledChange, onActiveChange),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) =>
          getAiModelPage({
            page: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          }),
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: {
      custom: true,
      export: false,
      refresh: true,
      search: true,
      zoom: true,
    },
  } as VxeTableGridOptions<AiModelApi.ModelConfig>,
});

function refreshGrid() {
  gridApi.query();
}
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="refreshGrid" />
    <Grid table-title="AI 模型管理">
      <template #latency="{ row }">
        <span class="inline-flex items-center gap-1">
          <span v-if="row.lastTestedAt">
            <span :class="row.lastTestOk ? 'text-green-600' : 'text-red-500'">
              {{ row.lastTestOk ? `${row.lastLatencyMs} ms` : '失败' }}
            </span>
          </span>
          <span v-else class="text-gray-400">未测试</span>
          <Tooltip title="刷新延时">
            <Button
              type="text"
              size="small"
              class="latency-refresh"
              :loading="row.testingLatency"
              @click.stop="onTest(row)"
            >
              <template #icon>
                <RotateCw class="size-3.5" />
              </template>
            </Button>
          </Tooltip>
        </span>
      </template>
      <template #toolbar-tools>
        <AccessControl type="code" :codes="['ai:model:add']">
          <Button type="primary" @click="formModalApi.setData(null).open()">
            <Plus class="size-5" />
            新增模型
          </Button>
        </AccessControl>
      </template>
    </Grid>
  </Page>
</template>
