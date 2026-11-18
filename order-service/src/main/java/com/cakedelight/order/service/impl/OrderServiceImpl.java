
package com.cakedelight.order.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.cakedelight.order.dto.request.CheckoutRequest;
import com.cakedelight.order.dto.response.OrderResponse;
import com.cakedelight.order.entity.Basket;
import com.cakedelight.order.entity.BasketItem;
import com.cakedelight.order.entity.Order;
import com.cakedelight.order.entity.OrderItem;
import com.cakedelight.order.event.OrderCompletedEvent;
import com.cakedelight.order.event.OrderEventPublisher;
import com.cakedelight.order.repository.BasketRepository;
import com.cakedelight.order.repository.OrderRepository;
import com.cakedelight.order.service.OrderService;
import com.cakedelight.order.util.OrderMapper;


@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger =
            LoggerFactory.getLogger(OrderServiceImpl.class);

    private final BasketRepository basketRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderEventPublisher orderEventPublisher;

    public OrderServiceImpl(BasketRepository basketRepository,
            OrderRepository orderRepository,
            OrderMapper orderMapper,
            OrderEventPublisher orderEventPublisher) {

        this.basketRepository = basketRepository;
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.orderEventPublisher = orderEventPublisher;
    }

    /**
     * Creates an order from the customer's basket, calculates the total,
     * updates the basket status, and publishes an order completion event.
     */
    @Override
    @Transactional
    public OrderResponse checkout(Long basketId, CheckoutRequest request) {

        logger.info("Starting checkout for basket ID: {}", basketId);

        Basket basket = findBasketOrThrow(basketId);
        checkBasketIsActive(basket);

        if (basket.getItems().isEmpty()) {
            logger.warn("Checkout failed. Basket is empty. Basket ID: {}", basketId);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cannot checkout an empty basket");
        }

        Order order = new Order();

        order.setBasket(basket);
        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setStatus("PLACED");

        // Create order items and calculate the total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (BasketItem basketItem : basket.getItems()) {

            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);
            orderItem.setCakeId(basketItem.getCakeId());
            orderItem.setCakeName(basketItem.getCakeName());
            orderItem.setUnitPrice(basketItem.getUnitPrice());
            orderItem.setQuantity(basketItem.getQuantity());

            BigDecimal subtotal = basketItem.getUnitPrice()
                    .multiply(BigDecimal.valueOf(basketItem.getQuantity()));

            orderItem.setSubtotal(subtotal);
            totalAmount = totalAmount.add(subtotal);
            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        // Save the order and update the basket status
        Order savedOrder = orderRepository.save(order);

        basket.setStatus("CHECKED_OUT");
        basketRepository.save(basket);

        logger.info("Order placed successfully. Order ID: {}, Total Amount: {}",
                savedOrder.getId(), savedOrder.getTotalAmount());

        // Publish an event to trigger the order confirmation notification
        OrderCompletedEvent event = new OrderCompletedEvent(
                savedOrder.getId(),
                savedOrder.getCustomerName(),
                savedOrder.getCustomerEmail(),
                savedOrder.getDeliveryAddress(),
                savedOrder.getTotalAmount(),
                orderMapper.toOrderResponse(savedOrder).getItems()
        );

        orderEventPublisher.publishOrderCompletedEvent(event);

        logger.info("Order completion event published. Order ID: {}",
                savedOrder.getId());

        return orderMapper.toOrderResponse(savedOrder);
    }

    /**
     * Retrieves all orders and converts them into response DTOs.
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {

        List<Order> orders = orderRepository.findAll();
        List<OrderResponse> responses = new ArrayList<>();

        for (Order order : orders) {
            responses.add(orderMapper.toOrderResponse(order));
        }

        logger.debug("Retrieved {} orders", responses.size());

        return responses;
    }

    /**
     * Retrieves an order by its ID or throws an exception if it does not exist.
     */
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {

        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found with ID: " + orderId));

        logger.debug("Retrieved order with ID: {}", orderId);

        return orderMapper.toOrderResponse(order);
    }

    /**
     * Finds a basket by its ID or throws a not-found exception.
     */
    private Basket findBasketOrThrow(Long basketId) {

        return basketRepository.findById(basketId).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Basket not found with ID: " + basketId));
    }

    /**
     * Checks whether the basket is active before allowing checkout.
     */
    private void checkBasketIsActive(Basket basket) {

        if (!"ACTIVE".equalsIgnoreCase(basket.getStatus())) {
            logger.warn("Checkout failed. Basket is not active. Basket ID: {}",
                    basket.getBasketId());

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Basket is not active");
        }
    }

    /**
     * Retrieves all orders belonging to a customer using their email address.
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomerEmail(String email) {

        List<Order> orders =
                orderRepository.findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(email);

        List<OrderResponse> responses = new ArrayList<>();

        for (Order order : orders) {
            responses.add(orderMapper.toOrderResponse(order));
        }

        logger.debug("Retrieved {} orders for customer", responses.size());

        return responses;
    }

    /**
     * Checks whether a customer purchased a specific cake in a particular order.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean verifyCakePurchase(Long orderId, String email, Long cakeId) {

        boolean purchased =
                orderRepository.existsByIdAndCustomerEmailIgnoreCaseAndItems_CakeId(
                        orderId, email, cakeId);

        logger.debug("Cake purchase verification completed for order ID: {}. Result: {}",
                orderId, purchased);

        return purchased;
    }
}
