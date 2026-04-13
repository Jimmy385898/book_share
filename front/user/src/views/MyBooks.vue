<template>
  <div class="my-books">
    <el-card>
      <template #header>
        <div class="header-bar">
          <div class="header-left">
            <h2>我的图书管理</h2>
            <p>管理自己发布的图书，编辑后需重新提交管理员审核</p>
          </div>
          <el-button type="primary" @click="router.push('/publish')">发布新图书</el-button>
        </div>
      </template>

      <div class="filter-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索书名、作者、ISBN"
          clearable
          style="width: 320px"
          @change="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="status" placeholder="图书状态" clearable style="width: 180px" @change="handleSearch">
          <el-option label="待审核" :value="0" />
          <el-option label="可借阅" :value="1" />
          <el-option label="已借出" :value="2" />
          <el-option label="已拒绝/下架" :value="3" />
        </el-select>
      </div>

      <el-table :data="books" v-loading="loading">
        <el-table-column label="封面" width="100">
          <template #default="{ row }">
            <img :src="row.cover || '/default-book.png'" class="book-cover" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="书名" min-width="180" />
        <el-table-column prop="author" label="作者" width="140" />
        <el-table-column prop="category" label="分类" width="110" />
        <el-table-column label="状态" width="140">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="borrowCount" label="借阅次数" width="100" />
        <el-table-column prop="followCount" label="关注人数" width="100" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row.id)">查看</el-button>
            <el-button link type="primary" :disabled="row.status === 2" @click="handleEdit(row.id)">编辑</el-button>
            <el-button link type="danger" :disabled="row.status === 2" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && books.length === 0" description="暂无图书记录" />

      <el-pagination
        v-if="total > 0"
        class="pagination"
        :current-page="page"
        :page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const keyword = ref('')
const status = ref(null)
const books = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)

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

const loadBooks = async () => {
  loading.value = true
  try {
    const res = await request.get('/book/my', {
      params: {
        page: page.value,
        size: size.value,
        keyword: keyword.value,
        status: status.value
      }
    })
    books.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadBooks()
}

const handlePageChange = (newPage) => {
  page.value = newPage
  loadBooks()
}

const handleView = (id) => {
  router.push(`/my-books/${id}`)
}

const handleEdit = (id) => {
  router.push(`/book/edit/${id}`)
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这本图书吗？删除后无法恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/book/${id}`)
    ElMessage.success('删除成功')
    loadBooks()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

onMounted(() => {
  loadBooks()
})
</script>

<style scoped>
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.header-left h2 {
  margin: 0;
}

.header-left p {
  margin: 8px 0 0;
  color: #909399;
  font-size: 14px;
}

.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.book-cover {
  width: 56px;
  height: 76px;
  object-fit: cover;
  border-radius: 6px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
