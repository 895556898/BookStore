<template>
  <div class="orders-container">
    <div class="page-header">
      <h1>我的订单</h1>
    </div>
    
    <div v-if="loading" class="loading">
      <el-skeleton style="width: 100%" :rows="5" animated />
    </div>
    
    <div v-else-if="!userStore.isLoggedIn" class="login-required">
      <el-empty description="请登录后查看订单">
        <template #extra>
          <el-button type="primary" @click="router.push('/login')">去登录</el-button>
        </template>
      </el-empty>
    </div>
    
    <div v-else-if="orders.length === 0" class="empty-orders">
      <el-empty description="暂无订单记录">
        <template #extra>
          <el-button type="primary" @click="router.push('/')">去购物</el-button>
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
          
          <el-form-item label="关键词">
            <el-input v-model="filterForm.keyword" placeholder="订单号/商品名称" />
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
        <el-table-column label="订单状态" min-width="10">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
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
                @click="goToBook(item.book.id)"
              >
                {{ item.book.title }}
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
              
              <template v-if="scope.row.status === 'PENDING'">
                <el-button type="info" size="small" @click="cancelOrder(scope.row.id)">
                  取消订单
                </el-button>
              </template>
              
              <template v-if="scope.row.status === 'COMPLETED'">
                <el-button type="danger" plain size="small" @click="deleteOrder(scope.row.id)">
                  删除订单
                </el-button>
              </template>
              
              <template v-if="scope.row.status === 'CANCELLED'">
                <el-button type="danger" plain size="small" @click="deleteOrder(scope.row.id)">
                  删除订单
                </el-button>
              </template>
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
    
    <!-- 支付对话框 -->
    <el-dialog
      v-model="showPaymentDialog"
      title="订单支付"
      width="30%"
      :close-on-click-modal="false"
    >
      <div class="payment-dialog-content">
        <p>订单号：{{ currentOrder?.id }}</p>
        <p>支付金额：<span class="total-price">￥{{ currentOrder?.totalAmount.toFixed(2) }}</span></p>
        <p>支付方式：{{ getPaymentMethodText(currentOrder?.paymentMethod) }}</p>
        
        <div class="payment-qr-code">
          <p>请使用{{ getPaymentMethodText(currentOrder?.paymentMethod) }}扫描下方二维码完成支付</p>
          <div class="qr-code">
            <el-image src="/qrcode-demo.jpg" style="width: 200px; height: 200px;"></el-image>
          </div>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showPaymentDialog = false">取消支付</el-button>
          <el-button type="primary" @click="confirmPayment">确认已支付</el-button>
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
const showPaymentDialog = ref(false)
const currentOrder = ref(null)
const baseUrl = ref(process.env.BASE_URL || 'http://localhost:8080')
const tagTypes = ['', 'success', 'warning', 'danger', 'info']

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

// 获取支付方式文本
const getPaymentMethodText = (method) => {
  const methods = {
    'WECHAT': '微信支付',
    'ALIPAY': '支付宝',
    'BANK_CARD': '银行卡',
    'CASH': '货到付款'
  }
  return methods[method] || '未知支付方式'
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
  
  loading.value = true
  try {
    let url = `${baseUrl.value}/api/order/search?page=${currentPage.value}&pageSize=${pageSize.value}`
    
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

// 支付订单
const payOrder = (order) => {
  currentOrder.value = order
  showPaymentDialog.value = true
}

// 确认支付
const confirmPayment = async () => {
  if (!currentOrder.value) return
  
  try {
    const response = await fetch(`${baseUrl.value}/api/order/${currentOrder.value.id}/pay?paymentMethod=${currentOrder.value.paymentMethod || 'ALIPAY'}`, {
      method: 'POST',
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
      ElMessage.success('支付成功')
      showPaymentDialog.value = false
      fetchOrders() // 刷新订单列表
    } else {
      ElMessage.error(result.message || '支付失败')
    }
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error('支付失败，请稍后再试')
  }
}

// 取消订单
const cancelOrder = (orderId) => {
  ElMessageBox.confirm('确定要取消该订单吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await fetch(`${baseUrl.value}/api/order/${orderId}/cancel`, {
        method: 'POST',
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
        ElMessage.success('订单已取消')
        fetchOrders() // 刷新订单列表
      } else {
        ElMessage.error(result.message || '取消订单失败')
      }
    } catch (error) {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败，请稍后再试')
    }
  }).catch(() => {})
}

// 确认收货
const confirmReceived = (orderId) => {
  ElMessageBox.confirm('确认已收到商品吗?', '提示', {
    confirmButtonText: '确认收货',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      const response = await fetch(`${baseUrl.value}/api/order/${orderId}/received`, {
        method: 'POST',
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
        ElMessage.success('确认收货成功')
        fetchOrders() // 刷新订单列表
      } else {
        ElMessage.error(result.message || '确认收货失败')
      }
    } catch (error) {
      console.error('确认收货失败:', error)
      ElMessage.error('确认收货失败，请稍后再试')
    }
  }).catch(() => {})
}

// 评价订单
const reviewOrder = (orderId) => {
  router.push(`/orders/${orderId}/review`)
}

// 删除订单
const deleteOrder = (orderId) => {
  ElMessageBox.confirm('确定要删除该订单吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await fetch(`${baseUrl.value}/api/order/${orderId}`, {
        method: 'DELETE',
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
        ElMessage.success('订单已删除')
        fetchOrders() // 刷新订单列表
      } else {
        ElMessage.error(result.message || '删除订单失败')
      }
    } catch (error) {
      console.error('删除订单失败:', error)
      ElMessage.error('删除订单失败，请稍后再试')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.orders-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-size: 16px; /* 增加基础字体大小 */
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 29px; /* 增加标题字体大小 */
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
  font-size: 15px; /* 表格字体调整 */
}

:deep(.el-table th) {
  font-size: 16px; /* 表头字体调整 */
}

.book-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  font-size: 14px; /* 标签字体大小 */
  cursor: pointer;
}

.tag-item:hover {
  opacity: 0.85;
}

.price {
  color: #ff6700;
  font-weight: bold;
  font-size: 16px; /* 价格字体大小 */
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

.payment-dialog-content {
  text-align: center;
  font-size: 16px;
}

.total-price {
  color: #ff6700;
  font-weight: bold;
  font-size: 22px; /* 增加总价字体大小 */
}

.payment-qr-code {
  margin-top: 20px;
}

.qr-code {
  margin: 15px auto;
  width: 200px;
  height: 200px;
  border: 1px solid #eee;
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
</style> 