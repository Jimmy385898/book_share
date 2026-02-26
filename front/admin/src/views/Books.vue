<template>
  <div class="books">
    <el-card>
      <div class="header-bar">
        <el-select v-model="status" placeholder="选择状态" clearable style="width: 150px" @change="loadBooks">
          <el-option label="待审核" :value="0" />
          <el-option label="可借阅" :value="1" />
          <el-option label="已借出" :value="2" />
          <el-option label="下架" :value="3" />
        </el-select>
      </div>

      <el-table :data="books" v-loading="loading" style="margin-top: 20px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="封面" width="100">
          <template #default="{ row }">
            <img :src="row.cover" style="width: 60px; height: 80px; object-fit: cover" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="书名" width="200" />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="borrowCount" label="借阅次数" width="100" />
        <el-table-column prop="followCount" label="关注人数" width="100" />
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="success" size="small" @click="handleAudit(row.id, 1)">
              通过
            </el-button>
            <el-button v-if="row.status === 0" type="warning" size="small" @click="handleAudit(row.id, 3)">
              拒绝
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

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
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const status = ref(null)
const books = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)

const loadBooks = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/book/list', {
      params: {
        page: page.value,
        size: size.value,
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

const getStatusType = (status) => {
  const types = ['warning', 'success', 'info', 'danger']
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = ['待审核', '可借阅', '已借出', '下架']
  return texts[status] || '未知'
}

const handleAudit = async (id, status) => {
  try {
    await request.put(`/admin/book/audit/${id}`, null, {
      params: { status }
    })
    ElMessage.success('操作成功')
    loadBooks()
  } catch (error) {
    console.error(error)
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该图书吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/admin/book/${id}`)
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
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>

