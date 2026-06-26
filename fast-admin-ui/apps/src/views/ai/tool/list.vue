<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { AiToolApi } from '#/api/ai/tool';

import { AccessControl } from '@vben/access';
import { Page, useVbenModal } from '@vben/common-ui';
import { Plus } from '@vben/icons';

import { Button, message, Modal as AModal } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  changeAiToolEnabled,
  deleteAiTool,
  getAiToolPage,
} from '#/api/ai/tool';

import { useColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

function onActionClick({ code, row }: OnActionClickParams<AiToolApi.ToolConfig>) {
  if (code === 'edit') {
    formModalApi.setData(row).open();
  } else if (code === 'delete') {
    AModal.confirm({
      content: `确认删除 AI 工具「${row.name}」？`,
      title: '删除确认',
      onOk: () =>
        deleteAiTool(row.id).then(() => {
          message.success('删除成功');
          refreshGrid();
        }),
    });
  }
}

async function onEnabledChange(
  newVal: boolean,
  row: AiToolApi.ToolConfig,
): Promise<boolean> {
  if (row.systemBuiltin) {
    message.info('内置工具的启用状态由系统参数控制，无法在此切换');
    return false;
  }
  try {
    await changeAiToolEnabled(row.id, newVal);
    message.success(newVal ? '已启用' : '已禁用');
    return true;
  } catch {
    return false;
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
    submitOnEnter: true,
    collapsed: true,
  },
  gridOptions: {
    columns: useColumns(onActionClick, onEnabledChange),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) =>
          getAiToolPage({
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
  } as VxeTableGridOptions<AiToolApi.ToolConfig>,
});

function refreshGrid() {
  gridApi.query();
}
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="refreshGrid" />
    <Grid table-title="AI 工具管理">
      <template #toolbar-tools>
        <AccessControl type="code" :codes="['ai:tool:add']">
          <Button type="primary" @click="formModalApi.setData(null).open()">
            <Plus class="size-5" />
            新增工具
          </Button>
        </AccessControl>
      </template>
    </Grid>
  </Page>
</template>
