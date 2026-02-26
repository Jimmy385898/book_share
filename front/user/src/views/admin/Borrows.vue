<template>
  <div class="borrows">
    <el-card>
      <div class="header-bar">
        <el-select v-model="status" placeholder="选择状态" clearable style="width: 150px" @change="loadBorrows">
          <el-option label="待确认" :value="0" />
          <el-option label="已确认" :value="1" />
          <el-option label="借阅中" :value="2" />
          <el-option label="已归还" :value="3" />
          <el-option label="已取消" :value="4" />
          <el-option label="已拒绝" :value="5" />
          <el-option label="待确认归还" :value="6" />
        </el-select>
      </div>

      <el-table :data="borrows" v-loading="loading" style="margin-top: 20px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="bookId" label="图书ID" width="100" />
        <el-table-column prop="borrowerId" label="借阅者ID" width="100" />
        <el-table-column prop="lenderId" label="出借者ID" width="100" />
        <el-table-column prop="borrowDays" label="借阅天数" width="100" />
        <el-table-column label="交接方式" width="100">
          <template #default="{ row }">
            {{ getDeliveryText(row.deliveryMethod) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否逾期" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isOverdue ? 'danger' : 'success'">
              {{ row.isOverdue ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="180" />
        <el-table-column prop="returnTime" label="应还时间" width="180" />
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
import request from '@/utils/request'

const status = ref(null)
const borrows = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)

const loadBorrows = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/borrow/list', {
      params: {
        page: page.value,
        size: size.value,
        status: status.value
      }
    })
    borrows.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handlePageChange = (newPage) => {
  page.value = newPage
  loadBorrows()
}

const getStatusType = (status) => {
  const types = ['warning', 'success', 'primary', 'info', 'info', 'danger', 'warning']
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = ['待确认', '已确认', '借阅中', '已归还', '已取消', '已拒绝', '待确认归还']
  return texts[status] || '未知'
}

const getDeliveryText = (method) => {
  const texts = { face: '面对面', pickup: '代收点', express: '快递' }
  return texts[method] || method
}

onMounted(() => {
  loadBorrows()
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

