<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { FlowCcApi } from '#/api/flow/cc';

import { Page, useVbenModal } from '@vben/common-ui';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getMyCc, markCcRead } from '#/api/flow/cc';

import TrackModal from '../components/track-modal.vue';

const [Track, trackApi] = useVbenModal({ connectedComponent: TrackModal });

function onActionClick({ code, row }: OnActionClickParams<FlowCcApi.CcRecord>) {
  if (code === 'track') {
    if (row.isRead === 0) {
      markCcRead(row.id).then(() => gridApi.query());
    }
    trackApi.setData({ instanceId: row.processInstanceId }).open();
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: [
      { field: 'processName', title: '流程', minWidth: 160 },
      { field: 'taskName', title: '抄送节点', minWidth: 140 },
      { field: 'creatorName', title: '抄送人', width: 120 },
      {
        field: 'isRead',
        title: '状态',
        width: 90,
        cellRender: {
          name: 'CellTag',
          options: [
            { label: '未读', value: 0, color: 'red' },
            { label: '已读', value: 1, color: 'default' },
          ],
        },
      },
      { field: 'createdAt', title: '抄送时间', width: 180 },
      {
        align: 'center',
        cellRender: {
          attrs: { onClick: onActionClick },
          name: 'CellOperation',
          options: [{ code: 'track', text: '查看' }],
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
          getMyCc({ page: page.currentPage, pageSize: page.pageSize }),
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: { refresh: true, zoom: true },
  } as VxeTableGridOptions<FlowCcApi.CcRecord>,
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="抄送我的" />
    <Track />
  </Page>
</template>
