package com.n11bootcamp.stock_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;

import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

@Configuration
public class RabbitConfig {

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange("order.exchange");
    }

    @Bean
    public Queue stockQueue() {
        return new Queue("stock.queue");
    }

    @Bean
    public Binding binding(Queue stockQueue, TopicExchange exchange) {
        return BindingBuilder.bind(stockQueue)
                .to(exchange)
                .with("order.created");
    }

    @Bean
    public Binding paymentFailedBinding(Queue stockQueue, TopicExchange exchange) {
        return BindingBuilder.bind(stockQueue)
                .to(exchange)
                .with("payment.failed");
    }

    @Bean
    public Binding paymentSuccessBinding(Queue stockQueue, TopicExchange exchange) {
        return BindingBuilder.bind(stockQueue)
                .to(exchange)
                .with("payment.success");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {

        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());

        return factory;
    }
}
