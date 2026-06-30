<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { FlowProcessApi } from '#/api/flow/process';

import { Page, useVbenModal } from '@vben/common-ui';

import { message, Modal as AModal } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { cancelInstance, getMyInitiated } from '#/api/flow/process';

import TrackModal from '../components/track-modal.vue';

const [Track, trackApi] = useVbenModal({ connectedComponent: TrackModal });

function onActionClick({
  code,
  row,
}: OnActionClickParams<FlowProcessApi.Instance>) {
  if (code === 'track') {
    trackApi.setData({ instanceId: row.id }).open();
  } else if (code === 'cancel') {
    AModal.confirm({
      title: '撤销确认',
      content: `确认撤销流程「${row.name}」？`,
      onOk: () =>
        cancelInstance(row.id).then(() => {
          message.success('已撤销');
          gridApi.query();
        }),
    });
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: [
      { field: 'name', title: '流程', minWidth: 160 },
      { field: 'processDefinitionKey', title: '流程标识', minWidth: 140 },
      {
        field: 'finished',
        title: '状态',
        width: 100,
        cellRender: {
          name: 'CellTag',
          options: [
            { label: '进行中', value: false, color: 'blue' },
            { label: '已结束', value: true, color: 'green' },
          ],
        },
      },
      { field: 'startTime', title: '发起时间', width: 180 },
      { field: 'endTime', title: '结束时间', width: 180 },
      {
        align: 'center',
        cellRender: {
          attrs: { onClick: onActionClick },
          name: 'CellOperation',
          options: [
            { code: 'track', text: '跟踪' },
            {
              code: 'cancel',
              text: '撤销',
              disabled: (row: FlowProcessApi.Instance) => !!row.finished,
            },
          ],
        },
        field: 'operation',
        fixed: 'right',
        title: '操作',
        width: 140,
      },
    ],
    height: 'auto',
    proxyConfig: {
      ajax: {
        query: async ({ page }) =>
          getMyInitiated({ page: page.currentPage, pageSize: page.pageSize }),
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: { refresh: true, zoom: true },
  } as VxeTableGridOptions<FlowProcessApi.Instance>,
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="我发起的" />
    <Track />
  </Page>
</template>
