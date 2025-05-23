<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search, ShoppingCart } from "@element-plus/icons-vue"
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

// 定义图书类型接口
interface Book {
  id: number;
  title: string;
  author?: string;
  writer?: string;
  cover?: string;
  price: number;
  sales?: number;
  stock?: number;
  tag?: unknown[];
}

const searchKey = ref('')
const recommendBooks = ref<Book[]>([])
const newBooks = ref<Book[]>([])
const topBooks = ref<Book[]>([]) // 销量前三的书籍
const baseUrl = ref('http://localhost:8080')

// 初始化路由
const router = useRouter()

// 获取销量前三的书籍用于轮播图
const fetchTopBooks = async () => {
  try {
    const response = await fetch(`${baseUrl.value}/api/book/search?pageNum=1&pageSize=3&sortBy=sales&sortOrder=desc`)
    const data = await response.json()
    if (data.code === 200) {
      topBooks.value = data.data.records
    }
  } catch (error) {
    console.error('获取销量前三书籍失败:', error)
  }
}

// 获取随机推荐图书
const fetchRecommendBooks = async () => {
  try {
    const response = await fetch(`${baseUrl.value}/api/book/search?pageNum=1&pageSize=20`)
    const data = await response.json()
    if (data.code === 200 && data.data.records.length > 0) {
      const allBooks = data.data.records
      const selectedBooks = []
      const totalBooks = allBooks.length
      
      if (totalBooks <= 4) {
        recommendBooks.value = allBooks
      } else {
        const selectedIndices = new Set()
        while (selectedIndices.size < 4) {
          const randomIndex = Math.floor(Math.random() * totalBooks)
          if (!selectedIndices.has(randomIndex)) {
            selectedIndices.add(randomIndex)
            selectedBooks.push(allBooks[randomIndex])
          }
        }
        recommendBooks.value = selectedBooks
      }
    }
  } catch (error) {
    console.error('获取推荐图书失败:', error)
  }
}

// 获取新书（按ID最大的4本书）
const fetchNewBooks = async () => {
  try {
    const response = await fetch(`${baseUrl.value}/api/book/search?pageNum=1&pageSize=4&sortBy=id&sortOrder=desc`)
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
  if (!searchKey.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  // 跳转到搜索结果页
  router.push({
    path: '/books',
    query: {
      keyword: searchKey.value.trim()
    }
  })
}

// 添加到购物车
const addToCart = async (bookId: number) => {
  try {
    const response = await fetch(`${baseUrl.value}/api/cart/add?bookId=${bookId}&quantity=1`, {
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
const navigateToDetail = (bookId: number) => {
  navigateTo(`/books/${bookId}`)
}

// 导航跳转
const navigateTo = (path: string) => {
  router.push(path)
}

// 检查是否登录
const isLoggedIn = ref(false)
const username = ref('')

const checkLoginStatus = async () => {
  try {
    const response = await fetch(`${baseUrl.value}/api/user/check`, {
      method: 'GET',
      credentials: 'include'
    })
    const data = await response.json()
    if (data.code === 200) { 
      isLoggedIn.value = true
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
  fetchTopBooks() 
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
              <img class="logo" src="/logo.png" alt="Logo" >
            </el-col>

            <el-col :span="13">
              <el-input v-model="searchKey" placeholder="请输入书名/作者" class="search-input" @keyup.enter="handleSearch">
                <template #append>
                  <el-button :icon="Search" @click="handleSearch" />
                </template>
              </el-input>
            </el-col>
          </el-row>
        </div>

        <!-- 轮播图 - 显示销量前三的书 -->
        <el-carousel height="300px" class="carousel">
          <el-carousel-item v-for="(book, index) in topBooks" :key="book?.id || index" @click="book?.id && navigateToDetail(book.id)">
            <div class="carousel-item" :style="`background-image: url(${book?.cover || `/banner${index+1}.jpg`})`">
              <h3 class="carousel-title">{{ book?.title || '精选好书推荐' }}</h3>
            </div>
          </el-carousel-item>
        </el-carousel>

        <!-- 推荐书籍 - 随机4本书 -->
        <div class="section">
          <div class="section-header">
            <h2>推荐书籍</h2>
            <el-button type="text" @click="navigateTo('/books')">查看更多 ></el-button>
          </div>
          <el-row :gutter="20">
            <el-col v-for="book in recommendBooks" :key="book?.id" :span="6">
              <el-card class="book-card" shadow="hover" @click="book?.id && navigateToDetail(book.id)">
                <img :src="book?.cover || '/default-book.jpg'" class="book-cover"  alt="">
                <div class="book-info">
                  <h3 class="book-title">{{ book?.title }}</h3>
                  <p class="book-author">{{ book?.author || book?.writer }}</p>
                  <div class="book-price-row">
                    <span class="book-price">￥{{ book?.price ? book.price.toFixed(2) : '0.00' }}</span>
                    <el-button
                        type="primary"
                        size="small"
                        circle
                        :icon="ShoppingCart"
                        @click.stop="book?.id && addToCart(book.id)"
                    />
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 新书上架 - ID最大的4本书 -->
        <div class="section">
          <div class="section-header">
            <h2>新书上架</h2>
            <el-button type="text" @click="navigateTo('/books')">查看更多 ></el-button>
          </div>
          <el-row :gutter="20">
            <el-col v-for="book in newBooks" :key="book?.id" :span="6">
              <el-card class="book-card" shadow="hover" @click="book?.id && navigateToDetail(book.id)">
                <img :src="book?.cover || '/default-book.jpg'" class="book-cover"  alt="">
                <div class="book-info">
                  <h3 class="book-title">{{ book?.title }}</h3>
                  <p class="book-author">{{ book?.author || book?.writer }}</p>
                  <div class="book-price-row">
                    <span class="book-price">￥{{ book?.price ? book.price.toFixed(2) : '0.00' }}</span>
                    <el-button
                        type="primary"
                        size="small"
                        circle
                        :icon="ShoppingCart"
                        @click.stop="book?.id && addToCart(book.id)"
                    />
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
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
  cursor: pointer;
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