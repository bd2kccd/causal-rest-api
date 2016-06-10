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

import edu.pitt.dbmi.ccd.causal.rest.api.repository.DataFileInfoRestRepository;
import edu.pitt.dbmi.ccd.causal.rest.api.repository.DataFileRestRepository;
import edu.pitt.dbmi.ccd.db.entity.DataFile;
import edu.pitt.dbmi.ccd.db.entity.DataFileInfo;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * Jun 10, 2016 2:49:09 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
@Transactional
public class DataFileRestService {

    private final DataFileRestRepository dataFileRestRepository;

    private final DataFileInfoRestRepository dataFileInfoRestRepository;

    @Autowired
    public DataFileRestService(DataFileRestRepository dataFileRestRepository, DataFileInfoRestRepository dataFileInfoRestRepository) {
        this.dataFileRestRepository = dataFileRestRepository;
        this.dataFileInfoRestRepository = dataFileInfoRestRepository;
    }

    public DataFile findByIdAndUserAccount(Long id, UserAccount userAccount) {
        return dataFileRestRepository.findByIdAndUserAccounts(id, Collections.singleton(userAccount));
    }

    public List<DataFile> findByUserAccount(UserAccount userAccount) {
        return dataFileRestRepository.findByUserAccounts(Collections.singleton(userAccount));
    }

    public void delete(DataFile dataFile) {
        DataFileInfo dataFileInfo = dataFile.getDataFileInfo();
        if (dataFileInfo != null) {
            dataFileInfoRestRepository.delete(dataFileInfo);
        }

        dataFileRestRepository.delete(dataFile);
    }

}
