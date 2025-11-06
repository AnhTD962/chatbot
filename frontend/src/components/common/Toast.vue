<template>
  <teleport to="body">
    <div class="fixed top-4 right-4 z-50 space-y-3">
      <transition-group name="toast">
        <div
            v-for="toast in toasts"
            :key="toast.id"
            :class="[
            'flex items-center p-4 rounded-lg shadow-lg min-w-[300px] max-w-md',
            toast.type === 'success' && 'bg-green-50 border border-green-200',
            toast.type === 'error' && 'bg-red-50 border border-red-200',
            toast.type === 'warning' && 'bg-yellow-50 border border-yellow-200',
            toast.type === 'info' && 'bg-blue-50 border border-blue-200'
          ]"
        >
          <!-- Icon -->
          <div class="flex-shrink-0">
            <svg
                v-if="toast.type === 'success'"
                class="h-5 w-5 text-green-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <svg
                v-else-if="toast.type === 'error'"
                class="h-5 w-5 text-red-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <svg
                v-else-if="toast.type === 'warning'"
                class="h-5 w-5 text-yellow-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            <svg
                v-else
                class="h-5 w-5 text-blue-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>

          <!-- Content -->
          <div class="ml-3 flex-1">
            <p
                :class="[
                'text-sm font-semibold',
                toast.type === 'success' && 'text-green-900',
                toast.type === 'error' && 'text-red-900',
                toast.type === 'warning' && 'text-yellow-900',
                toast.type === 'info' && 'text-blue-900'
              ]"
            >
              {{ toast.title }}
            </p>
            <p
                v-if="toast.message"
                :class="[
                'text-sm mt-1',
                toast.type === 'success' && 'text-green-700',
                toast.type === 'error' && 'text-red-700',
                toast.type === 'warning' && 'text-yellow-700',
                toast.type === 'info' && 'text-blue-700'
              ]"
            >
              {{ toast.message }}
            </p>
          </div>

          <!-- Close Button -->
          <button
              @click="removeToast(toast.id)"
              class="ml-3 flex-shrink-0 focus:outline-none"
          >
            <svg class="h-4 w-4 text-gray-400 hover:text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </transition-group>
    </div>
  </teleport>
</template>

<script setup>
import { ref } from 'vue'
import { useToast } from '@/composables/useToast'

const { toasts, removeToast } = useToast()
</script>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(100%);
}
</style>