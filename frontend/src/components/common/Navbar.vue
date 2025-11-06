<template>
  <header class="bg-white shadow-sm p-4">
    <div class="max-w-4xl mx-auto flex items-center justify-between">
      <!-- Logo -->
      <div class="flex items-center space-x-3">
        <div class="h-10 w-10 bg-blue-600 rounded-full flex items-center justify-center">
          <svg class="h-6 w-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
          </svg>
        </div>
        <div>
          <h1 class="text-lg font-bold text-gray-800">Hoaphat AI Assistant</h1>
          <p class="text-xs text-gray-500">Hỗ trợ 24/7</p>
        </div>
      </div>

      <!-- Auth Section -->
      <div>
        <!-- Chưa đăng nhập -->
        <div v-if="!isAuthenticated" class="flex items-center space-x-4 text-sm">
          <div class="flex items-center space-x-2">
            <span class="text-gray-600">Câu hỏi còn lại:</span>
            <span class="px-3 py-1 bg-orange-100 text-orange-700 rounded-full font-semibold">
              {{ remainingQuestions }}/5
            </span>
          </div>
          <router-link
              to="/login"
              class="px-4 py-2 bg-blue-50 text-blue-600 rounded-lg hover:bg-blue-100 transition"
          >
            Đăng nhập
          </router-link>
          <router-link
              to="/register"
              class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
          >
            Đăng ký
          </router-link>
        </div>

        <!-- Đã đăng nhập -->
        <div v-else class="flex items-center space-x-4">
          <div class="flex items-center space-x-2">
            <div
                class="h-8 w-8 bg-blue-600 rounded-full flex items-center justify-center text-white font-semibold uppercase"
            >
              {{ userInitials }}
            </div>
            <span class="font-medium text-gray-800">{{ userName }}</span>
          </div>
          <button
              @click="logout"
              class="px-3 py-2 bg-red-100 text-red-600 rounded-lg hover:bg-red-200 text-sm transition"
          >
            Đăng xuất
          </button>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const props = defineProps({
  remainingQuestions: {
    type: Number,
    default: 5
  }
})

const authStore = useAuthStore()
const isAuthenticated = computed(() => authStore.isAuthenticated)
const userName = computed(() => authStore.user?.name || 'Người dùng')
const userInitials = computed(() =>
    authStore.user?.name
        ? authStore.user.name.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase()
        : 'U'
)

const logout = () => {
  authStore.logout()
}
</script>
