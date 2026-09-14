import { defineStore } from 'pinia'

let nextId = 1

export const useToastStore = defineStore('toast', {
  state: () => ({
    toasts: [],
  }),

  actions: {
    show(message, type = 'success') {
      const id = nextId++
      this.toasts.push({ id, message, type })
      setTimeout(() => this.remove(id), 3500)
    },
    success(message) {
      this.show(message, 'success')
    },
    error(message) {
      this.show(message, 'error')
    },
    remove(id) {
      this.toasts = this.toasts.filter((t) => t.id !== id)
    },
  },
})