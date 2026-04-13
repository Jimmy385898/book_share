<template>
  <ChangePassword
    api-path="/user/password"
    title="修改密码"
    subtitle="请填写原密码和新密码，修改后需要重新登录"
    redirect-path="/login"
  />
</template>

<script setup>
import ChangePassword from './ChangePassword.vue'
</script>
