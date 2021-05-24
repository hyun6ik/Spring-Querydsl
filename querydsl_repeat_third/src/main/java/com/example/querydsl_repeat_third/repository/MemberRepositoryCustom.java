package com.example.querydsl_repeat_third.repository;

import com.example.querydsl_repeat_third.dto.MemberCond;
import com.example.querydsl_repeat_third.dto.MemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepositoryCustom {

    List<MemberTeamDto> search(MemberCond condition);
    Page<MemberTeamDto> searchPageSimple(MemberCond condition, Pageable pageable);
    Page<MemberTeamDto> searchPageComplex(MemberCond condition, Pageable pageable);




}
