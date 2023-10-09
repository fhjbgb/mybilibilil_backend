package com.mybilibili.service.util;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.mybilibili.domain.exception.ConditionException;

import java.util.Calendar;
import java.util.Date;

public class TokenUtil {

    private static final String ISSUER = "签发者";
    //创建token
    public static String generateToken(Long userId) throws Exception {
        //加密算法，使用RSA，将rsa的公私钥传入，
        Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
        //过期时间设置
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        //设置过期时间，1小时后过期，以后改
        calendar.add(Calendar.HOUR, 1);
        //jwt创建，传入keyid，传入签发者，传入过期时间，生成签名（要传入使用了什么加密算法）
        return JWT.create().withKeyId(String.valueOf(userId))
                .withIssuer(ISSUER)
                .withExpiresAt(calendar.getTime())
                .sign(algorithm);

    }
    //验证token
    public static Long verifyToken(String token) {
        try {
            //加密算法
            Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
            //jwt验证类
            JWTVerifier verifier = JWT.require(algorithm).build();
            //解密
            DecodedJWT jwt = verifier.verify(token);
            String userId = jwt.getKeyId();
            return Long.valueOf(userId);
        } catch (TokenExpiredException e) {
            throw new ConditionException("555", "token过期");
        } catch (Exception e) {
            throw new ConditionException("非法用户token!");
        }



    }

    public static String generateRefreshToken(Long userId) throws Exception {
        Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        return JWT.create().withKeyId(String.valueOf(userId))
                .withIssuer(ISSUER)
                .withExpiresAt(calendar.getTime())
                .sign(algorithm);

    }

}
