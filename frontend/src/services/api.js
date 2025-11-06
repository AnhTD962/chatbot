import axios from 'axios'

const isLocal = window.location.origin.includes('localhost')

// Nếu đang chạy local (dev), dùng proxy /api
// Nếu build production, dùng biến môi trường (VD: VITE_API_BASE_URL)
const API_BASE_URL = isLocal
    ? '/api'
    : import.meta.env.VITE_API_BASE_URL || '/api'

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json'
    }
})

// Giữ nguyên interceptors
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => Promise.reject(error)
)

api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('token')
            window.location.href = '/login'
        }
        return Promise.reject(error)
    }
)

export default api
