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

    @Value("${ccd.jar.algorithm}")
    private String algorithmJar;

    @Value("${ccd.algorithm.fges.cont:FGESc}")
    private String algoFgesCont;

    @Value("${ccd.algorithm.fges.disc:FGESd}")
    private String algoFgesDisc;

    @Value("${ccd.algorithm.gfci.cont:GFCIc}")
    private String algoGfciCont;

    @Value("${ccd.algorithm.gfci.disc:GFCId}")
    private String algoGfciDisc;

    @Value("${ccd.server.workspace:}")
    private String workspaceDir;

    @Value("${ccd.folder.data:data}")
    private String dataFolder;

    @Value("${ccd.folder.lib:lib}")
    private String libFolder;

    @Value("${ccd.folder.tmp:tmp}")
    private String tmpFolder;

    @Value("${ccd.folder.results:results}")
    private String resultsFolder;

    @Value("${ccd.folder.results.algorithm:algorithm}")
    private String algorithmFolder;

    @Value("${ccd.folder.results.comparison:comparison}")
    private String comparisonFolder;

    @Value("${ccd.jwt.issuer}")
    private String jwtIssuer;

    @Value("${ccd.jwt.secret}")
    private String jwtSecret;

    @Value("${ccd.jwt.lifetime}")
    private long jwtLifetime;

    public CausalRestProperties() {
    }

    public String getAlgorithmJar() {
        return algorithmJar;
    }

    public void setAlgorithmJar(String algorithmJar) {
        this.algorithmJar = algorithmJar;
    }

    public String getAlgoFgesCont() {
        return algoFgesCont;
    }

    public void setAlgoFgesCont(String algoFgesCont) {
        this.algoFgesCont = algoFgesCont;
    }

    public String getAlgoFgesDisc() {
        return algoFgesDisc;
    }

    public void setAlgoFgesDisc(String algoFgesDisc) {
        this.algoFgesDisc = algoFgesDisc;
    }

    public String getAlgoGfciCont() {
        return algoGfciCont;
    }

    public void setAlgoGfciCont(String algoGfciCont) {
        this.algoGfciCont = algoGfciCont;
    }

    public String getAlgoGfciDisc() {
        return algoGfciDisc;
    }

    public void setAlgoGfciDisc(String algoGfciDisc) {
        this.algoGfciDisc = algoGfciDisc;
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

    public void setDataFolder(String dataFolder) {
        this.dataFolder = dataFolder;
    }

    public String getLibFolder() {
        return libFolder;
    }

    public void setLibFolder(String libFolder) {
        this.libFolder = libFolder;
    }

    public String getTmpFolder() {
        return tmpFolder;
    }

    public void setTmpFolder(String tmpFolder) {
        this.tmpFolder = tmpFolder;
    }

    public String getResultsFolder() {
        return resultsFolder;
    }

    public void setResultsFolder(String resultsFolder) {
        this.resultsFolder = resultsFolder;
    }

    public String getAlgorithmFolder() {
        return algorithmFolder;
    }

    public void setAlgorithmFolder(String algorithmFolder) {
        this.algorithmFolder = algorithmFolder;
    }

    public String getComparisonFolder() {
        return comparisonFolder;
    }

    public void setComparisonFolder(String comparisonFolder) {
        this.comparisonFolder = comparisonFolder;
    }

    public String getJwtIssuer() {
        return jwtIssuer;
    }

    public void setJwtIssuer(String jwtIssuer) {
        this.jwtIssuer = jwtIssuer;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public long getJwtLifetime() {
        return jwtLifetime;
    }

    public void setJwtLifetime(long jwtLifetime) {
        this.jwtLifetime = jwtLifetime;
    }

}
