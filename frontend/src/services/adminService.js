import api from './api'

const adminService = {
    // Dashboard
    async getDashboardStats() {
        const response = await api.get('/admin/dashboard/stats')
        return response.data
    },

    async getRealtimeStats() {
        const response = await api.get('/admin/dashboard/realtime')
        return response.data
    },

    // Users Management
    async getUsers(params) {
        const response = await api.get('/admin/users', { params })
        return response.data
    },

    async updateUserStatus(userId, status) {
        const response = await api.put(`/admin/users/${userId}/status`, { status })
        return response.data
    },

    async deleteUser(userId) {
        const response = await api.delete(`/admin/users/${userId}`)
        return response.data
    },

    // Knowledge Base
    async getKnowledge(params) {
        const response = await api.get('/admin/knowledge', { params })
        return response.data
    },

    async createKnowledge(data) {
        const response = await api.post('/admin/knowledge', data)
        return response.data
    },

    async updateKnowledge(id, data) {
        const response = await api.put(`/admin/knowledge/${id}`, data)
        return response.data
    },

    async deleteKnowledge(id) {
        const response = await api.delete(`/admin/knowledge/${id}`)
        return response.data
    },

    // Revenue
    async getRevenueDashboard() {
        const response = await api.get('/admin/revenue/dashboard')
        return response.data
    },

    async getRevenueReport(startDate, endDate) {
        const response = await api.get('/admin/revenue/report', {
            params: { startDate, endDate }
        })
        return response.data
    },

    async getTransactions(params) {
        const response = await api.get('/admin/revenue/transactions', { params })
        return response.data
    },

    async refundTransaction(transactionId, reason) {
        const response = await api.post(`/admin/revenue/refund/${transactionId}`, { reason })
        return response.data
    },

    // Email Logs
    async getEmailLogs(params) {
        const response = await api.get('/admin/emails/logs', { params })
        return response.data
    },

    async getEmailStats() {
        const response = await api.get('/admin/emails/stats')
        return response.data
    },

    // System Health
    async getSystemHealth() {
        const response = await api.get('/admin/system/health')
        return response.data
    }
}

export default adminService