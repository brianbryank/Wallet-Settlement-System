package com.presta.Wallet.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    @Value("${wallet.queue.transaction-queue}")
    private String transactionQueueName;

    @Value("${wallet.queue.dlq-queue}")
    private String dlqQueueName;

    @Value("${wallet.exchange.name:wallet.exchange}")
    private String exchangeName;

    @Value("${wallet.routing.key:transaction.routing.key}")
    private String transactionRoutingKey;

    // Transaction Queue with DLQ binding
    @Bean
    public Queue transactionQueue() {
        return QueueBuilder.durable(transactionQueueName)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", dlqQueueName)
                .build();
    }

    // Dead Letter Queue
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(dlqQueueName).build();
    }

    // Direct Exchange
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchangeName);
    }

    // Binding transactionQueue -> exchange with routing key
    @Bean
    public Binding transactionBinding() {
        return BindingBuilder
                .bind(transactionQueue())
                .to(exchange())
                .with(transactionRoutingKey);
    }

    // Binding DLQ to default exchange
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(new DirectExchange("")) // default exchange
                .with(dlqQueueName);
    }

    // Message converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate for publishing
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
