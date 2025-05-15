<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search, ShoppingCart } from "@element-plus/icons-vue"

const searchKey = ref('')
const activeIndex = ref('1')
const bookList = ref([])
const recommendBooks = ref([])
const newBooks = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)

// 获取图书列表
const fetchBooks = async () => {
  loading.value = true
  try {
    const response = await fetch(`/api/book/search?pageNum=${currentPage.value}&pageSize=${pageSize.value}&keyword=${searchKey.value}`)
    const data = await response.json()
    if (data.code === 200) {
      bookList.value = data.data.records
      total.value = data.data.total
    }
  } catch (error) {
    console.error('获取图书列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取推荐图书
const fetchRecommendBooks = async () => {
  try {
    const response = await fetch('/api/book/search?pageNum=1&pageSize=4&keyword=推荐')
    const data = await response.json()
    if (data.code === 200) {
      recommendBooks.value = data.data.records
    }
  } catch (error) {
    console.error('获取推荐图书失败:', error)
  }
}

// 获取新书
const fetchNewBooks = async () => {
  try {
    const response = await fetch('/api/book/search?pageNum=1&pageSize=4&keyword=新书')
    const data = await response.json()
    if (data.code === 200) {
      newBooks.value = data.data.records
    }
  } catch (error) {
    console.error('获取新书失败:', error)
  }
}

// 搜索逻辑
const handleSearch = () => {
  currentPage.value = 1
  fetchBooks()
}

// 页面变化
const handlePageChange = (page) => {
  currentPage.value = page
  fetchBooks()
}

// 添加到购物车
const addToCart = async (bookId) => {
  try {
    const response = await fetch(`/api/cart/add?bookId=${bookId}&quantity=1`, {
      method: 'POST',
      credentials: 'include'
    })
    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success('添加购物车成功')
    } else {
      ElMessage.error(data.message || '添加购物车失败')
    }
  } catch (error) {
    console.error('添加购物车失败:', error)
    ElMessage.error('添加购物车失败')
  }
}

// 跳转到书籍详情页
const navigateToDetail = (bookId) => {
  navigateTo(`/books/${bookId}`)
}

// 导航跳转
const navigateTo = (path) => {
  useRouter().push(path)
}

// 检查是否登录
const isLoggedIn = ref(false)
const username = ref('')

const checkLoginStatus = async () => {
  try {
    const response = await fetch('/api/user/check', {
      method: 'GET',
      credentials: 'include'
    })
    const data = await response.json()
    if (data.code === 1006) { // 假设1006是已登录状态码
      isLoggedIn.value = true
      // 从localStorage获取用户名
      const userInfo = localStorage.getItem('userInfo')
      if (userInfo) {
        username.value = JSON.parse(userInfo).username
      }
    }
  } catch (error) {
    console.error('检查登录状态失败:', error)
  }
}

onMounted(() => {
  fetchBooks()
  fetchRecommendBooks()
  fetchNewBooks()
  checkLoginStatus()
})
</script>

<template>
  <div class="layout1">
    <el-container>
      <el-main style="height: 100%">
        <div class="main_header">
          <el-row :gutter="20" class="main-header" type="flex" align="middle">
            <el-col :span="6">
              <img class="logo" src="/logo.png" alt="Logo" />
            </el-col>

            <el-col :span="13">
              <el-input
                v-model="searchKey"
                placeholder="请输入书名/作者"
                class="search-input"
                @keyup.enter="handleSearch"
              >
                <template #append>
                  <el-button :icon="Search" @click="handleSearch" />
                </template>
              </el-input>
            </el-col>
          </el-row>
        </div>

        <el-row :gutter="20" id="nav_box" class="nav_content" style="background-color: #545c64">
          <el-col :span="16">
            <el-menu
              :default-active="activeIndex"
              mode="horizontal"
              class="nav navbar-nav navbar-left"
              background-color="#545c64"
              text-color="#fff"
              active-text-color="#ffd04b"
            >
              <el-menu-item index="1" @click="navigateTo('/')">
                <span class="white">首页</span>
              </el-menu-item>
              <el-menu-item index="2" @click="navigateTo('/books/hot')">
                <span class="white">热销</span>
              </el-menu-item>
              <el-menu-item index="3" @click="navigateTo('/books/new')">
                <span class="white">新书专区</span>
              </el-menu-item>
              <el-menu-item index="4" @click="navigateTo('/books/discount')">
                <span class="white">特价专区</span>
              </el-menu-item>
              <el-menu-item index="5" @click="navigateTo('/books/recommend')">
                <span class="white">推荐书单</span>
              </el-menu-item>
            </el-menu>
          </el-col>
        </el-row>
        
        <!-- 轮播图 -->
        <el-carousel height="300px" class="carousel">
          <el-carousel-item v-for="i in 4" :key="i">
            <div class="carousel-item" :style="`background-image: url(/banner${i}.jpg)`">
              <h3 class="carousel-title">精选好书推荐 {{ i }}</h3>
            </div>
          </el-carousel-item>
        </el-carousel>
        
        <!-- 推荐书籍 -->
        <div class="section">
          <div class="section-header">
            <h2>推荐书籍</h2>
            <el-button type="text" @click="navigateTo('/books/recommend')">查看更多 ></el-button>
          </div>
          <el-row :gutter="20">
            <el-col :span="6" v-for="book in recommendBooks" :key="book.id">
              <el-card class="book-card" shadow="hover" @click="navigateToDetail(book.id)">
                <img :src="book.cover || '/default-book.jpg'" class="book-cover" />
                <div class="book-info">
                  <h3 class="book-title">{{ book.title }}</h3>
                  <p class="book-author">{{ book.author }}</p>
                  <div class="book-price-row">
                    <span class="book-price">￥{{ book.price.toFixed(2) }}</span>
                    <el-button 
                      type="primary" 
                      size="small" 
                      circle 
                      @click.stop="addToCart(book.id)"
                      icon="ShoppingCart"
                    ></el-button>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
        
        <!-- 新书上架 -->
        <div class="section">
          <div class="section-header">
            <h2>新书上架</h2>
            <el-button type="text" @click="navigateTo('/books/new')">查看更多 ></el-button>
          </div>
          <el-row :gutter="20">
            <el-col :span="6" v-for="book in newBooks" :key="book.id">
              <el-card class="book-card" shadow="hover" @click="navigateToDetail(book.id)">
                <img :src="book.cover || '/default-book.jpg'" class="book-cover" />
                <div class="book-info">
                  <h3 class="book-title">{{ book.title }}</h3>
                  <p class="book-author">{{ book.author }}</p>
                  <div class="book-price-row">
                    <span class="book-price">￥{{ book.price.toFixed(2) }}</span>
                    <el-button 
                      type="primary" 
                      size="small" 
                      circle 
                      @click.stop="addToCart(book.id)"
                      icon="ShoppingCart"
                    ></el-button>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
        
        <!-- 所有图书 -->
        <div class="section">
          <div class="section-header">
            <h2>全部图书</h2>
          </div>
          <el-row :gutter="20">
            <el-col :span="6" v-for="book in bookList" :key="book.id">
              <el-card class="book-card" shadow="hover" @click="navigateToDetail(book.id)">
                <img :src="book.cover || '/default-book.jpg'" class="book-cover" />
                <div class="book-info">
                  <h3 class="book-title">{{ book.title }}</h3>
                  <p class="book-author">{{ book.author }}</p>
                  <div class="book-price-row">
                    <span class="book-price">￥{{ book.price.toFixed(2) }}</span>
                    <el-button 
                      type="primary" 
                      size="small" 
                      circle 
                      @click.stop="addToCart(book.id)"
                      icon="ShoppingCart"
                    ></el-button>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          
          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="total"
              :page-size="pageSize"
              :current-page="currentPage"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<style scoped lang="scss">
.layout1 {
  max-width: 1200px;
  margin: 0 auto;
  background-color: #fff;
}

.logo {
  width: 150px;
  height: auto;
  position: relative;
  left: 20px;
}

.text-logo {
  width: 200px;
  height: 55px;
  font-family: 'Microsoft YaHei', sans-serif;
  font-size: 30px;
  letter-spacing: 6px;
  font-weight: bold;
  position: relative;
  top: 5px;
}

.head_text {
  font-family: 'Microsoft YaHei', sans-serif;
  font-size: 18px;
  letter-spacing: 1px;
}

.white {
  color: white;
}

.search-input {
  width: 100%;
}

.carousel {
  margin: 20px 0;
  border-radius: 8px;
  overflow: hidden;
}

.carousel-item {
  height: 100%;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: flex-end;
  justify-content: flex-start;
  padding: 20px;
  color: white;
}

.carousel-title {
  background-color: rgba(0, 0, 0, 0.6);
  padding: 10px 20px;
  border-radius: 4px;
}

.section {
  margin: 30px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 10px;
}

.section-header h2 {
  margin: 0;
  color: #409EFF;
}

.book-card {
  margin-bottom: 20px;
  height: 340px;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: transform 0.3s;
}

.book-card:hover {
  transform: translateY(-5px);
}

.book-cover {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.book-info {
  padding: 10px;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.book-title {
  margin: 0 0 5px;
  font-size: 16px;
  font-weight: bold;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.book-author {
  color: #666;
  margin: 0 0 10px;
  font-size: 14px;
}

.book-price-row {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.book-price {
  font-size: 18px;
  color: #ff6700;
  font-weight: bold;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>