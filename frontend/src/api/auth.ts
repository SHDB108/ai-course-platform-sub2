import request from '@/utils/request'
import type { Result } from '@/utils/request'

export interface LoginData {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  username: string
  userId: number
  role: string
}

export const login = (data: LoginData): Promise<Result<LoginResponse>> => {
  return request({
    url: '/v1/auth/login',
    method: 'post',
    data
  })
}
