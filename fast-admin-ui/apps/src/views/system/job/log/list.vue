<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SysJobApi } from '#/api/system/job';

import { Page } from '@vben/common-ui';

import { Button, message, Modal as AModal } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { cleanJobLog, deleteJobLog, getJobLogPage } from '#/api/system/job';

import { useColumns, useGridFormSchema } from './data';

function onActionClick({ code, row }: OnActionClickParams<SysJobApi.JobLog>) {
  if (code === 'delete') {
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
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="任务执行日志">
      <template #toolbar-tools>
        <Button danger @click="onClean">清空日志</Button>
      </template>
    </Grid>
  </Page>
</template>
