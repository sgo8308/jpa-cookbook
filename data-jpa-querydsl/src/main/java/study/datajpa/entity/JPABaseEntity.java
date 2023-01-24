package study.datajpa.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import lombok.Getter;

/**
 * #datajpa Auditing - 등록,수정 시간 및 등록자, 수정자 간편하게 자동 등록하기
 *
 * 아래 방식은 순수 JPA 방식이며 Spring Data JPA 방식은 좀 더 간편한 방식을 제공한다.
 * 등록자와 수정자를 등록하는 간편한 방식도 제공한다. 그러나 순수한 JPA보다 월등히 편한지 모르겠고
 * 알 수 없는 어노테이션을 추가 적용해야 하기 때문에 이정도로도 충분하다 생각된다.
 * 사용하려면 스프링 데이터 JPA 강의자료 50p를 참고하자.
 * 
 * 참고 - JPA 주요 이벤트 어노테이션
 * @PrePersist, @PreUpdate, @PostPersist, @PostUpdate
 */
@MappedSuperclass
@Getter
public class JPABaseEntity {

    @Column(updatable = false)
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;


    @PrePersist //Persist하기 전에 자동 실행하게 해주는 어노테이션
    public void saveTime(){
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate //update하기 전에 자동 실행하게 해주는 어노테이션
    public void UpdateTime(){
        updatedAt = LocalDateTime.now();
    }
}
