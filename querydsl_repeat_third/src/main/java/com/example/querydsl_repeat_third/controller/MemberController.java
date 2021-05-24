package com.example.querydsl_repeat_third.controller;

import com.example.querydsl_repeat_third.dto.MemberCond;
import com.example.querydsl_repeat_third.dto.MemberTeamDto;
import com.example.querydsl_repeat_third.repository.MemberJpaRepository;
import com.example.querydsl_repeat_third.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepository memberJpaRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMemberV1(MemberCond condition){
        return memberJpaRepository.search(condition);
    }

    @GetMapping("/v2/members")
    public Page<MemberTeamDto> searchMemberV2(MemberCond condition, Pageable pageable){
        return memberRepository.searchPageSimple(condition, pageable);
    }

    @GetMapping("/v3/members")
    public Page<MemberTeamDto> searchMemberV3(MemberCond condition, Pageable pageable){
        return memberRepository.searchPageComplex(condition, pageable);
    }


}
