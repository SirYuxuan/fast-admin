<script lang="ts" setup>
import type { OnActionClickParams, VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SysJobApi } from '#/api/system/job';

import { computed, ref, watch } from 'vue';
import { useRoute } from 'vue-router';

import { Page } from '@vben/common-ui';

import {
  Button,
  Descriptions,
  DescriptionsItem,
  message,
  Modal as AModal,
  Tag,
} from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { cleanJobLog, deleteJobLog, getJobLogPage } from '#/api/system/job';

import { useColumns, useGridFormSchema } from './data';

const route = useRoute();
const detailOpen = ref(false);
const detailRow = ref<SysJobApi.JobLog>();
const routeJobId = computed(() => firstQueryValue(route.query.jobId));
const routeJobName = computed(() => firstQueryValue(route.query.jobName));

function firstQueryValue(value: unknown) {
  return Array.isArray(value) ? value[0] : (value as string | undefined);
}

function statusColor(status?: number) {
  if (status === 2) return 'processing';
  return status === 1 ? 'success' : 'error';
}

function statusText(status?: number) {
  if (status === 2) return '执行中';
  return status === 1 ? '成功' : '失败';
}

function onActionClick({ code, row }: OnActionClickParams<SysJobApi.JobLog>) {
  if (code === 'detail') {
    detailRow.value = row;
    detailOpen.value = true;
  } else if (code === 'delete') {
    AModal.confirm({
      title: '删除确认',
      content: '确认删除该条日志？',
      onOk: () =>
        deleteJobLog(row.id).then(() => {
          message.success('删除成功');
          refresh();
        }),
    });
  }
}

function onClean() {
  AModal.confirm({
    title: '清空确认',
    content: '将永久删除所有任务执行日志，确定吗？',
    onOk: () =>
      cleanJobLog().then(() => {
        message.success('已清空');
        refresh();
      }),
  });
}

const [Grid, gridApi] = useVbenVxeGrid({
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
          getJobLogPage({
            page: page.currentPage,
            pageSize: page.pageSize,
            jobId: routeJobId.value,
            ...formValues,
          }),
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: { custom: true, export: false, refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<SysJobApi.JobLog>,
});

function refresh() {
  gridApi.query();
}

watch(routeJobId, () => {
  refresh();
});
</script>

<template>
  <Page auto-content-height>
    <Grid :table-title="routeJobName ? `任务执行日志：${routeJobName}` : '任务执行日志'">
      <template #toolbar-tools>
        <Button danger @click="onClean">清空日志</Button>
      </template>
    </Grid>
    <AModal v-model:open="detailOpen" title="任务日志详情" width="760px" :footer="null">
      <Descriptions v-if="detailRow" bordered size="small" :column="2">
        <DescriptionsItem label="任务名称">{{ detailRow.jobName }}</DescriptionsItem>
        <DescriptionsItem label="分组">{{ detailRow.jobGroup || '-' }}</DescriptionsItem>
        <DescriptionsItem label="Bean">{{ detailRow.beanName || '-' }}</DescriptionsItem>
        <DescriptionsItem label="方法">{{ detailRow.methodName || '-' }}</DescriptionsItem>
        <DescriptionsItem label="状态">
          <Tag :color="statusColor(detailRow.status)">{{ statusText(detailRow.status) }}</Tag>
        </DescriptionsItem>
        <DescriptionsItem label="耗时">
          {{ detailRow.costTime == null ? '-' : `${detailRow.costTime} ms` }}
        </DescriptionsItem>
        <DescriptionsItem label="执行时间" :span="2">
          {{ detailRow.createdAt || '-' }}
        </DescriptionsItem>
        <DescriptionsItem label="方法参数" :span="2">
          <pre class="m-0 whitespace-pre-wrap text-xs leading-5">{{
            detailRow.methodParams || '-'
          }}</pre>
        </DescriptionsItem>
        <DescriptionsItem v-if="detailRow.errorMsg" label="错误信息" :span="2">
          <pre class="m-0 whitespace-pre-wrap text-xs leading-5 text-red-500">{{
            detailRow.errorMsg
          }}</pre>
        </DescriptionsItem>
      </Descriptions>
    </AModal>
  </Page>
</template>
