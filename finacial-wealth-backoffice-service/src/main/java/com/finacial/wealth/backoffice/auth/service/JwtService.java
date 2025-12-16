package com.finacial.wealth.backoffice.auth.service;

import com.finacial.wealth.backoffice.auth.entity.BoAdminUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final SecretKey key;
    private final String issuer;
    private final int accessTtlMinutes;
    private final int mfaTtlMinutes;

    public JwtService(
            @Value("${bo.jwt.hmac-secret-base64}") String secretBase64,
            @Value("${bo.jwt.issuer}") String issuer,
            @Value("${bo.jwt.access-ttl-minutes}") int accessTtlMinutes,
            @Value("${bo.jwt.mfa-ttl-minutes:5}") int mfaTtlMinutes
    ) {
        this.key = Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(secretBase64));
        this.issuer = issuer;
        this.accessTtlMinutes = accessTtlMinutes;
        this.mfaTtlMinutes = mfaTtlMinutes;
    }

    public String issueAccessToken(BoAdminUser user) {
        Instant now = Instant.now();
        Instant exp = now.plus(accessTtlMinutes, ChronoUnit.MINUTES);
        List<String> roles = user.getRoles().stream().map(r -> r.getName()).toList();

        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(user.getId()))
                .claim("typ", "ACCESS")
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    /**
     * Short-lived token used only for MFA step-2 verification
     */
    public String issueMfaToken(BoAdminUser user) {
        Instant now = Instant.now();
        Instant exp = now.plus(mfaTtlMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(user.getId()))
                .claim("typ", "MFA")
                .claim("email", user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public Claims parse(String jwt) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    public Claims parseMfaTokenOrThrow(String jwt) {
        Claims c = parse(jwt);

        if (!issuer.equals(c.getIssuer())) {
            throw new IllegalArgumentException("Invalid issuer");
        }

        String typ = c.get("typ", String.class);
        if (!"MFA".equals(typ)) {
            throw new IllegalArgumentException("Invalid token type");
        }

        if (c.getExpiration() == null || c.getExpiration().before(new Date())) {
            throw new IllegalArgumentException("MFA token expired");
        }

        return c;
    }
}
