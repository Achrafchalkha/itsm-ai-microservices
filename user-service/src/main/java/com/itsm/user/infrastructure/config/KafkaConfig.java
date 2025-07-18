package com.itsm.user.infrastructure.config;

import com.itsm.user.infrastructure.kafka.TeamCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration for team events
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    
    /**
     * Consumer factory for TeamCreatedEvent
     */
    @Bean
    public ConsumerFactory<String, TeamCreatedEvent> teamConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        // Configure JSON deserializer
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, TeamCreatedEvent.class.getName());
        
        // Consumer configuration for reliability
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), 
                new JsonDeserializer<>(TeamCreatedEvent.class, false));
    }
    
    /**
     * Kafka listener container factory for TeamCreatedEvent
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TeamCreatedEvent> teamKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TeamCreatedEvent> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(teamConsumerFactory());
        
        // Configure manual acknowledgment
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        // Error handling - use a simple error handler
        factory.setCommonErrorHandler(new org.springframework.kafka.listener.DefaultErrorHandler());
        
        return factory;
    }
}
