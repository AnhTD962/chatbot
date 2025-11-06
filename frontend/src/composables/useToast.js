import { ref } from 'vue'

const toasts = ref([])
let toastId = 0

export function useToast() {
    const addToast = (type, title, message = '', duration = 3000) => {
        const id = toastId++

        toasts.value.push({
            id,
            type,
            title,
            message
        })

        if (duration > 0) {
            setTimeout(() => {
                removeToast(id)
            }, duration)
        }

        return id
    }

    const removeToast = (id) => {
        const index = toasts.value.findIndex(t => t.id === id)
        if (index > -1) {
            toasts.value.splice(index, 1)
        }
    }

    const success = (title, message = '', duration = 3000) => {
        return addToast('success', title, message, duration)
    }

    const error = (title, message = '', duration = 5000) => {
        return addToast('error', title, message, duration)
    }

    const warning = (title, message = '', duration = 4000) => {
        return addToast('warning', title, message, duration)
    }

    const info = (title, message = '', duration = 3000) => {
        return addToast('info', title, message, duration)
    }

    return {
        toasts,
        addToast,
        removeToast,
        success,
        error,
        warning,
        info
    }
}