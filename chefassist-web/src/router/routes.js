
const routes = [
  {
    path: '/',
    component: () => import('layouts/MyLayout.vue'),
    children: [
      { path: '', component: () => import('pages/Index.vue') },
      { path: '/login', component: () => import('pages/Login.vue') },
      { path: '/change-pass', component: () => import('pages/Changepass.vue') },
      { path: '/user-manage', component: () => import('pages/Usermanage.vue') },
      { path: '/kitchen-manage', component: () => import('pages/Kitchenmanage.vue') },
      { path: '/table-manage', component: () => import('pages/Tablemanage.vue') },
      { path: '/dish-manage', component: () => import('pages/Dishmanage.vue') },
      { path: '/report-manage', component: () => import('pages/Reportmanage.vue') },
      { path: '/report-by-table', component: () => import('pages/Reportbytable.vue') },
      { path: '/history-order-by-table/:id', component: () => import('pages/Historyorderbytable.vue') },
      { path: '/monitor-manage', component: () => import('pages/Monitormanage.vue') },
      { path: '/new-dish', component: () => import('pages/Newdish.vue') },
      { path: '/edit-dish/:id', component: () => import('pages/Editdish.vue') },
      { path: '/order-manage', component: () => import('pages/Ordermanage.vue') },
      { path: '/order-detail/:id', component: () => import('pages/Orderdetail.vue') },
      { path: '/order-detail-by-table/:id', component: () => import('pages/OrderdetailByTable.vue') },
      { path: '/order-by-table', component: () => import('pages/OrderByTable.vue') },
      { path: '/add-dish-item-4-order/:id', component: () => import('pages/Adddishitem4order.vue') },
      { path: '/chef-assignment-board', component: () => import('pages/ChefAssignmentBoard.vue') }
    ]
  }
]

// Always leave this as last one
if (process.env.MODE !== 'ssr') {
  routes.push({
    path: '*',
    component: () => import('pages/Error404.vue')
  })
}

export default routes
