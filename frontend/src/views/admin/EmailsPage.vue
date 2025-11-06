<template>
  <div>
    <!-- Email Stats -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
      <div class="bg-white rounded-xl shadow-sm p-6">
        <p class="text-gray-600 text-sm mb-2">Tổng email</p>
        <p class="text-3xl font-bold text-gray-900">{{ emailStats.total }}</p>
      </div>
      <div class="bg-white rounded-xl shadow-sm p-6">
        <p class="text-gray-600 text-sm mb-2">Đã gửi</p>
        <p class="text-3xl font-bold text-green-600">{{ emailStats.sent }}</p>
      </div>
      <div class="bg-white rounded-xl shadow-sm p-6">
        <p class="text-gray-600 text-sm mb-2">Thất bại</p>
        <p class="text-3xl font-bold text-red-600">{{ emailStats.failed }}</p>
      </div>
      <div class="bg-white rounded-xl shadow-sm p-6">
        <p class="text-gray-600 text-sm mb-2">Tỷ lệ thành công</p>
        <p class="text-3xl font-bold text-blue-600">{{ emailStats.successRate }}%</p>
      </div>
    </div>

    <!-- Filters -->
    <div class="bg-white rounded-xl shadow-sm p-6 mb-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <select
            v-model="filters.type"
            class="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="">Tất cả loại</option>
          <option value="WELCOME">Welcome</option>
          <option value="PASSWORD_RESET">Reset Password</option>
          <option value="PAYMENT_SUCCESS">Payment Success</option>
          <option value="SUBSCRIPTION_EXPIRING">Expiring Warning</option>
        </select>
        <select
            v-model="filters.status"
            class="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="">Tất cả trạng thái</option>
          <option value="SENT">Đã gửi</option>
          <option value="FAILED">Thất bại</option>
        </select>
        <input
            v-model="filters.email"
            type="email"
            placeholder="Email người nhận..."
            class="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <button
            @click="loadEmailLogs"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
        >
          Lọc
        </button>
      </div>
    </div>

    <!-- Email Logs Table -->
    <div class="bg-white rounded-xl shadow-sm overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead class="bg-gray-50 border-b border-gray-200">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Email</th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Loại</th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Tiêu đề</th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Trạng thái</th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Ngày gửi</th>
          </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
          <tr v-if="loading">
            <td colspan="5" class="px-6 py-12 text-center">
              <LoadingSpinner text="Đang tải..." />
            </td>
          </tr>
          <tr v-else-if="emailLogs.length === 0">
            <td colspan="5" class="px-6 py-12 text-center text-gray-500">
              Không tìm thấy email
            </td>
          </tr>
          <tr v-else v-for="log in emailLogs" :key="log.id" class="hover:bg-gray-50">
            <td class="px-6 py-4">
              <p class="font-semibold text-gray-900 text-sm">{{ log.recipientEmail }}</p>
            </td>
            <td class="px-6 py-4">
                <span class="px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-xs font-semibold">
                  {{ log.emailType }}
                </span>
            </td>
            <td class="px-6 py-4">
              <p class="text-sm text-gray-900">{{ log.subject }}</p>
            </td>
            <td class="px-6 py-4">
                <span
                    :class="[
                    'inline-block px-3 py-1 rounded-full text-xs font-semibold',
                    log.status === 'SENT' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                  ]"
                >
                  {{ log.status === 'SENT' ? 'Đã gửi' : 'Thất bại' }}
                </span>
            </td>
            <td class="px-6 py-4 text-sm text-gray-600">
              {{ formatDate(log.sentAt) }}
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="px-6 py-4 border-t border-gray-200 flex items-center justify-between">
        <p class="text-sm text-gray-600">
          Tổng: {{ totalEmails }} emails
        </p>
        <div class="flex items-center space-x-2">
          <button
              @click="currentPage--"
              :disabled="currentPage === 1"
              class="px-3 py-1 border border-gray-300 rounded hover:bg-gray-50 disabled:opacity-50"
          >
            Trước
          </button>
          <span class="px-3 py-1 bg-blue-600 text-white rounded">{{ currentPage }}</span>
          <button
              @click="currentPage++"
              :disabled="currentPage * pageSize >= totalEmails"
              class="px-3 py-1 border border-gray-300 rounded hover:bg-gray-50 disabled:opacity-50"
          >
            Sau
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import adminService from '@/services/adminService'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import { useToast } from '@/composables/useToast'

const toast = useToast()

const emailStats = ref({
  total: 0,
  sent: 0,
  failed: 0,
  successRate: 0
})

const emailLogs = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalEmails = ref(0)

const filters = ref({
  type: '',
  status: '',
  email: ''
})

onMounted(() => {
  loadEmailStats()
  loadEmailLogs()
})

watch(currentPage, () => {
  loadEmailLogs()
})

const loadEmailStats = async () => {
  try {
    const response = await adminService.getEmailStats()
    emailStats.value = response
  } catch (error) {
    console.error('Failed to load email stats:', error)
  }
}

const loadEmailLogs = async () => {
  try {
    loading.value = true
    const response = await adminService.getEmailLogs({
      page: currentPage.value,
      size: pageSize.value,
      ...filters.value
    })
    emailLogs.value = response.logs
    totalEmails.value = response.total
  } catch (error) {
    toast.error('Lỗi', 'Không thể tải danh sách email')
  } finally {
    loading.value = false
  }
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>