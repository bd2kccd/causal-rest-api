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

import edu.pitt.dbmi.ccd.causal.rest.api.dto.DataFileDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.InternalErrorException;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.NotFoundByIdException;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.UserNotFoundException;
import edu.pitt.dbmi.ccd.causal.rest.api.prop.CausalRestProperties;
import edu.pitt.dbmi.ccd.causal.rest.api.service.db.DataFileRestService;
import edu.pitt.dbmi.ccd.causal.rest.api.service.db.UserAccountRestService;
import edu.pitt.dbmi.ccd.commons.file.info.FileInfos;
import edu.pitt.dbmi.ccd.db.entity.DataFile;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Jun 5, 2016 9:42:03 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class DataFileEndpointService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataFileEndpointService.class);

    private final CausalRestProperties causalRestProperties;

    private final UserAccountRestService userAccountRestService;

    private final DataFileRestService dataFileRestService;

    @Autowired
    public DataFileEndpointService(CausalRestProperties causalRestProperties, UserAccountRestService userAccountRestService, DataFileRestService dataFileRestService) {
        this.causalRestProperties = causalRestProperties;
        this.userAccountRestService = userAccountRestService;
        this.dataFileRestService = dataFileRestService;
    }

    public void deleteByIdAndUsername(Long id, String username) {
        UserAccount userAccount = userAccountRestService.findByUsername(username);
        if (userAccount == null) {
            throw new UserNotFoundException(username);
        }

        DataFile dataFile = dataFileRestService.findByIdAndUserAccount(id, userAccount);
        if (dataFile == null) {
            throw new NotFoundByIdException(id);
        }

        try {
            dataFileRestService.delete(dataFile);
            Files.deleteIfExists(Paths.get(dataFile.getAbsolutePath(), dataFile.getName()));
        } catch (Exception exception) {
            String errMsg = String.format("Unable to delete data file id=%d.", id);
            LOGGER.error(errMsg, exception);
            throw new InternalErrorException(errMsg);
        }
    }

    public DataFileDTO findByIdAndUsername(Long id, String username) {
        UserAccount userAccount = userAccountRestService.findByUsername(username);
        if (userAccount == null) {
            throw new UserNotFoundException(username);
        }

        DataFile dataFile = dataFileRestService.findByIdAndUserAccount(id, userAccount);
        if (dataFile == null) {
            throw new NotFoundByIdException(id);
        }

        DataFileDTO dataFileDTO = new DataFileDTO();
        dataFileDTO.setCreationTime(dataFile.getCreationTime());
        dataFileDTO.setFileSize(dataFile.getFileSize());
        dataFileDTO.setId(dataFile.getId());
        dataFileDTO.setLastModifiedTime(dataFile.getLastModifiedTime());
        dataFileDTO.setName(dataFile.getName());

        return dataFileDTO;
    }

    public List<DataFileDTO> listDataFiles(String username) {
        List<DataFileDTO> dataFileDTOs = new LinkedList<>();

        UserAccount userAccount = userAccountRestService.findByUsername(username);
        if (userAccount == null) {
            throw new UserNotFoundException(username);
        }

        List<DataFile> dataFiles = dataFileRestService.findByUserAccount(userAccount);
        dataFiles.forEach(dataFile -> {
            DataFileDTO dataFileDTO = new DataFileDTO();
            dataFileDTO.setCreationTime(dataFile.getCreationTime());
            dataFileDTO.setFileSize(dataFile.getFileSize());
            dataFileDTO.setId(dataFile.getId());
            dataFileDTO.setLastModifiedTime(dataFile.getLastModifiedTime());
            dataFileDTO.setName(dataFile.getName());

            dataFileDTOs.add(dataFileDTO);
        });

        return dataFileDTOs;
    }

    private void synchronizeDataFiles(UserAccount userAccount) {
        // get all the user's dataset from the database
        List<DataFile> dataFiles = dataFileRestService.findByUserAccount(userAccount);
        Map<String, DataFile> dbDataFile = new HashMap<>();
        dataFiles.forEach(file -> {
            dbDataFile.put(file.getName(), file);
        });

        String workspaceDir = causalRestProperties.getWorkspaceDir();
        String dataFolder = causalRestProperties.getDataFolder();
        Path dataDir = Paths.get(workspaceDir, userAccount.getUsername(), dataFolder);

        Map<String, DataFile> saveFiles = new HashMap<>();
        try {
            List<Path> localFiles = FileInfos.listDirectory(dataDir, false);
            localFiles.forEach(localFile -> {
            });
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }
    }

}
