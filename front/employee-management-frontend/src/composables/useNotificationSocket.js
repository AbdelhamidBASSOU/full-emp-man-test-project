import { ref, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useAuthStore } from '../stores/auth'

const RECONNECT_DELAY_MS = 5000
const MAX_RECONNECT_DELAY_MS = 60000

export function useNotificationSocket() {
  const lastNotification = ref(null)
  const connected = ref(false)

  let stompClient = null
  let reconnectDelay = RECONNECT_DELAY_MS
  let reconnectTimer = null
  let intentionalDisconnect = false

  function buildClient() {
    const authStore = useAuthStore()

    return new Client({
      webSocketFactory: () => new SockJS('/ws'),
      connectHeaders: {
        Authorization: `Bearer ${authStore.token}`,
      },
      reconnectDelay: 0,

      onConnect: () => {
        connected.value = true
        reconnectDelay = RECONNECT_DELAY_MS

        stompClient.subscribe('/topic/notifications', (frame) => {
          try {
            lastNotification.value = JSON.parse(frame.body)
          } catch (err) {
            console.warn('[WS] Failed to parse notification frame:', err)
          }
        })
      },

      onDisconnect: () => {
        connected.value = false
      },

      onStompError: (frame) => {
        console.warn('[WS] STOMP error:', frame.headers?.message)
        connected.value = false
      },

      onWebSocketClose: () => {
        connected.value = false
        if (!intentionalDisconnect) {
          scheduleReconnect()
        }
      },
    })
  }

  function scheduleReconnect() {
    if (reconnectTimer) return

    const delay = reconnectDelay
    reconnectDelay = Math.min(reconnectDelay * 2, MAX_RECONNECT_DELAY_MS)

    reconnectTimer = setTimeout(() => {
      reconnectTimer = null
      if (!intentionalDisconnect) {
        connect()
      }
    }, delay)
  }

  function connect() {
    intentionalDisconnect = false

    if (stompClient) {
      try { stompClient.deactivate() } catch { /* ignore */ }
      stompClient = null
    }

    stompClient = buildClient()
    stompClient.activate()
  }

  function disconnect() {
    intentionalDisconnect = true

    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }

    if (stompClient) {
      stompClient.deactivate()
      stompClient = null
    }

    connected.value = false
  }

  onUnmounted(disconnect)

  return {
    lastNotification,
    connected,
    connect,
    disconnect,
  }
}
