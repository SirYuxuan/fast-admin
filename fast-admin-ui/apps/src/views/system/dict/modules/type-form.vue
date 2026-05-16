<script lang="ts" setup>
import type { SysDictApi } from '#/api/system/dict';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { useVbenForm } from '#/adapter/form';
import { createDictType, updateDictType } from '#/api/system/dict';

import { useTypeFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<SysDictApi.DictType>();
const getTitle = computed(() =>
  formData.value?.id ? '编辑字典类型' : '新增字典类型',
);

const [Form, formApi] = useVbenForm({
  commonConfig: { componentProps: { class: 'w-full' }, labelWidth: 100 },
  layout: 'horizontal',
  schema: useTypeFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  class: 'w-[520px]',
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    modalApi.lock();
    try {
      const data = await formApi.getValues();
      if (formData.value?.id) {
        await updateDictType({ ...data, id: formData.value.id });
      } else {
        await createDictType(data);
      }
      modalApi.close();
      emit('success');
    } finally {
      modalApi.lock(false);
    }
  },
  onOpenChange(isOpen) {
    if (isOpen) {
      const data = modalApi.getData<SysDictApi.DictType>();
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
