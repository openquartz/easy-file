import { describe, expect, it } from 'vitest'
import { unwrap } from './http'

describe('unwrap', () => {
  it('returns data when success is true', () => {
    expect(unwrap({ success: true, code: '200', message: 'ok', data: { showAppId: true } }))
      .toEqual({ showAppId: true })
  })

  it('throws the server message when success is false', () => {
    expect(() => unwrap({ success: false, code: '01', message: '重试失败', data: null }))
      .toThrow('重试失败')
  })

  it('throws a fallback message when the body is missing', () => {
    expect(() => unwrap(undefined as never)).toThrow('请求失败')
  })
})
