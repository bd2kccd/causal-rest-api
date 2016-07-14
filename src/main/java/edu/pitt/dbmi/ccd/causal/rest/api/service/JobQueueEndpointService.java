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

import edu.pitt.dbmi.ccd.causal.rest.api.dto.FgsContinuousNewJob;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.FgsDiscreteNewJob;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.JobInfoDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.NotFoundByIdException;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.UserNotFoundException;
import edu.pitt.dbmi.ccd.causal.rest.api.prop.CausalRestProperties;
import edu.pitt.dbmi.ccd.db.entity.DataFile;
import edu.pitt.dbmi.ccd.db.entity.DataFileInfo;
import edu.pitt.dbmi.ccd.db.entity.FileDelimiter;
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
     * Add a new job to the job queue and run the FGS Discrete algorithm
     *
     * @param username
     * @param newJob
     * @return Job ID
     */
    public Long addFgsDiscreteNewJob(String username, FgsDiscreteNewJob newJob) {
        // Right now, we only support "fgs" and "fgs-discrete"
        String algorithm = "fgs-discrete";
        Long dataFileId = newJob.getDataFileId();
        // Not implimenting prior knowledge in API
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

        // Add classpath
        Path classPath = Paths.get(workspaceDir, libFolder, algorithmJar);
        commands.add("-jar");
        commands.add(classPath.toString());

        // Add algorithm
        commands.add("--algorithm");
        commands.add(algorithm);

        // Get data file name by file id
        DataFile dataFile = dataFileService.findByIdAndUserAccount(dataFileId, userAccount);
        if (dataFile == null) {
            throw new NotFoundByIdException(dataFileId);
        }
        Path dataPath = Paths.get(workspaceDir, username, dataFolder, dataFile.getName());

        commands.add("--data");
        commands.add(dataPath.toAbsolutePath().toString());

        // Parameters
        commands.add("--delimiter");
        commands.add(getFileDelimiter(newJob.getDataFileId()));

        commands.add("--structure-prior");
        commands.add(Double.toString(newJob.getStructurePrior()));

        commands.add("--sample-prior");
        commands.add(Double.toString(newJob.getSamplePrior()));

        commands.add("--depth");
        commands.add(Integer.toString(newJob.getDepth()));

        if (newJob.isVerbose()) {
            commands.add("--verbose");
        }

        if (!newJob.isHeuristicSpeedup()) {
            commands.add("--disable-heuristic-speedup");
        }

        // Data validation
        if (!newJob.isLimitNumOfCategory()) {
            commands.add("--skip-category-limit");
        }

        if (!newJob.isNonZeroVarianceValidation()) {
            commands.add("--skip-non-zero-variance");
        }

        if (!newJob.isUniqueVarNameValidation()) {
            commands.add("--skip-unique-var-name");
        }

        // Don't create any validation files
        commands.add("--no-validation-output");

        long currentTime = System.currentTimeMillis();
        // Algorithm result file name
        String fileName;

        DataFile df = dataFileService.findByIdAndUserAccount(dataFileId, userAccount);
        fileName = String.format("%s_%s_%d", algorithm, df.getName(), currentTime);

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
     * Add a new job to the job queue and run the FGS Continuous algorithm
     *
     * @param username
     * @param newJob
     * @return
     */
    public Long addFgsContinuousNewJob(String username, FgsContinuousNewJob newJob) {
        // Right now, we only support "fgs" and "fgs-discrete"
        String algorithm = "fgs";
        Long dataFileId = newJob.getDataFileId();

        // Not implimenting prior knowledge in API
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

        // Add classpath
        Path classPath = Paths.get(workspaceDir, libFolder, algorithmJar);
        commands.add("-jar");
        commands.add(classPath.toString());

        // Add algorithm
        commands.add("--algorithm");
        commands.add(algorithm);

        // Get data file name by file id
        DataFile dataFile = dataFileService.findByIdAndUserAccount(dataFileId, userAccount);
        if (dataFile == null) {
            throw new NotFoundByIdException(dataFileId);
        }
        Path dataPath = Paths.get(workspaceDir, username, dataFolder, dataFile.getName());

        commands.add("--data");
        commands.add(dataPath.toAbsolutePath().toString());

        // Parameters
        commands.add("--delimiter");
        commands.add(getFileDelimiter(newJob.getDataFileId()));

        commands.add("--penalty-discount");
        commands.add(Double.toString(newJob.getPenaltyDiscount()));

        commands.add("--depth");
        commands.add(Integer.toString(newJob.getDepth()));

        if (newJob.isVerbose()) {
            commands.add("--verbose");
        }

        if (!newJob.isHeuristicSpeedup()) {
            commands.add("--disable-heuristic-speedup");
        }

        if (newJob.isIgnoreLinearDependence()) {
            commands.add("--ignore-linear-dependence");
        }

        // Data validation
        if (!newJob.isNonZeroVarianceValidation()) {
            commands.add("--skip-non-zero-variance");
        }

        if (!newJob.isUniqueVarNameValidation()) {
            commands.add("--skip-unique-var-name");
        }

        // Don't create any validation files
        commands.add("--no-validation-output");

        long currentTime = System.currentTimeMillis();
        // Algorithm result file name
        String fileName;

        DataFile df = dataFileService.findByIdAndUserAccount(dataFileId, userAccount);
        fileName = String.format("%s_%s_%d", algorithm, df.getName(), currentTime);

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
     * List all Queued or Running jobs of a certain user
     *
     * @param username
     * @return
     */
    public List<JobInfoDTO> listAllJobs(String username) {
        List<JobInfoDTO> jobInfoDTOs = new LinkedList<>();

        UserAccount userAccount = userAccountService.findByUsername(username);
        if (userAccount == null) {
            throw new UserNotFoundException(username);
        }

        List<JobQueueInfo> jobs = jobQueueInfoService.findByUserAccounts(Collections.singleton(userAccount));
        jobs.forEach(job -> {
            JobInfoDTO jobInfoDTO = new JobInfoDTO();

            jobInfoDTO.setId(job.getId());
            jobInfoDTO.setAlgorithmName(job.getAlgorName());
            // This data file name is the origional file name
            // E.g. fgs_Lung-tetrad_hv.txt_1468499246916
            jobInfoDTO.setDataFileName(job.getFileName());
            jobInfoDTO.setAddedTime(job.getAddedTime());

            jobInfoDTOs.add(jobInfoDTO);
        });

        return jobInfoDTOs;
    }

    /**
     * Record added to table `job_queue_info` when new job added and the record
     * will be gone once the job is done
     *
     * Do we really care if a user can see the status of jobs created by others?
     *
     * @param username
     * @param id
     * @return true on completed or false on running
     */
    public boolean checkJobStatus(String username, Long id) {
        JobQueueInfo jobQueueInfo = jobQueueInfoService.findOne(id);
        // As long as there's database record, the job is pending
        return (jobQueueInfo == null);

        // We should also check to see if the result file exists
        // Since users may recheck the status of a canceld job,
        // and it will say "Completed" just by checking the database record
    }

    /**
     * Cancel a running job
     *
     * We'll need to make sure this job is created by this user
     *
     * @param username
     * @param id
     * @return true on canceled or false if job is already completed
     */
    public boolean cancelJob(String username, Long id) {
        UserAccount userAccount = userAccountService.findByUsername(username);
        if (userAccount == null) {
            throw new UserNotFoundException(username);
        }

        JobQueueInfo job = jobQueueInfoService.findByIdAndUseraccount(id, userAccount);
        // If can't find the job id from database, it's already completed
        // Then we are unable to cancel the job
        if (job == null) {
            return false;
        }
        // Set status to 2 in database so the job queue knows it's a flag to kill the job
        job.setStatus(2);
        jobQueueInfoService.saveJobIntoQueue(job);

        return true;
    }

    /**
     * Get file delimiter for a given data file ID
     *
     * @param id
     * @return
     */
    private String getFileDelimiter(Long id) {
        String delimiter = null;

        DataFile dataFile = dataFileService.findById(id);
        if (dataFile != null) {
            DataFileInfo dataFileInfo = dataFile.getDataFileInfo();
            if (dataFileInfo != null) {
                FileDelimiter fileDelimiter = dataFileInfo.getFileDelimiter();
                if (fileDelimiter != null) {
                    delimiter = fileDelimiter.getName();
                }
            }
        }

        return delimiter;
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
