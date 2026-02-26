# 社区图书共享平台

## 项目简介

这是一个基于 Spring Boot + Vue 3 + Redis + MySQL 的社区图书共享平台，支持用户发布图书、借阅交易、书评讨论等功能。

## 技术栈

### 后端
- Spring Boot 2.7.14
- MyBatis Plus 3.5.3.1
- MySQL 8.0
- Redis
- JWT
- Hutool

### 前端
- Vue 3
- Element Plus
- Pinia
- Vue Router
- Axios
- ECharts

## 功能模块

### 用户端
1. 用户注册登录（手机号/邮箱）
2. 图书发布与管理
3. 图书浏览与搜索
4. 借阅申请与管理（双向确认机制）
5. 书评发布与点赞（显示用户头像和昵称）
6. 讨论区交流（支持多级嵌套回复）
7. 排行榜展示（活跃用户排名）
8. 个人信息管理

### 后台管理端
1. 数据统计分析（ECharts 可视化）
2. 用户管理（启用/禁用/删除）
3. 图书审核管理（审核/拒绝/删除）
4. 借阅记录管理（状态查看）
5. 书评审核管理（审核/拒绝/删除）
6. 讨论审核管理（讨论和回复管理）

## 安装部署

### 1. 数据库初始化

```bash
# 执行 database.sql 文件创建数据库和表
mysql -u root -p < database.sql
```

### 2. 后端部署

```bash
cd backend

# 修改 application.yml 中的数据库密码（已配置为 jimmy）

# 使用 Maven 构建
mvn clean package

# 运行
java -jar target/community-book-share-1.0.0.jar
```

后端服务将在 http://localhost:8080 启动

### 3. 前端部署

**注意：前端已合并为单一项目，包含用户端和管理员端**

```bash
cd front/user

# 安装依赖
npm install

# 开发模式运行
npm run dev

# 生产构建
npm run build
```

前端将在 http://localhost:3000 启动

**访问方式：**
- 用户端登录：http://localhost:3000/login
- 管理员登录：http://localhost:3000/admin/login（或在用户登录页点击"管理员登录"）

### 4. Redis 启动

确保 Redis 服务已启动：
```bash
redis-server
```

## 默认账号

### 管理员账号
- 账号：13800138000
- 密码：admin123

## 项目结构

```
zjp/
├── backend/                    # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/bookshare/
│   │   │   │   ├── controller/      # 控制器
│   │   │   │   ├── service/         # 服务层
│   │   │   │   ├── mapper/          # 数据访问层
│   │   │   │   ├── entity/          # 实体类
│   │   │   │   ├── config/          # 配置类
│   │   │   │   ├── common/          # 公共类
│   │   │   │   ├── util/            # 工具类
│   │   │   │   └── interceptor/     # 拦截器
│   │   │   └── resources/
│   │   │       └── application.yml  # 配置文件
│   │   └── pom.xml                  # Maven 配置
│   └── ...
├── front/
│   └── user/                   # 前端项目（包含用户端和管理员端）
│       ├── src/
│       │   ├── views/          # 页面组件
│       │   │   ├── admin/      # 管理员页面
│       │   │   │   ├── AdminLogin.vue
│       │   │   │   ├── Dashboard.vue
│       │   │   │   ├── Users.vue
│       │   │   │   ├── AdminBooks.vue
│       │   │   │   ├── Borrows.vue
│       │   │   │   ├── Reviews.vue
│       │   │   │   └── Discussions.vue
│       │   │   ├── Login.vue
│       │   │   ├── Register.vue
│       │   │   ├── Home.vue
│       │   │   ├── Books.vue
│       │   │   ├── BookDetail.vue
│       │   │   ├── PublishBook.vue
│       │   │   ├── MyBorrow.vue
│       │   │   ├── Discussion.vue
│       │   │   ├── DiscussionDetail.vue
│       │   │   ├── Rank.vue
│       │   │   └── Profile.vue
│       │   ├── layout/         # 布局组件
│       │   │   ├── MainLayout.vue      # 用户端布局
│       │   │   └── admin/
│       │   │       └── AdminLayout.vue # 管理员端布局
│       │   ├── router/         # 路由配置（统一管理）
│       │   ├── stores/         # 状态管理
│       │   ├── utils/          # 工具函数
│       │   ├── App.vue
│       │   └── main.js
│       ├── package.json
│       └── vite.config.js
├── database.sql                # 数据库初始化脚本
├── .gitignore                  # Git 忽略文件
└── README.md                   # 项目说明
```

## 核心功能说明

### 借阅流程（双向确认机制）
1. 借阅者浏览图书并申请借阅
2. 出借者收到申请，可选择同意或拒绝
3. 同意后进入借阅中状态
4. **借阅者申请归还**（状态变为"待确认归还"）
5. **出借者确认归还**（状态变为"已归还"）
6. 支持续借功能（最多2次）

### 讨论区功能
- 支持多级嵌套回复（类似贴吧楼中楼）
- 显示用户头像和昵称
- 显示回复对象（@用户名）
- 管理员可管理讨论和回复

### 排行榜系统
- 活跃用户排名（基于书评和讨论活跃度）
- 评分算法：书评数 × 2 + 讨论数 × 3
- 实时更新排名

### 审核机制
- 图书发布需要管理员审核
- 书评发布需要管理员审核
- 讨论发布需要管理员审核
- 管理员可禁用/启用回复

### 文件上传
- 图书封面上传
- 用户头像上传
- 文件存储在本地：D:/bookshare/uploads/

## 注意事项

1. 确保 MySQL、Redis 服务已启动
2. 首次运行需要执行数据库初始化脚本
3. 文件上传目录会自动创建
4. 开发环境下前端通过代理访问后端 API

## 开发说明

- 后端 API 基础路径：/api
- 前端端口：3000
- 后端服务端口：8080
- Redis 端口：6379
- MySQL 端口：3306

### 路由说明
- 用户端路由：`/` 开头（如 `/home`, `/books`）
- 管理员路由：`/admin` 开头（如 `/admin/dashboard`, `/admin/users`）
- 路由守卫自动根据用户角色进行权限控制

### 数据库配置
- 数据库名：book_share
- 默认密码：jimmy（可在 application.yml 中修改）
- 已包含管理员初始数据

## 许可证

MIT License

