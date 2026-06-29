<script lang="ts" setup>
import type { AiRagApi } from '#/api/ai/rag';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { useVbenForm } from '#/adapter/form';
import { createAiKnowledgeBase, updateAiKnowledgeBase } from '#/api/ai/rag';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<AiRagApi.KnowledgeBase>();
const getTitle = computed(() =>
  formData.value?.id ? '编辑知识库' : '新增知识库',
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
  class: 'w-[calc(100vw-32px)]! sm:w-[680px]!',
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    modalApi.lock();
    try {
      const data = await formApi.getValues();
      if (formData.value?.id) {
        await updateAiKnowledgeBase({ ...data, id: formData.value.id });
      } else {
        await createAiKnowledgeBase(data);
      }
      modalApi.close();
      emit('success');
    } finally {
      modalApi.lock(false);
    }
  },
  onOpenChange(isOpen) {
    if (isOpen) {
      const data = modalApi.getData<AiRagApi.KnowledgeBase>();
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
