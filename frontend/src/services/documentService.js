import api from './api'

const documentService = {
    async uploadDocument(file, onProgress) {
        const formData = new FormData()
        formData.append('file', file)

        const response = await api.post('/documents/upload', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            },
            onUploadProgress: (progressEvent) => {
                if (onProgress) {
                    const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
                    onProgress(percentCompleted)
                }
            }
        })

        return response.data
    },

    async getDocuments() {
        const response = await api.get('/documents')
        return response.data
    },

    async getDocument(documentId) {
        const response = await api.get(`/documents/${documentId}`)
        return response.data
    },

    async deleteDocument(documentId) {
        const response = await api.delete(`/documents/${documentId}`)
        return response.data
    },

    async askQuestion(documentId, question) {
        const response = await api.post(`/documents/${documentId}/ask`, { question })
        return response.data
    }
}

export default documentService