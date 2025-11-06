<template>
  <div class="flex flex-col h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
    <!-- Header -->
    <Navbar :remaining-questions="remainingQuestions" />

    <!-- Tabs -->
    <div class="bg-white border-b border-gray-200 px-4 py-2">
      <div class="max-w-4xl mx-auto flex items-center justify-between">
        <div class="flex space-x-4">
          <button
              @click="activeTab = 'chat'"
              :class="activeTab === 'chat' ? 'font-semibold text-blue-600 border-b-2 border-blue-600' : 'text-gray-500'"
          >
            💬 Chat
          </button>
          <button
              v-if="isAuthenticated"
              @click="activeTab = 'documents'"
              :class="activeTab === 'documents' ? 'font-semibold text-blue-600 border-b-2 border-blue-600' : 'text-gray-500'"
          >
            📄 Tài liệu
          </button>
        </div>

        <div v-if="isAuthenticated" class="flex items-center space-x-2">
          <input type="file" id="upload" @change="handleFileUpload" accept=".pdf,.docx,.txt" class="hidden" />
          <label for="upload"
                 class="cursor-pointer px-3 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 text-sm">
            + Upload
          </label>
        </div>
      </div>
    </div>

    <!-- Chat Section -->
    <div v-if="activeTab === 'chat'" ref="messagesContainer" class="flex-1 overflow-y-auto p-4">
      <div class="max-w-4xl mx-auto space-y-4">
        <!-- Empty State -->
        <div v-if="messages.length === 0" class="text-center py-12">
          <div class="inline-block p-4 bg-white rounded-full shadow-lg mb-4">
            <svg class="h-16 w-16 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
            </svg>
          </div>
          <h2 class="text-2xl font-bold text-gray-800 mb-2">Xin chào! 👋</h2>
          <p class="text-gray-600 mb-6">Tôi có thể giúp gì cho bạn hôm nay?</p>
        </div>

        <!-- Messages -->
        <div
            v-for="(message, index) in messages"
            :key="index"
            :class="message.role === 'user' ? 'flex justify-end' : 'flex justify-start'"
        >
          <div
              :class="[ 'max-w-3xl px-4 py-3 rounded-lg',
              message.role === 'user'
                ? 'bg-blue-600 text-white'
                : 'bg-white text-gray-800 shadow' ]"
          >
            <p class="text-sm whitespace-pre-wrap">{{ message.content }}</p>
            <p :class="['text-xs mt-1', message.role === 'user' ? 'text-blue-100' : 'text-gray-400']">
              {{ formatTime(message.timestamp) }}
            </p>
          </div>
        </div>

        <!-- Typing Indicator -->
        <div v-if="isTyping" class="flex justify-start">
          <div class="bg-white px-4 py-3 rounded-lg shadow">
            <div class="flex space-x-2">
              <div class="h-2 w-2 bg-gray-400 rounded-full animate-bounce"></div>
              <div class="h-2 w-2 bg-gray-400 rounded-full animate-bounce delay-150"></div>
              <div class="h-2 w-2 bg-gray-400 rounded-full animate-bounce delay-300"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Documents Section -->
    <div v-else-if="activeTab === 'documents'" class="flex-1 overflow-y-auto p-4 bg-white">
      <div class="max-w-4xl mx-auto">
        <h2 class="text-lg font-semibold mb-3">📁 Danh sách tài liệu</h2>

        <ul v-if="uploadedDocuments.length > 0" class="space-y-2">
          <li
              v-for="doc in uploadedDocuments"
              :key="doc.id"
              @click="selectedDocumentId = doc.id"
              class="p-3 border rounded-lg hover:bg-blue-50 cursor-pointer flex justify-between items-center"
              :class="selectedDocumentId === doc.id ? 'border-blue-500 bg-blue-50' : 'border-gray-200'"
          >
            <span>{{ doc.fileName }}</span>
            <span v-if="selectedDocumentId === doc.id" class="text-blue-600 text-sm font-medium">
              Đang chọn
            </span>
          </li>
        </ul>

        <p v-else class="text-gray-500 text-sm mt-4">Chưa có tài liệu nào được tải lên.</p>

        <div v-if="uploadProgress > 0 && uploadProgress < 100" class="mt-4 text-sm text-gray-500">
          Đang tải lên... {{ uploadProgress }}%
        </div>
      </div>
      <div v-if="fileSummary" class="mt-6 p-4 bg-blue-50 rounded-lg shadow-sm">
        <h3 class="text-lg font-semibold text-blue-700 mb-2">🧾 Tóm tắt tài liệu mới tải lên:</h3>
        <p class="text-gray-700 whitespace-pre-line">{{ fileSummary }}</p>
      </div>
    </div>

    <!-- Input -->
    <div class="bg-white border-t border-gray-200 p-4">
      <div class="max-w-4xl mx-auto">
        <div class="flex items-end space-x-3">
          <div class="flex-1">
            <textarea
                v-model="inputMessage"
                ref="textareaRef"
                @input="autoResize"
                @keydown.enter.exact.prevent="sendMessage"
                placeholder="Nhập câu hỏi của bạn..."
                rows="1"
                class="w-full px-4 py-3 border border-gray-300 rounded-lg
                       focus:outline-none focus:ring-2 focus:ring-blue-500
                       resize-none overflow-y-auto transition-all duration-200 ease-in-out max-h-48"
                :disabled="isTyping"
            ></textarea>
          </div>
          <button
              @click="sendMessage"
              :disabled="!inputMessage.trim() || isTyping"
              class="px-6 py-3 bg-blue-600 text-white rounded-lg
                     hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed
                     flex items-center space-x-2 transition"
          >
            <span>Gửi</span>
            <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"/>
            </svg>
          </button>
        </div>
        <p class="text-xs text-gray-500 mt-2">
          Nhấn Enter để gửi, Shift + Enter để xuống dòng
        </p>
        <div v-if="selectedDocumentId" class="text-xs text-blue-600 mt-1">
          🧠 Đang hỏi về tài liệu:
          <span class="font-medium">
            {{ uploadedDocuments.find(d => d.id === selectedDocumentId)?.fileName }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import Navbar from '@/components/common/Navbar.vue'
import chatService from '@/services/chatService'
import documentService from '@/services/documentService'

const authStore = useAuthStore()

// ----- States -----
const activeTab = ref('chat')
const messages = ref([])
const inputMessage = ref('')
const isTyping = ref(false)
const messagesContainer = ref(null)
const textareaRef = ref(null)
const uploadedDocuments = ref([])
const selectedDocumentId = ref(null)
const uploadProgress = ref(0)
const remainingQuestions = ref(5)
const isAuthenticated = computed(() => authStore.isAuthenticated)
const fileSummary = ref(null)

// ----- Functions -----
const loadDocuments = async () => {
  try {
    uploadedDocuments.value = await documentService.getDocuments()
  } catch (err) {
    console.error('Không thể tải danh sách tài liệu:', err)
  }
}

const handleFileUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  uploadProgress.value = 0
  fileSummary.value = null

  try {
    const response = await documentService.uploadDocument(file, (percent) => {
      uploadProgress.value = percent
    })

    if (response.summary) {
      fileSummary.value = response.summary
    }

    await loadDocuments()
  } catch (err) {
    console.error('Lỗi khi upload:', err)
  } finally {
    uploadProgress.value = 100
  }
}


const autoResize = async () => {
  await nextTick()
  const textarea = textareaRef.value
  if (textarea) {
    textarea.style.height = 'auto'
    const maxHeight = 200
    textarea.style.height = Math.min(textarea.scrollHeight, maxHeight) + 'px'
    textarea.style.overflowY = textarea.scrollHeight > maxHeight ? 'auto' : 'hidden'
  }
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim()) return
  const question = inputMessage.value
  inputMessage.value = ''
  isTyping.value = true

  messages.value.push({ role: 'user', content: question, timestamp: new Date() })
  scrollToBottom()

  try {
    let response
    if (selectedDocumentId.value) {
      response = await documentService.askQuestion(selectedDocumentId.value, question)
    } else if (isAuthenticated.value) {
      response = await chatService.sendMessage(question)
    } else {
      response = await chatService.sendGuestMessage(question)
      remainingQuestions.value = response.remainingQuestions || 0
    }

    messages.value.push({
      role: 'assistant',
      content: response.answer || response.response || response.message,
      timestamp: new Date()
    })
  } catch (error) {
    messages.value.push({
      role: 'assistant',
      content: 'Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.',
      timestamp: new Date()
    })
  } finally {
    isTyping.value = false
    scrollToBottom()
  }
}

const formatTime = (date) => {
  return new Date(date).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
}

// ----- Lifecycle -----
onMounted(() => {
  if (isAuthenticated.value) authStore.fetchCurrentUser()
  loadDocuments()
})
</script>
