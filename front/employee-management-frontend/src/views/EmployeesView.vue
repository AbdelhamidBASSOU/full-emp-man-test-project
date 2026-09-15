<script setup>
import { ref, onMounted, watch } from 'vue'
import AppLayout from '../components/AppLayout.vue'
import EmployeeFormModal from '../components/EmployeeFormModal.vue'
import { getEmployees, deleteEmployee } from '../api/employees'
import { useToastStore } from '../stores/toast'
import { debounce } from '../utils/debounce'
import { useAuthStore } from '../stores/auth'

const toast = useToastStore()
const authStore = useAuthStore()

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

function formatDate(dateStr) {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString('en-GB', {
    day: '2-digit', month: 'short', year: 'numeric',
  })
}

// Debounce: reset page and load 300ms after the user stops typing
const debouncedSearch = debounce(() => {
  page.value = 0
  loadEmployees()
}, 300)

watch(search, debouncedSearch)
watch(page, loadEmployees)
onMounted(loadEmployees)
</script>

<template>
  <AppLayout>
    <div class="page">
      <div class="page-header">
        <div class="page-header-left">
          <h1>Employees</h1>
          <span v-if="!loading && totalElements > 0" class="record-count">
            {{ totalElements }} total
          </span>
        </div>
        <button
          v-if="authStore.isSuperAdmin || authStore.canCreate"
          class="btn btn-primary"
          @click="openCreateModal"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          Add Employee
        </button>
      </div>

      <div class="toolbar">
        <div class="search-wrapper">
          <svg class="search-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input
            v-model="search"
            type="text"
            placeholder="Search by name or email…"
            class="input search-input"
          />
        </div>
      </div>

      <div class="card table-card">
        <div class="table-scroll">
          <!-- Loading skeleton rows -->
          <table v-if="loading">
            <thead>
              <tr>
                <th style="width:48px"></th>
                <th>Full Name</th>
                <th>Email</th>
                <th>Job Title</th>
                <th>Department</th>
                <th>Hire Date</th>
                <th style="width:160px">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="i in size" :key="i" class="skeleton-row">
                <td><div class="skeleton skel-avatar"></div></td>
                <td><div class="skeleton skel-md"></div></td>
                <td><div class="skeleton skel-lg"></div></td>
                <td><div class="skeleton skel-md"></div></td>
                <td><div class="skeleton skel-sm"></div></td>
                <td><div class="skeleton skel-sm"></div></td>
                <td><div class="skeleton skel-actions"></div></td>
              </tr>
            </tbody>
          </table>

          <!-- Data table -->
          <table v-else-if="!error && employees.length > 0">
            <thead>
              <tr>
                <th style="width:48px"></th>
                <th>Full Name</th>
                <th>Email</th>
                <th>Job Title</th>
                <th>Department</th>
                <th>Hire Date</th>
                <th style="width:160px">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="emp in employees" :key="emp.id">
                <td>
                  <div class="avatar">{{ emp.firstName?.[0] }}{{ emp.lastName?.[0] }}</div>
                </td>
                <td class="col-name">
                  <span class="cell-primary">{{ emp.firstName }} {{ emp.lastName }}</span>
                </td>
                <td class="col-email">
                  <span class="cell-truncate">{{ emp.email }}</span>
                </td>
                <td>
                  <span class="cell-primary">{{ emp.jobTitle || '—' }}</span>
                </td>
                <td>
                  <span class="badge badge-neutral">{{ emp.department || '—' }}</span>
                </td>
                <td class="mono text-sm">{{ formatDate(emp.hireDate) }}</td>
                <td>
                  <div class="row-actions">
                    <button
                      class="btn btn-secondary btn-sm action-btn"
                      @click="openEditModal(emp)"
                      title="Edit employee details"
                    >
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                      </svg>
                      Edit
                    </button>
                    <button
                      class="btn btn-danger btn-sm action-btn"
                      @click="handleDelete(emp)"
                      title="Delete employee record"
                    >
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <polyline points="3 6 5 6 21 6"/>
                        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                      </svg>
                      Delete
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Empty state -->
        <div v-if="!loading && !error && employees.length === 0" class="state-box">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="state-icon">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <line x1="23" y1="11" x2="17" y2="11"/>
          </svg>
          <p class="state-title">{{ search ? 'No employees match your search' : 'No employees yet' }}</p>
          <p class="state-subtitle" v-if="!search">Add your first employee to get started.</p>
        </div>

        <!-- Error state -->
        <div v-if="!loading && error" class="state-box state-error">
          <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="state-icon">
            <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <p class="state-title">{{ error }}</p>
          <button class="btn btn-secondary btn-sm" @click="loadEmployees">Try again</button>
        </div>
      </div>

      <!-- Pagination -->
      <div class="pagination" v-if="!loading && !error && totalElements > 0">
        <span class="pagination-info">
          Page {{ page + 1 }} of {{ totalPages || 1 }} ({{ totalElements }} total)
        </span>
        <div class="pagination-buttons">
          <button class="btn btn-secondary btn-sm" :disabled="page === 0" @click="prevPage">← Previous</button>
          <button class="btn btn-secondary btn-sm" :disabled="page >= totalPages - 1" @click="nextPage">Next →</button>
        </div>
      </div>
    </div>

    <EmployeeFormModal v-if="showModal" :employee="editingEmployee" @close="closeModal" @saved="onSaved" />
  </AppLayout>
</template>

<style scoped>
/* ── Page shell ──────────────────────────────────────────── */
.page {
  padding: var(--space-xl);
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}
@media (max-width: 768px) {
  .page { padding: var(--space-md); }
}

/* ── Header ──────────────────────────────────────────────── */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-lg);
  gap: var(--space-md);
  flex-wrap: wrap;
}
.page-header-left {
  display: flex;
  align-items: baseline;
  gap: var(--space-sm);
}
.page-header h1 { font-size: 22px; }
.record-count {
  font-size: 13px;
  color: var(--color-text-muted);
  font-weight: 500;
}

/* ── Toolbar ─────────────────────────────────────────────── */
.toolbar { margin-bottom: var(--space-md); }
.search-wrapper {
  position: relative;
  max-width: 340px;
}
.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--color-text-muted);
  pointer-events: none;
}
.search-input {
  padding-left: 36px;
}

/* ── Table card ──────────────────────────────────────────── */
.table-card { overflow: hidden; }
.table-scroll { overflow-x: auto; }

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 700px;
}
thead { background: var(--color-bg); }
th {
  text-align: left;
  padding: 11px var(--space-md);
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-muted);
  border-bottom: 1px solid var(--color-border);
  white-space: nowrap;
  letter-spacing: 0.02em;
  text-transform: uppercase;
}
td {
  padding: 13px var(--space-md);
  border-bottom: 1px solid var(--color-border);
  font-size: 14px;
  vertical-align: middle;
}
tbody tr:last-child td { border-bottom: none; }
tbody tr:hover { background: var(--color-bg); }

.col-email { max-width: 200px; }
.col-name  { font-weight: 500; }
.cell-primary { font-weight: 500; }
.cell-truncate {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
  color: var(--color-text-muted);
  font-size: 13px;
}
.text-sm { font-size: 12px; color: var(--color-text-muted); }

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-primary-soft);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
}
.row-actions {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
}
.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  padding: 5px 10px;
}

/* ── Skeleton rows ───────────────────────────────────────── */
.skeleton-row td { padding: 14px var(--space-md); }
.skel-avatar { width: 32px; height: 32px; border-radius: 50%; }
.skel-sm     { height: 14px; width: 80px;  }
.skel-md     { height: 14px; width: 130px; }
.skel-lg     { height: 14px; width: 180px; }
.skel-actions{ height: 28px; width: 120px; border-radius: var(--radius); }

/* ── Empty / Error states ────────────────────────────────── */
.state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-2xl) var(--space-xl);
  gap: var(--space-sm);
  text-align: center;
}
.state-icon { color: var(--color-text-muted); margin-bottom: var(--space-xs); }
.state-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0;
}
.state-subtitle { font-size: 13px; color: var(--color-text-muted); margin: 0; }
.state-error .state-icon { color: var(--color-danger); }
.state-error .state-title { color: var(--color-danger); }

/* ── Pagination ──────────────────────────────────────────── */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: var(--space-md);
  gap: var(--space-md);
  flex-wrap: wrap;
}
.pagination-info {
  font-size: 13px;
  color: var(--color-text-muted);
}
.pagination-buttons { display: flex; gap: var(--space-sm); }
</style>