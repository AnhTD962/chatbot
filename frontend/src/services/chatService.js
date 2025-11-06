import api from './api'

const chatService = {
    async sendGuestMessage(message) {
        const response = await api.post('/chat/guest', { message })
        return response.data
    },

    async sendMessage(message) {
        const response = await api.post('/chat', { message })
        return response.data
    },

    async getChatSessions(userId) {
        const response = await api.get(`/chat/sessions/${userId}`)
        return response.data
    },

    async getChatHistory(sessionId) {
        const response = await api.get(`/chat/sessions/${sessionId}/messages`)
        return response.data
    }
}

export default chatService