package study.datajpa;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;
import study.datajpa.entity.QMember;

@SpringBootTest
@Transactional
class DataJpaApplicationTests {

    @PersistenceContext EntityManager em;

    @Test
    void contextLoads() {
        Member hello = new Member("jiwoo", 120);
        em.persist(hello);

        JPAQueryFactory query = new JPAQueryFactory(em);

        QMember qMember = QMember.member; //Querydsl Q타입 동작 확인

        List<Member> members = query
                .selectFrom(qMember)
                .fetch();

        members.stream().forEach(m -> System.out.println(m));
    }

}
