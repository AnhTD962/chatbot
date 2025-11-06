<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 px-4">
    <div class="max-w-md w-full bg-white rounded-2xl shadow-xl p-8">
      <!-- Header -->
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center h-16 w-16 rounded-full bg-blue-100 mb-4">
          <svg class="h-8 w-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
          </svg>
        </div>
        <h2 class="text-3xl font-bold text-gray-800">Đặt lại mật khẩu</h2>
        <p class="text-gray-600 mt-2">Nhập mật khẩu mới của bạn</p>
      </div>

      <!-- Loading State -->
      <div v-if="verifying" class="text-center py-8">
        <div class="spinner mx-auto mb-4"></div>
        <p class="text-gray-600">Đang xác thực...</p>
      </div>

      <!-- Invalid Token -->
      <div v-else-if="invalidToken" class="text-center py-8">
        <svg class="h-16 w-16 text-red-500 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <h3 class="text-xl font-bold text-gray-800 mb-2">Link không hợp lệ</h3>
        <p class="text-gray-600 mb-6">Link đặt lại mật khẩu đã hết hạn hoặc không hợp lệ</p>
        <router-link to="/forgot-password" class="text-blue-600 font-semibold hover:text-blue-700">
          Yêu cầu link mới
        </router-link>
      </div>

      <!-- Success Message -->
      <div v-else-if="success" class="text-center py-8">
        <svg class="h-16 w-16 text-green-500 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <h3 class="text-xl font-bold text-gray-800 mb-2">Đặt lại mật khẩu thành công!</h3>
        <p class="text-gray-600 mb-6">Bạn có thể đăng nhập với mật khẩu mới</p>
        <router-link
            to="/login"
            class="inline-block px-6 py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 transition"
        >
          Đăng nhập ngay
        </router-link>
      </div>

      <!-- Reset Form -->
      <form v-else @submit.prevent="handleResetPassword" class="space-y-6">
        <!-- Error Message -->
        <div v-if="error" class="p-4 bg-red-50 border border-red-200 rounded-lg">
          <p class="text-red-600 text-sm">{{ error }}</p>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Mật khẩu mới</label>
          <div class="relative">
            <input
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                required
                placeholder="••••••••"
                class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <button
                type="button"
                @click="showPassword = !showPassword"
                class="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500"
            >
              <svg v-if="!showPassword" class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
              </svg>
              <svg v-else class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
              </svg>
            </button>
          </div>
          <p class="text-xs text-gray-500 mt-1">Tối thiểu 6 ký tự</p>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Xác nhận mật khẩu mới</label>
          <input
              v-model="form.confirmPassword"
              :type="showPassword ? 'text' : 'password'"
              required
              placeholder="••••••••"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <button
            type="submit"
            :disabled="loading"
            class="w-full py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 disabled:bg-gray-400 transition"
        >
          {{ loading ? 'Đang xử lý...' : 'Đặt lại mật khẩu' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import authService from '@/services/authService'

const route = useRoute()
const authStore = useAuthStore()

const form = ref({
  password: '',
  confirmPassword: ''
})

const token = ref('')
const showPassword = ref(false)
const loading = ref(false)
const verifying = ref(true)
const invalidToken = ref(false)
const success = ref(false)
const error = ref('')

onMounted(async () => {
  token.value = route.query.token

  if (!token.value) {
    invalidToken.value = true
    verifying.value = false
    return
  }

  try {
    await authService.verifyResetToken(token.value)
    verifying.value = false
  } catch (err) {
    invalidToken.value = true
    verifying.value = false
  }
})

const handleResetPassword = async () => {
  error.value = ''

  if (form.value.password !== form.value.confirmPassword) {
    error.value = 'Mật khẩu xác nhận không khớp'
    return
  }

  if (form.value.password.length < 6) {
    error.value = 'Mật khẩu phải có ít nhất 6 ký tự'
    return
  }

  try {
    loading.value = true

    await authStore.resetPassword(token.value, form.value.password)

    success.value = true
  } catch (err) {
    error.value = err.response?.data?.message || 'Đặt lại mật khẩu thất bại. Vui lòng thử lại.'
  } finally {
    loading.value = false
  }
}
</script>