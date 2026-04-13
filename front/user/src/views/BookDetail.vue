<template>
  <div class="book-detail" v-loading="loading">
    <el-row :gutter="30" v-if="book.id">
      <el-col :span="8">
        <img :src="book.cover || '/default-book.png'" class="book-cover" />
        <div class="actions">
          <el-button type="primary" size="large" :disabled="book.status !== 1" @click="showBorrowDialog = true">
            {{ book.status === 1 ? '申请借阅' : '已借出' }}
          </el-button>
          <el-button size="large" @click="handleFollow">
            <el-icon><Star /></el-icon>
            {{ hasFollowed ? '取消关注' : '关注' }}
          </el-button>
        </div>
      </el-col>

      <el-col :span="16">
        <h1 class="book-title">{{ book.title }}</h1>
        <div class="book-meta">
          <div class="meta-item">
            <span class="label">作者：</span>
            <span>{{ book.author }}</span>
          </div>
          <div class="meta-item">
            <span class="label">出版社：</span>
            <span>{{ book.publisher || '-' }}</span>
          </div>
          <div class="meta-item">
            <span class="label">ISBN：</span>
            <span>{{ book.isbn || '-' }}</span>
          </div>
          <div class="meta-item">
            <span class="label">分类：</span>
            <el-tag>{{ book.category }}</el-tag>
          </div>
          <div class="meta-item">
            <span class="label">图书状态：</span>
            <el-tag :type="book.status === 1 ? 'success' : 'info'">
              {{ book.status === 1 ? '可借阅' : '已借出' }}
            </el-tag>
          </div>
          <div class="meta-item">
            <span class="label">借阅期限：</span>
            <span>{{ book.borrowDays }} 天</span>
          </div>
          <div class="meta-item">
            <span class="label">取书地址：</span>
            <span>{{ book.pickupAddress }}</span>
          </div>
        </div>

        <el-divider />

        <div class="book-description">
          <h3>图书简介</h3>
          <p>{{ book.description || '暂无简介' }}</p>
        </div>

        <el-divider />

        <el-tabs v-model="activeTab">
          <el-tab-pane label="书评" name="review">
            <div class="review-section">
              <el-button type="primary" @click="showReviewDialog = true">发表书评</el-button>
              <div class="review-list">
                <div class="review-item" v-for="review in reviews" :key="review.id">
                  <div class="review-user">
                    <el-avatar :size="40" :src="review.avatar">{{ (review.nickname || review.username)?.[0] }}</el-avatar>
                    <div class="user-detail">
                      <span class="username">{{ review.nickname || review.username }}</span>
                      <el-rate v-model="review.rating" disabled size="small" />
                    </div>
                    <span class="review-time">{{ review.createTime }}</span>
                  </div>
                  <p class="review-content">{{ review.content }}</p>
                  <div class="review-actions">
                    <el-button text @click="likeReview(review.id)">
                      <el-icon><Star /></el-icon> {{ review.likeCount }}
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="讨论" name="discussion">
            <div class="discussion-section">
              <el-button type="primary" @click="$router.push('/discussion')">发起讨论</el-button>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>

    <el-dialog v-model="showBorrowDialog" title="申请借阅" width="500px">
      <el-form :model="borrowForm" label-width="100px">
        <el-form-item label="借阅天数">
          <el-input-number v-model="borrowForm.borrowDays" :min="1" :max="book.borrowDays" />
        </el-form-item>
        <el-form-item label="交接方式">
          <el-radio-group v-model="borrowForm.deliveryMethod">
            <el-radio label="face">面对面</el-radio>
            <el-radio label="pickup">代收点</el-radio>
            <el-radio label="express">快递</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="borrowForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBorrowDialog = false">取消</el-button>
        <el-button type="primary" @click="handleBorrow">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showReviewDialog" title="发表书评" width="500px">
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.rating" />
        </el-form-item>
        <el-form-item label="书评内容">
          <el-input v-model="reviewForm.content" type="textarea" :rows="5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReviewDialog = false">取消</el-button>
        <el-button type="primary" @click="handlePublishReview">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const book = ref({})
const hasFollowed = ref(false)
const activeTab = ref('review')
const reviews = ref([])
const showBorrowDialog = ref(false)
const showReviewDialog = ref(false)

const borrowForm = reactive({
  bookId: null,
  borrowDays: 30,
  deliveryMethod: 'face',
  remark: ''
})

const reviewForm = reactive({
  bookId: null,
  rating: 5,
  content: ''
})

const loadBookDetail = async () => {
  loading.value = true
  try {
    const res = await request.get(`/book/${route.params.id}`)
    book.value = res.data
    borrowForm.bookId = res.data.id
    borrowForm.borrowDays = res.data.borrowDays
    reviewForm.bookId = res.data.id
    
    const followRes = await request.get(`/book/follow/status/${route.params.id}`)
    hasFollowed.value = followRes.data
    
    loadReviews()
  } catch (error) {
    console.error(error)
    router.push('/books')
  } finally {
    loading.value = false
  }
}

const loadReviews = async () => {
  try {
    const res = await request.get('/review/list', {
      params: { bookId: route.params.id, page: 1, size: 10 }
    })
    reviews.value = res.data.records
  } catch (error) {
    console.error(error)
  }
}

const handleFollow = async () => {
  try {
    await request.post(`/book/follow/${route.params.id}`)
    hasFollowed.value = !hasFollowed.value
    ElMessage.success(hasFollowed.value ? '关注成功' : '取消关注')
  } catch (error) {
    console.error(error)
  }
}

const handleBorrow = async () => {
  try {
    await request.post('/borrow/apply', borrowForm)
    ElMessage.success('申请成功，等待出借方确认')
    showBorrowDialog.value = false
  } catch (error) {
    console.error(error)
  }
}

const handlePublishReview = async () => {
  try {
    await request.post('/review/publish', reviewForm)
    ElMessage.success('发布成功，等待审核')
    showReviewDialog.value = false
    reviewForm.content = ''
    reviewForm.rating = 5
  } catch (error) {
    console.error(error)
  }
}

const likeReview = async (id) => {
  try {
    await request.post(`/review/like/${id}`)
    loadReviews()
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadBookDetail()
})
</script>

<style scoped>
.book-cover {
  width: 100%;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.actions {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.book-title {
  font-size: 32px;
  margin-bottom: 20px;
}

.book-meta {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.meta-item {
  display: flex;
  align-items: center;
  font-size: 16px;
}

.label {
  color: #666;
  margin-right: 10px;
  min-width: 80px;
}

.book-description {
  margin: 20px 0;
}

.book-description h3 {
  font-size: 20px;
  margin-bottom: 15px;
}

.book-description p {
  line-height: 1.8;
  color: #666;
}

.review-section {
  padding: 20px 0;
}

.review-list {
  margin-top: 20px;
}

.review-item {
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 15px;
}

.review-user {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 15px;
}

.user-detail {
  display: flex;
  flex-direction: column;
  gap: 5px;
  flex: 1;
}

.username {
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

.review-time {
  color: #999;
  font-size: 14px;
  margin-left: auto;
}

.review-content {
  line-height: 1.6;
  color: #333;
  margin-bottom: 10px;
}

.review-actions {
  display: flex;
  gap: 10px;
}
</style>
