const ADMIN_TOKEN_KEY = 'bear_mcp_admin_token'
const UNAUTHORIZED_EVENT = 'bear-mcp-admin-unauthorized'

function redirectToLogin() {
  localStorage.removeItem(ADMIN_TOKEN_KEY)
  window.dispatchEvent(new CustomEvent(UNAUTHORIZED_EVENT))
}

export async function api(path, options = {}) {
  const token = localStorage.getItem(ADMIN_TOKEN_KEY)

  if (!token) {
    redirectToLogin()
    throw new Error('请先登录')
  }

  const response = await fetch(`/api/admin${path}`, {
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}), ...(options.headers || {}) },
    ...options,
    body: options.body ? JSON.stringify(options.body) : undefined
  })
  const payload = await response.json().catch(() => null)

  if (response.status === 401 || payload?.code === 401) {
    redirectToLogin()
    throw new Error(payload?.message || '登录已过期，请重新登录')
  }

  if (!response.ok || !payload || payload.code !== 200) throw new Error(payload?.message || '请求失败')
  return payload.data
}

export async function login(username, password) {
  const response = await fetch('/api/admin/auth/login', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ username, password }) })
  const payload = await response.json().catch(() => null)
  if (!response.ok || !payload || payload.code !== 200) throw new Error(payload?.message || '登录失败')
  localStorage.setItem(ADMIN_TOKEN_KEY, payload.data.token)
  return payload.data
}

export { ADMIN_TOKEN_KEY, UNAUTHORIZED_EVENT }
