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

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import edu.pitt.dbmi.ccd.causal.rest.api.Role;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.AccessDeniedException;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.AccessForbiddenException;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.UserRole;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SignatureException;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * Jun 5, 2016 10:52:45 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class AuthFilterService {

    @Value("${ccd.jwt.issuer}")
    private String jwtIssuer;

    @Value("${ccd.jwt.secret}")
    private String jwtSecret;

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthFilterService.class);

    private static final String AUTH_HEADER = "Authorization";
    public static final String AUTH_SCHEME_BASIC = "Basic";
    public static final String AUTH_SCHEME_BEARER = "Bearer";

    private static final AccessDeniedException BASIC_AUTH_USER_CREDENTIALS_REQUIRED = new AccessDeniedException("User credentials are required.");
    private static final AccessDeniedException BASIC_AUTH_SCHEME_REQUIRED = new AccessDeniedException("Basic Authentication scheme is required to get the JSON Web Token(JWT).");
    private static final AccessDeniedException BASIC_AUTH_INVALID_USER_CREDENTIALS = new AccessDeniedException("Invalid user credentials.");

    private static final AccessDeniedException BEARER_AUTH_JWT_REQUIRED = new AccessDeniedException("JSON Web Token(JWT) is required.");
    private static final AccessDeniedException BEARER_AUTH_SCHEME_REQUIRED = new AccessDeniedException("Bearer Authentication scheme is required to acees this resource.");
    private static final AccessDeniedException BEARER_AUTH_EXPIRED_JWT = new AccessDeniedException("Your JSON Web Token(JWT) has expired, please get a new one and try again.");
    private static final AccessDeniedException BEARER_AUTH_INVALID_JWT = new AccessDeniedException("Invalid JSON Web Token(JWT).");

    private static final AccessForbiddenException FORBIDDEN_ACCESS = new AccessForbiddenException("You don't have permission to access this resource.");

    private final UserAccountService userAccountService;
    private final DefaultPasswordService defaultPasswordService;

    @Autowired
    public AuthFilterService(UserAccountService userAccountService, DefaultPasswordService defaultPasswordService) {
        this.userAccountService = userAccountService;
        this.defaultPasswordService = defaultPasswordService;
    }

    // Direct the actual authentication to baisc auth
    public void verifyBasicAuth(ContainerRequestContext requestContext) {
        String authCredentials = requestContext.getHeaderString(AUTH_HEADER);
        if (authCredentials == null) {
            throw BASIC_AUTH_USER_CREDENTIALS_REQUIRED;
        }

        if (!authCredentials.contains(AUTH_SCHEME_BASIC)) {
            throw BASIC_AUTH_SCHEME_REQUIRED;
        }

        String authCredentialBase64 = authCredentials.replaceFirst(AUTH_SCHEME_BASIC, "").trim();
        // In the basic auth schema, both username and password are encoded in the request header
        // So we'll need to get the user account info with username and password
        String credentials = new String(Base64.getDecoder().decode(authCredentialBase64));
        UserAccount userAccount = retrieveUserAccount(credentials);

        if (userAccount == null) {
            throw BASIC_AUTH_INVALID_USER_CREDENTIALS;
        }

        // No need to check isUserInRole("admin") since everyone can sign in
        // No need to check isAccountMatchesRequest(userAccount, requestContext) since the jwt URI doesn't contain username
        SecurityContext securityContext = createSecurityContext(userAccount, requestContext, AUTH_SCHEME_BASIC);

        requestContext.setSecurityContext(securityContext);
    }

    // Direct the actual authentication to jwt based bearer schema
    public void verifyJwt(ContainerRequestContext requestContext) {
        String authCredentials = requestContext.getHeaderString(AUTH_HEADER);
        if (authCredentials == null) {
            throw BEARER_AUTH_JWT_REQUIRED;
        }

        // All other endpoints use bearer JWT to verify the API consumer
        if (!authCredentials.contains(AUTH_SCHEME_BEARER)) {
            throw BEARER_AUTH_SCHEME_REQUIRED;
        }

        // Verify JWT
        try {
            String jwt = authCredentials.replaceFirst(AUTH_SCHEME_BEARER, "").trim();

            // Verify both secret and issuer
            final JWTVerifier jwtVerifier = new JWTVerifier(jwtSecret, null, jwtIssuer);
            final Map<String, Object> claims = jwtVerifier.verify(jwt);

            // Verify the expiration date
            Long exp = (Long) claims.get("exp");
            Instant nowInstant = Instant.now();
            Long now = Date.from(nowInstant).getTime();
            if (now.compareTo(exp) > 0) {
                throw BEARER_AUTH_EXPIRED_JWT;
            }

            // We can simply get the user account based on the user id
            // Turned out jwt library returns claims.get("uid") as java.lang.Integer
            //System.out.println(claims.get("uid").getClass().getName());
            Integer uidInteger = (Integer) claims.get("uid");
            Long uid = uidInteger.longValue();

            UserAccount userAccount = userAccountService.findById(uid);
            // Since we check the user existence here, no need to check it again in each endpoint service
            if (userAccount == null) {
                throw BEARER_AUTH_INVALID_JWT;
            }

            // Also make sure the uid found in jwt matches the one in URI
            SecurityContext securityContext = createSecurityContext(userAccount, requestContext, AUTH_SCHEME_BEARER);
            if (!(securityContext.isUserInRole("admin") || isAccountMatchesRequest(uid, requestContext))) {
                throw FORBIDDEN_ACCESS;
            }

            // Then compare the jwt with the one stored in `public_key` field in user account table
            // It's very possible that the jwt sent here has already been overwritten
            String currentJwt = userAccount.getPublicKey();
            if (!currentJwt.equals(jwt)) {
                throw BEARER_AUTH_INVALID_JWT;
            }

            requestContext.setSecurityContext(securityContext);
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException | IOException | SignatureException | JWTVerifyException ex) {
            LOGGER.error("Failed to verify JWT", ex);
        }
    }

    private boolean isAccountMatchesRequest(Long uid, ContainerRequestContext requestContext) {
        MultivaluedMap<String, String> pathParams = requestContext.getUriInfo().getPathParameters();
        long reqUid = Long.parseLong(pathParams.getFirst("uid"));

        return uid.equals(reqUid);
    }

    private SecurityContext createSecurityContext(UserAccount userAccount, ContainerRequestContext requestContext, String authScheme) {
        String username = userAccount.getUsername();

        Set<String> roles = new HashSet<>();
        Set<UserRole> userRoles = userAccount.getUserRoles();
        userRoles.forEach(userRole -> {
            roles.add(userRole.getName());
        });

        // remove this in the future whenever we have roles
        if (roles.isEmpty()) {
            roles.add(Role.USER);
        }

        boolean secure = "https".equals(requestContext.getUriInfo().getRequestUri().getScheme());

        return new CustomSecurityContext(username, roles, authScheme, secure);
    }

    /**
     * Find the user account by email and password provided in Basic Auth header
     *
     * @param credentials
     * @return
     */
    private UserAccount retrieveUserAccount(String credentials) {
        StringTokenizer tokenizer = new StringTokenizer(credentials, ":");
        String email = tokenizer.nextToken();
        String password = tokenizer.nextToken();

        UserAccount userAccount = userAccountService.findByEmail(email);
        if (userAccount != null) {
            String hashedPassword = userAccount.getPassword();
            if (!defaultPasswordService.passwordsMatch(password, hashedPassword)) {
                userAccount = null;
            }
        }

        return userAccount;
    }

    class CustomSecurityContext implements SecurityContext {

        private final Principal principal;
        private final Set<String> roles;
        private final String authScheme;
        private final boolean secure;

        public CustomSecurityContext(String username, Set<String> roles, String authScheme, boolean secure) {
            this.principal = () -> {
                return username;
            };
            this.roles = roles;
            this.authScheme = authScheme;
            this.secure = secure;
        }

        @Override
        public Principal getUserPrincipal() {
            return principal;
        }

        @Override
        public boolean isUserInRole(String role) {
            return roles.contains(role);
        }

        @Override
        public boolean isSecure() {
            return secure;
        }

        @Override
        public String getAuthenticationScheme() {
            return authScheme;
        }

    }

}
