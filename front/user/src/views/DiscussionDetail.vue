<template>
  <div class="discussion-detail" v-loading="loading">
    <el-card v-if="discussion.id" class="main-post">
      <div class="post-header">
        <div class="user-info">
          <el-avatar :size="50" :src="user.avatar">{{ (user.nickname || user.username)?.[0] }}</el-avatar>
          <div class="user-detail">
            <span class="username">{{ user.nickname || user.username }}</span>
            <span class="time">{{ discussion.createTime }}</span>
          </div>
        </div>
        <el-tag>{{ discussion.topic }}</el-tag>
      </div>
      <h1 class="post-title">{{ discussion.title }}</h1>
      <div class="post-content">
        <p>{{ discussion.content }}</p>
      </div>
      <div class="post-meta">
        <span class="meta-item"><el-icon><View /></el-icon> {{ discussion.viewCount }}</span>
        <span class="meta-item"><el-icon><ChatDotRound /></el-icon> {{ discussion.replyCount }}</span>
        <span class="meta-item like-action" @click="handleLike">
          <el-icon><Star /></el-icon>
          <span>点赞 {{ discussion.likeCount }}</span>
        </span>
      </div>
    </el-card>

    <el-card class="reply-section">
      <h3>全部回复 ({{ replies.length }})</h3>
      
      <!-- 回复列表 - 类似贴吧的楼层结构 -->
      <div class="reply-list">
        <div class="reply-item" v-for="(reply, index) in replies" :key="reply.id">
          <div class="floor-number">{{ index + 1 }}楼</div>
          <div class="reply-main">
            <div class="reply-header">
              <div class="user-info">
                <el-avatar :size="40" :src="reply.avatar">{{ (reply.nickname || reply.username)?.[0] }}</el-avatar>
                <div class="user-detail">
                  <span class="username">{{ reply.nickname || reply.username }}</span>
                  <span class="time">{{ reply.createTime }}</span>
                </div>
              </div>
              <el-button text type="primary" @click="showReplyDialog(reply)">
                <el-icon><ChatDotRound /></el-icon> 回复
              </el-button>
            </div>
            <div class="reply-content">
              <span v-if="reply.parentNickname" class="reply-to">
                回复 <span class="mention">@{{ reply.parentNickname || reply.parentUsername }}</span>：
              </span>
              {{ reply.content }}
            </div>
            
            <!-- 子回复列表 -->
            <div v-if="reply.children && reply.children.length > 0" class="sub-replies">
              <div class="sub-reply-item" v-for="child in reply.children" :key="child.id">
                <div class="sub-reply-header">
                  <el-avatar :size="32" :src="child.avatar">{{ (child.nickname || child.username)?.[0] }}</el-avatar>
                  <span class="username">{{ child.nickname || child.username }}</span>
                  <span class="reply-to">
                    回复 <span class="mention">@{{ child.parentNickname || child.parentUsername }}</span>
                  </span>
                  <span class="time">{{ child.createTime }}</span>
                  <el-button text type="primary" size="small" @click="showReplyDialog(child)">
                    <el-icon><ChatDotRound /></el-icon> 回复
                  </el-button>
                </div>
                <div class="sub-reply-content">{{ child.content }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <el-divider />

      <div class="reply-form">
        <div class="form-header">
          <el-avatar :size="40" :src="currentUser.avatar">{{ (currentUser.nickname || currentUser.username)?.[0] }}</el-avatar>
          <span class="current-username">{{ currentUser.nickname || currentUser.username }}</span>
        </div>
        <el-input
          v-model="replyContent"
          type="textarea"
          :rows="4"
          :placeholder="replyTarget ? `回复 @${replyTarget.nickname || replyTarget.username}` : '写下你的回复...'"
        />
        <div class="form-actions">
          <el-button v-if="replyTarget" @click="cancelReply">取消回复</el-button>
          <el-button type="primary" @click="handleReply">发表回复</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const discussion = ref({})
const user = ref({})
const replies = ref([])
const replyContent = ref('')
const replyTarget = ref(null)
const currentUser = ref(userStore.userInfo)

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await request.get(`/discussion/${route.params.id}`)
    discussion.value = res.data.discussion
    user.value = res.data.user
    loadReplies()
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadReplies = async () => {
  try {
    const res = await request.get(`/discussion/reply/${route.params.id}`)
    replies.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const handleLike = async () => {
  try {
    await request.post(`/discussion/like/${route.params.id}`)
    ElMessage.success('操作成功')
    await loadDetail()
  } catch (error) {
    console.error(error)
  }
}

const showReplyDialog = (reply) => {
  replyTarget.value = reply
  // 滚动到回复框
  document.querySelector('.reply-form').scrollIntoView({ behavior: 'smooth' })
}

const cancelReply = () => {
  replyTarget.value = null
  replyContent.value = ''
}

const handleReply = async () => {
  if (!replyContent.value) {
    ElMessage.warning('请输入回复内容')
    return
  }
  try {
    await request.post('/discussion/reply', {
      discussionId: route.params.id,
      parentId: replyTarget.value?.id || null,
      content: replyContent.value
    })
    ElMessage.success('回复成功')
    replyContent.value = ''
    replyTarget.value = null
    loadReplies()
    // 刷新讨论详情以更新回复数
    loadDetail()
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.main-post {
  margin-bottom: 20px;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
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

.post-title {
  font-size: 24px;
  margin: 0 0 20px 0;
  font-weight: 600;
  color: #333;
}

.post-content {
  margin: 20px 0;
  line-height: 1.8;
  font-size: 16px;
  color: #333;
}

.post-meta {
  display: flex;
  gap: 20px;
  color: #999;
  font-size: 14px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  align-items: center;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.like-action {
  cursor: pointer;
  color: #e6a23c;
  font-weight: 600;
  transition: color 0.2s;
}

.like-action:hover {
  color: #f56c6c;
}

.reply-section h3 {
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 600;
}

.reply-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.reply-item {
  display: flex;
  gap: 15px;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.reply-item:last-child {
  border-bottom: none;
}

.floor-number {
  flex-shrink: 0;
  width: 50px;
  text-align: center;
  color: #999;
  font-size: 14px;
  font-weight: 600;
}

.reply-main {
  flex: 1;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.reply-content {
  line-height: 1.8;
  font-size: 15px;
  color: #333;
  margin-bottom: 15px;
}

.reply-to {
  color: #999;
  font-size: 14px;
}

.mention {
  color: #409eff;
  font-weight: 600;
}

.sub-replies {
  margin-top: 15px;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 8px;
}

.sub-reply-item {
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.sub-reply-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.sub-reply-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.sub-reply-header .username {
  font-size: 13px;
}

.sub-reply-header .time {
  margin-left: auto;
  margin-right: 10px;
}

.sub-reply-header .el-button {
  font-size: 12px;
}

.sub-reply-content {
  padding-left: 42px;
  line-height: 1.6;
  font-size: 14px;
  color: #333;
}

.reply-form {
  margin-top: 20px;
}

.form-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 15px;
}

.current-username {
  font-weight: 600;
  color: #333;
}

.form-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>

