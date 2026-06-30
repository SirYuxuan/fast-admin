<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { FlowFormApi } from '#/api/flow/form';

import { AccessControl } from '@vben/access';
import { Page, useVbenModal } from '@vben/common-ui';
import { Plus } from '@vben/icons';

import { Button, message, Modal as AModal } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteForm, getFormPage } from '#/api/flow/form';

import { useColumns, useGridFormSchema } from './data';
import Builder from './modules/builder.vue';

const [BuilderModal, builderApi] = useVbenModal({ connectedComponent: Builder });

function onActionClick({ code, row }: OnActionClickParams<FlowFormApi.Form>) {
  if (code === 'edit') {
    builderApi.setData(row).open();
  } else if (code === 'delete') {
    AModal.confirm({
      title: '删除确认',
      content: `确认删除表单「${row.name}」？`,
      onOk: () =>
        deleteForm(row.id).then(() => {
          message.success('删除成功');
          refresh();
        }),
    });
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: { schema: useGridFormSchema(), submitOnEnter: true, collapsed: true },
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) =>
          getFormPage({
            page: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          }),
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: { refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<FlowFormApi.Form>,
});

function refresh() {
  gridApi.query();
}
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="表单管理">
      <template #toolbar-tools>
        <AccessControl type="code" :codes="['flow:form:add']">
          <Button type="primary" @click="builderApi.setData(null).open()">
            <Plus class="size-5" />
            新增表单
          </Button>
        </AccessControl>
      </template>
    </Grid>
    <BuilderModal @success="refresh" />
  </Page>
</template>
