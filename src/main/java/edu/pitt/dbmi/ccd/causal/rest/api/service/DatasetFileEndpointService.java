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

import edu.pitt.dbmi.ccd.causal.rest.api.dto.DatasetFileDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.DatasetFileSummarization;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.DatasetFileSummaryDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.InternalErrorException;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.NotFoundByIdException;
import edu.pitt.dbmi.ccd.commons.file.info.FileInfos;
import edu.pitt.dbmi.ccd.db.entity.DataFile;
import edu.pitt.dbmi.ccd.db.entity.DataFileInfo;
import edu.pitt.dbmi.ccd.db.entity.FileDelimiter;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.VariableType;
import edu.pitt.dbmi.ccd.db.service.DataFileService;
import edu.pitt.dbmi.ccd.db.service.FileDelimiterService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
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
public class DatasetFileEndpointService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetFileEndpointService.class);

    private final UserAccountService userAccountService;

    private final DataFileService dataFileService;

    private final VariableTypeService variableTypeService;

    private final FileDelimiterService fileDelimiterService;

    @Autowired
    public DatasetFileEndpointService(
            UserAccountService userAccountService,
            DataFileService dataFileService,
            VariableTypeService variableTypeService,
            FileDelimiterService fileDelimiterService) {
        this.userAccountService = userAccountService;
        this.dataFileService = dataFileService;
        this.variableTypeService = variableTypeService;
        this.fileDelimiterService = fileDelimiterService;
    }

    /**
     * Delete a dataset file for a given file ID of a given user
     *
     * @param id
     * @param uid
     */
    public void deleteByIdAndUid(Long id, Long uid) {
        // When we can get here vai AuthFilterSerice, it means the user exists
        // so no need to check if (userAccount == null) and throw UserNotFoundException(uid)
        UserAccount userAccount = userAccountService.findById(uid);

        DataFile dataFile = dataFileService.findByIdAndUserAccount(id, userAccount);
        if (dataFile == null) {
            throw new NotFoundByIdException(id);
        }

        try {
            // Delete records from data_file_info table and data_file table
            dataFileService.deleteDataFile(dataFile);
            // Delete the physical file from workspace folder
            Files.deleteIfExists(Paths.get(dataFile.getAbsolutePath(), dataFile.getName()));
            LOGGER.info(String.format("Dataset file '%s' (id=%d) has been deleted.", dataFile.getName(), id));
        } catch (Exception exception) {
            String errMsg = String.format("Unable to delete dataset file id=%d.", id);
            LOGGER.error(errMsg, exception);
            throw new InternalErrorException(errMsg);
        }
    }

    /**
     * Get a dataset file info for a given file ID of a given user
     *
     * @param id
     * @param uid
     * @return
     */
    public DatasetFileDTO findByIdAndUid(Long id, Long uid) {
        // When we can get here vai AuthFilterSerice, it means the user exists
        // so no need to check if (userAccount == null) and throw UserNotFoundException(uid)
        UserAccount userAccount = userAccountService.findById(uid);

        DataFile dataFile = dataFileService.findByIdAndUserAccount(id, userAccount);
        if (dataFile == null) {
            LOGGER.warn(String.format("Can not find data file id=%d for user id=%d.", id, uid));
            throw new NotFoundByIdException(id);
        }

        DatasetFileDTO datasetFileDTO = new DatasetFileDTO();

        DatasetFileSummaryDTO datasetFileSummaryDTO = new DatasetFileSummaryDTO();

        DataFileInfo dataFileInfo = dataFile.getDataFileInfo();

        // Will get java.lang.NullPointerException when calling dataFileInfo.getFileDelimiter().getName()
        // while dataFileInfo.getFileDelimiter() returns null
        if (dataFileInfo.getFileDelimiter() != null) {
            datasetFileSummaryDTO.setFileDelimiter(dataFileInfo.getFileDelimiter().getName());
        } else {
            datasetFileSummaryDTO.setFileDelimiter(null);
        }

        // Will get java.lang.NullPointerException when calling dataFileInfo.getVariableType().getName()
        // while dataFileInfo.getVariableType() returns null
        if (dataFileInfo.getVariableType() != null) {
            datasetFileSummaryDTO.setVariableType(dataFileInfo.getVariableType().getName());
        } else {
            datasetFileSummaryDTO.setVariableType(null);
        }

        datasetFileSummaryDTO.setNumOfColumns(dataFileInfo.getNumOfColumns());
        datasetFileSummaryDTO.setNumOfRows(dataFileInfo.getNumOfRows());

        datasetFileDTO.setId(dataFile.getId());
        datasetFileDTO.setName(dataFile.getName());
        datasetFileDTO.setCreationTime(dataFile.getCreationTime());
        datasetFileDTO.setFileSize(dataFile.getFileSize());
        datasetFileDTO.setLastModifiedTime(dataFile.getLastModifiedTime());
        datasetFileDTO.setMd5checkSum(dataFileInfo.getMd5checkSum());
        datasetFileDTO.setFileSummary(datasetFileSummaryDTO);

        return datasetFileDTO;
    }

    /**
     * List all the available dataset files for a given user ID
     *
     * @param uid
     * @return
     */
    public List<DatasetFileDTO> listAllDatasetFiles(Long uid) {
        List<DatasetFileDTO> dataFileDTOs = new LinkedList<>();

        // When we can get here vai AuthFilterSerice, it means the user exists
        // so no need to check if (userAccount == null) and throw UserNotFoundException(uid)
        UserAccount userAccount = userAccountService.findById(uid);

        List<DataFile> dataFiles = dataFileService.findByUserAccount(userAccount);
        dataFiles.forEach(dataFile -> {
            String fileName = dataFile.getName();

            // skip prior file
            if (!fileName.endsWith(".prior")) {
                DatasetFileDTO dataFileDTO = new DatasetFileDTO();

                DatasetFileSummaryDTO dataFileSummaryDTO = new DatasetFileSummaryDTO();

                DataFileInfo dataFileInfo = dataFile.getDataFileInfo();

                // Will get java.lang.NullPointerException when calling dataFileInfo.getFileDelimiter().getName()
                // while dataFileInfo.getFileDelimiter() returns null
                if (dataFileInfo.getFileDelimiter() != null) {
                    dataFileSummaryDTO.setFileDelimiter(dataFileInfo.getFileDelimiter().getName());
                } else {
                    dataFileSummaryDTO.setFileDelimiter(null);
                }

                // Will get java.lang.NullPointerException when calling dataFileInfo.getVariableType().getName()
                // while dataFileInfo.getVariableType() returns null
                if (dataFileInfo.getVariableType() != null) {
                    dataFileSummaryDTO.setVariableType(dataFileInfo.getVariableType().getName());
                } else {
                    dataFileSummaryDTO.setVariableType(null);
                }

                dataFileSummaryDTO.setNumOfColumns(dataFileInfo.getNumOfColumns());
                dataFileSummaryDTO.setNumOfRows(dataFileInfo.getNumOfRows());

                dataFileDTO.setId(dataFile.getId());
                dataFileDTO.setName(dataFile.getName());
                dataFileDTO.setCreationTime(dataFile.getCreationTime());
                dataFileDTO.setFileSize(dataFile.getFileSize());
                dataFileDTO.setLastModifiedTime(dataFile.getLastModifiedTime());
                dataFileDTO.setMd5checkSum(dataFileInfo.getMd5checkSum());
                dataFileDTO.setFileSummary(dataFileSummaryDTO);

                dataFileDTOs.add(dataFileDTO);
            }
        });

        return dataFileDTOs;
    }

    /**
     * Summarize data file by adding fileDelimiter, variableType, numOfRows,
     * numOfColumns, and missingValue
     *
     * @param uid
     * @param datasetFileSummarization
     * @return The info of just summarized file
     * @throws IOException
     */
    public DatasetFileDTO summarizeDatasetFile(Long uid, DatasetFileSummarization datasetFileSummarization) throws IOException {
        Long id = datasetFileSummarization.getId();

        // When we can get here vai AuthFilterSerice, it means the user exists
        // so no need to check if (userAccount == null) and throw UserNotFoundException(uid)
        UserAccount userAccount = userAccountService.findById(uid);

        DataFile dataFile = dataFileService.findByIdAndUserAccount(id, userAccount);
        if (dataFile == null) {
            throw new NotFoundByIdException(id);
        }

        DataFileInfo dataFileInfo = dataFile.getDataFileInfo();

        // Since we only get the string values from request
        // Here we'll need to convert the string values to FileDelimiter and VariableType objects
        FileDelimiter fileDelimiter = fileDelimiterService.getFileDelimiterRepository().findByName(datasetFileSummarization.getFileDelimiter());
        VariableType variableType = variableTypeService.findByName(datasetFileSummarization.getVariableType());

        // Set file delimiter and variable type
        dataFileInfo.setFileDelimiter(fileDelimiter);
        dataFileInfo.setVariableType(variableType);

        // Set the numbers of columns and rows based on the physical file
        char delimiter = FileInfos.delimiterNameToChar(datasetFileSummarization.getFileDelimiter());
        Path file = Paths.get(dataFile.getAbsolutePath(), dataFile.getName());
        dataFileInfo.setNumOfRows(FileInfos.countLine(file.toFile()));
        dataFileInfo.setNumOfColumns(FileInfos.countColumn(file.toFile(), delimiter));

        // Ignore missing value here since it'll be removed from db in the new design
        // Update the dataFileInfo property
        dataFile.setDataFileInfo(dataFileInfo);

        // Update record in database table `data_file_info`
        dataFileService.saveDataFile(dataFile);

        // Create DTO to be used for HTTP response
        DatasetFileDTO datasetFileDTO = new DatasetFileDTO();

        DatasetFileSummaryDTO datasetFileSummaryDTO = new DatasetFileSummaryDTO();

        // Will get java.lang.NullPointerException when calling dataFileInfo.getFileDelimiter().getName()
        // while dataFileInfo.getFileDelimiter() returns null
        if (dataFileInfo.getFileDelimiter() != null) {
            datasetFileSummaryDTO.setFileDelimiter(dataFileInfo.getFileDelimiter().getName());
        } else {
            datasetFileSummaryDTO.setFileDelimiter(null);
        }

        // Will get java.lang.NullPointerException when calling dataFileInfo.getVariableType().getName()
        // while dataFileInfo.getVariableType() returns null
        if (dataFileInfo.getVariableType() != null) {
            datasetFileSummaryDTO.setVariableType(dataFileInfo.getVariableType().getName());
        } else {
            datasetFileSummaryDTO.setVariableType(null);
        }

        datasetFileSummaryDTO.setNumOfColumns(dataFileInfo.getNumOfColumns());
        datasetFileSummaryDTO.setNumOfRows(dataFileInfo.getNumOfRows());

        datasetFileDTO.setId(dataFile.getId());
        datasetFileDTO.setName(dataFile.getName());
        datasetFileDTO.setCreationTime(dataFile.getCreationTime());
        datasetFileDTO.setFileSize(dataFile.getFileSize());
        datasetFileDTO.setLastModifiedTime(dataFile.getLastModifiedTime());
        datasetFileDTO.setMd5checkSum(dataFileInfo.getMd5checkSum());
        datasetFileDTO.setFileSummary(datasetFileSummaryDTO);

        LOGGER.info(String.format("Dataset file '%s' (id=%d) has been summarized successfully.", dataFile.getName(), dataFile.getId()));

        return datasetFileDTO;
    }

}
