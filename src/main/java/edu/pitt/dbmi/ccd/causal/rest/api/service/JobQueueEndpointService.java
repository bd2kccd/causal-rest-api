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

import edu.pitt.dbmi.ccd.causal.rest.api.dto.AlgoParameter;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.JobInfoDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.JvmOptions;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.NewJob;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.NotFoundByIdException;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.causal.rest.api.prop.CausalRestProperties;
import edu.pitt.dbmi.ccd.causal.rest.api.util.CmdOptions;
import edu.pitt.dbmi.ccd.db.entity.DataFile;
import edu.pitt.dbmi.ccd.db.entity.DataFileInfo;
import edu.pitt.dbmi.ccd.db.entity.FileDelimiter;
import edu.pitt.dbmi.ccd.db.entity.HpcParameter;
import edu.pitt.dbmi.ccd.db.entity.JobQueueInfo;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.DataFileService;
import edu.pitt.dbmi.ccd.db.service.JobQueueInfoService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 *
 * @author Zhou Yuan (zhy19@pitt.edu)
 * @author Chirayu (Kong) Wongchokprasitti, PhD (chw20@pitt.edu)
 */
@Service
public class JobQueueEndpointService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobQueueEndpointService.class);

    private final CausalRestProperties causalRestProperties;

    private final UserAccountService userAccountService;

    private final DataFileService dataFileService;

    private final JobQueueInfoService jobQueueInfoService;

    @Autowired
    private Environment env;

    @Autowired
    @Value("${ccd.remote.server.dataspace:}")
    private String remotedataspace;

    @Autowired
    @Value("${ccd.remote.server.workspace:}")
    private String remoteworkspace;

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
    
    public JobInfoDTO addNewJob(Long uid, NewJob newJob) {
        // When we can get here vai AuthFilterSerice, it means the user exists
        // so no need to check if (userAccount == null) and throw UserNotFoundException(uid)
        UserAccount userAccount = userAccountService.findById(uid);

        // Get algorithm ID string
        String algoId = newJob.getAlgoId();
                
        // algorithmJarPath, dataDir, tmpDir, resultDir
        // will be used in all algorithms
        Map<String, String> map = createSharedMapping(uid);

        // Building the command line
        List<String> commands = new LinkedList<>();

        // The first command
        commands.add("java");

        // Add JVM options
        if (newJob.getJvmOptions() != null) {
            JvmOptions jvmOptions = newJob.getJvmOptions();
            commands.add(String.format("-Xmx%dG", jvmOptions.getMaxHeapSize()));
        }

        // Add causal-cmd jar path
        commands.add("-jar");
        commands.add(map.get("algorithmJarPath"));

        // Add algorithm
        commands.add("--algorithm");
        commands.add(algoId);

        // Get dataset file name by file id
        Long datasetFileId = newJob.getDatasetFileId();
        DataFile datasetFile = dataFileService.findByIdAndUserAccount(datasetFileId, userAccount);
        if (datasetFile == null) {
            throw new NotFoundByIdException(datasetFileId);
        }
        
        // Specify data type
        commands.add(CmdOptions.DATATYPE);
        commands.add(datasetFile.getDataFileInfo().getVariableType().getName());
        
        // Specify dataset file path
        Path datasetPath = Paths.get(map.get("dataDir"), datasetFile.getName());

        commands.add(CmdOptions.DATASET);
        commands.add(datasetPath.toAbsolutePath().toString());

        // Add prior knowloedge file (optional)
        if (newJob.getPriorKnowledgeFileId() != null) {
            // Get prior knowledge file name by file id
            Long priorKnowledgeFileId = newJob.getPriorKnowledgeFileId();
            DataFile priorKnowledgeFile = dataFileService.findByIdAndUserAccount(priorKnowledgeFileId, userAccount);
            if (priorKnowledgeFile == null) {
                throw new NotFoundByIdException(priorKnowledgeFileId);
            }
            Path priorKnowledgePath = Paths.get(map.get("dataDir"), priorKnowledgeFile.getName());

            commands.add(CmdOptions.KNOWLEDGE);
            commands.add(priorKnowledgePath.toAbsolutePath().toString());
        }

        
        // Set delimiter
        commands.add(CmdOptions.DELIMITER);
        commands.add(getFileDelimiter(newJob.getDatasetFileId()));

        // Create tetrad graph json for HPC?
        commands.add(CmdOptions.JSON);
        
        // Algorithm parameters
        Set<AlgoParameter> algorithmParameters = newJob.getAlgoParameters();
        // Get key-value from algo parameters
        algorithmParameters.forEach(param -> {
            commands.add("--" + param.getKey());
            commands.add(param.getValue().toString());
        });

        long currentTime = System.currentTimeMillis();
        // Algorithm result file name
        String fileName;

        DataFile df = dataFileService.findByIdAndUserAccount(datasetFileId, userAccount);
        // The algorithm name can be different from the value of causalRestProperties.getAlgoFgesCont()
        fileName = String.format("%s_%s_%d", algoId, df.getName(), currentTime);

        // Output file name prefix
        commands.add(CmdOptions.OUTPUT_PREFIX);
        commands.add(fileName);

        // Skip data validation?
        if (newJob.getSkipDataValidation()) {
            commands.add(CmdOptions.SKIP_VALIDATION);
        } 
        
        // Then separate those commands with ; and store the whole string into database
        // ccd-job-queue will assemble the command line again at
        // https://github.com/bd2kccd/ccd-job-queue/blob/master/src/main/java/edu/pitt/dbmi/ccd/queue/service/AlgorithmQueueService.java#L79
        String cmd = listToSeparatedValues(commands, ";");

        // Insert to database table `job_queue_info`
        JobQueueInfo jobQueueInfo = new JobQueueInfo();
        jobQueueInfo.setAddedTime(new Date(System.currentTimeMillis()));
        jobQueueInfo.setAlgorName(algoId);
        jobQueueInfo.setCommands(cmd);
        jobQueueInfo.setFileName(fileName);
        jobQueueInfo.setOutputDirectory(map.get("resultDir"));
        jobQueueInfo.setStatus(0);
        jobQueueInfo.setTmpDirectory(map.get("tmpDir"));
        jobQueueInfo.setUserAccounts(Collections.singleton(userAccount));

        // Hpc Parameters
        if (newJob.getHpcParameters() != null) {
            Set<HpcParameter> hpcParameters = new HashSet<>();
            newJob.getHpcParameters().forEach(param -> {
                HpcParameter hpcParameter = new HpcParameter();
                hpcParameter.setParameterKey(param.getKey());
                hpcParameter.setParameterValue(param.getValue());
                hpcParameters.add(hpcParameter);
            });
            jobQueueInfo.setHpcParameters(hpcParameters);
        }

        jobQueueInfo = jobQueueInfoService.saveJobIntoQueue(jobQueueInfo);

        Long newJobId = jobQueueInfo.getId();

        LOGGER.info(String.format("New job submitted. Job ID: %d", newJobId));

        String resultJsonFileName = fileName + ".json";
        fileName = fileName + ".txt";
        String errorFileName = String.format("error_%s", fileName);

        JobInfoDTO jobInfo = new JobInfoDTO();
        jobInfo.setStatus(0);
        jobInfo.setAddedTime(jobQueueInfo.getAddedTime());
        jobInfo.setAlgorithmName(algoId);
        jobInfo.setResultFileName(fileName);
        jobInfo.setResultJsonFileName(resultJsonFileName);
        jobInfo.setErrorResultFileName(errorFileName);
        jobInfo.setId(jobQueueInfo.getId());

        return jobInfo;
    }

    /**
     * Shared values to be used in both algorithms
     *
     * @param uid
     * @return key-value mapping
     */
    private Map<String, String> createSharedMapping(Long uid) {
        // When we can get here vai AuthFilterSerice, it means the user exists
        // so no need to check if (userAccount == null) and throw UserNotFoundException(uid)
        UserAccount userAccount = userAccountService.findById(uid);

        // Get the username
        String username = userAccount.getUsername();

        Map<String, String> map = new HashMap<>();

        String workspaceDir = causalRestProperties.getWorkspaceDir();
        String libFolder = causalRestProperties.getLibFolder();
        String tmpFolder = causalRestProperties.getTmpFolder();
        String dataFolder = causalRestProperties.getDataFolder();
        String resultsFolder = causalRestProperties.getResultsFolder();
        String algorithmFolder = causalRestProperties.getAlgorithmFolder();
        String algorithmJar = causalRestProperties.getAlgorithmJar();

        Path algorithmJarPath = Paths.get(workspaceDir, libFolder, algorithmJar);
        Path dataDir = Paths.get(workspaceDir, username, dataFolder);
        Path tmpDir = Paths.get(workspaceDir, username, tmpFolder);
        Path resultDir = Paths.get(workspaceDir, username, resultsFolder, algorithmFolder);

        if (env.acceptsProfiles("slurm")) {
            tmpDir = Paths.get(remoteworkspace, username, tmpFolder);
            algorithmJarPath = Paths.get(remoteworkspace, libFolder, algorithmJar);
            dataDir = Paths.get(remotedataspace, username, dataFolder);
        }

        // The following keys will be shared when running each algorithm
        map.put("algorithmJarPath", algorithmJarPath.toString());
        map.put("dataDir", dataDir.toAbsolutePath().toString());
        map.put("tmpDir", tmpDir.toAbsolutePath().toString());
        map.put("resultDir", resultDir.toAbsolutePath().toString());

        return map;
    }

    /**
     * List all Queued or Running jobs of a certain user
     *
     * @param uid
     * @return
     */
    public List<JobInfoDTO> listAllJobs(Long uid) {
        List<JobInfoDTO> jobInfoDTOs = new LinkedList<>();

        // When we can get here vai AuthFilterSerice, it means the user exists
        // so no need to check if (userAccount == null) and throw UserNotFoundException(uid)
        UserAccount userAccount = userAccountService.findById(uid);

        List<JobQueueInfo> jobs = jobQueueInfoService.findByUserAccounts(Collections.singleton(userAccount));
        jobs.forEach(job -> {
            JobInfoDTO jobInfoDTO = new JobInfoDTO();

            // Not listing data file name nor ID in response at this moment
            jobInfoDTO.setId(job.getId()); // Job ID
            jobInfoDTO.setAlgorithmName(job.getAlgorName());
            jobInfoDTO.setStatus(job.getStatus());
            jobInfoDTO.setAddedTime(job.getAddedTime());

            String fileName = job.getFileName();
            String resultJsonFileName = fileName + ".json";
            fileName = fileName + ".txt";
            String errorFileName = String.format("error_%s", fileName);

            jobInfoDTO.setResultFileName(fileName);
            jobInfoDTO.setResultJsonFileName(resultJsonFileName);
            jobInfoDTO.setErrorResultFileName(errorFileName);

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
     * @param uid
     * @param id
     * @return jobInfoDTO
     */
    public JobInfoDTO checkJobStatus(Long uid, Long id) {
        // When we can get here vai AuthFilterSerice, it means the user exists
        // so no need to check if (userAccount == null) and throw UserNotFoundException(uid)
        UserAccount userAccount = userAccountService.findById(uid);

        JobQueueInfo job = jobQueueInfoService.findByIdAndUseraccount(id, userAccount);

        if (job == null) {
            throw new ResourceNotFoundException(String.format("Unable to find job with ID %d for user with ID: %d", id, uid));
        }

        JobInfoDTO jobInfoDTO = new JobInfoDTO();

        // Not listing data file name nor ID in response at this moment
        jobInfoDTO.setId(job.getId()); // Job ID
        jobInfoDTO.setAlgorithmName(job.getAlgorName());
        jobInfoDTO.setStatus(job.getStatus());
        jobInfoDTO.setAddedTime(job.getAddedTime());

        String fileName = job.getFileName();
        String resultJsonFileName = fileName + ".json";
        fileName = fileName + ".txt";
        String errorFileName = String.format("error_%s", fileName);

        jobInfoDTO.setResultFileName(fileName);
        jobInfoDTO.setResultJsonFileName(resultJsonFileName);
        jobInfoDTO.setErrorResultFileName(errorFileName);

        return jobInfoDTO;
    }

    /**
     * Cancel a running job
     *
     * We'll need to make sure this job is created by this user
     *
     * @param uid
     * @param id
     * @return true on canceled or false if job is already completed
     */
    public boolean cancelJob(Long uid, Long id) {
        // When we can get here vai AuthFilterSerice, it means the user exists
        // so no need to check if (userAccount == null) and throw UserNotFoundException(uid)
        UserAccount userAccount = userAccountService.findById(uid);

        JobQueueInfo job = jobQueueInfoService.findByIdAndUseraccount(id, userAccount);
        // If can't find the job id from database, it's already completed
        // Then we are unable to cancel the job
        if (job == null) {
            LOGGER.warn(String.format("Unable to cancel job from queue. Job ID: %d", id));
            return false;
        }
        // Set status to 2 in database so the job queue knows it's a flag to kill the job
        job.setStatus(2);
        jobQueueInfoService.saveJobIntoQueue(job);

        LOGGER.info(String.format("Job canceled from queue. Job ID: %d", id));

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
