import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import authService from '@/services/authService'

export const useAuthStore = defineStore('auth', () => {
    const user = ref(null)
    const token = ref(localStorage.getItem('token') || null)
    const loading = ref(false)
    const error = ref(null)

    const isAuthenticated = computed(() => !!token.value)
    const isAdmin = computed(() => user.value?.role === 'ADMIN')
    const isPremium = computed(() => user.value?.role === 'PREMIUM')
    const isFree = computed(() => user.value?.role === 'FREE')

    async function login(credentials) {
        try {
            loading.value = true
            error.value = null
            const response = await authService.login(credentials)
            token.value = response.token
            user.value = response.user
            localStorage.setItem('token', response.token)
            return response
        } catch (err) {
            error.value = err.response?.data?.message || 'Đăng nhập thất bại'
            throw err
        } finally {
            loading.value = false
        }
    }

    async function register(userData) {
        try {
            loading.value = true
            error.value = null
            const response = await authService.register(userData)
            return response
        } catch (err) {
            error.value = err.response?.data?.message || 'Đăng ký thất bại'
            throw err
        } finally {
            loading.value = false
        }
    }

    async function logout() {
        const { useChatStore } = await import('@/stores/chatStore')
        const { useDocumentStore } = await import('@/stores/documentStore')

        const chatStore = useChatStore()
        const documentStore = useDocumentStore()

        // 1️⃣ Xóa dữ liệu người dùng
        token.value = null
        user.value = null
        localStorage.removeItem('token')

        // 2️⃣ Reset các store khác
        chatStore.reset()
        documentStore.reset()

        // 3️⃣ (Tùy chọn) Chuyển hướng về trang login
        window.location.href = '/'
    }

    async function fetchCurrentUser() {
        if (!token.value) return
        try {
            const response = await authService.getCurrentUser()
            user.value = response.data
        } catch (err) {
            console.error('Failed to fetch user:', err)
            logout()
        }
    }

    async function forgotPassword(email) {
        try {
            loading.value = true
            error.value = null
            await authService.forgotPassword(email)
        } catch (err) {
            error.value = err.response?.data?.message || 'Gửi email thất bại'
            throw err
        } finally {
            loading.value = false
        }
    }

    async function resetPassword(token, newPassword) {
        try {
            loading.value = true
            error.value = null
            await authService.resetPassword(token, newPassword)
        } catch (err) {
            error.value = err.response?.data?.message || 'Reset mật khẩu thất bại'
            throw err
        } finally {
            loading.value = false
        }
    }

    return {
        user,
        token,
        loading,
        error,
        isAuthenticated,
        isAdmin,
        isPremium,
        isFree,
        login,
        register,
        logout,
        fetchCurrentUser,
        forgotPassword,
        resetPassword
    }
})