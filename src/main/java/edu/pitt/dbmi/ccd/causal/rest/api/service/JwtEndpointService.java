/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.dbmi.ccd.causal.rest.api.service;

import com.auth0.jwt.JWTSigner;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.JwtDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.prop.CausalRestProperties;
import java.util.Base64;
import java.util.HashMap;
import java.util.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Zhou Yuan (zhy19@pitt.edu)
 */
@Service
public class JwtEndpointService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtEndpointService.class);

    public static final String AUTH_SCHEME_BASIC = "Basic";

    private final CausalRestProperties causalRestProperties;

    @Autowired
    public JwtEndpointService(CausalRestProperties causalRestProperties) {
        this.causalRestProperties = causalRestProperties;
    }

    public JwtDTO generateJwt(String authString) {
        // Parse the username from the authString
        String authCredentialBase64 = authString.replaceFirst(AUTH_SCHEME_BASIC, "").trim();
        String credentials = new String(Base64.getDecoder().decode(authCredentialBase64));
        StringTokenizer tokenizer = new StringTokenizer(credentials, ":");
        String username = tokenizer.nextToken();

        // Generate JWT (JSON Web Token, for API authentication)
        // Each jwt is issued at claim (per page refresh)
        final long iat = System.currentTimeMillis() / 1000l;
        // Expires claim. In this case the token expires in 3600 seconds (1 hour)
        final long exp = iat + 3600L;

        // Sign the token with secret
        final JWTSigner signer = new JWTSigner(causalRestProperties.getJwtSecret());

        // JWT claims
        final HashMap<String, Object> claims = new HashMap<>();
        // Add reserved claims
        claims.put("iss", causalRestProperties.getJwtIssuer());
        claims.put("iat", iat);
        claims.put("exp", exp);
        // Private/custom claim
        claims.put("name", username);

        // Generate the token string
        String jwt = signer.sign(claims);

        JwtDTO jwtDTO = new JwtDTO();
        jwtDTO.setJwt(jwt);
        jwtDTO.setIat(iat);
        jwtDTO.setExp(exp);

        return jwtDTO;
    }

}
