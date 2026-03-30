import Cookies from 'js-cookie'

const TokenKey = 'token'

export function getToken() {
  return Cookies.get(TokenKey)
}
export function getUsername() {
  return Cookies.get('username')
}

export function setToken(token) {
  return Cookies.set(TokenKey, token, { expires: 1 / 48 })
}

export function setUsername(username) {
  return Cookies.set('username', username, { expires: 1 / 48 })
}

export function removeKey() {
  return Cookies.remove(TokenKey)
}

export function removeUsername() {
  return Cookies.remove('username')
}


