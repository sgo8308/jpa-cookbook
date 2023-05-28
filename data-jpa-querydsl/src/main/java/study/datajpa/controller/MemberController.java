package study.datajpa.controller;


import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import study.datajpa.dto.datajpa.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.repository.MemberRepository;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    /**
     * #datajpa 페이징을 컨트롤러에서 쉽게 적용 가능하다.
     *
     * - 요청시
     * GET http://localhost:8080/member/10?page=0&size=3&sort=id,desc&sort=name,desc 과 같은 식으로 요청할 때
     * 알아서 PageRequest 객체를 만들어서 Pageable 파라미터에 집어 넣어준다.
     *
     * - 반환시
     * 페이지로 감싸진 객체를 반환할 경우, 그 안에 페이지된 내용과 메타 데이터들을 리턴한다.
     *
     * @see MemberRepository#findByAge(int, Pageable) 페이징에 대한 자세한 내용이 궁금할 경우 참고
     */
    @GetMapping("member/{age}")
    public Page<MemberDto> getMemberByAge(@PathVariable("age") int age, Pageable pageable) {
        //아래 초기화 코드 주석 풀고 실험 진행할 것
        Page<Member> members = memberRepository.findByAge(age, pageable);
        Page<MemberDto> memberDtos = members.map(member -> new MemberDto(member));

        return memberDtos;
    }

    //10명 멤버 넣기 초기화
//    @PostConstruct
//    public void saveMembers() {
//        for (int i = 0; i < 10; i++) {
//            Member mem = new Member("jiwoo" , 10);
//            memberRepository.save(mem);
//        }
//    }
}
