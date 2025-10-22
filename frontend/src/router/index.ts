import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw, RouteMeta } from 'vue-router'
import { useAppStore } from '@/stores/app'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// 配置NProgress
NProgress.configure({
  showSpinner: false,
  minimum: 0.2,
  easing: 'ease',
  speed: 500
})

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomeView.vue'),
    meta: {
      title: '首页',
      icon: 'House',
      keepAlive: true,
      transition: 'fade'
    }
  },
  {
    path: '/game',
    name: 'Game',
    component: () => import('@/views/GameView.vue'),
    meta: {
      title: '游戏',
      icon: 'Trophy',
      keepAlive: true,
      requiresAuth: true,
      transition: 'slide-left'
    }
  },
  {
    path: '/game/:sessionId',
    name: 'GameSession',
    component: () => import('@/views/GameSessionView.vue'),
    meta: {
      title: '游戏会话',
      icon: 'Trophy',
      keepAlive: false,
      requiresAuth: true,
      transition: 'slide-left'
    },
    props: true
  },
  {
    path: '/leaderboard',
    name: 'Leaderboard',
    component: () => import('@/views/LeaderboardView.vue'),
    meta: {
      title: '排行榜',
      icon: 'TrendCharts',
      keepAlive: true,
      transition: 'slide-right'
    }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/ProfileView.vue'),
    meta: {
      title: '个人中心',
      icon: 'User',
      keepAlive: true,
      requiresAuth: true,
      transition: 'slide-right'
    }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/SettingsView.vue'),
    meta: {
      title: '设置',
      icon: 'Setting',
      keepAlive: true,
      transition: 'fade'
    }
  },
  {
    path: '/about',
    name: 'About',
    component: () => import('@/views/AboutView.vue'),
    meta: {
      title: '关于',
      icon: 'InfoFilled',
      keepAlive: true,
      transition: 'fade'
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: {
      title: '登录',
      icon: 'Key',
      hidden: true,
      layout: 'auth',
      transition: 'fade'
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: {
      title: '注册',
      icon: 'UserFilled',
      hidden: true,
      layout: 'auth',
      transition: 'fade'
    }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/NotFoundView.vue'),
    meta: {
      title: '页面未找到',
      hidden: true,
      layout: 'error',
      transition: 'fade'
    }
  },
  {
    path: '/500',
    name: 'ServerError',
    component: () => import('@/views/error/ServerErrorView.vue'),
    meta: {
      title: '服务器错误',
      hidden: true,
      layout: 'error',
      transition: 'fade'
    }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else if (to.hash) {
      return {
        el: to.hash,
        behavior: 'smooth'
      }
    } else {
      return { top: 0 }
    }
  }
})

// 全局前置守卫
router.beforeEach(async (to, from, next) => {
  // 开始进度条
  NProgress.start()
  
  const appStore = useAppStore()
  
  try {
    console.log('Route guard:', {
      to: to.name,
      from: from.name,
      isAuthenticated: appStore.isAuthenticated,
      currentUser: appStore.currentUser?.username
    })
    
    // 设置页面标题
    const title = to.meta?.title as string
    if (title) {
      document.title = `${title} - ${appStore.appInfo.name}`
    } else {
      document.title = appStore.appInfo.name
    }
    
    // 检查认证要求
    if (to.meta?.requiresAuth && !appStore.isAuthenticated) {
      console.log('Auth required but not authenticated, redirecting to login')
      // 保存目标路由，登录后跳转
      sessionStorage.setItem('redirect-after-login', to.fullPath)
      next('/login')
      return
    }
    
    // 检查角色权限
    if (to.meta?.roles && appStore.currentUser) {
      const userRoles = appStore.currentUser.roles || []
      const requiredRoles = to.meta.roles as string[]
      const hasPermission = requiredRoles.some(role => userRoles.includes(role))
      
      if (!hasPermission) {
        appStore.setError('您没有访问此页面的权限')
        next('/403')
        return
      }
    }
    
    // 如果已登录且访问登录页，重定向到首页
    if ((to.name === 'Login' || to.name === 'Register') && appStore.isAuthenticated) {
      console.log('Already authenticated, redirecting from login to home')
      next('/')
      return
    }
    
    console.log('Route guard passed, proceeding to:', to.name)
    next()
  } catch (error) {
    console.error('Route guard error:', error)
    appStore.setError('页面加载失败')
    next('/500')
  }
})

// 全局后置守卫
router.afterEach((to, from, failure) => {
  // 结束进度条
  NProgress.done()
  
  if (failure) {
    console.error('Route navigation failed:', failure)
    const appStore = useAppStore()
    appStore.setError('页面导航失败')
  }
  
  // 记录页面访问
  if (import.meta.env.DEV) {
    console.log(`Route changed: ${from.path} -> ${to.path}`)
  }
})

// 路由错误处理
router.onError((error) => {
  console.error('Router error:', error)
  const appStore = useAppStore()
  appStore.setError('路由错误', error)
  NProgress.done()
})

export default router

// 导出路由工具函数
export function getRoutesByRole(roles: string[] = []) {
  return routes.filter(route => {
    const routeRoles = route.meta?.roles as string[]
    if (!routeRoles) return true
    return routeRoles.some(role => roles.includes(role))
  })
}

export function getVisibleRoutes() {
  return routes.filter(route => !route.meta?.hidden)
}

export function findRouteByName(name: string) {
  return routes.find(route => route.name === name)
}

export function generateBreadcrumbs(route: any) {
  const breadcrumbs = []
  let currentRoute = route
  
  while (currentRoute) {
    if (currentRoute.meta?.title) {
      breadcrumbs.unshift({
        name: currentRoute.name,
        title: currentRoute.meta.title,
        path: currentRoute.path
      })
    }
    currentRoute = currentRoute.parent
  }
  
  return breadcrumbs
}