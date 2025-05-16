<template>
  <div class="book-detail-container">
    <!-- 书籍搜索区域 -->
    <div class="search-container">
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="请输入商品名称"
          class="search-input"
        >
          <template #append>
            <el-button @click="handleSearch">
              搜索
            </el-button>
          </template>
        </el-input>
      </div>
    </div>

    <div v-if="loading" class="loading">
      <el-skeleton style="width: 100%" :rows="10" animated />
    </div>
    
    <div v-else-if="error" class="error-container">
      <el-result
        icon="error"
        title="获取图书信息失败"
        sub-title="请稍后再试或联系管理员"
      >
        <template #extra>
          <el-button type="primary" @click="router.push('/')">返回首页</el-button>
        </template>
      </el-result>
    </div>
    
    <div v-else class="book-detail">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/books' }">图书</el-breadcrumb-item>
        <el-breadcrumb-item>{{ book.title }}</el-breadcrumb-item>
      </el-breadcrumb>
      
      <div class="book-content">
        <div class="book-image">
          <el-image 
            :src="book.cover || '/default-book.jpg'" 
            fit="contain"
            :preview-src-list="[book.cover || '/default-book.jpg']"
          >
            <template #error>
              <div class="image-error">
                <el-icon><Picture /></el-icon>
                <p>图片加载失败</p>
              </div>
            </template>
          </el-image>
        </div>
        
        <div class="book-info">
          <h1>{{ book.title }}</h1>
          <p class="author">作者：{{ book.writer || book.author || '暂无' }}</p>
          <p class="isbn">ISBN：{{ book.isbn || '暂无' }}</p>
          
          <div class="price-info">
            <span class="price">￥{{ book.price ? book.price.toFixed(2) : '0.00' }}</span>
            <span v-if="book.originalPrice && book.originalPrice > book.price" class="original-price">
              原价: ￥{{ book.originalPrice.toFixed(2) }}
            </span>
          </div>
          
          <div class="book-tags" v-if="book.tags && book.tags.length > 0">
            <el-tag v-for="tag in book.tags" :key="tag.id" :type="getTagType(tag.name)" effect="plain" class="tag">
              {{ tag.name }}
            </el-tag>
          </div>
          
          <div class="stock-info">
            <span :class="['stock-status', book.stock > 0 ? 'in-stock' : 'out-stock']">
              {{ book.stock > 0 ? '有货' : '缺货' }}
            </span>
            <span class="stock-count" v-if="book.stock > 0">库存: {{ book.stock }}</span>
            <span class="sales-count">销量: {{ book.sales || 0 }}</span>
          </div>
          
          <div class="book-actions">
            <el-input-number 
              v-model="quantity" 
              :min="1" 
              :max="book.stock || 1" 
              :disabled="book.stock <= 0"
              size="large"
            />
            
            <el-button 
              type="primary" 
              size="large" 
              :disabled="book.stock <= 0"
              @click="addToCart"
            >
              <el-icon><ShoppingCart /></el-icon> 加入购物车
            </el-button>
            
            <el-button 
              type="danger" 
              size="large" 
              :disabled="book.stock <= 0"
              @click="buyNow"
            >
              立即购买
            </el-button>
          </div>
        </div>
      </div>

      <div class="related-books">
        <h2>相关推荐</h2>
        <el-row :gutter="20">
          <el-col :span="4" v-for="(relatedBook, index) in relatedBooks" :key="index">
            <el-card
              class="related-book-card"
              shadow="hover"
              @click="goToBookDetail(relatedBook.id)"
            >
              <img :src="relatedBook.cover || '/default-book.jpg'" class="related-book-cover" />
              <div class="related-book-info">
                <h3 class="related-book-title">{{ relatedBook.title }}</h3>
                <p class="related-book-price">￥{{ relatedBook.price ? relatedBook.price.toFixed(2) : '0.00' }}</p>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ShoppingCart, Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '~/stores/user.js'

const router = useRouter()
const route = useRoute()
const bookId = route.params.id
const userStore = useUserStore()

const book = ref({})
const loading = ref(true)
const error = ref(false)
const quantity = ref(1)
const relatedBooks = ref([])
const searchKeyword = ref('')
const baseUrl = ref(process.env.BASE_URL || 'http://localhost:8080')
const isLoggedIn = ref(false)

// 获取认证请求头
const getAuthHeaders = () => {
  return userStore.getAuthHeaders();
}

// 检查登录状态
const checkLoginStatus = () => {
  // 初始化用户状态 - 从localStorage恢复
  if (!userStore.isLoggedIn) {
    userStore.initUserFromStorage()
  }

  // 检查用户是否已登录
  isLoggedIn.value = userStore.isLoggedIn && userStore.user
  return isLoggedIn.value
}

// 处理搜索
const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push(`/books?keyword=${encodeURIComponent(searchKeyword.value.trim())}`)
  }
}

// 获取图书详情
const fetchBookDetail = async () => {
  loading.value = true
  try {
    // 检查登录状态
    const loggedIn = checkLoginStatus()
    
    // 如果本地有用户状态但没有检查过会话状态，先验证会话
    if (loggedIn && !userStore.sessionChecked) {
      await userStore.checkSession()
    }
    
    const response = await fetch(`${baseUrl.value}/api/book/get/${bookId}`, {
      method: 'GET',
      credentials: 'include',
      headers: getAuthHeaders()
    })
    
    if (!response.ok) {
      // 检查是否未授权
      if (response.status === 401 || response.status === 403) {
        ElMessage.warning('请先登录后查看')
        router.push('/login')
        return
      }
      throw new Error(`HTTP error! Status: ${response.status}`)
    }
    
    const result = await response.json()
    
    if (result.code === 200) {
      book.value = result.data
      // 设置默认数量不超过库存
      if (book.value.stock && book.value.stock > 0) {
        quantity.value = Math.min(1, book.value.stock)
      }
      fetchRelatedBooks()
    } else {
      error.value = true
      ElMessage.error(result.message || '获取图书信息失败')
    }
  } catch (err) {
    console.error('获取图书详情失败:', err)
    error.value = true
  } finally {
    loading.value = false
  }
}

// 获取相关推荐图书
const fetchRelatedBooks = async () => {
  try {
    // 可以根据当前图书的标签推荐相关图书
    let tagIds = []
    if (book.value.tags && book.value.tags.length > 0) {
      tagIds = book.value.tags.map(tag => tag.id)
    }
    
    if (tagIds.length > 0) {
      const response = await fetch(`${baseUrl.value}/api/book/searchByTags?pageNum=1&pageSize=6&tids=${tagIds.join(',')}`, {
        method: 'GET',
        credentials: 'include',
        headers: getAuthHeaders()
      })
      
      if (!response.ok) {
        console.error('获取相关图书失败:', response.status)
        return
      }
      
      const result = await response.json()
      
      if (result.code === 200) {
        // 过滤掉当前图书
        relatedBooks.value = result.data.records.filter(item => item.id != bookId)
      }
    } else {
      // 如果没有标签，则获取随机图书
      const response = await fetch(`${baseUrl.value}/api/book/search?pageNum=1&pageSize=6`, {
        method: 'GET',
        credentials: 'include',
        headers: getAuthHeaders()
      })
      
      if (!response.ok) {
        console.error('获取相关图书失败:', response.status)
        return
      }
      
      const result = await response.json()
      
      if (result.code === 200) {
        // 过滤掉当前图书
        relatedBooks.value = result.data.records.filter(item => item.id != bookId)
      }
    }
  } catch (err) {
    console.error('获取相关图书失败:', err)
  }
}

// 添加到购物车
const addToCart = async () => {
  try {
    if (!checkLogin()) return
    
    const response = await fetch(`${baseUrl.value}/api/cart/add?bookId=${bookId}&quantity=${quantity.value}`, {
      method: 'POST',
      credentials: 'include',
      headers: getAuthHeaders()
    })
    
    if (!response.ok) {
      if (response.status === 401 || response.status === 403) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
      }
      throw new Error(`HTTP error! Status: ${response.status}`)
    }
    
    const result = await response.json()
    if (result.code === 200) {
      ElMessage.success('成功加入购物车')
    } else {
      ElMessage.error(result.message || '添加购物车失败')
    }
  } catch (err) {
    console.error('添加购物车失败:', err)
    ElMessage.error('添加购物车失败，请稍后再试')
  }
}

// 立即购买
const buyNow = async () => {
  if (!checkLogin()) return
  
  try {
    // 先加入购物车
    const response = await fetch(`${baseUrl.value}/api/cart/add?bookId=${bookId}&quantity=${quantity.value}`, {
      method: 'POST',
      credentials: 'include',
      headers: getAuthHeaders()
    })
    
    if (!response.ok) {
      if (response.status === 401 || response.status === 403) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
      }
      throw new Error(`HTTP error! Status: ${response.status}`)
    }
    
    const result = await response.json()
    if (result.code === 200) {
      // 跳转到结算页面
      router.push('/checkout')
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (err) {
    console.error('购买失败:', err)
    ElMessage.error('操作失败，请稍后再试')
  }
}

// 检查是否登录
const checkLogin = () => {
  const isLogin = checkLoginStatus()
  if (!isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return false
  }
  return true
}

// 获取标签颜色
const getTagType = (tagName) => {
  const tagTypes = {
    '科技': 'success',
    '文学': 'info',
    '历史': 'warning',
    '艺术': 'danger',
  }
  
  // 为特定标签名分配类型，其他标签随机
  return tagTypes[tagName] || ''
}

// 跳转到相关图书详情
const goToBookDetail = (id) => {
  router.push(`/books/${id}`)
}

onMounted(() => {
  fetchBookDetail()
})
</script>

<style scoped>
.book-detail-container {
  padding: 0 0 20px 0;
  max-width: 1200px;
  margin: 0 auto;
}

/* 搜索区域样式 */
.search-container {
  background-color: #fff;
  padding: 15px 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  border-radius: 4px;
}

.search-bar {
  max-width: 500px;
  margin: 0 auto;
}

.search-input {
  width: 100%;
}

.loading {
  margin: 20px 0;
}

.error-container {
  margin: 40px 0;
}

.book-detail {
  margin-top: 20px;
}

.book-content {
  display: flex;
  gap: 30px;
  margin: 30px 0;
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.book-image {
  width: 350px;
  height: 450px;
  flex-shrink: 0;
  border: 1px solid #eee;
  padding: 10px;
  border-radius: 8px;
  overflow: hidden;
  background-color: #fff;
}

.image-error {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #909399;
  font-size: 14px;
}

.book-info {
  flex-grow: 1;
}

.book-info h1 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 24px;
  color: #333;
  font-weight: bold;
}

.author, .isbn {
  margin: 10px 0;
  color: #666;
}

.price-info {
  margin: 20px 0;
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: 4px;
}

.price {
  font-size: 28px;
  color: #ff6700;
  font-weight: bold;
}

.original-price {
  margin-left: 10px;
  color: #999;
  text-decoration: line-through;
  font-size: 16px;
}

.book-tags {
  margin: 15px 0;
}

.tag {
  margin-right: 10px;
}

.stock-info {
  margin: 15px 0;
  display: flex;
  align-items: center;
}

.stock-status {
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: bold;
}

.stock-count, .sales-count {
  margin-left: 10px;
  color: #666;
}

.in-stock {
  color: #67c23a;
  background-color: #f0f9eb;
}

.out-stock {
  color: #f56c6c;
  background-color: #fef0f0;
}

.book-actions {
  margin-top: 30px;
  display: flex;
  gap: 15px;
}

.related-books {
  margin-top: 40px;
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.related-books h2 {
  margin-bottom: 20px;
  font-size: 20px;
  color: #333;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 10px;
}

.related-book-card {
  height: 250px;
  cursor: pointer;
  transition: transform 0.3s;
  margin-bottom: 20px;
}

.related-book-card:hover {
  transform: translateY(-5px);
}

.related-book-cover {
  width: 100%;
  height: 150px;
  object-fit: cover;
}

.related-book-info {
  padding: 10px;
}

.related-book-title {
  margin: 5px 0;
  font-size: 14px;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.related-book-price {
  color: #ff6700;
  font-weight: bold;
  margin: 5px 0 0;
}
</style> 