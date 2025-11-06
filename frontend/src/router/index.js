import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
    {
        path: '/',
        name: 'Home',
        component: () => import('@/views/HomePage.vue')
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/LoginPage.vue')
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/RegisterPage.vue')
    },
    {
        path: '/forgot-password',
        name: 'ForgotPassword',
        component: () => import('@/views/ForgotPasswordPage.vue')
    },
    {
        path: '/reset-password',
        name: 'ResetPassword',
        component: () => import('@/views/ResetPasswordPage.vue')
    },
    {
        path: '/premium',
        name: 'Premium',
        component: () => import('@/views/PremiumPage.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/payment/callback',
        name: 'PaymentCallback',
        component: () => import('@/views/PaymentCallbackPage.vue')
    },
    {
        path: '/documents',
        name: 'Documents',
        component: () => import('@/views/DocumentsPage.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/ProfilePage.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/transactions',
        name: 'Transactions',
        component: () => import('@/views/TransactionsPage.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/admin',
        component: () => import('@/views/admin/AdminLayout.vue'),
        meta: { requiresAuth: true, requiresAdmin: true },
        children: [
            {
                path: '',
                name: 'AdminDashboard',
                component: () => import('@/views/admin/DashboardPage.vue')
            },
            {
                path: 'users',
                name: 'AdminUsers',
                component: () => import('@/views/admin/UsersPage.vue')
            },
            {
                path: 'knowledge',
                name: 'AdminKnowledge',
                component: () => import('@/views/admin/KnowledgePage.vue')
            },
            {
                path: 'revenue',
                name: 'AdminRevenue',
                component: () => import('@/views/admin/RevenuePage.vue')
            },
            {
                path: 'analytics',
                name: 'AdminAnalytics',
                component: () => import('@/views/admin/AnalyticsPage.vue')
            },
            {
                path: 'emails',
                name: 'AdminEmails',
                component: () => import('@/views/admin/EmailsPage.vue')
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    const authStore = useAuthStore()

    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
        next('/login')
    } else if (to.meta.requiresAdmin && !authStore.isAdmin) {
        next('/')
    } else {
        next()
    }
})

export default router