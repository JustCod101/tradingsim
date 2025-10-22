#!/bin/bash

# 交易模拟器部署脚本
# 用法: ./deploy.sh [dev|prod|test]

set -e

# 默认环境为开发环境
ENVIRONMENT=${1:-dev}

echo "🚀 开始部署交易模拟器 - 环境: $ENVIRONMENT"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查Docker是否安装
check_docker() {
    if ! command -v docker &> /dev/null; then
        log_error "Docker 未安装，请先安装 Docker"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose 未安装，请先安装 Docker Compose"
        exit 1
    fi
    
    log_info "Docker 环境检查通过"
}

# 清理旧容器和镜像
cleanup() {
    log_info "清理旧容器和镜像..."
    
    # 停止并删除容器
    docker-compose down --remove-orphans || true
    
    # 删除旧镜像（可选）
    if [ "$ENVIRONMENT" = "prod" ]; then
        docker system prune -f
        log_info "生产环境清理完成"
    fi
}

# 构建应用
build_app() {
    log_info "构建应用..."
    
    # 构建后端
    log_info "构建后端应用..."
    cd backend
    ./mvnw clean package -DskipTests
    cd ..
    
    # 构建前端
    log_info "构建前端应用..."
    cd frontend
    npm ci
    npm run build
    cd ..
    
    log_info "应用构建完成"
}

# 启动服务
start_services() {
    log_info "启动服务..."
    
    case $ENVIRONMENT in
        "dev")
            docker-compose up -d db redis
            log_info "开发环境：仅启动数据库和缓存服务"
            ;;
        "test")
            docker-compose up -d
            log_info "测试环境：启动所有服务"
            ;;
        "prod")
            docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
            log_info "生产环境：启动所有服务（生产配置）"
            ;;
        *)
            log_error "未知环境: $ENVIRONMENT"
            exit 1
            ;;
    esac
}

# 健康检查
health_check() {
    log_info "执行健康检查..."
    
    # 等待服务启动
    sleep 30
    
    # 检查数据库
    if docker-compose exec -T db pg_isready -U postgres > /dev/null 2>&1; then
        log_info "✅ 数据库服务正常"
    else
        log_error "❌ 数据库服务异常"
        return 1
    fi
    
    # 检查Redis
    if docker-compose exec -T redis redis-cli ping > /dev/null 2>&1; then
        log_info "✅ Redis服务正常"
    else
        log_error "❌ Redis服务异常"
        return 1
    fi
    
    # 检查后端（如果在容器中运行）
    if [ "$ENVIRONMENT" != "dev" ]; then
        if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
            log_info "✅ 后端服务正常"
        else
            log_error "❌ 后端服务异常"
            return 1
        fi
        
        # 检查前端
        if curl -f http://localhost:5173 > /dev/null 2>&1; then
            log_info "✅ 前端服务正常"
        else
            log_error "❌ 前端服务异常"
            return 1
        fi
    fi
    
    log_info "所有服务健康检查通过"
}

# 显示服务状态
show_status() {
    log_info "服务状态:"
    docker-compose ps
    
    echo ""
    log_info "访问地址:"
    echo "  前端: http://localhost:5173"
    echo "  后端API: http://localhost:8080"
    echo "  Grafana: http://localhost:3000 (admin/admin)"
    echo "  Prometheus: http://localhost:9090"
    
    if [ "$ENVIRONMENT" = "dev" ]; then
        echo ""
        log_warn "开发环境提示:"
        echo "  - 数据库: localhost:5432 (postgres/postgres)"
        echo "  - Redis: localhost:6379"
        echo "  - 请手动启动后端和前端开发服务器"
    fi
}

# 主函数
main() {
    echo "=========================================="
    echo "    交易模拟器部署脚本"
    echo "    环境: $ENVIRONMENT"
    echo "    时间: $(date)"
    echo "=========================================="
    
    check_docker
    cleanup
    
    if [ "$ENVIRONMENT" != "dev" ]; then
        build_app
    fi
    
    start_services
    health_check
    show_status
    
    log_info "🎉 部署完成！"
}

# 错误处理
trap 'log_error "部署失败！"; exit 1' ERR

# 执行主函数
main