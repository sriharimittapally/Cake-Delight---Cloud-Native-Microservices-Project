
package com.cakedelight.notification.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.cakedelight.notification.config.RabbitMQConfig;
import com.cakedelight.notification.entity.Notification;
import com.cakedelight.notification.event.OrderCompletedEvent;
import com.cakedelight.notification.repository.NotificationRepository;
import com.cakedelight.notification.service.EmailService;

@Component
public class OrderCompletedEventListener {

    private static final Logger logger =
            LoggerFactory.getLogger(OrderCompletedEventListener.class);

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public OrderCompletedEventListener(
            NotificationRepository notificationRepository,
            EmailService emailService) {

        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleOrderCompleted(OrderCompletedEvent event) {

        logger.info("Received order completion event. Order ID: {}",
                event.getOrderId());

        Notification notification = new Notification();

        notification.setOrderId(event.getOrderId());
        notification.setCustomerName(event.getCustomerName());
        notification.setCustomerEmail(event.getCustomerEmail());
        notification.setSubject(
                "Order Confirmation - Cake Delight | Order #"
                        + event.getOrderId());

        notification.setStatus("PENDING");

        Notification savedNotification =
                notificationRepository.save(notification);

        try {

            emailService.sendOrderConfirmation(event);

            savedNotification.setStatus("SENT");

            notificationRepository.save(savedNotification);

            logger.info("Order confirmation email sent successfully. Order ID: {}",
                    event.getOrderId());

        } catch (Exception e) {

            savedNotification.setStatus("FAILED");

            notificationRepository.save(savedNotification);

            logger.error("Failed to send order confirmation email. Order ID: {}",
                    event.getOrderId(), e);
        }
    }
}
