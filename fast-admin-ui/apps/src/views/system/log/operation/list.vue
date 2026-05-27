<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SysLogApi } from '#/api/system/log';

import { ref } from 'vue';

import { AccessControl } from '@vben/access';
import { Page } from '@vben/common-ui';

import {
  Button,
  Descriptions,
  DescriptionsItem,
  Drawer,
  message,
  Modal as AModal,
  Tag,
} from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  cleanOperationLog,
  deleteOperationLog,
  getOperationLogPage,
} from '#/api/system/log';

import { useColumns, useGridFormSchema } from './data';

const detailOpen = ref(false);
const detailRow = ref<SysLogApi.OperationLog>();

function onActionClick({
  code,
  row,
}: OnActionClickParams<SysLogApi.OperationLog>) {
  switch (code) {
    case 'delete': {
      AModal.confirm({
        title: '删除确认',
        content: `确认删除该条日志？`,
        onOk: () =>
          deleteOperationLog(row.id).then(() => {
            message.success('删除成功');
            refresh();
          }),
      });
      break;
    }
    case 'detail': {
      detailRow.value = row;
      detailOpen.value = true;
      break;
    }
  }
}

function onClean() {
  AModal.confirm({
    title: '清空确认',
    content: '将永久删除所有操作日志，确定吗？',
    onOk: () =>
      cleanOperationLog().then(() => {
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
          getOperationLogPage({
            page: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          }),
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: { custom: true, export: false, refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<SysLogApi.OperationLog>,
});

function refresh() {
  gridApi.query();
}
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="操作日志">
      <template #toolbar-tools>
        <AccessControl type="code" :codes="['system:log:operation:delete']">
          <Button danger @click="onClean">清空日志</Button>
        </AccessControl>
      </template>
    </Grid>

    <Drawer
      v-model:open="detailOpen"
      title="操作日志详情"
      :width="640"
      placement="right"
    >
      <Descriptions v-if="detailRow" :column="1" bordered size="small">
        <DescriptionsItem label="操作模块">{{ detailRow.title }}</DescriptionsItem>
        <DescriptionsItem label="业务类型">{{ detailRow.businessType }}</DescriptionsItem>
        <DescriptionsItem label="方法">{{ detailRow.method }}</DescriptionsItem>
        <DescriptionsItem label="请求 URL">
          <Tag>{{ detailRow.requestMethod }}</Tag>
          {{ detailRow.url }}
        </DescriptionsItem>
        <DescriptionsItem label="操作人ID">{{ detailRow.userId || '-' }}</DescriptionsItem>
        <DescriptionsItem label="IP">{{ detailRow.ip }}</DescriptionsItem>
        <DescriptionsItem label="状态">
          <Tag :color="detailRow.status === 1 ? 'success' : 'error'">
            {{ detailRow.status === 1 ? '成功' : '失败' }}
          </Tag>
          <span v-if="detailRow.errorMsg" class="ml-2 text-red-500">
            {{ detailRow.errorMsg }}
          </span>
        </DescriptionsItem>
        <DescriptionsItem label="耗时">{{ detailRow.costTime }} ms</DescriptionsItem>
        <DescriptionsItem label="操作时间">{{ detailRow.createdAt }}</DescriptionsItem>
        <DescriptionsItem label="请求参数">
          <pre class="log-pre">{{ detailRow.requestParams || '-' }}</pre>
        </DescriptionsItem>
        <DescriptionsItem label="响应结果">
          <pre class="log-pre">{{ detailRow.responseResult || '-' }}</pre>
        </DescriptionsItem>
      </Descriptions>
    </Drawer>
  </Page>
</template>

<style scoped>
.log-pre {
  margin: 0;
  padding: 8px;
  background: #fafafa;
  border-radius: 4px;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 240px;
  overflow: auto;
}
</style>
