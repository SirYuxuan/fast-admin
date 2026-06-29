<script lang="ts" setup>
import type { AiModelApi } from '#/api/ai/model';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { RotateCw } from '@vben/icons';

import { Button, message, Select } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createAiModel,
  fetchAiModelList,
  testAiModel,
  updateAiModel,
} from '#/api/ai/model';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<AiModelApi.ModelConfig>();
const fetching = ref(false);
const testing = ref(false);
const getTitle = computed(() =>
  formData.value?.id ? '编辑模型配置' : '新增模型配置',
);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    labelWidth: 120,
  },
  layout: 'horizontal',
  schema: useFormSchema(),
  showDefaultActions: false,
});

function setModelOptions(models: string[]) {
  formApi.updateSchema([
    {
      componentProps: {
        allowClear: true,
        class: 'w-full',
        options: models.map((value) => ({ label: value, value })),
        showSearch: true,
      },
      fieldName: 'model',
    },
  ]);
}

/** 携带当前配置 id，便于后端在 API Key 脱敏时回退到已存储的密钥。 */
async function currentPayload() {
  const data = await formApi.getValues();
  return formData.value?.id ? { ...data, id: formData.value.id } : data;
}

async function onFetchModels() {
  fetching.value = true;
  try {
    const models = await fetchAiModelList(await currentPayload());
    setModelOptions(models);
    message.success(`已获取 ${models.length} 个模型`);
  } finally {
    fetching.value = false;
  }
}

async function onTest() {
  const { valid } = await formApi.validate();
  if (!valid) return;
  testing.value = true;
  try {
    const result = await testAiModel(await currentPayload());
    message.success(`连接成功，延时 ${result.latencyMs} ms`);
  } finally {
    testing.value = false;
  }
}

const [Modal, modalApi] = useVbenModal({
  class: 'w-[640px]',
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    modalApi.lock();
    try {
      const data = await formApi.getValues();
      if (formData.value?.id) {
        await updateAiModel({ ...data, id: formData.value.id });
      } else {
        await createAiModel(data);
      }
      modalApi.close();
      emit('success');
    } finally {
      modalApi.lock(false);
    }
  },
  onOpenChange(isOpen) {
    if (isOpen) {
      const data = modalApi.getData<AiModelApi.ModelConfig>();
      formData.value = data ?? undefined;
      formApi.resetForm();
      // 编辑时把已保存的模型名作为选项回填，避免选择框显示为空。
      setModelOptions(data?.model ? [data.model] : []);
      if (data) formApi.setValues(data);
    }
  },
});
</script>

<template>
  <Modal :title="getTitle">
    <Form class="mx-4">
      <template #model="slotProps">
        <div class="flex w-full items-center gap-2">
          <Select v-bind="slotProps" class="min-w-0 flex-1" />
          <Button
            aria-label="获取模型列表"
            html-type="button"
            :loading="fetching"
            title="获取模型列表"
            @click="onFetchModels"
          >
            <template #icon>
              <RotateCw class="size-4" />
            </template>
          </Button>
        </div>
      </template>
    </Form>
    <template #prepend-footer>
      <div class="flex-auto">
        <Button :loading="testing" @click="onTest">测试连接</Button>
      </div>
    </template>
  </Modal>
</template>
