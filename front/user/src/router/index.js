import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/AdminLogin.vue')
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue')
      },
      {
        path: 'books',
        name: 'Books',
        component: () => import('@/views/Books.vue')
      },
      {
        path: 'book/:id',
        name: 'BookDetail',
        component: () => import('@/views/BookDetail.vue')
      },
      {
        path: 'publish',
        name: 'PublishBook',
        component: () => import('@/views/PublishBook.vue')
      },
      {
        path: 'book/edit/:id',
        name: 'EditBook',
        component: () => import('@/views/PublishBook.vue')
      },
      {
        path: 'my-books',
        name: 'MyBooks',
        component: () => import('@/views/MyBooks.vue')
      },
      {
        path: 'my-books/:id',
        name: 'MyBookDetail',
        component: () => import('@/views/MyBookDetail.vue')
      },
      {
        path: 'borrow',
        name: 'MyBorrow',
        component: () => import('@/views/MyBorrow.vue')
      },
      {
        path: 'discussion',
        name: 'Discussion',
        component: () => import('@/views/Discussion.vue')
      },
      {
        path: 'discussion/:id',
        name: 'DiscussionDetail',
        component: () => import('@/views/DiscussionDetail.vue')
      },
      {
        path: 'rank',
        name: 'Rank',
        component: () => import('@/views/Rank.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue')
      },
      {
        path: 'change-password',
        name: 'UserChangePassword',
        component: () => import('@/views/UserChangePassword.vue')
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layout/admin/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue')
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/Users.vue')
      },
      {
        path: 'books',
        name: 'AdminBooks',
        component: () => import('@/views/admin/AdminBooks.vue')
      },
      {
        path: 'borrows',
        name: 'AdminBorrows',
        component: () => import('@/views/admin/Borrows.vue')
      },
      {
        path: 'reviews',
        name: 'AdminReviews',
        component: () => import('@/views/admin/Reviews.vue')
      },
      {
        path: 'discussions',
        name: 'AdminDiscussions',
        component: () => import('@/views/admin/Discussions.vue')
      },
      {
        path: 'change-password',
        name: 'AdminChangePassword',
        component: () => import('@/views/admin/AdminChangePassword.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token
  
  let userInfo = {}
  try {
    const storedUserInfo = localStorage.getItem('userInfo')
    if (storedUserInfo) {
      userInfo = JSON.parse(storedUserInfo)
    }
  } catch (e) {
    console.error('解析 userInfo 失败:', e)
  }
  
  console.log('路由守卫 - 目标路径:', to.path)
  console.log('路由守卫 - token:', token)
  console.log('路由守卫 - userInfo:', userInfo)
  console.log('路由守卫 - role:', userInfo?.role)
  
  if (to.path.startsWith('/admin') && to.path !== '/admin/login') {
    if (!token) {
      next('/admin/login')
    } else if (userInfo?.role !== 'admin') {
      next('/admin/login')
    } else {
      next()
    }
  } else if (!token && to.path !== '/login' && to.path !== '/register' && to.path !== '/admin/login') {
    next('/login')
  } else if (token && to.path === '/login') {
    if (userInfo?.role === 'admin') {
      next('/admin/dashboard')
    } else {
      next('/home')
    }
  } else if (token && to.path === '/admin/login') {
    if (userInfo?.role === 'admin') {
      next('/admin/dashboard')
    } else {
      next('/login')
    }
  } else {
    next()
  }
})

export default router
