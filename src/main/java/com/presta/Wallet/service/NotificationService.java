package com.presta.Wallet.service;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.presta.Wallet.config.TransactionMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${wallet.queue.transaction-queue}")
    private String transactionQueue;

    public void publishTransactionEvent(TransactionMessage message) {
        try {
            log.info("Publishing transaction event: transactionId={}, type={}, amount={}", 
                    message.getTransactionId(), message.getTransactionType(), message.getAmount());

            rabbitTemplate.convertAndSend(transactionQueue, message);
            
            log.debug("Transaction event published successfully: messageId={}", message.getMessageId());
            
        } catch (Exception e) {
            log.error("Failed to publish transaction event: transactionId={}, error={}", 
                     message.getTransactionId(), e.getMessage(), e);
        
        }
    }

   
    public void handleTransactionEvent(TransactionMessage message) {
        log.info("Processing transaction event: transactionId={}, type={}, status={}", 
                message.getTransactionId(), message.getTransactionType(), message.getStatus());

        try {
           
            processTransactionNotification(message);
            
        } catch (Exception e) {
            log.error("Failed to process transaction event: transactionId={}, error={}", 
                     message.getTransactionId(), e.getMessage(), e);
            throw e; // send message to DLQ
        }
    }

    private void processTransactionNotification(TransactionMessage message) {
           
        log.info("Processing notification for transaction: {}", message.getTransactionId());
        
        switch (message.getTransactionType()) {
            case "TOPUP":
                handleTopupNotification(message);
                break;
            case "CONSUMPTION":
                handleConsumptionNotification(message);
                break;
            default:
                log.warn("Unknown transaction type for notification: {}", message.getTransactionType());
        }
    }

    private void handleTopupNotification(TransactionMessage message) {
        log.info("Sending top-up confirmation: customerId={}, amount={}", 
                message.getCustomerId(), message.getAmount());
        
        
    }

    private void handleConsumptionNotification(TransactionMessage message) {
        log.info("Sending consumption confirmation: customerId={}, service={}, amount={}", 
                message.getCustomerId(), message.getServiceType(), message.getAmount());
        
        
    }
}
