package study.datajpa.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import org.springframework.data.domain.Persistable;

/**
 * Persistable - PK값을 Generated하기 못하는 상황에서 em.save()의 merge 막기
 *
 * em.save()안에서는 isNew()라는 메소드를 통해 엔티티가 신규 등록하는 엔티티인지 아닌지를 판단하여 em.persist()할지 em.merge()를 할지 결정한다.
 * 기본 전략은 엔티티의 PK값이 객체일 경우 NULL 기본 타입일 경우 0인지 보고 신규 엔티티인지 판단한다.
 * 때문에 PK값을 Generated하지 못해서 PK 값이 미리 집어넣어져 있다면 신규 엔티티는 항상 em.merge()로 등록될 것이다.
 *
 * em.merge()의 경우 먼저 값이 있는지 없는지 Select를 해오고 있다면 현재 전달된 엔티티 값으로 모두 교체(완전히 새 데이터로 갈기),
 * 없다면 집어넣는 insertOrUpdate의 방식으로 동작하기 때문에 피하는 것이 좋다.(실수로 Null 데이터 삽입 가능성, 추가 Select 쿼리 문제)
 *
 * 따라서 신규로 넣는 엔티티인지 아닌지 직접 명시해주어야 한다.
 * 그러기 위해서는 Persistable을 구현하고 isNew 메소드를 오버라이드해서 어느 상황이 신규 생성하는 엔티티 명시해줌으로써 해결가능하다.
 *
 * 일반적으로 createdAt 과 같이 생성 일자는 어느 엔티티에나 포함되기 때문에 createdAt이 Null인지 아닌지 여부로 판단해주면 좋다.
 *
 * @see org.springframework.data.jpa.repository.support.SimpleJpaRepository#save(Object)
 */

@Entity
public class Item extends JPABaseEntity implements Persistable<String> {

    @Id
    private String id;
    private String name;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return createdAt == null;
    }
}
