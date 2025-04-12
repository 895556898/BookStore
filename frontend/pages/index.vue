<script setup lang="ts">

import {Reading, Search, ShoppingCart} from "@element-plus/icons-vue";
import 'public/logo.png'

const searchKey = ref('')

//搜索逻辑
const handleSearch = () => {

}
</script>

<template>
  <div class="layout1">
    <el-container>
      <el-header class="layout_header" style="height: 48px">
        <el-row class="header_row" type="flex" align="middle" style="height: 100%" justify="space-between">
          <el-col :span="4">
            <el-button round class="text-logo" type="primary" @click="navigateTo('/')">遇见美好</el-button>
          </el-col>

          <el-col :span="1" :offset="14" class="cart">
            <el-button class="head_text" type="text" @click="navigateTo('/cart')">
              <el-icon :size="22"><ShoppingCart /></el-icon>购物车
            </el-button>
          </el-col>

          <el-col :span="2" >
            <el-button class="head_text" type="text" @click="navigateTo('/login')">登录账号</el-button>
          </el-col>

        </el-row>
      </el-header>

      <el-main style="height: 100%">
        <div class="main_header" >
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
<!--          <el-col :span="3" class="bookclass" style="font-size: x-large">图书分类</el-col>-->
          <el-col :span="16">
            <el-menu
                :default-active="activeIndex"
                mode="horizontal"
                class="nav navbar-nav navbar-left"
                background-color="#545c64"
                text-color="#fff"
                active-text-color="#ffd04b">

            <el-menu-item index="1" class="nav_active">
              <a href="http://localhost:3000" class="white">首页</a>
            </el-menu-item>
            <el-menu-item index="2">
              <a href="hotSell.jsp" class="white">热销</a>
            </el-menu-item>
            <el-menu-item index="3">
              <a href="newBook.jsp" class="white">新书专区</a>
            </el-menu-item>
            <el-menu-item index="4">
              <a href="discBook.jsp" class="white">特价专区</a>
            </el-menu-item>
            <el-menu-item index="5">
              <a href="recBook.jsp" class="white">推荐书单</a>
            </el-menu-item>
            </el-menu>
          </el-col>
        </el-row>
      </el-main>

    </el-container>
  </div>
</template>

<style scoped lang="scss">
.layout1 {
  max-width: 1200px;
  margin: 0 auto;
  background-color: #c4c8cc;
}//父容器限制大小

.logo {
  width: 150px;
  height: auto;
  position: relative;
  left: 20px;
}//图标大小

.text-logo {
  width: 200px;
  height: 55px;
  font-family: 楷体_GB2312,serif;
  font-size: 35px;
  letter-spacing: 6px;
  font-weight: bold;
  position: relative;
  top: 5px;
}//文字logo

.head_text {
  font-family: serif;
  font-size: 20px;
  letter-spacing: 1px;
}//文字logo

</style>