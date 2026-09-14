import api from './axios'

export function getEmployees(search = '', page = 0, size = 10) {
  return api.get('/employees', { params: { search, page, size } })
}

export function createEmployee(data) {
  return api.post('/employees', data)
}

export function updateEmployee(id, data) {
  return api.put(`/employees/${id}`, data)
}

export function deleteEmployee(id) {
  return api.delete(`/employees/${id}`)
}

export function uploadPhoto(id, file) {
  const formData = new FormData()
  formData.append('file', file)
  return api.post(`/employees/${id}/photo`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function uploadCv(id, file) {
  const formData = new FormData()
  formData.append('file', file)
  return api.post(`/employees/${id}/cv`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function getPhotoBlob(id) {
  return api.get(`/employees/${id}/photo`, { responseType: 'blob' })
}

export function getCvBlob(id) {
  return api.get(`/employees/${id}/cv`, { responseType: 'blob' })
}

export function getContractBlob(id) {
  return api.get(`/employees/${id}/contract`, { responseType: 'blob' })
}

// Helper: triggers a browser download for any blob response
export function downloadBlob(blob, filename) {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  window.URL.revokeObjectURL(url)
}