package com.example.querydsl_repeat_third.repository;

import com.example.querydsl_repeat_third.dto.MemberCond;
import com.example.querydsl_repeat_third.dto.MemberTeamDto;
import com.example.querydsl_repeat_third.entity.Member;
import com.example.querydsl_repeat_third.entity.Team;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    private EntityManager em;
    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Test
    public void basicTest() throws Exception {
        //given
        Member member = new Member("member1",10);
        memberJpaRepository.save(member);
        //when
        Member findMember = memberJpaRepository.findById(member.getId()).orElse(null);
        List<Member> result = memberJpaRepository.findAll();
        List<Member> result2 = memberJpaRepository.findByUsername(member.getUsername());
        //then
        assertThat(findMember).isEqualTo(member);
        assertThat(result).containsExactly(member);
        assertThat(result2).containsExactly(member);

     }

     @Test
     public void basicQuerydslTest() throws Exception {
         //given
         Member member = new Member("member1",10);
         memberJpaRepository.save(member);
         //when
         Member findMember = memberJpaRepository.findById(member.getId()).orElse(null);
         List<Member> result = memberJpaRepository.findAll_QueryDsl();
         List<Member> result2 = memberJpaRepository.findByUsername_QueryDsl(member.getUsername());
         //then
         assertThat(findMember).isEqualTo(member);
         assertThat(result).containsExactly(member);
         assertThat(result2).containsExactly(member);
      }

      @Test
      public void searchTest() throws Exception {
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
//          condition.setAgeGoe(20);
//          condition.setAgeLoe(50);
//          condition.setTeamName("teamB");
          //when
          List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);
          //then
          assertThat(result).extracting("username").containsExactly("member4");

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
           List<MemberTeamDto> result = memberJpaRepository.search(condition);
           //then
           assertThat(result).extracting("username").containsExactly("member4");
        }

}