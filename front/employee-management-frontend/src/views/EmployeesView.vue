<script setup>
import { ref, onMounted, watch } from 'vue'
import AppLayout from '../components/AppLayout.vue'
import EmployeeFormModal from '../components/EmployeeFormModal.vue'
import { getEmployees, deleteEmployee } from '../api/employees'
import { useToastStore } from '../stores/toast'

const toast = useToastStore()

const employees = ref([])
const search = ref('')
const page = ref(0)
const size = ref(10)
const totalPages = ref(0)
const totalElements = ref(0)
const loading = ref(true)
const error = ref('')

const showModal = ref(false)
const editingEmployee = ref(null)

async function loadEmployees() {
  loading.value = true
  error.value = ''
  try {
    const response = await getEmployees(search.value, page.value, size.value)
    employees.value = response.data.content
    totalPages.value = response.data.totalPages
    totalElements.value = response.data.totalElements
  } catch (err) {
    error.value = 'Failed to load employees. Please try again.'
  } finally {
    loading.value = false
  }
}

function nextPage() {
  if (page.value < totalPages.value - 1) page.value++
}
function prevPage() {
  if (page.value > 0) page.value--
}

function openCreateModal() {
  editingEmployee.value = null
  showModal.value = true
}
function openEditModal(emp) {
  editingEmployee.value = emp
  showModal.value = true
}
function closeModal() {
  showModal.value = false
}
function onSaved() {
  const wasEditing = !!editingEmployee.value
  showModal.value = false
  loadEmployees()
  toast.success(wasEditing ? 'Employee updated successfully' : 'Employee created successfully')
}

async function handleDelete(emp) {
  if (!confirm(`Delete ${emp.firstName} ${emp.lastName}? This cannot be undone.`)) return
  try {
    await deleteEmployee(emp.id)
    loadEmployees()
    toast.success('Employee deleted successfully')
  } catch (err) {
    toast.error(err.response?.data?.message || 'Failed to delete employee.')
  }
}

watch(search, () => { page.value = 0 })
watch([search, page], () => { loadEmployees() })
onMounted(() => { loadEmployees() })
</script>

<template>
  <AppLayout>
    <div class="page">
      <div class="page-header">
        <h1>Employees</h1>
        <button class="btn btn-primary" @click="openCreateModal">+ Add Employee</button>
      </div>

      <div class="toolbar">
        <input v-model="search" type="text" placeholder="Search by name or email..." class="input search-input" />
      </div>

      <div class="card table-card">
        <table v-if="!loading && !error && employees.length > 0">
          <thead>
            <tr>
              <th>Photo</th>
              <th>Full Name</th>
              <th>Email</th>
              <th>Job Title</th>
              <th>Department</th>
              <th>Hire Date</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="emp in employees" :key="emp.id">
              <td>
                <div class="avatar">{{ emp.firstName?.[0] }}{{ emp.lastName?.[0] }}</div>
              </td>
              <td>{{ emp.firstName }} {{ emp.lastName }}</td>
              <td>{{ emp.email }}</td>
              <td>{{ emp.jobTitle }}</td>
              <td>{{ emp.department || '—' }}</td>
              <td>{{ emp.hireDate }}</td>
              <td>
                <div class="row-actions">
                  <button class="btn btn-secondary btn-sm" @click="openEditModal(emp)">Edit</button>
                  <button class="btn btn-danger btn-sm" @click="handleDelete(emp)">Delete</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="loading" class="state-message">Loading employees...</div>
        <div v-else-if="error" class="state-message error-message">{{ error }}</div>
        <div v-else-if="employees.length === 0" class="state-message">No employees found.</div>
      </div>

      <div class="pagination" v-if="!loading && !error && totalElements > 0">
        <span class="pagination-info">Page {{ page + 1 }} of {{ totalPages }} ({{ totalElements }} total)</span>
        <div class="pagination-buttons">
          <button class="btn btn-secondary btn-sm" :disabled="page === 0" @click="prevPage">Previous</button>
          <button class="btn btn-secondary btn-sm" :disabled="page >= totalPages - 1" @click="nextPage">Next</button>
        </div>
      </div>
    </div>

    <EmployeeFormModal v-if="showModal" :employee="editingEmployee" @close="closeModal" @saved="onSaved" />
  </AppLayout>
</template>

<style scoped>
.page { padding: var(--space-xl); max-width: 1100px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-lg); }
.page-header h1 { font-size: 22px; }
.toolbar { margin-bottom: var(--space-md); }
.search-input { max-width: 320px; }
.table-card { overflow: hidden; }
table { width: 100%; border-collapse: collapse; }
thead { background: var(--color-bg); }
th { text-align: left; padding: 12px var(--space-md); font-size: 12px; font-weight: 600; color: var(--color-text-muted); border-bottom: 1px solid var(--color-border); }
td { padding: 12px var(--space-md); border-bottom: 1px solid var(--color-border); font-size: 14px; }
tbody tr:last-child td { border-bottom: none; }
tbody tr:hover { background: var(--color-bg); }
.avatar { width: 32px; height: 32px; border-radius: 50%; background: var(--color-primary); color: white; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 600; }
.row-actions { display: flex; gap: var(--space-sm); }
.btn-sm { padding: 5px 12px; font-size: 13px; }
.state-message { padding: var(--space-xl); text-align: center; color: var(--color-text-muted); }
.error-message { color: var(--color-danger); }
.pagination { display: flex; justify-content: space-between; align-items: center; margin-top: var(--space-md); }
.pagination-info { font-size: 13px; color: var(--color-text-muted); }
.pagination-buttons { display: flex; gap: var(--space-sm); }
</style>