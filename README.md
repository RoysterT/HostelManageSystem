## 宿舍管理系统 - HMS

### 部署方式（部分资源为 Windows x64）

1. 下载[Erlang](https://www.erlang.org/downloads)安装包并运行(RabbitMq运行前置)
2. 下载最新版本[RabbitMq](https://github.com/rabbitmq/rabbitmq-server/releases)
   并安装，完成后在开始菜单中运行`RabbitMQ Service - start`快捷方式运行RabbitMq
3. 下载[Redis-x64-HMS](https://oss.iuoyt.com/hms/Redis-x64-5.0.14.1-HMS.rar)
   压缩包，在任意目录下解压，运行目录下的`startRedis.bat`程序启动Redis
4. 在Intellij IDEA中打开项目，加载pom组件
5. 在项目资源配置文件`application.yml`中修改如下内容：
    1. Mysql配置字段
        ```yaml
        datasource:
            username: (数据库用户名)
            password: (数据库密码)
        ```
    2. 邮件配置字段
        ```yaml
        mail:
            host: (smtp服务器地址)
            username: (邮箱地址)
            password: (邮箱密码)
        ```

### 技术栈

#### 后端

- **Spring Boot**：

  Spring框架的扩展，用于快速开发Java后端应用程序，提供了诸多工具和功能。

- **Mybatis Plus**：

  MyBatis的增强工具包，简化了数据持久化的开发，提供便捷的CRUD操作。

- **Spring Security**：

  Spring的子项目，用于处理身份验证和授权，确保应用程序的安全性。

- **JWT（JSON Web Token）**：

  用于实现身份验证和在前端和后端之间安全传输信息的标准，通常用于生成和验证用户身份信息的令牌。

- **MySQL**：

  关系型数据库管理系统，用于存储和管理应用程序的数据。

- **Redis**：

  内存数据库，用于缓存和加速数据访问，提供高性能的键值存储。

- **RabbitMQ**：

  消息队列中间件，用于异步消息传递，处理消息和事件驱动的任务。

#### 前端

- **Vue.js**：

  流行的JavaScript前端框架，用于构建用户界面，提供了组件化的开发方式。

- **Vue Router**：

  Vue.js的官方路由管理器，用于管理前端应用程序的路由和导航。

- **Element UI**：

  基于Vue.js的UI组件库，提供了各种可重用的界面组件，用于创建用户友好的前端界面。

