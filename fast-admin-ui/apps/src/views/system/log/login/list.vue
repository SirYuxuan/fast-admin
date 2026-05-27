<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SysLogApi } from '#/api/system/log';

import { AccessControl } from '@vben/access';
import { Page } from '@vben/common-ui';

import { Button, message, Modal as AModal } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { cleanLoginLog, deleteLoginLog, getLoginLogPage } from '#/api/system/log';

import { useColumns, useGridFormSchema } from './data';

function onActionClick({ code, row }: OnActionClickParams<SysLogApi.LoginLog>) {
  if (code === 'delete') {
    AModal.confirm({
      title: '删除确认',
      content: '确认删除该条日志？',
      onOk: () =>
        deleteLoginLog(row.id).then(() => {
          message.success('删除成功');
          refresh();
        }),
    });
  }
}

function onClean() {
  AModal.confirm({
    title: '清空确认',
    content: '将永久删除所有登录日志，确定吗？',
    onOk: () =>
      cleanLoginLog().then(() => {
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
          getLoginLogPage({
            page: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          }),
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: { custom: true, export: false, refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<SysLogApi.LoginLog>,
});

function refresh() {
  gridApi.query();
}
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="登录日志">
      <template #toolbar-tools>
        <AccessControl type="code" :codes="['system:log:login:delete']">
          <Button danger @click="onClean">清空日志</Button>
        </AccessControl>
      </template>
    </Grid>
  </Page>
</template>
