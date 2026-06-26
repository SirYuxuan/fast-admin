<script lang="ts" setup>
import type { TableColumnsType } from 'ant-design-vue';

import type { AiUsageApi } from '#/api/ai/usage';

import { onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';

import {
  Card,
  Col,
  Row,
  Select,
  Statistic,
  Table,
} from 'ant-design-vue';

import { getAiUsageStats } from '#/api/ai/usage';

const days = ref(14);
const loading = ref(false);
const stats = ref<AiUsageApi.UsageStats>({
  totalMessages: 0,
  promptTokens: 0,
  completionTokens: 0,
  totalTokens: 0,
  byModel: [],
  byDay: [],
});

const dayOptions = [
  { label: '近 7 天', value: 7 },
  { label: '近 14 天', value: 14 },
  { label: '近 30 天', value: 30 },
  { label: '近 90 天', value: 90 },
];

const modelColumns: TableColumnsType = [
  {
    dataIndex: 'modelName',
    title: '模型',
    customRender: ({ record }) => record.modelName || record.modelCode,
  },
  { dataIndex: 'modelCode', title: '模型编码' },
  { dataIndex: 'messages', title: '回复条数', align: 'right' },
  { dataIndex: 'totalTokens', title: '总 Token', align: 'right' },
];

const dayColumns: TableColumnsType = [
  { dataIndex: 'day', title: '日期' },
  { dataIndex: 'messages', title: '回复条数', align: 'right' },
  { dataIndex: 'totalTokens', title: '总 Token', align: 'right' },
];

async function load() {
  loading.value = true;
  try {
    stats.value = await getAiUsageStats(days.value);
  } finally {
    loading.value = false;
  }
}

function onDaysChange() {
  load();
}

onMounted(load);
</script>

<template>
  <Page auto-content-height>
    <div class="mb-3 flex items-center justify-end">
      <Select
        v-model:value="days"
        :options="dayOptions"
        style="width: 140px"
        @change="onDaysChange"
      />
    </div>

    <Row :gutter="12">
      <Col :span="6">
        <Card><Statistic :loading="loading" :value="stats.totalMessages" title="助手回复条数" /></Card>
      </Col>
      <Col :span="6">
        <Card><Statistic :loading="loading" :value="stats.promptTokens" title="输入 Token" /></Card>
      </Col>
      <Col :span="6">
        <Card><Statistic :loading="loading" :value="stats.completionTokens" title="输出 Token" /></Card>
      </Col>
      <Col :span="6">
        <Card><Statistic :loading="loading" :value="stats.totalTokens" title="总 Token" /></Card>
      </Col>
    </Row>

    <Row :gutter="12" class="mt-3">
      <Col :span="12">
        <Card title="按模型用量">
          <Table
            :columns="modelColumns"
            :data-source="stats.byModel"
            :loading="loading"
            :pagination="false"
            row-key="modelCode"
            size="small"
          />
        </Card>
      </Col>
      <Col :span="12">
        <Card title="每日用量">
          <Table
            :columns="dayColumns"
            :data-source="stats.byDay"
            :loading="loading"
            :pagination="false"
            row-key="day"
            size="small"
            :scroll="{ y: 320 }"
          />
        </Card>
      </Col>
    </Row>
  </Page>
</template>
