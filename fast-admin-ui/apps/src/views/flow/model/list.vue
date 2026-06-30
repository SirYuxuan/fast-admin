<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { FlowModelApi } from '#/api/flow/model';

import { AccessControl } from '@vben/access';
import { Page, useVbenModal } from '@vben/common-ui';
import { Plus } from '@vben/icons';

import { Button, message, Modal as AModal } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteModel, deployModel, getModelPage } from '#/api/flow/model';

import { useColumns, useGridFormSchema } from './data';
import Designer from './modules/designer.vue';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({ connectedComponent: Form });
const [DesignerModal, designerModalApi] = useVbenModal({
  connectedComponent: Designer,
});

function onActionClick({ code, row }: OnActionClickParams<FlowModelApi.Model>) {
  switch (code) {
    case 'delete': {
      AModal.confirm({
        title: '删除确认',
        content: `确认删除模型「${row.name}」？`,
        onOk: () =>
          deleteModel(row.id).then(() => {
            message.success('删除成功');
            refresh();
          }),
      });
      break;
    }
    case 'deploy': {
      AModal.confirm({
        title: '部署确认',
        content: `将模型「${row.name}」部署为新版本流程定义？`,
        onOk: () =>
          deployModel(row.id).then(() => {
            message.success('部署成功');
            refresh();
          }),
      });
      break;
    }
    case 'design': {
      designerModalApi.setData(row).open();
      break;
    }
    case 'edit': {
      formModalApi.setData(row).open();
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
          getModelPage({
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
  } as VxeTableGridOptions<FlowModelApi.Model>,
});

function refresh() {
  gridApi.query();
}

function onSuccess() {
  refresh();
}
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="流程模型">
      <template #toolbar-tools>
        <AccessControl type="code" :codes="['flow:model:add']">
          <Button type="primary" @click="formModalApi.setData(null).open()">
            <Plus class="size-5" />
            新增模型
          </Button>
        </AccessControl>
      </template>
    </Grid>
    <FormModal @success="onSuccess" />
    <DesignerModal @success="onSuccess" />
  </Page>
</template>
