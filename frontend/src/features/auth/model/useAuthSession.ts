import { computed, ref } from 'vue'

import type { AuthCredentials, AuthSession } from '@/features/auth/model/authSession'
import { login } from '@/features/auth/login/api/login'
import { refreshSession } from '@/features/auth/refresh-session/api/refreshSession'
import { setAccessTokenProvider } from '@/shared/api/http'

const STORAGE_KEY = 'auth_session'

const accessToken = ref<string | null>(null)
const refreshToken = ref<string | null>(null)
const expiresInMs = ref<number>(0)

const saveToStorage = () => {
  if (!accessToken.value || !refreshToken.value) {
    localStorage.removeItem(STORAGE_KEY)
    return
  }

  localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify({
      accessToken: accessToken.value,
      refreshToken: refreshToken.value,
      expiresInMs: expiresInMs.value,
    }),
  )
}

const applySession = (session: AuthSession) => {
  accessToken.value = session.accessToken
  refreshToken.value = session.refreshToken
  expiresInMs.value = session.expiresInMs
  saveToStorage()
}

const clear = () => {
  accessToken.value = null
  refreshToken.value = null
  expiresInMs.value = 0
  saveToStorage()
}

const loadFromStorage = () => {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) {
    return
  }

  try {
    const parsed: unknown = JSON.parse(raw)
    if (!parsed || typeof parsed !== 'object') {
      clear()
      return
    }

    if ('accessToken' in parsed && 'refreshToken' in parsed && 'expiresInMs' in parsed) {
      applySession(parsed as AuthSession)
      return
    }

    if ('accessToken' in parsed && 'refreshToken' in parsed && 'expiresInMilliseconds' in parsed) {
      const legacy = parsed as {
        accessToken: string
        refreshToken: string
        expiresInMilliseconds: number
      }
      applySession({
        accessToken: legacy.accessToken,
        refreshToken: legacy.refreshToken,
        expiresInMs: legacy.expiresInMilliseconds,
      })
      return
    }

    clear()
  } catch {
    clear()
  }
}

setAccessTokenProvider(() => accessToken.value)
loadFromStorage()

export const useAuthSession = () => {
  const isAuthenticated = computed(() => Boolean(accessToken.value))

  const signIn = async (credentials: AuthCredentials) => {
    const session = await login(credentials)
    applySession(session)
    return session
  }

  const refresh = async () => {
    if (!refreshToken.value) {
      throw new Error('No refresh token in session')
    }

    const session = await refreshSession(refreshToken.value)
    applySession(session)
    return session
  }

  return {
    accessToken,
    refreshToken,
    expiresInMs,
    isAuthenticated,
    signIn,
    refresh,
    clear,
  }
}

