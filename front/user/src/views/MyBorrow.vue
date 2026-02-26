<template>
  <div class="my-borrow">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="我借的" name="1">
        <el-table :data="borrowList" v-loading="loading">
          <el-table-column prop="bookId" label="图书ID" width="80" />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
              <el-tag v-if="row.isOverdue && row.status === 2" type="danger" size="small" style="margin-left: 5px">逾期</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="borrowDays" label="借阅天数" width="100" />
          <el-table-column prop="deliveryMethod" label="交接方式" width="100">
            <template #default="{ row }">
              {{ getDeliveryText(row.deliveryMethod) }}
            </template>
          </el-table-column>
          <el-table-column prop="applyTime" label="申请时间" width="180" />
          <el-table-column prop="returnTime" label="应还时间" width="180" />
          <el-table-column prop="renewCount" label="续借次数" width="100">
            <template #default="{ row }">
              {{ row.renewCount || 0 }}/2
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280">
            <template #default="{ row }">
              <el-button v-if="row.status === 0" type="warning" size="small" @click="handleCancel(row.id)">
                取消申请
              </el-button>
              <el-button v-if="row.status >= 1" type="info" size="small" @click="viewAgreement(row.id)">
                查看协议
              </el-button>
              <el-button v-if="row.status === 2" type="primary" size="small" @click="handleApplyReturn(row.id)">
                申请归还
              </el-button>
              <el-button v-if="row.status === 2 && row.renewCount < 2" size="small" @click="handleRenew(row.id)">
                续借
              </el-button>
              <el-tag v-if="row.status === 6" type="warning" size="small">等待确认归还</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="我出借的" name="2">
        <el-table :data="lendList" v-loading="loading">
          <el-table-column prop="bookId" label="图书ID" width="80" />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
              <el-tag v-if="row.isOverdue && row.status === 2" type="danger" size="small" style="margin-left: 5px">逾期</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="borrowDays" label="借阅天数" width="100" />
          <el-table-column prop="deliveryMethod" label="交接方式" width="100">
            <template #default="{ row }">
              {{ getDeliveryText(row.deliveryMethod) }}
            </template>
          </el-table-column>
          <el-table-column prop="applyTime" label="申请时间" width="180" />
          <el-table-column prop="returnTime" label="应还时间" width="180" />
          <el-table-column label="操作" width="300">
            <template #default="{ row }">
              <el-button v-if="row.status === 0" type="success" size="small" @click="handleConfirm(row.id)">
                同意借阅
              </el-button>
              <el-button v-if="row.status === 0" type="danger" size="small" @click="handleReject(row.id)">
                拒绝
              </el-button>
              <el-button v-if="row.status === 1" type="primary" size="small" @click="handleDeliver(row.id)">
                确认发货
              </el-button>
              <el-button v-if="row.status === 6" type="success" size="small" @click="handleConfirmReturn(row.id)">
                确认归还
              </el-button>
              <el-button v-if="row.status >= 1" type="info" size="small" @click="viewAgreement(row.id)">
                查看协议
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-pagination
      v-if="total > 0"
      class="pagination"
      :current-page="page"
      :page-size="size"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="handlePageChange"
    />

    <el-dialog v-model="showRenewDialog" title="续借申请" width="400px">
      <el-form label-width="100px">
        <el-form-item label="续借天数">
          <el-input-number v-model="renewDays" :min="1" :max="30" />
        </el-form-item>
        <el-alert type="info" :closable="false" style="margin-top: 10px">
          <template #default>
            <div>续借说明：</div>
            <div>1. 每本图书最多可续借2次</div>
            <div>2. 续借将延长归还期限</div>
            <div>3. 请按时归还，避免逾期</div>
          </template>
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="showRenewDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmRenew">确定续借</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showAgreementDialog" title="电子借阅协议" width="700px">
      <div class="agreement-content">
        <pre>{{ agreementText }}</pre>
      </div>
      <template #footer>
        <el-button type="primary" @click="showAgreementDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const activeTab = ref('1')
const borrowList = ref([])
const lendList = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const showRenewDialog = ref(false)
const renewDays = ref(7)
const currentRecordId = ref(null)
const showAgreementDialog = ref(false)
const agreementText = ref('')

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/borrow/my', {
      params: {
        page: page.value,
        size: size.value,
        type: parseInt(activeTab.value)
      }
    })
    if (activeTab.value === '1') {
      borrowList.value = res.data.records
    } else {
      lendList.value = res.data.records
    }
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  page.value = 1
  loadData()
}

const handlePageChange = (newPage) => {
  page.value = newPage
  loadData()
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

const handleConfirm = async (id) => {
  try {
    await request.post(`/borrow/confirm/${id}`)
    ElMessage.success('已确认')
    loadData()
  } catch (error) {
    console.error(error)
  }
}

const handleReject = async (id) => {
  try {
    await ElMessageBox.confirm('确定要拒绝此借阅申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.post(`/borrow/reject/${id}`)
    ElMessage.success('已拒绝')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleDeliver = async (id) => {
  try {
    await request.post(`/borrow/deliver/${id}`)
    ElMessage.success('已发货')
    loadData()
  } catch (error) {
    console.error(error)
  }
}

const handleApplyReturn = async (id) => {
  try {
    await ElMessageBox.confirm('确认要申请归还图书吗？提交后需等待出借方确认。', '申请归还', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.post(`/borrow/return/apply/${id}`)
    ElMessage.success('归还申请已提交，等待出借方确认')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleConfirmReturn = async (id) => {
  try {
    await ElMessageBox.confirm('确认借阅者已归还图书？确认后图书将恢复为可借阅状态。', '确认归还', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.post(`/borrow/return/confirm/${id}`)
    ElMessage.success('归还确认成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleRenew = (id) => {
  currentRecordId.value = id
  showRenewDialog.value = true
}

const confirmRenew = async () => {
  try {
    await request.post(`/borrow/renew/${currentRecordId.value}`, null, {
      params: { days: renewDays.value }
    })
    ElMessage.success('续借成功，归还期限已延长')
    showRenewDialog.value = false
    loadData()
  } catch (error) {
    console.error(error)
  }
}

const viewAgreement = async (id) => {
  try {
    const res = await request.get(`/borrow/agreement/${id}`)
    agreementText.value = res.data
    showAgreementDialog.value = true
  } catch (error) {
    console.error(error)
  }
}

const handleCancel = async (id) => {
  try {
    await ElMessageBox.confirm('确定要取消借阅申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.post(`/borrow/cancel/${id}`)
    ElMessage.success('已取消借阅申请')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.agreement-content {
  max-height: 500px;
  overflow-y: auto;
  background: #f5f5f5;
  padding: 20px;
  border-radius: 4px;
}

.agreement-content pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
}
</style>

