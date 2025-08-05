package com.itsm.notifications.infrastructure.config;

import com.itsm.notifications.infrastructure.kafka.event.AssignmentCreatedEvent;
import com.itsm.notifications.infrastructure.kafka.event.AssignmentFailedEvent;
import com.itsm.notifications.infrastructure.kafka.event.AssignmentReassignedEvent;
import com.itsm.notifications.infrastructure.kafka.event.TicketStatusChangedEvent;
import com.itsm.notifications.infrastructure.kafka.event.TicketNoteAddedEvent;
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
     * Consumer factory for TicketStatusChangedEvent with type mapping
     */
    @Bean
    public ConsumerFactory<String, TicketStatusChangedEvent> ticketStatusChangedConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, TicketStatusChangedEvent.class.getName());

        // Map ticket-service's TicketStatusChangedEvent to notifications-service's TicketStatusChangedEvent
        configProps.put(JsonDeserializer.TYPE_MAPPINGS,
            "com.itsm.ticket.infrastructure.kafka.event.TicketStatusChangedEvent:" + TicketStatusChangedEvent.class.getName());

        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(),
                new JsonDeserializer<>(TicketStatusChangedEvent.class, false));
    }

    /**
     * Consumer factory for TicketNoteAddedEvent with type mapping
     */
    @Bean
    public ConsumerFactory<String, TicketNoteAddedEvent> ticketNoteAddedConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, TicketNoteAddedEvent.class.getName());

        // Map ticket-service's TicketNoteAddedEvent to notifications-service's TicketNoteAddedEvent
        configProps.put(JsonDeserializer.TYPE_MAPPINGS,
            "com.itsm.ticket.infrastructure.kafka.event.TicketNoteAddedEvent:" + TicketNoteAddedEvent.class.getName());

        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(),
                new JsonDeserializer<>(TicketNoteAddedEvent.class, false));
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

    /**
     * Kafka listener container factory for TicketStatusChangedEvent
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TicketStatusChangedEvent> ticketStatusChangedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TicketStatusChangedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(ticketStatusChangedConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(new org.springframework.kafka.listener.DefaultErrorHandler());
        return factory;
    }

    /**
     * Kafka listener container factory for TicketNoteAddedEvent
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TicketNoteAddedEvent> ticketNoteAddedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TicketNoteAddedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(ticketNoteAddedConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(new org.springframework.kafka.listener.DefaultErrorHandler());
        return factory;
    }
}
