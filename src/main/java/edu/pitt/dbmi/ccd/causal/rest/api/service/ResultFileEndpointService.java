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

import edu.pitt.dbmi.ccd.causal.rest.api.dto.ResultComparison;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.ResultComparisonData;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.ResultComparisonFileDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.ResultFileDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.causal.rest.api.prop.CausalRestProperties;
import edu.pitt.dbmi.ccd.commons.file.info.BasicFileInfo;
import edu.pitt.dbmi.ccd.commons.file.info.FileInfos;
import edu.pitt.dbmi.ccd.commons.graph.SimpleGraph;
import edu.pitt.dbmi.ccd.commons.graph.SimpleGraphComparison;
import edu.pitt.dbmi.ccd.commons.graph.SimpleGraphUtil;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Zhou Yuan (zhy19@pitt.edu)
 */
@Service
public class ResultFileEndpointService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultFileEndpointService.class);

    private final CausalRestProperties causalRestProperties;

    private final UserAccountService userAccountService;

    @Autowired
    public ResultFileEndpointService(CausalRestProperties causalRestProperties, UserAccountService userAccountService) {
        this.causalRestProperties = causalRestProperties;
        this.userAccountService = userAccountService;
    }

    /**
     * List all the algorithm result files for a given user
     *
     * @param uid
     * @return A list of result files
     * @throws IOException
     */
    public List<ResultFileDTO> listAlgorithmResults(Long uid) throws IOException {
        // When we can get here vai AuthFilterSerice, it means the user exists
        // so no need to check if (userAccount == null) and throw UserNotFoundException(uid)
        UserAccount userAccount = userAccountService.findById(uid);

        String username = userAccount.getUsername();

        String workspaceDir = causalRestProperties.getWorkspaceDir();
        String resultsFolder = causalRestProperties.getResultsFolder();
        String algorithmFolder = causalRestProperties.getAlgorithmFolder();
        Path algorithmDir = Paths.get(workspaceDir, username, resultsFolder, algorithmFolder);

        List<ResultFileDTO> algorithmResultDTOs = new LinkedList<>();

        List<Path> algorithmResultFiles = FileInfos.listDirectory(algorithmDir, false);

        for (Path algorithmResultFile : algorithmResultFiles) {
            // Create DTO for each file
            ResultFileDTO algorithmResultDTO = new ResultFileDTO();
            // Get file information of each path
            BasicFileInfo fileInfo = FileInfos.basicPathInfo(algorithmResultFile);

            // In ccd-commons, BasicFileInfo.getCreationTime() and BasicFileInfo.getLastModifiedTime()
            // return long type instead of Date, that's why we defined creationTime and lastModifiedTime as long
            // in ResultFileDTO.java
            algorithmResultDTO.setCreationTime(fileInfo.getCreationTime());
            algorithmResultDTO.setFileSize(fileInfo.getSize());
            algorithmResultDTO.setLastModifiedTime(fileInfo.getLastModifiedTime());
            algorithmResultDTO.setName(fileInfo.getFilename());

            algorithmResultDTOs.add(algorithmResultDTO);
        }

        return algorithmResultDTOs;
    }

    /**
     * Get the result file content based on user and the file name
     *
     * @param uid
     * @param fileName
     * @return The algorithm result file
     */
    public File getAlgorithmResultFile(Long uid, String fileName) {
        // When we can get here vai AuthFilterSerice, it means the user exists
        // so no need to check if (userAccount == null) and throw UserNotFoundException(uid)
        UserAccount userAccount = userAccountService.findById(uid);

        String username = userAccount.getUsername();

        String workspaceDir = causalRestProperties.getWorkspaceDir();
        String resultsFolder = causalRestProperties.getResultsFolder();
        String algorithmFolder = causalRestProperties.getAlgorithmFolder();
        Path resultFile = Paths.get(workspaceDir, username, resultsFolder, algorithmFolder, fileName);

        File file = new File(resultFile.toString());

        if (file.exists() && !file.isDirectory()) {
            return file;
        } else {
            throw new ResourceNotFoundException(String.format("Algorithm result file %s does not exist.", fileName));
        }
    }

    /**
     * List all the algorithm results comparison files for a given user
     *
     * @param uid
     * @return A list of result comparison files
     * @throws IOException
     */
    public List<ResultFileDTO> listAlgorithmResultComparisons(Long uid) throws IOException {
        // When we can get here vai AuthFilterSerice, it means the user exists
        // so no need to check if (userAccount == null) and throw UserNotFoundException(uid)
        UserAccount userAccount = userAccountService.findById(uid);

        String username = userAccount.getUsername();

        String workspaceDir = causalRestProperties.getWorkspaceDir();
        String resultsFolder = causalRestProperties.getResultsFolder();
        String comparisonFolder = causalRestProperties.getComparisonFolder();
        Path comparisonDir = Paths.get(workspaceDir, username, resultsFolder, comparisonFolder);

        List<ResultFileDTO> algorithmResultDTOs = new LinkedList<>();

        List<Path> comparisonFiles = FileInfos.listDirectory(comparisonDir, false);

        for (Path comparisonFile : comparisonFiles) {
            // Create DTO for each file
            ResultFileDTO algorithmResultDTO = new ResultFileDTO();
            // Get file information of each path
            BasicFileInfo fileInfo = FileInfos.basicPathInfo(comparisonFile);

            // In ccd-commons, BasicFileInfo.getCreationTime() and BasicFileInfo.getLastModifiedTime()
            // return long type instead of Date, that's why we defined creationTime and lastModifiedTime as long
            // in ResultFileDTO.java
            algorithmResultDTO.setCreationTime(fileInfo.getCreationTime());
            algorithmResultDTO.setFileSize(fileInfo.getSize());
            algorithmResultDTO.setLastModifiedTime(fileInfo.getLastModifiedTime());
            algorithmResultDTO.setName(fileInfo.getFilename());

            algorithmResultDTOs.add(algorithmResultDTO);
        }

        return algorithmResultDTOs;
    }

    /**
     * Get the result comparison file content based on user and the file name
     *
     * @param uid
     * @param fileName
     * @return The comparison file
     */
    public File getAlgorithmResultsComparisonFile(Long uid, String fileName) {
        // When we can get here vai AuthFilterSerice, it means the user exists
        // so no need to check if (userAccount == null) and throw UserNotFoundException(uid)
        UserAccount userAccount = userAccountService.findById(uid);

        String username = userAccount.getUsername();

        String workspaceDir = causalRestProperties.getWorkspaceDir();
        String resultsFolder = causalRestProperties.getResultsFolder();
        String comparisonFolder = causalRestProperties.getComparisonFolder();
        Path comparisonFile = Paths.get(workspaceDir, username, resultsFolder, comparisonFolder, fileName);

        File file = new File(comparisonFile.toString());

        if (file.exists() && !file.isDirectory()) {
            return file;
        } else {
            throw new ResourceNotFoundException(String.format("Algorithm results comparison file %s does not exist.", fileName));
        }
    }

    /**
     * Handles the comparison of multi result files
     *
     * @param uid
     * @param fileNames
     * @return Comparison result
     */
    public ResultComparisonFileDTO compareAlgorithmResults(Long uid, String fileNames) {
        // When we can get here vai AuthFilterSerice, it means the user exists
        // so no need to check if (userAccount == null) and throw UserNotFoundException(uid)
        UserAccount userAccount = userAccountService.findById(uid);

        String username = userAccount.getUsername();

        String workspaceDir = causalRestProperties.getWorkspaceDir();
        String resultsFolder = causalRestProperties.getResultsFolder();
        String algorithmFolder = causalRestProperties.getAlgorithmFolder();
        String comparisonFolder = causalRestProperties.getComparisonFolder();

        // Split the concatenated file names
        List<String> items = Arrays.asList(fileNames.split("!!"));

        List<SimpleGraph> graphs = new LinkedList<>();
        items.forEach(fileName -> {
            Path file = Paths.get(workspaceDir, username, resultsFolder, algorithmFolder, fileName);
            if (Files.exists(file)) {
                try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
                    graphs.add(SimpleGraphUtil.readInSimpleGraph(reader));
                } catch (IOException exception) {
                    LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
                }
            }
        });

        SimpleGraphComparison simpleGraphComparison = new SimpleGraphComparison();
        simpleGraphComparison.compare(graphs);

        Set<String> distinctEdges = simpleGraphComparison.getDistinctEdges();
        Set<String> edgesInAll = simpleGraphComparison.getEdgesInAll();
        Set<String> sameEdgeTypes = simpleGraphComparison.getSameEdgeTypes();

        String resultFileName = "result_comparison_" + System.currentTimeMillis() + ".txt";

        ResultComparison resultComparison = new ResultComparison(resultFileName);
        resultComparison.getFileNames().addAll(items);

        List<ResultComparisonData> comparisonResults = resultComparison.getComparisonData();
        int countIndex = 0;
        for (String edge : distinctEdges) {
            ResultComparisonData rc = new ResultComparisonData(edge);
            rc.setInAll(edgesInAll.contains(edge));
            rc.setSameEdgeType(sameEdgeTypes.contains(edge));
            rc.setCountIndex(++countIndex);

            comparisonResults.add(rc);
        }

        Path file = Paths.get(workspaceDir, username, resultsFolder, comparisonFolder, resultFileName);
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardOpenOption.CREATE)) {
            StringBuilder sb = new StringBuilder();
            items.forEach(fileName -> {
                sb.append(fileName);
                sb.append("\t");
            });
            // Write each file name on top
            writer.write(sb.toString().trim());
            writer.write("\n");

            // Write table headers
            writer.write(String.format("%s\t%s\t%s\n",
                    "Edges",
                    "In All",
                    "Same End Point"));

            List<ResultComparisonData> comparisonData = resultComparison.getComparisonData();
            for (ResultComparisonData comparison : comparisonData) {
                writer.write(String.format("%s\t%s\t%s\n",
                        comparison.getEdge(),
                        comparison.isInAll() ? "1" : "0",
                        comparison.isSameEdgeType() ? "1" : "0"));
            }
            LOGGER.info(String.format("Result file '%s' has been created.", resultFileName));
        } catch (IOException exception) {
            LOGGER.error(String.format("Unable to write file '%s'.", resultFileName), exception);
        }

        // For response, this DTO contains the result file name and actual file content
        ResultComparisonFileDTO resultComparisonFileDTO = new ResultComparisonFileDTO();

        File resultComparisonFile = new File(file.toString());

        resultComparisonFileDTO.setFileName(resultFileName);
        resultComparisonFileDTO.setFile(resultComparisonFile);

        return resultComparisonFileDTO;
    }

}
