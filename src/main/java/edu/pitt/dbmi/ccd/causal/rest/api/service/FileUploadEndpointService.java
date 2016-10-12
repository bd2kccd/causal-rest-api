/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.dbmi.ccd.causal.rest.api.service;

import edu.pitt.dbmi.ccd.causal.rest.api.dto.DatasetFileDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.DatasetFileSummaryDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.PriorKnowledgeFileDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.ResumableChunkViaGet;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.ResumableChunkViaPost;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.UserNotFoundException;
import edu.pitt.dbmi.ccd.causal.rest.api.prop.CausalRestProperties;
import edu.pitt.dbmi.ccd.commons.file.MessageDigestHash;
import edu.pitt.dbmi.ccd.commons.file.info.BasicFileInfo;
import edu.pitt.dbmi.ccd.commons.file.info.FileInfos;
import edu.pitt.dbmi.ccd.db.entity.DataFile;
import edu.pitt.dbmi.ccd.db.entity.DataFileInfo;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.DataFileService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
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
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Zhou Yuan (zhy19@pitt.edu)
 */
@Service
public class FileUploadEndpointService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadEndpointService.class);

    private final CausalRestProperties causalRestProperties;

    private final UserAccountService userAccountService;

    private final DataFileService dataFileService;

    @Autowired
    public FileUploadEndpointService(
            CausalRestProperties causalRestProperties,
            UserAccountService userAccountService,
            DataFileService dataFileService) {
        this.causalRestProperties = causalRestProperties;
        this.userAccountService = userAccountService;
        this.dataFileService = dataFileService;
    }

    /**
     * Small dataset file upload, not resumable
     *
     * @param uid
     * @param inputStream
     * @param fileDetail
     * @return Info of just uploaded file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public DatasetFileDTO uploadDatasetFile(Long uid, InputStream inputStream, FormDataContentDisposition fileDetail) throws FileNotFoundException, IOException {
        UserAccount userAccount = userAccountService.findById(uid);
        if (userAccount == null) {
            throw new UserNotFoundException(uid);
        }

        // Get the username since it's used as the data file folder name
        String username = userAccount.getUsername();

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
        DatasetFileDTO dataFileDTO = new DatasetFileDTO();

        // We should get the data from database since the new record has an ID
        // that can be used for later API calls
        // All other info can be obtained solely based on the file system but no ID
        DataFile newDataFile = dataFileService.findByAbsolutePathAndName(directory, fileName);

        DatasetFileSummaryDTO dataFileSummaryDTO = new DatasetFileSummaryDTO();

        dataFileSummaryDTO.setFileDelimiter(null);
        dataFileSummaryDTO.setVariableType(null);
        dataFileSummaryDTO.setNumOfColumns(null);
        dataFileSummaryDTO.setNumOfRows(null);

        dataFileDTO.setId(newDataFile.getId());
        dataFileDTO.setName(newDataFile.getName());
        dataFileDTO.setCreationTime(newDataFile.getCreationTime());
        dataFileDTO.setFileSize(newDataFile.getFileSize());
        dataFileDTO.setLastModifiedTime(newDataFile.getLastModifiedTime());
        dataFileDTO.setMd5checkSum(md5checkSum);
        dataFileDTO.setFileSummary(dataFileSummaryDTO);

        LOGGER.info(String.format("New dataset file '%s' (id=%d) has been uploaded successfully.", newDataFile.getName(), newDataFile.getId()));

        return dataFileDTO;
    }

    /**
     * Small prior knowledge file upload, not resumable
     *
     * @param uid
     * @param inputStream
     * @param fileDetail
     * @return Info of just uploaded file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public PriorKnowledgeFileDTO uploadPriorKnowledgeFile(Long uid, InputStream inputStream, FormDataContentDisposition fileDetail) throws FileNotFoundException, IOException {
        UserAccount userAccount = userAccountService.findById(uid);
        if (userAccount == null) {
            throw new UserNotFoundException(uid);
        }

        // Get the username since it's used as the data file folder name
        String username = userAccount.getUsername();

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
        PriorKnowledgeFileDTO priorKnowledgeFileDTO = new PriorKnowledgeFileDTO();

        // We should get the data from database since the new record has an ID
        // that can be used for later API calls
        // All other info can be obtained solely based on the file system but no ID
        DataFile newDataFile = dataFileService.findByAbsolutePathAndName(directory, fileName);

        priorKnowledgeFileDTO.setId(newDataFile.getId());
        priorKnowledgeFileDTO.setName(newDataFile.getName());
        priorKnowledgeFileDTO.setCreationTime(newDataFile.getCreationTime());
        priorKnowledgeFileDTO.setFileSize(newDataFile.getFileSize());
        priorKnowledgeFileDTO.setLastModifiedTime(newDataFile.getLastModifiedTime());

        LOGGER.info(String.format("New prior knowledge file '%s' (id=%d) has been uploaded successfully.", newDataFile.getName(), newDataFile.getId()));

        return priorKnowledgeFileDTO;
    }

    /**
     * Chunk upload, check chunk existence
     *
     * @param chunk
     * @param uid
     * @return true or false
     * @throws IOException
     */
    public boolean chunkExists(ResumableChunkViaGet chunk, Long uid) throws IOException {
        UserAccount userAccount = userAccountService.findById(uid);
        if (userAccount == null) {
            throw new UserNotFoundException(uid);
        }

        // Get the username since it's used as the data file folder name
        String username = userAccount.getUsername();

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
     * @param uid
     * @return md5checksum string
     * @throws IOException
     */
    public String uploadChunk(ResumableChunkViaPost chunk, Long uid) throws IOException {
        UserAccount userAccount = userAccountService.findById(uid);
        if (userAccount == null) {
            throw new UserNotFoundException(uid);
        }

        // Get the username since it's used as the data file folder name
        String username = userAccount.getUsername();

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
}
