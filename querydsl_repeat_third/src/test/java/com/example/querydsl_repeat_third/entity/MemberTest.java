package com.example.querydsl_repeat_third.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberTest {

    @Autowired
    EntityManager em;

    @Test
    public void testEntity() throws Exception {
        //given
        Team teamA = new Team().builder()
                .name("teamA")
                .build();
        Team teamB = new Team().builder()
                .name("teamB")
                .build();
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member().builder()
                .username("member1")
                .age(10)
                .team(teamA)
                .build();
        Member member2 = new Member().builder()
                .username("member2")
                .age(20)
                .team(teamA)
                .build();
        Member member3 = new Member().builder()
                .username("member3")
                .age(30)
                .team(teamB)
                .build();
        Member member4 = new Member().builder()
                .username("member4")
                .age(40)
                .team(teamB)
                .build();
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        em.flush();
        em.clear();

        //when
        List<Member> members = em.createQuery("select m from Member m", Member.class).getResultList();
        //then
        for (Member member : members) {
            System.out.println("member = " + member);
            System.out.println("member.getTeam() = " + member.getTeam());
        }

    }
}