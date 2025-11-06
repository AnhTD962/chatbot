<template>
  <div class="min-h-screen bg-gradient-to-br from-purple-50 via-blue-50 to-indigo-100 py-12 px-4">
    <div class="max-w-7xl mx-auto">
      <!-- Header -->
      <div class="text-center mb-12">
        <h1 class="text-4xl md:text-5xl font-bold text-gray-900 mb-4">
          Nâng cấp lên <span class="text-transparent bg-clip-text bg-gradient-to-r from-yellow-400 to-orange-600">Premium</span>
        </h1>
        <p class="text-xl text-gray-600">Mở khóa toàn bộ tính năng và trải nghiệm không giới hạn</p>
      </div>

      <!-- Pricing Cards -->
      <div class="grid md:grid-cols-3 gap-8 mb-12">
        <!-- Free Plan -->
        <div class="bg-white rounded-2xl shadow-lg p-8 border-2 border-gray-200">
          <div class="text-center mb-6">
            <h3 class="text-2xl font-bold text-gray-800 mb-2">Miễn phí</h3>
            <div class="text-4xl font-bold text-gray-900 mb-1">0đ</div>
            <p class="text-gray-500">Mãi mãi</p>
          </div>

          <ul class="space-y-4 mb-8">
            <li class="flex items-start">
              <svg class="h-5 w-5 text-green-500 mr-3 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
              </svg>
              <span class="text-gray-600">20 câu hỏi/ngày</span>
            </li>
            <li class="flex items-start">
              <svg class="h-5 w-5 text-green-500 mr-3 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
              </svg>
              <span class="text-gray-600">Trả lời cơ bản</span>
            </li>
            <li class="flex items-start">
              <svg class="h-5 w-5 text-gray-300 mr-3 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
              </svg>
              <span class="text-gray-400 line-through">Phân tích PDF</span>
            </li>
            <li class="flex items-start">
              <svg class="h-5 w-5 text-gray-300 mr-3 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
              </svg>
              <span class="text-gray-400 line-through">Hỗ trợ ưu tiên</span>
            </li>
          </ul>

          <button
              disabled
              class="w-full py-3 bg-gray-300 text-gray-500 rounded-lg font-semibold cursor-not-allowed"
          >
            Gói hiện tại
          </button>
        </div>

        <!-- Monthly Premium -->
        <div class="bg-gradient-to-br from-blue-500 to-indigo-600 rounded-2xl shadow-2xl p-8 text-white transform scale-105 relative">
          <div class="absolute -top-4 left-1/2 transform -translate-x-1/2">
            <span class="bg-yellow-400 text-gray-900 px-4 py-1 rounded-full text-sm font-bold">PHỔ BIẾN</span>
          </div>

          <div class="text-center mb-6">
            <h3 class="text-2xl font-bold mb-2">Premium Tháng</h3>
            <div class="text-5xl font-bold mb-1">99.000đ</div>
            <p class="text-blue-100">Mỗi tháng</p>
          </div>

          <ul class="space-y-4 mb-8">
            <li class="flex items-start">
              <svg class="h-5 w-5 text-yellow-400 mr-3 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
              </svg>
              <span class="font-semibold">Không giới hạn câu hỏi</span>
            </li>
            <li class="flex items-start">
              <svg class="h-5 w-5 text-yellow-400 mr-3 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
              </svg>
              <span class="font-semibold">Trả lời chi tiết, chính xác</span>
            </li>
            <li class="flex items-start">
              <svg class="h-5 w-5 text-yellow-400 mr-3 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
              </svg>
              <span class="font-semibold">Phân tích PDF không giới hạn</span>
            </li>
            <li class="flex items-start">
              <svg class="h-5 w-5 text-yellow-400 mr-3 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
              </svg>
              <span class="font-semibold">Hỗ trợ ưu tiên 24/7</span>
            </li>
          </ul>

          <button
              @click="handleUpgrade('MONTHLY')"
              :disabled="loading"
              class="w-full py-3 bg-white text-blue-600 rounded-lg font-bold hover:bg-gray-100 transition"
          >
            {{ loading ? 'Đang xử lý...' : 'Nâng cấp ngay' }}
          </button>
        </div>

        <!-- Yearly Premium -->
        <div class="bg-white rounded-2xl shadow-lg p-8 border-2 border-purple-200 relative">
          <div class="absolute -top-4 left-1/2 transform -translate-x-1/2">
            <span class="bg-purple-500 text-white px-4 py-1 rounded-full text-sm font-bold">TIẾT KIỆM 17%</span>
          </div>

          <div class="text-center mb-6">
            <h3 class="text-2xl font-bold text-gray-800 mb-2">Premium Năm</h3>
            <div class="text-5xl font-bold text-gray-900 mb-1">990.000đ</div>
            <p class="text-gray-500">
              <span class="line-through">1.188.000đ</span> / năm
            </p>
          </div>

          <ul class="space-y-4 mb-8">
            <li class="flex items-start">
              <svg class="h-5 w-5 text-green-500 mr-3 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
              </svg>
              <span class="text-gray-700 font-semibold">Tất cả tính năng Premium</span>
            </li>
            <li class="flex items-start">
              <svg class="h-5 w-5 text-green-500 mr-3 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
              </svg>
              <span class="text-gray-700 font-semibold">Tiết kiệm 198.000đ</span>
            </li>
            <li class="flex items-start">
              <svg class="h-5 w-5 text-green-500 mr-3 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
              </svg>
              <span class="text-gray-700 font-semibold">Ưu đãi đặc biệt</span>
            </li>
            <li class="flex items-start">
              <svg class="h-5 w-5 text-green-500 mr-3 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
              </svg>
              <span class="text-gray-700 font-semibold">Tính năng mới sớm nhất</span>
            </li>
          </ul>

          <button
              @click="handleUpgrade('YEARLY')"
              :disabled="loading"
              class="w-full py-3 bg-gradient-to-r from-purple-600 to-indigo-600 text-white rounded-lg font-bold hover:shadow-lg transition"
          >
            {{ loading ? 'Đang xử lý...' : 'Nâng cấp ngay' }}
          </button>
        </div>
      </div>

      <!-- Payment Methods -->
      <div class="text-center">
        <p class="text-gray-600 mb-4">Thanh toán an toàn với</p>
        <div class="flex justify-center items-center space-x-6">
          <img src="/vnpay-logo.png" alt="VNPay" class="h-8" />
          <span class="text-gray-400">|</span>
          <span class="text-gray-600">Visa, Mastercard, ATM</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import paymentService from '@/services/paymentService'

const router = useRouter()
const loading = ref(false)

const handleUpgrade = async (planType) => {
  try {
    loading.value = true
    const response = await paymentService.createPayment(planType)

    if (response.paymentUrl) {
      window.location.href = response.paymentUrl
    }
  } catch (error) {
    alert('Có lỗi xảy ra. Vui lòng thử lại sau.')
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>