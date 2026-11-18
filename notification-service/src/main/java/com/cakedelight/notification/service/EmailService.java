
package com.cakedelight.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.cakedelight.notification.dto.response.OrderItemResponse;
import com.cakedelight.notification.event.OrderCompletedEvent;

@Service
public class EmailService {

    private static final Logger logger =
            LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOrderConfirmation(OrderCompletedEvent event) {

        logger.info("Sending confirmation email for order ID: {}", event.getOrderId());

        String subject = "Order Confirmation - Cake Delight";

        // Build the email content using customer and order details
        StringBuilder body = new StringBuilder();

        body.append("Hello ").append(event.getCustomerName()).append(",\n\n");
        body.append("Your order has been successfully placed!\n\n");

        body.append("Order ID: #").append(event.getOrderId()).append("\n");
        body.append("Delivery Address: ").append(event.getDeliveryAddress()).append("\n\n");

        body.append("Order Details:\n");

        for (OrderItemResponse item : event.getItems()) {
            body.append("\nCake: ").append(item.getCakeName()).append("\n");
            body.append("Quantity: ").append(item.getQuantity()).append("\n");
            body.append("Subtotal: ₹").append(item.getSubtotal()).append("\n");
        }

        body.append("\nTotal Amount: ₹").append(event.getTotalAmount());
        body.append("\n\nThank you for choosing Cake Delight!");
        body.append("\n\nRegards,\nCake Delight Team");

        // Set the recipient, subject, and message before sending
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(event.getCustomerEmail());
        message.setSubject(subject);
        message.setText(body.toString());

        mailSender.send(message);

        logger.info("Order confirmation email sent for order ID: {}", event.getOrderId());
    }
}