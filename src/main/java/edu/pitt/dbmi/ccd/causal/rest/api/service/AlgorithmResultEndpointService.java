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

import edu.pitt.dbmi.ccd.causal.rest.api.dto.AlgorithmResultDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.causal.rest.api.prop.CausalRestProperties;
import edu.pitt.dbmi.ccd.commons.file.info.BasicFileInfo;
import edu.pitt.dbmi.ccd.commons.file.info.FileInfos;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Zhou Yuan (zhy19@pitt.edu)
 */
@Service
public class AlgorithmResultEndpointService {

    private final CausalRestProperties causalRestProperties;

    @Autowired
    public AlgorithmResultEndpointService(CausalRestProperties causalRestProperties) {
        this.causalRestProperties = causalRestProperties;
    }

    public List<AlgorithmResultDTO> listAlgorithmResults(String username) throws IOException {
        String workspaceDir = causalRestProperties.getWorkspaceDir();
        String resultsFolder = causalRestProperties.getResultsFolder();
        String algorithmFolder = causalRestProperties.getAlgorithmFolder();
        Path algorithmDir = Paths.get(workspaceDir, username, resultsFolder, algorithmFolder);

        List<AlgorithmResultDTO> algorithmResultDTOs = new LinkedList<>();

        List<Path> algorithmResultFiles = FileInfos.listDirectory(algorithmDir, false);

        for (Path algorithmResultFile : algorithmResultFiles) {
            // Create DTO for each file
            AlgorithmResultDTO algorithmResultDTO = new AlgorithmResultDTO();
            // Get file information of each path
            BasicFileInfo fileInfo = FileInfos.basicPathInfo(algorithmResultFile);

            // In ccd-commons, BasicFileInfo.getCreationTime() and BasicFileInfo.getLastModifiedTime()
            // return long type instead of Date, that's why we defined creationTime and lastModifiedTime as long
            // in AlgorithmResultDTO.java
            algorithmResultDTO.setCreationTime(fileInfo.getCreationTime());
            algorithmResultDTO.setFileSize(fileInfo.getSize());
            algorithmResultDTO.setLastModifiedTime(fileInfo.getLastModifiedTime());
            algorithmResultDTO.setName(fileInfo.getFilename());

            algorithmResultDTOs.add(algorithmResultDTO);
        }

        return algorithmResultDTOs;
    }

    public File getAlgorithmResultFile(String username, String fileName) {
        String workspaceDir = causalRestProperties.getWorkspaceDir();
        String resultsFolder = causalRestProperties.getResultsFolder();
        String algorithmFolder = causalRestProperties.getAlgorithmFolder();
        Path resultFile = Paths.get(workspaceDir, username, resultsFolder, algorithmFolder, fileName);

        File file = new File(resultFile.toString());

        if (file.exists() && !file.isDirectory()) {
            return file;
        } else {
            throw new ResourceNotFoundException();
        }
    }
}
