<template>
  <div>
    <!-- Filters -->
    <div class="bg-white rounded-xl shadow-sm p-6 mb-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <input
            v-model="filters.search"
            type="text"
            placeholder="Tìm kiếm email, tên..."
            class="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <select
            v-model="filters.role"
            class="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="">Tất cả vai trò</option>
          <option value="FREE">Free</option>
          <option value="PREMIUM">Premium</option>
          <option value="ADMIN">Admin</option>
        </select>
        <select
            v-model="filters.status"
            class="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="">Tất cả trạng thái</option>
          <option value="ACTIVE">Hoạt động</option>
          <option value="DISABLED">Vô hiệu hóa</option>
        </select>
        <button
            @click="loadUsers"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
        >
          Tìm kiếm
        </button>
      </div>
    </div>

    <!-- Users Table -->
    <div class="bg-white rounded-xl shadow-sm overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead class="bg-gray-50 border-b border-gray-200">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Người dùng</th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Vai trò</th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Trạng thái</th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Ngày tạo</th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Hành động</th>
          </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
          <tr v-if="loading">
            <td colspan="5" class="px-6 py-12 text-center">
              <LoadingSpinner text="Đang tải..." />
            </td>
          </tr>
          <tr v-else-if="users.length === 0">
            <td colspan="5" class="px-6 py-12 text-center text-gray-500">
              Không tìm thấy người dùng
            </td>
          </tr>
          <tr v-else v-for="user in users" :key="user.id" class="hover:bg-gray-50">
            <td class="px-6 py-4">
              <div class="flex items-center space-x-3">
                <div class="h-10 w-10 rounded-full bg-blue-600 flex items-center justify-center text-white font-semibold">
                  {{ user.email[0].toUpperCase() }}
                </div>
                <div>
                  <p class="font-semibold text-gray-900">{{ user.fullName }}</p>
                  <p class="text-sm text-gray-500">{{ user.email }}</p>
                </div>
              </div>
            </td>
            <td class="px-6 py-4">
                <span
                    :class="[
                    'inline-block px-3 py-1 rounded-full text-xs font-semibold',
                    user.role === 'PREMIUM' ? 'bg-yellow-100 text-yellow-700' :
                    user.role === 'ADMIN' ? 'bg-purple-100 text-purple-700' :
                    'bg-gray-100 text-gray-700'
                  ]"
                >
                  {{ user.role }}
                </span>
            </td>
            <td class="px-6 py-4">
                <span
                    :class="[
                    'inline-block px-3 py-1 rounded-full text-xs font-semibold',
                    user.status === 'ACTIVE' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                  ]"
                >
                  {{ user.status === 'ACTIVE' ? 'Hoạt động' : 'Vô hiệu hóa' }}
                </span>
            </td>
            <td class="px-6 py-4 text-sm text-gray-600">
              {{ formatDate(user.createdAt) }}
            </td>
            <td class="px-6 py-4">
              <div class="flex items-center space-x-2">
                <button
                    @click="toggleUserStatus(user)"
                    :class="[
                      'px-3 py-1 rounded text-xs font-semibold transition',
                      user.status === 'ACTIVE'
                        ? 'bg-red-100 text-red-700 hover:bg-red-200'
                        : 'bg-green-100 text-green-700 hover:bg-green-200'
                    ]"
                >
                  {{ user.status === 'ACTIVE' ? 'Vô hiệu hóa' : 'Kích hoạt' }}
                </button>
                <button
                    @click="viewUserDetails(user)"
                    class="px-3 py-1 bg-blue-100 text-blue-700 rounded text-xs font-semibold hover:bg-blue-200 transition"
                >
                  Chi tiết
                </button>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="px-6 py-4 border-t border-gray-200 flex items-center justify-between">
        <p class="text-sm text-gray-600">
          Hiển thị {{ (currentPage - 1) * pageSize + 1 }} - {{ Math.min(currentPage * pageSize, totalUsers) }}
          trong tổng số {{ totalUsers }} người dùng
        </p>
        <div class="flex items-center space-x-2">
          <button
              @click="currentPage--"
              :disabled="currentPage === 1"
              class="px-3 py-1 border border-gray-300 rounded hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Trước
          </button>
          <span class="px-3 py-1 bg-blue-600 text-white rounded">{{ currentPage }}</span>
          <button
              @click="currentPage++"
              :disabled="currentPage * pageSize >= totalUsers"
              class="px-3 py-1 border border-gray-300 rounded hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
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

const users = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalUsers = ref(0)

const filters = ref({
  search: '',
  role: '',
  status: ''
})

onMounted(() => {
  loadUsers()
})

watch(currentPage, () => {
  loadUsers()
})

const loadUsers = async () => {
  try {
    loading.value = true
    const response = await adminService.getUsers({
      page: currentPage.value,
      size: pageSize.value,
      ...filters.value
    })
    users.value = response.users
    totalUsers.value = response.total
  } catch (error) {
    toast.error('Lỗi', 'Không thể tải danh sách người dùng')
  } finally {
    loading.value = false
  }
}

const toggleUserStatus = async (user) => {
  const newStatus = user.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'

  if (!confirm(`Bạn có chắc chắn muốn ${newStatus === 'DISABLED' ? 'vô hiệu hóa' : 'kích hoạt'} người dùng này?`)) {
    return
  }

  try {
    await adminService.updateUserStatus(user.id, newStatus)
    toast.success('Thành công', 'Cập nhật trạng thái người dùng thành công')
    loadUsers()
  } catch (error) {
    toast.error('Lỗi', 'Không thể cập nhật trạng thái')
  }
}

const viewUserDetails = (user) => {
  // Open user details modal
  console.log('View user details:', user)
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}
</script>