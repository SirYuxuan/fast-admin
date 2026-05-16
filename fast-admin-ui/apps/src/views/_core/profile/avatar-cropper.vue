<script setup lang="ts">
import { nextTick, ref, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { Button, Modal as AModal, Slider, Spin } from 'ant-design-vue';

import 'vue-cropper/dist/index.css';
import { VueCropper } from 'vue-cropper';

const props = defineProps<{
  open: boolean;
  /** 待裁剪的图片源（File 或 dataURL 字符串） */
  src: File | string | null;
  /** 上传中状态，由父组件传入 */
  uploading?: boolean;
}>();

const emits = defineEmits<{
  (e: 'update:open', val: boolean): void;
  (e: 'confirm', blob: Blob): void;
}>();

const cropperRef = ref();
const imgSrc = ref<string>('');
const previewSrc = ref<string>('');
const scale = ref(1);

// 当 src 变化时读取图片
watch(
  () => props.src,
  (val) => {
    if (!val) {
      imgSrc.value = '';
      previewSrc.value = '';
      return;
    }
    if (typeof val === 'string') {
      imgSrc.value = val;
      return;
    }
    // 转 File 为 DataURL
    const reader = new FileReader();
    reader.onload = (e) => {
      imgSrc.value = (e.target?.result as string) || '';
    };
    reader.readAsDataURL(val);
  },
  { immediate: true },
);

// 实时预览
function onCropMoving() {
  cropperRef.value?.getCropData?.((data: string) => {
    previewSrc.value = data;
  });
}

// 旋转
function rotateLeft() {
  cropperRef.value?.rotateLeft?.();
}

function rotateRight() {
  cropperRef.value?.rotateRight?.();
}

// 缩放
function onScaleChange(val: number | [number, number]) {
  const v = Array.isArray(val) ? val[0] : val;
  const delta = v - scale.value;
  scale.value = v;
  cropperRef.value?.changeScale?.(delta * 10);
}

// 取消
function onCancel() {
  emits('update:open', false);
}

// 确认裁剪
function onConfirm() {
  cropperRef.value?.getCropBlob?.((blob: Blob) => {
    if (!blob) return;
    emits('confirm', blob);
  });
}

// Modal 打开时重置预览
watch(
  () => props.open,
  async (val) => {
    if (val) {
      scale.value = 1;
      await nextTick();
      // 等 cropper 渲染后触发一次预览
      setTimeout(() => onCropMoving(), 300);
    }
  },
);
</script>

<template>
  <AModal
    :open="open"
    title="裁剪头像"
    :width="720"
    :mask-closable="false"
    :keyboard="false"
    :footer="null"
    @cancel="onCancel"
  >
    <Spin :spinning="!!props.uploading" tip="正在上传...">
      <div class="cropper-wrap">
        <!-- 左侧裁剪区 -->
        <div class="cropper-area">
          <VueCropper
            v-if="imgSrc"
            ref="cropperRef"
            :img="imgSrc"
            :auto-crop="true"
            :fixed="true"
            :fixed-number="[1, 1]"
            :center-box="true"
            :can-move-box="true"
            :info-true="true"
            :full="false"
            :auto-crop-width="200"
            :auto-crop-height="200"
            output-type="png"
            @real-time="onCropMoving"
            @img-load="onCropMoving"
          />
        </div>

        <!-- 右侧预览 -->
        <div class="preview-area">
          <div class="preview-title">预览效果</div>
          <div class="preview-list">
            <div class="preview-circle preview-lg">
              <img v-if="previewSrc" :src="previewSrc" alt="" />
            </div>
            <div class="preview-circle preview-md">
              <img v-if="previewSrc" :src="previewSrc" alt="" />
            </div>
            <div class="preview-circle preview-sm">
              <img v-if="previewSrc" :src="previewSrc" alt="" />
            </div>
          </div>
        </div>
      </div>

      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="zoom-row">
          <IconifyIcon icon="lucide:zoom-out" />
          <Slider
            :value="scale"
            :min="0.2"
            :max="3"
            :step="0.1"
            class="zoom-slider"
            @update:value="onScaleChange"
          />
          <IconifyIcon icon="lucide:zoom-in" />
        </div>
        <div class="tool-buttons">
          <Button @click="rotateLeft">
            <template #icon>
              <IconifyIcon icon="lucide:rotate-ccw" />
            </template>
            向左旋转
          </Button>
          <Button @click="rotateRight">
            <template #icon>
              <IconifyIcon icon="lucide:rotate-cw" />
            </template>
            向右旋转
          </Button>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div class="footer-row">
        <Button @click="onCancel">取消</Button>
        <Button type="primary" :loading="props.uploading" @click="onConfirm">
          确认上传
        </Button>
      </div>
    </Spin>
  </AModal>
</template>

<style scoped>
.cropper-wrap {
  display: flex;
  gap: 16px;
}

.cropper-area {
  flex: 1;
  height: 360px;
  background: #f5f5f5;
  border-radius: 4px;
  overflow: hidden;
}

.preview-area {
  width: 200px;
  flex-shrink: 0;
  padding-left: 16px;
  border-left: 1px solid #f0f0f0;
}

.preview-title {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 16px;
}

.preview-list {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.preview-circle {
  border-radius: 50%;
  overflow: hidden;
  background: #f5f5f5;
  border: 1px solid #f0f0f0;
}

.preview-circle img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-lg {
  width: 120px;
  height: 120px;
}

.preview-md {
  width: 64px;
  height: 64px;
}

.preview-sm {
  width: 36px;
  height: 36px;
}

.toolbar {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 16px;
  background: #fafafa;
  border-radius: 4px;
}

.zoom-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  color: #595959;
}

.zoom-slider {
  flex: 1;
  margin: 0 8px;
}

.tool-buttons {
  display: flex;
  gap: 8px;
}

.footer-row {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
