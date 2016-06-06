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
package edu.pitt.dbmi.ccd.causal.rest.api.filter;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

/**
 *
 * Jun 5, 2016 10:49:39 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Provider
public class CORSFilter implements ContainerResponseFilter {

    private static final String ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";

    private static final String REQUEST_HEADER = "Access-Control-Request-Headers";

    private static final String ALLOW_HEADER = "Access-Control-Allow-Headers";

    private static final String ALLOW_METHODS_HEADER = "Access-Control-Allow-Methods";

    private static final String ALLOW_CREDENTIALS_HEADER = "Access-Control-Allow-Credentials";

    private static final String MAX_AGE_HEADER = "Access-Control-Max-Age";

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        MultivaluedMap<String, Object> headers = responseContext.getHeaders();
        headers.add(ALLOW_ORIGIN_HEADER, "*");
        headers.add(ALLOW_METHODS_HEADER, "POST, GET, OPTIONS, DELETE");
        headers.add(MAX_AGE_HEADER, "3600");
        headers.add(ALLOW_HEADER, "x-requested-with");
    }

}
