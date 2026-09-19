package com.splitbill.balanceservice.config;

import com.splitbill.balanceservice.event.ExpenseEvent;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, ExpenseEvent> expenseEventConsumerFactory() {

        Map<String, Object> properties = new HashMap<>();

        properties.put("bootstrap.servers", "localhost:9092");

        properties.put("group.id", "balance-service");

        properties.put("auto.offset.reset", "earliest");

        JacksonJsonDeserializer<ExpenseEvent> deserializer = new JacksonJsonDeserializer<>(ExpenseEvent.class, false);

        return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ExpenseEvent> expenseEventKafkaListenerContainerFactory(ConsumerFactory<String, ExpenseEvent> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, ExpenseEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        return factory;
    }
}