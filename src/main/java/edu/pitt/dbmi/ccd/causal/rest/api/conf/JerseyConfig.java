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
package edu.pitt.dbmi.ccd.causal.rest.api.conf;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.springframework.stereotype.Component;

/**
 *
 * Jun 4, 2016 5:12:43 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Component
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        packages(
                "edu.pitt.dbmi.ccd.causal.rest.api.endpoint",
                "edu.pitt.dbmi.ccd.causal.rest.api.exception.mapper",
                "edu.pitt.dbmi.ccd.causal.rest.api.filter"
        );

        register(RolesAllowedDynamicFeature.class);

        // https://jersey.java.net/documentation/latest/media.html 9.3. Multipart
        // http://stackoverflow.com/questions/30653012/multipart-form-data-no-injection-source-found-for-a-parameter-of-type-public-ja
        register(MultiPartFeature.class);
    }

}
