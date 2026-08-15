package com.oem.evwarranty.common.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Cache Configuration for VinFast EV Platform.
 * Manages in-memory / distributed caching for vehicles catalog, live telemetry, charging stations, and financial KPIs.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_VEHICLES = "vehicles";
    public static final String CACHE_CHARGING_STATIONS = "charging_stations";
    public static final String CACHE_FINANCIAL_OVERVIEW = "financial_overview";
    public static final String CACHE_TELEMETRY = "telemetry";

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(List.of(
                CACHE_VEHICLES,
                CACHE_CHARGING_STATIONS,
                CACHE_FINANCIAL_OVERVIEW,
                CACHE_TELEMETRY
        ));
        return cacheManager;
    }
}
