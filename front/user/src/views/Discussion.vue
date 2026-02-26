<template>
  <div class="discussion">
    <div class="header-bar">
      <el-select v-model="topic" placeholder="选择主题" clearable style="width: 200px" @change="loadDiscussions">
        <el-option label="科幻" value="科幻" />
        <el-option label="历史" value="历史" />
        <el-option label="心理" value="心理" />
        <el-option label="文学" value="文学" />
        <el-option label="技术" value="技术" />
        <el-option label="其他" value="其他" />
      </el-select>
      <el-button type="primary" @click="showPublishDialog = true">发起讨论</el-button>
    </div>

    <el-card v-for="item in discussions" :key="item.id" class="discussion-card" shadow="hover" @click="$router.push(`/discussion/${item.id}`)">
      <div class="discussion-header">
        <div class="user-info">
          <el-avatar :size="40" :src="item.avatar">{{ (item.nickname || item.username)?.[0] }}</el-avatar>
          <div class="user-detail">
            <span class="username">{{ item.nickname || item.username }}</span>
            <span class="time">{{ item.createTime }}</span>
          </div>
        </div>
        <el-tag size="small">{{ item.topic }}</el-tag>
      </div>
      <h3 class="discussion-title">{{ item.title }}</h3>
      <p class="discussion-content">{{ item.content }}</p>
      <div class="discussion-meta">
        <span><el-icon><View /></el-icon> {{ item.viewCount }}</span>
        <span><el-icon><ChatDotRound /></el-icon> {{ item.replyCount }}</span>
        <span><el-icon><Star /></el-icon> {{ item.likeCount }}</span>
      </div>
    </el-card>

    <el-pagination
      v-if="total > 0"
      class="pagination"
      :current-page="page"
      :page-size="size"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="handlePageChange"
    />

    <el-dialog v-model="showPublishDialog" title="发起讨论" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="主题">
          <el-select v-model="form.topic" placeholder="选择主题">
            <el-option label="科幻" value="科幻" />
            <el-option label="历史" value="历史" />
            <el-option label="心理" value="心理" />
            <el-option label="文学" value="文学" />
            <el-option label="技术" value="技术" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPublishDialog = false">取消</el-button>
        <el-button type="primary" @click="handlePublish">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const topic = ref('')
const discussions = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const showPublishDialog = ref(false)

const form = reactive({
  title: '',
  topic: '',
  content: ''
})

const loadDiscussions = async () => {
  try {
    const res = await request.get('/discussion/list', {
      params: {
        page: page.value,
        size: size.value,
        topic: topic.value
      }
    })
    discussions.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  }
}

const handlePageChange = (newPage) => {
  page.value = newPage
  loadDiscussions()
}

const handlePublish = async () => {
  if (!form.title || !form.content) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    await request.post('/discussion/publish', form)
    ElMessage.success('发布成功，等待审核')
    showPublishDialog.value = false
    form.title = ''
    form.topic = ''
    form.content = ''
    loadDiscussions()
  } catch (error) {
    console.error(error)
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
  margin-bottom: 20px;
}

.discussion-card {
  cursor: pointer;
  margin-bottom: 15px;
  transition: transform 0.3s;
}

.discussion-card:hover {
  transform: translateY(-2px);
}

.discussion-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.username {
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

.time {
  font-size: 12px;
  color: #999;
}

.discussion-title {
  font-size: 18px;
  margin: 0 0 10px 0;
  font-weight: 600;
}

.discussion-content {
  color: #666;
  line-height: 1.6;
  margin-bottom: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.discussion-meta {
  display: flex;
  gap: 20px;
  color: #999;
  font-size: 14px;
}

.discussion-meta span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.time {
  margin-left: auto;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>

