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
package edu.pitt.dbmi.ccd.causal.rest.api.prop;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 *
 * Jun 10, 2016 2:44:29 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Component
@PropertySource("classpath:causal.properties")
public class CausalRestProperties {

    @Value("${ccd.dir.workspace}")
    private String workspaceDir;

    @Value("${ccd.folder.data}")
    private String dataFolder;
    
    @Value("${ccd.folder.results}")
    private String resultsFolder;

    @Value("${ccd.folder.results.algorithm}")
    private String algorithmFolder;

    public CausalRestProperties() {
    }

    public String getWorkspaceDir() {
        return workspaceDir;
    }

    public void setWorkspaceDir(String workspaceDir) {
        this.workspaceDir = workspaceDir;
    }

    public String getDataFolder() {
        return dataFolder;
    }
    
    public String getResultsFolder() {
        return resultsFolder;
    }
    
    public String getAlgorithmFolder() {
        return algorithmFolder;
    }

    public void setDataFolder(String dataFolder) {
        this.dataFolder = dataFolder;
    }
    
    public void setResultsFolder(String resultsFolder) {
        this.resultsFolder = resultsFolder;
    }
    
    public void setAlgorithmFolder(String algorithmFolder) {
        this.algorithmFolder = algorithmFolder;
    }

}
