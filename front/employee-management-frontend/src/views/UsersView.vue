<script setup>
import { ref, onMounted, watch } from 'vue'
import AppLayout from '../components/AppLayout.vue'
import UserFormModal from '../components/UserFormModal.vue'
import PermissionsModal from '../components/PermissionsModal.vue'
import { getUsers, deleteUser } from '../api/users'
import { useToastStore } from '../stores/toast'

const toast = useToastStore()

const users = ref([])
const search = ref('')
const page = ref(0)
const size = ref(10)
const totalPages = ref(0)
const totalElements = ref(0)
const loading = ref(true)
const error = ref('')

const showFormModal = ref(false)
const editingUser = ref(null)

const showPermissionsModal = ref(false)
const permissionsUser = ref(null)

async function loadUsers() {
  loading.value = true
  error.value = ''
  try {
    const response = await getUsers(search.value, page.value, size.value)
    users.value = response.data.content
    totalPages.value = response.data.totalPages
    totalElements.value = response.data.totalElements
  } catch (err) {
    error.value = 'Failed to load users. Please try again.'
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
  editingUser.value = null
  showFormModal.value = true
}
function openEditModal(user) {
  editingUser.value = user
  showFormModal.value = true
}
function closeFormModal() {
  showFormModal.value = false
}
function onSaved() {
  const wasEditing = !!editingUser.value
  showFormModal.value = false
  loadUsers()
  toast.success(wasEditing ? 'User updated successfully' : 'User created successfully')
}

function openPermissionsModal(user) {
  permissionsUser.value = user
  showPermissionsModal.value = true
}
function closePermissionsModal() {
  showPermissionsModal.value = false
}
function onPermissionsSaved() {
  showPermissionsModal.value = false
  loadUsers()
  toast.success('Permissions updated successfully')
}

async function handleDelete(user) {
  if (!confirm(`Delete ${user.name}? This cannot be undone.`)) return
  try {
    await deleteUser(user.id)
    loadUsers()
    toast.success('User deleted successfully')
  } catch (err) {
    toast.error(err.response?.data?.message || 'Failed to delete user.')
  }
}

watch(search, () => { page.value = 0 })
watch([search, page], () => { loadUsers() })
onMounted(() => { loadUsers() })
</script>

<template>
  <AppLayout>
    <div class="page">
      <div class="page-header">
        <h1>Users</h1>
        <button class="btn btn-primary" @click="openCreateModal">+ Add User</button>
      </div>

      <div class="toolbar">
        <input v-model="search" type="text" placeholder="Search by name or email..." class="input search-input" />
      </div>

      <div class="card table-card">
        <table v-if="!loading && !error && users.length > 0">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Type</th>
              <th>Permissions</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td>{{ user.name }}</td>
              <td>{{ user.email }}</td>
              <td>
                <span class="badge" :class="user.userType === 'SUPER_ADMIN' ? 'badge-primary' : 'badge-neutral'">
                  {{ user.userType === 'SUPER_ADMIN' ? 'Super Admin' : 'Normal User' }}
                </span>
              </td>
              <td>
                <span v-if="user.userType === 'SUPER_ADMIN'" class="perm-text">Full access</span>
                <span v-else class="perm-text">
                  {{ [
                    user.canCreate && 'Create',
                    user.canRead && 'Read',
                    user.canUpdate && 'Update',
                    user.canDelete && 'Delete'
                  ].filter(Boolean).join(', ') || 'None' }}
                </span>
              </td>
              <td>
                <span class="badge" :class="user.enabled ? 'badge-success' : 'badge-neutral'">
                  {{ user.enabled ? 'Active' : 'Disabled' }}
                </span>
              </td>
              <td>
                <div class="row-actions">
                  <button class="btn btn-secondary btn-sm" @click="openEditModal(user)">Edit</button>
                  <button
                    v-if="user.userType !== 'SUPER_ADMIN'"
                    class="btn btn-secondary btn-sm"
                    @click="openPermissionsModal(user)"
                  >
                    Permissions
                  </button>
                  <button class="btn btn-danger btn-sm" @click="handleDelete(user)">Delete</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="loading" class="state-message">Loading users...</div>
        <div v-else-if="error" class="state-message error-message">{{ error }}</div>
        <div v-else-if="users.length === 0" class="state-message">No users found.</div>
      </div>

      <div class="pagination" v-if="!loading && !error && totalElements > 0">
        <span class="pagination-info">Page {{ page + 1 }} of {{ totalPages }} ({{ totalElements }} total)</span>
        <div class="pagination-buttons">
          <button class="btn btn-secondary btn-sm" :disabled="page === 0" @click="prevPage">Previous</button>
          <button class="btn btn-secondary btn-sm" :disabled="page >= totalPages - 1" @click="nextPage">Next</button>
        </div>
      </div>
    </div>

    <UserFormModal v-if="showFormModal" :user="editingUser" @close="closeFormModal" @saved="onSaved" />
    <PermissionsModal
      v-if="showPermissionsModal"
      :user="permissionsUser"
      @close="closePermissionsModal"
      @saved="onPermissionsSaved"
    />
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
.row-actions { display: flex; gap: var(--space-sm); flex-wrap: wrap; }
.btn-sm { padding: 5px 12px; font-size: 13px; }
.badge { display: inline-block; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; }
.badge-primary { background: var(--color-primary-soft); color: var(--color-primary); }
.badge-neutral { background: var(--color-bg); color: var(--color-text-muted); border: 1px solid var(--color-border); }
.badge-success { background: var(--color-success-soft); color: var(--color-success); }
.perm-text { font-size: 13px; color: var(--color-text-muted); }
.state-message { padding: var(--space-xl); text-align: center; color: var(--color-text-muted); }
.error-message { color: var(--color-danger); }
.pagination { display: flex; justify-content: space-between; align-items: center; margin-top: var(--space-md); }
.pagination-info { font-size: 13px; color: var(--color-text-muted); }
.pagination-buttons { display: flex; gap: var(--space-sm); }
</style>