/*
 * Copyright (C) 2016 University of Pittsburgh.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
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

        // When we can get here vai AuthFilterSerice, it means the user exists
        // so no need to check if (userAccount == null) and throw UserNotFoundException(uid)
        UserAccount userAccount = userAccountService.findByUsername(username);

        // Note this uid is Long object, we'll need to use the numeric primitive long
        // to store it into JWT claims
        Long uid = userAccount.getId();

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
        // Convert iatDate and expDate into long primitive
        claims.put("iat", iatDate.getTime());
        claims.put("exp", expDate.getTime());
        // Private/custom claim
        claims.put("uid", uid);

        // Generate the token string
        String jwt = signer.sign(claims);

        // We store this JWT into `public_key` field of the user account table
        userAccount.setPublicKey(jwt);
        userAccountService.save(userAccount);

        LOGGER.info("Added JWT for user id %d", uid);

        // Return the jwt to API consumer
        JwtDTO jwtDTO = new JwtDTO();
        jwtDTO.setUserId(uid);
        jwtDTO.setJwt(jwt);
        jwtDTO.setIssuedTime(iatDate);
        jwtDTO.setLifetime(causalRestProperties.getJwtLifetime());
        jwtDTO.setExpireTime(expDate);

        return jwtDTO;
    }

}
