import api from './axios'

export function getUsers(search = '', page = 0, size = 10) {
  return api.get('/users', { params: { search, page, size } })
}

export function createUser(data) {
  return api.post('/users', data)
}

export function updateUser(id, data) {
  return api.put(`/users/${id}`, data)
}

export function deleteUser(id) {
  return api.delete(`/users/${id}`)
}

export function updatePermissions(id, permissions) {
  return api.patch(`/users/${id}/permissions`, null, { params: permissions })
}