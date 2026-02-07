import axios from 'axios'
import { getToken, getUsername } from '@/core/auth.js'
import { isNotEmpty } from '@/utils/plugins.js'
import router from '@/router'

const baseURL = '/api/short-link/v1'
const http = axios.create({
  baseURL: baseURL,
  timeout: 15000
})

http.interceptors.request.use(
  (config) => {
    config.headers.Token = isNotEmpty(getToken()) ? getToken() : ''
    config.headers.Username = isNotEmpty(getUsername()) ? getUsername() : ''
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

http.interceptors.response.use(
  (res) => {
    if (res.status == 0 || res.status == 200) {
      return Promise.resolve(res)
    }
    return Promise.reject(res)
  },
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      router.push('/login')
    }
    return Promise.reject(err)
  }
)

export default http
