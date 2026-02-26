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
4. 借阅申请与管理
5. 书评发布与点赞
6. 讨论区交流
7. 排行榜展示
8. 个人信息管理

### 后台管理端
1. 数据统计分析
2. 用户管理
3. 图书审核管理
4. 借阅记录管理
5. 书评审核管理
6. 讨论审核管理

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

#### 用户端
```bash
cd front/user

# 安装依赖
npm install

# 开发模式运行
npm run dev

# 生产构建
npm run build
```

用户端将在 http://localhost:3000 启动

#### 后台管理端
```bash
cd front/admin

# 安装依赖
npm install

# 开发模式运行
npm run dev

# 生产构建
npm run build
```

后台管理端将在 http://localhost:3001 启动

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
│   ├── user/                   # 用户端前端
│   │   ├── src/
│   │   │   ├── views/          # 页面组件
│   │   │   ├── layout/         # 布局组件
│   │   │   ├── router/         # 路由配置
│   │   │   ├── stores/         # 状态管理
│   │   │   ├── utils/          # 工具函数
│   │   │   ├── App.vue
│   │   │   └── main.js
│   │   ├── package.json
│   │   └── vite.config.js
│   └── admin/                  # 后台管理端前端
│       ├── src/
│       │   ├── views/
│       │   ├── layout/
│       │   ├── router/
│       │   ├── stores/
│       │   ├── utils/
│       │   ├── App.vue
│       │   └── main.js
│       ├── package.json
│       └── vite.config.js
└── database.sql                # 数据库初始化脚本
```

## 核心功能说明

### 借阅流程
1. 借阅者浏览图书并申请借阅
2. 出借者收到申请，可选择同意或拒绝
3. 同意后，出借者确认发货
4. 借阅者确认归还
5. 支持续借功能（最多2次）

### 审核机制
- 图书发布需要管理员审核
- 书评发布需要管理员审核
- 讨论发布需要管理员审核

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
- 用户端前端端口：3000
- 后台管理端端口：3001
- 后端服务端口：8080
- Redis 端口：6379
- MySQL 端口：3306

## 许可证

MIT License

