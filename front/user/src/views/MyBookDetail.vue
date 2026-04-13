<template>
  <div class="my-book-detail" v-loading="loading">
    <el-card v-if="book.id">
      <template #header>
        <div class="header-bar">
          <div>
            <h2>{{ book.title }}</h2>
            <p>图书状态：<el-tag :type="getStatusType(book.status)">{{ getStatusText(book.status) }}</el-tag></p>
          </div>
          <div class="actions">
            <el-button @click="router.push('/my-books')">返回列表</el-button>
            <el-button type="primary" :disabled="book.status === 2" @click="router.push(`/book/edit/${book.id}`)">编辑图书</el-button>
          </div>
        </div>
      </template>

      <div class="content">
        <div class="cover-panel">
          <img :src="book.cover || '/default-book.png'" class="book-cover" />
        </div>
        <div class="info-panel">
          <div class="info-grid">
            <div><span class="label">作者：</span>{{ book.author || '-' }}</div>
            <div><span class="label">出版社：</span>{{ book.publisher || '-' }}</div>
            <div><span class="label">ISBN：</span>{{ book.isbn || '-' }}</div>
            <div><span class="label">分类：</span>{{ book.category || '-' }}</div>
            <div><span class="label">借阅期限：</span>{{ book.borrowDays }} 天</div>
            <div><span class="label">允许续借：</span>{{ book.allowRenew ? '是' : '否' }}</div>
            <div><span class="label">允许转借：</span>{{ book.allowTransfer ? '是' : '否' }}</div>
            <div><span class="label">取书地址：</span>{{ book.pickupAddress || '-' }}</div>
            <div><span class="label">配送范围：</span>{{ book.deliveryRange || '-' }}</div>
            <div><span class="label">归还点：</span>{{ book.returnPoint || '-' }}</div>
            <div><span class="label">关注人数：</span>{{ book.followCount || 0 }}</div>
            <div><span class="label">借阅次数：</span>{{ book.borrowCount || 0 }}</div>
            <div><span class="label">发布时间：</span>{{ book.createTime || '-' }}</div>
            <div><span class="label">更新时间：</span>{{ book.updateTime || '-' }}</div>
          </div>

          <el-divider />

          <div>
            <h3>图书简介</h3>
            <p class="description">{{ book.description || '暂无简介' }}</p>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const book = ref({})

const getStatusType = (value) => {
  const types = {
    0: 'warning',
    1: 'success',
    2: 'info',
    3: 'danger'
  }
  return types[value] || 'info'
}

const getStatusText = (value) => {
  const texts = {
    0: '待审核',
    1: '可借阅',
    2: '已借出',
    3: '已拒绝/下架'
  }
  return texts[value] || '未知'
}

const loadBook = async () => {
  loading.value = true
  try {
    const res = await request.get(`/book/my/${route.params.id}`)
    book.value = res.data
  } catch (error) {
    console.error(error)
    router.push('/my-books')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadBook()
})
</script>

<style scoped>
.header-bar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.header-bar h2 {
  margin: 0;
}

.header-bar p {
  margin: 10px 0 0;
  color: #606266;
}

.actions {
  display: flex;
  gap: 12px;
}

.content {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 28px;
}

.book-cover {
  width: 100%;
  max-width: 260px;
  border-radius: 12px;
  object-fit: cover;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px 24px;
  color: #303133;
}

.label {
  color: #909399;
}

.description {
  margin: 0;
  line-height: 1.8;
  color: #606266;
}
</style>
