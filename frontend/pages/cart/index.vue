<template>
  <div class="cart-container">
    <div class="page-header">
      <h1>我的购物车</h1>
    </div>
    
    <div v-if="loading" class="loading">
      <el-skeleton style="width: 100%" :rows="5" animated />
    </div>

    <div v-else-if="!isLoggedIn" class="login-required">
      <el-empty description="请登录后查看购物车">
        <template #extra>
          <el-button type="primary" @click="router.push('/login')">去登录</el-button>
        </template>
      </el-empty>
    </div>

    <div v-else-if="cartItems.length === 0" class="empty-cart">
      <el-empty description="购物车还是空的，去添加商品吧~">
        <template #extra>
          <el-button type="primary" @click="router.push('/')">去购物</el-button>
        </template>
      </el-empty>
    </div>
    
    <div v-else class="cart-content">
      <el-table
        ref="multipleTable"
        :data="cartItems"
        tooltip-effect="dark"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column label="商品信息">
          <template #default="scope">
            <div class="cart-item">
              <div class="cart-item-image">
                <el-image
                  :src="scope.row.book.cover || '/default-book.jpg'"
                  fit="cover"
                  :preview-src-list="[scope.row.book.cover || '/default-book.jpg']"
                  class="book-cover"
                />
              </div>
              <div class="cart-item-info">
                <h3 class="book-title" @click="navigateToBook(scope.row.book.id)">
                  {{ scope.row.book.title }}
                </h3>
                <p class="book-author">{{ scope.row.book.author }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="单价" width="150">
          <template #default="scope">
            <span class="price">￥{{ scope.row.book.price?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="数量" width="200">
          <template #default="scope">
            <el-input-number
              v-model="scope.row.quantity"
              :min="1"
              :max="scope.row.book.stock || 99"
              size="small"
              @change="handleQuantityChange(scope.row)"
            />
          </template>
        </el-table-column>
        
        <el-table-column label="小计" width="150">
          <template #default="scope">
            <span class="subtotal">￥{{ calculateSubtotal(scope.row).toFixed(2) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button
              type="danger"
              size="small"
              circle
              @click="removeItem(scope.row.id)"
              title="删除"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="cart-footer">
        <div class="cart-actions">
          <el-checkbox v-model="selectAll" @change="toggleSelectAll">全选</el-checkbox>
          <el-button type="link" @click="removeSelected">删除选中商品</el-button>
          <el-button type="link" @click="clearCart">清空购物车</el-button>
        </div>
        
        <div class="cart-summary">
          <div class="summary-items">
            <p>已选商品 <span class="selected-count">{{ selectedItems.length }}</span> 件</p>
            <p>合计(不含运费)：<span class="total-price">￥{{ calculateTotal().toFixed(2) }}</span></p>
          </div>
          <el-button
            type="primary"
            size="large"
            :disabled="selectedItems.length === 0"
            @click="checkout"
          >
            结算
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 确认删除对话框 -->
    <el-dialog
      v-model="showDeleteConfirm"
      title="确认删除"
      width="30%"
    >
      <p>{{ deleteConfirmMessage }}</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDeleteConfirm = false">取消</el-button>
          <el-button type="danger" @click="confirmDelete">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { useUserStore } from '~/stores/user.js'

// 路由和状态管理
const router = useRouter()
const userStore = useUserStore()

// 页面状态
const loading = ref(true)
const isLoggedIn = ref(false)
const cartItems = ref([])
const selectedItems = ref([])
const selectAll = ref(false)

// 删除相关状态
const showDeleteConfirm = ref(false)
const deleteConfirmMessage = ref('')
const deleteType = ref('') // 'single'/'selected'/'all'
const deleteItemId = ref(null)

// API 基础URL
const baseUrl = ref(process.env.BASE_URL || 'http://localhost:8080')

/**
 * 获取认证请求头
 * @returns {Object} 包含认证信息的请求头
 */
const getAuthHeaders = () => {
  const headers = {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
  
  const token = localStorage.getItem('token') || sessionStorage.getItem('token')
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  
  return headers
}

/**
 * 检查登录状态并加载购物车
 */
const checkLoginStatus = async () => {
  // 初始化用户状态 - 从localStorage恢复
  if (!userStore.isLoggedIn) {
    userStore.initUserFromStorage()
  }

  // 检查用户是否已登录
  if (userStore.isLoggedIn && userStore.user) {
    isLoggedIn.value = true
    await fetchCartItems()
  } else {
    isLoggedIn.value = false
  }
  loading.value = false
}

/**
 * 获取购物车列表
 */
const fetchCartItems = async () => {
  loading.value = true
  try {
    const response = await fetch(`${baseUrl.value}/api/cart`, {
      method: 'GET',
      credentials: 'include',
      headers: getAuthHeaders()
    })
    
    if (!response.ok) {
      // 检查是否未授权
      if (response.status === 401 || response.status === 403) {
        isLoggedIn.value = false
        loading.value = false
        return
      }
      throw new Error(`HTTP error! Status: ${response.status}`)
    }
    
    const result = await response.json()
    
    if (result.code === 200) {
      cartItems.value = result.data || []
      console.log('购物车数据:', cartItems.value)
    } else {
      ElMessage.error(result.message || '获取购物车失败')
    }
  } catch (error) {
    console.error('获取购物车失败:', error)
    ElMessage.error('获取购物车失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

/**
 * 修改商品数量
 * @param {Object} item 要修改的购物车项
 */
const handleQuantityChange = async (item) => {
  try {
    const response = await fetch(`${baseUrl.value}/api/cart/update/${item.id}?quantity=${item.quantity}`, {
      method: 'PUT',
      credentials: 'include',
      headers: getAuthHeaders()
    })
    const result = await response.json()
    
    if (result.code !== 200) {
      ElMessage.error(result.message || '修改数量失败')
      fetchCartItems() // 刷新购物车
    }
  } catch (error) {
    console.error('修改数量失败:', error)
    ElMessage.error('修改数量失败')
  }
}

/**
 * 删除单个商品
 * @param {number} itemId 要删除的商品ID
 */
const removeItem = (itemId) => {
  deleteItemId.value = itemId
  deleteType.value = 'single'
  deleteConfirmMessage.value = '确定要从购物车中删除该商品吗？'
  showDeleteConfirm.value = true
}

/**
 * 删除选中商品
 */
const removeSelected = () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请先选择要删除的商品')
    return
  }
  
  deleteType.value = 'selected'
  deleteConfirmMessage.value = `确定要删除选中的 ${selectedItems.value.length} 件商品吗？`
  showDeleteConfirm.value = true
}

/**
 * 清空购物车
 */
const clearCart = () => {
  if (cartItems.value.length === 0) {
    ElMessage.warning('购物车已经是空的了')
    return
  }
  
  deleteType.value = 'all'
  deleteConfirmMessage.value = '确定要清空购物车吗？'
  showDeleteConfirm.value = true
}

/**
 * 确认删除操作
 */
const confirmDelete = async () => {
  try {
    let response;
    
    if (deleteType.value === 'single') {
      // 删除单个商品
      response = await fetch(`${baseUrl.value}/api/cart/remove/${deleteItemId.value}`, {
        method: 'DELETE',
        credentials: 'include',
        headers: getAuthHeaders()
      })
    } else if (deleteType.value === 'selected') {
      // 删除选中的商品 - 循环删除每个选中项
      const selectedIds = selectedItems.value.map(item => item.id)
      for (const id of selectedIds) {
        await fetch(`${baseUrl.value}/api/cart/remove/${id}`, {
          method: 'DELETE',
          credentials: 'include',
          headers: getAuthHeaders()
        })
      }
      response = { ok: true }
    } else if (deleteType.value === 'all') {
      // 清空整个购物车
      response = await fetch(`${baseUrl.value}/api/cart/clear`, {
        method: 'DELETE',
        credentials: 'include',
        headers: getAuthHeaders()
      })
    }
    
    if (!response.ok) {
      const result = await response.json()
      ElMessage.error(result.message || '删除失败')
    } else {
      ElMessage.success('删除成功')
      fetchCartItems() // 刷新购物车列表
    }
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  } finally {
    showDeleteConfirm.value = false
  }
}

/**
 * 计算商品小计金额
 * @param {Object} item 购物车项
 * @returns {number} 小计金额
 */
const calculateSubtotal = (item) => {
  const price = item.book?.price || 0
  return price * item.quantity
}

/**
 * 计算选中商品总价
 * @returns {number} 总价金额
 */
const calculateTotal = () => {
  let total = 0
  selectedItems.value.forEach(item => {
    total += calculateSubtotal(item)
  })
  return total
}

/**
 * 处理表格选择变化
 * @param {Array} selection 当前选中的项
 */
const handleSelectionChange = (selection) => {
  selectedItems.value = selection
  selectAll.value = selection.length === cartItems.value.length && cartItems.value.length > 0
}

/**
 * 全选/取消全选
 * @param {boolean} val 是否全选
 */
const toggleSelectAll = (val) => {
  const table = document.querySelector('.el-table__body-wrapper table')
  const checkboxes = table.querySelectorAll('.el-checkbox__input')
  
  checkboxes.forEach(checkbox => {
    if (val && !checkbox.classList.contains('is-checked')) {
      checkbox.querySelector('input').click()
    } else if (!val && checkbox.classList.contains('is-checked')) {
      checkbox.querySelector('input').click()
    }
  })
}

/**
 * 跳转到商品详情页
 * @param {number} bookId 书籍ID
 */
const navigateToBook = (bookId) => {
  router.push(`/books/${bookId}`)
}

/**
 * 前往结算页面
 */
const checkout = () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }
  
  // 保存选中的商品到localStorage，以便结算页面使用
  localStorage.setItem('checkoutItems', JSON.stringify(selectedItems.value))
  router.push('/checkout')
}

// 页面加载时执行
onMounted(() => {
  checkLoginStatus()
})
</script>

<style scoped>
.cart-container {
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

.loading, .empty-cart, .login-required {
  margin: 40px 0;
  text-align: center;
}

.cart-item {
  display: flex;
  align-items: center;
}

.cart-item-image {
  width: 80px;
  margin-right: 15px;
}

.book-cover {
  width: 80px;
  height: 80px;
  object-fit: cover;
}

.cart-item-info {
  flex-grow: 1;
}

.book-title {
  margin: 0 0 5px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  color: #333;
}

.book-title:hover {
  color: #409EFF;
}

.book-author {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.price, .subtotal {
  font-weight: bold;
}

.subtotal {
  color: #ff6700;
}

.cart-footer {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
}

.cart-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.cart-summary {
  display: flex;
  align-items: center;
  gap: 20px;
}

.summary-items {
  text-align: right;
}

.summary-items p {
  margin: 5px 0;
}

.selected-count {
  color: #ff6700;
  font-weight: bold;
}

.total-price {
  color: #ff6700;
  font-size: 20px;
  font-weight: bold;
}
</style> 