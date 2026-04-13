<template>
  <div class="publish-book">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>{{ isEdit ? '编辑图书' : '发布图书' }}</h2>
          <p>{{ isEdit ? '修改后需要重新提交管理员审核' : '发布后需要管理员审核通过才会在前台展示' }}</p>
        </div>
      </template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px" v-loading="loadingDetail">
        <el-form-item label="图书封面" prop="cover">
          <el-upload
            class="cover-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
          >
            <img v-if="form.cover" :src="form.cover" class="cover-preview" />
            <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="书名" prop="title">
          <el-input v-model="form.title" placeholder="请输入书名" />
        </el-form-item>

        <el-form-item label="作者" prop="author">
          <el-input v-model="form.author" placeholder="请输入作者" />
        </el-form-item>

        <el-form-item label="出版社" prop="publisher">
          <el-input v-model="form.publisher" placeholder="请输入出版社" />
        </el-form-item>

        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="form.isbn" placeholder="请输入ISBN" />
        </el-form-item>

        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类">
            <el-option label="文学" value="文学" />
            <el-option label="历史" value="历史" />
            <el-option label="科幻" value="科幻" />
            <el-option label="心理" value="心理" />
            <el-option label="技术" value="技术" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="图书简介" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入图书简介" />
        </el-form-item>

        <el-form-item label="借阅期限" prop="borrowDays">
          <el-input-number v-model="form.borrowDays" :min="1" :max="90" /> 天
        </el-form-item>

        <el-form-item label="允许续借">
          <el-switch v-model="form.allowRenew" />
        </el-form-item>

        <el-form-item label="允许转借">
          <el-switch v-model="form.allowTransfer" />
        </el-form-item>

        <el-form-item label="取书地址" prop="pickupAddress">
          <el-input v-model="form.pickupAddress" placeholder="请输入取书地址" />
        </el-form-item>

        <el-form-item label="配送范围">
          <el-input v-model="form.deliveryRange" placeholder="请输入配送范围" />
        </el-form-item>

        <el-form-item label="归还点">
          <el-input v-model="form.returnPoint" placeholder="请输入归还点" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">{{ isEdit ? '保存并提交审核' : '发布' }}</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const loadingDetail = ref(false)
const uploadUrl = '/api/file/upload'
const isEdit = computed(() => Boolean(route.params.id))

const form = reactive({
  cover: '',
  title: '',
  author: '',
  publisher: '',
  isbn: '',
  category: '',
  description: '',
  borrowDays: 30,
  allowRenew: true,
  allowTransfer: false,
  pickupAddress: '',
  deliveryRange: '',
  returnPoint: ''
})

const rules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  pickupAddress: [{ required: true, message: '请输入取书地址', trigger: 'blur' }]
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

const handleUploadSuccess = (response) => {
  form.cover = response.data
}

const loadBookDetail = async () => {
  if (!isEdit.value) {
    return
  }
  loadingDetail.value = true
  try {
    const res = await request.get(`/book/my/${route.params.id}`)
    Object.assign(form, {
      cover: res.data.cover || '',
      title: res.data.title || '',
      author: res.data.author || '',
      publisher: res.data.publisher || '',
      isbn: res.data.isbn || '',
      category: res.data.category || '',
      description: res.data.description || '',
      borrowDays: res.data.borrowDays || 30,
      allowRenew: !!res.data.allowRenew,
      allowTransfer: !!res.data.allowTransfer,
      pickupAddress: res.data.pickupAddress || '',
      deliveryRange: res.data.deliveryRange || '',
      returnPoint: res.data.returnPoint || ''
    })
  } catch (error) {
    console.error(error)
    router.push('/my-books')
  } finally {
    loadingDetail.value = false
  }
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put(`/book/${route.params.id}`, form)
      ElMessage.success('图书修改成功，已重新提交审核')
    } else {
      await request.post('/book/publish', form)
      ElMessage.success('发布成功，等待审核')
    }
    router.push('/my-books')
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadBookDetail()
})
</script>

<style scoped>
.publish-book {
  max-width: 800px;
  margin: 0 auto;
}

.card-header h2 {
  margin: 0;
}

.card-header p {
  margin: 8px 0 0;
  color: #909399;
  font-size: 14px;
}

.cover-uploader {
  width: 178px;
  height: 178px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.3s;
}

.cover-uploader:hover {
  border-color: #409eff;
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-preview {
  width: 178px;
  height: 178px;
  object-fit: cover;
}
</style>
