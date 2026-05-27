<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SysConfigApi } from '#/api/system/config';

import { AccessControl } from '@vben/access';
import { Page, useVbenModal } from '@vben/common-ui';
import { Plus } from '@vben/icons';

import { Button, message, Modal as AModal } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteConfig, getConfigPage } from '#/api/system/config';

import { useColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({ connectedComponent: Form });

function onActionClick({ code, row }: OnActionClickParams<SysConfigApi.Config>) {
  if (code === 'edit') {
    formModalApi.setData(row).open();
  } else if (code === 'delete') {
    AModal.confirm({
      title: '删除确认',
      content: `确认删除参数「${row.configKey}」？`,
      onOk: () =>
        deleteConfig(row.id).then(() => {
          message.success('删除成功');
          refresh();
        }),
    });
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
          getConfigPage({
            page: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          }),
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: { custom: true, export: false, refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<SysConfigApi.Config>,
});

function refresh() {
  gridApi.query();
}

function onSuccess() {
  message.success('保存成功');
  refresh();
}
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="系统参数">
      <template #toolbar-tools>
        <AccessControl type="code" :codes="['system:config:add']">
          <Button type="primary" @click="formModalApi.setData(null).open()">
            <Plus class="size-5" />
            新增参数
          </Button>
        </AccessControl>
      </template>
    </Grid>
    <FormModal @success="onSuccess" />
  </Page>
</template>
