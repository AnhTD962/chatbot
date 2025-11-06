<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 px-4">
    <div class="max-w-md w-full bg-white rounded-2xl shadow-xl p-8">
      <!-- Back Button -->
      <router-link to="/login" class="flex items-center text-gray-600 hover:text-gray-800 mb-6">
        <svg class="h-5 w-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
        Quay lại
      </router-link>

      <!-- Header -->
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center h-16 w-16 rounded-full bg-blue-100 mb-4">
          <svg class="h-8 w-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" />
          </svg>
        </div>
        <h2 class="text-3xl font-bold text-gray-800">Quên mật khẩu?</h2>
        <p class="text-gray-600 mt-2">Nhập email của bạn để đặt lại mật khẩu</p>
      </div>

      <!-- Success Message -->
      <div v-if="emailSent" class="mb-6 p-4 bg-green-50 border border-green-200 rounded-lg">
        <div class="flex items-start">
          <svg class="h-5 w-5 text-green-600 mt-0.5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <div>
            <p class="font-semibold text-green-900">Email đã được gửi!</p>
            <p class="text-sm text-green-700 mt-1">
              Vui lòng kiểm tra hộp thư của bạn và làm theo hướng dẫn để đặt lại mật khẩu.
            </p>
          </div>
        </div>
      </div>

      <!-- Error Message -->
      <div v-if="error" class="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
        <p class="text-red-600 text-sm">{{ error }}</p>
      </div>

      <!-- Form -->
      <form v-if="!emailSent" @submit.prevent="handleSubmit" class="space-y-6">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Email</label>
          <input
              v-model="email"
              type="email"
              required
              placeholder="example@email.com"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <button
            type="submit"
            :disabled="loading"
            class="w-full py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 disabled:bg-gray-400 transition"
        >
          {{ loading ? 'Đang gửi...' : 'Gửi email đặt lại mật khẩu' }}
        </button>
      </form>

      <!-- Resend Link -->
      <div v-if="emailSent" class="text-center">
        <p class="text-sm text-gray-600 mb-4">Không nhận được email?</p>
        <button
            @click="handleSubmit"
            :disabled="loading || countdown > 0"
            class="text-blue-600 font-semibold hover:text-blue-700 disabled:text-gray-400"
        >
          {{ countdown > 0 ? `Gửi lại sau ${countdown}s` : 'Gửi lại email' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const email = ref('')
const loading = ref(false)
const error = ref('')
const emailSent = ref(false)
const countdown = ref(0)

const handleSubmit = async () => {
  if (countdown.value > 0) return

  try {
    loading.value = true
    error.value = ''

    await authStore.forgotPassword(email.value)

    emailSent.value = true
    countdown.value = 60

    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value === 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (err) {
    error.value = err.response?.data?.message || 'Có lỗi xảy ra. Vui lòng thử lại.'
  } finally {
    loading.value = false
  }
}
</script>