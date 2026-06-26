<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { AiToolLogApi } from '#/api/ai/tool-log';

import { ref } from 'vue';

import { Page } from '@vben/common-ui';

import { Descriptions, DescriptionsItem, Modal as AModal, Tag } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getAiToolLogPage } from '#/api/ai/tool-log';

import { useColumns, useGridFormSchema } from './data';

const detailOpen = ref(false);
const detailRow = ref<AiToolLogApi.ToolCallLog | null>(null);

function prettyJson(raw?: string) {
  if (!raw) return '-';
  try {
    return JSON.stringify(JSON.parse(raw), null, 2);
  } catch {
    return raw;
  }
}

function onActionClick({ code, row }: OnActionClickParams<AiToolLogApi.ToolCallLog>) {
  if (code === 'detail') {
    detailRow.value = row;
    detailOpen.value = true;
  }
}

const [Grid] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
    submitOnEnter: true,
    collapsed: true,
  },
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) =>
          getAiToolLogPage({
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
  } as VxeTableGridOptions<AiToolLogApi.ToolCallLog>,
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="AI 工具调用日志" />
    <AModal
      v-model:open="detailOpen"
      :footer="null"
      title="调用详情"
      width="760px"
    >
      <Descriptions
        v-if="detailRow"
        :column="2"
        :label-style="{ width: '88px', whiteSpace: 'nowrap' }"
        bordered
        size="small"
      >
        <DescriptionsItem label="工具">{{ detailRow.toolName }}</DescriptionsItem>
        <DescriptionsItem label="来源">{{ detailRow.source }}</DescriptionsItem>
        <DescriptionsItem label="结果">
          <Tag :color="detailRow.success ? 'green' : 'red'">
            {{ detailRow.success ? '成功' : '失败' }}
          </Tag>
        </DescriptionsItem>
        <DescriptionsItem label="耗时">
          {{ detailRow.costMs == null ? '-' : `${detailRow.costMs} ms` }}
        </DescriptionsItem>
        <DescriptionsItem label="操作人">{{ detailRow.operatorId || '-' }}</DescriptionsItem>
        <DescriptionsItem label="调用时间">{{ detailRow.createdAt || '-' }}</DescriptionsItem>
        <DescriptionsItem :span="2" label="会话 ID">{{ detailRow.sessionId || '-' }}</DescriptionsItem>
        <DescriptionsItem v-if="detailRow.errorMsg" :span="2" label="错误信息">
          {{ detailRow.errorMsg }}
        </DescriptionsItem>
        <DescriptionsItem :span="2" label="入参">
          <pre class="ai-log-pre">{{ prettyJson(detailRow.argumentsJson) }}</pre>
        </DescriptionsItem>
        <DescriptionsItem :span="2" label="结果">
          <pre class="ai-log-pre">{{ prettyJson(detailRow.resultJson) }}</pre>
        </DescriptionsItem>
      </Descriptions>
    </AModal>
  </Page>
</template>

<style scoped>
.ai-log-pre {
  max-height: 280px;
  margin: 0;
  overflow: auto;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
