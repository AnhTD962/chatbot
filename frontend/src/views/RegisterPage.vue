<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 px-4">
    <div class="max-w-md w-full bg-white rounded-2xl shadow-xl p-8">
      <!-- Logo -->
      <div class="text-center mb-8">
        <img src="../assets/hoaphat-logo.png" alt="Hoaphat" class="h-20 w-30 mx-auto mb-4" />
        <h2 class="text-3xl font-bold text-gray-800">Đăng ký</h2>
        <p class="text-gray-600 mt-2">Tạo tài khoản miễn phí ngay!</p>
      </div>

      <!-- Error Message -->
      <div v-if="error" class="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg">
        <p class="text-red-600 text-sm">{{ error }}</p>
      </div>

      <!-- Success Message -->
      <div v-if="success" class="mb-4 p-4 bg-green-50 border border-green-200 rounded-lg">
        <p class="text-green-600 text-sm">{{ success }}</p>
      </div>

      <!-- Register Form -->
      <form @submit.prevent="handleRegister" class="space-y-5">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Họ và tên</label>
          <input
              v-model="form.fullName"
              type="text"
              required
              placeholder="Nguyễn Văn A"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Email</label>
          <input
              v-model="form.email"
              type="email"
              required
              placeholder="example@email.com"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Số điện thoại</label>
          <input
              v-model="form.phoneNumber"
              type="tel"
              placeholder="0123456789"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Mật khẩu</label>
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
          <label class="block text-sm font-medium text-gray-700 mb-2">Xác nhận mật khẩu</label>
          <input
              v-model="form.confirmPassword"
              :type="showPassword ? 'text' : 'password'"
              required
              placeholder="••••••••"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div class="flex items-start">
          <input
              type="checkbox"
              v-model="form.acceptTerms"
              required
              class="h-4 w-4 text-blue-600 rounded mt-1"
          />
          <label class="ml-2 text-sm text-gray-600">
            Tôi đồng ý với
            <a href="#" class="text-blue-600 hover:text-blue-700">Điều khoản dịch vụ</a>
            và
            <a href="#" class="text-blue-600 hover:text-blue-700">Chính sách bảo mật</a>
          </label>
        </div>

        <button
            type="submit"
            :disabled="loading"
            class="w-full py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 disabled:bg-gray-400 transition"
        >
          {{ loading ? 'Đang đăng ký...' : 'Đăng ký' }}
        </button>
      </form>

      <!-- Login Link -->
      <div class="mt-6 text-center">
        <p class="text-gray-600">
          Đã có tài khoản?
          <router-link to="/login" class="text-blue-600 font-semibold hover:text-blue-700">
            Đăng nhập ngay
          </router-link>
        </p>
        <p class="text-gray-600 mt-2">
          <router-link to="/" class="text-blue-500 hover:text-blue-700">
            ← Quay về trang chủ
          </router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

const form = ref({
  fullName: '',
  email: '',
  phoneNumber: '',
  password: '',
  confirmPassword: '',
  acceptTerms: false
})

const showPassword = ref(false)
const loading = ref(false)
const error = ref('')
const success = ref('')

const handleRegister = async () => {
  error.value = ''
  success.value = ''

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

    await authStore.register({
      fullName: form.value.fullName,
      email: form.value.email,
      phone: form.value.phoneNumber,
      password: form.value.password
    })

    success.value = 'Đăng ký thành công! Đang chuyển đến trang đăng nhập...'
    toast.success('Đăng ký thành công!', 'Vui lòng đăng nhập để tiếp tục')

    setTimeout(() => {
      router.push('/login')
    }, 2000)
  } catch (err) {
    error.value = err.response?.data?.message || 'Đăng ký thất bại. Vui lòng thử lại.'
  } finally {
    loading.value = false
  }
}
</script>