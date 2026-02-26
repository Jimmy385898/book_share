<template>
  <div class="books">
    <div class="filter-bar">
      <el-input v-model="keyword" placeholder="搜索图书名称、作者、ISBN" clearable style="width: 300px" @change="loadBooks">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="category" placeholder="选择分类" clearable style="width: 150px" @change="loadBooks">
        <el-option label="文学" value="文学" />
        <el-option label="历史" value="历史" />
        <el-option label="科幻" value="科幻" />
        <el-option label="心理" value="心理" />
        <el-option label="技术" value="技术" />
        <el-option label="其他" value="其他" />
      </el-select>
      <el-select v-model="status" placeholder="图书状态" clearable style="width: 150px" @change="loadBooks">
        <el-option label="可借阅" :value="1" />
        <el-option label="已借出" :value="2" />
      </el-select>
    </div>

    <el-row :gutter="20" v-loading="loading">
      <el-col :span="6" v-for="book in books" :key="book.id">
        <el-card class="book-card" shadow="hover" @click="$router.push(`/book/${book.id}`)">
          <img :src="book.cover || '/default-book.png'" class="book-cover" />
          <div class="book-info">
            <h3>{{ book.title }}</h3>
            <p class="author">{{ book.author }}</p>
            <el-tag :type="book.status === 1 ? 'success' : 'info'" size="small">
              {{ book.status === 1 ? '可借阅' : '已借出' }}
            </el-tag>
            <div class="book-stats">
              <span><el-icon><View /></el-icon> {{ book.borrowCount }}</span>
              <span><el-icon><Star /></el-icon> {{ book.followCount }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-pagination
      v-if="total > 0"
      class="pagination"
      :current-page="page"
      :page-size="size"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="handlePageChange"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const keyword = ref('')
const category = ref('')
const status = ref(null)
const books = ref([])
const page = ref(1)
const size = ref(12)
const total = ref(0)
const loading = ref(false)

const loadBooks = async () => {
  loading.value = true
  try {
    const res = await request.get('/book/list', {
      params: {
        page: page.value,
        size: size.value,
        keyword: keyword.value,
        category: category.value,
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

const handlePageChange = (newPage) => {
  page.value = newPage
  loadBooks()
}

onMounted(() => {
  loadBooks()
})
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
}

.book-card {
  cursor: pointer;
  transition: transform 0.3s;
  margin-bottom: 20px;
  height: 100%;
}

.book-card:hover {
  transform: translateY(-5px);
}

.book-cover {
  width: 100%;
  height: 280px;
  object-fit: cover;
  border-radius: 8px;
}

.book-info {
  margin-top: 15px;
}

.book-info h3 {
  font-size: 16px;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.author {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
}

.book-stats {
  display: flex;
  gap: 15px;
  color: #999;
  font-size: 14px;
  margin-top: 10px;
}

.book-stats span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style>

