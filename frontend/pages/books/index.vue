<template>
  <div class="books-page">
    <!-- 搜索栏 -->
    <div class="search-section">
      <el-input
        v-model="searchQuery"
        placeholder="请输入书名或作者"
        class="search-input"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <el-card shadow="never">
        <div class="filter-row">
          <span class="filter-label">价格：</span>
          <div class="filter-tags">
            <el-tag 
              :effect="selectedPriceRange === '' ? 'dark' : 'plain'" 
              @click="selectPriceRange('')"
              class="filter-tag"
            >
              全部
            </el-tag>
            <el-tag 
              v-for="(range, index) in priceRanges" 
              :key="index"
              :effect="selectedPriceRange === range.value ? 'dark' : 'plain'" 
              @click="selectPriceRange(range.value)"
              class="filter-tag"
            >
              {{ range.label }}
            </el-tag>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 排序栏 -->
    <div class="sort-section">
      <el-space>
        <el-radio-group v-model="sortBy" size="small" @change="handleSort">
          <el-radio-button label="default">默认排序</el-radio-button>
          <el-radio-button label="sales">销量</el-radio-button>
          <el-radio-button label="price">价格</el-radio-button>
        </el-radio-group>
        
        <el-button 
          size="small" 
          :icon="sortOrder === 'asc' ? ArrowUp : ArrowDown" 
          @click="toggleSortOrder"
          :disabled="sortBy === 'default'"
        >
          {{ sortOrder === 'asc' ? '升序' : '降序' }}
        </el-button>
      </el-space>
    </div>
    
    <!-- 图书列表 -->
    <div v-loading="loading" class="books-container">
      <el-empty v-if="!loading && books.length === 0" description="暂无图书" />
      
      <el-row :gutter="20">
        <el-col 
          v-for="book in books" 
          :key="book.id" 
          :xs="12" 
          :sm="8" 
          :md="6" 
          :lg="4" 
          :xl="4"
        >
          <el-card 
            shadow="hover" 
            class="book-card" 
            @click="navigateToDetail(book.id)"
          >
            <div class="book-cover">
              <el-image 
                :src="book.cover || defaultCover" 
                fit="cover"
                :lazy="true"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </div>
            <div class="book-info">
              <h3 class="book-title" :title="book.title">{{ book.title }}</h3>
              <p class="book-author">{{ book.writer }}</p>
              <div class="book-tags" v-if="book.tag && book.tag.length > 0">
                <el-tag 
                  v-for="(tag, index) in book.tag.slice(0, 2)" 
                  :key="index"
                  size="small"
                  :type="tagTypes[index % tagTypes.length]"
                >
                  {{ typeof tag === 'object' ? tag.name : tag }}
                </el-tag>
              </div>
              <div class="book-price">
                <span class="price">¥{{ book.price ? book.price.toFixed(2) : '0.00' }}</span>
                <span class="sales">销量: {{ book.sales || 0 }}</span>
              </div>
              <div class="book-actions">
                <el-button 
                  type="primary" 
                  size="default"
                  :icon="ShoppingCart"
                  circle 
                  @click.stop="addToCart(book.id)"
                  :disabled="book.stock <= 0"
                />
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 36, 48]"
        :background="true"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalBooks"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, ShoppingCart, Picture, ArrowDown, ArrowUp } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const searchQuery = ref('')
const loading = ref(false)
const books = ref([])
const currentPage = ref(1)
const pageSize = ref(12)
const totalBooks = ref(0)
const defaultCover = ref('/default-book.jpg')
const baseUrl = ref(process.env.BASE_URL || 'http://localhost:8080')
const tagTypes = ['', 'success', 'warning', 'danger', 'info']

// 排序相关
const sortBy = ref('default')
const sortOrder = ref('desc')

// 分类和筛选
const selectedCategory = ref('')
const selectedPriceRange = ref('')

// 价格区间
const priceRanges = ref([
  { label: '0-50元', value: '0-50' },
  { label: '50-100元', value: '50-100' },
  { label: '100-150元', value: '100-150' },
  { label: '150元以上', value: '150-999' }
])

// 获取图书列表
const fetchBooks = async () => {
  loading.value = true
  try {
    // 构建URL和参数
    let url = `${baseUrl.value}/api/book/search`
    const params = new URLSearchParams()
    
    // 添加分页参数
    params.append('pageNum', currentPage.value)
    params.append('pageSize', pageSize.value)
    
    // 添加搜索关键词
    if (searchQuery.value) {
      params.append('keyword', searchQuery.value)
    }
    
    // 添加排序参数
    if (sortBy.value !== 'default') {
      params.append('sortBy', sortBy.value)
      params.append('sortOrder', sortOrder.value)
    }
    
    // 添加价格区间参数
    if (selectedPriceRange.value) {
      const [min, max] = selectedPriceRange.value.split('-').map(Number)
      if (!isNaN(min)) {
        params.append('minPrice', min)
      }
      if (!isNaN(max)) {
        params.append('maxPrice', max)
      }
    }
    
    // 完整URL
    url = `${url}?${params.toString()}`
    
    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`)
    }
    
    const data = await response.json()
    
    if (data && data.code === 200) {
      books.value = data.data.records || []
      totalBooks.value = data.data.totalRow || 0
      
      // 使用后端返回的分页信息
      if (data.data.pageNumber) {
        currentPage.value = parseInt(data.data.pageNumber) || currentPage.value
      }
      if (data.data.pageSize) {
        pageSize.value = parseInt(data.data.pageSize) || pageSize.value
      }
    } else {
      ElMessage.error(data?.message || '获取图书列表失败')
    }
  } catch (error) {
    console.error('获取图书列表错误:', error)
    ElMessage.error('获取图书列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 搜索图书
const handleSearch = () => {
  currentPage.value = 1
  fetchBooks()
}

// 重置搜索
const resetSearch = () => {
  searchQuery.value = ''
  currentPage.value = 1
  selectedCategory.value = ''
  selectedPriceRange.value = ''
  sortBy.value = 'default'
  sortOrder.value = 'desc'
  fetchBooks()
}

// 处理排序
const handleSort = () => {
  currentPage.value = 1
  fetchBooks()
}

// 切换排序顺序
const toggleSortOrder = () => {
  sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  fetchBooks()
}

// 选择分类
const selectCategory = (categoryId) => {
  selectedCategory.value = categoryId
  currentPage.value = 1
  fetchBooks()
}

// 选择价格区间
const selectPriceRange = (range) => {
  selectedPriceRange.value = range
  currentPage.value = 1
  fetchBooks()
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchBooks()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchBooks()
}

// 导航到详情页
const navigateToDetail = (bookId) => {
  router.push(`/books/${bookId}`)
}

// 添加到购物车
const addToCart = async (bookId) => {
  try {
    const response = await fetch(`${baseUrl.value}/api/cart/add?bookId=${bookId}&quantity=1`, {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    })
    
    if (!response.ok) {
      if (response.status === 401) {
        ElMessage.error('请先登录')
        router.push('/login')
        return
      }
      throw new Error(`HTTP error! Status: ${response.status}`)
    }
    
    const data = await response.json()
    
    if (data && data.code === 200) {
      ElMessage.success('添加购物车成功')
    } else {
      ElMessage.error(data?.message || '添加购物车失败')
    }
  } catch (error) {
    console.error('添加购物车错误:', error)
    ElMessage.error('添加购物车失败: ' + error.message)
  }
}

// 生命周期钩子
onMounted(() => {
  // 从URL获取搜索关键词
  const keywordFromURL = route.query.keyword
  if (keywordFromURL) {
    searchQuery.value = keywordFromURL
  }
  
  // 获取图书列表
  fetchBooks()
})
</script>

<style scoped>
.books-page {
  padding: 20px;
}

.search-section {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.search-input {
  width: 400px;
}

.filter-section {
  margin-bottom: 20px;
}

.filter-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.filter-row:last-child {
  margin-bottom: 0;
}

.filter-label {
  width: 70px;
  color: #606266;
  font-weight: bold;
}

.filter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-tag {
  cursor: pointer;
}

.sort-section {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.books-container {
  margin-bottom: 20px;
  min-height: 400px;
}

.book-card {
  height: 100%;
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.3s;
  display: flex;
  flex-direction: column;
}

.book-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.book-cover {
  width: 100%;
  height: 200px;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.book-cover .el-image {
  width: 100%;
  height: 100%;
}

.image-error {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  color: #909399;
}

.book-info {
  padding: 10px;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.book-title {
  margin: 0 0 5px 0;
  font-size: 16px;
  font-weight: bold;
  line-height: 1.4;
  height: 44px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.book-author {
  margin: 0 0 5px 0;
  font-size: 14px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-tags {
  margin-bottom: 10px;
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.book-price {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.price {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.sales {
  font-size: 12px;
  color: #909399;
}

.book-actions {
  margin-top: auto;
  display: flex;
  justify-content: flex-end;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .search-input {
    width: 100%;
  }
  
  .filter-section,
  .sort-section {
    padding: 10px;
  }
  
  .filter-label {
    width: 60px;
  }
}
</style>
