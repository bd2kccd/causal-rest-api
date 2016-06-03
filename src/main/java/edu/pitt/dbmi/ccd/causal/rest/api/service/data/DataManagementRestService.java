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
package edu.pitt.dbmi.ccd.causal.rest.api.service.data;

import edu.pitt.dbmi.ccd.causal.rest.api.dto.data.DataFileDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.error.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.db.entity.DataFile;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.DataFileService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Jun 3, 2016 3:39:06 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class DataManagementRestService {

    private final UserAccountService userAccountService;

    private final DataFileService dataFileService;

    @Autowired
    public DataManagementRestService(UserAccountService userAccountService, DataFileService dataFileService) {
        this.userAccountService = userAccountService;
        this.dataFileService = dataFileService;
    }

    public List<DataFileDTO> listUserDataFiles(String username) {
        List<DataFileDTO> dataFileDTOs = new LinkedList<>();

        UserAccount userAccount = userAccountService.findByUsername(username);
        if (userAccount == null) {
            throw new ResourceNotFoundException("No such user found.");
        }

        List<DataFile> dataFiles = dataFileService.findByUserAccounts(Collections.singleton(userAccount));
        dataFiles.forEach(dataFile -> {
            DataFileDTO dto = new DataFileDTO();
            dto.setCreationTime(dataFile.getCreationTime());
            dto.setFileSize(dataFile.getFileSize());
            dto.setId(dataFile.getId());
            dto.setLastModifiedTime(dataFile.getLastModifiedTime());
            dto.setName(dataFile.getName());

            dataFileDTOs.add(dto);
        });

        return dataFileDTOs;
    }

}
