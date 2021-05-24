package com.example.querydsl_repeat_third.repository;

import com.example.querydsl_repeat_third.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member,Long>, MemberRepositoryCustom {

    List<Member> findByUsername(String username);
}
