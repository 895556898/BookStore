<template>
  <div class="checkout-container">
    <div class="page-header">
      <h1>确认订单</h1>
    </div>
    
    <div v-if="loading" class="loading">
      <el-skeleton style="width: 100%" :rows="10" animated />
    </div>
    
    <div v-else-if="checkoutItems.length === 0" class="empty-checkout">
      <el-empty description="没有可结算的商品">
        <template #extra>
          <el-button type="primary" @click="router.push('/cart')">返回购物车</el-button>
        </template>
      </el-empty>
    </div>
    
    <div v-else class="checkout-content">
      <!-- 收货地址 -->
      <div class="section">
        <div class="section-title">
          <h2>收货地址</h2>
        </div>
        
        <el-form
          ref="addressFormRef"
          :model="addressForm"
          :rules="addressRules"
          label-width="100px"
        >
          <el-form-item label="收货人" prop="receiver">
            <el-input v-model="addressForm.receiver" />
          </el-form-item>

          <el-form-item label="手机号码" prop="phone">
            <el-input v-model="addressForm.phone" />
          </el-form-item>

          <el-form-item label="所在地区" prop="region">
            <el-cascader
              v-model="addressForm.region"
              :options="regionOptions"
              placeholder="请选择省/市/区"
            />
          </el-form-item>

          <el-form-item label="详细地址" prop="detailAddress">
            <el-input v-model="addressForm.detailAddress" type="textarea" :rows="2" />
          </el-form-item>

          <el-form-item label="邮政编码" prop="zipCode">
            <el-input v-model="addressForm.zipCode" />
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 订单商品 -->
      <div class="section">
        <div class="section-title">
          <h2>商品清单</h2>
          <el-button type="primary" link @click="router.push('/cart')">
            返回购物车
          </el-button>
        </div>
        
        <div class="order-items">
          <el-table :data="checkoutItems" border style="width: 100%">
            <el-table-column label="商品信息">
              <template #default="scope">
                <div class="checkout-item">
                  <div class="checkout-item-image">
                    <el-image
                      :src="scope.row.book.cover || '/default-book.jpg'"
                      fit="cover"
                      class="book-cover"
                    />
                  </div>
                  <div class="checkout-item-info">
                    <h3 class="book-title">{{ scope.row.book.title }}</h3>
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
            
            <el-table-column label="数量" width="100" prop="quantity" />
            
            <el-table-column label="小计" width="150">
              <template #default="scope">
                <span class="subtotal">￥{{ calculateSubtotal(scope.row).toFixed(2) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      
      <!-- 支付方式 -->
      <div class="section">
        <div class="section-title">
          <h2>支付方式</h2>
        </div>
        
        <div class="payment-methods">
          <el-radio-group v-model="paymentMethod">
            <el-radio label="WECHAT">微信支付</el-radio>
            <el-radio label="ALIPAY">支付宝</el-radio>
          </el-radio-group>
        </div>
      </div>
      
      <!-- 订单备注 -->
      <div class="section">
        <div class="section-title">
          <h2>订单备注</h2>
        </div>
        
        <el-input
          v-model="remark"
          type="textarea"
          :rows="2"
          placeholder="请输入订单备注（选填）"
          maxlength="100"
          show-word-limit
        />
      </div>
      
      <!-- 订单金额 -->
      <div class="order-summary">
        <div class="summary-item">
          <span>商品金额：</span>
          <span>￥{{ totalAmount.toFixed(2) }}</span>
        </div>
        </div>
        <div class="summary-item total">
          <span>应付金额：</span>
          <span class="total-price">￥{{ (totalAmount).toFixed(2) }}</span>
        </div>
        
        <div class="submit-order">
          <el-button 
            type="danger" 
            size="large" 
            @click="submitOrder"
            :loading="submitting"
          >
            提交订单
          </el-button>
        </div>
      </div>

    <!-- 支付确认对话框 -->
    <el-dialog
      v-model="showPaymentDialog"
      title="订单支付"
      width="30%"
      :close-on-click-modal="false"
    >
      <div class="payment-dialog-content">
        <p>订单号：{{ createdOrderId }}</p>
        <p>支付金额：<span class="total-price">￥{{ (totalAmount).toFixed(2) }}</span></p>
        <p>支付方式：{{ getPaymentMethodText() }}</p>
        
        <div class="payment-qr-code">
          <p>请使用{{ getPaymentMethodText() }}扫描下方二维码完成支付</p>
          <div class="qr-code">
            <el-image src="/qrcode-demo.jpg" style="width: 200px; height: 200px;"></el-image>
          </div>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelPayment">取消支付</el-button>
          <el-button type="primary" @click="confirmPayment">确认已支付</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useUserStore } from "~/stores/user.js";

const router = useRouter()
const loading = ref(true)
const submitting = ref(false)
const checkoutItems = ref([])
const paymentMethod = ref('ALIPAY')
const remark = ref('')
const showPaymentDialog = ref(false)
const addressFormRef = ref(null) // 添加表单引用
const addressForm = ref({
  receiver: '',
  phone: '',
  region: [],
  detailAddress: '',
  zipCode: '',
  isDefault: false
})
const createdOrderId = ref('')
const userStore = useUserStore()

// 地址表单验证规则
const addressRules = {
  receiver: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  region: [
    { required: true, message: '请选择所在地区', trigger: 'change' }
  ],
  detailAddress: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { min: 5, max: 100, message: '地址长度在 5 到 100 个字符', trigger: 'blur' }
  ],
  zipCode: [
    { pattern: /^\d{6}$/, message: '请输入正确的邮政编码', trigger: 'blur' }
  ]
}

// 示例的地区数据(实际项目中应该从API获取)
const regionOptions = [
  {
    value: '110000',
    label: '北京市',
    children: [
      {
        value: '110100',
        label: '北京市',
        children: [
          { value: '110101', label: '东城区' },
          { value: '110102', label: '西城区' },
          { value: '110105', label: '朝阳区' },
          { value: '110106', label: '丰台区' }
        ]
      }
    ]
  },
  {
    value: '310000',
    label: '上海市',
    children: [
      {
        value: '310100',
        label: '上海市',
        children: [
          { value: '310101', label: '黄浦区' },
          { value: '310104', label: '徐汇区' },
          { value: '310112', label: '闵行区' },
          { value: '310114', label: '嘉定区' }
        ]
      }
    ]
  }
]

// 计算总金额
const totalAmount = computed(() => {
  return checkoutItems.value.reduce((total, item) => {
    return total + calculateSubtotal(item)
  }, 0)
})

// 计算小计
const calculateSubtotal = (item) => {
  const price = item.book?.price || 0
  return price * item.quantity
}

// 从本地存储获取结算商品
const getCheckoutItems = () => {
  const items = localStorage.getItem('checkoutItems')
  if (items) {
    try {
      checkoutItems.value = JSON.parse(items)
      console.log('获取到结算商品:', checkoutItems.value)
    } catch (error) {
      console.error('解析结算商品失败:', error)
      checkoutItems.value = []
    }
  } else {
    checkoutItems.value = []
  }
  loading.value = false
}

// 提交订单
const submitOrder = async () => {
  // 验证表单
  if (!addressFormRef.value) {
    ElMessage.warning('表单引用不存在')
    return
  }
  
  // 验证表单
  let formValid = false
  await addressFormRef.value.validate((valid) => {
    formValid = valid
  })
  
  if (!formValid) {
    ElMessage.warning('请完善收货地址信息')
    return
  }
  
  if (checkoutItems.value.length === 0) {
    ElMessage.warning('没有可结算的商品')
    console.log('当前结算商品:', checkoutItems.value)
    // 尝试重新获取
    getCheckoutItems()
    if (checkoutItems.value.length === 0) {
      return
    }
  }
  
  submitting.value = true
  
  try {
    // 构建订单请求参数
    const items = checkoutItems.value.map(item => ({
      bookId: item.book.id,
      quantity: item.quantity
    }))
    
    // 构建地址信息
    const addressInfo = {
      receiver: addressForm.value.receiver,
      phone: addressForm.value.phone,
      region: addressForm.value.region,
      detailAddress: addressForm.value.detailAddress,
      zipCode: addressForm.value.zipCode
    }

    //构建用户信息
    console.log(userStore)
    console.log('————————————')
    console.log(userStore.user)
    
    const orderRequest = {
      items: items,
      userId: userStore.user.id,
      userName: userStore.user.username,
      paymentMethod: paymentMethod.value,
      addressInfo: addressInfo,
      remark: remark.value
    }
    
    console.log('提交订单请求:', orderRequest)
    
    // 发送创建订单请求
    const response = await fetch('http://localhost:8080/api/order', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(orderRequest),
      credentials: 'include'
    })
    
    const result = await response.json()
    if (result.code === 200) {
      // 订单创建成功
      createdOrderId.value = result.data.id
      localStorage.removeItem('checkoutItems') // 清除结算商品
      ElMessage.success('订单创建成功')
      
      // 跳转到订单详情页展示订单信息
      router.push({
        path: `/orders/${result.data.id}`,
        query: { orderInfo: JSON.stringify(result.data) }
      })
    } else {
      ElMessage.error(result.message || '创建订单失败')
    }
  } catch (error) {
    console.error('创建订单失败:', error)
    ElMessage.error('创建订单失败，请稍后再试')
  } finally {
    submitting.value = false
  }
}

// 获取支付方式文本
const getPaymentMethodText = () => {
  const methods = {
    'WECHAT': '微信支付',
    'ALIPAY': '支付宝',
    'BANK_CARD': '银行卡'
  }
  return methods[paymentMethod.value] || '未知支付方式'
}

// 确认支付
const confirmPayment = async () => {
  try {
    // 发送支付请求
    const response = await fetch(`${baseUrl.value}/api/order/${createdOrderId.value}/pay?paymentMethod=${paymentMethod.value}`, {
      method: 'POST',
      credentials: 'include'
    })
    
    const result = await response.json()
    if (result.code === 200) {
      ElMessage.success('支付成功')
      showPaymentDialog.value = false
      
      // 跳转到订单详情页
      router.push(`/orders/${createdOrderId.value}`)
    } else {
      ElMessage.error(result.message || '支付失败')
    }
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error('支付失败，请稍后再试')
  }
}

// 取消支付
const cancelPayment = () => {
  ElMessageBox.confirm('确定要取消支付吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '继续支付',
    type: 'warning'
  }).then(() => {
    showPaymentDialog.value = false
    router.push(`/orders/${createdOrderId.value}`)
  }).catch(() => {})
}

onMounted(() => {
  getCheckoutItems()
  
  // 初始化地址表单
  addressForm.value = {
    receiver: '',
    phone: '',
    region: [],
    detailAddress: '',
    zipCode: '',
    isDefault: false
  }
})
</script>

<style scoped>
.checkout-container {
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

.loading, .empty-checkout {
  margin: 40px 0;
  text-align: center;
}

.section {
  margin-bottom: 30px;
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.section-title h2 {
  font-size: 18px;
  margin: 0;
}

.address-info p {
  margin: 5px 0;
}

.checkout-item {
  display: flex;
  align-items: center;
}

.checkout-item-image {
  width: 80px;
  margin-right: 15px;
}

.book-cover {
  width: 80px;
  height: 80px;
  object-fit: cover;
}

.checkout-item-info {
  flex-grow: 1;
}

.book-title {
  margin: 0 0 5px;
  font-size: 16px;
  font-weight: bold;
  color: #333;
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

.payment-methods {
  padding: 15px 0;
}

.order-summary {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  margin-top: 30px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.summary-item.total {
  margin-top: 20px;
  font-size: 18px;
  font-weight: bold;
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.total-price {
  color: #ff6700;
  font-size: 20px;
}

.submit-order {
  margin-top: 20px;
  text-align: right;
}

.payment-dialog-content {
  text-align: center;
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