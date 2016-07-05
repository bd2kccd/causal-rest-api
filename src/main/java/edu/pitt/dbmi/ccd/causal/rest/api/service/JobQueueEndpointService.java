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
import edu.pitt.dbmi.ccd.causal.rest.api.exception.UserNotFoundException;
import edu.pitt.dbmi.ccd.causal.rest.api.prop.CausalRestProperties;
import edu.pitt.dbmi.ccd.causal.rest.api.service.db.UserAccountRestService;
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

    private final JobQueueInfoService jobQueueInfoService;

    @Autowired
    public JobQueueEndpointService(
            CausalRestProperties causalRestProperties,
            UserAccountRestService userAccountRestService,
            JobQueueInfoService jobQueueInfoService) {
        this.causalRestProperties = causalRestProperties;
        this.userAccountRestService = userAccountRestService;
        this.jobQueueInfoService = jobQueueInfoService;
    }

    public Long addNewJob(String username, NewJob newJob) {
        String algorithmName = newJob.getAlgorithmName();

        String algorithm = newJob.getAlgorithm();
        List<String> dataset = newJob.getDataset();
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

        // add dataset
        List<String> datasetPath = new LinkedList<>();
        dataset.forEach(dataFile -> {
            Path dataPath = Paths.get(workspaceDir, username, dataFolder, dataFile);
            datasetPath.add(dataPath.toAbsolutePath().toString());
        });
        String datasetList = listToSeperatedValues(datasetPath, ",");
        commands.add("--data");
        commands.add(datasetList);

        // add parameters
        commands.addAll(parameters);

        // don't create any validation files
        commands.add("--no-validation-output");

        long currentTime = System.currentTimeMillis();
        String fileName = (dataset.size() > 1)
                ? String.format("%s_%s_%d", algorithmName, "multi-dataset", currentTime)
                : String.format("%s_%s_%d", algorithmName, listToSeperatedValues(dataset, ","), currentTime);
        commands.add("--output-prefix");
        commands.add(fileName);

        String cmd = listToSeperatedValues(commands, ";");

        UserAccount userAccount = userAccountRestService.findByUsername(username);
        if (userAccount == null) {
            throw new UserNotFoundException(username);
        }

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
