package com.hostelms.utils;

public class Const {
    public static final String JWT_BLACK_LIST = "jwt:blacklist";
    public static final String VERIFY_EMAIL_LIMIT = "verify:email:limit";   // 邮件限制键
    public static final int ASK_EMAIL_TIME = 60; // 邮件申请时间限制 秒
    public static final int CODE_VALID_TIME = 300; // 邮件验证码有效时间 秒
    public static final String VERIFY_EMAIL_DATA = "verify:email:data"; // 邮件数据
    public static final int ORDER_CORS = -102;  // 跨域
    public static final int ORDER_LIMIT = -101;  // 限流
    public static final String FLOW_LIMIT_COUNTER = "FLOW:COUNTER:";    //计数器
    public static final String FLOW_LIMIT_BLOCK = "FLOW:block:";    // 封禁标志
}
