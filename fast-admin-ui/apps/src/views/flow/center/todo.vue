<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { FlowTaskApi } from '#/api/flow/task';

import { Page, useVbenModal } from '@vben/common-ui';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getTodoTasks } from '#/api/flow/task';

import ApproveModal from './modules/approve-modal.vue';

const [Approve, approveApi] = useVbenModal({ connectedComponent: ApproveModal });

function onActionClick({ code, row }: OnActionClickParams<FlowTaskApi.TodoTask>) {
  if (code === 'approve') {
    approveApi.setData({ taskId: row.taskId, taskName: row.taskName }).open();
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: [
      { field: 'taskName', title: '任务节点', minWidth: 160 },
      { field: 'processName', title: '所属流程', minWidth: 160 },
      {
        field: 'claimed',
        title: '签收',
        width: 90,
        cellRender: {
          name: 'CellTag',
          options: [
            { label: '已签收', value: true, color: 'green' },
            { label: '待签收', value: false, color: 'orange' },
          ],
        },
      },
      { field: 'createTime', title: '到达时间', width: 180 },
      {
        align: 'center',
        cellRender: {
          attrs: { onClick: onActionClick },
          name: 'CellOperation',
          options: [{ code: 'approve', text: '审批', authCode: 'flow:task:todo' }],
        },
        field: 'operation',
        fixed: 'right',
        title: '操作',
        width: 120,
      },
    ],
    height: 'auto',
    proxyConfig: {
      ajax: {
        query: async ({ page }) =>
          getTodoTasks({ page: page.currentPage, pageSize: page.pageSize }),
      },
    },
    rowConfig: { keyField: 'taskId' },
    toolbarConfig: { refresh: true, zoom: true },
  } as VxeTableGridOptions<FlowTaskApi.TodoTask>,
});

function onSuccess() {
  gridApi.query();
}
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="待办任务" />
    <Approve @success="onSuccess" />
  </Page>
</template>
