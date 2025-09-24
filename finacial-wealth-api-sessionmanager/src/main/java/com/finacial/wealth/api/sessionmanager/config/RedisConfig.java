package com.finacial.wealth.api.sessionmanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfig {

    @Value("${fellow.redis.host}")
    private String redisHost;

    // @Value("${fellow.redis.password}")
    // private String password;
    /*@Bean
    JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(System.getenv().getOrDefault("REDIS_HOST", redisHost), 6379);
        // config.setPassword(password);
        return new JedisConnectionFactory(config);
    }*/

    @Value("${fellow.redis.password}")
    private String password;

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(System.getenv().getOrDefault("REDIS_HOST", redisHost), 6379);
       // config.setPassword(password);
        return new JedisConnectionFactory(config);
    }

    @Bean
    RedisTemplate< String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }
}
