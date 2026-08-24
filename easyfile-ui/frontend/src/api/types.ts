export interface ResponseResult<T = unknown> {
  success: boolean
  code: string
  message: string
  data: T
}

export interface Pagination<T> {
  page: number
  pageSize: number
  totalRecords: number
  modelList: T[]
}

export interface DownloadTask {
  registerId: string
  uploadStatus: string
  uploadStatusDesc: string
  errorMsg: string
  downloadCode: string
  downloadCodeDesc: string
  downloadOperateBy: string
  downloadOperateName: string
  exportTime: string
  lastExecuteTime: string
  invalidTime: string
  downloadNum: number
  executeProcess: number
  updateBy: string
}

export interface DownloadUrlResult {
  url: string
  fileName: string
  fileSystem: string
}

export interface AppTree {
  appId: string
  childList?: string[]
}

export interface AdminUiConfig {
  showAppId: boolean
}
