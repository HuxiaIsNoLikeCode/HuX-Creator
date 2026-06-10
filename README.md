# HuX Creator

> AI 视频创作策划助手 — 多模型迭代流水线，从选题到成片的一站式创作平台

HuX Creator 是一个基于多模型协作的视频内容策划平台。通过 5 个 AI 模型的流水线协作，自动生成结构化的视频脚本、分镜设计、素材清单和画面提示词，并通过迭代优化机制确保输出质量。

## 核心特性

- **多模型流水线** — 5 个 AI 模型分工协作：类型分类 → 脚本生成 → 连贯性检查 → 素材标记 → 质量评估
- **迭代优化** — 快速模式（3 轮）和专业模式（5+ 轮），评分低于 90 分自动进入下一轮优化
- **结构化存储** — 脚本、场景、镜头、素材独立存储，支持细粒度编辑和优化
- **三种视频模板** — 营销类（结构化脚本）、知识科普类（讲解逻辑）、故事类（分镜脚本）
- **帧图提示词** — 为每个镜头生成首尾帧画面提示词，支持角色标签保持人物一致性
- **多模型切换** — 支持 Mimo、通义千问、豆包、Gemini 等模型，每个步骤可独立选择

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 运行环境 |
| Spring Boot | 3.2.5 | 应用框架 |
| Spring Data JPA | 3.2.5 | 数据持久化 |
| MySQL | 8.x | 数据库 |
| HikariCP | 5.0.1 | 连接池 |
| JJWT | 0.12.5 | JWT 认证 |
| Spring Security Crypto | 6.2.4 | 密码加密 |
| Lombok | 1.18.32 | 代码简化 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4 | 前端框架 |
| Element Plus | 2.6 | UI 组件库 |
| Pinia | 2.1 | 状态管理 |
| Vue Router | 4.3 | 路由管理 |
| Axios | 1.6 | HTTP 客户端 |
| Vite | 5.1 | 构建工具 |

## 项目结构

```
HuX Creator/
├── backend/                          # 后端 Spring Boot 项目
│   ├── src/main/java/com/hux/creator/
│   │   ├── ai/                       # AI 模块
│   │   │   ├── adapter/              # 模型适配器 (Doubao/Qwen/DeepSeek/Gemini)
│   │   │   ├── generator/            # 内容生成器 (8个)
│   │   │   │   └── pipeline/         # 流水线生成器 (5个)
│   │   │   └── prompt/               # 提示词模板
│   │   ├── controller/               # 控制器层
│   │   ├── service/                  # 服务层
│   │   ├── repository/               # 数据访问层
│   │   ├── model/                    # 数据模型
│   │   │   ├── entity/               # 实体类
│   │   │   ├── dto/                  # 数据传输对象
│   │   │   └── vo/                   # 视图对象
│   │   ├── config/                   # 配置类
│   │   └── util/                     # 工具类
│   └── src/main/resources/
│       └── application.yml.example   # 配置文件模板
├── frontend/                         # 前端 Vue 项目
│   └── src/
│       ├── api/                      # API 接口
│       ├── views/                    # 页面组件
│       ├── components/               # 公共组件
│       ├── stores/                   # Pinia 状态
│       ├── router/                   # 路由配置
│       └── styles/                   # 全局样式
└── PROJECT_PLAN.md                   # 项目设计文档
```

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.x
- Maven 3.8+

### 1. 克隆项目

```bash
git clone https://github.com/your-username/hux-creator.git
cd hux-creator
```

### 2. 配置后端

```bash
cd backend

# 复制配置文件模板
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

编辑 `src/main/resources/application.yml`，填写以下配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/hux_creator?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&autoReconnect=true
    username: your_username
    password: your_password

hux:
  ai:
    providers:
      deepseek:
        api-key: your_api_key
        base-url: https://api.deepseek.com/v1
        model: deepseek-chat
```

### 3. 创建数据库

```sql
CREATE DATABASE hux_creator DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 4. 启动后端

```bash
mvn spring-boot:run
```

后端将在 `http://localhost:8080` 启动。

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端将在 `http://localhost:3000` 启动。

### 6. 访问应用

打开浏览器访问 `http://localhost:3000`，注册账号即可开始使用。

## 使用流程

```
创建项目 → 选择视频类型/节奏/风格/模式 → 启动 AI 生成
                                              ↓
                              模型1: 类型分类 (MARKETING/KNOWLEDGE/STORY)
                                              ↓
                              模型2: 脚本生成 (场景 + 镜头拆分)
                                              ↓
                              模型3: 连贯性检查 (逻辑/视觉/时长)
                                              ↓
                              模型4: 素材标记 (画面/人物/道具)
                                              ↓
                              模型5: 评估打分 (>=90分通过)
                                              ↓
                              查看脚本 → 生成首尾帧提示词 → 导出
```

## AI 模型配置

支持多个 AI 模型提供商，通过 OpenAI 兼容接口接入：

| 提供商 | 模型 | 说明 |
|--------|------|------|
| DeepSeek | deepseek-chat | 默认模型，中文能力强 |
| 通义千问 | qwen-turbo | 阿里云大模型 |
| 豆包 | doubao-pro | 字节跳动大模型 |
| Gemini | gemini-pro | Google 大模型 |

所有模型通过统一的 `ModelAdapter` 接口接入，支持在运行时切换。

## 数据库设计

### 核心表

| 表名 | 说明 |
|------|------|
| t_user | 用户表 |
| t_project | 项目表（含视频类型/节奏/风格/模式） |
| t_script | 脚本表（含轮次/版本/评分） |
| t_scene | 场景表（含背景提示词） |
| t_shot | 镜头表（含首尾帧提示词） |
| t_material | 素材表（含角色标签） |
| t_generation_round | 生成轮次表 |
| t_evaluation | 评估表（4维度评分） |
| t_content_plan | 内容方案表（V1 兼容） |
| t_revision_record | 修订记录表 |

## API 接口

### 认证

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/user/register` | 注册 |
| POST | `/api/user/login` | 登录 |
| GET | `/api/user/info` | 获取用户信息 |

### 项目管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/project/create` | 创建项目 |
| GET | `/api/project/list` | 项目列表 |
| GET | `/api/project/{id}` | 项目详情 |
| PUT | `/api/project/{id}` | 更新项目 |
| DELETE | `/api/project/{id}` | 删除项目 |

### 内容生成（V2 流水线）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/generation/start` | 启动生成 |
| GET | `/api/generation/progress/{id}` | 查询进度 |
| GET | `/api/script/{projectId}` | 获取脚本 |
| GET | `/api/script/scene/{id}/shots` | 场景镜头 |
| GET | `/api/script/scene/{id}/materials` | 场景素材 |

### 帧图生成

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/frame/scene/{id}/background` | 生成场景背景提示词 |
| POST | `/api/frame/shot/{id}/first-frame` | 生成首帧提示词 |
| POST | `/api/frame/shot/{id}/last-frame` | 生成尾帧提示词 |
| PUT | `/api/frame/material/{id}/tag` | 更新角色标签 |

### 内容导出

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/export/markdown/{id}` | 导出 Markdown |
| GET | `/api/export/json/{id}` | 导出 JSON |
| GET | `/api/export/word/{id}` | 导出 Word |

## 开发指南

### 添加新的 AI 模型

1. 在 `ai/adapter/` 下创建新的适配器类，继承 `AbstractModelAdapter`
2. 实现 `doChat()` 方法
3. 在 `application.yml` 中添加对应的 provider 配置

```java
@Component
@ConditionalOnProperty(prefix = "hux.ai.providers.newmodel", name = "api-key")
public class NewModelAdapter extends AbstractModelAdapter {
    // 实现细节
}
```

### 添加新的生成器

1. 在 `ai/generator/` 下创建新的生成器类，继承 `BaseGenerator`
2. 实现 `getTemplate()`、`getSystemPrompt()`、`getSectionName()` 方法
3. 在 `GenerationServiceImpl` 中注入并使用

## 许可证

MIT License

## 致谢

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [Element Plus Icons](https://element-plus.org/zh-CN/component/icon.html)
