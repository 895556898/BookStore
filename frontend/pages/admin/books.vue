<template>
  <div class="admin-books-container">
    <div class="page-header">
      <h2>图书管理</h2>
      <el-button type="primary" @click="openAddDialog" icon="Plus">添加图书</el-button>
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
    <el-table :data="filteredBooks" style="width: 100%" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" min-width="5" />
      <el-table-column label="封面" min-width="8">
        <template #default="scope">
          <div class="book-cover">
            <el-image 
              :src="scope.row.cover || defaultCover" 
              fit="cover"
              :preview-src-list="[scope.row.cover || defaultCover]"
              style="width: 60px; height: 80px;"
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
      <el-table-column prop="title" label="书名" min-width="15" />
      <el-table-column prop="writer" label="作者" min-width="10" />
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
              {{ tag }}
            </el-tag>
            <el-tag v-if="scope.row.tag && scope.row.tag.length > 3" size="small" type="info">
              +{{ scope.row.tag.length - 3 }}
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格" min-width="8">
        <template #default="scope">
          ¥{{ scope.row.price.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="cost" label="成本" min-width="8">
        <template #default="scope">
          ¥{{ scope.row.cost.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" min-width="8" />
      <el-table-column prop="isbn" label="ISBN" min-width="15" />
      <el-table-column prop="sales" label="销量" min-width="8" />
      <el-table-column label="操作" fixed="right" min-width="12">
        <template #default="scope">
          <el-button type="danger" size="small" @click="handleDelete(scope.row)" icon="Delete">删除</el-button>
          <el-button type="primary" size="small" @click="handleEdit(scope.row)" icon="Edit">修改</el-button>
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
          <el-upload
            class="cover-upload"
            :action="`${baseUrl}/api/admin/upload/cover`"
            :headers="getUploadHeaders()"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
          >
            <el-button type="primary" :loading="uploadLoading">
              {{ uploadLoading ? '上传中...' : '上传封面' }}
            </el-button>
          </el-upload>
        </div>
        
        <el-form 
          :model="bookForm" 
          :rules="bookRules" 
          ref="bookFormRef" 
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
                  :key="tag"
                  closable
                  :disable-transitions="false"
                  @close="handleTagClose(tag)"
                  class="tag-item"
                >
                  {{ tag }}
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
          <el-button type="primary" @click="submitBookForm" :loading="submitLoading">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="确认删除"
      width="30%"
    >
      <p>确定要删除书籍 "{{ bookToDelete?.title }}" 吗？此操作不可撤销。</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmDelete" :loading="deleteLoading">确认删除</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Edit, Delete, User, Plus, Picture } from '@element-plus/icons-vue'
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
const deleteDialogVisible = ref(false)
const deleteLoading = ref(false)
const bookToDelete = ref(null)
const searchQuery = ref('')
const authorQuery = ref('')
const baseUrl = ref(process.env.BASE_URL || 'http://localhost:8080')
const defaultCover = ref('https://via.placeholder.com/150x200/e0e0e0/808080?text=No+Cover')
const uploadLoading = ref(false)

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

// 获取上传请求头
const getUploadHeaders = () => {
  const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
  return {
    Authorization: token ? `Bearer ${token}` : ''
  }
}

// 上传前验证
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('上传封面只能是图片格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('上传封面图片大小不能超过 2MB!')
    return false
  }
  uploadLoading.value = true
  return true
}

// 处理上传成功
const handleUploadSuccess = (response, file) => {
  uploadLoading.value = false
  if (response && response.code === 200 && response.data) {
    bookForm.cover = response.data // 假设后端返回的是完整的图片URL
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error(response?.message || '封面上传失败')
  }
}

// 处理上传失败
const handleUploadError = (error) => {
  uploadLoading.value = false
  console.error('上传错误:', error)
  ElMessage.error('封面上传失败')
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
    if (!bookForm.tag.includes(tagInputValue.value)) {
      bookForm.tag.push(tagInputValue.value)
    }
  }
  tagInputVisible.value = false
  tagInputValue.value = ''
}

// 处理标签删除
const handleTagClose = (tag) => {
  bookForm.tag = bookForm.tag.filter(t => t !== tag)
}

// 处理搜索过滤
const filteredBooks = computed(() => {
  return books.value
})

// 获取所有图书
const fetchBooks = async () => {
  loading.value = true
  try {
    // 获取存储的token或session
    const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
    
    const url = searchQuery.value 
      ? `${baseUrl.value}/api/book/search?keyword=${encodeURIComponent(searchQuery.value)}`
      : `${baseUrl.value}/api/book/search`
    
    console.log('请求URL:', url)
    
    const headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    }
    
    // 如果有token，添加到请求头
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }
    
    const response = await fetch(url, {
      method: 'GET',
      headers,
      credentials: 'include'
    })
    
    // 检查是否认证失败
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
      currentPage.value = data.data.pageNumber || 1
      pageSize.value = data.data.pageSize || 10
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

// 处理删除
const handleDelete = (row) => {
  bookToDelete.value = row
  deleteDialogVisible.value = true
}

// 确认删除
const confirmDelete = async () => {
  if (!bookToDelete.value) return
  
  deleteLoading.value = true
  try {
    // 获取存储的token
    const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
    
    const headers = {
      'Content-Type': 'application/json'
    }
    
    // 如果有token，添加到请求头
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }
    
    const response = await fetch(`${baseUrl.value}/api/book/admin/delete/${bookToDelete.value.id}`, {
      method: 'DELETE',
      headers,
      credentials: 'include'
    })
    
    // 检查是否认证失败
    if (response.status === 401 || response.status === 403) {
      ElMessage.error('登录已过期或权限不足，请重新登录')
      deleteDialogVisible.value = false
      // 可选：重定向到登录页
      setTimeout(() => {
        router.push('/login')
      }, 1500)
      return
    }
    
    const data = await response.json()
    if (data && data.code === 200) {
      ElMessage.success('删除成功')
      fetchBooks()
    } else {
      ElMessage.error(data?.message || '删除失败')
    }
  } catch (error) {
    console.error('删除图书错误:', error)
    ElMessage.error('删除失败: ' + error.message)
  } finally {
    deleteLoading.value = false
    deleteDialogVisible.value = false
  }
}

// 提交表单
const submitBookForm = async () => {
  if (!bookFormRef.value) return
  
  await bookFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        // 获取存储的token
        const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
        
        const url = isEditing.value 
          ? `${baseUrl.value}/api/book/admin/update/${bookForm.id}`
          : `${baseUrl.value}/api/book/admin/add`
        
        const method = isEditing.value ? 'PUT' : 'POST'
        
        const headers = {
          'Content-Type': 'application/json'
        }
        
        // 如果有token，添加到请求头
        if (token) {
          headers['Authorization'] = `Bearer ${token}`
        }
        
        const response = await fetch(url, {
          method,
          headers,
          body: JSON.stringify(bookForm),
          credentials: 'include'
        })
        
        // 检查是否认证失败
        if (response.status === 401 || response.status === 403) {
          ElMessage.error('登录已过期或权限不足，请重新登录')
          dialogVisible.value = false
          // 可选：重定向到登录页
          setTimeout(() => {
            router.push('/login')
          }, 1500)
          return
        }
        
        const data = await response.json()
        if (data && data.code === 200) {
          ElMessage.success(isEditing.value ? '更新成功' : '添加成功')
          dialogVisible.value = false
          fetchBooks()
        } else {
          ElMessage.error(data?.message || (isEditing.value ? '更新失败' : '添加失败'))
        }
      } catch (error) {
        console.error(isEditing.value ? '更新图书错误:' : '添加图书错误:', error)
        ElMessage.error((isEditing.value ? '更新' : '添加') + '失败: ' + error.message)
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
  fetchBooks()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchBooks()
}

// 生命周期钩子
onMounted(() => {
  fetchBooks()
})
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