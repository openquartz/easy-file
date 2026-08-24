import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'
import LoginView from '@/views/LoginView.vue'
import DownloadTaskView from '@/views/DownloadTaskView.vue'

const router = createRouter({
  history: createWebHistory('/easyfile-ui/'),
  routes: [
    {
      path: '/toLogin',
      name: 'login',
      component: LoginView
    },
    {
      path: '/',
      component: AdminLayout,
      children: [
        {
          path: '',
          name: 'download-task',
          component: DownloadTaskView
        }
      ]
    }
  ]
})

export default router
