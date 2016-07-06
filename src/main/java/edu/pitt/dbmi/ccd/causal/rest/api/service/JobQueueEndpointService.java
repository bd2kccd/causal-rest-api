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
import edu.pitt.dbmi.ccd.db.entity.DataFile;
import edu.pitt.dbmi.ccd.db.entity.JobQueueInfo;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.DataFileService;
import edu.pitt.dbmi.ccd.db.service.JobQueueInfoService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
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

    private final UserAccountService userAccountService;

    private final DataFileService dataFileService;

    private final JobQueueInfoService jobQueueInfoService;

    @Autowired
    public JobQueueEndpointService(
            CausalRestProperties causalRestProperties,
            UserAccountService userAccountService,
            DataFileService dataFileService,
            JobQueueInfoService jobQueueInfoService) {
        this.causalRestProperties = causalRestProperties;
        this.userAccountService = userAccountService;
        this.dataFileService = dataFileService;
        this.jobQueueInfoService = jobQueueInfoService;
    }

    /**
     * Add a new job to the job queue and run the algorithm
     *
     * @param username
     * @param newJob
     * @return Job ID
     */
    public Long addNewJob(String username, NewJob newJob) {
        // Right now, we only support "fgsc" and "fgsd"
        String algorithm = newJob.getAlgorithm();
        List<Long> dataFileIdList = newJob.getDataFileIdList();
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

        UserAccount userAccount = userAccountService.findByUsername(username);
        if (userAccount == null) {
            throw new UserNotFoundException(username);
        }

        // Building the command line
        List<String> commands = new LinkedList<>();
        commands.add("java");

        // Add jvm options
        commands.addAll(jvmOptions);

        // Add classpath
        Path classPath = Paths.get(workspaceDir, libFolder, algorithmJar);
        commands.add("-jar");
        commands.add(classPath.toString());

        // Add algorithm
        commands.add("--algorithm");
        commands.add(algorithm);

        // Add dataset
        List<String> datasetPath = new LinkedList<>();
        dataFileIdList.forEach(dataFileId -> {
            // Get data file name by file id
            DataFile dataFile = dataFileService.findByIdAndUserAccount(dataFileId, userAccount);
            Path dataPath = Paths.get(workspaceDir, username, dataFolder, dataFile.getName());
            datasetPath.add(dataPath.toAbsolutePath().toString());
        });
        String datasetList = listToSeparatedValues(datasetPath, ",");
        commands.add("--data");
        commands.add(datasetList);

        // Add parameters
        commands.addAll(parameters);

        // Don't create any validation files
        commands.add("--no-validation-output");

        long currentTime = System.currentTimeMillis();
        // Algorithm result file name
        String fileName;

        if (dataFileIdList.size() > 1) {
            // FGS Image takes multi image files
            fileName = String.format("%s_%s_%d", algorithm, "multi-dataset", currentTime);
        } else {
            Long id = dataFileIdList.get(0);
            DataFile df = dataFileService.findByIdAndUserAccount(id, userAccount);
            fileName = String.format("%s_%s_%d", algorithm, df.getName(), currentTime);
        }

        commands.add("--output-prefix");
        commands.add(fileName);

        // Then separate those commands with ; and store the whole string into database
        // ccd-job-queue will assemble the command line again at
        // https://github.com/bd2kccd/ccd-job-queue/blob/master/src/main/java/edu/pitt/dbmi/ccd/queue/service/AlgorithmQueueService.java#L79
        String cmd = listToSeparatedValues(commands, ";");

        // Insert to database table `job_queue_info`
        JobQueueInfo jobQueueInfo = new JobQueueInfo();
        jobQueueInfo.setAddedTime(new Date(System.currentTimeMillis()));
        jobQueueInfo.setAlgorName(algorithm);
        jobQueueInfo.setCommands(cmd);
        jobQueueInfo.setFileName(fileName);
        jobQueueInfo.setOutputDirectory(userResultDir.toAbsolutePath().toString());
        jobQueueInfo.setStatus(0);
        jobQueueInfo.setTmpDirectory(userTmpDir.toAbsolutePath().toString());
        jobQueueInfo.setUserAccounts(Collections.singleton(userAccount));

        jobQueueInfo = jobQueueInfoService.saveJobIntoQueue(jobQueueInfo);

        return jobQueueInfo.getId();
    }

    /**
     * Record added to table `job_queue_info` when new job added and the record
     * will be gone once the job is done
     *
     * @param id
     * @return true on completed or false on running
     */
    public boolean checkJobStatus(Long id) {
        JobQueueInfo jobQueueInfo = jobQueueInfoService.findOne(id);
        // As long as there's database record, the job is pending
        return (jobQueueInfo == null);
    }

    /**
     * Convert a string list into a delimiter separated string
     *
     * @param list
     * @param delimiter
     * @return A delimiter separated string
     */
    private String listToSeparatedValues(List<String> list, String delimiter) {
        StringBuilder sb = new StringBuilder();
        list.forEach(item -> {
            sb.append(item);
            sb.append(delimiter);
        });
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

}
