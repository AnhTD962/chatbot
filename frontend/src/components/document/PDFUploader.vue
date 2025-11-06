<template>
  <div class="max-w-4xl mx-auto p-6">
    <!-- Upload Area -->
    <div
        @dragover.prevent="dragOver = true"
        @dragleave.prevent="dragOver = false"
        @drop.prevent="handleDrop"
        :class="[
        'border-2 border-dashed rounded-2xl p-12 transition-all',
        dragOver ? 'border-blue-500 bg-blue-50' : 'border-gray-300 bg-white'
      ]"
    >
      <div class="text-center">
        <svg class="mx-auto h-16 w-16 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
        </svg>

        <h3 class="text-lg font-semibold text-gray-900 mb-2">
          Tải lên tài liệu PDF
        </h3>

        <p class="text-gray-600 mb-6">
          Kéo thả file vào đây hoặc click để chọn file
        </p>

        <input
            ref="fileInput"
            type="file"
            accept=".pdf"
            @change="handleFileSelect"
            class="hidden"
        />

        <button
            @click="$refs.fileInput.click()"
            class="px-6 py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 transition"
        >
          Chọn file PDF
        </button>

        <p class="text-sm text-gray-500 mt-4">
          Hỗ trợ file PDF, tối đa 10MB
        </p>
      </div>
    </div>

    <!-- Upload Progress -->
    <div v-if="uploading" class="mt-6 bg-white rounded-xl shadow-lg p-6">
      <div class="flex items-center mb-4">
        <svg class="animate-spin h-5 w-5 text-blue-600 mr-3" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
        <span class="text-gray-700 font-medium">Đang tải lên {{ selectedFile?.name }}</span>
      </div>

      <div class="w-full bg-gray-200 rounded-full h-3 overflow-hidden">
        <div
            class="bg-blue-600 h-3 transition-all duration-300 rounded-full"
            :style="{ width: `${uploadProgress}%` }"
        ></div>
      </div>

      <p class="text-sm text-gray-600 mt-2 text-center">{{ uploadProgress }}%</p>
    </div>

    <!-- Success Message -->
    <div v-if="uploadSuccess" class="mt-6 bg-green-50 border border-green-200 rounded-xl p-6">
      <div class="flex items-center">
        <svg class="h-6 w-6 text-green-600 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <div>
          <p class="font-semibold text-green-900">Tải lên thành công!</p>
          <p class="text-sm text-green-700">{{ selectedFile?.name }} đã được xử lý</p>
        </div>
      </div>
    </div>

    <!-- Error Message -->
    <div v-if="error" class="mt-6 bg-red-50 border border-red-200 rounded-xl p-6">
      <div class="flex items-center">
        <svg class="h-6 w-6 text-red-600 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <div>
          <p class="font-semibold text-red-900">Lỗi</p>
          <p class="text-sm text-red-700">{{ error }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import documentService from '@/services/documentService'

const emit = defineEmits(['upload-success'])

const fileInput = ref(null)
const selectedFile = ref(null)
const dragOver = ref(false)
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadSuccess = ref(false)
const error = ref('')

const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (file) {
    validateAndUpload(file)
  }
}

const handleDrop = (event) => {
  dragOver.value = false
  const file = event.dataTransfer.files[0]
  if (file) {
    validateAndUpload(file)
  }
}

const validateAndUpload = async (file) => {
  error.value = ''
  uploadSuccess.value = false

  // Validate file type
  if (!file.type.includes('pdf')) {
    error.value = 'Chỉ hỗ trợ file PDF'
    return
  }

  // Validate file size (10MB)
  if (file.size > 10 * 1024 * 1024) {
    error.value = 'File không được vượt quá 10MB'
    return
  }

  selectedFile.value = file
  await uploadFile(file)
}

const uploadFile = async (file) => {
  try {
    uploading.value = true
    uploadProgress.value = 0

    const response = await documentService.uploadDocument(file, (progress) => {
      uploadProgress.value = progress
    })

    uploadSuccess.value = true
    emit('upload-success', response)

    setTimeout(() => {
      uploadSuccess.value = false
      selectedFile.value = null
    }, 3000)
  } catch (err) {
    error.value = err.response?.data?.message || 'Tải lên thất bại'
  } finally {
    uploading.value = false
  }
}
</script>