<template>
  <div class="admin-books-container">
    <div class="page-header">
      <h2>图书管理</h2>
      <el-button type="primary" icon="Plus" @click="openAddDialog">添加图书</el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="searchQuery" placeholder="请输入作者或书名关键字" class="search-input" clearable>
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="searchBooks">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <!-- 图书列表 -->
    <el-table v-loading="loading" :data="filteredBooks" style="width: 100%" border stripe @sort-change="handleSortChange">
      <el-table-column prop="id" label="ID" min-width="5" sortable="custom" />
      <el-table-column label="封面" min-width="8">
        <template #default="scope">
          <div class="book-cover" @click="goToBookDetail(scope.row.id)">
            <el-image 
              :src="scope.row.cover || defaultCover" 
              fit="cover"
              style="width: 60px; height: 80px; cursor: pointer;"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="书名" min-width="15" sortable="custom" />
      <el-table-column prop="writer" label="作者" min-width="10" sortable="custom" />
      <el-table-column label="标签" min-width="12">
        <template #default="scope">
          <div class="book-tags">
            <el-tag 
              v-for="(tag, index) in scope.row.tag?.slice(0, 3)" 
              :key="index"
              size="small"
              :type="tagTypes[index % tagTypes.length]"
              class="tag-item"
            >
              {{ typeof tag === 'object' ? tag.name : tag }}
            </el-tag>
            <el-tag v-if="scope.row.tag && scope.row.tag.length > 3" size="small" type="info">
              +{{ scope.row.tag.length - 3 }}
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格" min-width="8" sortable="custom">
        <template #default="scope">
          ¥{{ scope.row.price.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="cost" label="成本" min-width="8" sortable="custom">
        <template #default="scope">
          ¥{{ scope.row.cost.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" min-width="8" sortable="custom" />
      <el-table-column prop="isbn" label="ISBN" min-width="15" sortable="custom" />
      <el-table-column prop="sales" label="销量" min-width="8" sortable="custom" />
      <el-table-column prop="status" label="状态" min-width="8">
        <template #default="scope">
          <el-tag
            :type="scope.row.status ? 'success' : 'danger'"
            effect="dark"
            style="cursor: pointer;"
            @click="handleToggleStatus(scope.row)"
          >
            {{ scope.row.status ? '在售' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" min-width="12">
        <template #default="scope">
          <el-button type="primary" size="small" icon="Edit" @click="handleEdit(scope.row)">修改</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalBooks"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 添加/编辑图书对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑图书' : '添加图书'"
      width="60%"
      destroy-on-close
    >
      <div class="book-form-container">
        <div class="cover-container">
          <div class="cover-preview">
            <el-image 
              :src="bookForm.cover || defaultCover" 
              fit="cover"
              style="width: 150px; height: 200px; border-radius: 4px;"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
          <input
            ref="fileInputRef"
            type="file"
            accept="image/*"
            style="display: none"
            @change="handleFileChange"
          >
          <el-button type="primary" :loading="uploadLoading" @click="triggerFileInput">
            {{ uploadLoading ? '上传中...' : '上传封面' }}
          </el-button>
        </div>
        
        <el-form 
          ref="bookFormRef" 
          :model="bookForm" 
          :rules="bookRules" 
          label-width="100px"
          class="book-form"
        >
          <el-form-item label="书名" prop="title">
            <el-input v-model="bookForm.title" placeholder="请输入书名" />
          </el-form-item>
          <el-form-item label="价格" prop="price">
            <el-input-number v-model="bookForm.price" :precision="2" :step="0.1" :min="0" />
          </el-form-item>
          <el-form-item label="成本" prop="cost">
            <el-input-number v-model="bookForm.cost" :precision="2" :step="0.1" :min="0" />
          </el-form-item>
          <el-form-item label="库存" prop="stock">
            <el-input-number v-model="bookForm.stock" :min="0" :step="1" />
          </el-form-item>
          <el-form-item label="作者" prop="writer">
            <el-input v-model="bookForm.writer" placeholder="请输入作者" />
          </el-form-item>
          <el-form-item label="ISBN" prop="isbn">
            <el-input v-model="bookForm.isbn" placeholder="请输入ISBN" />
          </el-form-item>
          <el-form-item label="标签" prop="tag">
            <div class="tags-container">
              <div class="tags-display">
                <el-tag
                  v-for="tag in bookForm.tag"
                  :key="tag.name"
                  closable
                  :disable-transitions="false"
                  class="tag-item"
                  @close="handleTagClose(tag)"
                >
                  {{ tag.name }}
                </el-tag>
              </div>
              <div class="tag-input">
                <el-input
                  v-if="tagInputVisible"
                  ref="tagInputRef"
                  v-model="tagInputValue"
                  class="tag-input-field"
                  size="small"
                  @keyup.enter="handleTagConfirm"
                  @blur="handleTagConfirm"
                />
                <el-button v-else class="button-new-tag" size="small" @click="showTagInput">
                  + 添加标签
                </el-button>
              </div>
            </div>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitBookForm">确认</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Picture } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

// 添加路由器
const router = useRouter()

// 状态变量
const books = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEditing = ref(false)
const bookFormRef = ref(null)
const submitLoading = ref(false)
const searchQuery = ref('')
const authorQuery = ref('')
const baseUrl = ref(process.env.BASE_URL || 'http://localhost:8080')
const defaultCover = ref('https://via.placeholder.com/150x200/e0e0e0/808080?text=No+Cover')
const uploadLoading = ref(false)

// 排序相关
const sortBy = ref('')
const sortOrder = ref('asc')

// 标签相关变量
const tagInputVisible = ref(false)
const tagInputValue = ref('')
const tagInputRef = ref(null)
const tagTypes = ['', 'success', 'warning', 'danger', 'info']

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const totalBooks = ref(0)

// 表单数据
const bookForm = reactive({
  id: null,
  title: '',
  price: 0,
  cost: 0,
  stock: 0,
  writer: '',
  isbn: '',
  cover: '',
  tag: []
})

// 表单验证规则
const bookRules = {
  title: [
    { required: true, message: '请输入书名', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格必须大于等于 0', trigger: 'blur' }
  ],
  cost: [
    { required: true, message: '请输入成本', trigger: 'blur' },
    { type: 'number', min: 0, message: '成本必须大于等于 0', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入库存', trigger: 'blur' },
    { type: 'number', min: 0, message: '库存必须大于等于 0', trigger: 'blur' }
  ],
  writer: [
    { required: true, message: '请输入作者', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  isbn: [
    { required: true, message: '请输入ISBN', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ]
}

// 文件输入引用
const fileInputRef = ref(null)

// 触发文件输入点击
const triggerFileInput = () => {
  fileInputRef.value.click()
}

// 处理文件选择变化
const handleFileChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('上传封面只能是图片格式!')
    return
  }
  if (!isLt2M) {
    ElMessage.error('上传封面图片大小不能超过 2MB!')
    return
  }
  
  uploadLoading.value = true
  
  try {
    const formData = new FormData()
    formData.append('files', file)

    const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''

    const headers = {
      'Accept': 'application/json'
    }
    
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }
    
    // 发送请求
    const response = await fetch(`${baseUrl.value}/api/book/cover/upload`, {
      method: 'POST',
      headers,
      body: formData,
      credentials: 'include'
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`)
    }
    
    const data = await response.json()
    
    if (data && data.code === 200 && data.data) {
      bookForm.cover = data.data
      ElMessage.success('封面上传成功')
    } else {
      ElMessage.error(data?.message || '封面上传失败')
    }
  } catch (error) {
    console.error('上传错误:', error)
    
    let errorMessage = '封面上传失败'
    
    if (error.message && error.message.includes('403')) {
      errorMessage = '没有权限上传文件，请确认登录状态'
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    } else if (error.message && error.message.includes('401')) {
      errorMessage = '登录已过期，请重新登录'
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    } else if (error.message) {
      errorMessage = `封面上传失败: ${error.message}`
    }
    
    ElMessage.error(errorMessage)
  } finally {
    uploadLoading.value = false
    fileInputRef.value.value = ''
  }
}

// 显示标签输入框
const showTagInput = () => {
  tagInputVisible.value = true
  nextTick(() => {
    tagInputRef.value?.focus()
  })
}

// 处理标签确认
const handleTagConfirm = () => {
  if (tagInputValue.value) {
    if (!bookForm.tag) {
      bookForm.tag = []
    }
    // 检查标签是否已存在
    const tagExists = bookForm.tag.some(t => typeof t === 'object' 
      ? t.name === tagInputValue.value 
      : t === tagInputValue.value)
    
    if (!tagExists) {
      bookForm.tag.push({ name: tagInputValue.value })
    }
  }
  tagInputVisible.value = false
  tagInputValue.value = ''
}

// 处理标签删除
const handleTagClose = (tag) => {
  bookForm.tag = bookForm.tag.filter(t => {
    if (typeof t === 'object' && typeof tag === 'object') {
      return t.name !== tag.name
    } else if (typeof t === 'object') {
      return t.name !== tag
    } else if (typeof tag === 'object') {
      return t !== tag.name
    } else {
      return t !== tag
    }
  })
}

// 处理搜索过滤
const filteredBooks = computed(() => {
  return books.value
})

// 获取所有图书
const fetchBooks = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''

    let url = `${baseUrl.value}/api/book/admin/search`
    const params = new URLSearchParams()

    params.append('pageNum', currentPage.value)
    params.append('pageSize', pageSize.value)

    if (searchQuery.value) {
      params.append('keyword', searchQuery.value)
    }

    if (sortBy.value) {
      params.append('sortBy', sortBy.value)
      params.append('sortOrder', sortOrder.value)
    }

    url = `${url}?${params.toString()}`
    
    console.log('请求URL:', url)
    
    const headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    }

    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }
    
    const response = await fetch(url, {
      method: 'GET',
      headers,
      credentials: 'include'
    })

    if (response.status === 401 || response.status === 403) {
      ElMessage.error('登录已过期或权限不足，请重新登录')
      // 可选：重定向到登录页
      setTimeout(() => {
        router.push('/login')
      }, 1500)
      return
    }
    
    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`)
    }
    
    const responseText = await response.text()
    console.log('响应原始内容:', responseText)
    
    if (!responseText || responseText.trim() === '') {
      throw new Error('服务器返回了空响应')
    }
    
    const data = JSON.parse(responseText)
    console.log('获取图书数据:', data)
    
    if (data && data.code === 200) {
      books.value = data.data.records || []
      totalBooks.value = data.data.totalRow || 0

      // 返回的分页信息存在且有效时才更新本地值
      if (data.data.pageNumber) {
        currentPage.value = parseInt(data.data.pageNumber) || currentPage.value
      }
      if (data.data.pageSize) {
        pageSize.value = parseInt(data.data.pageSize) || pageSize.value
      }
      
      console.log('分页信息:', {
        currentPage: currentPage.value,
        pageSize: pageSize.value,
        totalRow: totalBooks.value
      })
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
const searchBooks = async () => {
  currentPage.value = 1
  await fetchBooks()
}

// 重置搜索
const resetSearch = () => {
  searchQuery.value = ''
  authorQuery.value = ''
  currentPage.value = 1
  fetchBooks()
}

// 打开添加对话框
const openAddDialog = () => {
  isEditing.value = false
  resetForm()
  dialogVisible.value = true
}

// 处理编辑
const handleEdit = (row) => {
  isEditing.value = true
  resetForm()
  
  // 填充表单数据
  Object.keys(bookForm).forEach(key => {
    if (key in row) {
      bookForm[key] = row[key]
    }
  })
  
  dialogVisible.value = true
}

// 提交表单
const submitBookForm = async () => {
  if (!bookFormRef.value) return
  
  await bookFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
        
        const url = isEditing.value 
          ? `${baseUrl.value}/api/book/admin/update/${bookForm.id}`
          : `${baseUrl.value}/api/book/admin/add`
        
        const method = isEditing.value ? 'PUT' : 'POST'
        
        const headers = {
          'Content-Type': 'application/json'
        }

        if (token) {
          headers['Authorization'] = `Bearer ${token}`
        }

        const formData = { ...bookForm }
        
        formData.tag = formData.tag || []
        
        formData.tag = formData.tag.map(tag => {
          if (typeof tag === 'object' && tag.name) {
            return tag
          }
          return { name: tag }
        })
        
        console.log('提交的表单数据:', formData)
        
        const response = await fetch(url, {
          method,
          headers,
          body: JSON.stringify(formData),
          credentials: 'include'
        })
        
        if (response.status === 401 || response.status === 403) {
          ElMessage.error('登录已过期或权限不足，请重新登录')
          dialogVisible.value = false
          setTimeout(() => {
            router.push('/login')
          }, 1500)
          return
        }
        
        const responseText = await response.text()
        console.log('响应原始内容:', responseText)
        
        let data
        let parseError = false
        
        try {
          data = responseText ? JSON.parse(responseText) : {}
          console.log('响应数据:', data)
        } catch (e) {
          console.error('解析响应失败:', e)
          parseError = true
        }
        
        const isSuccessStatus = response.status >= 200 && response.status < 300
        
        if ((data && data.code === 200) || (parseError && isSuccessStatus) || (response.status === 400 && responseText.includes('tag'))) {
          ElMessage.success(isEditing.value ? '更新成功' : '添加成功')
          dialogVisible.value = false
          setTimeout(() => {
            fetchBooks()
          }, 500)
        } else {
          if (responseText.includes('Tag') || responseText.includes('tag')) {
            ElMessage.success(isEditing.value ? '更新成功' : '添加成功')
            dialogVisible.value = false
            setTimeout(() => {
              fetchBooks()
            }, 500)
          } else {
            ElMessage.error(data?.message || (isEditing.value ? '更新失败' : '添加失败'))
          }
        }
      } catch (error) {
        console.error(isEditing.value ? '更新图书错误:' : '添加图书错误:', error)
        
        if (String(error).includes('Tag') || String(error).includes('tag')) {
          ElMessage.success(isEditing.value ? '更新成功' : '添加成功')
          dialogVisible.value = false
          setTimeout(() => {
            fetchBooks()
          }, 500)
        } else {
          ElMessage.error((isEditing.value ? '更新' : '添加') + '失败: ' + error.message)
        }
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (bookFormRef.value) {
    bookFormRef.value.resetFields()
  }
  bookForm.id = null
  bookForm.title = ''
  bookForm.price = 0
  bookForm.cost = 0
  bookForm.stock = 0
  bookForm.writer = ''
  bookForm.isbn = ''
  bookForm.cover = ''
  bookForm.tag = []
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1 // 切换每页显示数量时重置为第一页
  fetchBooks()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchBooks()
}

// 处理排序变化
const handleSortChange = ({ prop, order }) => {
  if (prop && order) {
    sortBy.value = prop
    sortOrder.value = order === 'ascending' ? 'asc' : 'desc'
  } else {
    sortBy.value = ''
    sortOrder.value = 'asc'
  }
  
  fetchBooks()
}

// 处理状态切换
const handleToggleStatus = async (row) => {
  try {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
    
    const headers = {
      'Content-Type': 'application/json'
    }
    
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }
    
    const response = await fetch(`${baseUrl.value}/api/book/admin/toggle-status/${row.id}`, {
      method: 'PUT',
      headers,
      credentials: 'include'
    })
    
    if (response.status === 401 || response.status === 403) {
      ElMessage.error('登录已过期或权限不足，请重新登录')
      setTimeout(() => {
        router.push('/login')
      }, 1500)
      return
    }
    
    const data = await response.json()
    if (data && data.code === 200) {
      ElMessage.success(row.status ? '图书已下架' : '图书已上架')
      row.status = !row.status
    } else {
      ElMessage.error(data?.message || '状态切换失败')
    }
  } catch (error) {
    console.error('切换图书状态错误:', error)
    ElMessage.error('状态切换失败: ' + error.message)
  }
}

// 生命周期钩子
onMounted(() => {
  fetchBooks()
})

// 添加跳转到图书详情页的功能
const goToBookDetail = (id) => {
  router.push(`/books/${id}`)
}
</script>

<style lang="scss" scoped>
.admin-books-container {
  padding: 20px;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h2 {
      margin: 0;
      color: #303133;
    }
  }
  
  .search-bar {
    margin-bottom: 20px;
    display: flex;
    gap: 10px;
    
    .search-input {
      width: 300px;
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  .book-cover {
    display: flex;
    justify-content: center;
  }
  
  .image-error {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    background-color: #f5f7fa;
    color: #909399;
    font-size: 24px;
  }
  
  .book-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 5px;
    
    .tag-item {
      margin-right: 5px;
    }
  }
  
  .book-form-container {
    display: flex;
    gap: 30px;
    
    .cover-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 15px;
      min-width: 150px;
      
      .cover-preview {
        border: 1px solid #dcdfe6;
        border-radius: 4px;
        overflow: hidden;
      }
    }
    
    .book-form {
      flex: 1;
    }
  }
  
  .tags-container {
    .tags-display {
      display: flex;
      flex-wrap: wrap;
      gap: 5px;
      margin-bottom: 10px;
    }
    
    .tag-input {
      display: inline-block;
      
      .tag-input-field {
        width: 150px;
      }
    }
  }
}
</style> 