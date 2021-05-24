package com.example.querydsl_repeat_third.dto;

import lombok.Data;

@Data
public class MemberCond {

    //회원명, 팀명, 나이(ageGoe, ageLoe)

    private String username;
    private String teamName;
    private Integer ageGoe;
    private Integer ageLoe;
}
