<template>
  <div class="flex items-center justify-center" :class="containerClass">
    <div
        class="spinner"
        :style="{
        width: size + 'px',
        height: size + 'px',
        borderWidth: borderWidth + 'px',
        borderTopColor: color
      }"
    ></div>
    <p v-if="text" class="ml-3 text-gray-600">{{ text }}</p>
  </div>
</template>

<script setup>
const props = defineProps({
  size: {
    type: Number,
    default: 40
  },
  borderWidth: {
    type: Number,
    default: 3
  },
  color: {
    type: String,
    default: '#3b82f6'
  },
  text: {
    type: String,
    default: ''
  },
  containerClass: {
    type: String,
    default: ''
  }
})
</script>