package com.abhishek.candidateService.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {
private final SecretKey key;
private final long expirationMs = 3600000; // can be externalized


public JwtUtil(@Value("${jwt.secret}") String secret) {
this.key = resolveKey(secret);
}


private SecretKey resolveKey(String secret) {
try { return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)); }
catch (Exception e) { return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); }
}


public Claims parseClaims(String token) {
return (Claims) Jwts.parser().verifyWith(key).build().parse(token).getPayload();
}
}