<template>
  <div class="min-h-screen bg-gray-50 py-8">
    <div class="max-w-7xl mx-auto px-4">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">Quản lý tài liệu</h1>
        <p class="text-gray-600 mt-2">Tải lên và phân tích tài liệu PDF của bạn</p>
      </div>

      <!-- Upload Section -->
      <div class="mb-8">
        <PDFUploader @upload-success="handleUploadSuccess" />
      </div>

      <!-- Documents List -->
      <div class="bg-white rounded-xl shadow-sm p-6">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-xl font-bold text-gray-900">Tài liệu của tôi</h2>
          <button
              @click="loadDocuments"
              class="text-blue-600 hover:text-blue-700"
          >
            <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
          </button>
        </div>

        <!-- Loading -->
        <div v-if="loading" class="text-center py-12">
          <div class="spinner mx-auto mb-4"></div>
          <p class="text-gray-600">Đang tải...</p>
        </div>

        <!-- Empty State -->
        <div v-else-if="documents.length === 0" class="text-center py-12">
          <svg class="h-16 w-16 text-gray-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
          </svg>
          <h3 class="text-lg font-semibold text-gray-900 mb-2">Chưa có tài liệu</h3>
          <p class="text-gray-600">Tải lên tài liệu PDF đầu tiên của bạn</p>
        </div>

        <!-- Documents Grid -->
        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div
              v-for="doc in documents"
              :key="doc.id"
              class="border border-gray-200 rounded-lg p-4 hover:shadow-md transition"
          >
            <div class="flex items-start justify-between mb-3">
              <div class="flex items-center">
                <div class="h-10 w-10 bg-red-100 rounded-lg flex items-center justify-center mr-3">
                  <svg class="h-6 w-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
                  </svg>
                </div>
                <div class="flex-1">
                  <h3 class="font-semibold text-gray-900 text-sm truncate">{{ doc.fileName }}</h3>
                  <p class="text-xs text-gray-500">{{ formatFileSize(doc.fileSize) }}</p>
                </div>
              </div>

              <button
                  @click="handleDelete(doc.id)"
                  class="text-gray-400 hover:text-red-600"
              >
                <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                </svg>
              </button>
            </div>

            <div class="mb-3">
              <p class="text-xs text-gray-500 mb-1">{{ doc.totalPages }} trang</p>
              <p class="text-xs text-gray-500">Tải lên: {{ formatDate(doc.uploadedAt) }}</p>
            </div>

            <button
                @click="openQAModal(doc)"
                class="w-full py-2 bg-blue-600 text-white rounded-lg text-sm font-semibold hover:bg-blue-700 transition"
            >
              Hỏi đáp về tài liệu
            </button>
          </div>
        </div>
      </div>

      <!-- Q&A Modal -->
      <DocumentQA
          v-if="selectedDocument"
          :document="selectedDocument"
          @close="selectedDocument = null"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import PDFUploader from '@/components/document/PDFUploader.vue'
import DocumentQA from '@/components/document/DocumentQA.vue'
import documentService from '@/services/documentService'
import { useToast } from '@/composables/useToast'

const toast = useToast()

const documents = ref([])
const loading = ref(false)
const selectedDocument = ref(null)

onMounted(() => {
  loadDocuments()
})

const loadDocuments = async () => {
  try {
    loading.value = true
    const response = await documentService.getDocuments()
    documents.value = response
  } catch (error) {
    toast.error('Lỗi', 'Không thể tải danh sách tài liệu')
  } finally {
    loading.value = false
  }
}

const handleUploadSuccess = (document) => {
  toast.success('Thành công', 'Tải lên tài liệu thành công!')
  loadDocuments()
}

const handleDelete = async (documentId) => {
  if (!confirm('Bạn có chắc chắn muốn xóa tài liệu này?')) return

  try {
    await documentService.deleteDocument(documentId)
    toast.success('Thành công', 'Đã xóa tài liệu')
    loadDocuments()
  } catch (error) {
    toast.error('Lỗi', 'Không thể xóa tài liệu')
  }
}

const openQAModal = (document) => {
  selectedDocument.value = document
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}
</script>