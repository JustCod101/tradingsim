package com.tradingsim.infrastructure.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * SPI管理器
 * 负责加载和管理所有SPI实现
 */
@Component
public class SpiManager {

    private static final Logger logger = LoggerFactory.getLogger(SpiManager.class);

    private final Map<Class<?>, List<Object>> spiProviders = new ConcurrentHashMap<>();

    @PostConstruct
    public void loadSpiProviders() {
        logger.info("开始加载SPI提供者...");
        
        loadProviders(MarketDataProvider.class);
        loadProviders(GameStrategyProvider.class);
        loadProviders(NotificationProvider.class);
        
        logger.info("SPI提供者加载完成");
    }

    /**
     * 加载指定类型的SPI提供者
     */
    @SuppressWarnings("unchecked")
    private <T> void loadProviders(Class<T> serviceClass) {
        try {
            ServiceLoader<T> serviceLoader = ServiceLoader.load(serviceClass);
            List<T> providers = new ArrayList<>();
            
            for (T provider : serviceLoader) {
                providers.add(provider);
                logger.info("加载SPI提供者: {} - {}", serviceClass.getSimpleName(), provider.getClass().getName());
            }
            
            // 按优先级排序
            if (!providers.isEmpty()) {
                providers.sort(this::compareProviders);
                spiProviders.put(serviceClass, (List<Object>) providers);
                logger.info("为 {} 加载了 {} 个提供者", serviceClass.getSimpleName(), providers.size());
            }
        } catch (Exception e) {
            logger.error("加载SPI提供者失败: {}", serviceClass.getSimpleName(), e);
        }
    }

    /**
     * 比较提供者优先级
     */
    private int compareProviders(Object a, Object b) {
        int priorityA = getPriority(a);
        int priorityB = getPriority(b);
        return Integer.compare(priorityA, priorityB);
    }

    /**
     * 获取提供者优先级
     */
    private int getPriority(Object provider) {
        try {
            if (provider instanceof MarketDataProvider) {
                return ((MarketDataProvider) provider).getPriority();
            } else if (provider instanceof GameStrategyProvider) {
                return ((GameStrategyProvider) provider).getPriority();
            } else if (provider instanceof NotificationProvider) {
                return ((NotificationProvider) provider).getPriority();
            }
        } catch (Exception e) {
            logger.warn("获取提供者优先级失败: {}", provider.getClass().getName(), e);
        }
        return Integer.MAX_VALUE; // 默认最低优先级
    }

    /**
     * 获取所有指定类型的提供者
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getProviders(Class<T> serviceClass) {
        List<Object> providers = spiProviders.get(serviceClass);
        if (providers == null) {
            return Collections.emptyList();
        }
        return providers.stream()
                .map(provider -> (T) provider)
                .collect(Collectors.toList());
    }

    /**
     * 获取启用的指定类型提供者
     */
    public <T> List<T> getEnabledProviders(Class<T> serviceClass) {
        return getProviders(serviceClass).stream()
                .filter(this::isProviderEnabled)
                .collect(Collectors.toList());
    }

    /**
     * 获取第一个启用的提供者
     */
    public <T> Optional<T> getFirstEnabledProvider(Class<T> serviceClass) {
        return getEnabledProviders(serviceClass).stream().findFirst();
    }

    /**
     * 根据名称获取提供者
     */
    public <T> Optional<T> getProviderByName(Class<T> serviceClass, String name) {
        return getProviders(serviceClass).stream()
                .filter(provider -> name.equals(getProviderName(provider)))
                .findFirst();
    }

    /**
     * 检查提供者是否启用
     */
    private boolean isProviderEnabled(Object provider) {
        try {
            if (provider instanceof MarketDataProvider) {
                return ((MarketDataProvider) provider).isEnabled();
            } else if (provider instanceof GameStrategyProvider) {
                return ((GameStrategyProvider) provider).isEnabled();
            } else if (provider instanceof NotificationProvider) {
                return ((NotificationProvider) provider).isEnabled();
            }
        } catch (Exception e) {
            logger.warn("检查提供者启用状态失败: {}", provider.getClass().getName(), e);
        }
        return false;
    }

    /**
     * 获取提供者名称
     */
    private String getProviderName(Object provider) {
        try {
            if (provider instanceof MarketDataProvider) {
                return ((MarketDataProvider) provider).getProviderName();
            } else if (provider instanceof GameStrategyProvider) {
                return ((GameStrategyProvider) provider).getStrategyName();
            } else if (provider instanceof NotificationProvider) {
                return ((NotificationProvider) provider).getProviderName();
            }
        } catch (Exception e) {
            logger.warn("获取提供者名称失败: {}", provider.getClass().getName(), e);
        }
        return provider.getClass().getSimpleName();
    }

    /**
     * 重新加载SPI提供者
     */
    public void reloadProviders() {
        logger.info("重新加载SPI提供者...");
        spiProviders.clear();
        loadSpiProviders();
    }

    /**
     * 获取所有提供者信息
     */
    public Map<String, List<String>> getAllProvidersInfo() {
        Map<String, List<String>> info = new HashMap<>();
        
        spiProviders.forEach((serviceClass, providers) -> {
            List<String> providerNames = providers.stream()
                    .map(this::getProviderName)
                    .collect(Collectors.toList());
            info.put(serviceClass.getSimpleName(), providerNames);
        });
        
        return info;
    }
}