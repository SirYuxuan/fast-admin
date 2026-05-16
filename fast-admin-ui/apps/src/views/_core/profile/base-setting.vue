<script setup lang="ts">
import { reactive, ref, watch } from 'vue';

import {
  Button,
  Form,
  FormItem,
  Input,
  message,
} from 'ant-design-vue';

import { updateUserProfile } from '#/api/system/user';

const props = defineProps<{
  detail?: Record<string, any>;
}>();

const emits = defineEmits<{
  (e: 'updated'): void;
}>();

const formRef = ref();
const submitting = ref(false);

const formState = reactive({
  nickname: '',
  email: '',
  phone: '',
});

// 将后端的 detail 同步进表单
watch(
  () => props.detail,
  (val) => {
    if (!val) return;
    formState.nickname = val.nickname ?? '';
    formState.email = val.email ?? '';
    formState.phone = val.phone ?? '';
  },
  { immediate: true, deep: true },
);

const rules = {
  nickname: [
    { required: true, message: '请输入昵称' },
    { min: 2, max: 20, message: '昵称长度为 2-20 个字符' },
  ],
  email: [
    { type: 'email' as const, message: '请输入有效的邮箱地址' },
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号' },
  ],
};

async function onSubmit() {
  try {
    await formRef.value.validate();
  } catch {
    return;
  }
  try {
    submitting.value = true;
    await updateUserProfile({
      nickname: formState.nickname,
      email: formState.email || undefined,
      phone: formState.phone || undefined,
    });
    message.success('个人信息更新成功');
    emits('updated');
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <Form
    ref="formRef"
    :model="formState"
    :rules="rules"
    layout="vertical"
    class="profile-form"
  >
    <FormItem label="用户名">
      <Input :value="props.detail?.username" disabled />
      <div class="form-hint">用户名为登录账号，不可修改</div>
    </FormItem>

    <FormItem label="昵称" name="nickname">
      <Input v-model:value="formState.nickname" placeholder="请输入昵称" />
    </FormItem>

    <FormItem label="邮箱" name="email">
      <Input v-model:value="formState.email" placeholder="请输入邮箱" />
    </FormItem>

    <FormItem label="手机号" name="phone">
      <Input v-model:value="formState.phone" placeholder="请输入手机号" />
    </FormItem>

    <FormItem>
      <Button type="primary" :loading="submitting" @click="onSubmit">
        保存修改
      </Button>
    </FormItem>
  </Form>
</template>

<style scoped>
.profile-form {
  max-width: 480px;
}

.form-hint {
  margin-top: 4px;
  font-size: 12px;
  color: #8c8c8c;
}
</style>
