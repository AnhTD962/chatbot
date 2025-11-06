<template>
  <div>
    <!-- Add Knowledge Button -->
    <div class="mb-6 flex justify-end">
      <button
          @click="showAddModal = true"
          class="px-4 py-2 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 transition flex items-center space-x-2"
      >
        <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        <span>Thêm kiến thức</span>
      </button>
    </div>

    <!-- Knowledge List -->
    <div class="space-y-4">
      <div v-if="loading" class="bg-white rounded-xl shadow-sm p-12 text-center">
        <LoadingSpinner text="Đang tải..." />
      </div>

      <div v-else-if="knowledgeList.length === 0" class="bg-white rounded-xl shadow-sm p-12 text-center">
        <svg class="h-16 w-16 text-gray-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        <h3 class="text-lg font-semibold text-gray-900 mb-2">Chưa có kiến thức</h3>
        <p class="text-gray-600">Thêm kiến thức mới để chatbot trả lời chính xác hơn</p>
      </div>

      <div
          v-else
          v-for="item in knowledgeList"
          :key="item.id"
          class="bg-white rounded-xl shadow-sm p-6 hover:shadow-md transition"
      >
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <div class="flex items-center space-x-2 mb-2">
              <span class="px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-xs font-semibold">
                {{ item.category }}
              </span>
              <span v-if="item.isActive" class="px-3 py-1 bg-green-100 text-green-700 rounded-full text-xs font-semibold">
                Đang hoạt động
              </span>
            </div>

            <h3 class="text-lg font-bold text-gray-900 mb-2">{{ item.question }}</h3>
            <p class="text-gray-600 mb-4">{{ item.answer }}</p>

            <div class="flex items-center space-x-4 text-sm text-gray-500">
              <span class="flex items-center">
                <svg class="h-4 w-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
                {{ formatDate(item.createdAt) }}
              </span>
              <span class="flex items-center">
                <svg class="h-4 w-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
                {{ item.usageCount || 0 }} lần sử dụng
              </span>
            </div>
          </div>

          <div class="flex items-center space-x-2">
            <button
                @click="editKnowledge(item)"
                class="px-3 py-1 bg-blue-100 text-blue-700 rounded text-sm font-semibold hover:bg-blue-200 transition"
            >
              Sửa
            </button>
            <button
                @click="deleteKnowledge(item.id)"
                class="px-3 py-1 bg-red-100 text-red-700 rounded text-sm font-semibold hover:bg-red-200 transition"
            >
              Xóa
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Add/Edit Modal -->
    <div v-if="showAddModal || editingItem" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl shadow-2xl max-w-2xl w-full p-6">
        <h3 class="text-xl font-bold text-gray-900 mb-4">
          {{ editingItem ? 'Sửa kiến thức' : 'Thêm kiến thức mới' }}
        </h3>

        <form @submit.prevent="saveKnowledge" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Danh mục</label>
            <select
                v-model="knowledgeForm.category"
                required
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">Chọn danh mục</option>
              <option value="company">Công ty</option>
              <option value="products">Sản phẩm</option>
              <option value="services">Dịch vụ</option>
              <option value="contact">Liên hệ</option>
              <option value="other">Khác</option>
            </select>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Câu hỏi</label>
            <input
                v-model="knowledgeForm.question"
                type="text"
                required
                placeholder="Ví dụ: Hoaphat là công ty gì?"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Câu trả lời</label>
            <textarea
                v-model="knowledgeForm.answer"
                required
                rows="5"
                placeholder="Nhập câu trả lời chi tiết..."
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            ></textarea>
          </div>

          <div class="flex items-center space-x-2">
            <input
                v-model="knowledgeForm.isActive"
                type="checkbox"
                id="isActive"
                class="h-4 w-4 text-blue-600 rounded"
            />
            <label for="isActive" class="text-sm text-gray-700">Kích hoạt ngay</label>
          </div>

          <div class="flex items-center space-x-3 pt-4">
            <button
                type="submit"
                :disabled="saving"
                class="flex-1 py-2 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 disabled:bg-gray-400 transition"
            >
              {{ saving ? 'Đang lưu...' : 'Lưu' }}
            </button>
            <button
                type="button"
                @click="closeModal"
                class="flex-1 py-2 bg-gray-200 text-gray-700 rounded-lg font-semibold hover:bg-gray-300 transition"
            >
              Hủy
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import adminService from '@/services/adminService'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import { useToast } from '@/composables/useToast'

const toast = useToast()

const knowledgeList = ref([])
const loading = ref(false)
const showAddModal = ref(false)
const editingItem = ref(null)
const saving = ref(false)

const knowledgeForm = ref({
  category: '',
  question: '',
  answer: '',
  isActive: true
})

onMounted(() => {
  loadKnowledge()
})

const loadKnowledge = async () => {
  try {
    loading.value = true
    const response = await adminService.getKnowledge()
    knowledgeList.value = response
  } catch (error) {
    toast.error('Lỗi', 'Không thể tải danh sách kiến thức')
  } finally {
    loading.value = false
  }
}

const editKnowledge = (item) => {
  editingItem.value = item
  knowledgeForm.value = { ...item }
}

const deleteKnowledge = async (id) => {
  if (!confirm('Bạn có chắc chắn muốn xóa kiến thức này?')) return

  try {
    await adminService.deleteKnowledge(id)
    toast.success('Thành công', 'Đã xóa kiến thức')
    loadKnowledge()
  } catch (error) {
    toast.error('Lỗi', 'Không thể xóa kiến thức')
  }
}

const saveKnowledge = async () => {
  try {
    saving.value = true

    if (editingItem.value) {
      await adminService.updateKnowledge(editingItem.value.id, knowledgeForm.value)
      toast.success('Thành công', 'Cập nhật kiến thức thành công')
    } else {
      await adminService.createKnowledge(knowledgeForm.value)
      toast.success('Thành công', 'Thêm kiến thức mới thành công')
    }

    closeModal()
    loadKnowledge()
  } catch (error) {
    toast.error('Lỗi', 'Không thể lưu kiến thức')
  } finally {
    saving.value = false
  }
}

const closeModal = () => {
  showAddModal.value = false
  editingItem.value = null
  knowledgeForm.value = {
    category: '',
    question: '',
    answer: '',
    isActive: true
  }
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString('vi-VN')
}
</script>