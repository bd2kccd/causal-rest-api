/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.dbmi.ccd.causal.rest.api.service;

import com.auth0.jwt.JWTSigner;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.JwtDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.prop.CausalRestProperties;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
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

    private final UserAccountService userAccountService;

    @Autowired
    public JwtEndpointService(CausalRestProperties causalRestProperties, UserAccountService userAccountService) {
        this.causalRestProperties = causalRestProperties;
        this.userAccountService = userAccountService;
    }

    public JwtDTO generateJwt(String authString) {
        // Parse the username from the authString
        String authCredentialBase64 = authString.replaceFirst(AUTH_SCHEME_BASIC, "").trim();
        String credentials = new String(Base64.getDecoder().decode(authCredentialBase64));
        StringTokenizer tokenizer = new StringTokenizer(credentials, ":");
        String username = tokenizer.nextToken();

        // Generate JWT (JSON Web Token, for API authentication)
        // Each jwt is issued at claim (per API request)
        // When refresh the request, the new jwt will overwrite the old one
        // Using Java 8 time API
        Instant iatInstant = Instant.now();
        // The token expires in 3600 seconds (1 hour)
        Instant expInstant = iatInstant.plusSeconds(causalRestProperties.getJwtLifetime());
        Date iatDate = Date.from(iatInstant);
        Date expDate = Date.from(expInstant);

        // Sign the token with secret
        JWTSigner signer = new JWTSigner(causalRestProperties.getJwtSecret());

        // JWT claims
        HashMap<String, Object> claims = new HashMap<>();
        // Add reserved claims
        claims.put("iss", causalRestProperties.getJwtIssuer());
        // Convert iatDate and expDate into long
        claims.put("iat", iatDate.getTime());
        claims.put("exp", expDate.getTime());
        // Private/custom claim
        claims.put("name", username);

        // Generate the token string
        String jwt = signer.sign(claims);

        // No need to check if the user exists, since the AuthFiter also done that check.
        UserAccount userAccount = userAccountService.findByUsername(username);
        // We store this JWT into `public_key` field of the user account table
        userAccount.setPublicKey(jwt);
        userAccountService.saveUserAccount(userAccount);

        // Return the jwt to API consumer
        JwtDTO jwtDTO = new JwtDTO();
        jwtDTO.setJwt(jwt);
        jwtDTO.setIssuedTime(iatDate);
        jwtDTO.setLifetime(causalRestProperties.getJwtLifetime());
        jwtDTO.setExpireTime(expDate);

        return jwtDTO;
    }

}
