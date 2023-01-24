package study.datajpa.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.datajpa.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.repository.MemberRepository.NameOnly;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    /**
     * #datajpa 같은 트랜잭션 안에서는 JpaRepository가 사용하는 EntitiytManager와 주입 받은 EntityManager는 같은 영속성 컨텍스트를 바라본다.
     */
    @PersistenceContext
    EntityManager em;

    @Test
    void findByNameAndAgeGreaterThan() {
        Member member1 = new Member("jiwoo", 15);
        Member member2 = new Member("jiwoo", 13);
        Member member3 = new Member("jiwoo", 9);

        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);

        List<Member> members = memberRepository.findByNameAndAgeGreaterThan("jiwoo", 10);
        assertThat(members).hasSize(2);
    }

    @Test
    void findUser() {
        Member member1 = new Member("jiwoo", 15);
        Member member2 = new Member("jiwoo", 13);
        Member member3 = new Member("jiwoo", 9);

        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);

        List<Member> members = memberRepository.findUser("jiwoo", 15);

        assertThat(member1).isEqualTo(members.get(0));
    }

    @Test
    void findByAge() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        //when
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC,
                "name")); // 페이지는 0번부터 시작하는 것 주의, 이름 기준으로 DESC 정렬

        Page<Member> page = memberRepository.findByAge(10, pageRequest);

        //then
        List<Member> content = page.getContent(); //조회된 데이터
        assertThat(content.size()).isEqualTo(3); //조회된 데이터 수
        assertThat(page.getTotalElements()).isEqualTo(5); //전체 데이터 수
        assertThat(page.getNumber()).isEqualTo(0); //페이지 번호
        assertThat(page.getTotalPages()).isEqualTo(2); //전체 페이지 번호
        assertThat(page.isFirst()).isTrue(); //첫번째 항목인가?
        assertThat(page.hasNext()).isTrue(); //다음 페이지가 있는가?

        /**
         * #datajpa 페이지를 유지하면서 엔티티를 DTO로 변환하기
         */
        Page<MemberDto> memberDtoPages = page.map(member -> new MemberDto(member));
    }

    @Test
    void findNameById() {
        //given
        Member member1 = new Member("jeuse", 15);
        Member member2 = new Member("jiwoo", 13);
        Member member3 = new Member("jiwoo", 9);

        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);

        //when
        List<NameOnly> nameById = memberRepository.findNameById(member1.getId());

        //then
        assertThat(nameById.get(0).getName()).isEqualTo("jeuse");
    }
}