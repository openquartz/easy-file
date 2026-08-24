import { postJson } from './http'
import type { AdminUiConfig, AppTree, DownloadTask, DownloadUrlResult, Pagination } from './types'

export function fetchUiConfig(): Promise<AdminUiConfig> {
  return postJson<AdminUiConfig>('/easyfile-ui/download-task/ui-config')
}

export function fetchAppTree(): Promise<AppTree[]> {
  return postJson<AppTree[]>('/easyfile-ui/download-task/listAppId')
}

export function fetchTaskList(
  pageNum: number,
  pageSize: number,
  unifiedAppId?: string
): Promise<Pagination<DownloadTask>> {
  const query = unifiedAppId ? `?unifiedAppId=${encodeURIComponent(unifiedAppId)}` : ''
  return postJson<Pagination<DownloadTask>>(`/easyfile-ui/download-task/list${query}`, {
    pageNum,
    pageSize
  })
}

export function clickDownload(registerId: string): Promise<DownloadUrlResult> {
  return postJson<DownloadUrlResult>('/easyfile-ui/download-task/download', { registerId })
}

export function revokeTask(registerId: string): Promise<unknown> {
  return postJson('/easyfile-ui/download-task/revoke', { registerId })
}

export function retryTask(registerId: string): Promise<unknown> {
  return postJson('/easyfile-ui/download-task/retry', { registerId })
}
