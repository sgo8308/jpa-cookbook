package com.example.jpaplayground.inheritance;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
/**
 * #jpabasic 상속 전략 - joined, single table, table per class
 *
   상속 관계를 DB에서 구현할 때 기본적으로 3가지 방식이 존재한다.

     1. Joined 방식
        공통 속성을 정의하는 테이블(Item)을 하나 만들고 나머지(Book, Movie)를 다 다른 테이블로 쪼갠다.
        정규화가 된다는 장점이 있다.
        테이블이 많아지고 복잡해지는 단점과 조회시 Join으로 인해 성능이 느리다는 단점이 있다.

     2. Single table 방식
        하나의 테이블에 모든 속성을 다 넣는다.
        join이 필요 없기 때문에 성능이 좋으나 테이블이 임계치를 넘어갈 정도로 비대해지면 오히려 느리다.
        공통 속성 칼럼 외에 모두 null을 허용해주어야 하기 때문에 무결성에서 손해를 본다.

     3. table per class 방식
        각 엔티티마다 테이블을 따로 만든다. 이 방식은 사용하지 않는 게 낫다.
        그 이유는 공통된 속성(ex. price)에 대해 어떤 작업이 필요할 때 테이블을 각각 일일이 뒤져야 하고,
        Item item = entityManger.find(Item.class, 5L)과 같이 부모 타입으로 조회할 때,
        모든 테이블을 다 뒤져서(Union) 가져오기 때문에 비효율적이다.

   * 일반적으로 Joined 방식을 사용하고 매우 간단할 경우 트레이드 오프를 고려해서 Single Table 전략을 사용하자.
*/
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn // 상속하는 엔티티들을 구분하기 위한 칼럼을 DDL에 자동으로 넣어주고 이를 바탕으로 데이터를 찾게 한다.
                     // 꼭 적어주자. 옵션이 없다면 DTYPE이라는 이름의 컬럼으로 생성된다.
public abstract class Item extends BaseEntity{ // 상속 관계를 DB에 적용할 때 반드시 abstract로 해주어야 Item 테이블이 따로 생성되지 않음

    @Id @GeneratedValue
    private Long id;

    private String name;

}
