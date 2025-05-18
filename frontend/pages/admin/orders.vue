<template>
  <div class="admin-orders-container">
    <div class="page-header">
      <h1>订单管理</h1>
    </div>
    
    <div v-if="loading" class="loading">
      <el-skeleton style="width: 100%" :rows="5" animated />
    </div>
    
    <div v-else-if="!userStore.isLoggedIn || userStore.getUserRole.toLowerCase() !== 'admin'" class="login-required">
      <el-empty description="您需要管理员权限才能访问此页面">
        <template #extra>
          <el-button type="primary" @click="router.push('/login')">去登录</el-button>
        </template>
      </el-empty>
    </div>
    
    <div v-else>
      <!-- 筛选栏 -->
      <div class="filter-bar">
        <el-form :inline="true" :model="filterForm" class="order-filter">
          <el-form-item label="订单状态">
            <el-select v-model="filterForm.status" placeholder="全部" clearable>
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD HH:mm:ss"
              :default-time="['00:00:00', '23:59:59']"
            />
          </el-form-item>
          
          <el-form-item label="关键词">
            <el-input v-model="filterForm.keyword" placeholder="订单号/用户名" />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 订单表格 -->
      <el-table :data="orders" style="width: 100%" v-loading="loading" border stripe>
        <el-table-column prop="id" label="订单号" min-width="10" />
        <el-table-column label="用户信息" min-width="12">
          <template #default="scope">
            <div class="user-info">
              <span>{{ scope.row.user?.username || '未知用户' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" min-width="10">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
            <el-tag v-if="scope.row.isDeleted" type="info" size="small" style="margin-left: 5px">已删除</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="商品信息" min-width="30">
          <template #default="scope">
            <div class="book-tags">
              <el-tag 
                v-for="item in scope.row.orderItems" 
                :key="item.id"
                size="default"
                :type="tagTypes[item.id % tagTypes.length]"
                class="tag-item"
                @click="item.book && item.book.id ? goToBook(item.book.id) : null"
              >
                {{ item.book?.title || '未知商品' }} x{{ item.quantity }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总金额" min-width="10">
          <template #default="scope">
            <span class="price">￥{{ scope.row.totalAmount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" min-width="15">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="20">
          <template #default="scope">
            <div class="action-buttons">
              <el-button type="primary" size="small" @click="goToOrderDetail(scope.row.id)">
                订单详情
              </el-button>
              
              <!-- 已支付待发货 -->
              <template v-if="scope.row.status === 'PAID'">
                <el-button type="success" size="small" @click="completeOrder(scope.row.id)">
                  完成订单
                </el-button>
              </template>
              
<!--              &lt;!&ndash; 已发货待收货 &ndash;&gt;-->
<!--              <template v-if="scope.row.status === 'SHIPPED'">-->
<!--                <el-button type="success" size="small" @click="deliverOrder(scope.row.id)">-->
<!--                  确认送达-->
<!--                </el-button>-->
<!--              </template>-->
<!--              -->
<!--              &lt;!&ndash; 待收货 &ndash;&gt;-->
<!--              <template v-if="scope.row.status === 'DELIVERED'">-->
<!--                <el-button type="success" size="small" @click="completeOrder(scope.row.id)">-->
<!--                  完成订单-->
<!--                </el-button>-->
<!--              </template>-->
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :current-page="currentPage"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>
    
    <!-- 操作确认对话框 -->
    <el-dialog
      v-model="showActionDialog"
      :title="actionDialogTitle"
      width="30%"
      :close-on-click-modal="false"
    >
      <div class="action-dialog-content">
        <p>订单号：{{ currentOrder?.id }}</p>
        <p>用户：{{ currentOrder?.user?.username || '未知用户' }}</p>
        <p>金额：<span class="total-price">￥{{ currentOrder?.totalAmount?.toFixed(2) }}</span></p>
        
        <template v-if="actionType === 'ship'">
          <el-form :model="shipForm" label-width="80px">
            <el-form-item label="快递公司">
              <el-select v-model="shipForm.company" placeholder="请选择快递公司">
                <el-option label="顺丰速运" value="SF" />
                <el-option label="中通快递" value="ZTO" />
                <el-option label="圆通速递" value="YTO" />
                <el-option label="申通快递" value="STO" />
                <el-option label="韵达速递" value="YD" />
                <el-option label="京东物流" value="JD" />
              </el-select>
            </el-form-item>
            <el-form-item label="快递单号">
              <el-input v-model="shipForm.trackingNumber" placeholder="请输入快递单号" />
            </el-form-item>
          </el-form>
        </template>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showActionDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmAction" :loading="actionLoading">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate as formatDateUtil } from '../../utils/dateUtils'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const orders = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const baseUrl = ref(process.env.BASE_URL || 'http://localhost:8080')
const tagTypes = ['', 'success', 'warning', 'danger', 'info']

// 当前选中的订单
const currentOrder = ref(null)

// 操作对话框
const showActionDialog = ref(false)
const actionDialogTitle = ref('')
const actionType = ref('')
const actionLoading = ref(false)

// 发货表单
const shipForm = reactive({
  company: '',
  trackingNumber: ''
})

// 订单状态选项
const statusOptions = [
  { value: 'PENDING', label: '待付款' },
  { value: 'PAID', label: '待发货' },
  { value: 'SHIPPED', label: '已发货' },
  { value: 'DELIVERED', label: '待收货' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'CANCELLED', label: '已取消' }
]

// 筛选表单
const filterForm = reactive({
  status: '',
  dateRange: null,
  keyword: ''
})

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  return formatDateUtil(dateString, 'YYYY-MM-DD HH:mm:ss')
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待付款',
    'PAID': '待发货',
    'SHIPPED': '已发货',
    'DELIVERED': '待收货',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || '未知状态'
}

// 获取状态样式类型
const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'PAID': 'info',
    'SHIPPED': 'primary',
    'DELIVERED': 'success',
    'COMPLETED': 'success',
    'CANCELLED': 'info'
  }
  return typeMap[status] || ''
}

// 获取订单列表
const fetchOrders = async () => {
  if (!userStore.isLoggedIn) {
    // 尝试初始化用户状态
    userStore.initUserFromStorage()
    // 检查会话状态
    const isLoggedIn = await userStore.checkSession()
    if (!isLoggedIn) {
      loading.value = false
      return
    }
  }
  
  // 检查是否为管理员
  if (userStore.getUserRole.toLowerCase() !== 'admin') {
    ElMessage.error('您没有管理员权限')
    loading.value = false
    return
  }
  
  loading.value = true
  try {
    let url = `${baseUrl.value}/api/order/admin/search?page=${currentPage.value}&pageSize=${pageSize.value}`
    
    if (filterForm.status) {
      url += `&status=${filterForm.status}`
    }
    
    if (filterForm.keyword) {
      url += `&keyword=${encodeURIComponent(filterForm.keyword)}`
    }
    
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      url += `&start=${encodeURIComponent(filterForm.dateRange[0])}&end=${encodeURIComponent(filterForm.dateRange[1])}`
    }
    
    const response = await fetch(url, {
      credentials: 'include',
      headers: userStore.getAuthHeaders()
    })
    
    if (response.status === 401 || response.status === 403) {
      ElMessage.error('登录已过期或权限不足，请重新登录')
      userStore.clearUser()
      setTimeout(() => {
        router.push('/login')
      }, 1500)
      return
    }
    
    const result = await response.json()
    if (result.code === 200) {
      orders.value = result.data.records || []
      total.value = result.data.total || 0
    } else {
      ElMessage.error(result.message || '获取订单列表失败')
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchOrders()
}

// 重置筛选
const resetFilter = () => {
  filterForm.status = ''
  filterForm.dateRange = null
  filterForm.keyword = ''
  handleSearch()
}

// 页码变化
const handlePageChange = (page) => {
  currentPage.value = page
  fetchOrders()
}

// 页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchOrders()
}

// 跳转到订单详情
const goToOrderDetail = (orderId) => {
  router.push(`/orders/${orderId}`)
}

// 跳转到商品详情
const goToBook = (bookId) => {
  router.push(`/books/${bookId}`)
}

// 发货
const shipOrder = (orderId) => {
  const order = orders.value.find(o => o.id === orderId)
  if (!order) return
  
  currentOrder.value = order
  actionType.value = 'ship'
  actionDialogTitle.value = '确认发货'
  showActionDialog.value = true
  
  // 重置表单
  shipForm.company = 'SF'
  shipForm.trackingNumber = ''
}

// 确认送达
const deliverOrder = (orderId) => {
  const order = orders.value.find(o => o.id === orderId)
  if (!order) return
  
  currentOrder.value = order
  actionType.value = 'deliver'
  actionDialogTitle.value = '确认订单已送达'
  showActionDialog.value = true
}

// 完成订单
const completeOrder = (orderId) => {
  const order = orders.value.find(o => o.id === orderId)
  if (!order) return
  
  currentOrder.value = order
  actionType.value = 'complete'
  actionDialogTitle.value = '确认完成订单'
  showActionDialog.value = true
}

// 确认操作
const confirmAction = async () => {
  if (!currentOrder.value) return
  
  actionLoading.value = true
  try {
    let url = ''
    let method = 'POST'
    let body = {}
    
    switch (actionType.value) {
      case 'ship':
        url = `${baseUrl.value}/api/order/${currentOrder.value.id}/ship`
        body = {
          company: shipForm.company,
          trackingNumber: shipForm.trackingNumber
        }
        break
      case 'deliver':
        url = `${baseUrl.value}/api/order/${currentOrder.value.id}/deliver`
        break
      case 'complete':
        url = `${baseUrl.value}/api/order/${currentOrder.value.id}/complete`
        break
      default:
        ElMessage.error('未知操作类型')
        actionLoading.value = false
        return
    }
    
    const response = await fetch(url, {
      method,
      credentials: 'include',
      headers: {
        ...userStore.getAuthHeaders(),
        'Content-Type': 'application/json'
      },
      body: Object.keys(body).length > 0 ? JSON.stringify(body) : undefined
    })
    
    if (response.status === 401 || response.status === 403) {
      ElMessage.error('登录已过期或权限不足，请重新登录')
      userStore.clearUser()
      setTimeout(() => {
        router.push('/login')
      }, 1500)
      return
    }
    
    const result = await response.json()
    if (result.code === 200) {
      ElMessage.success('操作成功')
      showActionDialog.value = false
      fetchOrders() // 刷新订单列表
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败，请稍后再试')
  } finally {
    actionLoading.value = false
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.admin-orders-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-size: 16px;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 29px;
  color: #333;
  margin: 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.loading, .empty-orders, .login-required {
  margin: 40px 0;
  text-align: center;
}

.filter-bar {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

/* 表格内容字体调整 */
:deep(.el-table) {
  font-size: 15px;
}

:deep(.el-table th) {
  font-size: 16px;
}

.book-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  font-size: 14px;
  cursor: pointer;
}

.tag-item:hover {
  opacity: 0.85;
}

.price {
  color: #ff6700;
  font-weight: bold;
  font-size: 16px;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

/* 按钮字体大小调整 */
:deep(.el-button) {
  font-size: 14px;
}

:deep(.el-button--small) {
  font-size: 13px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 分页组件字体调整 */
:deep(.el-pagination) {
  font-size: 15px;
}

.action-dialog-content {
  text-align: center;
  font-size: 16px;
}

.total-price {
  color: #ff6700;
  font-weight: bold;
  font-size: 22px;
}

/* 表单控件字体大小调整 */
:deep(.el-form-item__label) {
  font-size: 15px;
}

:deep(.el-input__inner),
:deep(.el-select__input),
:deep(.el-select-dropdown__item) {
  font-size: 15px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style> 