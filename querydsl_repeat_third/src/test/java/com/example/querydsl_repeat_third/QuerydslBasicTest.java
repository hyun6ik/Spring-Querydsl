package com.example.querydsl_repeat_third;

import com.example.querydsl_repeat_third.dto.MemberDto;
import com.example.querydsl_repeat_third.dto.QMemberDto;
import com.example.querydsl_repeat_third.dto.UserDto;
import com.example.querydsl_repeat_third.entity.Member;
import com.example.querydsl_repeat_third.entity.QMember;
import com.example.querydsl_repeat_third.entity.QTeam;
import com.example.querydsl_repeat_third.entity.Team;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import java.util.List;

import static com.example.querydsl_repeat_third.entity.QMember.*;
import static com.example.querydsl_repeat_third.entity.QTeam.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);
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
    }

    @Test
    public void startJPQL() throws Exception {
        //given
        Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();
        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");

    }

    @Test
    public void startQuerydsl() throws Exception {
        //given

        QMember m = new QMember(member);
        //when
        Member findMember = queryFactory
                .selectFrom(m)
                .where(m.username.eq("member1"))
                .fetchOne();
        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search() throws Exception {
        //given
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"),
                        (member.age.eq(10)))
                .fetchOne();

        List<Member> findMembers = queryFactory
                .selectFrom(member)
                .where(member.age.between(10, 30))
                .fetch();
        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");
        assertThat(findMembers)
                .extracting("username")
                .containsExactly("member1", "member2", "member3");
    }


    @Test
    public void resultFetchTest() throws Exception {
        //given
//        List<Member> fetch = queryFactory
//                .selectFrom(member)
//                .fetch();
//        Member fetchOne = queryFactory
//                .selectFrom(member)
//                .fetchOne();
//        Member fetchFirst = queryFactory
//                .selectFrom(member)
//                .fetchFirst();
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();

        results.getTotal();
        List<Member> content = results.getResults();

        long total = queryFactory
                .selectFrom(member)
                .fetchCount();
        //when

        //then

    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순
     * 2. 회원 이름 올림차순
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort() throws Exception {
        //given
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member member7 = result.get(2);
        //then
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(member7.getUsername()).isNull();

    }

    @Test
    public void paging1() throws Exception {
        //given
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();
        //then
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void paging2() throws Exception {
        //given
        QueryResults<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();
        //then
        assertThat(result.getTotal()).isEqualTo(4);
        assertThat(result.getLimit()).isEqualTo(2);
        assertThat(result.getOffset()).isEqualTo(1);
        assertThat(result.getResults().size()).isEqualTo(2);
    }


    @Test
    public void agreegation() throws Exception {
        //given
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();
        //when
        Tuple tuple = result.get(0);

        //then
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);

    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    public void group() throws Exception {
        //given
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();
        //when
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);
        //then
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);

    }

    /**
     * 팀 A에 소속된 모든 회원
     */

    @Test
    public void join() throws Exception {
        //given
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();
        //then
        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");

    }


    /**
     * 세타조인
     * 회원 이름이 팀 이름과 같은 회원 조회
     * 모든 Member, Team 테이블 조인 후 where절로 찾는 방식
     */
    @Test
    public void thetaJoin() throws Exception {
        //given
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();
        //then
        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    /**
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     */
    @Test
    public void join_on_filtering() throws Exception {
        //given
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();
        //then
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }

    }

    /**
     * 연관관계 없는 엔티티 외부 조인
     * 회원의 이름이 팀 이름과 같은 대상 외부 조인
     */
    @Test
    public void join_on_no_relation() throws Exception {
        //given
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team)
                .on(member.username.eq(team.name))
                .fetch();
        //then
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }

    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetchJoinNo() throws Exception {
        em.flush();
        em.clear();

        //given
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        //when
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        //then
        assertThat(loaded).as("페치 조인 미적용").isFalse();

    }

    @Test
    public void fetchJoinUse() throws Exception {
        //given
        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();
        //when
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        //then
        assertThat(loaded).as("페치 조인 적용").isTrue();

    }

    /**
     * 나이가 가장 많은 회원 조회
     */
    @Test
    public void subQuery() throws Exception {
        //given

        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();
        //then
        assertThat(result).extracting("age").containsExactly(40);

    }

    /**
     * 나이가 평균 이상인 회원
     */
    @Test
    public void subQuery2() throws Exception {
        //given
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();
        //then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).extracting("age").containsExactly(30, 40);

    }

    /**
     * subQuery In예제
     */
    @Test
    public void subQueryIn() throws Exception {
        //given
        QMember memberSub = new QMember("memberSub");
        //when
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(member.age)
                                .from(memberSub)
                                .where(member.age.gt(10))
                ))
                .fetch();
        //then
        assertThat(result.size()).isEqualTo(3);
        assertThat(result).extracting("age").containsExactly(20, 30, 40);

    }

    /**
     * select subQuery 예제
     */
    @Test
    public void selectSubQuery() throws Exception {
        //given
        QMember memberSub = new QMember("memberSub");

        List<Tuple> result = queryFactory
                .select(member.username,
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();
        //then
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void basicCase() throws Exception {
        //given
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();
        //when
        for (String s : result) {
            System.out.println("s = " + s);
        }

    }

    @Test
    public void complexCase() throws Exception {
        //given
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타"))
                .from(member)
                .fetch();
        //when
        for (String s : result) {
            System.out.println("s = " + s);
        }

        //then

    }

    @Test
    public void constant() throws Exception {
        //given
        List<Tuple> result = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();
        //when
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }

        //then

     }

     @Test
     public void concat() throws Exception {
         //given
         //username_age
         List<String> result = queryFactory
                 .select(member.username.concat("_").concat(member.age.stringValue()))
                 .from(member)
                 .where(member.username.eq("member1"))
                 .fetch();
         //when
         for (String s : result) {
             System.out.println("s = " + s);
         }

         //then

      }


      @Test
      public void simpleProjection() throws Exception {
          //given
          List<String> result = queryFactory
                  .select(member.username)
                  .from(member)
                  .fetch();
          //when
          for (String s : result) {
              System.out.println("s = " + s);
          }

       }

       @Test
       public void tupleProejection() throws Exception {
           //given
           List<Tuple> result = queryFactory
                   .select(member.username, member.age)
                   .from(member)
                   .fetch();
           //when
           for (Tuple tuple : result) {
               String username = tuple.get(member.username);
               Integer age = tuple.get(member.age);
               System.out.println("username = " + username);
               System.out.println("age = " + age);
           }
        }
        
        
        @Test
        public void findDtoBySetter() throws Exception {
            //given
            List<MemberDto> result = queryFactory
                    .select(Projections.bean(MemberDto.class,
                            member.username,
                            member.age))
                    .from(member)
                    .fetch();
            //when

            for (MemberDto memberDto : result) {
                System.out.println("memberDto = " + memberDto);
            }
         }

         @Test
         public void findDtoByField() throws Exception {
             //given
             List<MemberDto> result = queryFactory
                     .select(Projections.fields(MemberDto.class, member.username, member.age))
                     .from(member)
                     .fetch();
             //when
             for (MemberDto memberDto : result) {
                 System.out.println("memberDto = " + memberDto);    
             }

             //then
          }
          
          @Test
          public void findDtoByConstructor() throws Exception {
              //given
              List<MemberDto> result = queryFactory
                      .select(Projections.constructor(MemberDto.class, member.username, member.age))
                      .from(member)
                      .fetch();
              //when
              for (MemberDto memberDto : result) {
                  System.out.println("memberDto = " + memberDto);
              }

           }
           @Test
           public void findUserDto() throws Exception {
               QMember memberSub = new QMember("memberSub");
               //given
               List<UserDto> result = queryFactory
                       .select(Projections.fields(UserDto.class,
                               member.username.as("name"),
                               ExpressionUtils.as(JPAExpressions
                               .select(memberSub.age.max())
                                       .from(memberSub), "age")
                       ))
                       .from(member)
                       .fetch();
               //when
               for (UserDto userDto : result) {
                   System.out.println("userDto = " + userDto);
               }
               //then

            }


            @Test
            public void findDtoByQueryProjection() throws Exception {
                //given
                List<MemberDto> result = queryFactory
                        .select(new QMemberDto(member.username, member.age))
                        .from(member)
                        .fetch();
                //when
                for (MemberDto memberDto : result) {
                    System.out.println("memberDto = " + memberDto);
                }

                //then

             }

             @Test
             public void dynamicQuery_BooleanBuilder() throws Exception {
                 //given
                 String usernameParam = "member1";
                 Integer ageParam = null;

                 List<Member> result = searchMember1(usernameParam, ageParam);
                 //when

                 //then
                 assertThat(result.size()).isEqualTo(1);
                 for (Member member1 : result) {
                     System.out.println("member1 = " + member1);
                 }
              }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {

        BooleanBuilder builder = new BooleanBuilder();
        if(usernameCond != null){
            builder.and(member.username.eq(usernameCond));
        }

        if(ageCond != null){
            builder.and(member.age.eq(ageCond));
        }

        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    @Test
    public void dynamic_WhereParam() throws Exception {
        //given
        String usernameParam = "member1";
        Integer ageParam = null;
        //when
        List<Member> result = searchMember2(usernameParam, ageParam);
        //then
        assertThat(result.size()).isEqualTo(1);
        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }

     }

    private List<Member> searchMember2(String usernameCond, Integer ageCond) {

        return queryFactory
                .selectFrom(member)
                .where(usernameEq(usernameCond), ageEq(ageCond))
//                .where(allEq(usernameCond, ageCond))
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameCond) {
//        if (usernameCond == null) {
//            return null;
//        } else {
//            return member.username.eq(usernameCond);
//        }
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }


    //광고 상태 isValid, 날짜가 IN : isServicable

    private BooleanExpression allEq(String usernameCond, Integer ageCond){
       return usernameEq(usernameCond).and(ageEq(ageCond));
    }


    @Test
    public void bulkUpdate() throws Exception {
        //given

        //member1 -> 비회원
        //member2 -> 비회원

        long count = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();

        em.flush();
        em.clear();
        //when
        List<Member> result = queryFactory
                .selectFrom(member)
                .fetch();
        //then
        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }

     }

     @Test
     public void bulkAdd() throws Exception {
         //given
         queryFactory
                 .update(member)
                 .set(member.age, member.age.add(1))
                 .execute();
         //when

         //then
      }

      @Test
      public void bulkDelete() throws Exception {
          //given
          queryFactory
                  .delete(member)
                  .where(member.age.gt(18))
                  .execute();

       }

       @Test
       public void sqlFunction() throws Exception {
           //given
           List<String> result = queryFactory
                   .select(Expressions.stringTemplate("function('replace', {0},{1},{2})",
                           member.username, "member", "M"))
                   .from(member)
                   .fetch();
           //when
           for (String s : result) {
               System.out.println("s = " + s);
           }

           //then

        }

        @Test
        public void sqlFunction2() throws Exception {
            //given
            List<String> result = queryFactory
                    .select(member.username)
                    .from(member)
                    .where(member.username.eq(member.username.lower()))
//                            (Expressions.stringTemplate(
//                            "function('lower',{0})", member.username)))

                    .fetch();
            //when
            for (String s : result) {
                System.out.println("s = " + s);
            }
            //then

         }


}


