import AuthView from '@/views/AuthView.vue'
import HomeView from '@/views/HomeView.vue'
import RegistrationView from '@/views/RegistrationView.vue'
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView,
    meta: {
      title: 'Главная страница'
    }
  },
  {
    path: '/auth',
    name: 'auth',
    component: AuthView,
    meta: {
      title: 'Авторизация'
    }
  },
  {
    path: '/reg',
    name: 'reg',
    component: RegistrationView,
    meta: {
      title: 'Регистрация'
    }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    return savedPosition || { top: 0 }
  }
})

router.beforeEach((to) => {
  document.title = to.meta.title || 'BIBLCHECKER'
})

export default router