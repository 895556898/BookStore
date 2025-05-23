<template>
  <div class="order-detail-container">
    <div class="page-header">
      <h1>订单详情</h1>
      <div class="page-actions">
        <el-button @click="handleReturn">返回订单列表</el-button>
      </div>
    </div>
    
    <div v-if="loading" class="loading">
      <el-skeleton style="width: 100%" :rows="10" animated />
    </div>
    
    <div v-else-if="error" class="error-container">
      <el-result
        icon="error"
        title="获取订单信息失败"
        sub-title="请稍后再试或联系客服"
      >
        <template #extra>
          <el-button type="primary" @click="handleReturn">返回订单列表</el-button>
        </template>
      </el-result>
    </div>
    
    <div v-else class="order-detail">
      <!-- 订单状态 -->
      <div class="order-status-section">
        <div class="status-info">
          <el-tag size="large" :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
          <p class="status-desc">{{ getStatusDescription(order.status) }}</p>
        </div>
        
        <div class="status-timeline">
          <el-steps :active="getStatusStep(order.status)" finish-status="success" simple>
            <el-step title="提交订单" :icon="Document" />
            <el-step title="付款成功" :icon="Wallet" />
            <el-step title="商家发货" :icon="Van" />
            <el-step title="交易完成" :icon="CircleCheck" />
          </el-steps>
        </div>
        
        <div class="order-actions">
          <template v-if="order.status === 'PENDING'">
            <el-button type="danger" @click="payOrder">去支付</el-button>
            <el-button @click="cancelOrder">取消订单</el-button>
          </template>
          
          <template v-if="order.status === 'COMPLETED'">
            <el-button type="danger" plain @click="deleteOrder">删除订单</el-button>
          </template>
          
          <template v-if="order.status === 'CANCELLED'">
            <el-button type="danger" plain @click="deleteOrder">删除订单</el-button>
          </template>
        </div>
      </div>
      
      <!-- 订单号信息 -->
      <div class="section">
        <h2 class="section-title">订单信息</h2>
        <div class="section-content">
          <div class="order-info">
            <p><span class="label">订单编号：</span>{{ order.id }}</p>
            <p><span class="label">下单时间：</span>{{ formatDate(order.createTime) }}</p>
            <p><span class="label">支付方式：</span>{{ getPaymentMethodText(order.paymentMethod) }}</p>
            <p><span class="label">订单备注：</span>{{ order.remark || '无' }}</p>
          </div>
        </div>
      </div>

      <!-- 收货信息 -->
      <div class="section">
        <h2 class="section-title">收货信息</h2>
        <div class="section-content">
          <div class="address-info">
            <p><span class="label">收货人：</span>{{ order.address?.receiver }}</p>
            <p><span class="label">联系电话：</span>{{ order.address?.phone }}</p>
            <p><span class="label">收货地址：</span>{{ getFullAddress(order.address) }}</p>
          </div>
        </div>
      </div>
      
      <!-- 商品信息 -->
      <div class="section">
        <h2 class="section-title">商品信息</h2>
        <div class="section-content">
          <el-table :data="order.orderItems" border style="width: 100%">
            <el-table-column label="商品信息">
              <template #default="scope">
                <div class="product-item">
                  <div class="product-image">
                    <el-image
                      :src="scope.row.book.cover || '/default-book.jpg'"
                      fit="cover"
                      class="book-cover"
                      @click="goToBook(scope.row.book.id)"
                    />
                  </div>
                  <div class="product-info">
                    <h3 class="product-title" @click="goToBook(scope.row.book.id)">{{ scope.row.book.title }}</h3>
                    <p class="product-author">{{ scope.row.book.writer }}</p>
                  </div>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column label="单价" width="120" prop="price">
              <template #default="scope">
                <span>￥{{ scope.row.price?.toFixed(2) }}</span>
              </template>
            </el-table-column>
            
            <el-table-column label="数量" width="80" prop="quantity" />
            
            <el-table-column label="小计" width="120">
              <template #default="scope">
                <span class="subtotal">￥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      
      <!-- 金额信息 -->
      <div class="section">
        <h2 class="section-title">金额信息</h2>
        <div class="section-content price-summary">
          <div class="price-item">
            <span class="label">商品总额：</span>
            <span>￥{{ calculateSubtotal().toFixed(2) }}</span>
          </div>
          <div class="price-item">
            <span class="label">优惠：</span>
            <span>-￥{{ order.discount?.toFixed(2) || '0.00' }}</span>
          </div>
          <div class="price-item total">
            <span class="label">实付款：</span>
            <span class="total-price">￥{{ order.totalAmount?.toFixed(2) || '0.00' }}</span>
          </div>
        </div>
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
        <p>订单号：{{ order?.id }}</p>
        <p>支付金额：<span class="total-price">￥{{ order?.totalAmount?.toFixed(2) }}</span></p>
        <p>支付方式：{{ getPaymentMethodText(order?.paymentMethod) }}</p>
        
        <div class="payment-qr-code">
          <p>请使用{{ getPaymentMethodText(order?.paymentMethod) }}扫描下方二维码完成支付</p>
          <div class="qr-code">
            <el-image src="/qrcode-demo.jpg" style="width: 200px; height: 200px;"/>
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
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Wallet, Van, CircleCheck } from '@element-plus/icons-vue'
import { formatDate as formatDateUtil } from '~/utils/dateUtils'
import { useUserStore } from '~/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const orderId = route.params.id
const baseUrl = ref(process.env.BASE_URL || 'http://localhost:8080')
const isFromAdmin = ref(false)

const order = ref({})
const loading = ref(true)
const error = ref(false)
const showPaymentDialog = ref(false)

// 获取订单详情
const fetchOrderDetail = async () => {
  loading.value = true;
  error.value = false;

  if (!userStore.isLoggedIn) {
    userStore.initUserFromStorage();
    const isLoggedIn = await userStore.checkSession();
    if (!isLoggedIn) {
      ElMessage.warning('请先登录');
      router.push('/login');
      return;
    }
  }

  isFromAdmin.value = userStore.getUserRole.toLowerCase() === 'admin' && route.query.fromAdmin === 'true';

  try {
    const apiUrl = isFromAdmin.value
      ? `${baseUrl.value}/api/order/admin/${orderId}` 
      : `${baseUrl.value}/api/order/${orderId}`;

    const response = await fetch(apiUrl, {
      credentials: 'include',
      headers: userStore.getAuthHeaders()
    });

    if (response.status === 401 || response.status === 403) {
      ElMessage.error('登录已过期或权限不足，请重新登录');
      userStore.clearUser();
      setTimeout(() => {
        router.push('/login');
      }, 1500);
      return;
    }

    const result = await response.json();
    if (result.code === 200) {
      order.value = result.data;
      console.log('订单详情:', order.value);
    } else {
      error.value = true;
      ElMessage.error(result.message || '获取订单详情失败');
    }
  } catch (err) {
    console.error('获取订单详情失败:', err);
    error.value = true;
    ElMessage.error('获取订单详情失败，请稍后再试');
  } finally {
    loading.value = false;
  }
};

// 获取订单状态文本
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

// 获取订单状态描述
const getStatusDescription = (status) => {
  const descMap = {
    'PENDING': '订单已提交，请尽快完成支付',
    'PAID': '订单已支付，等待商家发货',
    'SHIPPED': '商家已发货，请耐心等待',
    'DELIVERED': '包裹已送达，请确认收货',
    'COMPLETED': '交易已完成，感谢您的购买',
    'CANCELLED': '订单已取消'
  }
  return descMap[status] || ''
}

// 获取订单状态类型
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

// 获取订单流程步骤
const getStatusStep = (status) => {
  const stepMap = {
    'PENDING': 1,
    'PAID': 2,
    'SHIPPED': 3,
    'DELIVERED': 3,
    'COMPLETED': 4,
    'CANCELLED': 1
  }
  return stepMap[status] || 0
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  return formatDateUtil(dateString, 'YYYY-MM-DD HH:mm:ss')
}

// 获取支付方式文本
const getPaymentMethodText = (method) => {
  const methods = {
    'WECHAT': '微信支付',
    'ALIPAY': '支付宝'
  }
  return methods[method] || '未知支付方式'
}

// 获取完整地址
const getFullAddress = (address) => {
  if (!address) return ''
  return `${address.province || ''} ${address.city || ''} ${address.district || ''} ${address.detailAddress || ''} ${address.zipCode ? `(${address.zipCode})` : ''}`
}

// 计算商品小计
const calculateSubtotal = () => {
  if (!order.value.orderItems) return 0
  
  return order.value.orderItems.reduce((total, item) => {
    return total + (item.price * item.quantity)
  }, 0)
}

// 跳转到商品详情
const goToBook = (bookId) => {
  router.push(`/books/${bookId}`)
}

// 支付订单
const payOrder = () => {
  showPaymentDialog.value = true
}

// 确认支付
const confirmPayment = async () => {
  try {
    const response = await fetch(`${baseUrl.value}/api/order/${orderId}/pay?paymentMethod=${order.value.paymentMethod || 'ALIPAY'}`, {
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
      fetchOrderDetail()
    } else {
      ElMessage.error(result.message || '支付失败')
    }
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error('支付失败，请稍后再试')
  }
}

// 取消订单
const cancelOrder = () => {
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
        fetchOrderDetail() // 刷新订单详情
      } else {
        ElMessage.error(result.message || '取消订单失败')
      }
    } catch (error) {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败，请稍后再试')
    }
  }).catch(() => {})
}

// 删除订单
const deleteOrder = () => {
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
        router.push('/orders')
      } else {
        ElMessage.error(result.message || '删除订单失败')
      }
    } catch (error) {
      console.error('删除订单失败:', error)
      ElMessage.error('删除订单失败，请稍后再试')
    }
  }).catch(() => {})
}

// 处理返回按钮点击
const handleReturn = () => {
  if (isFromAdmin.value) {
    router.push('/admin/orders');
  } else {
    router.push('/orders');
  }
}

onMounted(() => {
  fetchOrderDetail()
})
</script>

<style scoped>
.order-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-size: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 29px;
  color: #333;
  margin: 0;
}

.loading, .error-container {
  margin: 40px 0;
  text-align: center;
}

.order-status-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.status-info {
  margin-bottom: 20px;
}

.status-desc {
  color: #666;
  margin-top: 10px;
  font-size: 16px;
}

.status-timeline {
  margin: 30px 0;
}

.order-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.section {
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.section-title {
  padding: 15px 20px;
  margin: 0;
  font-size: 20px;
  border-bottom: 1px solid #eee;
  background-color: #f5f7fa;
}

.section-content {
  padding: 20px;
}

.label {
  color: #666;
  display: inline-block;
  width: 100px;
  font-size: 16px;
}

.address-info p, .order-info p, .shipment-info p {
  margin: 10px 0;
  font-size: 16px;
}

:deep(.el-table th) {
  font-size: 16px;
}

.product-item {
  display: flex;
  align-items: center;
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

.product-author {
  color: #666;
  margin: 0;
  font-size: 14px;
}

.subtotal {
  color: #ff6700;
  font-weight: bold;
  font-size: 16px;
}

.price-summary {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.price-item {
  padding: 8px 0;
  font-size: 16px;
}

.price-item.total {
  font-size: 18px;
  font-weight: bold;
  border-top: 1px solid #eee;
  padding-top: 15px;
  margin-top: 10px;
}

.total-price {
  color: #ff6700;
  font-size: 22px;
  font-weight: bold;
}

.payment-dialog-content {
  text-align: center;
  font-size: 16px;
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