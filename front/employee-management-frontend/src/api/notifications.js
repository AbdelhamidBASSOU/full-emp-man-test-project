import api from './axios'

export function getNotifications() {
  return api.get('/notifications')
}

export function getUnreadCount() {
  return api.get('/notifications/unread-count')
}

export function markAllAsRead() {
  return api.post('/notifications/mark-read')
}