package com.hostelms.entity.vo.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AuthorizeVO {
    String username;
    int role;
    String token;
    Date expire;
    String id;
}
