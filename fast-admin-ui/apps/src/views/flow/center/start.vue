<script lang="ts" setup>
import { onMounted, ref, shallowRef } from 'vue';

import { Page } from '@vben/common-ui';

import { Button, Card, Empty, message } from 'ant-design-vue';

import { getStartableProcesses, getStartForm } from '#/api/flow/definition';
import { startProcess } from '#/api/flow/process';

import FormRender from '../components/form-render.vue';

const processes = ref<any[]>([]);
const selected = ref<any>();
const formSchema = ref<any[]>([]);
const loadingForm = ref(false);
const formRef = shallowRef<any>();

onMounted(async () => {
  processes.value = (await getStartableProcesses()) ?? [];
});

async function pick(p: any) {
  selected.value = p;
  formSchema.value = [];
  loadingForm.value = true;
  try {
    const res = await getStartForm(p.id);
    if (res?.form) {
      formSchema.value = JSON.parse(res.form);
    }
  } catch {
    formSchema.value = [];
  } finally {
    loadingForm.value = false;
  }
}

async function submit() {
  let variables: Record<string, any> = {};
  if (formSchema.value.length > 0 && formRef.value) {
    const values = await formRef.value.getValidatedValues();
    if (!values) return;
    variables = values;
  }
  await startProcess(selected.value.key, variables);
  message.success('已发起流程');
  selected.value = undefined;
  formSchema.value = [];
}
</script>

<template>
  <Page auto-content-height>
    <div class="flex h-full gap-4">
      <Card title="选择流程" class="w-[320px] shrink-0 overflow-auto">
        <Empty v-if="!processes.length" description="暂无可发起的流程" />
        <div v-else class="space-y-2">
          <div
            v-for="p in processes"
            :key="p.id"
            class="cursor-pointer rounded border p-3 transition hover:border-blue-400"
            :class="{ 'border-blue-500 bg-blue-50': selected?.id === p.id }"
            @click="pick(p)"
          >
            <div class="font-medium">{{ p.name }}</div>
            <div class="text-xs text-gray-500">
              {{ p.key }} · v{{ p.version }}
            </div>
          </div>
        </div>
      </Card>

      <Card :title="selected ? `发起：${selected.name}` : '流程表单'" class="flex-1">
        <Empty v-if="!selected" description="请先在左侧选择一个流程" />
        <template v-else>
          <FormRender
            v-if="formSchema.length"
            ref="formRef"
            :schema="formSchema"
          />
          <div v-else-if="!loadingForm" class="mb-4 text-sm text-gray-500">
            该流程未绑定发起表单，可直接发起。
          </div>
          <div class="mt-4">
            <Button type="primary" @click="submit">发起</Button>
          </div>
        </template>
      </Card>
    </div>
  </Page>
</template>
