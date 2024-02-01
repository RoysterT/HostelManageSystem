import {createRouter, createWebHistory} from "vue-router";
import {unauthorized, takeAccessToken, isTokenExpired} from "@/net";
import {ElMessage} from "element-plus";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        // 未登录
        {
            path: '/',
            name: 'welcome',
            component: () => import('@/views/WelcomeView.vue'),
            children: [
                // 默认 - 登录
                {
                    path: '',
                    name: 'welcome-login',
                    component: () => import('@/views/welcome/LoginPage.vue')
                },
                // 注册
                {
                    path: '/register',
                    name: 'welcome-register',
                    component: () => import('@/views/welcome/RegisterPage.vue')
                },
                // 忘记密码
                {
                    path: '/reset',
                    name: 'welcome-reset',
                    component: () => import('@/views/welcome/ResetPage.vue')
                }
            ]
        },
        // 已登录
        {
            path: '/index',
            name: 'index',
            redirect: '/home',
            component: () => import('@/views/IndexView.vue'),
            children: [
                {
                    path: '/home',
                    name: 'index-home',
                    component: () => import('@/views/index/HomePage.vue')
                },
                // 用户信息
                {
                    path: '/user',
                    name: 'index-user',
                    component: () => import('@/views/index/UserView.vue'),
                    children: [
                        // 信息页
                        {
                            path: '/user/info',
                            name: 'index-user-info',
                            component: () => import('@/views/index/user/InfoPage.vue')
                        },
                        // 修改页
                        {
                            path: '/user/edit',
                            name: 'index-user-edit',
                            component: () => import('@/views/index/user/EditPage.vue')
                        },
                        // 修改页
                        {
                            path: '/user/changeEmail',
                            name: 'index-user-email',
                            component: () => import('@/views/index/user/ChangeEmail.vue')
                        },
                        // 修改页
                        {
                            path: '/user/changePhone',
                            name: 'index-user-phone',
                            component: () => import('@/views/index/user/ChangePhone.vue')
                        },
                        // 密码重置页
                        {
                            path: '/user/reset',
                            name: 'index-user-passwordReset',
                            component: () => import('@/views/index/user/PasswordResetPage.vue')
                        },
                        // 测试页
                        {
                            path: '/user/test',
                            name: 'index-user-test',
                            component: () => import('@/views/index/user/TestPage.vue')
                        }
                    ]
                },
                // 住宿管理
                {
                    path: '/accommodation',
                    name: 'index-accommodation',
                    component: () => import('@/views/index/AccommodationView.vue'),
                    children: [
                        // 住宿信息页
                        {
                            path: '/accommodation/info',
                            name: 'index-accommodation-info',
                            component: () => import('@/views/index/accommodation/InfoPage.vue')
                        },
                        // 换宿申请
                        {
                            path: '/accommodation/apply',
                            name: 'index-accommodation-apply',
                            component: () => import('@/views/index/accommodation/ApplyPage.vue')
                        },
                        // 换宿确认页面（楼栋管理员）
                        {
                            path: '/accommodation/audit',
                            name: 'index-accommodation-audit',
                            component: () => import('@/views/index/accommodation/AuditPage.vue')
                        },
                        // 换宿审核页（校方）
                        {
                            path: '/accommodation/confirm',
                            name: 'index-accommodation-confirm',
                            component: () => import('@/views/index/accommodation/ConfirmPage.vue')
                        },
                    ]
                },
                // 卫生管理
                {
                    path: '/inspect',
                    name: 'index-inspect',
                    component: () => import('@/views/index/InspectView.vue'),
                    children: [
                        {
                            path: '/inspect/rules',
                            name: 'index-inspect-rules',
                            component: () => import('@/views/index/inspect/RulesPage.vue')
                        },
                        {
                            path: '/inspect/score',
                            name: 'index-inspect-score',
                            component: () => import('@/views/index/inspect/ScorePage.vue')
                        },
                        {
                            path: '/inspect/mark',
                            name: 'index-inspect-mark',
                            component: () => import('@/views/index/inspect/MarkPage.vue')
                        },
                        {
                            path: '/inspect/activity',
                            name: 'index-inspect-activity',
                            component: () => import('@/views/index/inspect/ActivityPage.vue')
                        }
                    ]
                },
                // 宿舍报修
                {
                    path: '/maintenance',
                    name: 'index-maintenance',
                    component: () => import('@/views/index/MaintenanceView.vue'),
                    children: [
                        // 提交报修
                        {
                            path: '/maintenance/apply',
                            name: 'index-maintenance-apply',
                            component: () => import('@/views/index/maintenance/ApplyPage.vue')
                        },
                        // 报修历史
                        {
                            path: '/maintenance/history',
                            name: 'index-maintenance-history',
                            component: () => import('@/views/index/maintenance/HistoryPage.vue')
                        },
                        //详细页面
                        {
                            path: '/maintenance/detail/:repairOrder',
                            name: 'index-maintenance-detail',
                            component: () => import('@/views/index/maintenance/DetailPage.vue')
                        },
                        // 楼栋报修管理
                        {
                            path: '/maintenance/building',
                            name: 'index-maintenance-building',
                            component: () => import('@/views/index/maintenance/BuildingPage.vue')
                        },
                        // 报修管理
                        {
                            path: '/maintenance/manage',
                            name: 'index-maintenance-manage',
                            component: () => import('@/views/index/maintenance/ManagePage.vue')
                        }
                    ]
                },
                // 缴费服务
                {
                    path: '/payment',
                    name: 'index-payment',
                    component: () => import('@/views/index/PaymentView.vue'),
                    children: [
                        // 水电缴费
                        {
                            path: '/payment/bill',
                            name: 'index-payment-bill',
                            component: () => import('@/views/index/payment/BillPage.vue')
                        },
                        // 历史缴费
                        {
                            path: '/payment/history',
                            name: 'index-payment-history',
                            component: () => import('@/views/index/payment/HistoryPage.vue')
                        },
                        // 楼栋缴费信息
                        {
                            path: '/payment/building',
                            name: 'index-payment-building',
                            component: () => import('@/views/index/payment/BuildingPage.vue')
                        },
                        // 缴费管理
                        {
                            path: '/payment/manage',
                            name: 'index-payment-manage',
                            component: () => import('@/views/index/payment/ManagePage.vue')
                        }
                    ]
                },
                // 缴费服务
                {
                    path: '/water',
                    name: 'index-water',
                    component: () => import('@/views/index/WaterView.vue'),
                    children: [
                        // 水电缴费
                        {
                            path: '/water/bill',
                            name: 'index-water-bill',
                            component: () => import('@/views/index/water/BillPage.vue')
                        },
                        // 历史缴费
                        {
                            path: '/water/history',
                            name: 'index-water-history',
                            component: () => import('@/views/index/water/HistoryPage.vue')
                        },
                        // 楼栋缴费信息
                        {
                            path: '/water/sending',
                            name: 'index-water-sending',
                            component: () => import('@/views/index/water/SendingPage.vue')
                        },
                        // 缴费管理
                        {
                            path: '/water/manage',
                            name: 'index-water-manage',
                            component: () => import('@/views/index/water/ManagePage.vue')
                        }
                    ]
                },
                // 系统维护
                {
                    path: '/system',
                    name: 'index-system',
                    component: () => import('@/views/index/SystemView.vue'),
                    children: [
                        // 用户组权限设置
                        {
                            path: '/system/permission',
                            name: 'index-system-permission',
                            component: () => import('@/views/index/system/PermissionPage.vue')
                        },
                        // 用户所属组管理
                        {
                            path: '/system/assignment',
                            name: 'index-system-assignment',
                            component: () => import('@/views/index/system/AssignmentPage.vue')
                        }
                    ]
                },
                // 账户管理
                {
                    path: '/manage',
                    name: 'index-manage',
                    component: () => import('@/views/index/ManageView.vue'),
                    children: [
                        {
                            path: '/manage/account',
                            name: 'index-manage-account',
                            component: () => import('@/views/index/manage/AccountPage.vue')
                        },
                        {
                            path: '/manage/student',
                            name: 'index-manage-student',
                            component: () => import('@/views/index/manage/StudentPage.vue')
                        },
                        {
                            path: '/manage/staff',
                            name: 'index-manage-staff',
                            component: () => import('@/views/index/manage/StaffPage.vue')
                        },
                        {
                            path: '/manage/accommodation',
                            name: 'index-manage-accommodation',
                            component: () => import('@/views/index/manage/AccommodationPage.vue')
                        }
                    ]
                }
            ]
        },
        // 404 页面
        {
            path: '/:catchAll(.*)',
            name: 'not-found',
            component: () => import('@/views/NotFound.vue'),
        }
    ]
})

router.beforeEach((to, from, next) => {
    const isUnauthorized = unauthorized()
    if (to.name === undefined) {
        next({ name: 'not-found' });
    }
    // 已经登录就不能去登录、注册页面
    else if (to.name.startsWith('welcome-') && !isUnauthorized) {
        next('/index')
    }
    // 未登录就不能去不以welcome-开头的页面
    else if (!to.name.startsWith('welcome-') && isUnauthorized) {
        next('/')
    }
    else {
        next()
    }
})

export default router