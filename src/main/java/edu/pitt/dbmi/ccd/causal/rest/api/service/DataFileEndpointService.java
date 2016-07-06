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
import edu.pitt.dbmi.ccd.causal.rest.api.dto.DataFileSummarization;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.DataFileSummaryDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.ResumableChunkViaGet;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.ResumableChunkViaPost;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.InternalErrorException;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.NotFoundByIdException;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.UserNotFoundException;
import edu.pitt.dbmi.ccd.causal.rest.api.prop.CausalRestProperties;
import edu.pitt.dbmi.ccd.commons.file.MessageDigestHash;
import edu.pitt.dbmi.ccd.commons.file.info.BasicFileInfo;
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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
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

    private final UserAccountService userAccountService;

    private final DataFileService dataFileService;

    private final VariableTypeService variableTypeService;

    private final FileDelimiterService fileDelimiterService;

    @Autowired
    public DataFileEndpointService(
            CausalRestProperties causalRestProperties,
            UserAccountService userAccountService,
            DataFileService dataFileService,
            VariableTypeService variableTypeService,
            FileDelimiterService fileDelimiterService) {
        this.causalRestProperties = causalRestProperties;
        this.userAccountService = userAccountService;
        this.dataFileService = dataFileService;
        this.variableTypeService = variableTypeService;
        this.fileDelimiterService = fileDelimiterService;
    }

    /**
     * Delete a data file for a given file ID
     *
     * @param id
     * @param username
     */
    public void deleteByIdAndUsername(Long id, String username) {
        UserAccount userAccount = userAccountService.findByUsername(username);
        if (userAccount == null) {
            throw new UserNotFoundException(username);
        }

        DataFile dataFile = dataFileService.findByIdAndUserAccount(id, userAccount);
        if (dataFile == null) {
            throw new NotFoundByIdException(id);
        }

        try {
            // Delete records from data_file_info table and data_file table
            dataFileService.deleteDataFile(dataFile);
            // Delete the physical file from workspace folder
            Files.deleteIfExists(Paths.get(dataFile.getAbsolutePath(), dataFile.getName()));
        } catch (Exception exception) {
            String errMsg = String.format("Unable to delete data file id=%d.", id);
            LOGGER.error(errMsg, exception);
            throw new InternalErrorException(errMsg);
        }
    }

    /**
     * Get a data file info for a given file ID
     *
     * @param id
     * @param username
     * @return
     */
    public DataFileDTO findByIdAndUsername(Long id, String username) {
        UserAccount userAccount = userAccountService.findByUsername(username);
        if (userAccount == null) {
            throw new UserNotFoundException(username);
        }

        DataFile dataFile = dataFileService.findByIdAndUserAccount(id, userAccount);
        if (dataFile == null) {
            throw new NotFoundByIdException(id);
        }

        DataFileDTO dataFileDTO = new DataFileDTO();

        DataFileSummaryDTO dataFileSummaryDTO = new DataFileSummaryDTO();

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

        dataFileSummaryDTO.setMd5checkSum(dataFileInfo.getMd5checkSum());
        dataFileSummaryDTO.setNumOfColumns(dataFileInfo.getNumOfColumns());
        dataFileSummaryDTO.setNumOfRows(dataFileInfo.getNumOfRows());

        dataFileDTO.setCreationTime(dataFile.getCreationTime());
        dataFileDTO.setFileSize(dataFile.getFileSize());
        dataFileDTO.setId(dataFile.getId());
        dataFileDTO.setLastModifiedTime(dataFile.getLastModifiedTime());
        dataFileDTO.setName(dataFile.getName());
        dataFileDTO.setFileSummary(dataFileSummaryDTO);

        return dataFileDTO;
    }

    /**
     * List all the available data files for a given user
     *
     * @param username
     * @return
     */
    public List<DataFileDTO> listDataFiles(String username) {
        List<DataFileDTO> dataFileDTOs = new LinkedList<>();

        UserAccount userAccount = userAccountService.findByUsername(username);
        if (userAccount == null) {
            throw new UserNotFoundException(username);
        }

        List<DataFile> dataFiles = dataFileService.findByUserAccount(userAccount);
        dataFiles.forEach(dataFile -> {
            DataFileDTO dataFileDTO = new DataFileDTO();

            DataFileSummaryDTO dataFileSummaryDTO = new DataFileSummaryDTO();

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

            // md5checkSum is always created by the time we add new data file
            dataFileSummaryDTO.setMd5checkSum(dataFileInfo.getMd5checkSum());
            dataFileSummaryDTO.setNumOfColumns(dataFileInfo.getNumOfColumns());
            dataFileSummaryDTO.setNumOfRows(dataFileInfo.getNumOfRows());

            dataFileDTO.setId(dataFile.getId());
            dataFileDTO.setName(dataFile.getName());
            dataFileDTO.setCreationTime(dataFile.getCreationTime());
            dataFileDTO.setFileSize(dataFile.getFileSize());
            dataFileDTO.setLastModifiedTime(dataFile.getLastModifiedTime());
            dataFileDTO.setFileSummary(dataFileSummaryDTO);

            dataFileDTOs.add(dataFileDTO);
        });

        return dataFileDTOs;
    }

    /**
     * Small file upload, not resumable
     *
     * @param username
     * @param inputStream
     * @param fileDetail
     * @return Info of just uploaded file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public DataFileDTO upload(String username, InputStream inputStream, FormDataContentDisposition fileDetail) throws FileNotFoundException, IOException {
        String workspaceDir = causalRestProperties.getWorkspaceDir();
        String dataFolder = causalRestProperties.getDataFolder();
        String fileName = fileDetail.getFileName();

        Path uploadedFile = Paths.get(workspaceDir, username, dataFolder, fileName);

        // Actual file upload, will make it resumeble by uploading chunk by chunk
        // Also need to use md5checksum to make sure the file is complete
        int read = 0;
        byte[] bytes = new byte[1024];

        OutputStream out = new FileOutputStream(new File(uploadedFile.toString()));
        while ((read = inputStream.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
        out.flush();

        // Now if everything worked fine, the new file should have been uploaded
        // Then we'll also need to insert the data file info into three database tables:
        // `data_file_info`, `data_file`, and `user_account_data_file_rel`
        // First we'll need to know who uploaded this file
        UserAccount userAccount = userAccountService.findByUsername(username);

        // Get file information with FileInfos of ccd-commons
        BasicFileInfo fileInfo = FileInfos.basicPathInfo(uploadedFile);

        // By now these info are solely based on the file system
        String directory = fileInfo.getAbsolutePath().toString();
        long size = fileInfo.getSize();
        long creationTime = fileInfo.getCreationTime();
        long lastModifiedTime = fileInfo.getLastModifiedTime();

        // Let's check if a file with the same fileName has already been there
        DataFile dataFile = dataFileService.findByAbsolutePathAndName(directory, fileName);

        if (dataFile == null) {
            dataFile = new DataFile();
            dataFile.setUserAccounts(Collections.singleton(userAccount));
        }

        dataFile.setName(fileName);
        dataFile.setAbsolutePath(directory);
        dataFile.setCreationTime(new Date(creationTime));
        dataFile.setFileSize(size);
        dataFile.setLastModifiedTime(new Date(lastModifiedTime));

        // Generate md5 checksum based on the file path
        String md5checkSum = MessageDigestHash.computeMD5Hash(uploadedFile);

        DataFileInfo dataFileInfo = dataFile.getDataFileInfo();

        if (dataFileInfo == null) {
            dataFileInfo = new DataFileInfo();
        }

        dataFileInfo.setFileDelimiter(null);
        dataFileInfo.setVariableType(null);
        dataFileInfo.setMd5checkSum(md5checkSum);
        dataFileInfo.setNumOfColumns(null);
        dataFileInfo.setNumOfRows(null);

        // Now add new records into database
        dataFile.setDataFileInfo(dataFileInfo);
        dataFileService.saveDataFile(dataFile);

        // Create DTO to be used for HTTP response
        DataFileDTO dataFileDTO = new DataFileDTO();

        // We should get the data from database since the new record has an ID
        // that can be used for later API calls
        // All other info can be obtained solely based on the file system but no ID
        DataFile newDataFile = dataFileService.findByAbsolutePathAndName(directory, fileName);

        DataFileSummaryDTO dataFileSummaryDTO = new DataFileSummaryDTO();

        dataFileSummaryDTO.setFileDelimiter(null);
        dataFileSummaryDTO.setVariableType(null);
        dataFileSummaryDTO.setMd5checkSum(md5checkSum);
        dataFileSummaryDTO.setNumOfColumns(null);
        dataFileSummaryDTO.setNumOfRows(null);

        dataFileDTO.setId(newDataFile.getId());
        dataFileDTO.setName(newDataFile.getName());
        dataFileDTO.setCreationTime(newDataFile.getCreationTime());
        dataFileDTO.setFileSize(newDataFile.getFileSize());
        dataFileDTO.setLastModifiedTime(newDataFile.getLastModifiedTime());
        dataFileDTO.setFileSummary(dataFileSummaryDTO);

        return dataFileDTO;
    }

    /**
     * Chunk upload, check chunk existence
     *
     * @param chunk
     * @param username
     * @return true or false
     * @throws IOException
     */
    public boolean chunkExists(ResumableChunkViaGet chunk, String username) throws IOException {
        String identifier = chunk.getResumableIdentifier();
        int chunkNumber = chunk.getResumableChunkNumber();

        String workspaceDir = causalRestProperties.getWorkspaceDir();
        String dataFolder = causalRestProperties.getDataFolder();

        Path chunkFile = Paths.get(workspaceDir, username, dataFolder, identifier, Integer.toString(chunkNumber));

        if (Files.exists(chunkFile)) {
            long size = (Long) Files.getAttribute(chunkFile, "basic:size");
            return (size == chunk.getResumableChunkSize());
        }

        return false;
    }

    /**
     * Chunk upload, upload each chunk data and generate md5checksum on
     * completion of the whole file
     *
     * @param chunk
     * @param username
     * @return md5checksum string
     * @throws IOException
     */
    public String uploadChunk(ResumableChunkViaPost chunk, String username) throws IOException {
        String fileName = chunk.getResumableFilename();
        String md5checkSum = null;

        try {
            storeChunk(chunk, username);
            if (allChunksUploaded(chunk, username)) {
                md5checkSum = mergeDeleteSave(chunk, username);
            }
        } catch (IOException exception) {
            String errorMsg = String.format("Unable to upload chunk %s.", fileName);
            LOGGER.error(errorMsg, exception);
            throw exception;
        }

        return md5checkSum;
    }

    /**
     * Chunk upload, store chunk data to the data folder
     *
     * @param chunk
     * @param username
     * @throws IOException
     */
    public void storeChunk(ResumableChunkViaPost chunk, String username) throws IOException {
        String identifier = chunk.getResumableIdentifier();
        int chunkNumber = chunk.getResumableChunkNumber();

        String workspaceDir = causalRestProperties.getWorkspaceDir();
        String dataFolder = causalRestProperties.getDataFolder();

        Path chunkFile = Paths.get(workspaceDir, username, dataFolder, identifier, Integer.toString(chunkNumber));

        if (Files.notExists(chunkFile)) {
            try {
                Files.createDirectories(chunkFile);
            } catch (IOException exception) {
                LOGGER.error(exception.getMessage());
            }
        }
        Files.copy(chunk.getFile(), chunkFile, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Chunk upload, check if all chunks are uploaded
     *
     * @param chunk
     * @param username
     * @return true or false
     * @throws IOException
     */
    public boolean allChunksUploaded(ResumableChunkViaPost chunk, String username) throws IOException {
        String identifier = chunk.getResumableIdentifier();
        int numOfChunks = chunk.getResumableTotalChunks();

        String workspaceDir = causalRestProperties.getWorkspaceDir();
        String dataFolder = causalRestProperties.getDataFolder();

        for (int chunkNo = 1; chunkNo <= numOfChunks; chunkNo++) {
            if (!Files.exists(Paths.get(workspaceDir, username, dataFolder, identifier, Integer.toString(chunkNo)))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Chunk upload, save data information to database
     *
     * @param file
     * @param username
     * @return md5checkSum string
     * @throws IOException
     */
    private String saveDataFile(Path file, String username) throws IOException {
        UserAccount userAccount = userAccountService.findByUsername(username);

        BasicFileInfo fileInfo = FileInfos.basicPathInfo(file);
        String directory = fileInfo.getAbsolutePath().toString();
        String fileName = fileInfo.getFilename();
        long size = fileInfo.getSize();
        long creationTime = fileInfo.getCreationTime();
        long lastModifiedTime = fileInfo.getLastModifiedTime();

        DataFile dataFile = dataFileService.findByAbsolutePathAndName(directory, fileName);
        if (dataFile == null) {
            dataFile = new DataFile();
            dataFile.setUserAccounts(Collections.singleton(userAccount));
        }
        dataFile.setName(fileName);
        dataFile.setAbsolutePath(directory);
        dataFile.setCreationTime(new Date(creationTime));
        dataFile.setFileSize(size);
        dataFile.setLastModifiedTime(new Date(lastModifiedTime));

        String md5checkSum = MessageDigestHash.computeMD5Hash(file);
        DataFileInfo dataFileInfo = dataFile.getDataFileInfo();
        if (dataFileInfo == null) {
            dataFileInfo = new DataFileInfo();
        }
        dataFileInfo.setFileDelimiter(null);
        dataFileInfo.setMd5checkSum(md5checkSum);
        dataFileInfo.setNumOfColumns(null);
        dataFileInfo.setNumOfRows(null);
        dataFileInfo.setVariableType(null);

        dataFile.setDataFileInfo(dataFileInfo);
        dataFileService.saveDataFile(dataFile);

        return md5checkSum;
    }

    /**
     * Chunk upload, delete tmp chunks from data folder
     *
     * @param chunk
     * @param username
     * @return md5checkSum string
     * @throws IOException
     */
    public String mergeDeleteSave(ResumableChunkViaPost chunk, String username) throws IOException {
        String fileName = chunk.getResumableFilename();
        int numOfChunks = chunk.getResumableTotalChunks();
        String identifier = chunk.getResumableIdentifier();

        String workspaceDir = causalRestProperties.getWorkspaceDir();
        String dataFolder = causalRestProperties.getDataFolder();

        Path newFile = Paths.get(workspaceDir, username, dataFolder, fileName);
        Files.deleteIfExists(newFile); // delete the existing file
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile.toFile(), false))) {
            for (int chunkNumber = 1; chunkNumber <= numOfChunks; chunkNumber++) {
                Path chunkPath = Paths.get(workspaceDir, username, dataFolder, identifier, Integer.toString(chunkNumber));
                Files.copy(chunkPath, bos);
            }
        }

        // Save data info into database tables
        String md5checkSum = saveDataFile(newFile, username);

        try {
            deleteNonEmptyDir(Paths.get(workspaceDir, username, dataFolder, identifier));
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        return md5checkSum;
    }

    /**
     * Chunk upload, recursively delete subdirectories
     *
     * @param path
     * @throws IOException
     */
    private void deleteNonEmptyDir(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exception) throws IOException {
                if (exception == null) {
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                } else {
                    throw exception;
                }
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Summarize data file by adding fileDelimiter, variableType, numOfRows,
     * numOfColumns, and missingValue
     *
     * @param username
     * @param dataFileSummarization
     * @return The info of just summarized file
     * @throws IOException
     */
    public DataFileDTO summarizeDataFile(String username, DataFileSummarization dataFileSummarization) throws IOException {
        Long id = dataFileSummarization.getId();

        UserAccount userAccount = userAccountService.findByUsername(username);
        if (userAccount == null) {
            throw new UserNotFoundException(username);
        }

        DataFile dataFile = dataFileService.findByIdAndUserAccount(id, userAccount);
        if (dataFile == null) {
            throw new NotFoundByIdException(id);
        }

        DataFileInfo dataFileInfo = dataFile.getDataFileInfo();

        // Since we only get the string values from request
        // Here we'll need to convert the string values to FileDelimiter and VariableType objects
        FileDelimiter fileDelimiter = fileDelimiterService.getFileDelimiterRepository().findByName(dataFileSummarization.getFileDelimiter());
        VariableType variableType = variableTypeService.findByName(dataFileSummarization.getVariableType());

        // Set file delimiter and variable type
        dataFileInfo.setFileDelimiter(fileDelimiter);
        dataFileInfo.setVariableType(variableType);

        // Set the numbers of columns and rows based on the physical file
        char delimiter = FileInfos.delimiterNameToChar(dataFileSummarization.getFileDelimiter());
        Path file = Paths.get(dataFile.getAbsolutePath(), dataFile.getName());
        dataFileInfo.setNumOfRows(FileInfos.countLine(file.toFile()));
        dataFileInfo.setNumOfColumns(FileInfos.countColumn(file.toFile(), delimiter));

        // Ignore missing value here since it'll be removed from db in the new design
        // Update the dataFileInfo property
        dataFile.setDataFileInfo(dataFileInfo);

        // Update record in database table `data_file_info`
        dataFileService.saveDataFile(dataFile);

        // Create DTO to be used for HTTP response
        DataFileDTO dataFileDTO = new DataFileDTO();

        DataFileSummaryDTO dataFileSummaryDTO = new DataFileSummaryDTO();

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

        dataFileSummaryDTO.setMd5checkSum(dataFileInfo.getMd5checkSum());
        dataFileSummaryDTO.setNumOfColumns(dataFileInfo.getNumOfColumns());
        dataFileSummaryDTO.setNumOfRows(dataFileInfo.getNumOfRows());

        dataFileDTO.setCreationTime(dataFile.getCreationTime());
        dataFileDTO.setFileSize(dataFile.getFileSize());
        dataFileDTO.setId(dataFile.getId());
        dataFileDTO.setLastModifiedTime(dataFile.getLastModifiedTime());
        dataFileDTO.setName(dataFile.getName());
        dataFileDTO.setFileSummary(dataFileSummaryDTO);

        return dataFileDTO;
    }

    /*
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
     */
}
