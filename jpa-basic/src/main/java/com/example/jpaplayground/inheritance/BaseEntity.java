package com.example.jpaplayground.inheritance;

import java.time.LocalDate;
import javax.persistence.MappedSuperclass;

/**
 * #jpabasic @MappedSuperclass - 상속 관계는 아니지만 공통된 속성들을 모든 클래스에 적어주기 귀찮을 때 사용하는 방식
 *
 * 이 어노테이션이 붙은 클래스에 공통 속성을 정의해두고 다른 클래스에서는 상속을 하면 된다.
 * 이렇게 될 경우 일반적인 상속이 불가능해지는 거 아닌가?라고 생각할 수 있지만
 * Book이 BaseEntity를 바로 상속 받는게 아니라 Item에 BaseEntity를 상속시키고 Book은 Item을 상속시키도록 하면 된다.
 */
@MappedSuperclass
public class BaseEntity {

    private String createdBy;
    private LocalDate createdAt;

}
