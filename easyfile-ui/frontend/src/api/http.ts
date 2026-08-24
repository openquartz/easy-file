import axios from 'axios'
import type { ResponseResult } from './types'

export const http = axios.create({
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' }
})

export function unwrap<T>(body: ResponseResult<T>): T {
  if (!body || !body.success) {
    throw new Error((body && body.message) || '请求失败')
  }
  return body.data
}

export async function postJson<T>(url: string, data?: unknown): Promise<T> {
  const response = await http.post<ResponseResult<T>>(url, data ?? {})
  return unwrap(response.data)
}
