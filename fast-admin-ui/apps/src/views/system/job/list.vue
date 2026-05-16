<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SysJobApi } from '#/api/system/job';

import { useRouter } from 'vue-router';

import { AccessControl } from '@vben/access';
import { Page, useVbenModal } from '@vben/common-ui';
import { Plus } from '@vben/icons';

import { Button, message, Modal as AModal } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteJob,
  getJobPage,
  pauseJob,
  runJobOnce,
  startJob,
} from '#/api/system/job';

import { useColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const router = useRouter();
const [FormModal, formModalApi] = useVbenModal({ connectedComponent: Form });

function onActionClick({ code, row }: OnActionClickParams<SysJobApi.Job>) {
  switch (code) {
    case 'delete': {
      AModal.confirm({
        title: '删除确认',
        content: `确认删除任务「${row.jobName}」？`,
        onOk: () =>
          deleteJob(row.id).then(() => {
            message.success('删除成功');
            refresh();
          }),
      });
      break;
    }
    case 'edit': {
      formModalApi.setData(row).open();
      break;
    }
    case 'run': {
      runJobOnce(row.id).then(() => {
        message.success(`已触发执行：${row.jobName}`);
      });
      break;
    }
    case 'toggle': {
      const isRunning = row.status === 1;
      const promise = isRunning ? pauseJob(row.id) : startJob(row.id);
      promise.then(() => {
        message.success(isRunning ? '已暂停' : '已启动');
        refresh();
      });
      break;
    }
  }
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
          getJobPage({
            page: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          }),
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: { custom: true, export: false, refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<SysJobApi.Job>,
});

function refresh() {
  gridApi.query();
}

function onSuccess() {
  message.success('保存成功');
  refresh();
}

function gotoLog() {
  router.push('/system/job/log');
}
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="定时任务">
      <template #toolbar-tools>
        <div class="flex items-center gap-2">
          <Button @click="gotoLog">执行日志</Button>
          <AccessControl :codes="['system:job:add']">
            <Button type="primary" @click="formModalApi.setData(null).open()">
              <Plus class="size-5" />
              新增任务
            </Button>
          </AccessControl>
        </div>
      </template>
    </Grid>
    <FormModal @success="onSuccess" />
  </Page>
</template>
