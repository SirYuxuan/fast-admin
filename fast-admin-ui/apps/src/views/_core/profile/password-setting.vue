<script setup lang="ts">
import { reactive, ref } from 'vue';

import {
  Button,
  Form,
  FormItem,
  InputPassword,
  message,
} from 'ant-design-vue';

import { changeUserPassword } from '#/api/system/user';

const formRef = ref();
const submitting = ref(false);

const formState = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const rules = {
  oldPassword: [{ required: true, message: '请输入旧密码' }],
  newPassword: [
    { required: true, message: '请输入新密码' },
    { min: 6, max: 32, message: '新密码长度为 6-32 个字符' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码' },
    {
      validator: async (_rule: any, value: string) => {
        if (value !== formState.newPassword) {
          throw new Error('两次输入的密码不一致');
        }
      },
    },
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
    await changeUserPassword({
      oldPassword: formState.oldPassword,
      newPassword: formState.newPassword,
    });
    message.success('密码修改成功，建议重新登录');
    formState.oldPassword = '';
    formState.newPassword = '';
    formState.confirmPassword = '';
    formRef.value?.resetFields();
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <div class="password-tip">
    <div class="tip-title">
      <span class="dot dot-blue"></span>
      密码安全建议
    </div>
    <ul class="tip-list">
      <li>密码长度至少 6 位，建议 8 位以上</li>
      <li>建议包含大小写字母、数字、特殊字符的组合</li>
      <li>不要使用容易被猜测的密码，如生日、电话号码等</li>
      <li>定期修改密码可以提升账号安全性</li>
    </ul>
  </div>

  <Form
    ref="formRef"
    :model="formState"
    :rules="rules"
    layout="vertical"
    class="password-form"
  >
    <FormItem label="旧密码" name="oldPassword">
      <InputPassword
        v-model:value="formState.oldPassword"
        placeholder="请输入旧密码"
      />
    </FormItem>

    <FormItem label="新密码" name="newPassword">
      <InputPassword
        v-model:value="formState.newPassword"
        placeholder="请输入新密码"
      />
    </FormItem>

    <FormItem label="确认新密码" name="confirmPassword">
      <InputPassword
        v-model:value="formState.confirmPassword"
        placeholder="请再次输入新密码"
      />
    </FormItem>

    <FormItem>
      <Button type="primary" :loading="submitting" @click="onSubmit">
        确认修改
      </Button>
    </FormItem>
  </Form>
</template>

<style scoped>
.password-form {
  max-width: 480px;
}

.password-tip {
  padding: 12px 16px;
  margin-bottom: 20px;
  background: rgba(24, 144, 255, 0.06);
  border-left: 3px solid #1890ff;
  border-radius: 4px;
}

.tip-title {
  font-size: 13px;
  font-weight: 600;
  color: #1890ff;
  display: flex;
  align-items: center;
}

.dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 6px;
}

.dot-blue {
  background: #1890ff;
}

.tip-list {
  margin: 8px 0 0 0;
  padding-left: 20px;
  font-size: 12px;
  color: #595959;
}

.tip-list li {
  line-height: 1.8;
}
</style>
