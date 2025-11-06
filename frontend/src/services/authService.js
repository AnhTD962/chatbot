import api from './api'

const authService = {
    async login(credentials) {
        const response = await api.post('/auth/login', credentials)
        return response.data
    },

    async register(userData) {
        const response = await api.post('/auth/register', userData)
        return response.data
    },

    async getCurrentUser() {
        return await api.get('/auth/me')
    },

    async forgotPassword(email) {
        const response = await api.post('/auth/forgot-password', { email })
        return response.data
    },

    async resetPassword(token, newPassword) {
        const response = await api.post('/auth/reset-password', { token, newPassword })
        return response.data
    },

    async verifyResetToken(token) {
        const response = await api.get(`/auth/verify-reset-token?token=${token}`)
        return response.data
    },

    async upgradePremium() {
        const response = await api.post('/auth/upgrade-premium')
        return response.data
    }
}

export default authService