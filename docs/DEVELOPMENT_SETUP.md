# TradingSim 开发环境搭建指南

## 📋 目录

- [系统要求](#系统要求)
- [快速开始](#快速开始)
- [详细安装步骤](#详细安装步骤)
- [开发工具配置](#开发工具配置)
- [项目结构说明](#项目结构说明)
- [常用开发命令](#常用开发命令)
- [调试指南](#调试指南)
- [常见问题](#常见问题)

---

## 系统要求

### 基础环境
```yaml
操作系统:
  - macOS 10.15+ / Windows 10+ / Ubuntu 18.04+
  
硬件要求:
  - CPU: 4核心以上
  - 内存: 8GB以上 (推荐16GB)
  - 磁盘: 20GB可用空间
  - 网络: 稳定的互联网连接
```

### 必需软件
```yaml
核心工具:
  - Git 2.30+
  - Docker 20.10+
  - Docker Compose 2.0+
  - Node.js 18+ (LTS版本)
  - Java 17+ (推荐OpenJDK)
  - Maven 3.8+

推荐工具:
  - IntelliJ IDEA / VS Code
  - Postman / Insomnia
  - DBeaver / pgAdmin
  - k6 (负载测试)
```

---

## 快速开始

### 🚀 一键启动 (推荐新手)
```bash
# 1. 克隆项目
git clone https://github.com/your-org/tradingsim.git
cd tradingsim

# 2. 启动所有服务 (包含数据库、缓存、监控)
docker compose up -d

# 3. 等待服务启动 (约2-3分钟)
docker compose logs -f

# 4. 访问应用
open http://localhost:5173  # 前端应用
open http://localhost:8080  # 后端API
open http://localhost:3000  # Grafana监控
```

### ⚡ 开发模式启动
```bash
# 1. 启动基础服务 (数据库、缓存)
docker compose up -d db redis

# 2. 启动后端 (开发模式)
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 3. 启动前端 (开发模式)
cd frontend
npm install
npm run dev

# 4. 启动监控 (可选)
cd monitoring
./start-monitoring.sh
```

---

## 详细安装步骤

### 1. 安装基础工具

#### macOS (使用Homebrew)
```bash
# 安装Homebrew (如果未安装)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# 安装必需工具
brew install git
brew install --cask docker
brew install node@18
brew install openjdk@17
brew install maven

# 配置Java环境
echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 17)' >> ~/.zshrc
source ~/.zshrc
```

#### Ubuntu/Debian
```bash
# 更新包管理器
sudo apt update

# 安装Git
sudo apt install git

# 安装Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# 安装Node.js
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# 安装Java 17
sudo apt install openjdk-17-jdk

# 安装Maven
sudo apt install maven

# 重新登录以应用Docker组权限
newgrp docker
```

#### Windows (使用Chocolatey)
```powershell
# 安装Chocolatey (管理员权限)
Set-ExecutionPolicy Bypass -Scope Process -Force
[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# 安装必需工具
choco install git
choco install docker-desktop
choco install nodejs-lts
choco install openjdk17
choco install maven
```

### 2. 验证安装
```bash
# 验证所有工具是否正确安装
git --version          # 应显示 2.30+
docker --version       # 应显示 20.10+
docker compose version # 应显示 2.0+
node --version         # 应显示 v18+
java --version         # 应显示 17+
mvn --version          # 应显示 3.8+
```

### 3. 克隆项目
```bash
# 克隆主仓库
git clone https://github.com/your-org/tradingsim.git
cd tradingsim

# 查看项目结构
tree -L 2  # 或使用 ls -la
```

### 4. 环境配置

#### 后端配置
```bash
cd backend

# 复制配置文件模板
cp src/main/resources/application-dev.yml.template src/main/resources/application-dev.yml

# 编辑配置文件 (根据需要修改)
vim src/main/resources/application-dev.yml
```

#### 前端配置
```bash
cd frontend

# 复制环境变量文件
cp .env.example .env.development

# 编辑环境变量 (根据需要修改)
vim .env.development
```

---

## 开发工具配置

### IntelliJ IDEA 配置

#### 1. 导入项目
```
File → Open → 选择 tradingsim 目录
选择 "Import project from external model" → Maven
等待索引完成
```

#### 2. 配置JDK
```
File → Project Structure → Project Settings → Project
Project SDK: 选择 Java 17
Project language level: 17
```

#### 3. 配置Maven
```
File → Settings → Build Tools → Maven
Maven home directory: 选择Maven安装目录
User settings file: 使用默认或自定义
Local repository: 使用默认或自定义
```

#### 4. 安装推荐插件
```
File → Settings → Plugins → 搜索并安装:
- Lombok
- Spring Boot
- Database Navigator
- Docker
- Vue.js
```

#### 5. 配置代码格式
```bash
# 导入代码格式配置
File → Settings → Editor → Code Style
Import Scheme → 选择项目根目录下的 .idea/codeStyles/Project.xml
```

### VS Code 配置

#### 1. 安装推荐扩展
```json
// .vscode/extensions.json
{
  "recommendations": [
    "vscjava.vscode-java-pack",
    "vmware.vscode-spring-boot",
    "vue.volar",
    "bradlc.vscode-tailwindcss",
    "ms-vscode.vscode-typescript-next",
    "esbenp.prettier-vscode",
    "dbaeumer.vscode-eslint"
  ]
}
```

#### 2. 工作区配置
```json
// .vscode/settings.json
{
  "java.home": "/path/to/java17",
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-17",
      "path": "/path/to/java17"
    }
  ],
  "spring-boot.ls.java.home": "/path/to/java17",
  "editor.formatOnSave": true,
  "editor.codeActionsOnSave": {
    "source.fixAll.eslint": true
  }
}
```

---

## 项目结构说明

### 目录结构
```
tradingsim/
├── README.md                 # 项目说明
├── TODO.md                   # 开发计划
├── RISK_ASSESSMENT.md        # 风险评估
├── docker-compose.yml        # Docker编排文件
│
├── docs/                     # 项目文档
│   ├── ARCHITECTURE.md       # 架构设计
│   ├── DEVELOPMENT_SETUP.md  # 开发环境搭建
│   ├── openapi.yaml          # API文档
│   └── websocket-protocol.md # WebSocket协议
│
├── backend/                  # 后端项目
│   ├── pom.xml              # Maven配置
│   ├── Dockerfile           # Docker镜像构建
│   └── src/main/java/       # Java源码
│
├── frontend/                 # 前端项目
│   ├── package.json         # NPM配置
│   ├── vite.config.ts       # Vite配置
│   ├── Dockerfile           # Docker镜像构建
│   └── src/                 # Vue源码
│
├── monitoring/               # 监控配置
│   ├── docker-compose.yml   # 监控栈
│   ├── prometheus.yml       # Prometheus配置
│   └── grafana/             # Grafana配置
│
├── load-testing/             # 负载测试
│   ├── README.md            # 测试说明
│   ├── run-tests.sh         # 测试脚本
│   └── *.js                 # k6测试文件
│
└── scripts/                  # 工具脚本
    └── sql/                 # 数据库脚本
```

### 配置文件说明
```yaml
核心配置:
  - docker-compose.yml: 服务编排
  - backend/pom.xml: 后端依赖
  - frontend/package.json: 前端依赖
  - monitoring/prometheus.yml: 监控配置

环境配置:
  - backend/application-*.yml: 后端环境配置
  - frontend/.env.*: 前端环境变量
  - monitoring/grafana/: 监控仪表盘

开发配置:
  - .gitignore: Git忽略文件
  - .editorconfig: 编辑器配置
  - frontend/.eslintrc.js: ESLint配置
  - frontend/tsconfig.json: TypeScript配置
```

---

## 常用开发命令

### Docker 命令
```bash
# 启动所有服务
docker compose up -d

# 查看服务状态
docker compose ps

# 查看服务日志
docker compose logs -f [service_name]

# 停止所有服务
docker compose down

# 重建并启动服务
docker compose up -d --build

# 清理所有数据
docker compose down -v
```

### 后端开发命令
```bash
cd backend

# 编译项目
./mvnw compile

# 运行测试
./mvnw test

# 启动开发服务器
./mvnw spring-boot:run

# 启动指定环境
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 打包应用
./mvnw package

# 跳过测试打包
./mvnw package -DskipTests

# 清理构建
./mvnw clean
```

### 前端开发命令
```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览构建结果
npm run preview

# 运行测试
npm run test

# 代码检查
npm run lint

# 代码格式化
npm run format

# 类型检查
npm run type-check
```

### 数据库命令
```bash
# 连接到PostgreSQL
docker compose exec db psql -U tradingsim -d tradingsim

# 执行SQL脚本
docker compose exec db psql -U tradingsim -d tradingsim -f /scripts/01_timescale_schema.sql

# 备份数据库
docker compose exec db pg_dump -U tradingsim tradingsim > backup.sql

# 恢复数据库
docker compose exec -T db psql -U tradingsim -d tradingsim < backup.sql
```

### 监控命令
```bash
cd monitoring

# 启动监控栈
./start-monitoring.sh

# 查看监控服务状态
docker compose ps

# 重启Grafana
docker compose restart grafana

# 查看Prometheus配置
docker compose exec prometheus cat /etc/prometheus/prometheus.yml
```

### 负载测试命令
```bash
cd load-testing

# 运行所有测试
./run-tests.sh all

# 运行基础负载测试
./run-tests.sh basic

# 运行游戏会话测试
./run-tests.sh game

# 运行WebSocket压力测试
./run-tests.sh websocket

# 自定义测试参数
./run-tests.sh basic --base-url http://localhost:8080 --results-dir ./results
```

---

## 调试指南

### 后端调试

#### IntelliJ IDEA 调试
```
1. 在代码中设置断点
2. 右键点击 Application.java
3. 选择 "Debug 'Application'"
4. 或者配置 Remote Debug:
   - Run → Edit Configurations
   - 添加 Remote JVM Debug
   - Host: localhost, Port: 5005
```

#### 命令行调试
```bash
# 启动调试模式
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

# 或者使用环境变量
export MAVEN_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
./mvnw spring-boot:run
```

### 前端调试

#### 浏览器调试
```
1. 打开浏览器开发者工具 (F12)
2. 在 Sources 标签页中设置断点
3. 使用 Console 进行交互式调试
4. 使用 Network 标签页查看网络请求
```

#### VS Code 调试
```json
// .vscode/launch.json
{
  "version": "0.2.0",
  "configurations": [
    {
      "name": "Launch Chrome",
      "request": "launch",
      "type": "chrome",
      "url": "http://localhost:5173",
      "webRoot": "${workspaceFolder}/frontend/src"
    }
  ]
}
```

### 数据库调试
```bash
# 查看数据库连接
docker compose exec db psql -U tradingsim -d tradingsim -c "\conninfo"

# 查看表结构
docker compose exec db psql -U tradingsim -d tradingsim -c "\dt"

# 查看表数据
docker compose exec db psql -U tradingsim -d tradingsim -c "SELECT * FROM users LIMIT 10;"

# 查看慢查询
docker compose exec db psql -U tradingsim -d tradingsim -c "SELECT query, mean_time, calls FROM pg_stat_statements ORDER BY mean_time DESC LIMIT 10;"
```

### 日志调试
```bash
# 查看应用日志
docker compose logs -f backend

# 查看特定时间段的日志
docker compose logs --since="2024-01-01T00:00:00" --until="2024-01-01T23:59:59" backend

# 查看错误日志
docker compose logs backend 2>&1 | grep ERROR

# 实时监控日志
tail -f backend/logs/application.log
```

---

## 常见问题

### 🔧 环境问题

#### Q: Docker 启动失败
```bash
# 检查Docker是否运行
docker info

# 检查端口占用
lsof -i :8080
lsof -i :5173
lsof -i :5432

# 清理Docker资源
docker system prune -a
```

#### Q: Java版本问题
```bash
# 检查Java版本
java -version
javac -version

# 设置JAVA_HOME (macOS)
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# 设置JAVA_HOME (Linux)
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
```

#### Q: Node.js版本问题
```bash
# 使用nvm管理Node.js版本
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
nvm install 18
nvm use 18
```

### 🐛 开发问题

#### Q: 后端启动失败
```bash
# 检查配置文件
cat backend/src/main/resources/application-dev.yml

# 检查数据库连接
docker compose exec db psql -U tradingsim -d tradingsim -c "SELECT 1;"

# 清理Maven缓存
./mvnw clean
rm -rf ~/.m2/repository/com/tradingsim
```

#### Q: 前端启动失败
```bash
# 清理node_modules
rm -rf frontend/node_modules
rm frontend/package-lock.json
cd frontend && npm install

# 检查环境变量
cat frontend/.env.development

# 检查端口占用
lsof -i :5173
```

#### Q: WebSocket连接失败
```bash
# 检查后端WebSocket配置
grep -r "websocket" backend/src/main/resources/

# 检查前端WebSocket配置
grep -r "websocket" frontend/src/

# 测试WebSocket连接
curl -i -N -H "Connection: Upgrade" -H "Upgrade: websocket" -H "Sec-WebSocket-Key: test" -H "Sec-WebSocket-Version: 13" http://localhost:8080/ws
```

### 📊 性能问题

#### Q: 应用响应慢
```bash
# 检查系统资源
top
htop
docker stats

# 检查数据库性能
docker compose exec db psql -U tradingsim -d tradingsim -c "SELECT * FROM pg_stat_activity;"

# 检查应用指标
curl http://localhost:8080/actuator/metrics
```

#### Q: 内存使用过高
```bash
# 检查Java堆内存
jps
jstat -gc <pid>

# 生成堆转储
jcmd <pid> GC.run_finalization
jcmd <pid> VM.gc

# 分析内存使用
docker compose exec backend jcmd 1 VM.memory_summary
```

### 🔍 调试技巧

#### 启用详细日志
```yaml
# backend/src/main/resources/application-dev.yml
logging:
  level:
    com.tradingsim: DEBUG
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
```

#### 使用Actuator端点
```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 应用信息
curl http://localhost:8080/actuator/info

# 指标信息
curl http://localhost:8080/actuator/metrics

# 环境信息
curl http://localhost:8080/actuator/env
```

#### 数据库调试
```sql
-- 启用查询日志
ALTER SYSTEM SET log_statement = 'all';
SELECT pg_reload_conf();

-- 查看慢查询
SELECT query, mean_time, calls 
FROM pg_stat_statements 
ORDER BY mean_time DESC 
LIMIT 10;
```

---

## 🎯 下一步

环境搭建完成后，建议按以下顺序进行开发：

1. **熟悉项目结构**: 阅读 [ARCHITECTURE.md](./ARCHITECTURE.md)
2. **了解API设计**: 查看 [openapi.yaml](./openapi.yaml)
3. **学习WebSocket协议**: 阅读 [websocket-protocol.md](./websocket-protocol.md)
4. **运行负载测试**: 参考 [load-testing/README.md](../load-testing/README.md)
5. **查看监控仪表盘**: 访问 http://localhost:3000

## 📞 获取帮助

如果遇到问题，可以通过以下方式获取帮助：

1. **查看文档**: 项目 `docs/` 目录下的相关文档
2. **搜索Issues**: GitHub Issues 中搜索相关问题
3. **提交Issue**: 创建新的 GitHub Issue
4. **社区讨论**: 参与项目讨论区

---

*祝你开发愉快！🚀*