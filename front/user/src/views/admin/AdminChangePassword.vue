<template>
  <ChangePassword
    api-path="/admin/user/password"
    title="管理员修改密码"
    subtitle="请输入当前管理员账号密码，修改后将跳转回管理员登录页"
    redirect-path="/admin/login"
  />
</template>

<script setup>
import ChangePassword from '../ChangePassword.vue'
</script>
