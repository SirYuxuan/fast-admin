<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';

import { Page, useVbenModal } from '@vben/common-ui';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getDoneTasks } from '#/api/flow/task';

import TrackModal from '../components/track-modal.vue';

const [Track, trackApi] = useVbenModal({ connectedComponent: TrackModal });

function onActionClick({ code, row }: OnActionClickParams<any>) {
  if (code === 'track') {
    trackApi.setData({ instanceId: row.processInstanceId }).open();
  }
}

const [Grid] = useVbenVxeGrid({
  gridOptions: {
    columns: [
      { field: 'taskName', title: '任务节点', minWidth: 160 },
      { field: 'processName', title: '所属流程', minWidth: 160 },
      { field: 'startTime', title: '开始时间', width: 180 },
      { field: 'endTime', title: '完成时间', width: 180 },
      {
        align: 'center',
        cellRender: {
          attrs: { onClick: onActionClick },
          name: 'CellOperation',
          options: [{ code: 'track', text: '跟踪' }],
        },
        field: 'operation',
        fixed: 'right',
        title: '操作',
        width: 100,
      },
    ],
    height: 'auto',
    proxyConfig: {
      ajax: {
        query: async ({ page }) =>
          getDoneTasks({ page: page.currentPage, pageSize: page.pageSize }),
      },
    },
    rowConfig: { keyField: 'taskId' },
    toolbarConfig: { refresh: true, zoom: true },
  } as VxeTableGridOptions<any>,
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="我的已办" />
    <Track />
  </Page>
</template>
