# Agent-Free AI Server 项目文档

## 1. 项目概述

### 1.1 项目名称
Agent-Free AI Server

### 1.2 项目定位
基于Spring Boot的动态AI Agent管理系统，支持多策略聊天、流式响应、工具注入和会话管理

### 1.3 核心价值
- 动态Agent配置与管理
- 多策略聊天支持
- Server-Sent Events (SSE) 流式响应
- 工具注入与调用
- 会话管理与持久化
- 意图识别与策略路由

## 2. 技术栈

| 类别 | 技术/框架 | 版本 | 用途 |
|------|-----------|------|------|
| 基础框架 | Spring Boot | 3.4.2 | 应用框架 |
| AI框架 | Spring AI | 1.1.2 | AI模型集成 |
| 服务发现 | Nacos | - | 服务注册与配置中心 |
| 远程调用 | OpenFeign | - | 服务间通信 |
| 负载均衡 | Spring Cloud LoadBalancer | - | 服务负载均衡 |
| 数据库 | MySQL | - | 数据持久化 |
| 工具库 | Hutool | - | 通用工具类 |
| 文档处理 | Apache POI | - | Word/Excel/PDF处理 |
| 图表生成 | JFreeChart | - | 数据可视化 |
| 日志框架 | SLF4J | - | 日志记录 |
| 代码简化 | Lombok | 1.18.24 | 减少样板代码 |
| JSON处理 | FastJSON | - | JSON序列化/反序列化 |

## 3. 核心功能模块

### 3.1 Agent管理模块
- **动态Agent配置**：从数据库加载Agent配置
- **Agent创建与初始化**：基于配置动态创建Agent实例
- **工具注入**：支持动态向Agent注入工具
- **会话管理**：支持Agent会话状态持久化

### 3.2 聊天策略模块
- **意图识别**：识别用户意图，路由到对应策略
- **多策略支持**：
  - 通用聊天策略
  - 指标查询策略
  - 文件处理策略
  - 用户列表策略

### 3.3 SSE响应模块
- **流式响应**：支持AI模型流式输出
- **连接管理**：管理SSE连接生命周期
- **消息分组**：按Q&A交互分组消息
- **会话持久化**：会话信息保存到数据库

### 3.4 工具调用模块
- **工具定义**：支持自定义工具
- **工具注入**：动态注入到Agent
- **工具调用**：AI模型调用工具获取外部数据

## 4. 架构设计

### 4.1 分层架构
```
┌───────────────────────────────────────────────┐
│                 REST API层                    │
├───────────────────────────────────────────────┤
│               策略与路由层                     │
├───────────────────────────────────────────────┤
│               Agent管理层                      │
├───────────────────────────────────────────────┤
│               AI模型与工具层                    │
├───────────────────────────────────────────────┤
│               数据持久化层                     │
└───────────────────────────────────────────────┘
```

### 4.2 核心设计模式
- **策略模式**：不同聊天策略的实现与选择
- **工厂模式**：Agent实例的创建
- **建造者模式**：Agent的灵活构建
- **模板方法**：Agent基础行为定义
- **观察者模式**：Agent配置变更通知

### 4.3 关键流程

#### 4.3.1 聊天请求处理流程
1. 前端发送聊天请求
2. 意图识别与策略选择
3. 创建或获取对应Agent
4. Agent处理请求，调用AI模型
5. 流式响应通过SSE发送到前端
6. 消息持久化到数据库

#### 4.3.2 Agent创建流程
1. 从数据库加载Agent配置
2. 初始化AgentBuilder
3. 配置ChatClient、工具、聊天记忆
4. 构建Agent实例
5. 注入SSE管理器
6. 注册到Agent管理池

## 5. 关键类和组件

### 5.1 核心Agent类

#### 5.1.1 BaseAgent
- **用途**：Agent抽象基类，定义公共行为
- **核心方法**：
  - `chat()`：同步聊天方法
  - `chatStream()`：流式聊天方法
  - `createPrompt()`：创建提示词
- **依赖**：ChatClient、PromptManager

#### 5.1.2 GenericDynamicAgent
- **用途**：通用动态Agent实现
- **核心功能**：
  - 支持动态配置
  - 工具注入与调用
  - 流式响应处理
- **所在位置**：`dynamic/GenericDynamicAgent.java`

#### 5.1.3 AgentBuilder
- **用途**：Agent建造者，用于灵活构建Agent
- **核心方法**：
  - `withChatClient()`：设置ChatClient
  - `withTools()`：设置工具列表
  - `withSseEmitterManager()`：设置SSE管理器
  - `build()`：构建Agent实例

### 5.2 Agent管理组件

#### 5.2.1 DynamicAgentManager
- **用途**：动态Agent管理器
- **核心功能**：
  - Agent配置加载与更新
  - Agent实例创建与管理
  - 任务状态管理
  - 安全重载Agent
- **所在位置**：`dynamic/DynamicAgentManager.java`

#### 5.2.2 AgentFactory
- **用途**：Agent工厂，创建不同类型的Agent
- **核心方法**：
  - `createAgent()`：根据类型创建Agent
- **所在位置**：`factory/AgentFactory.java`

### 5.3 SSE管理组件

#### 5.3.1 AiGlobalSseEmitterManager
- **用途**：SSE连接管理器
- **核心功能**：
  - SSE连接创建与关闭
  - 事件发送
  - 流式响应处理
- **所在位置**：`common/sse/AiGlobalSseEmitterManager.java`

#### 5.3.2 SessionManagementService
- **用途**：会话管理服务
- **核心功能**：
  - 会话创建与查询
  - 会话状态更新
  - 会话持久化

### 5.4 策略组件

#### 5.4.1 IntentBasedRequestContext
- **用途**：意图识别与策略路由
- **核心功能**：
  - 意图识别
  - 策略选择
  - 请求路由

#### 5.4.2 GeneralChatStrategy
- **用途**：通用聊天策略
- **核心功能**：处理无明确意图的聊天请求

## 6. 实现思路

### 6.1 动态Agent设计
- **配置驱动**：Agent配置存储在数据库，支持动态更新
- **灵活构建**：使用Builder模式构建Agent，支持多种配置组合
- **工具注入**：支持运行时向Agent注入工具
- **会话持久化**：会话状态保存到数据库，支持跨会话连续对话

### 6.2 SSE流式响应
- **响应式编程**：使用Reactor处理流式响应
- **连接管理**：维护SSE连接池，支持多客户端并发
- **消息分组**：按Q&A交互分组消息，便于前端展示
- **异常处理**：优雅处理连接关闭、超时等异常

### 6.3 工具调用机制
- **工具定义**：通过注解定义工具方法
- **动态注入**：运行时将工具注入到Agent
- **安全调用**：严格的参数验证和异常处理
- **结果解析**：自动解析工具调用结果

### 6.4 会话管理
- **会话标识**：每个会话分配唯一ID
- **状态持久化**：会话信息保存到数据库
- **活动跟踪**：记录会话最后活跃时间
- **会话过期**：支持会话自动过期清理

## 7. 当前成果

### 7.1 已实现功能
- 动态Agent配置与管理
- SSE流式响应
- 多策略聊天支持
- 工具注入与调用
- 会话管理与持久化
- 意图识别与策略路由

### 7.2 修复的关键问题
- **SSE连接管理**：修复SSE连接提前关闭问题
- **工具注入**：修复重复工具注入问题
- **类型转换**：修复日期时间类型转换异常
- **SpringContextUtil调用**：修复@PostConstruct中调用SpringContextUtil失败问题

### 7.3 项目状态
- 项目已成功构建
- 应用程序正常运行
- 模型输出能够通过SSE发送到前端
- 支持动态Agent配置与更新

## 8. 代码组织

### 8.1 目录结构
```
├── agent/               # Agent核心模块
│   ├── builder/         # Agent构建器
│   ├── core/            # Agent核心接口
│   ├── dynamic/         # 动态Agent实现
│   ├── factory/         # Agent工厂
│   ├── impl/            # 具体Agent实现
│   ├── manager/         # Prompt管理
│   └── template/        # Agent模板
├── common/              # 通用模块
│   ├── sse/             # SSE相关实现
│   └── data/            # 数据处理
├── config/              # 配置类
├── constant/            # 常量定义
├── feign/               # Feign客户端
├── interceptor/         # 拦截器
├── rest/                # REST API
│   ├── controller/      # 控制器
│   ├── entity/          # 实体类
│   ├── request/         # 请求类
│   ├── response/        # 响应类
│   └── service/         # 业务服务
├── strategy/            # 策略实现
└── tools/               # 工具类
```

### 8.2 核心文件位置
- **入口类**：`MetricAiMain.java`
- **Agent管理**：`dynamic/DynamicAgentManager.java`
- **SSE管理**：`common/sse/AiGlobalSseEmitterManager.java`
- **策略路由**：`strategy/context/IntentBasedRequestContext.java`
- **REST API**：`rest/controller/WebAiController.java`

## 9. 未来发展方向

### 9.1 功能增强
- 支持更多AI模型
- 增强工具调用能力
- 支持多模态交互
- 增强会话管理功能

### 9.2 性能优化
- 优化Agent创建和初始化
- 优化SSE连接管理
- 优化数据库查询性能

### 9.3 扩展性提升
- 支持插件式工具开发
- 支持更多策略扩展
- 增强配置管理能力

### 9.4 可观测性
- 增强日志记录
- 添加监控指标
- 支持分布式追踪

## 10. 关键API说明

### 10.1 聊天API
- **路径**：`/api/ai/chat`
- **方法**：POST
- **请求体**：`RequestAi`
- **响应**：SSE流式响应

### 10.2 Agent配置API
- **路径**：`/api/agent/config`
- **方法**：GET/POST/PUT/DELETE
- **功能**：管理Agent配置

### 10.3 会话管理API
- **路径**：`/api/session`
- **方法**：GET/POST
- **功能**：管理会话信息

## 11. 部署说明

### 11.1 环境要求
- JDK 17+
- MySQL 5.7+
- Nacos服务

### 11.2 部署步骤
1. 配置Nacos服务
2. 创建MySQL数据库
3. 配置应用属性
4. 启动应用

### 11.3 启动命令
```bash
java -jar c2000-ai-server-metric-1.0-SNAPSHOT.jar
```

## 12. 监控与维护

### 12.1 日志监控
- 应用日志：`logs/`目录
- Nacos监控：Nacos控制台

### 12.2 常见问题排查
- **Agent创建失败**：检查数据库配置和权限
- **SSE连接关闭**：检查网络和防火墙配置
- **工具调用失败**：检查工具实现和依赖

## 13. 总结

Agent-Free AI Server是一个基于Spring Boot和Spring AI的动态AI Agent管理系统，支持多策略聊天、流式响应、工具注入和会话管理。该系统采用了模块化设计，具有良好的扩展性和可维护性，能够满足不同场景下的AI应用需求。

系统的核心优势包括：
- 动态Agent配置，支持热更新
- 多策略聊天支持，灵活应对不同场景
- 高效的SSE流式响应
- 强大的工具调用能力
- 完善的会话管理
- 良好的可扩展性

该系统已经成功修复了多个关键问题，能够稳定运行，为前端提供流畅的AI聊天体验。