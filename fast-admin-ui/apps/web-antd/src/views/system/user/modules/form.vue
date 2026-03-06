<script lang="ts" setup>
import type { ModelType } from '../data-types';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Button } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';

import { crud, useFormSchema } from '../data';

const emit = defineEmits(['success']);

const formData = ref<ModelType>();

const [Form, formApi] = useVbenForm({
  schema: useFormSchema(),
  showDefaultActions: false,
});

// 通用Crud函数引用

function resetForm() {
  formApi.resetForm();
  formApi.setValues(formData.value || {});
}
const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (valid) {
      modalApi.lock();
      const data = await formApi.getValues();
      try {
        await (formData.value?.id
          ? crud.edit({ ...data, id: formData.value.id })
          : crud.add(data));
        modalApi.close();
        emit('success');
      } finally {
        modalApi.lock(false);
      }
    }
  },
  onOpenChange(isOpen) {
    if (isOpen) {
      const data = modalApi.getData<ModelType>();
      if (data) {
        formData.value = data;
        formApi.setValues(formData.value);
      }
    }
  },
});

const getTitle = computed(() => {
  return formData.value?.id ? '编辑用户' : '新增用户';
});
</script>
<template>
  <Modal :title="getTitle">
    <Form class="mx-4" />
    <template #prepend-footer>
      <div class="flex-auto">
        <Button type="primary" danger @click="resetForm"> 重置 </Button>
      </div>
    </template>
  </Modal>
</template>
