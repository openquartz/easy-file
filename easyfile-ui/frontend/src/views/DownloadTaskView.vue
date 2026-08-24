<template>
  <section class="panel">
    <div class="toolbar">
      <div class="left">
        <el-button type="primary" @click="reload">刷新</el-button>
        <el-select
          v-if="showAppId"
          v-model="unifiedAppId"
          class="app-select"
          placeholder="统一APPID"
          @change="reload"
        >
          <el-option
            v-for="app in apps"
            :key="app.appId"
            :label="app.appId"
            :value="app.appId"
          />
        </el-select>
      </div>
      <el-button text type="primary" @click="openHelp">帮助</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" height="calc(100vh - 220px)">
      <el-table-column prop="registerId" label="ID" width="80" sortable />
      <el-table-column label="处理状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.uploadStatus)" effect="light">
            {{ row.uploadStatusDesc }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="downloadCodeDesc" label="下载描述" width="150" />
      <el-table-column prop="downloadOperateName" label="下载人" width="120" sortable />
      <el-table-column prop="exportTime" label="下载时间" width="170" sortable />
      <el-table-column prop="lastExecuteTime" label="最新执行时间" width="170" />
      <el-table-column prop="invalidTime" label="失效时间" width="170" sortable />
      <el-table-column prop="downloadNum" label="下载次数" width="110" sortable />
      <el-table-column label="执行进度" width="180">
        <template #default="{ row }">
          <el-progress :percentage="row.executeProcess || 0" :stroke-width="10" />
        </template>
      </el-table-column>
      <el-table-column prop="errorMsg" label="异常信息" min-width="160" sortable />
      <el-table-column label="操作" width="210" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="onDownload(row)">下载</el-button>
          <el-button
            v-if="row.uploadStatus === 'fail'"
            link
            type="primary"
            @click="onRetry(row)"
          >
            重试
          </el-button>
          <el-button link type="danger" @click="onCancel(row)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="sizes, total, prev, pager, next, jumper"
        @current-change="loadList"
        @size-change="loadList"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { AppTree, DownloadTask } from '@/api/types'
import {
  clickDownload,
  fetchAppTree,
  fetchTaskList,
  fetchUiConfig,
  retryTask,
  revokeTask
} from '@/api/downloadTask'

const loading = ref(false)
const rows = ref<DownloadTask[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showAppId = ref(false)
const apps = ref<AppTree[]>([])
const unifiedAppId = ref<string>()

function statusType(status: string): 'success' | 'danger' | 'warning' | 'info' | 'primary' {
  if (status === 'success') {
    return 'success'
  }
  if (status === 'fail') {
    return 'danger'
  }
  if (status === 'executing' || status === 'uploading') {
    return 'primary'
  }
  if (status === 'cancel' || status === 'invalid') {
    return 'info'
  }
  return 'warning'
}

async function loadList() {
  loading.value = true
  try {
    const page = await fetchTaskList(pageNum.value, pageSize.value, unifiedAppId.value)
    rows.value = page.modelList || []
    total.value = Number(page.totalRecords || 0)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function reload() {
  pageNum.value = 1
  void loadList()
}

async function confirmAction(message: string): Promise<boolean> {
  try {
    await ElMessageBox.confirm(message, '提示', { type: 'warning' })
    return true
  } catch {
    return false
  }
}

async function onDownload(row: DownloadTask) {
  if (!(await confirmAction('真的要下载吗?'))) {
    return
  }
  try {
    const result = await clickDownload(row.registerId)
    downloadUrl(result.url, result.fileName)
    ElMessage.success('下载成功!')
  } catch (error) {
    ElMessage.error(`下载失败!失败原因:${error instanceof Error ? error.message : String(error)}`)
  }
}

async function onCancel(row: DownloadTask) {
  if (!(await confirmAction(`真的要取消下载任务: ${row.registerId} 吗?`))) {
    return
  }
  try {
    await revokeTask(row.registerId)
    ElMessage.success('取消成功！')
    await loadList()
  } catch (error) {
    ElMessage.error(`取消失败!失败原因:${error instanceof Error ? error.message : String(error)}`)
  }
}

async function onRetry(row: DownloadTask) {
  if (!(await confirmAction(`真的要重试下载任务: ${row.registerId} 吗?`))) {
    return
  }
  try {
    await retryTask(row.registerId)
    ElMessage.success('重试成功！')
    await loadList()
  } catch (error) {
    ElMessage.error(`重试失败!失败原因:${error instanceof Error ? error.message : String(error)}`)
  }
}

function downloadUrl(url: string, filename: string) {
  const normalized = url.replace(/\\/g, '/')
  const xhr = new XMLHttpRequest()
  xhr.open('GET', normalized, true)
  xhr.responseType = 'blob'
  xhr.onload = () => {
    if (xhr.status === 200) {
      const objectUrl = window.URL.createObjectURL(xhr.response)
      const link = document.createElement('a')
      link.href = objectUrl
      link.download = filename
      link.click()
      window.URL.revokeObjectURL(objectUrl)
    }
  }
  xhr.send()
}

function openHelp() {
  window.location.href = 'https://github.com/svnlab/easyfile'
}

onMounted(async () => {
  try {
    const config = await fetchUiConfig()
    showAppId.value = Boolean(config.showAppId)
    if (showAppId.value) {
      apps.value = await fetchAppTree()
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载配置失败')
  }
  await loadList()
})
</script>

<style scoped>
.panel {
  height: 100%;
  background: var(--ef-surface);
  border: 1px solid var(--ef-border);
  border-radius: var(--ef-radius);
  box-shadow: var(--ef-shadow);
  padding: 16px 16px 8px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.left {
  display: flex;
  gap: 10px;
}

.app-select {
  width: 220px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  padding: 12px 0 4px;
}
</style>
