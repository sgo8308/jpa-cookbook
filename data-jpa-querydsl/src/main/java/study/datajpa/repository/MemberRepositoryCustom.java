package study.datajpa.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.datajpa.entity.Member;

/**
 * #datajpa 사용자 정의 인터페이스 - JPA가 기본 제공하는 것 외에 직접 복잡한 쿼리 구현 필요할 때 ex jdbc 직접 사용, mybatis,jdbctemplate, QueryDSL 등
 *
 * 다음과 같이 커스텀 인터페이스를 만든 후 MemberRepository에 인터페이스 상속을 시킨다.
 * 그 후 MemberRepositoryCustomImpl이라는 이름으로(Impl 꼭 붙여야 함) 구현체를 만든다.
 * 이렇게 할 경우 MemberRepository를 주입 받아서 사용할 때 findUserByNameWithMybatis()를 호출하면 MemberRepositoryCustomImpl에게 기능을 위임하여 동작한다.
 *
 * - 주의 사항
 * 웬만하면 Custom을 만들어서 하나의 MemberRepository에 때려 박기 보다는 아예 Repository를 2개로 분리하자.
 * 왜냐하면 이렇게 Custom으로 만들어야 될 정도의 쿼리는 매우 복잡한 조회용 쿼리거나 화면에 특화된 쿼리일 가능성이 크고,
 * 그러한 쿼리는 나머지 MemberRepository만으로 처리되는 핵심 로직들과는 변경의 라이프사이클이 다르고 함께 있을 때 복잡해서 유지보수하기도 힘들다.
 * 따라서 그냥 MemberQueryRepositry와 같이 분리해서 새로 만들고 주입 받는 쪽에서 2개를 주입 받아서 사용하는 것이 낫다.
 */
public interface MemberRepositoryCustom {

    List<Member> findUserByNameWithMybatis(String name);

    Page<Member> searchPageOrder(Pageable pageable);

}
