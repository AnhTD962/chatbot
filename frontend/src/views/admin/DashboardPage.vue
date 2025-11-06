<template>
  <div>
    <!-- Stats Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <div class="bg-white rounded-xl shadow-sm p-6">
        <div class="flex items-center justify-between mb-4">
          <div class="h-12 w-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <svg class="h-6 w-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
            </svg>
          </div>
          <span class="text-sm font-semibold text-green-600">+12%</span>
        </div>
        <p class="text-gray-600 text-sm mb-1">Tổng người dùng</p>
        <p class="text-3xl font-bold text-gray-900">{{ stats.totalUsers }}</p>
      </div>

      <div class="bg-white rounded-xl shadow-sm p-6">
        <div class="flex items-center justify-between mb-4">
          <div class="h-12 w-12 bg-green-100 rounded-lg flex items-center justify-center">
            <svg class="h-6 w-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <span class="text-sm font-semibold text-green-600">+8%</span>
        </div>
        <p class="text-gray-600 text-sm mb-1">Doanh thu tháng</p>
        <p class="text-3xl font-bold text-gray-900">{{ formatCurrency(stats.monthlyRevenue) }}</p>
      </div>

      <div class="bg-white rounded-xl shadow-sm p-6">
        <div class="flex items-center justify-between mb-4">
          <div class="h-12 w-12 bg-purple-100 rounded-lg flex items-center justify-center">
            <svg class="h-6 w-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
            </svg>
          </div>
          <span class="text-sm font-semibold text-green-600">+25%</span>
        </div>
        <p class="text-gray-600 text-sm mb-1">Câu hỏi hôm nay</p>
        <p class="text-3xl font-bold text-gray-900">{{ stats.todayQuestions }}</p>
      </div>

      <div class="bg-white rounded-xl shadow-sm p-6">
        <div class="flex items-center justify-between mb-4">
          <div class="h-12 w-12 bg-orange-100 rounded-lg flex items-center justify-center">
            <svg class="h-6 w-6 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
          </div>
          <span class="text-sm font-semibold text-green-600">+15%</span>
        </div>
        <p class="text-gray-600 text-sm mb-1">Premium users</p>
        <p class="text-3xl font-bold text-gray-900">{{ stats.premiumUsers }}</p>
      </div>
    </div>

    <!-- Charts Row -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
      <!-- Revenue Chart -->
      <div class="bg-white rounded-xl shadow-sm p-6">
        <h3 class="text-lg font-bold text-gray-900 mb-4">Doanh thu 7 ngày</h3>
        <div class="h-64 flex items-end justify-between space-x-2">
          <div
              v-for="(day, index) in revenueChart"
              :key="index"
              class="flex-1 bg-blue-500 rounded-t-lg hover:bg-blue-600 transition cursor-pointer relative group"
              :style="{ height: `${(day.amount / maxRevenue) * 100}%` }"
          >
            <div class="absolute -top-8 left-1/2 transform -translate-x-1/2 bg-gray-900 text-white text-xs px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition whitespace-nowrap">
              {{ formatCurrency(day.amount) }}
            </div>
          </div>
        </div>
        <div class="flex justify-between mt-2 text-xs text-gray-600">
          <span v-for="(day, index) in revenueChart" :key="index">{{ day.label }}</span>
        </div>
      </div>

      <!-- User Growth Chart -->
      <div class="bg-white rounded-xl shadow-sm p-6">
        <h3 class="text-lg font-bold text-gray-900 mb-4">Người dùng mới 7 ngày</h3>
        <div class="h-64 flex items-end justify-between space-x-2">
          <div
              v-for="(day, index) in userGrowthChart"
              :key="index"
              class="flex-1 bg-green-500 rounded-t-lg hover:bg-green-600 transition cursor-pointer relative group"
              :style="{ height: `${(day.users / maxUsers) * 100}%` }"
          >
            <div class="absolute -top-8 left-1/2 transform -translate-x-1/2 bg-gray-900 text-white text-xs px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition">
              {{ day.users }} users
            </div>
          </div>
        </div>
        <div class="flex justify-between mt-2 text-xs text-gray-600">
          <span v-for="(day, index) in userGrowthChart" :key="index">{{ day.label }}</span>
        </div>
      </div>
    </div>

    <!-- Recent Activity -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Recent Transactions -->
      <div class="bg-white rounded-xl shadow-sm p-6">
        <h3 class="text-lg font-bold text-gray-900 mb-4">Giao dịch gần đây</h3>
        <div class="space-y-4">
          <div
              v-for="transaction in recentTransactions"
              :key="transaction.id"
              class="flex items-center justify-between pb-4 border-b border-gray-100 last:border-0"
          >
            <div class="flex items-center space-x-3">
              <div class="h-10 w-10 bg-green-100 rounded-full flex items-center justify-center">
                <svg class="h-5 w-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <div>
                <p class="font-semibold text-gray-900 text-sm">{{ transaction.userEmail }}</p>
                <p class="text-xs text-gray-500">{{ transaction.planName }}</p>
              </div>
            </div>
            <div class="text-right">
              <p class="font-bold text-gray-900">{{ formatCurrency(transaction.amount) }}</p>
              <p class="text-xs text-gray-500">{{ formatTime(transaction.createdAt) }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- New Users -->
      <div class="bg-white rounded-xl shadow-sm p-6">
        <h3 class="text-lg font-bold text-gray-900 mb-4">Người dùng mới</h3>
        <div class="space-y-4">
          <div
              v-for="user in newUsers"
              :key="user.id"
              class="flex items-center justify-between pb-4 border-b border-gray-100 last:border-0"
          >
            <div class="flex items-center space-x-3">
              <div class="h-10 w-10 bg-blue-600 rounded-full flex items-center justify-center text-white font-semibold">
                {{ user.email[0].toUpperCase() }}
              </div>
              <div>
                <p class="font-semibold text-gray-900 text-sm">{{ user.fullName }}</p>
                <p class="text-xs text-gray-500">{{ user.email }}</p>
              </div>
            </div>
            <span
                :class="[
                'px-2 py-1 rounded-full text-xs font-semibold',
                user.role === 'PREMIUM' ? 'bg-yellow-100 text-yellow-700' : 'bg-gray-100 text-gray-700'
              ]"
            >
              {{ user.role }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import adminService from '@/services/adminService'

const stats = ref({
  totalUsers: 0,
  monthlyRevenue: 0,
  todayQuestions: 0,
  premiumUsers: 0
})

const revenueChart = ref([])
const userGrowthChart = ref([])
const recentTransactions = ref([])
const newUsers = ref([])

const maxRevenue = computed(() => Math.max(...revenueChart.value.map(d => d.amount), 1))
const maxUsers = computed(() => Math.max(...userGrowthChart.value.map(d => d.users), 1))

onMounted(async () => {
  await loadDashboardData()
})

const loadDashboardData = async () => {
  try {
    const response = await adminService.getDashboardStats()
    stats.value = response.stats
    revenueChart.value = response.revenueChart || generateMockRevenueData()
    userGrowthChart.value = response.userGrowthChart || generateMockUserData()
    recentTransactions.value = response.recentTransactions || []
    newUsers.value = response.newUsers || []
  } catch (error) {
    console.error('Failed to load dashboard:', error)
    // Use mock data for demo
    stats.value = {
      totalUsers: 1234,
      monthlyRevenue: 45600000,
      todayQuestions: 567,
      premiumUsers: 89
    }
    revenueChart.value = generateMockRevenueData()
    userGrowthChart.value = generateMockUserData()
  }
}

const generateMockRevenueData = () => {
  const days = ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN']
  return days.map((label, i) => ({
    label,
    amount: Math.random() * 5000000 + 1000000
  }))
}

const generateMockUserData = () => {
  const days = ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN']
  return days.map((label, i) => ({
    label,
    users: Math.floor(Math.random() * 50) + 10
  }))
}

const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0
  }).format(amount)
}

const formatTime = (dateString) => {
  return new Date(dateString).toLocaleString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
    day: '2-digit',
    month: '2-digit'
  })
}
</script>