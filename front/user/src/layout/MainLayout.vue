<template>
  <div class="layout">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <div class="logo">
            <el-icon :size="28"><Reading /></el-icon>
            <span>图书共享</span>
          </div>
          <el-menu mode="horizontal" :default-active="activeMenu" router class="nav-menu">
            <el-menu-item index="/home">首页</el-menu-item>
            <el-menu-item index="/books">图书广场</el-menu-item>
            <el-menu-item index="/discussion">讨论区</el-menu-item>
            <el-menu-item index="/rank">排行榜</el-menu-item>
            <el-menu-item index="/my-books">我的图书</el-menu-item>
          </el-menu>
          <div class="user-info">
            <el-button type="primary" @click="router.push('/publish')">发布图书</el-button>
            <el-dropdown @command="handleCommand">
              <div class="user-avatar">
                <el-avatar :src="userStore.userInfo.avatar || '/default-avatar.png'">
                  {{ userStore.userInfo.nickname?.[0] || 'U' }}
                </el-avatar>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="borrow">我的借阅</el-dropdown-item>
                  <el-dropdown-item command="myBooks">我的图书</el-dropdown-item>
                  <el-dropdown-item command="changePassword">修改密码</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => {
  return '/' + route.path.split('/')[1]
})

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      router.push('/login')
    })
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'borrow') {
    router.push('/borrow')
  } else if (command === 'myBooks') {
    router.push('/my-books')
  } else if (command === 'changePassword') {
    router.push('/change-password')
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.header {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  cursor: pointer;
}

.nav-menu {
  flex: 1;
  margin: 0 40px;
  border: none;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-avatar {
  cursor: pointer;
}

.main {
  max-width: 1400px;
  margin: 0 auto;
  padding: 30px 20px;
}
</style>
