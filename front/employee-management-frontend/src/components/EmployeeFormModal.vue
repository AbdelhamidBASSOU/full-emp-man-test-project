<script setup>
import { ref, watch } from 'vue'
import {
  createEmployee, updateEmployee,
  uploadPhoto, uploadCv, getPhotoBlob, getCvBlob, getContractBlob, downloadBlob
} from '../api/employees'

const props = defineProps({
  employee: { type: Object, default: null },
})
const emit = defineEmits(['close', 'saved'])

const form = ref({
  firstName: '', lastName: '', email: '', phoneNumber: '',
  jobTitle: '', department: '', hireDate: '', salary: null,
})

const errors = ref({})
const saving = ref(false)
const generalError = ref('')

const photoPreviewUrl = ref(null)
const photoUploading = ref(false)
const cvUploading = ref(false)
const contractDownloading = ref(false)
const fileError = ref('')

watch(
  () => props.employee,
  (emp) => {
    if (emp) {
      form.value = { ...emp }
      loadPhotoPreview()
    }
  },
  { immediate: true }
)

async function loadPhotoPreview() {
  if (!props.employee?.photoUrl) return
  try {
    const response = await getPhotoBlob(props.employee.id)
    photoPreviewUrl.value = window.URL.createObjectURL(response.data)
  } catch {
    photoPreviewUrl.value = null
  }
}

async function handleSubmit() {
  errors.value = {}
  generalError.value = ''
  saving.value = true
  try {
    if (props.employee) {
      await updateEmployee(props.employee.id, form.value)
    } else {
      await createEmployee(form.value)
    }
    emit('saved')
  } catch (err) {
    if (err.response?.status === 400 && typeof err.response.data === 'object') {
      errors.value = err.response.data
    } else {
      generalError.value = err.response?.data?.message || 'Something went wrong. Please try again.'
    }
  } finally {
    saving.value = false
  }
}

async function handlePhotoChange(event) {
  const file = event.target.files[0]
  if (!file || !props.employee) return
  fileError.value = ''
  photoUploading.value = true
  try {
    await uploadPhoto(props.employee.id, file)
    await loadPhotoPreview()
  } catch (err) {
    fileError.value = err.response?.data?.message || 'Failed to upload photo.'
  } finally {
    photoUploading.value = false
  }
}

async function handleCvChange(event) {
  const file = event.target.files[0]
  if (!file || !props.employee) return
  fileError.value = ''
  cvUploading.value = true
  try {
    await uploadCv(props.employee.id, file)
    props.employee.cvUrl = 'uploaded'
  } catch (err) {
    fileError.value = err.response?.data?.message || 'Failed to upload CV.'
  } finally {
    cvUploading.value = false
  }
}

async function handleDownloadCv() {
  try {
    const response = await getCvBlob(props.employee.id)
    downloadBlob(response.data, `cv-${props.employee.firstName}-${props.employee.lastName}.pdf`)
  } catch {
    fileError.value = 'Failed to download CV.'
  }
}

async function handleDownloadContract() {
  contractDownloading.value = true
  fileError.value = ''
  try {
    const response = await getContractBlob(props.employee.id)
    downloadBlob(response.data, `contract-${props.employee.firstName}-${props.employee.lastName}.pdf`)
  } catch {
    fileError.value = 'Failed to generate contract.'
  } finally {
    contractDownloading.value = false
  }
}
</script>

<template>
  <div class="overlay" @click.self="$emit('close')">
    <div class="modal card">
      <!-- Modal header -->
      <div class="modal-header">
        <div>
          <h2>{{ employee ? 'Edit Employee' : 'Add Employee' }}</h2>
          <p v-if="employee" class="modal-subtitle">
            {{ employee.firstName }} {{ employee.lastName }}
          </p>
        </div>
        <button class="modal-close btn-ghost btn" @click="$emit('close')" aria-label="Close">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
      </div>

      <!-- Form -->
      <form id="employeeForm" @submit.prevent="handleSubmit" class="modal-body">
        <p v-if="generalError" class="general-error">{{ generalError }}</p>

        <div class="section-label">Personal Information</div>

        <div class="form-row">
          <div class="field">
            <label>First Name <span class="required">*</span></label>
            <input v-model="form.firstName" class="input" :class="{ 'input-error': errors.firstName }" />
            <span v-if="errors.firstName" class="field-error">{{ errors.firstName }}</span>
          </div>
          <div class="field">
            <label>Last Name <span class="required">*</span></label>
            <input v-model="form.lastName" class="input" :class="{ 'input-error': errors.lastName }" />
            <span v-if="errors.lastName" class="field-error">{{ errors.lastName }}</span>
          </div>
        </div>

        <div class="field">
          <label>Email <span class="required">*</span></label>
          <input v-model="form.email" type="email" class="input" :class="{ 'input-error': errors.email }" />
          <span v-if="errors.email" class="field-error">{{ errors.email }}</span>
        </div>

        <div class="field">
          <label>Phone Number</label>
          <input v-model="form.phoneNumber" class="input" />
        </div>

        <div class="section-label" style="margin-top: var(--space-md);">Employment Details</div>

        <div class="form-row">
          <div class="field">
            <label>Job Title <span class="required">*</span></label>
            <input v-model="form.jobTitle" class="input" :class="{ 'input-error': errors.jobTitle }" />
            <span v-if="errors.jobTitle" class="field-error">{{ errors.jobTitle }}</span>
          </div>
          <div class="field">
            <label>Department</label>
            <input v-model="form.department" class="input" />
          </div>
        </div>

        <div class="form-row">
          <div class="field">
            <label>Hire Date</label>
            <input v-model="form.hireDate" type="date" class="input" :class="{ 'input-error': errors.hireDate }" />
            <span v-if="errors.hireDate" class="field-error">{{ errors.hireDate }}</span>
          </div>
          <div class="field">
            <label>Salary</label>
            <input v-model.number="form.salary" type="number" class="input" placeholder="0.00" />
          </div>
        </div>
      </form>

      <!-- Files section — only available once the employee exists -->
      <div v-if="employee" class="files-section">
        <div class="section-label">Files & Documents</div>
        <p v-if="fileError" class="general-error">{{ fileError }}</p>

        <div class="file-row">
          <div class="file-info">
            <img v-if="photoPreviewUrl" :src="photoPreviewUrl" class="photo-preview" alt="Employee photo" />
            <div v-else class="photo-placeholder">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <circle cx="12" cy="8" r="4"/><path d="M6 20v-2a4 4 0 0 1 8 0v2"/>
              </svg>
            </div>
            <div>
              <div class="file-label">Photo</div>
              <div class="file-status">{{ photoPreviewUrl ? 'Uploaded' : 'No photo' }}</div>
            </div>
          </div>
          <label class="btn btn-secondary btn-sm file-upload-btn">
            {{ photoUploading ? 'Uploading…' : 'Upload Photo' }}
            <input type="file" accept="image/jpeg,image/png" hidden @change="handlePhotoChange" />
          </label>
        </div>

        <div class="file-row">
          <div class="file-info">
            <div class="file-icon-box">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
            </div>
            <div>
              <div class="file-label">Curriculum Vitae</div>
              <div class="file-status">{{ employee.cvUrl ? 'Uploaded' : 'No file' }}</div>
            </div>
          </div>
          <div class="file-buttons">
            <button v-if="employee.cvUrl" type="button" class="btn btn-secondary btn-sm" @click="handleDownloadCv">
              Download
            </button>
            <label class="btn btn-secondary btn-sm file-upload-btn">
              {{ cvUploading ? 'Uploading…' : (employee.cvUrl ? 'Replace' : 'Upload CV') }}
              <input type="file" accept="application/pdf" hidden @change="handleCvChange" />
            </label>
          </div>
        </div>

        <div class="file-row">
          <div class="file-info">
            <div class="file-icon-box file-icon-box--contract">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
              </svg>
            </div>
            <div>
              <div class="file-label">Employment Contract</div>
              <div class="file-status">Generated from current data</div>
            </div>
          </div>
          <button
            type="button"
            class="btn btn-secondary btn-sm"
            :disabled="contractDownloading"
            @click="handleDownloadContract"
          >
            {{ contractDownloading ? 'Generating…' : 'Download PDF' }}
          </button>
        </div>
      </div>

      <!-- Footer -->
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" @click="$emit('close')">Cancel</button>
        <button type="submit" form="employeeForm" class="btn btn-primary" :disabled="saving">
          <svg v-if="saving" class="spinner" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"/>
          </svg>
          {{ saving ? 'Saving…' : (employee ? 'Save Changes' : 'Add Employee') }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(19, 23, 34, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 400;
  padding: var(--space-md);
  backdrop-filter: blur(2px);
}
.modal {
  width: 100%;
  max-width: 520px;
  max-height: 92vh;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  box-shadow: var(--shadow-lg);
  border-radius: var(--radius-lg);
}
@media (max-width: 600px) {
  .overlay { padding: 0; align-items: flex-end; }
  .modal {
    max-width: 100%;
    max-height: 96vh;
    border-bottom-left-radius: 0;
    border-bottom-right-radius: 0;
  }
}

/* ── Header ───────────────────────────────────────────────── */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: var(--space-lg) var(--space-xl);
  border-bottom: 1px solid var(--color-border);
  flex-shrink: 0;
}
.modal-header h2 { font-size: 17px; }
.modal-subtitle { font-size: 13px; color: var(--color-text-muted); margin: 2px 0 0; }
.modal-close { color: var(--color-text-muted); padding: 4px; }

/* ── Body / Form ──────────────────────────────────────────── */
.modal-body {
  padding: var(--space-lg) var(--space-xl);
  flex: 1;
  overflow-y: auto;
}
.section-label {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--color-text-muted);
  margin-bottom: var(--space-md);
}
.form-row { display: flex; gap: var(--space-md); }
@media (max-width: 480px) {
  .form-row { flex-direction: column; }
}
.field { flex: 1; margin-bottom: var(--space-md); }
.field label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: 5px;
}
.required { color: var(--color-danger); }
.field-error { display: block; color: var(--color-danger); font-size: 12px; margin-top: 4px; }
.general-error {
  background: var(--color-danger-soft);
  color: var(--color-danger);
  font-size: 13px;
  padding: var(--space-sm) var(--space-md);
  border-radius: var(--radius);
  margin-bottom: var(--space-md);
}
.input-error {
  border-color: var(--color-danger);
}
.input-error:focus {
  box-shadow: 0 0 0 3px var(--color-danger-soft);
}

/* ── Files section ───────────────────────────────────────── */
.files-section {
  padding: var(--space-lg) var(--space-xl);
  border-top: 1px solid var(--color-border);
  background: var(--color-bg);
  flex-shrink: 0;
}
.file-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-md) 0;
  gap: var(--space-md);
}
.file-row + .file-row {
  border-top: 1px solid var(--color-border);
}
.file-info {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  min-width: 0;
}
.file-label { font-size: 13px; font-weight: 600; }
.file-status { font-size: 12px; color: var(--color-text-muted); }

.photo-preview {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
  border: 2px solid var(--color-border);
}
.photo-placeholder {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--color-border);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-muted);
  flex-shrink: 0;
}
.file-icon-box {
  width: 36px;
  height: 36px;
  border-radius: var(--radius);
  background: var(--color-primary-soft);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.file-icon-box--contract {
  background: var(--color-success-soft);
  color: var(--color-success);
}
.file-buttons {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  flex-shrink: 0;
}
.file-upload-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  white-space: nowrap;
}

/* ── Footer ──────────────────────────────────────────────── */
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-sm);
  padding: var(--space-md) var(--space-xl);
  border-top: 1px solid var(--color-border);
  background: var(--color-surface);
  flex-shrink: 0;
}

/* ── Spinner animation ───────────────────────────────────── */
.spinner {
  animation: spin 0.8s linear infinite;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}
</style>