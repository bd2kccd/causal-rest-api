/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.dbmi.ccd.causal.rest.api.service;

import com.auth0.jwt.JWTSigner;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author Zhou Yuan (zhy19@pitt.edu)
 */
public class JwtEndpointService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataFileEndpointService.class);

    @Value("${ccd.jwt.issuer}")
    private String jwtIssuer;

    @Value("${ccd.jwt.secret}")
    private String jwtSecret;

    public String generateJwt(String username) {
        // Generate JWT (JSON Web Token, for API authentication)
        // Each jwt is issued at claim (per page refresh)
        final long iat = System.currentTimeMillis() / 1000l;
        // Expires claim. In this case the token expires in 3600 seconds (1 hour)
        final long exp = iat + 3600L;

        // Sign the token with secret
        final JWTSigner signer = new JWTSigner(jwtSecret);

        // JWT claims
        final HashMap<String, Object> claims = new HashMap<>();
        // Add reserved claims
        claims.put("iss", jwtIssuer);
        claims.put("iat", iat);
        claims.put("exp", exp);
        // Private/custom claim
        claims.put("name", username);

        // Generate the token string
        String jwt = signer.sign(claims);

        return jwt;
    }

}
