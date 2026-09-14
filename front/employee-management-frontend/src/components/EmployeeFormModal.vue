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
    props.employee.cvUrl = 'uploaded' // mark as present so download button shows
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
  } catch (err) {
    fileError.value = 'Failed to download CV.'
  }
}

async function handleDownloadContract() {
  contractDownloading.value = true
  fileError.value = ''
  try {
    const response = await getContractBlob(props.employee.id)
    downloadBlob(response.data, `contract-${props.employee.firstName}-${props.employee.lastName}.pdf`)
  } catch (err) {
    fileError.value = 'Failed to generate contract.'
  } finally {
    contractDownloading.value = false
  }
}
</script>

<template>
  <div class="overlay" @click.self="$emit('close')">
    <div class="modal card">
      <h2>{{ employee ? 'Edit Employee' : 'Add Employee' }}</h2>

      <form id="employeeForm" @submit.prevent="handleSubmit">
        <div class="form-row">
          <div class="field">
            <label>First Name</label>
            <input v-model="form.firstName" class="input" />
            <span v-if="errors.firstName" class="field-error">{{ errors.firstName }}</span>
          </div>
          <div class="field">
            <label>Last Name</label>
            <input v-model="form.lastName" class="input" />
            <span v-if="errors.lastName" class="field-error">{{ errors.lastName }}</span>
          </div>
        </div>

        <div class="field">
          <label>Email</label>
          <input v-model="form.email" type="email" class="input" />
          <span v-if="errors.email" class="field-error">{{ errors.email }}</span>
        </div>

        <div class="form-row">
          <div class="field">
            <label>Phone Number</label>
            <input v-model="form.phoneNumber" class="input" />
          </div>
          <div class="field">
            <label>Job Title</label>
            <input v-model="form.jobTitle" class="input" />
            <span v-if="errors.jobTitle" class="field-error">{{ errors.jobTitle }}</span>
          </div>
        </div>

        <div class="form-row">
          <div class="field">
            <label>Department</label>
            <input v-model="form.department" class="input" />
          </div>
          <div class="field">
            <label>Hire Date</label>
            <input v-model="form.hireDate" type="date" class="input" />
            <span v-if="errors.hireDate" class="field-error">{{ errors.hireDate }}</span>
          </div>
        </div>

        <div class="field">
          <label>Salary</label>
          <input v-model.number="form.salary" type="number" class="input" />
        </div>

        <p v-if="generalError" class="general-error">{{ generalError }}</p>
      </form>

      <!-- Files section: only available once the employee actually exists -->
      <div v-if="employee" class="files-section">
        <h3>Files</h3>
        <p v-if="fileError" class="general-error">{{ fileError }}</p>

        <div class="file-row">
          <div class="file-info">
            <span class="file-label">Photo</span>
            <img v-if="photoPreviewUrl" :src="photoPreviewUrl" class="photo-preview" />
            <span v-else class="file-empty">No photo uploaded</span>
          </div>
          <label class="btn btn-secondary btn-sm file-upload-btn">
            {{ photoUploading ? 'Uploading...' : 'Upload Photo' }}
            <input type="file" accept="image/jpeg,image/png" hidden @change="handlePhotoChange" />
          </label>
        </div>

        <div class="file-row">
          <div class="file-info">
            <span class="file-label">CV</span>
            <span class="file-empty">{{ employee.cvUrl ? 'Uploaded' : 'No CV uploaded' }}</span>
          </div>
          <div class="file-buttons">
            <button v-if="employee.cvUrl" type="button" class="btn btn-secondary btn-sm" @click="handleDownloadCv">
              Download
            </button>
            <label class="btn btn-secondary btn-sm file-upload-btn">
              {{ cvUploading ? 'Uploading...' : (employee.cvUrl ? 'Replace CV' : 'Upload CV') }}
              <input type="file" accept="application/pdf" hidden @change="handleCvChange" />
            </label>
          </div>
        </div>

        <div class="file-row">
          <div class="file-info">
            <span class="file-label">Contract</span>
            <span class="file-empty">Generated from current employee data</span>
          </div>
          <button type="button" class="btn btn-secondary btn-sm" :disabled="contractDownloading" @click="handleDownloadContract">
            {{ contractDownloading ? 'Generating...' : 'Download Contract' }}
          </button>
        </div>
      </div>

      <!-- Save/Cancel now always the last thing in the modal -->
      <div class="modal-actions">
        <button type="button" class="btn btn-secondary" @click="$emit('close')">Cancel</button>
        <button type="submit" form="employeeForm" class="btn btn-primary" :disabled="saving">
          {{ saving ? 'Saving...' : 'Save' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.overlay { position: fixed; inset: 0; background: rgba(28, 36, 48, 0.4); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { width: 480px; max-height: 90vh; overflow-y: auto; padding: var(--space-xl); }
.modal h2 { font-size: 18px; margin-bottom: var(--space-lg); }
.form-row { display: flex; gap: var(--space-md); }
.field { flex: 1; margin-bottom: var(--space-md); }
.field label { display: block; font-size: 13px; color: var(--color-text-muted); margin-bottom: 4px; }
.field-error { display: block; color: var(--color-danger); font-size: 12px; margin-top: 4px; }
.general-error { color: var(--color-danger); font-size: 13px; margin-bottom: var(--space-md); }
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-sm);
  margin-top: var(--space-lg);
  padding-top: var(--space-lg);
  border-top: 1px solid var(--color-border);
}

.files-section {
  margin-top: var(--space-xl);
  padding-top: var(--space-lg);
  border-top: 1px solid var(--color-border);
}
.files-section h3 {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-muted);
  margin-bottom: var(--space-md);
}
.file-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-md) 0;
  border-bottom: 1px solid var(--color-border);
  gap: var(--space-md);
}
.file-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}
.file-info {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  min-width: 0;
}
.file-label {
  font-size: 13px;
  font-weight: 600;
  width: 55px;
  flex-shrink: 0;
}
.file-empty {
  font-size: 13px;
  color: var(--color-text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.photo-preview {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
  border: 1px solid var(--color-border);
}
.file-buttons {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  flex-shrink: 0;
}
.file-upload-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  white-space: nowrap;
}
.btn-sm {
  padding: 5px 12px;
  font-size: 13px;
  line-height: 1.4;
}
</style>