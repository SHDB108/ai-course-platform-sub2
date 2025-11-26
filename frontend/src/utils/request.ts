import axios from 'axios'
import type { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

export interface Result<T = any> {
  code: number
  msg: string
  data: T
}

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response: AxiosResponse<Result>) => {
    const res = response.data
    if (res.code !== 0) {
      ElMessage.error(res.msg || 'Request failed')
      return Promise.reject(new Error(res.msg || 'Error'))
    }
    return res
  },
  (error) => {
    // Handle 401 Unauthorized - redirect to login
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      window.location.href = '/login'
      return Promise.reject(error)
    }
    ElMessage.error(error.message || 'Network error')
    return Promise.reject(error)
  }
)

export default service
