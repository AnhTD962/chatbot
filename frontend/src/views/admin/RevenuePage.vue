<template>
  <div>
    <!-- Revenue Stats -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
      <div class="bg-white rounded-xl shadow-sm p-6">
        <p class="text-gray-600 text-sm mb-2">Doanh thu hôm nay</p>
        <p class="text-3xl font-bold text-gray-900">{{ formatCurrency(revenueStats.today) }}</p>
        <p class="text-sm text-green-600 mt-2">+{{ revenueStats.todayGrowth }}%</p>
      </div>
      <div class="bg-white rounded-xl shadow-sm p-6">
        <p class="text-gray-600 text-sm mb-2">Doanh thu tháng</p>
        <p class="text-3xl font-bold text-gray-900">{{ formatCurrency(revenueStats.month) }}</p>
        <p class="text-sm text-green-600 mt-2">+{{ revenueStats.monthGrowth }}%</p>
      </div>
      <div class="bg-white rounded-xl shadow-sm p-6">
        <p class="text-gray-600 text-sm mb-2">Doanh thu năm</p>
        <p class="text-3xl font-bold text-gray-900">{{ formatCurrency(revenueStats.year) }}</p>
        <p class="text-sm text-green-600 mt-2">+{{ revenueStats.yearGrowth }}%</p>
      </div>
      <div class="bg-white rounded-xl shadow-sm p-6">
        <p class="text-gray-600 text-sm mb-2">Tổng doanh thu</p>
        <p class="text-3xl font-bold text-gray-900">{{ formatCurrency(revenueStats.total) }}</p>
      </div>
    </div>

    <!-- Filters -->
    <div class="bg-white rounded-xl shadow-sm p-6 mb-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <input
            v-model="filters.startDate"
            type="date"
            class="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <input
            v-model="filters.endDate"
            type="date"
            class="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <select
            v-model="filters.status"
            class="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="">Tất cả trạng thái</option>
          <option value="SUCCESS">Thành công</option>
          <option value="PENDING">Đang xử lý</option>
          <option value="FAILED">Thất bại</option>
          <option value="REFUNDED">Đã hoàn tiền</option>
        </select>
        <button
            @click="loadTransactions"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
        >
          Lọc
        </button>
      </div>
    </div>

    <!-- Transactions Table -->
    <div class="bg-white rounded-xl shadow-sm overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead class="bg-gray-50 border-b border-gray-200">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Mã GD</th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Người dùng</th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Gói</th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Số tiền</th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Trạng thái</th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Ngày</th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Hành động</th>
          </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
          <tr v-if="loading">
            <td colspan="7" class="px-6 py-12 text-center">
              <LoadingSpinner text="Đang tải..." />
            </td>
          </tr>
          <tr v-else-if="transactions.length === 0">
            <td colspan="7" class="px-6 py-12 text-center text-gray-500">
              Không tìm thấy giao dịch
            </td>
          </tr>
          <tr v-else v-for="transaction in transactions" :key="transaction.id" class="hover:bg-gray-50">
            <td class="px-6 py-4">
              <p class="font-mono text-sm text-gray-900">{{ transaction.transactionId }}</p>
            </td>
            <td class="px-6 py-4">
              <div>
                <p class="font-semibold text-gray-900 text-sm">{{ transaction.userEmail }}</p>
                <p class="text-xs text-gray-500">{{ transaction.userId }}</p>
              </div>
            </td>
            <td class="px-6 py-4">
              <span class="font-semibold text-gray-900">{{ transaction.planName }}</span>
            </td>
            <td class="px-6 py-4">
              <p class="font-bold text-gray-900">{{ formatCurrency(transaction.amount) }}</p>
            </td>
            <td class="px-6 py-4">
                <span
                    :class="[
                    'inline-block px-3 py-1 rounded-full text-xs font-semibold',
                    transaction.status === 'SUCCESS' ? 'bg-green-100 text-green-700' :
                    transaction.status === 'PENDING' ? 'bg-yellow-100 text-yellow-700' :
                    transaction.status === 'FAILED' ? 'bg-red-100 text-red-700' :
                    'bg-gray-100 text-gray-700'
                  ]"
                >
                  {{ getStatusText(transaction.status) }}
                </span>
            </td>
            <td class="px-6 py-4 text-sm text-gray-600">
              {{ formatDate(transaction.createdAt) }}
            </td>
            <td class="px-6 py-4">
              <button
                  v-if="transaction.status === 'SUCCESS'"
                  @click="refundTransaction(transaction)"
                  class="px-3 py-1 bg-red-100 text-red-700 rounded text-xs font-semibold hover:bg-red-200 transition"
              >
                Hoàn tiền
              </button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="px-6 py-4 border-t border-gray-200 flex items-center justify-between">
        <p class="text-sm text-gray-600">
          Tổng: {{ totalTransactions }} giao dịch
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
              :disabled="currentPage * pageSize >= totalTransactions"
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

const revenueStats = ref({
  today: 0,
  todayGrowth: 0,
  month: 0,
  monthGrowth: 0,
  year: 0,
  yearGrowth: 0,
  total: 0
})

const transactions = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalTransactions = ref(0)

const filters = ref({
  startDate: '',
  endDate: '',
  status: ''
})

onMounted(() => {
  loadRevenueStats()
  loadTransactions()
})

watch(currentPage, () => {
  loadTransactions()
})

const loadRevenueStats = async () => {
  try {
    const response = await adminService.getRevenueDashboard()
    revenueStats.value = response
  } catch (error) {
    console.error('Failed to load revenue stats:', error)
  }
}

const loadTransactions = async () => {
  try {
    loading.value = true
    const response = await adminService.getTransactions({
      page: currentPage.value,
      size: pageSize.value,
      ...filters.value
    })
    transactions.value = response.transactions
    totalTransactions.value = response.total
  } catch (error) {
    toast.error('Lỗi', 'Không thể tải danh sách giao dịch')
  } finally {
    loading.value = false
  }
}

const refundTransaction = async (transaction) => {
  const reason = prompt('Nhập lý do hoàn tiền:')
  if (!reason) return

  if (!confirm(`Xác nhận hoàn tiền ${formatCurrency(transaction.amount)} cho ${transaction.userEmail}?`)) {
    return
  }

  try {
    await adminService.refundTransaction(transaction.id, reason)
    toast.success('Thành công', 'Đã hoàn tiền thành công')
    loadTransactions()
    loadRevenueStats()
  } catch (error) {
    toast.error('Lỗi', 'Không thể hoàn tiền')
  }
}

const getStatusText = (status) => {
  const statuses = {
    'SUCCESS': 'Thành công',
    'PENDING': 'Đang xử lý',
    'FAILED': 'Thất bại',
    'REFUNDED': 'Đã hoàn tiền'
  }
  return statuses[status] || status
}

const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0
  }).format(amount)
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