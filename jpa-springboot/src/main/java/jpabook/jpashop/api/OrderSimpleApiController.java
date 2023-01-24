package jpabook.jpashop.api;

import static java.util.stream.Collectors.toList;

import java.time.LocalDateTime;
import java.util.List;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * xToOne(ManyToOne, OneToOne) 관계 최적화
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController { // oneToOne, ManyToOne일 대의 조회 성능 최적화

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository; //의존관계 주입

    /**
     * #jpa-springboot V1. 컨트롤러에 엔티티 직접 노출
     *
     * - 양방향 관계로 인한 무한 루프 문제 발생 -> @JsonIgnore로 해결
     * - 프록시는 Json Library가 처리하지 못하는 문제 발생 -> Hibernate5Module 모듈 등록, LAZY=null 처리
     *
     * 그러나 이 방법들 다 필요 없고 애초에 엔티티를 직접 노출하지 말 것
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); //Lazy 강제 초기화
            order.getDelivery().getAddress(); //Lazy 강제 초기화
        }
        return all;
    }

    /**
     * #jpa-springboot V2. 컨트롤러에서 엔티티를 조회해서 DTO로 변환(fetch join 사용X)
     * - 단점: 지연로딩으로 쿼리 N번 호출
     */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAll();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(toList());

        return result;
    }

    /**
     * #jpa-springboot toOne 관계를 가진 엔티티 조회시 엔티티를 먼저 조회 후 DTO로 반환(fetch join 사용O)
     * - fetch join으로 쿼리 1번 호출
     * - 단점 : DB에서 필요한 칼럼 외에 추가적인 칼럼을 모두 갖고 오므로 약간의 성능 문제가 있을 수 있음.
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(toList());
        return result;
    }

    /**
     * #jpa-springboot toOne 관계를 가진 엔티티 조회시 DTO로 레포지토리에서 바로 반환
     *
     * - fetch join으로 쿼리 1번 호출
     * - 필요한 칼럼만 명시해서 성능 최적화 가능
     * - 단점
     *   1. 재사용성이 떨어짐 (처음 의도된 API에만 Fit하게 사용 가능)
     *   2. Repository가 API Spec 즉 화면 레이어에 논리적으로 의존하게 됨.
     *   3. 코드가 지저분하고, 추가적인 DTO 클래스를 만들어야 해서 복잡함.
     *
     * #jpa-springboot toOne 관계 엔티티 조회 방식 권장 순서
     *  1. 우선 엔티티를 DTO로 변환하는 방법을 선택한다.
     *  2. 필요하면 페치 조인으로 성능을 최적화 한다. 대부분의 성능 이슈가 해결된다.
     *  3. 그래도 안되면 DTO로 직접 조회하는 방법을 사용한다.
     *  4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접 사용한다
     */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderSimpleQueryRepository.findOrderDtos();

    }

    @Data
    static class SimpleOrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate; //주문시간
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
        }
    }

}
