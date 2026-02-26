<template>
  <div class="reviews">
    <el-card>
      <div class="header-bar">
        <el-select v-model="status" placeholder="选择状态" clearable style="width: 150px" @change="loadReviews">
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已拒绝" :value="2" />
        </el-select>
      </div>

      <el-table :data="reviews" v-loading="loading" style="margin-top: 20px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="bookId" label="图书ID" width="100" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column label="评分" width="150">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="likeCount" label="点赞数" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="success" size="small" @click="handleAudit(row.id, 1)">
              通过
            </el-button>
            <el-button v-if="row.status === 0" type="warning" size="small" @click="handleAudit(row.id, 2)">
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
const reviews = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)

const loadReviews = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/review/list', {
      params: {
        page: page.value,
        size: size.value,
        status: status.value
      }
    })
    reviews.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handlePageChange = (newPage) => {
  page.value = newPage
  loadReviews()
}

const getStatusType = (status) => {
  const types = ['warning', 'success', 'danger']
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = ['待审核', '已通过', '已拒绝']
  return texts[status] || '未知'
}

const handleAudit = async (id, status) => {
  try {
    await request.put(`/admin/review/audit/${id}`, null, {
      params: { status }
    })
    ElMessage.success('操作成功')
    loadReviews()
  } catch (error) {
    console.error(error)
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该书评吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/admin/review/${id}`)
    ElMessage.success('删除成功')
    loadReviews()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

onMounted(() => {
  loadReviews()
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

