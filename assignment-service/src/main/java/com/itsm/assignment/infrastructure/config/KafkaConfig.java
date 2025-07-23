package com.itsm.assignment.infrastructure.config;

import com.itsm.assignment.infrastructure.kafka.event.TicketCreatedEvent;
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
 * Kafka configuration for assignment-service
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
     * Consumer factory for TicketCreatedEvent with type mapping
     * Maps ticket-service's TicketCreatedEvent to assignment-service's TicketCreatedEvent
     */
    @Bean
    public ConsumerFactory<String, TicketCreatedEvent> ticketConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        // Configure JSON deserializer with type mapping
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, TicketCreatedEvent.class.getName());
        
        // Map ticket-service's TicketCreatedEvent to assignment-service's TicketCreatedEvent
        configProps.put(JsonDeserializer.TYPE_MAPPINGS, 
            "com.itsm.ticket.infrastructure.kafka.event.TicketCreatedEvent:" + TicketCreatedEvent.class.getName());
        
        // Consumer configuration for reliability
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), 
                new JsonDeserializer<>(TicketCreatedEvent.class, false));
    }
    
    /**
     * Kafka listener container factory for TicketCreatedEvent
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TicketCreatedEvent> ticketKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TicketCreatedEvent> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(ticketConsumerFactory());
        
        // Configure manual acknowledgment
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        // Error handling - use a simple error handler
        factory.setCommonErrorHandler(new org.springframework.kafka.listener.DefaultErrorHandler());
        
        return factory;
    }
}
