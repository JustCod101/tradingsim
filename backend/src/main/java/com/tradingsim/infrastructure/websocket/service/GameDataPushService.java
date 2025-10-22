package com.tradingsim.infrastructure.websocket.service;

import com.tradingsim.application.service.GameApplicationService;
import com.tradingsim.domain.model.GameSession;
import com.tradingsim.domain.model.SessionStatus;
import com.tradingsim.infrastructure.websocket.message.GameDataMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 游戏数据推送服务
 * 负责定时推送市场数据和游戏状态更新
 */
@Service
public class GameDataPushService {

    private static final Logger logger = LoggerFactory.getLogger(GameDataPushService.class);

    @Autowired
    private GameWebSocketService webSocketService;

    @Autowired
    private GameApplicationService gameApplicationService;

    // 数据推送调度器
    private final ScheduledExecutorService pushScheduler = Executors.newScheduledThreadPool(5);
    
    // 活跃推送任务管理
    private final Map<String, ScheduledFuture<?>> activePushTasks = new ConcurrentHashMap<>();

    /**
     * 开始推送游戏数据
     */
    public void startDataPush(String sessionId) {
        logger.info("开始推送游戏数据: sessionId={}", sessionId);
        
        // 如果已有推送任务，先停止
        stopDataPush(sessionId);
        
        // 启动市场数据推送任务
        ScheduledFuture<?> pushTask = pushScheduler.scheduleAtFixedRate(() -> {
            try {
                pushGameFrame(sessionId);
            } catch (Exception e) {
                logger.error("推送游戏帧数据失败: sessionId={}, error={}", 
                    sessionId, e.getMessage(), e);
            }
        }, 0, 1, TimeUnit.SECONDS); // 每秒推送一次
        
        activePushTasks.put(sessionId, pushTask);
        
        // 启动会话监控
        webSocketService.startSessionMonitoring(sessionId);
    }

    /**
     * 停止推送游戏数据
     */
    public void stopDataPush(String sessionId) {
        logger.info("停止推送游戏数据: sessionId={}", sessionId);
        
        ScheduledFuture<?> pushTask = activePushTasks.remove(sessionId);
        if (pushTask != null && !pushTask.isCancelled()) {
            pushTask.cancel(false);
        }
        
        // 停止会话监控
        webSocketService.stopSessionMonitoring(sessionId);
    }

    /**
     * 暂停推送游戏数据
     */
    public void pauseDataPush(String sessionId) {
        logger.info("暂停推送游戏数据: sessionId={}", sessionId);
        
        ScheduledFuture<?> pushTask = activePushTasks.get(sessionId);
        if (pushTask != null && !pushTask.isCancelled()) {
            pushTask.cancel(false);
            activePushTasks.remove(sessionId);
        }
    }

    /**
     * 恢复推送游戏数据
     */
    public void resumeDataPush(String sessionId) {
        logger.info("恢复推送游戏数据: sessionId={}", sessionId);
        
        // 重新启动推送任务
        startDataPush(sessionId);
    }

    /**
     * 推送单帧游戏数据
     */
    private void pushGameFrame(String sessionId) {
        try {
            // 获取当前游戏会话状态
            var sessionResponse = gameApplicationService.getSessionById(sessionId);
            if (sessionResponse == null) {
                logger.warn("会话不存在，停止推送: sessionId={}", sessionId);
                stopDataPush(sessionId);
                return;
            }

            // 检查会话状态
            if (!SessionStatus.RUNNING.name().equals(sessionResponse.getStatus())) {
                logger.debug("会话未运行，跳过推送: sessionId={}, status={}", 
                    sessionId, sessionResponse.getStatus());
                return;
            }

            // 模拟市场数据（实际应用中应从数据源获取）
            int currentFrame = sessionResponse.getCurrentFrame();
            String stockCode = sessionResponse.getStockCode();
            
            // 生成模拟价格数据
            BigDecimal basePrice = BigDecimal.valueOf(100.0);
            BigDecimal volatility = BigDecimal.valueOf(0.02);
            
            BigDecimal open = generatePrice(basePrice, volatility);
            BigDecimal high = open.add(BigDecimal.valueOf(Math.random() * 2));
            BigDecimal low = open.subtract(BigDecimal.valueOf(Math.random() * 2));
            BigDecimal close = generatePrice(open, volatility);
            Long volume = (long) (Math.random() * 1000000 + 100000);

            // 推送市场数据
            webSocketService.pushMarketFrame(
                sessionId, currentFrame, stockCode, 
                close, open, high, low, close, volume
            );

            // 推送账户状态
            webSocketService.pushAccountUpdate(
                sessionId, currentFrame,
                sessionResponse.getCurrentBalance(),
                sessionResponse.getTotalReturn(),
                BigDecimal.ZERO, // position
                BigDecimal.ZERO, // unrealizedPnl
                sessionResponse.getTotalReturn(), // realizedPnl
                0 // totalTrades - using 0 as default since method doesn't exist
            );

            // 推送游戏状态更新（每10帧推送一次）
            if (currentFrame % 10 == 0) {
                // 这里需要从GameSession实体获取数据，暂时跳过
                logger.debug("推送游戏状态更新: sessionId={}, frame={}", sessionId, currentFrame);
            }

        } catch (Exception e) {
            logger.error("推送游戏帧失败: sessionId={}, error={}", 
                sessionId, e.getMessage(), e);
        }
    }

    /**
     * 生成模拟价格
     */
    private BigDecimal generatePrice(BigDecimal basePrice, BigDecimal volatility) {
        double random = Math.random() - 0.5; // -0.5 到 0.5
        double change = random * volatility.doubleValue();
        return basePrice.multiply(BigDecimal.valueOf(1 + change));
    }

    /**
     * 获取活跃推送任务数量
     */
    public int getActivePushTaskCount() {
        return activePushTasks.size();
    }

    /**
     * 检查是否正在推送数据
     */
    public boolean isPushingData(String sessionId) {
        ScheduledFuture<?> task = activePushTasks.get(sessionId);
        return task != null && !task.isCancelled() && !task.isDone();
    }

    /**
     * 关闭服务
     */
    public void shutdown() {
        logger.info("关闭GameDataPushService");
        
        // 取消所有推送任务
        activePushTasks.values().forEach(task -> {
            if (!task.isCancelled()) {
                task.cancel(false);
            }
        });
        activePushTasks.clear();
        
        // 关闭调度器
        pushScheduler.shutdown();
        try {
            if (!pushScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                pushScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            pushScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}