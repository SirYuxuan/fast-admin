<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { AiRagApi } from '#/api/ai/rag';

import { useRouter } from 'vue-router';

import { AccessControl } from '@vben/access';
import { Page, useVbenModal } from '@vben/common-ui';
import { Plus } from '@vben/icons';

import { Button, message, Modal as AModal } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  changeAiKnowledgeBaseEnabled,
  deleteAiKnowledgeBase,
  getAiKnowledgeBasePage,
} from '#/api/ai/rag';

import { useColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const router = useRouter();

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

function onActionClick({
  code,
  row,
}: OnActionClickParams<AiRagApi.KnowledgeBase>) {
  if (code === 'edit') {
    formModalApi.setData(row).open();
  } else if (code === 'delete') {
    onDelete(row);
  } else if (code === 'detail') {
    router.push(`/ai/rag/${row.id}`);
  }
}

function onDelete(row: AiRagApi.KnowledgeBase) {
  AModal.confirm({
    content: `确认删除知识库「${row.name}」？文档索引也会同步删除。`,
    title: '删除确认',
    onOk: () =>
      deleteAiKnowledgeBase(row.id).then(() => {
        message.success('删除成功');
        refreshGrid();
      }),
  });
}

async function onEnabledChange(
  newVal: boolean,
  row: AiRagApi.KnowledgeBase,
): Promise<boolean> {
  try {
    await changeAiKnowledgeBaseEnabled(row.id, newVal);
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
          getAiKnowledgeBasePage({
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
  } as VxeTableGridOptions<AiRagApi.KnowledgeBase>,
});

function refreshGrid() {
  gridApi.query();
}
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="refreshGrid" />
    <Grid table-title="AI 知识库">
      <template #toolbar-tools>
        <AccessControl type="code" :codes="['ai:rag:add']">
          <Button type="primary" @click="formModalApi.setData(null).open()">
            <Plus class="size-5" />
            新增知识库
          </Button>
        </AccessControl>
      </template>
    </Grid>
  </Page>
</template>
