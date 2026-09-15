<script setup>
import { ref, onMounted, watch } from 'vue'
import AppLayout from '../components/AppLayout.vue'
import UserFormModal from '../components/UserFormModal.vue'
import PermissionsModal from '../components/PermissionsModal.vue'
import { getUsers, deleteUser } from '../api/users'
import { useToastStore } from '../stores/toast'
import { debounce } from '../utils/debounce'

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

function getPermissionsList(user) {
  if (user.userType === 'SUPER_ADMIN') return ['Full Access']
  const perms = [
    user.canCreate && 'Create',
    user.canRead && 'Read',
    user.canUpdate && 'Update',
    user.canDelete && 'Delete',
  ].filter(Boolean)
  return perms.length ? perms : ['None']
}

const debouncedSearch = debounce(() => {
  page.value = 0
  loadUsers()
}, 300)

watch(search, debouncedSearch)
watch(page, loadUsers)
onMounted(loadUsers)
</script>

<template>
  <AppLayout>
    <div class="page">
      <div class="page-header">
        <div class="page-header-left">
          <h1>Users</h1>
          <span v-if="!loading && totalElements > 0" class="record-count">
            {{ totalElements }} total
          </span>
        </div>
        <button class="btn btn-primary" @click="openCreateModal">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          Add User
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
          <!-- Loading skeleton -->
          <table v-if="loading">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Type</th>
                <th>Permissions</th>
                <th>Status</th>
                <th style="width:230px">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="i in size" :key="i" class="skeleton-row">
                <td><div class="skeleton skel-md"></div></td>
                <td><div class="skeleton skel-lg"></div></td>
                <td><div class="skeleton skel-badge"></div></td>
                <td><div class="skeleton skel-md"></div></td>
                <td><div class="skeleton skel-badge"></div></td>
                <td><div class="skeleton skel-actions"></div></td>
              </tr>
            </tbody>
          </table>

          <!-- Data table -->
          <table v-else-if="!error && users.length > 0">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Type</th>
                <th>Permissions</th>
                <th>Status</th>
                <th style="width:230px">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in users" :key="user.id">
                <td>
                  <div class="user-cell">
                    <div class="user-avatar">{{ user.name?.[0]?.toUpperCase() }}</div>
                    <span class="cell-primary">{{ user.name }}</span>
                  </div>
                </td>
                <td>
                  <span class="cell-email">{{ user.email }}</span>
                </td>
                <td>
                  <span
                    class="badge"
                    :class="user.userType === 'SUPER_ADMIN' ? 'badge-primary' : 'badge-neutral'"
                  >
                    {{ user.userType === 'SUPER_ADMIN' ? 'Super Admin' : 'Normal User' }}
                  </span>
                </td>
                <td>
                  <div class="perm-tags">
                    <span
                      v-for="perm in getPermissionsList(user)"
                      :key="perm"
                      class="perm-tag"
                      :class="{
                        'perm-tag-admin': perm === 'Full Access',
                        'perm-tag-none': perm === 'None'
                      }"
                    >
                      {{ perm }}
                    </span>
                  </div>
                </td>
                <td>
                  <span
                    class="badge"
                    :class="user.enabled ? 'badge-success' : 'badge-neutral'"
                  >
                    {{ user.enabled ? 'Active' : 'Disabled' }}
                  </span>
                </td>
                <td>
                  <div class="row-actions">
                    <button
                      class="btn btn-secondary btn-sm action-btn"
                      @click="openEditModal(user)"
                      title="Edit user details"
                    >
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                      </svg>
                      Edit
                    </button>

                    <button
                      v-if="user.userType !== 'SUPER_ADMIN'"
                      class="btn btn-perm btn-sm action-btn"
                      @click="openPermissionsModal(user)"
                      title="Configure user permissions"
                    >
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                      </svg>
                      Permissions
                    </button>

                    <button
                      class="btn btn-danger btn-sm action-btn"
                      @click="handleDelete(user)"
                      title="Delete user account"
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
        <div v-if="!loading && !error && users.length === 0" class="state-box">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="state-icon">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
          <p class="state-title">{{ search ? 'No users match your search' : 'No users yet' }}</p>
          <p class="state-subtitle" v-if="!search">Add a user to get started.</p>
        </div>

        <!-- Error state -->
        <div v-if="!loading && error" class="state-box state-error">
          <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="state-icon">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <p class="state-title">{{ error }}</p>
          <button class="btn btn-secondary btn-sm" @click="loadUsers">Try again</button>
        </div>
      </div>

      <!-- Pagination -->
      <div class="pagination" v-if="!loading && !error && totalElements > 0">
        <span class="pagination-info">Page {{ page + 1 }} of {{ totalPages || 1 }} ({{ totalElements }} total)</span>
        <div class="pagination-buttons">
          <button class="btn btn-secondary btn-sm" :disabled="page === 0" @click="prevPage">← Previous</button>
          <button class="btn btn-secondary btn-sm" :disabled="page >= totalPages - 1" @click="nextPage">Next →</button>
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
.page {
  padding: var(--space-xl);
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}
@media (max-width: 768px) {
  .page { padding: var(--space-md); }
}

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
.search-input { padding-left: 36px; }

.table-card { overflow: hidden; }
.table-scroll { overflow-x: auto; }

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 780px;
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

.user-cell {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}
.user-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--color-primary-soft);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  flex-shrink: 0;
}
.cell-primary { font-weight: 500; }
.cell-email {
  font-size: 13px;
  color: var(--color-text-muted);
}

/* ── Permissions mini-tags ──────────────────────────── */
.perm-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.perm-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 7px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  background: var(--color-bg);
  color: var(--color-text-muted);
  border: 1px solid var(--color-border);
}
.perm-tag-admin {
  background: var(--color-primary-soft);
  color: var(--color-primary);
  border-color: rgba(76, 111, 255, 0.25);
  font-weight: 600;
}
.perm-tag-none {
  opacity: 0.6;
}

/* ── Action buttons ────────────────────────────────── */
.row-actions {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  flex-wrap: nowrap;
}
.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  padding: 5px 10px;
}
.btn-perm {
  background: var(--color-primary-soft);
  color: var(--color-primary);
  border: 1px solid rgba(76, 111, 255, 0.25);
  font-weight: 600;
}
.btn-perm:hover {
  background: #DCE3FF;
  border-color: var(--color-primary);
  color: var(--color-primary-hover);
}

/* ── Skeleton ──────────────────────────────────────────────── */
.skeleton-row td { padding: 14px var(--space-md); }
.skel-sm    { height: 14px; width: 60px;  }
.skel-md    { height: 14px; width: 110px; }
.skel-lg    { height: 14px; width: 160px; }
.skel-badge { height: 22px; width: 80px; border-radius: var(--radius-full); }
.skel-actions { height: 28px; width: 170px; border-radius: var(--radius); }

/* ── Empty / Error states ──────────────────────────────────── */
.state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-2xl) var(--space-xl);
  gap: var(--space-sm);
  text-align: center;
}
.state-icon { color: var(--color-text-muted); margin-bottom: var(--space-xs); }
.state-title { font-size: 15px; font-weight: 600; color: var(--color-text); margin: 0; }
.state-subtitle { font-size: 13px; color: var(--color-text-muted); margin: 0; }
.state-error .state-icon { color: var(--color-danger); }
.state-error .state-title { color: var(--color-danger); }

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: var(--space-md);
  gap: var(--space-md);
  flex-wrap: wrap;
}
.pagination-info { font-size: 13px; color: var(--color-text-muted); }
.pagination-buttons { display: flex; gap: var(--space-sm); }
</style>