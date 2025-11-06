<template>
  <div class="min-h-screen bg-gray-50 py-8">
    <div class="max-w-4xl mx-auto px-4">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">Hồ sơ cá nhân</h1>
        <p class="text-gray-600 mt-2">Quản lý thông tin tài khoản của bạn</p>
      </div>

      <!-- Profile Card -->
      <div class="bg-white rounded-xl shadow-sm p-6 mb-6">
        <div class="flex items-start justify-between mb-6">
          <div class="flex items-center space-x-4">
            <div class="h-20 w-20 rounded-full bg-blue-600 flex items-center justify-center text-white text-3xl font-bold">
              {{ user?.email?.[0]?.toUpperCase() }}
            </div>
            <div>
              <h2 class="text-2xl font-bold text-gray-900">{{ user?.fullName || 'Người dùng' }}</h2>
              <p class="text-gray-600">{{ user?.email }}</p>
              <div class="mt-2">
                <span
                    :class="[
                    'inline-block px-3 py-1 rounded-full text-sm font-semibold',
                    user?.role === 'PREMIUM' ? 'bg-gradient-to-r from-yellow-400 to-orange-500 text-white' :
                    user?.role === 'ADMIN' ? 'bg-purple-600 text-white' :
                    'bg-gray-200 text-gray-700'
                  ]"
                >
                  {{ getRoleText(user?.role) }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Stats -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
          <div class="bg-blue-50 rounded-lg p-4">
            <p class="text-sm text-gray-600 mb-1">Tổng câu hỏi</p>
            <p class="text-2xl font-bold text-blue-600">{{ stats.totalQuestions || 0 }}</p>
          </div>
          <div class="bg-green-50 rounded-lg p-4">
            <p class="text-sm text-gray-600 mb-1">Tài liệu</p>
            <p class="text-2xl font-bold text-green-600">{{ stats.totalDocuments || 0 }}</p>
          </div>
          <div class="bg-purple-50 rounded-lg p-4">
            <p class="text-sm text-gray-600 mb-1">Thành viên từ</p>
            <p class="text-lg font-bold text-purple-600">{{ formatDate(user?.createdAt) }}</p>
          </div>
        </div>

        <!-- Premium Info -->
        <div v-if="user?.role === 'PREMIUM'" class="bg-gradient-to-r from-yellow-50 to-orange-50 border border-orange-200 rounded-lg p-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="font-semibold text-gray-900">Gói Premium</p>
              <p class="text-sm text-gray-600">Hết hạn: {{ formatDate(user?.premiumExpiresAt) }}</p>
            </div>
            <router-link
                to="/premium"
                class="px-4 py-2 bg-orange-500 text-white rounded-lg font-semibold hover:bg-orange-600 transition"
            >
              Gia hạn
            </router-link>
          </div>
        </div>

        <!-- Upgrade Button for Free Users -->
        <div v-else-if="user?.role === 'FREE'" class="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="font-semibold text-gray-900">Nâng cấp lên Premium</p>
              <p class="text-sm text-gray-600">Mở khóa tất cả tính năng</p>
            </div>
            <router-link
                to="/premium"
                class="px-4 py-2 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 transition"
            >
              Nâng cấp
            </router-link>
          </div>
        </div>
      </div>

      <!-- Account Information -->
      <div class="bg-white rounded-xl shadow-sm p-6 mb-6">
        <h3 class="text-xl font-bold text-gray-900 mb-4">Thông tin tài khoản</h3>

        <form @submit.prevent="updateProfile" class="space-y-4">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Họ và tên</label>
              <input
                  v-model="profileForm.fullName"
                  type="text"
                  class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Số điện thoại</label>
              <input
                  v-model="profileForm.phone"
                  type="tel"
                  class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Email</label>
            <input
                :value="user?.email"
                type="email"
                disabled
                class="w-full px-4 py-3 border border-gray-300 rounded-lg bg-gray-100 cursor-not-allowed"
            />
            <p class="text-xs text-gray-500 mt-1">Email không thể thay đổi</p>
          </div>

          <button
              type="submit"
              :disabled="updating"
              class="px-6 py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 disabled:bg-gray-400 transition"
          >
            {{ updating ? 'Đang cập nhật...' : 'Cập nhật thông tin' }}
          </button>
        </form>
      </div>

      <!-- Change Password -->
      <div class="bg-white rounded-xl shadow-sm p-6">
        <h3 class="text-xl font-bold text-gray-900 mb-4">Đổi mật khẩu</h3>

        <form @submit.prevent="changePassword" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Mật khẩu hiện tại</label>
            <input
                v-model="passwordForm.currentPassword"
                type="password"
                class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Mật khẩu mới</label>
            <input
                v-model="passwordForm.newPassword"
                type="password"
                class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Xác nhận mật khẩu mới</label>
            <input
                v-model="passwordForm.confirmPassword"
                type="password"
                class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <button
              type="submit"
              :disabled="changingPassword"
              class="px-6 py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 disabled:bg-gray-400 transition"
          >
            {{ changingPassword ? 'Đang đổi...' : 'Đổi mật khẩu' }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const authStore = useAuthStore()
const toast = useToast()

const user = computed(() => authStore.user)
const stats = ref({
  totalQuestions: 0,
  totalDocuments: 0
})

const profileForm = ref({
  fullName: '',
  phone: ''
})

const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const updating = ref(false)
const changingPassword = ref(false)

onMounted(async () => {
  await authStore.fetchCurrentUser()
  profileForm.value.fullName = user.value?.fullName || ''
  profileForm.value.phone = user.value?.phone || ''
})

const getRoleText = (role) => {
  const roles = {
    'FREE': 'Miễn phí',
    'PREMIUM': '⚡ Premium',
    'ADMIN': '👑 Admin'
  }
  return roles[role] || role
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const updateProfile = async () => {
  try {
    updating.value = true
    // Call API to update profile
    toast.success('Thành công', 'Cập nhật thông tin thành công!')
  } catch (error) {
    toast.error('Lỗi', 'Không thể cập nhật thông tin')
  } finally {
    updating.value = false
  }
}

const changePassword = async () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    toast.error('Lỗi', 'Mật khẩu xác nhận không khớp')
    return
  }

  try {
    changingPassword.value = true
    // Call API to change password
    toast.success('Thành công', 'Đổi mật khẩu thành công!')
    passwordForm.value = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  } catch (error) {
    toast.error('Lỗi', 'Không thể đổi mật khẩu')
  } finally {
    changingPassword.value = false
  }
}
</script>