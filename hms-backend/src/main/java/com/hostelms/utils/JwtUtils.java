/**
 * JwtUtils 类是用于处理 JSON Web Token (JWT) 相关操作的工具类。
 *
 * 这个类包括了创建、解析和验证 JWT，以及处理 JWT 黑名单的功能。
 */
package com.hostelms.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtils {

    @Value("${spring.security.jwt.key}")
    String key;

    @Value("${spring.security.jwt.expire}")
    int expire;

    @Resource
    StringRedisTemplate template;

    /**
     * 检查 JWT 是否失效，如果 JWT 已经失效或被加入黑名单，则返回 true，否则返回 false。
     *
     * @param headerToken 包含 JWT 的请求头
     * @return boolean 如果 JWT 已经失效或被加入黑名单则返回 true，否则返回 false
     */
    public boolean invaliddateJwt(String headerToken) {
        String token = this.convertToken(headerToken);
        if (token == null) {
            return false;
        }
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            DecodedJWT jwt = jwtVerifier.verify(token);
            String id = jwt.getId();
            return deleteToken(id, jwt.getExpiresAt());
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * 删除 JWT 加入黑名单，以避免重复使用。
     *
     * @param uuid JWT 的唯一标识符
     * @param time JWT 的过期时间
     * @return boolean 如果成功删除则返回 true，否则返回 false
     */
    private boolean deleteToken(String uuid, Date time) {
        if (this.isInvalidToken(uuid)) {
            return false;
        }
        Date now = new Date();
        long expire = Math.max(time.getTime() - now.getTime(), 0);
        template.opsForValue().set(Const.JWT_BLACK_LIST + uuid, "", expire, TimeUnit.MILLISECONDS);
        return true;
    }

    /**
     * 检查 JWT 是否在黑名单中，如果在黑名单中则返回 true，否则返回 false。
     *
     * @param uuid JWT 的唯一标识符
     * @return boolean 如果 JWT 在黑名单中则返回 true，否则返回 false
     */
    private boolean isInvalidToken(String uuid) {
        return Boolean.TRUE.equals(template.hasKey(Const.JWT_BLACK_LIST + uuid));
    }

    /**
     * 解析 JWT 并验证其有效性，如果 JWT 有效则返回解码后的 DecodedJWT 对象，否则返回 null。
     *
     * @param headerToken 包含 JWT 的请求头
     * @return DecodedJWT 解码后的 JWT 对象，如果 JWT 无效则返回 null
     */
    public DecodedJWT resolveJwt(String headerToken) {
        String token = this.convertToken(headerToken);
        if (token == null) {
            return null;
        }
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            DecodedJWT verify = jwtVerifier.verify(token);
            if (this.isInvalidToken(verify.getId())) {
                return null;
            }
            Date expiresAt = verify.getExpiresAt();
            return new Date().after(expiresAt) ? null : verify;
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    /**
     * 创建新的 JWT 并返回其字符串表示。
     *
     * @param userDetails 包含用户信息的 UserDetails 对象
     * @param id 用户的唯一标识符
     * @param truename 用户名
     * @return String 包含 JWT 的字符串
     */
    public String createJwt(UserDetails userDetails,
                            String id,
                            String truename) {
        Algorithm algorithm = Algorithm.HMAC256(key);
        Date expire = this.expireTime();
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("id", id)
                .withClaim("truename", truename)
                .withClaim("authorities", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .withExpiresAt(expire)
                .withIssuedAt(new Date())
                .sign(algorithm);
    }

    /**
     * 计算 JWT 的过期时间。
     *
     * @return Date 表示 JWT 过期时间的 Date 对象
     */
    public Date expireTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, expire);
        return calendar.getTime();
    }

    /**
     * 从 DecodedJWT 对象中提取用户信息并返回 UserDetails 对象。
     *
     * @param jwt 包含用户信息的 DecodedJWT 对象
     * @return UserDetails 用户信息的 UserDetails 对象
     */
    public UserDetails toUser(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims();
        return User
                .withUsername(claims.get("id").asString())
                .password("******")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
    }

    /**
     * 从 DecodedJWT 对象中提取用户的唯一标识符并返回。
     *
     * @param jwt 包含用户信息的 DecodedJWT 对象
     * @return Integer 用户的唯一标识符
     */
    public Integer toId(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims();
        return claims.get("id").asInt();
    }

    /**
     * 从请求头中提取 JWT，并去掉 "Bearer " 前缀，然后返回 JWT 字符串。
     *
     * @param headerToken 包含 JWT 的请求头
     * @return String JWT 字符串，如果请求头不合法则返回 null
     */
    private String convertToken(String headerToken) {
        if (headerToken == null || !headerToken.startsWith("Bearer ")) {
            return null;
        }
        return headerToken.substring(7);
    }
}
