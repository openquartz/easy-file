<template>
  <div class="admin-shell">
    <header class="titlebar">
      <div class="brand">
        <span class="mark" />
        EasyFile Admin
      </div>
      <div class="account">
        <span class="name">admin</span>
        <a class="logout" href="/easyfile-ui/logout">退出</a>
      </div>
    </header>
    <div class="body">
      <aside class="sidebar">
        <router-link class="nav-item active" to="/">下载任务</router-link>
      </aside>
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.admin-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--ef-bg);
}

.titlebar {
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: rgba(255, 255, 255, 0.72);
  border-bottom: 1px solid var(--ef-border);
  backdrop-filter: blur(16px);
  position: sticky;
  top: 0;
  z-index: 10;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  letter-spacing: -0.02em;
}

.mark {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: linear-gradient(180deg, #7ec2f5, #5ba3e8);
  box-shadow: 0 0 0 3px rgba(91, 163, 232, 0.16);
}

.account {
  display: flex;
  align-items: center;
  gap: 14px;
  color: var(--ef-text-secondary);
  font-size: 14px;
}

.logout {
  color: var(--ef-accent);
  text-decoration: none;
}

.logout:hover {
  text-decoration: underline;
}

.body {
  flex: 1;
  display: flex;
  min-height: 0;
}

.sidebar {
  width: 196px;
  padding: 16px 12px;
  background: var(--ef-sidebar);
  border-right: 1px solid var(--ef-border);
}

.nav-item {
  display: block;
  padding: 10px 12px;
  border-radius: 10px;
  color: var(--ef-text);
  text-decoration: none;
}

.nav-item.active {
  background: #ffffff;
  color: var(--ef-accent);
  box-shadow: var(--ef-shadow);
}

.content {
  flex: 1;
  min-width: 0;
  padding: 18px;
}
</style>
