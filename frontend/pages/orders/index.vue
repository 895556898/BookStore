<template>
  <div class="orders-container">
    <div class="page-header">
      <h1>我的订单</h1>
    </div>
    
    <div v-if="loading" class="loading">
      <el-skeleton style="width: 100%" :rows="5" animated />
    </div>
    
    <div v-else-if="!isLoggedIn" class="login-required">
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
            <el-input v-model="filterForm.keyword" placeholder="订单号/商品名称" />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 订单列表 -->
      <div class="order-list">
        <div v-for="order in orders" :key="order.id" class="order-item">
          <div class="order-header">
            <div class="order-info">
              <span class="order-time">下单时间: {{ formatDate(order.createTime) }}</span>
              <span class="order-number">订单号: {{ order.orderNumber }}</span>
            </div>
            <div class="order-status">
              <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
            </div>
          </div>
          
          <div class="order-content">
            <div class="order-products">
              <div v-for="item in order.items" :key="item.id" class="product-item">
                <div class="product-image">
                  <el-image
                    :src="item.book.cover || '/default-book.jpg'"
                    fit="cover"
                    @click="goToBook(item.book.id)"
                    class="book-cover"
                  />
                </div>
                <div class="product-info">
                  <h3 class="product-title" @click="goToBook(item.book.id)">{{ item.book.title }}</h3>
                  <p class="product-price">￥{{ item.price.toFixed(2) }} x {{ item.quantity }}</p>
                </div>
              </div>
            </div>
            
            <div class="order-summary">
              <p class="order-total">总计: <span class="price">￥{{ order.totalAmount.toFixed(2) }}</span></p>
              <div class="order-actions">
                <el-button 
                  type="primary" 
                  size="small"
                  @click="goToOrderDetail(order.id)"
                >
                  订单详情
                </el-button>
                
                <template v-if="order.status === 'PENDING_PAYMENT'">
                  <el-button 
                    type="danger" 
                    size="small"
                    @click="payOrder(order)"
                  >
                    去支付
                  </el-button>
                  <el-button 
                    type="info" 
                    size="small"
                    @click="cancelOrder(order.id)"
                  >
                    取消订单
                  </el-button>
                </template>
                
                <template v-if="order.status === 'DELIVERED'">
                  <el-button 
                    type="success" 
                    size="small"
                    @click="confirmReceived(order.id)"
                  >
                    确认收货
                  </el-button>
                </template>
                
                <template v-if="order.status === 'COMPLETED'">
                  <el-button 
                    type="primary" 
                    plain
                    size="small"
                    @click="reviewOrder(order.id)"
                  >
                    评价
                  </el-button>
                  <el-button 
                    type="danger" 
                    plain
                    size="small"
                    @click="deleteOrder(order.id)"
                  >
                    删除订单
                  </el-button>
                </template>
                
                <template v-if="order.status === 'CANCELLED'">
                  <el-button 
                    type="danger" 
                    plain
                    size="small"
                    @click="deleteOrder(order.id)"
                  >
                    删除订单
                  </el-button>
                </template>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          background
          layout="prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange"
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
        <p>订单号：{{ currentOrder?.orderNumber }}</p>
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate as formatDateUtil } from '../../utils/dateUtils'

const router = useRouter()
const loading = ref(true)
const isLoggedIn = ref(false)
const orders = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(5)
const showPaymentDialog = ref(false)
const currentOrder = ref(null)

// 订单状态选项
const statusOptions = [
  { value: 'PENDING_PAYMENT', label: '待付款' },
  { value: 'PENDING_SHIPMENT', label: '待发货' },
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
    'PENDING_PAYMENT': '待付款',
    'PENDING_SHIPMENT': '待发货',
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
    'PENDING_PAYMENT': 'warning',
    'PENDING_SHIPMENT': 'info',
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

// 检查登录状态
const checkLoginStatus = () => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    isLoggedIn.value = true
    return true
  } else {
    isLoggedIn.value = false
    loading.value = false
    return false
  }
}

// 获取订单列表
const fetchOrders = async () => {
  if (!checkLoginStatus()) return
  
  loading.value = true
  try {
    let url = `/api/order/search?page=${currentPage.value}&pageSize=${pageSize.value}`
    
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
      credentials: 'include'
    })
    
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
    const response = await fetch(`/api/order/${currentOrder.value.id}/pay?paymentMethod=${currentOrder.value.paymentMethod || 'ALIPAY'}`, {
      method: 'POST',
      credentials: 'include'
    })
    
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
      const response = await fetch(`/api/order/${orderId}/cancel`, {
        method: 'POST',
        credentials: 'include'
      })
      
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
      // 这个API可能不存在，需要根据后端实际情况调整
      const response = await fetch(`/api/order/${orderId}/received`, {
        method: 'POST',
        credentials: 'include'
      })
      
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
      const response = await fetch(`/api/order/${orderId}`, {
        method: 'DELETE',
        credentials: 'include'
      })
      
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
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 24px;
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

.order-list {
  margin-bottom: 20px;
}

.order-item {
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #eee;
}

.order-info {
  display: flex;
  gap: 20px;
  color: #666;
}

.order-content {
  padding: 20px;
}

.order-products {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 20px;
}

.product-item {
  display: flex;
  align-items: center;
  padding-bottom: 15px;
  border-bottom: 1px dashed #eee;
}

.product-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.product-image {
  width: 80px;
  height: 80px;
  margin-right: 15px;
  flex-shrink: 0;
}

.book-cover {
  width: 80px;
  height: 80px;
  object-fit: cover;
  cursor: pointer;
}

.product-info {
  flex-grow: 1;
}

.product-title {
  margin: 0 0 5px;
  font-size: 16px;
  cursor: pointer;
  color: #333;
}

.product-title:hover {
  color: #409EFF;
}

.product-price {
  color: #666;
  margin: 0;
}

.order-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.order-total {
  margin: 0;
  font-size: 16px;
}

.price {
  color: #ff6700;
  font-weight: bold;
}

.order-actions {
  display: flex;
  gap: 10px;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.payment-dialog-content {
  text-align: center;
}

.total-price {
  color: #ff6700;
  font-weight: bold;
  font-size: 18px;
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
</style> 