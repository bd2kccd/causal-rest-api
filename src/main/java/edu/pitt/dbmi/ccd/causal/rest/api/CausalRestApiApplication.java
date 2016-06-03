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
package edu.pitt.dbmi.ccd.causal.rest.api;

import edu.pitt.dbmi.ccd.db.CCDDatabaseApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 *
 * Jun 3, 2016 3:05:13 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@SpringBootApplication
@Import({CCDDatabaseApplication.class})
public class CausalRestApiApplication {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(CausalRestApiApplication.class, args);
    }

}
