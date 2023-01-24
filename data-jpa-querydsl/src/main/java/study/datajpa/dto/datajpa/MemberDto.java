package study.datajpa.dto.datajpa;

import lombok.Data;
import study.datajpa.entity.Member;

@Data
public class MemberDto {

    Long id;
    String memberMame;
    String teamName;

    public MemberDto(Long id, String memberMame, String teamName) {
        this.id = id;
        this.memberMame = memberMame;
        this.teamName = teamName;
    }

    public MemberDto(Member member) {
        this.id = member.getId();
        this.memberMame = member.getName();
        if(member.getTeam() != null)
            this.teamName = member.getTeam().getName();
    }
}
