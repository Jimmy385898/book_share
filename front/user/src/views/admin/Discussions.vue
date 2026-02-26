<template>
  <div class="discussions">
    <el-card>
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="讨论管理" name="discussion">
          <div class="header-bar">
            <el-select v-model="status" placeholder="选择状态" clearable style="width: 150px" @change="loadDiscussions">
              <el-option label="待审核" :value="0" />
              <el-option label="已通过" :value="1" />
              <el-option label="已拒绝" :value="2" />
            </el-select>
          </div>

          <el-table :data="discussions" v-loading="loading" style="margin-top: 20px">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="userId" label="用户ID" width="100" />
            <el-table-column prop="title" label="标题" width="200" />
            <el-table-column prop="topic" label="主题" width="100" />
            <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
            <el-table-column prop="viewCount" label="浏览数" width="100" />
            <el-table-column prop="replyCount" label="回复数" width="100" />
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
        </el-tab-pane>

        <el-tab-pane label="回复管理" name="reply">
          <div class="header-bar">
            <el-input 
              v-model="replyDiscussionId" 
              placeholder="输入讨论ID筛选" 
              clearable 
              style="width: 200px"
              @change="loadReplies"
            />
            <el-select v-model="replyStatus" placeholder="选择状态" clearable style="width: 150px; margin-left: 10px" @change="loadReplies">
              <el-option label="已禁用" :value="0" />
              <el-option label="正常" :value="1" />
            </el-select>
          </div>

          <el-table :data="replies" v-loading="replyLoading" style="margin-top: 20px">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="discussionId" label="讨论ID" width="100" />
            <el-table-column label="回复者" width="150">
              <template #default="{ row }">
                <div style="display: flex; align-items: center; gap: 8px;">
                  <el-avatar :size="30" :src="row.avatar">{{ (row.nickname || row.username)?.[0] }}</el-avatar>
                  <span>{{ row.nickname || row.username }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="回复对象" width="150">
              <template #default="{ row }">
                <span v-if="row.parentNickname || row.parentUsername">
                  @{{ row.parentNickname || row.parentUsername }}
                </span>
                <span v-else style="color: #999;">主题回复</span>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="回复内容" min-width="300" show-overflow-tooltip />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '正常' : '已禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="回复时间" width="180" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button 
                  v-if="row.status === 1" 
                  type="warning" 
                  size="small" 
                  @click="handleReplyStatus(row.id, 0)"
                >
                  禁用
                </el-button>
                <el-button 
                  v-if="row.status === 0" 
                  type="success" 
                  size="small" 
                  @click="handleReplyStatus(row.id, 1)"
                >
                  启用
                </el-button>
                <el-button type="danger" size="small" @click="handleDeleteReply(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-if="replyTotal > 0"
            class="pagination"
            :current-page="replyPage"
            :page-size="replySize"
            :total="replyTotal"
            layout="total, prev, pager, next"
            @current-change="handleReplyPageChange"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const activeTab = ref('discussion')
const status = ref(null)
const discussions = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)

// 回复管理相关
const replies = ref([])
const replyPage = ref(1)
const replySize = ref(10)
const replyTotal = ref(0)
const replyLoading = ref(false)
const replyDiscussionId = ref(null)
const replyStatus = ref(null)

const handleTabChange = (tab) => {
  if (tab === 'discussion') {
    loadDiscussions()
  } else if (tab === 'reply') {
    loadReplies()
  }
}

const loadDiscussions = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/discussion/list', {
      params: {
        page: page.value,
        size: size.value,
        status: status.value
      }
    })
    discussions.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadReplies = async () => {
  replyLoading.value = true
  try {
    const res = await request.get('/admin/discussion/reply/list', {
      params: {
        page: replyPage.value,
        size: replySize.value,
        discussionId: replyDiscussionId.value || null,
        status: replyStatus.value
      }
    })
    replies.value = res.data.records
    replyTotal.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    replyLoading.value = false
  }
}

const handlePageChange = (newPage) => {
  page.value = newPage
  loadDiscussions()
}

const handleReplyPageChange = (newPage) => {
  replyPage.value = newPage
  loadReplies()
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
    await request.put(`/admin/discussion/audit/${id}`, null, {
      params: { status }
    })
    ElMessage.success('操作成功')
    loadDiscussions()
  } catch (error) {
    console.error(error)
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该讨论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/admin/discussion/${id}`)
    ElMessage.success('删除成功')
    loadDiscussions()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleReplyStatus = async (id, status) => {
  try {
    await request.put(`/admin/discussion/reply/status/${id}`, null, {
      params: { status }
    })
    ElMessage.success('操作成功')
    loadReplies()
  } catch (error) {
    console.error(error)
  }
}

const handleDeleteReply = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该回复吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/admin/discussion/reply/${id}`)
    ElMessage.success('删除成功')
    loadReplies()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

onMounted(() => {
  loadDiscussions()
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

