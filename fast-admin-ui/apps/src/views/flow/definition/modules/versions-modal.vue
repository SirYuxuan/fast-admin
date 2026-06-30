<script lang="ts" setup>
import type { FlowDefinitionApi } from '#/api/flow/definition';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Button, message, Modal as AModal, Table, Tag } from 'ant-design-vue';

import {
  activateDefinition,
  deleteDeployment,
  getDefinitionVersions,
  suspendDefinition,
} from '#/api/flow/definition';

const emit = defineEmits(['changed']);

const list = ref<FlowDefinitionApi.Definition[]>([]);
const flowKey = ref('');
const flowName = ref('');
const loading = ref(false);

const columns = [
  { title: '版本', dataIndex: 'version', width: 80 },
  { title: '状态', dataIndex: 'suspended', width: 100 },
  { title: '部署ID', dataIndex: 'deploymentId', ellipsis: true },
  { title: '操作', dataIndex: 'op', width: 170 },
];

async function load() {
  loading.value = true;
  try {
    list.value = (await getDefinitionVersions(flowKey.value)) ?? [];
  } finally {
    loading.value = false;
  }
}

function toggle(row: FlowDefinitionApi.Definition) {
  const fn = row.suspended ? activateDefinition : suspendDefinition;
  fn(row.id).then(() => {
    message.success(row.suspended ? '已激活' : '已挂起');
    load();
    emit('changed');
  });
}

function remove(row: FlowDefinitionApi.Definition) {
  AModal.confirm({
    title: '删除确认',
    content: `删除 ${flowName.value} v${row.version} 的部署？该版本运行中的实例将一并删除。`,
    onOk: () =>
      deleteDeployment(row.deploymentId, true).then(() => {
        message.success('删除成功');
        load();
        emit('changed');
      }),
  });
}

const [Modal, modalApi] = useVbenModal({
  class: 'w-[760px]',
  showConfirmButton: false,
  cancelText: '关闭',
  onOpenChange(isOpen) {
    if (!isOpen) return;
    const data = modalApi.getData<{ key: string; name: string }>();
    flowKey.value = data?.key ?? '';
    flowName.value = data?.name ?? '';
    load();
  },
});
</script>

<template>
  <Modal :title="`历史版本 - ${flowName}`">
    <Table
      :columns="columns"
      :data-source="list"
      :loading="loading"
      :pagination="false"
      row-key="id"
      size="small"
    >
      <template #bodyCell="{ column, record, index }">
        <template v-if="column.dataIndex === 'version'">
          <span>v{{ record.version }}</span>
          <Tag v-if="index === 0" class="ml-1" color="blue">最新</Tag>
        </template>
        <template v-else-if="column.dataIndex === 'suspended'">
          <Tag :color="record.suspended ? 'red' : 'green'">
            {{ record.suspended ? '挂起' : '激活' }}
          </Tag>
        </template>
        <template v-else-if="column.dataIndex === 'op'">
          <Button type="link" size="small" @click="toggle(record)">
            {{ record.suspended ? '激活' : '挂起' }}
          </Button>
          <Button type="link" size="small" danger @click="remove(record)">
            删除
          </Button>
        </template>
      </template>
    </Table>
  </Modal>
</template>
