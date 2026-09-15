export function decodeToken(token) {
  try {
    const payload = token.split('.')[1]
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    return JSON.parse(decoded)
  } catch {
    return null
  }
}

export function getTokenExpiryMs(token) {
  const decoded = decodeToken(token)
  if (!decoded?.exp) return null
  return decoded.exp * 1000 // JWT exp is in seconds, JS timestamps are in ms
}