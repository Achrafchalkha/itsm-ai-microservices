package com.itsm.notifications.infrastructure.config;

import com.itsm.notifications.infrastructure.kafka.event.AssignmentCreatedEvent;
import com.itsm.notifications.infrastructure.kafka.event.AssignmentFailedEvent;
import com.itsm.notifications.infrastructure.kafka.event.AssignmentReassignedEvent;
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
 * Kafka configuration for notifications-service
 * Handles type mapping for cross-service event consumption
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    
    /**
     * Consumer factory for AssignmentCreatedEvent with type mapping
     */
    @Bean
    public ConsumerFactory<String, AssignmentCreatedEvent> assignmentCreatedConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        // Configure JSON deserializer with type mapping
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, AssignmentCreatedEvent.class.getName());
        
        // Map assignment-service's AssignmentCreatedEvent to notifications-service's AssignmentCreatedEvent
        configProps.put(JsonDeserializer.TYPE_MAPPINGS, 
            "com.itsm.assignment.infrastructure.kafka.event.AssignmentCreatedEvent:" + AssignmentCreatedEvent.class.getName());
        
        // Consumer configuration for reliability
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), 
                new JsonDeserializer<>(AssignmentCreatedEvent.class, false));
    }
    
    /**
     * Consumer factory for AssignmentReassignedEvent with type mapping
     */
    @Bean
    public ConsumerFactory<String, AssignmentReassignedEvent> assignmentReassignedConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, AssignmentReassignedEvent.class.getName());
        
        configProps.put(JsonDeserializer.TYPE_MAPPINGS, 
            "com.itsm.assignment.infrastructure.kafka.event.AssignmentReassignedEvent:" + AssignmentReassignedEvent.class.getName());
        
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), 
                new JsonDeserializer<>(AssignmentReassignedEvent.class, false));
    }
    
    /**
     * Consumer factory for AssignmentFailedEvent with type mapping
     */
    @Bean
    public ConsumerFactory<String, AssignmentFailedEvent> assignmentFailedConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, AssignmentFailedEvent.class.getName());
        
        configProps.put(JsonDeserializer.TYPE_MAPPINGS, 
            "com.itsm.assignment.infrastructure.kafka.event.AssignmentFailedEvent:" + AssignmentFailedEvent.class.getName());
        
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), 
                new JsonDeserializer<>(AssignmentFailedEvent.class, false));
    }
    
    /**
     * Kafka listener container factory for AssignmentCreatedEvent
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AssignmentCreatedEvent> assignmentCreatedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AssignmentCreatedEvent> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(assignmentCreatedConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(new org.springframework.kafka.listener.DefaultErrorHandler());
        return factory;
    }
    
    /**
     * Kafka listener container factory for AssignmentReassignedEvent
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AssignmentReassignedEvent> assignmentReassignedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AssignmentReassignedEvent> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(assignmentReassignedConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(new org.springframework.kafka.listener.DefaultErrorHandler());
        return factory;
    }
    
    /**
     * Kafka listener container factory for AssignmentFailedEvent
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AssignmentFailedEvent> assignmentFailedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AssignmentFailedEvent> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(assignmentFailedConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(new org.springframework.kafka.listener.DefaultErrorHandler());
        return factory;
    }
}
