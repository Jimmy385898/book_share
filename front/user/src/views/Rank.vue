<template>
  <div class="rank">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <h2>热门图书榜</h2>
          </template>
          <div class="rank-list">
            <div class="rank-item" v-for="(book, index) in hotBooks" :key="book.id" @click="$router.push(`/book/${book.id}`)">
              <span class="rank-num" :class="'rank-' + (index + 1)">{{ index + 1 }}</span>
              <img :src="book.cover || '/default-book.png'" class="book-thumb" />
              <div class="book-info">
                <h4>{{ book.title }}</h4>
                <p>{{ book.author }}</p>
              </div>
              <span class="count">借阅 {{ book.borrowCount }} 次</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <h2>人气图书榜</h2>
          </template>
          <div class="rank-list">
            <div class="rank-item" v-for="(book, index) in popularBooks" :key="book.id" @click="$router.push(`/book/${book.id}`)">
              <span class="rank-num" :class="'rank-' + (index + 1)">{{ index + 1 }}</span>
              <img :src="book.cover || '/default-book.png'" class="book-thumb" />
              <div class="book-info">
                <h4>{{ book.title }}</h4>
                <p>{{ book.author }}</p>
              </div>
              <span class="count">关注 {{ book.followCount }} 人</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px">
      <template #header>
        <div class="header-with-tip">
          <h2>活跃用户榜</h2>
          <el-tooltip content="根据用户发布的书评和讨论数量计算活跃度" placement="top">
            <el-icon><QuestionFilled /></el-icon>
          </el-tooltip>
        </div>
      </template>
      <div class="user-rank-list">
        <div class="user-rank-item" v-for="(user, index) in activeUsers" :key="user.id">
          <span class="rank-num" :class="'rank-' + (index + 1)">{{ index + 1 }}</span>
          <el-avatar :size="50" :src="user.avatar">{{ (user.nickname || user.username)?.[0] }}</el-avatar>
          <div class="user-info">
            <h4>{{ user.nickname || user.username }}</h4>
            <p class="activity-score">活跃度：{{ user.activityScore }}</p>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { QuestionFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'

const hotBooks = ref([])
const popularBooks = ref([])
const activeUsers = ref([])

const loadData = async () => {
  try {
    const [hotRes, popularRes, usersRes] = await Promise.all([
      request.get('/rank/hot-books'),
      request.get('/rank/popular-books'),
      request.get('/rank/active-users')
    ])
    hotBooks.value = hotRes.data
    popularBooks.value = popularRes.data
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
.rank-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s;
}

.rank-item:hover {
  transform: translateX(5px);
}

.rank-num {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #f0f0f0;
  font-weight: 700;
  font-size: 16px;
  flex-shrink: 0;
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

.book-thumb {
  width: 60px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  flex-shrink: 0;
}

.book-info {
  flex: 1;
}

.book-info h4 {
  font-size: 16px;
  margin: 0 0 5px 0;
}

.book-info p {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.count {
  color: #667eea;
  font-weight: 600;
  font-size: 14px;
}

.user-rank-list {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
}

.user-rank-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
}

.user-info {
  text-align: center;
  width: 100%;
}

.user-info h4 {
  font-size: 14px;
  margin: 0 0 5px 0;
}

.activity-score {
  color: #667eea;
  font-size: 12px;
  font-weight: 600;
  margin: 0;
}

.user-info p {
  color: #666;
  font-size: 12px;
  margin: 0;
}

.header-with-tip {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-with-tip h2 {
  margin: 0;
}

.header-with-tip .el-icon {
  color: #909399;
  cursor: help;
}
</style>

