package jpabook.jpashop.service.query;


import static java.util.stream.Collectors.toList;

import java.time.LocalDateTime;
import java.util.List;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

@Data
public class OrderDto {

    private Long orderId;
    private String name;
    private LocalDateTime orderDate; //주문시간
    private OrderStatus orderStatus;
    private Address address;
    private List<OrderItemDto> orderItems; // 이 부분에서도 List<OrderItem>과 같이 '엔티티'를 노출하면 안된다.

    public OrderDto(Order order) {
        orderId = order.getId();
        name = order.getMember().getName(); // OSIV가 꺼져 있다면 여기서 LazyInitializationException 발생
        orderDate = order.getOrderDate();
        orderStatus = order.getStatus();
        address = order.getDelivery().getAddress();
        orderItems = order.getOrderItems().stream()
                .map(orderItem -> new OrderItemDto(orderItem))
                .collect(toList());
    }
}
