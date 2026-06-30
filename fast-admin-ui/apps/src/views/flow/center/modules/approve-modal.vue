<script lang="ts" setup>
import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import {
  Button,
  message,
  RadioButton,
  RadioGroup,
  Select,
  Textarea,
} from 'ant-design-vue';

import { getUserOptions } from '#/api/flow/org';
import { completeTask, getTaskDetail, transferTask } from '#/api/flow/task';

import FormRender from '../../components/form-render.vue';

const emit = defineEmits(['success']);

const taskId = ref<string>('');
const taskName = ref<string>('');
const formSchema = ref<any[]>([]);
const formValues = ref<Record<string, any>>({});
const hasForm = computed(() => formSchema.value.length > 0);

const outcome = ref<'approve' | 'reject'>('approve');
const comment = ref<string>('');
const ccUserIds = ref<string[]>([]);
const transferTo = ref<string | undefined>();
const userOptions = ref<{ label: string; value: string }[]>([]);

async function loadUsers() {
  if (userOptions.value.length > 0) return;
  userOptions.value = await getUserOptions();
}

const [Modal, modalApi] = useVbenModal({
  class: 'w-[640px]',
  async onConfirm() {
    if (!comment.value && outcome.value === 'reject') {
      message.warning('驳回时请填写审批意见');
      return;
    }
    modalApi.lock();
    try {
      await completeTask(taskId.value, {
        outcome: outcome.value,
        comment: comment.value,
        variables: formValues.value,
        ccUserIds: ccUserIds.value,
      });
      message.success('审批完成');
      modalApi.close();
      emit('success');
    } finally {
      modalApi.lock(false);
    }
  },
  async onOpenChange(isOpen) {
    if (!isOpen) return;
    const data = modalApi.getData<{ taskId: string; taskName: string }>();
    taskId.value = data?.taskId ?? '';
    taskName.value = data?.taskName ?? '';
    outcome.value = 'approve';
    comment.value = '';
    ccUserIds.value = [];
    transferTo.value = undefined;
    formSchema.value = [];
    formValues.value = {};
    loadUsers();
    const detail: any = await getTaskDetail(taskId.value);
    formValues.value = detail?.variables ?? {};
    if (detail?.form) {
      try {
        formSchema.value = JSON.parse(detail.form);
      } catch {
        formSchema.value = [];
      }
    }
  },
});

async function doTransfer() {
  if (!transferTo.value) {
    message.warning('请选择转办对象');
    return;
  }
  await transferTask(taskId.value, transferTo.value, comment.value);
  message.success('已转办');
  modalApi.close();
  emit('success');
}
</script>

<template>
  <Modal :title="`审批 - ${taskName}`">
    <div class="space-y-4 p-1">
      <div v-if="hasForm" class="rounded border p-3">
        <div class="mb-2 text-sm font-medium text-gray-600">表单内容</div>
        <FormRender :schema="formSchema" :values="formValues" disabled />
      </div>

      <div>
        <div class="mb-1 text-sm">审批结果</div>
        <RadioGroup v-model:value="outcome" button-style="solid">
          <RadioButton value="approve">同意</RadioButton>
          <RadioButton value="reject">驳回</RadioButton>
        </RadioGroup>
      </div>

      <div>
        <div class="mb-1 text-sm">审批意见</div>
        <Textarea v-model:value="comment" :rows="3" placeholder="请输入审批意见" />
      </div>

      <div>
        <div class="mb-1 text-sm">抄送（可选）</div>
        <Select
          v-model:value="ccUserIds"
          mode="multiple"
          allow-clear
          class="w-full"
          placeholder="选择抄送人"
          :options="userOptions"
          :filter-option="(i, o) => o.label.includes(i)"
        />
      </div>

      <div class="flex items-end gap-2 border-t pt-3">
        <div class="flex-1">
          <div class="mb-1 text-sm">转办（可选）</div>
          <Select
            v-model:value="transferTo"
            allow-clear
            show-search
            class="w-full"
            placeholder="选择转办对象"
            :options="userOptions"
            :filter-option="(i, o) => o.label.includes(i)"
          />
        </div>
        <Button @click="doTransfer">转办</Button>
      </div>
    </div>
  </Modal>
</template>
