package jpabook.jpashop.api;


import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.time.LocalDateTime;
import java.util.List;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import jpabook.jpashop.service.query.OrderQueryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * #jpa-springboot oneToMany 관계에서 조회 성능 최적화 하는 방법
 *
 * 실무에서 권장하는 방식
 * 1. 엔티티로 조회하는 방식을 가장 먼저 고려
 * 2. 엔티티 조회 방식으로 해결이 안 될 때 DTO 방식을 고려
 *    엔티티 방식은 코드 변경을 적게 가져가면서 성능 최적화나 최적화 방식의 변경이 가능하지만,
 *    DTO 방식은 엔티티 방식에 비해 비슷한 작업을 할 때 너무 많은 공수가 들어간다
 *
 * 3. DTO가 안되면 NativeSQL이나 스프링 JDBCTemplate을 사용
 */
@RestController
@RequiredArgsConstructor
public class OrderApiController { // oneToMany 일 때의 조회 성능 최적화(데이터 뻥튀기 처리 방법)

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /**
     * V1. 엔티티 직접 노출
     * - Hibernate5Module 모듈 등록, LAZY=null 처리
     * - 양방향 관계 문제 발생 -> @JsonIgnore
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAll();
        for (Order order : all) {
            order.getMember().getName(); //Lazy 강제 초기화 , OSIV가 꺼져 있을 경우 LazyInitializationException 발생
            order.getDelivery().getAddress(); //Lazy 강제 초기환
            List<OrderItem> orderItems = order.getOrderItems();
        orderItems.stream().forEach(o -> o.getItem().getName()); //Lazy 강제 초기화
        }
        return all;
    }
    /**
     * V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X) -> N + 1 문제 발생
     * - 트랜잭션 안에서 지연 로딩 필요
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAll();
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());

        return result;
    }

    /**
     * #jpa-springboot OSIV - open session in view
     *
     * session이란 EntityManager를 뜻한다.(과거 hibernate만 있던 시절 불리던 이름)
     * OSIV란 영속성 컨텍스트를 트랜잭션이 끝나는 시점에 없앨 것인지 아니면 응답이 나가는 순간까지 유지할 것인지에 관한 옵션이다.
     * OSIV를 끌 경우 영속성 컨텍스트는 Service 레이어에서 트랜잭션이 끝나는 순간 사라지기 때문에 Controller Layer에서 진행하는 Lazy Loading이 불가능하다.
     *
     * 이 때는 아래 메소드와 같은 방식으로 서비스 Layer에서 지연 로딩을 모두 초기화 시켜주어야 한다.
     * 그리고 이렇게 진행하는 서비스는 OrderQueryService로 따로 분리하는 것이 좋다. 화면에 특화되어 있어 OrderService와는 라이프사이클이 다르기 때문이다.
     *
     * OSIV를 끄지 않을 경우 Controller에서도 Lazy Loading이 가능하다는 장점이 있지만 DB Connection을 오래 물고 있기 때문에
     * Connection 부족 현상이 생길 수 있어, 실시간으로 많은 트래픽이 오고가는 서비스 API에는 적합하지 않다.
     *
     * 아래 2.1버전은 ordersV2()를 open session in view 옵션이 꺼져 있을 때의 예시다
     * application.yml에서 jpa.hibernate.open-in-view : false로 설정해주어야 한다.
     */
    private final OrderQueryService orderQueryService;
    @GetMapping("/api/v2.1/orders")
    public List<jpabook.jpashop.service.query.OrderDto> ordersV2_1() {
        return orderQueryService.orderV2();
    }

    /**
     * #jpa-springboot toMany 관계를 가진 엔티티 조회시 엔티티를 가져온 후 DTO 변환 (fetch join 사용O) - 페이징 불가능
     *
     * - 억지로 페이징 진행할 경우 메모리에 모든 데이터를 가져온 후 페이징하기 때문에 하면 안됨.
     * - 둘 이상의 컬렉션에는 페치 조인 불가
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());

        return result;
    }

    /**
     * #jpa-springboot toMany 관계를 가진 엔티티 조회시 엔티티를 가져온 후 DTO 변환 (batchsize 사용) - 페이징 가능
     *
     * - ToOne 관계만 우선 모두 페치 조인으로 최적화
     * - 컬렉션 관계는 spring.jpa.properties.hibernate.default_batch_fetch_size, @BatchSize로 최적화
     * - 페치 조인으로 한 번에 모두 가져오는 방식에 비해서 쿼리 호출 수는 약간 증가하지만 중복 데이터가 없어서
     *   DB 데이터의 네트워크 전송량이 감소한다.
     *
     * 실무에서 권장하는 방식
     * - xToOne 관계는 일단 fetch join으로 모두 불러온다.
     * - 그 이후 일대다 관계에 것들을 batchSize로 해결한다.
     *
     * ----
     *
     * BatchSize 설정 방법 2가지
     * 1. application.yml에 default_batch_fetch_size: 100으로 글로벌 적용. 글로벌이 더 많이 쓰인다.
     * 2. 일대다 관계에 원하는 리스트 변수에 @BatchSize 붙히기. 다대일이나 일대일은 클래스에 선언해야 함.
     *
     * BatchSize 동작 방식
     * 만약 Member 5명이 있고 각각 연관된 Team이 Lazy Loading으로 세팅되어 있다고 하자.
     * findAll()을 통해 모든 Member를 불러온 후에 루프를 돌면서 Team의 이름을 찾는다고 해보자.
     * 이 경우 루프를 한 번 돌 때 마다 각 Member의 Team을 찾는 쿼리가 나가서 총 5번의 추가 쿼리가 나갈 것이다.
     * 이 때 만약 BatchSize를 10으로 두면 첫 루프를 돌 때 다음과 같이 쿼리가 나가서 5개의 Team을 한번에 불러온다.
     * " select * from Team t where t.member_id in (?, ?, ?, ?, ?) " {@link com.example.jpaplayground.JPQLTest#batchSize()}
     *
     * BatchSize는 얼마나 해야 할까?
     * 100 ~ 1000개를 권장, 1000개 이상의 in 쿼리를 허용하지 않는 DB도 있기 때문.
     * 일반적으로 많이 한 번에 땡겨올수록 성능상 좋지만 그만큼 순간적으로 애플리케이션이나 DB에 부하가 갈 수 있기 때문에
     * 부하 정도와 성능을 잘 판단해서 적용하는 것이 좋음.
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                        @RequestParam(value = "limit", defaultValue = "100") int limit) {

        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());

        return result;
    }

    /**
     * toMany 관계를 가진 엔티티 조회시 DTO로 바로 가져오기 (1 + N Query)
     *
     * 페이징 가능
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    /**
     * #jpa-springboot toMany 관계를 가진 엔티티 조회시 DTO로 바로 가져오기 - in절을 사용해서 '다' 쪽의 데이터를 한 번에 긁어오기(1 + 1 Query)
     *
     * 페이징 가능
     *
     * - V5와 V6는 상황에 따라 성능의 우위가 달라질 수 있다. V5는 쿼리가 한 번 더 나가지만 V6는 뻥튀기되는 데이터가 많으면 V6는 너무 많은 중복 데이터로 느려진다.
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    /**
     * #jpa-springboot toMany 관계를 가진 엔티티 조회시 DTO로 바로 가져오기  - 뻥튀기 된 데이터를 그대로 쿼리 한 방에 받아온 후 애플리케이션에서 직접 조작(1 Query)
     *
     * 성능은 좋지만 매우 귀찮고 복잡해진다.
     * 페이징 불가능... 위의 2개는 일단 toOne 관계의 것들을 먼저 페이징해서 가져온 후에 toMany 엔티티를 조회하는 방식이라 페이징 가능
     *
     *
     * - V5와 V6는 상황에 따라 성능의 우위가 달라질 수 있다. V5는 쿼리가 한 번 더 나가지만 V6는 뻥튀기되는 데이터가 많으면 V6는 너무 많은 중복 데이터로 느려진다.
     */
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .collect(toList());
    }


    @Data
    static class OrderDto {

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

    @Data
    static class OrderItemDto {

        private String itemName;//상품 명
        private int orderPrice; //주문 가격
        private int count;      //주문 수량

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

}
