import { postJson } from './http'

export function login(username: string, password: string, rememberMe: boolean): Promise<string> {
  return postJson<string>('/easyfile-ui/login', {
    username,
    password,
    rememberMe: rememberMe ? 'on' : ''
  })
}
