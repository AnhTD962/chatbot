import { ref, onMounted, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const WS_URL = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws'

export function useWebSocket() {
    const client = ref(null)
    const connected = ref(false)
    const messages = ref([])
    const error = ref(null)

    const connect = (token) => {
        return new Promise((resolve, reject) => {
            try {
                client.value = new Client({
                    webSocketFactory: () => new SockJS(WS_URL),
                    connectHeaders: {
                        Authorization: `Bearer ${token}`
                    },
                    debug: (str) => {
                        console.log('STOMP Debug:', str)
                    },
                    reconnectDelay: 5000,
                    heartbeatIncoming: 4000,
                    heartbeatOutgoing: 4000,
                    onConnect: () => {
                        connected.value = true
                        error.value = null
                        console.log('WebSocket connected')
                        resolve()
                    },
                    onStompError: (frame) => {
                        error.value = frame.headers['message']
                        console.error('STOMP error:', frame)
                        reject(frame)
                    },
                    onWebSocketError: (event) => {
                        error.value = 'WebSocket connection error'
                        console.error('WebSocket error:', event)
                        reject(event)
                    },
                    onDisconnect: () => {
                        connected.value = false
                        console.log('WebSocket disconnected')
                    }
                })

                client.value.activate()
            } catch (err) {
                error.value = err.message
                reject(err)
            }
        })
    }

    const disconnect = () => {
        if (client.value) {
            client.value.deactivate()
            connected.value = false
        }
    }

    const subscribe = (destination, callback) => {
        if (!client.value || !connected.value) {
            console.warn('WebSocket not connected')
            return null
        }

        return client.value.subscribe(destination, (message) => {
            try {
                const data = JSON.parse(message.body)
                callback(data)
            } catch (err) {
                console.error('Error parsing message:', err)
            }
        })
    }

    const sendMessage = (destination, body) => {
        if (!client.value || !connected.value) {
            console.warn('WebSocket not connected')
            return
        }

        client.value.publish({
            destination,
            body: JSON.stringify(body)
        })
    }

    const sendChatMessage = (message) => {
        sendMessage('/app/chat.send', { content: message })
    }

    const sendTypingIndicator = (isTyping) => {
        sendMessage('/app/chat.typing', { typing: isTyping })
    }

    const subscribeToMessages = (callback) => {
        return subscribe('/user/queue/messages', callback)
    }

    const subscribeToTyping = (callback) => {
        return subscribe('/user/queue/typing', callback)
    }

    onUnmounted(() => {
        disconnect()
    })

    return {
        client,
        connected,
        messages,
        error,
        connect,
        disconnect,
        subscribe,
        sendMessage,
        sendChatMessage,
        sendTypingIndicator,
        subscribeToMessages,
        subscribeToTyping
    }
}