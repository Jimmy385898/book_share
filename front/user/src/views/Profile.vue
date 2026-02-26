<template>
  <div class="profile">
    <el-card>
      <template #header>
        <h2>个人信息</h2>
      </template>
      <el-form :model="form" label-width="100px">
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
          >
            <img v-if="form.avatar" :src="form.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="信用分">
          <el-tag type="success" size="large">{{ form.creditScore }}</el-tag>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleUpdate">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const uploadUrl = '/api/file/upload'

const form = reactive({
  avatar: '',
  username: '',
  nickname: '',
  phone: '',
  email: '',
  realName: '',
  address: '',
  creditScore: 100
})

const loadUserInfo = async () => {
  try {
    const res = await request.get('/user/info')
    Object.assign(form, res.data)
  } catch (error) {
    console.error(error)
  }
}

const handleUploadSuccess = (response) => {
  form.avatar = response.data
}

const handleUpdate = async () => {
  try {
    await request.put('/user/info', form)
    userStore.setUserInfo(form)
    ElMessage.success('更新成功')
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile {
  max-width: 600px;
  margin: 0 auto;
}

.avatar-uploader {
  width: 120px;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.3s;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar {
  width: 120px;
  height: 120px;
  object-fit: cover;
}
</style>

