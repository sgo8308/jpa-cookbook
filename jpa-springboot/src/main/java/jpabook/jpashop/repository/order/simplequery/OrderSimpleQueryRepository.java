package jpabook.jpashop.repository.order.simplequery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

    private final EntityManager em;

    public List<OrderSimpleQueryDto> findOrderDtos() {
        /**
         * #jpa-springboot DTO를 이용한 방식은 fetch join이 아닌 일반 join을 쓴다.
         *
         * fetch join은 엔티티를 대상으로 하는 방식으로 Lazy Loading임에도 연관된 엔티티를 한 방에 가져오는 방식이다.
         * join을 엔티티를 대상으로 하면 Lazy Loading 방식이 작동해서 join은 하지만 Select문의 컬럼을 연관된 엔티티의 컬럼은 생략하고 가져온다.
         *
         * 하지만 Dto를 대상으로 할 경우 Lazy Loading이 작동하지 않고 필요한 컬럼을 모두 한 번에 가져오게 된다.(애초에 DTO는 Entity가 아니므로 Lazy Loading이 불가능하다.)
         */
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderSimpleQueryDto.class)
                .getResultList();
    }
}