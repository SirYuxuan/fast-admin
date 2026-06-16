<script lang="ts" setup>
import type { AiToolApi } from '#/api/ai/tool';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { useVbenForm } from '#/adapter/form';
import { createAiTool, updateAiTool } from '#/api/ai/tool';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<AiToolApi.ToolConfig>();
const getTitle = computed(() =>
  formData.value?.id ? '编辑 AI 工具' : '新增 AI 工具',
);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    labelWidth: 110,
  },
  layout: 'horizontal',
  schema: useFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  class: 'w-[760px]',
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    modalApi.lock();
    try {
      const data = await formApi.getValues();
      if (formData.value?.id) {
        await updateAiTool({ ...data, id: formData.value.id });
      } else {
        await createAiTool(data);
      }
      modalApi.close();
      emit('success');
    } finally {
      modalApi.lock(false);
    }
  },
  onOpenChange(isOpen) {
    if (isOpen) {
      const data = modalApi.getData<AiToolApi.ToolConfig>();
      formData.value = data ?? undefined;
      formApi.resetForm();
      if (data) formApi.setValues(data);
    }
  },
});
</script>

<template>
  <Modal :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>
