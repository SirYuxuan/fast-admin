<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { FlowDefinitionApi } from '#/api/flow/definition';

import { Page, useVbenModal } from '@vben/common-ui';

import { message, Modal as AModal } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  activateDefinition,
  deleteDeployment,
  getDefinitionPage,
  suspendDefinition,
} from '#/api/flow/definition';

import { useColumns, useGridFormSchema } from './data';
import VersionsModal from './modules/versions-modal.vue';

const [Versions, versionsApi] = useVbenModal({
  connectedComponent: VersionsModal,
});

function onActionClick({
  code,
  row,
}: OnActionClickParams<FlowDefinitionApi.Definition>) {
  switch (code) {
    case 'versions': {
      versionsApi.setData({ key: row.key, name: row.name }).open();
      break;
    }
    case 'activate': {
      activateDefinition(row.id).then(() => {
        message.success('已激活');
        refresh();
      });
      break;
    }
    case 'delete': {
      AModal.confirm({
        title: '删除确认',
        content: `删除流程「${row.name} v${row.version}」的部署？运行中的实例将一并删除。`,
        onOk: () =>
          deleteDeployment(row.deploymentId, true).then(() => {
            message.success('删除成功');
            refresh();
          }),
      });
      break;
    }
    case 'suspend': {
      suspendDefinition(row.id).then(() => {
        message.success('已挂起');
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
          getDefinitionPage({
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
  } as VxeTableGridOptions<FlowDefinitionApi.Definition>,
});

function refresh() {
  gridApi.query();
}
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="流程定义" />
    <Versions @changed="refresh" />
  </Page>
</template>
