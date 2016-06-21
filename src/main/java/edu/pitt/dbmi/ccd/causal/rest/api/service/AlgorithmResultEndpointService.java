/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.dbmi.ccd.causal.rest.api.service;

import edu.pitt.dbmi.ccd.causal.rest.api.prop.CausalRestProperties;
import edu.pitt.dbmi.ccd.commons.file.info.FileInfos;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author zhy19
 */
@Service
public class AlgorithmResultEndpointService {

    private final CausalRestProperties causalRestProperties;
    
    @Autowired
    public AlgorithmResultEndpointService(CausalRestProperties causalRestProperties) {
        this.causalRestProperties = causalRestProperties;
    }
    
    public List<Path> listAlgorithmResults(String username) throws IOException {
        String workspaceDir = causalRestProperties.getWorkspaceDir();
        String resultsFolder = causalRestProperties.getResultsFolder();
        String algorithmFolder = causalRestProperties.getAlgorithmFolder();
        Path algorithmDir = Paths.get(workspaceDir, username, resultsFolder, algorithmFolder);

        // Call listDirectory() from ccd/commons/file/info/FileInfos.java
        List<Path> files = FileInfos.listDirectory(algorithmDir, false);
        //System.out.println(files);
        return files;
    }
}
