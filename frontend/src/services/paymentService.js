import api from './api'

const paymentService = {
    async getPlans() {
        const response = await api.get('/payment/plans')
        return response.data
    },

    async createPayment(planId) {
        const response = await api.post('/payment/create', { planId })
        return response.data
    },

    async getTransaction(transactionId) {
        const response = await api.get(`/payment/transaction/${transactionId}`)
        return response.data
    },

    async getMyTransactions() {
        const response = await api.get('/payment/my-transactions')
        return response.data
    },

    async handleCallback(queryParams) {
        const response = await api.get('/payment/callback', { params: queryParams })
        return response.data
    }
}

export default paymentService