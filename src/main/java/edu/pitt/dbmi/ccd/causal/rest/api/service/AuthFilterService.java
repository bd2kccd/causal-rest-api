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

import edu.pitt.dbmi.ccd.causal.rest.api.Role;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.AccessDeniedException;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.AccessForbiddenException;
import edu.pitt.dbmi.ccd.causal.rest.api.service.db.UserAccountRestService;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.UserRole;
import java.security.Principal;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Jun 5, 2016 10:52:45 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class AuthFilterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthFilterService.class);

    private static final String AUTH_HEADER = "Authorization";
    public static final String AUTH_SCHEME_BASIC = "Basic";

    private static final AccessDeniedException USER_CREDENTIALS_REQUIRED = new AccessDeniedException("User credentials are required.");
    private static final AccessDeniedException INVALID_USER_CREDENTIALS = new AccessDeniedException("Invalid username and/or password.");
    private static final AccessForbiddenException FORBIDDEN_ACCESS = new AccessForbiddenException("You cannot access this resource.");

    private final UserAccountRestService userAccountRestService;
    private final DefaultPasswordService defaultPasswordService;

    @Autowired
    public AuthFilterService(UserAccountRestService userAccountRestService, DefaultPasswordService defaultPasswordService) {
        this.userAccountRestService = userAccountRestService;
        this.defaultPasswordService = defaultPasswordService;
    }

    public void doBasicAuth(ContainerRequestContext requestContext) {
        String authCredentials = requestContext.getHeaderString(AUTH_HEADER);
        if (authCredentials == null || !authCredentials.contains(AUTH_SCHEME_BASIC)) {
            throw USER_CREDENTIALS_REQUIRED;
        }

        String authCredentialBase64 = authCredentials.replaceFirst(AUTH_SCHEME_BASIC, "").trim();
        String credentials = new String(Base64.getDecoder().decode(authCredentialBase64));
        UserAccount userAccount = retrieveUserAccount(credentials);
        if (userAccount == null) {
            throw INVALID_USER_CREDENTIALS;
        }

        SecurityContext securityContext = createSecurityContext(userAccount, requestContext, SecurityContext.BASIC_AUTH);
        if (!(securityContext.isUserInRole("admin") || isAccountMatchesRequest(userAccount, requestContext))) {
            throw FORBIDDEN_ACCESS;
        }
        requestContext.setSecurityContext(securityContext);
    }

    private boolean isAccountMatchesRequest(UserAccount userAccount, ContainerRequestContext requestContext) {
        MultivaluedMap<String, String> pathParams = requestContext.getUriInfo().getPathParameters();
        String username = pathParams.getFirst("username");

        return userAccount.getUsername().equals(username);
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

    private UserAccount retrieveUserAccount(String credentials) {
        StringTokenizer tokenizer = new StringTokenizer(credentials, ":");
        String username = tokenizer.nextToken();
        String password = tokenizer.nextToken();

        UserAccount userAccount = userAccountRestService.findByUsername(username);
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
