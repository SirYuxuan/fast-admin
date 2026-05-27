<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SystemFileApi } from '#/api/system/file';

import { AccessControl } from '@vben/access';
import { Page, useVbenModal } from '@vben/common-ui';
import { Plus } from '@vben/icons';

import { Button, message, Modal as AModal } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  activateFileConfig,
  deleteFileConfig,
  getFileConfigPage,
} from '#/api/system/file';

import { useColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

function onActionClick({ code, row }: OnActionClickParams<SystemFileApi.FileConfig>) {
  switch (code) {
    case 'activate': {
      onActivate(row);
      break;
    }
    case 'delete': {
      onDelete(row);
      break;
    }
    case 'edit': {
      onEdit(row);
      break;
    }
  }
}

function onCreate() {
  formModalApi.setData(null).open();
}

function onEdit(row: SystemFileApi.FileConfig) {
  formModalApi.setData(row).open();
}

function onDelete(row: SystemFileApi.FileConfig) {
  AModal.confirm({
    content: `确认删除配置「${row.name}」？`,
    title: '删除确认',
    onOk: () =>
      deleteFileConfig(row.id).then(() => {
        message.success('删除成功');
        refreshGrid();
      }),
  });
}

function onActivate(row: SystemFileApi.FileConfig) {
  if (row.isActive) {
    message.info('该配置已激活');
    return;
  }
  AModal.confirm({
    content: `确认激活配置「${row.name}」？激活后其它配置会自动取消激活。`,
    title: '激活确认',
    onOk: () =>
      activateFileConfig(row.id).then(() => {
        message.success('激活成功');
        refreshGrid();
      }),
  });
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
    submitOnChange: false,
    submitOnEnter: true,
    collapsed: true,
  },
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getFileConfigPage({
            page: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
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
  } as VxeTableGridOptions<SystemFileApi.FileConfig>,
});

function refreshGrid() {
  gridApi.query();
}
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="refreshGrid" />
    <Grid table-title="文件存储配置">
      <template #toolbar-tools>
        <AccessControl type="code" :codes="['system:file:config:add']">
          <Button type="primary" @click="onCreate">
            <Plus class="size-5" />
            新增配置
          </Button>
        </AccessControl>
      </template>
    </Grid>
  </Page>
</template>
