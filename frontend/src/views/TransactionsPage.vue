<template>
  <div class="min-h-screen bg-gray-50 py-8">
    <div class="max-w-6xl mx-auto px-4">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">Lịch sử giao dịch</h1>
        <p class="text-gray-600 mt-2">Theo dõi các giao dịch thanh toán của bạn</p>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="text-center py-12">
        <div class="spinner mx-auto mb-4"></div>
        <p class="text-gray-600">Đang tải...</p>
      </div>

      <!-- Empty State -->
      <div v-else-if="transactions.length === 0" class="bg-white rounded-xl shadow-sm p-12 text-center">
        <svg class="h-16 w-16 text-gray-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
        </svg>
        <h3 class="text-lg font-semibold text-gray-900 mb-2">Chưa có giao dịch</h3>
        <p class="text-gray-600 mb-6">Nâng cấp lên Premium để trải nghiệm đầy đủ tính năng</p>
        <router-link
            to="/premium"
            class="inline-block px-6 py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 transition"
        >
          Xem gói Premium
        </router-link>
      </div>

      <!-- Transactions List -->
      <div v-else class="space-y-4">
        <div
            v-for="transaction in transactions"
            :key="transaction.id"
            class="bg-white rounded-xl shadow-sm p-6 hover:shadow-md transition"
        >
          <div class="flex items-start justify-between">
            <div class="flex items-start space-x-4">
              <!-- Icon -->
              <div
                  :class="[
                  'h-12 w-12 rounded-full flex items-center justify-center',
                  transaction.status === 'SUCCESS' ? 'bg-green-100' :
                  transaction.status === 'PENDING' ? 'bg-yellow-100' :
                  'bg-red-100'
                ]"
              >
                <svg
                    v-if="transaction.status === 'SUCCESS'"
                    class="h-6 w-6 text-green-600"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                >
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg>
                <svg
                    v-else-if="transaction.status === 'PENDING'"
                    class="h-6 w-6 text-yellow-600"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                >
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <svg
                    v-else
                    class="h-6 w-6 text-red-600"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                >
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </div>

              <!-- Info -->
              <div class="flex-1">
                <div class="flex items-center space-x-2 mb-1">
                  <h3 class="font-bold text-gray-900">{{ transaction.planName }}</h3>
                  <span
                      :class="[
                      'px-2 py-1 rounded-full text-xs font-semibold',
                      transaction.status === 'SUCCESS' ? 'bg-green-100 text-green-700' :
                      transaction.status === 'PENDING' ? 'bg-yellow-100 text-yellow-700' :
                      transaction.status === 'FAILED' ? 'bg-red-100 text-red-700' :
                      'bg-gray-100 text-gray-700'
                    ]"
                  >
                    {{ getStatusText(transaction.status) }}
                  </span>
                </div>

                <p class="text-sm text-gray-600 mb-2">{{ transaction.description }}</p>

                <div class="flex items-center space-x-4 text-sm text-gray-500">
                  <span class="flex items-center">
                    <svg class="h-4 w-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    {{ formatDate(transaction.createdAt) }}
                  </span>
                  <span class="flex items-center">
                    <svg class="h-4 w-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
                    </svg>
                    {{ transaction.paymentMethod }}
                  </span>
                  <span class="font-mono text-xs">
                    #{{ transaction.transactionId }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Amount -->
            <div class="text-right">
              <p class="text-2xl font-bold text-gray-900">{{ formatCurrency(transaction.amount) }}</p>
              <p v-if="transaction.status === 'SUCCESS'" class="text-sm text-green-600 mt-1">
                ✓ Đã thanh toán
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import paymentService from '@/services/paymentService'
import { useToast } from '@/composables/useToast'

const toast = useToast()

const transactions = ref([])
const loading = ref(false)

onMounted(() => {
  loadTransactions()
})

const loadTransactions = async () => {
  try {
    loading.value = true
    const response = await paymentService.getMyTransactions()
    transactions.value = response
  } catch (error) {
    toast.error('Lỗi', 'Không thể tải lịch sử giao dịch')
  } finally {
    loading.value = false
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
    currency: 'VND'
  }).format(amount)
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleString('vi-VN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>