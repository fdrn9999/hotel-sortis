import { ref, onMounted, onUnmounted } from 'vue'
import { Client, type IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

export function useWebSocket() {
  const stompClient = ref<Client | null>(null)
  const connected = ref(false)

  const connect = () => {
    const socket = new SockJS('/ws')
    stompClient.value = new Client({
      webSocketFactory: () => socket as WebSocket,
      onConnect: () => {
        connected.value = true
        console.log('WebSocket connected')
      },
      onDisconnect: () => {
        connected.value = false
        console.log('WebSocket disconnected')
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame.headers['message'])
      }
    })

    stompClient.value.activate()
  }

  const disconnect = () => {
    if (stompClient.value) {
      stompClient.value.deactivate()
      stompClient.value = null
      connected.value = false
    }
  }

  const subscribe = (destination: string, callback: (message: IMessage) => void) => {
    if (stompClient.value && connected.value) {
      return stompClient.value.subscribe(destination, callback)
    }
    return null
  }

  const send = (destination: string, body: object) => {
    if (stompClient.value && connected.value) {
      stompClient.value.publish({
        destination,
        body: JSON.stringify(body)
      })
    }
  }

  onMounted(() => {
    connect()
  })

  onUnmounted(() => {
    disconnect()
  })

  return {
    connected,
    connect,
    disconnect,
    subscribe,
    send
  }
}
