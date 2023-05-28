package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
/**
 * #jpa-springboot @Transactional, readonly 옵션 설명
 *
 * JPA에서는 데이터 변경시에 꼭 트랜잭션 안에서 진행되어야 한다.(데이터 조회는 트랜잭션 없이도 가능하다.)
 * @Transactional은 javax와 spring이 제공하는 것 2개가 있으며, spring이 제공하는 것이 기능이 더 많기 때문에 사용하자
 * @Transactional을 클레스 레벨에 붙히면 모든 메소드에 각각 적용한 것과 같이 된다.
 *
 * - readOnly
 * readOnly가 true면 영속성 컨텍스트를 플러시 하지 않고, 더티체킹도 하지 않는다. 저장,수정 메소드에 이 옵션 붙히면 제대로 작동 안된다.
 * 따라서 읽기 전용 메소드에 이 옵션을 넣어서 성능 향상을 주자
 * 또 DB에 따라서 읽기 전용 트랜잭션은 최적화해서 진행하는 DB 드라이버를 갖고 있는 DB가 있다.
 *
 * - 테스트에서 쓸 경우
 * 테스트가 끝나면 모두 롤백 시켜버린다. 이 때 영속성 컨텍스트를 플러쉬조차 안하기 떄문에 쿼리가 나가는 것을 볼 수 없다.
 * 만약 보고 싶다면 테스트 클래스 위에 @RollBack(false)를 해놓자.
 */
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * 회원 가입
     */
    @Transactional // 이 join은 읽기 전용 메소드가 아니므로 직접 @Transactional을 걸어서 readOnly=false 적용
    public Long join(Member member) {

        validateDuplicateMember(member); //중복 회원 검증
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    //회원 전체 조회
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }

    /**
     * 회원 수정
     */
    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepository.findOne(id);
        member.setName(name);
    }

}
