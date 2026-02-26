<template>
  <div class="home">
    <el-row :gutter="20">
      <el-col :span="18">
        <div class="banner">
          <h1>欢迎来到社区图书共享平台</h1>
          <p>分享阅读，传递知识</p>
        </div>

        <div class="section">
          <div class="section-header">
            <h2>热门图书</h2>
            <el-button text @click="$router.push('/books')">查看更多 <el-icon><ArrowRight /></el-icon></el-button>
          </div>
          <el-row :gutter="20">
            <el-col :span="6" v-for="book in hotBooks" :key="book.id">
              <el-card class="book-card" shadow="hover" @click="$router.push(`/book/${book.id}`)">
                <img :src="book.cover || '/default-book.png'" class="book-cover" />
                <div class="book-info">
                  <h3>{{ book.title }}</h3>
                  <p>{{ book.author }}</p>
                  <div class="book-stats">
                    <span><el-icon><View /></el-icon> {{ book.borrowCount }}</span>
                    <span><el-icon><Star /></el-icon> {{ book.followCount }}</span>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <div class="section">
          <div class="section-header">
            <h2>热门讨论</h2>
            <el-button text @click="$router.push('/discussion')">查看更多 <el-icon><ArrowRight /></el-icon></el-button>
          </div>
          <el-card v-for="item in discussions" :key="item.id" class="discussion-card" shadow="hover" @click="$router.push(`/discussion/${item.id}`)">
            <div class="discussion-content">
              <h3>{{ item.title }}</h3>
              <p>{{ item.content }}</p>
              <div class="discussion-meta">
                <el-tag size="small">{{ item.topic }}</el-tag>
                <span><el-icon><View /></el-icon> {{ item.viewCount }}</span>
                <span><el-icon><ChatDotRound /></el-icon> {{ item.replyCount }}</span>
              </div>
            </div>
          </el-card>
        </div>
      </el-col>

      <el-col :span="6">
        <el-card class="rank-card">
          <template #header>
            <div class="card-header">
              <span>活跃用户榜</span>
              <el-button text @click="$router.push('/rank')">更多</el-button>
            </div>
          </template>
          <div class="rank-list">
            <div class="rank-item" v-for="(user, index) in activeUsers" :key="user.id">
              <span class="rank-num" :class="'rank-' + (index + 1)">{{ index + 1 }}</span>
              <el-avatar :size="32" :src="user.avatar">{{ (user.nickname || user.username)?.[0] }}</el-avatar>
              <span class="user-name">{{ user.nickname || user.username }}</span>
              <span class="activity-score">{{ user.activityScore }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const hotBooks = ref([])
const discussions = ref([])
const activeUsers = ref([])

const loadData = async () => {
  try {
    const [booksRes, discussionsRes, usersRes] = await Promise.all([
      request.get('/rank/hot-books'),
      request.get('/discussion/list', { params: { page: 1, size: 5 } }),
      request.get('/rank/active-users')
    ])
    hotBooks.value = booksRes.data.slice(0, 4)
    discussions.value = discussionsRes.data.records.slice(0, 3)
    activeUsers.value = usersRes.data
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 80px 40px;
  border-radius: 16px;
  color: white;
  text-align: center;
  margin-bottom: 30px;
}

.banner h1 {
  font-size: 42px;
  margin-bottom: 20px;
  font-weight: 700;
}

.banner p {
  font-size: 20px;
  opacity: 0.9;
}

.section {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 24px;
  font-weight: 600;
}

.book-card {
  cursor: pointer;
  transition: transform 0.3s;
  margin-bottom: 20px;
}

.book-card:hover {
  transform: translateY(-5px);
}

.book-cover {
  width: 100%;
  height: 200px;
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

.book-info p {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
}

.book-stats {
  display: flex;
  gap: 15px;
  color: #999;
  font-size: 14px;
}

.book-stats span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.discussion-card {
  cursor: pointer;
  margin-bottom: 15px;
}

.discussion-content h3 {
  font-size: 18px;
  margin-bottom: 10px;
}

.discussion-content p {
  color: #666;
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
  align-items: center;
  color: #999;
  font-size: 14px;
}

.discussion-meta span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.rank-card {
  position: sticky;
  top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.rank-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rank-num {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #f0f0f0;
  font-weight: 600;
  font-size: 14px;
}

.rank-1 {
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  color: white;
}

.rank-2 {
  background: linear-gradient(135deg, #c0c0c0, #e8e8e8);
  color: white;
}

.rank-3 {
  background: linear-gradient(135deg, #cd7f32, #e8a87c);
  color: white;
}

.user-name {
  flex: 1;
  font-size: 14px;
}

.activity-score {
  color: #667eea;
  font-weight: 600;
  font-size: 14px;
}
</style>

