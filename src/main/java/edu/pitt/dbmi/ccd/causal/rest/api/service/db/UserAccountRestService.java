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
package edu.pitt.dbmi.ccd.causal.rest.api.service.db;

import edu.pitt.dbmi.ccd.causal.rest.api.repository.UserAccountRestRepository;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Jun 10, 2016 3:02:32 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class UserAccountRestService {

    private final UserAccountRestRepository userAccountRepository;

    @Autowired
    public UserAccountRestService(UserAccountRestRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public UserAccount findByUsername(String username) {
        return userAccountRepository.findByUsername(username);
    }

}
