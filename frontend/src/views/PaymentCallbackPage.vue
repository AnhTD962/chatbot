<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 px-4">
    <div class="max-w-md w-full bg-white rounded-2xl shadow-xl p-8">
      <!-- Loading State -->
      <div v-if="loading" class="text-center py-8">
        <div class="spinner mx-auto mb-4"></div>
        <p class="text-gray-600">Đang xử lý thanh toán...</p>
      </div>

      <!-- Success State -->
      <div v-else-if="status === 'success'" class="text-center py-8">
        <div class="inline-flex items-center justify-center h-20 w-20 rounded-full bg-green-100 mb-6">
          <svg class="h-10 w-10 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
          </svg>
        </div>

        <h2 class="text-3xl font-bold text-gray-800 mb-2">Thanh toán thành công!</h2>
        <p class="text-gray-600 mb-6">Tài khoản của bạn đã được nâng cấp lên Premium</p>

        <div class="bg-blue-50 rounded-lg p-4 mb-6">
          <div class="flex justify-between items-center mb-2">
            <span class="text-sm text-gray-600">Gói đã mua:</span>
            <span class="font-semibold text-gray-800">{{ transactionData?.planName }}</span>
          </div>
          <div class="flex justify-between items-center mb-2">
            <span class="text-sm text-gray-600">Số tiền:</span>
            <span class="font-semibold text-gray-800">{{ formatCurrency(transactionData?.amount) }}</span>
          </div>
          <div class="flex justify-between items-center">
            <span class="text-sm text-gray-600">Mã giao dịch:</span>
            <span class="font-mono text-sm text-gray-800">{{ transactionData?.transactionId }}</span>
          </div>
        </div>

        <div class="space-y-3">
          <router-link
              to="/"
              class="block w-full py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 transition text-center"
          >
            Bắt đầu sử dụng
          </router-link>
          <router-link
              to="/transactions"
              class="block w-full py-3 bg-gray-100 text-gray-700 rounded-lg font-semibold hover:bg-gray-200 transition text-center"
          >
            Xem lịch sử giao dịch
          </router-link>
        </div>
      </div>

      <!-- Failed State -->
      <div v-else-if="status === 'failed'" class="text-center py-8">
        <div class="inline-flex items-center justify-center h-20 w-20 rounded-full bg-red-100 mb-6">
          <svg class="h-10 w-10 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </div>

        <h2 class="text-3xl font-bold text-gray-800 mb-2">Thanh toán thất bại</h2>
        <p class="text-gray-600 mb-6">{{ errorMessage || 'Có lỗi xảy ra trong quá trình thanh toán' }}</p>

        <div class="space-y-3">
          <router-link
              to="/premium"
              class="block w-full py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 transition text-center"
          >
            Thử lại
          </router-link>
          <router-link
              to="/"
              class="block w-full py-3 bg-gray-100 text-gray-700 rounded-lg font-semibold hover:bg-gray-200 transition text-center"
          >
            Về trang chủ
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import paymentService from '@/services/paymentService'

const route = useRoute()

const loading = ref(true)
const status = ref('')
const transactionData = ref(null)
const errorMessage = ref('')

onMounted(async () => {
  try {
    const queryParams = route.query
    const response = await paymentService.handleCallback(queryParams)

    if (response.success) {
      status.value = 'success'
      transactionData.value = response.transaction
    } else {
      status.value = 'failed'
      errorMessage.value = response.message
    }
  } catch (error) {
    status.value = 'failed'
    errorMessage.value = 'Không thể xác minh thanh toán. Vui lòng liên hệ hỗ trợ.'
  } finally {
    loading.value = false
  }
})

const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}
</script>