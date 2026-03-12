package ru.sg.order.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ru.sg.order.dto.request.CreateOrderRequest;
import ru.sg.order.entity.Order;
import ru.sg.order.enums.OrderStatus;
import ru.sg.order.repository.OrderRepository;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=localhost:29092",
        "spring.kafka.producer.bootstrap-servers=localhost:29092",
        "spring.kafka.consumer.bootstrap-servers=localhost:29092"
})
class OrderPaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateOrderRequest validOrder;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();

        validOrder = CreateOrderRequest.builder()
                .name("Test Product")
                .price(BigDecimal.valueOf(150))
                .quantity(2)
                .build();
    }


    @Test
    void testCreateOrderSuccess() throws Exception {
        mockMvc.perform(post("/orders")
                        .header("X-USER-ID", "test-user-123")
                        .header("X-EMAIL", "user@example.com")
                        .header("X-USERNAME", "testuser")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validOrder)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.sagaId", notNullValue()))
                .andExpect(jsonPath("$.keycloakId").value("test-user-123"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        var orders = orderRepository.findAll();
        assert !orders.isEmpty();
        assert orders.getFirst().getStatus() == OrderStatus.PENDING;
    }


    @Test
    void testCreateOrderWithInvalidPrice() throws Exception {
        CreateOrderRequest invalidOrder = CreateOrderRequest.builder()
                .name("Invalid Product")
                .price(BigDecimal.valueOf(-10))
                .quantity(1)
                .build();

        mockMvc.perform(post("/orders")
                        .header("X-USER-ID", "test-user-123")
                        .header("X-EMAIL", "user@example.com")
                        .header("X-USERNAME", "testuser")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidOrder)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testGetOrderById() throws Exception {
        Order order = Order.builder()
                .sagaId("test-saga-123")
                .keycloakId("test-user-123")
                .name("Test Product")
                .price(BigDecimal.valueOf(100))
                .quantity(1)
                .status(OrderStatus.PENDING)
                .build();
        Order savedOrder = orderRepository.save(order);

        mockMvc.perform(get("/orders/" + savedOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedOrder.getId().intValue()))
                .andExpect(jsonPath("$.sagaId").value("test-saga-123"));
    }
}