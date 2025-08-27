package com.example.authorizer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2CustomAuthorizerEvent;
import com.auth0.jwk.*;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.*;
import com.auth0.jwt.algorithms.Algorithm;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

// payload format 2.0 / simple response
public class LambdaAuthorizerHandler implements RequestHandler<APIGatewayV2CustomAuthorizerEvent, Map<String, Object>> {

    private static final String REGION   = getenv("COGNITO_REGION", "");
    private static final String POOL_ID  = getenv("COGNITO_USER_POOL_ID", "");
    private static final String ISSUER   = getenv("EXPECTED_ISSUER",
            "https://cognito-idp." + REGION + ".amazonaws.com/" + POOL_ID);
    private static final String AUD      = getenv("EXPECTED_AUDIENCE", ""); // optional
    private static final Set<String> ACCEPTED_TOKEN_USE = setEnv("ACCEPTED_TOKEN_USE", Set.of("id","access"));

    private static final String JWKS_URL = ISSUER + "/.well-known/jwks.json";

    private volatile JwkProvider jwkProvider; // lazy

    @Override
    public Map<String, Object> handleRequest(APIGatewayV2CustomAuthorizerEvent event, Context ctx) {
        try {
            // 1) Authorization 헤더 추출
            String token = extractBearer(event.getHeaders());
            if (token == null) return deny("missing token");

            // 2) 헤더에서 kid 읽고 JWKS로 공개키 조회
            DecodedJWT decodedHeader = JWT.decode(token);
            String kid = decodedHeader.getKeyId();
            if (kid == null || kid.isBlank()) return deny("kid missing");

            Jwk jwk = getJwkProvider().get(kid);
            RSAPublicKey publicKey = (RSAPublicKey) jwk.getPublicKey();
            Algorithm alg = Algorithm.RSA256(publicKey, null);

            // 3) 표준 검증: iss / exp / (aud or client_id) / token_use
            JWTVerifier.BaseVerification base = (JWTVerifier.BaseVerification) JWT.require(alg).withIssuer(ISSUER);
            if (!AUD.isBlank()) base.withAudience(AUD);
            JWTVerifier verifier = base.build();
            DecodedJWT jwt = verifier.verify(token);

            // token_use 체크(id | access)
            String tokenUse = opt(jwt, "token_use");
            if (!ACCEPTED_TOKEN_USE.isEmpty() && !ACCEPTED_TOKEN_USE.contains(tokenUse)) {
                return deny("token_use not accepted: " + tokenUse);
            }

            // 4) 클레임 추출 (Cognito 기준)
            String sub = jwt.getSubject();
            String username = opt(jwt, "cognito:username");
            String email = opt(jwt, "email");
            List<String> groups = claimAsList(jwt, "cognito:groups");

            // 5) 헤더 스펙에 맞춘 context 구성
            String principalGroup = derivePrimaryGroup(groups); // OWNER | CUSTOMER | ADMIN
            String rolesCsv = String.join(",", groups != null ? groups : List.of());

            Map<String, Object> context = new HashMap<>();
            context.put("groups", nullSafe(principalGroup));
            context.put("userId", nullSafe(sub));
            context.put("userName", nullSafe(username));
            context.put("email", nullSafe(email));
            context.put("userRoles", nullSafe(rolesCsv));

            // 6) simple response (HTTP API payload 2.0)
            Map<String, Object> resp = new HashMap<>();
            resp.put("isAuthorized", true);
            resp.put("context", context);
            return resp;

        } catch (Exception e) {
            return deny("verify error: " + e.getMessage());
        }
    }

    // ========== helpers ==========

    private static String extractBearer(Map<String, String> headers) {
        if (headers == null) return null;
        String h = headers.getOrDefault("authorization",
                headers.getOrDefault("Authorization", null));
        if (h == null) return null;
        if (h.startsWith("Bearer ")) return h.substring(7).trim();
        return null;
    }

    private JwkProvider getJwkProvider() throws Exception {
        if (jwkProvider == null) {
            synchronized (this) {
                if (jwkProvider == null) {
                    jwkProvider = new UrlJwkProvider(new URL(JWKS_URL));
                }
            }
        }
        return jwkProvider;
    }

    private static String opt(DecodedJWT jwt, String claim) {
        Claim c = jwt.getClaim(claim);
        return (c == null || c.isNull()) ? "" : String.valueOf(c.as(Object.class));
    }

    private static List<String> claimAsList(DecodedJWT jwt, String claim) {
        Claim c = jwt.getClaim(claim);
        if (c == null || c.isNull()) return List.of();
        List<String> list = c.asList(String.class);
        if (list != null) return list;
        String s = c.asString();
        if (s != null && !s.isBlank()) {
            return Arrays.stream(s.split("[,\\s]+"))
                    .filter(t -> !t.isBlank()).toList();
        }
        return List.of();
    }

    private static String derivePrimaryGroup(List<String> groups) {
        if (groups == null || groups.isEmpty()) return "CUSTOMER";
        List<String> upper = groups.stream().filter(Objects::nonNull)
                .map(s -> s.toUpperCase(Locale.ROOT)).toList();
        if (upper.contains("ADMIN")) return "ADMIN";
        if (upper.contains("OWNER")) return "OWNER";
        return upper.get(0);
    }

    private static String nullSafe(String v) { return v == null ? "" : v; }

    private static Map<String, Object> deny(String reason) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("isAuthorized", false); // HTTP API simple response
        // 선택: 디버그를 위해 context에 reason 남길 수도 있음(운영에선 제거 가능)
        Map<String,Object> ctx = new HashMap<>();
        ctx.put("error", reason);
        resp.put("context", ctx);
        return resp;
    }

    private static String getenv(String k, String def) {
        String v = System.getenv(k);
        return (v == null || v.isBlank()) ? def : v;
    }

    private static Set<String> setEnv(String key, Set<String> def) {
        String v = getenv(key, "");
        if (v.isBlank()) return def;
        Set<String> out = new HashSet<>();
        for (String s : v.split("[,\\s]+")) if (!s.isBlank()) out.add(s.trim());
        return out;
    }
}