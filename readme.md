# BookStore 在线书店系统

一个功能完整的在线书店管理系统，采用前后端分离架构，支持图书浏览、购物车、订单管理和后台管理等功能。

## 项目架构

### 后端技术栈
- **Java 21** - 主要编程语言
- **Spring Boot 3.4.4** - 主框架
- **Spring Security** - 安全认证框架
- **Spring Data JPA** - 数据持久层
- **MyBatis-Flex** - ORM框架
- **MySQL 8.0** - 数据库
- **Lombok** - 代码简化工具

### 前端技术栈
- **Nuxt.js 3.16.2** - Vue.js 全栈框架
- **Vue 3.5.13** - 前端框架
- **Element Plus** - UI组件库
- **Pinia** - 状态管理
- **TypeScript** - 类型安全

## 主要功能

### 图书管理
- 图书信息展示（书名、作者、ISBN、价格、库存等）
- 图书搜索和筛选功能
- 按标签分类浏览
- 图书封面上传功能

### 用户系统
- 用户注册与登录
- 角色权限管理（普通用户/管理员）
- 会话状态管理

### 购物车与订单
- 购物车商品管理
- 订单创建和状态跟踪
- 支付方式选择

### 管理员功能
- 图书信息管理（增删改查）
- 图书上架/下架管理
- 权限控制（仅管理员可访问）

### 前端页面
- 首页展示（轮播图、推荐书籍、新书上架）
- 用户登录注册页面
- 购物车和结账页面
- 管理员后台页面

## 项目结构

```
BookStore/
├── backend/                    # 后端Spring Boot应用
│   ├── src/main/java/com/zwj/backend/
│   │   ├── controller/         # REST API控制器
│   │   ├── entity/            # 数据实体类
│   │   ├── service/           # 业务逻辑层
│   │   ├── mapper/            # 数据访问层
│   │   ├── config/            # 配置类
│   │   └── common/            # 公共组件
│   ├── src/main/resources/    # 资源文件
│   └── build.gradle           # 构建配置
└── frontend/                  # 前端Nuxt.js应用
    ├── pages/                 # 页面组件
    ├── layouts/               # 布局组件
    ├── stores/                # Pinia状态管理
    ├── utils/                 # 工具函数
    ├── nuxt.config.ts         # Nuxt配置
    └── package.json           # 依赖管理
```

## 数据库配置

项目使用MySQL数据库，配置信息：

支持的主要数据表：
- `book` - 图书信息表
- `user` - 用户信息表
- `orders` - 订单信息表

## API代理配置

前端通过Nuxt.js配置API代理，自动转发到后端服务：

## 开发环境启动

### 后端启动
```bash
cd backend
./gradlew bootRun
```
检查 backend/src/main/resources/application.yml 文件有无信息存在问题，例如

### 前端启动
```bash
cd frontend
npm install
npm run dev
```

## 文件上传功能

系统支持图书封面上传，最大支持10MB文件：

## 安全特性

- 基于Spring Security的用户认证
- 角色权限控制
- 会话管理和状态检查

## 技术亮点

1. **前后端分离架构**：清晰的业务逻辑分层
2. **响应式设计**：基于Element Plus的现代化UI
3. **状态管理**：使用Pinia进行前端状态管理
4. **ORM框架**：使用MyBatis-Flex提供高效的数据访问
5. **权限控制**：基于注解的方法级权限控制

这是一个功能完整、架构清晰的现代化在线书店系统，适合作为Java的学习辅助项目。

## Notes

该项目展现了完整的全栈开发能力，包含了现代web应用的核心功能：用户管理、商品管理、购物车、订单系统和后台管理。技术栈选择使用了当前主流的Spring Boot 3.x + Vue 3 + TypeScript组合，具有很好的可维护性和扩展性。项目结构清晰，前后端分离设计使得各层职责明确，非常适合自己在大学中深入学习Java等语言的软件实现。