package com.capstone.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Configure JSON serialization with Java 8 time support
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // Set serializers
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration - 30 minutes TTL for collections
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(createJsonSerializer()));

        // Custom cache configurations for different cache types
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // TalentRoute caches - 30 minutes TTL for collections
        cacheConfigurations.put("talent-routes", defaultCacheConfig);
        cacheConfigurations.put("talent-routes-with-tracks", defaultCacheConfig);
        cacheConfigurations.put("talent-routes-search",
                defaultCacheConfig.entryTtl(Duration.ofMinutes(15))); // 15 minutes TTL for search results

        // GrowthTrack caches - 30 minutes TTL for collections
        cacheConfigurations.put("growth-tracks", defaultCacheConfig);
        cacheConfigurations.put("growth-tracks-with-capsules", defaultCacheConfig);
        cacheConfigurations.put("growth-tracks-search",
                defaultCacheConfig.entryTtl(Duration.ofMinutes(15))); // 15 minutes TTL for search results

        // SkillCapsule caches - 30 minutes TTL for collections
        cacheConfigurations.put("skill-capsules", defaultCacheConfig);
        cacheConfigurations.put("skill-capsules-with-atoms", defaultCacheConfig);
        cacheConfigurations.put("skill-capsules-search",
                defaultCacheConfig.entryTtl(Duration.ofMinutes(15))); // 15 minutes TTL for search results

        // SkillAtom caches - 30 minutes TTL for collections
        cacheConfigurations.put("skill-atoms", defaultCacheConfig);
        cacheConfigurations.put("skill-atoms-search",
                defaultCacheConfig.entryTtl(Duration.ofMinutes(15))); // 15 minutes TTL for search results

        // Single entity caches - 60 minutes TTL (less frequently changed)
        RedisCacheConfiguration longTtlConfig = defaultCacheConfig.entryTtl(Duration.ofMinutes(60));
        cacheConfigurations.put("talent-route-single", longTtlConfig);
        cacheConfigurations.put("growth-track-single", longTtlConfig);
        cacheConfigurations.put("skill-capsule-single", longTtlConfig);
        cacheConfigurations.put("skill-atom-single", longTtlConfig);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    private GenericJackson2JsonRedisSerializer createJsonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }
}