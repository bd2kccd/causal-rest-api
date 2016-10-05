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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
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

    // The JWT is sent via the Authorization header using the Bearer authentication scheme
    private static final String AUTH_HEADER = "Authorization";
    public static final String AUTH_SCHEME_BEARER = "Bearer";

    private static final AccessDeniedException JWT_REQUIRED = new AccessDeniedException("API JWT(JSON Web Token) is required.");
    private static final AccessDeniedException INVALID_JWT = new AccessDeniedException("Invalid JWT(JSON Web Token).");
    private static final AccessForbiddenException FORBIDDEN_ACCESS = new AccessForbiddenException("You cannot access this resource.");

    private final UserAccountService userAccountService;

    @Autowired
    public AuthFilterService(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    public void auth(ContainerRequestContext requestContext) throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, IOException, SignatureException, JWTVerifyException {
        String authCredentials = requestContext.getHeaderString(AUTH_HEADER);
        if (authCredentials == null || !authCredentials.contains(AUTH_SCHEME_BEARER)) {
            throw JWT_REQUIRED;
        }

        String jwt = authCredentials.replaceFirst(AUTH_SCHEME_BEARER, "").trim();
        // Verify both secret and issuer
        final JWTVerifier jwtVerifier = new JWTVerifier(jwtSecret, null, jwtIssuer);
        final Map<String, Object> claims = jwtVerifier.verify(jwt);

        String username = claims.get("username").toString();
        boolean valid = (Boolean) claims.get("valid");

        if (valid != true) {
            throw INVALID_JWT;
        }

        UserAccount userAccount = userAccountService.findByUsername(username);

        SecurityContext securityContext = createSecurityContext(userAccount, requestContext, SecurityContext.BASIC_AUTH);
        if (!(securityContext.isUserInRole("admin") || isAccountMatchesRequest(username, requestContext))) {
            throw FORBIDDEN_ACCESS;
        }
        requestContext.setSecurityContext(securityContext);
    }

    private boolean isAccountMatchesRequest(String jwtUsername, ContainerRequestContext requestContext) {
        MultivaluedMap<String, String> pathParams = requestContext.getUriInfo().getPathParameters();
        String username = pathParams.getFirst("username");

        return jwtUsername.equals(username);
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
