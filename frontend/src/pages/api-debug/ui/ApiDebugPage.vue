<script setup lang="ts">
import { ref } from 'vue'

import { getOpinionById } from '@/entities/opinion/api/getOpinionById'
import { getOpinionForSubject } from '@/entities/opinion/api/getOpinionForSubject'
import { createOpinion } from '@/features/opinion/create/api/createOpinion'
import { updateOpinion } from '@/features/opinion/edit/api/updateOpinion'
import { deleteOpinion } from '@/features/opinion/delete/api/deleteOpinion'
import { linkComponent } from '@/features/opinion/link-component/api/linkComponent'
import { unlinkComponent } from '@/features/opinion/link-component/api/unlinkComponent'
import { adjustComponentWeight } from '@/features/opinion/adjust-component-weight/api/adjustComponentWeight'
import { useAuthSession } from '@/features/auth/model/useAuthSession'
import { HttpError } from '@/shared/api/http'
import { Button } from '@/shared/ui/button'

const auth = useAuthSession()

const loginValue = ref('test')
const password = ref('1234')
const opinionId = ref('00000000-0000-0000-0000-000000000000')
const subjectId = ref('00000000-0000-0000-0000-000000000001')
const childOpinionId = ref('00000000-0000-0000-0000-000000000002')
const linkId = ref('00000000-0000-0000-0000-000000000003')

const loading = ref(false)
const result = ref<string>('')
const error = ref<string>('')

const setResult = (value: unknown) => {
  result.value = JSON.stringify(value, null, 2)
}

const setError = (value: unknown) => {
  if (value instanceof HttpError) {
    error.value = `HTTP ${value.status}\n${JSON.stringify(value.payload, null, 2)}`
    return
  }

  if (value instanceof Error) {
    error.value = value.message
    return
  }

  error.value = String(value)
}

const run = async (request: () => Promise<unknown>) => {
  loading.value = true
  result.value = ''
  error.value = ''

  try {
    const payload = await request()
    setResult(payload)
  } catch (err) {
    setError(err)
  } finally {
    loading.value = false
  }
}

const onLogin = () =>
  run(() =>
    auth.signIn({
      login: loginValue.value,
      password: password.value,
    }),
  )

const onRefresh = () => run(() => auth.refresh())
const onClearAuth = () => {
  auth.clear()
  result.value = 'Session cleared'
  error.value = ''
}

const onGetById = () => run(() => getOpinionById(opinionId.value))
const onGetForSubject = () => run(() => getOpinionForSubject(subjectId.value))

const onCreate = () =>
  run(async () => {
    const created = await createOpinion({
      subjectId: subjectId.value,
      subjective: ['debug subjective'],
      objective: ['debug objective'],
      mark: 8,
    })
    opinionId.value = created.id
    return created
  })

const onUpdate = () =>
  run(() =>
    updateOpinion({
      opinionId: opinionId.value,
      subjective: ['updated subjective'],
      objective: ['updated objective'],
      mark: 7.5,
    }),
  )

const onDelete = () => run(() => deleteOpinion(opinionId.value))

const onLink = () =>
  run(async () => {
    const linked = await linkComponent({
      parentOpinionId: opinionId.value,
      childOpinionId: childOpinionId.value,
      weight: 0.4,
    })

    const firstLink = linked.components[0]
    if (firstLink) {
      linkId.value = firstLink.id
    }

    return linked
  })

const onUnlink = () => run(() => unlinkComponent(linkId.value))

const onAdjustWeight = () =>
  run(() =>
    adjustComponentWeight({
      linkId: linkId.value,
      weight: 0.8,
    }),
  )
</script>

<template>
  <main class="page">
    <section class="card">
      <h1>API Debug</h1>
      <p class="hint">Buttons for auth and opinions requests.</p>

      <div class="fields">
        <label>
          <span>Login</span>
          <input v-model="loginValue" />
        </label>
        <label>
          <span>Password</span>
          <input v-model="password" type="password" />
        </label>
        <label>
          <span>Opinion ID</span>
          <input v-model="opinionId" />
        </label>
        <label>
          <span>Subject ID</span>
          <input v-model="subjectId" />
        </label>
        <label>
          <span>Child Opinion ID</span>
          <input v-model="childOpinionId" />
        </label>
        <label>
          <span>Link ID</span>
          <input v-model="linkId" />
        </label>
      </div>

      <div class="actions">
        <Button :disabled="loading" @click="onLogin">Auth Login</Button>
        <Button :disabled="loading" variant="outline" @click="onRefresh">Auth Refresh</Button>
        <Button :disabled="loading" variant="outline" @click="onClearAuth">Auth Clear</Button>
      </div>

      <div class="actions">
        <Button :disabled="loading" @click="onGetById">Opinion Get By Id</Button>
        <Button :disabled="loading" @click="onGetForSubject">Opinion Get For Subject</Button>
        <Button :disabled="loading" @click="onCreate">Opinion Create</Button>
        <Button :disabled="loading" @click="onUpdate">Opinion Update</Button>
        <Button :disabled="loading" variant="destructive" @click="onDelete">Opinion Delete</Button>
      </div>

      <div class="actions">
        <Button :disabled="loading" @click="onLink">Opinion Link Component</Button>
        <Button :disabled="loading" @click="onUnlink">Opinion Unlink Component</Button>
        <Button :disabled="loading" @click="onAdjustWeight">Opinion Adjust Weight</Button>
      </div>

      <p class="state">
        isAuthenticated:
        <b>{{ auth.isAuthenticated.value }}</b>
      </p>
      <p class="state">
        accessToken:
        <code>{{ auth.accessToken.value ?? 'null' }}</code>
      </p>
      <p class="state">
        refreshToken:
        <code>{{ auth.refreshToken.value ?? 'null' }}</code>
      </p>

      <p v-if="error" class="error">{{ error }}</p>
      <pre v-if="result" class="result">{{ result }}</pre>
    </section>
  </main>
</template>

<style scoped>
.page {
  display: grid;
  place-items: center;
  padding: 2rem 1rem;
}

.card {
  width: min(100%, 980px);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 1rem;
  display: grid;
  gap: 0.9rem;
}

.hint {
  margin: 0;
  opacity: 0.8;
}

.fields {
  display: grid;
  gap: 0.55rem;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
}

.fields label {
  display: grid;
  gap: 0.3rem;
}

.fields input {
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 0.45rem 0.6rem;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.state {
  margin: 0;
}

.error {
  margin: 0;
  white-space: pre-wrap;
  color: #cf2f2f;
}

.result {
  margin: 0;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 0.6rem;
  overflow: auto;
  max-height: 320px;
}
</style>

