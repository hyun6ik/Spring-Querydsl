package com.example.querydsl_repeat_third.repository;

import com.example.querydsl_repeat_third.dto.MemberCond;
import com.example.querydsl_repeat_third.dto.MemberTeamDto;
import com.example.querydsl_repeat_third.entity.Member;
import com.example.querydsl_repeat_third.entity.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private EntityManager em;

    @Test
    public void basicTest() throws Exception {
        //given
        Member member = new Member("member1",10);
        memberRepository.save(member);
        //when
        Member findMember = memberRepository.findById(member.getId()).orElse(null);
        List<Member> result = memberRepository.findAll();
        List<Member> result2 = memberRepository.findByUsername(member.getUsername());
        //then
        assertThat(findMember).isEqualTo(member);
        assertThat(result).containsExactly(member);
        assertThat(result2).containsExactly(member);

    }
    @Test
    public void search() throws Exception {
        //given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberCond condition = new MemberCond();
        condition.setTeamName("teamB");
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);

        //when
        List<MemberTeamDto> result = memberRepository.search(condition);
        //then
        assertThat(result).extracting("username").containsExactly("member4");
    }


    @Test
    public void searchSimpleTest() throws Exception {
        //given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberCond condition = new MemberCond();
        PageRequest pageRequest = PageRequest.of(0, 3);
        //when
        Page<MemberTeamDto> result = memberRepository.searchPageSimple(condition, pageRequest);
        //then
        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent()).extracting("username").containsExactly("member1", "member2", "member3");

     }
}
