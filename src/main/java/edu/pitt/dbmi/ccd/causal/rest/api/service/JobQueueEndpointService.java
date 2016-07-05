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

import edu.pitt.dbmi.ccd.causal.rest.api.dto.NewJob;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.NotFoundByIdException;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.UserNotFoundException;
import edu.pitt.dbmi.ccd.causal.rest.api.prop.CausalRestProperties;
import edu.pitt.dbmi.ccd.causal.rest.api.service.db.DataFileRestService;
import edu.pitt.dbmi.ccd.causal.rest.api.service.db.UserAccountRestService;
import edu.pitt.dbmi.ccd.db.entity.DataFile;
import edu.pitt.dbmi.ccd.db.entity.JobQueueInfo;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.JobQueueInfoService;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Zhou Yuan (zhy19@pitt.edu)
 */
@Service
public class JobQueueEndpointService {

    private final CausalRestProperties causalRestProperties;

    private final UserAccountRestService userAccountRestService;

    private final DataFileRestService dataFileRestService;

    private final JobQueueInfoService jobQueueInfoService;

    @Autowired
    public JobQueueEndpointService(
            CausalRestProperties causalRestProperties,
            UserAccountRestService userAccountRestService,
            DataFileRestService dataFileRestService,
            JobQueueInfoService jobQueueInfoService) {
        this.causalRestProperties = causalRestProperties;
        this.userAccountRestService = userAccountRestService;
        this.dataFileRestService = dataFileRestService;
        this.jobQueueInfoService = jobQueueInfoService;
    }

    public Long addNewJob(String username, NewJob newJob) {
        // algorithmName is prefixed in the result file name
        String algorithmName = newJob.getAlgorithmName();

        String algorithm = newJob.getAlgorithm();
        Long dataFileId = newJob.getDataFileId();
        // Not implimenting prior knowledge in API
        List<String> jvmOptions = newJob.getJvmOptions();
        List<String> parameters = newJob.getParameters();

        String workspaceDir = causalRestProperties.getWorkspaceDir();
        String libFolder = causalRestProperties.getLibFolder();
        String tmpFolder = causalRestProperties.getTmpFolder();
        String dataFolder = causalRestProperties.getDataFolder();
        String resultsFolder = causalRestProperties.getResultsFolder();
        String algorithmFolder = causalRestProperties.getAlgorithmFolder();

        String algorithmJar = causalRestProperties.getAlgorithmJar();

        Path userResultDir = Paths.get(workspaceDir, username, resultsFolder, algorithmFolder);
        Path userTmpDir = Paths.get(workspaceDir, username, tmpFolder);

        UserAccount userAccount = userAccountRestService.findByUsername(username);
        if (userAccount == null) {
            throw new UserNotFoundException(username);
        }

        // Building the command line
        List<String> commands = new LinkedList<>();
        commands.add("java");

        // add jvm options
        commands.addAll(jvmOptions);

        // add classpath
        Path classPath = Paths.get(workspaceDir, libFolder, algorithmJar);
        commands.add("-jar");
        commands.add(classPath.toString());

        // add algorithm
        commands.add("--algorithm");
        commands.add(algorithm);

        // add data file, get data file name by id
        DataFile dataFile = dataFileRestService.findByIdAndUserAccount(dataFileId, userAccount);
        if (dataFile == null) {
            throw new NotFoundByIdException(dataFileId);
        }

        Path dataPath = Paths.get(workspaceDir, username, dataFolder, dataFile.getName());

        // Add data file
        commands.add("--data");
        commands.add(dataPath.toAbsolutePath().toString());

        // add parameters
        commands.addAll(parameters);

        // don't create any validation files
        commands.add("--no-validation-output");

        long currentTime = System.currentTimeMillis();
        String fileName = String.format("%s_%s_%d", algorithmName, dataFile.getName(), currentTime);
        commands.add("--output-prefix");
        commands.add(fileName);

        String cmd = listToSeperatedValues(commands, ";");

        // Insert to database table `job_queue_info`
        JobQueueInfo jobQueueInfo = new JobQueueInfo();
        jobQueueInfo.setAddedTime(new Date(System.currentTimeMillis()));
        jobQueueInfo.setAlgorName(algorithmName);
        jobQueueInfo.setCommands(cmd);
        jobQueueInfo.setFileName(fileName);
        jobQueueInfo.setOutputDirectory(userResultDir.toAbsolutePath().toString());
        jobQueueInfo.setStatus(0);
        jobQueueInfo.setTmpDirectory(userTmpDir.toAbsolutePath().toString());
        jobQueueInfo.setUserAccounts(Collections.singleton(userAccount));

        jobQueueInfo = jobQueueInfoService.saveJobIntoQueue(jobQueueInfo);

        return jobQueueInfo.getId();
    }

    public String listToSeperatedValues(List<String> list, String delimiter) {
        StringBuilder sb = new StringBuilder();
        list.forEach(item -> {
            sb.append(item);
            sb.append(delimiter);
        });
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

}
