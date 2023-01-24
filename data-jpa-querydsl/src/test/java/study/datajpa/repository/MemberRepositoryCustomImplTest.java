package study.datajpa.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.datajpa.entity.Member;

@SpringBootTest
class MemberRepositoryCustomImplTest {

    @Autowired MemberRepository memberRepository;

    @Test
    void findUserByNameWithMybatis() {
        Member member1 = new Member("jiwoo", 15);
        Member member2 = new Member("jiwoo", 13);
        Member member3 = new Member("jiwoo", 9);
        Member member4 = new Member("brave", 9);

        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);

        List<Member> members = memberRepository.findUserByNameWithMybatis("jiwoo");

        Assertions.assertThat(members).hasSize(3);
    }
}