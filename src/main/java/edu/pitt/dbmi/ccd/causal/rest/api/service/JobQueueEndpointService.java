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

import edu.cmu.tetrad.annotation.AlgorithmAnnotations;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.AlgoParameter;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.JobInfoDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.JvmOptions;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.NewJob;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.BadRequestException;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.NotFoundByIdException;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.causal.rest.api.prop.CausalRestProperties;
import edu.pitt.dbmi.ccd.causal.rest.api.util.CmdOptions;
import edu.pitt.dbmi.ccd.db.entity.DataFile;
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
    
    private final AlgorithmEndpointService algorithmEndpointService;
    
    private final ScoreEndpointService scoreEndpointService;
    
    private final IndependenceTestEndpointService independenceTestEndpointService;
    
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
            AlgorithmEndpointService algorithmEndpointService,
            IndependenceTestEndpointService independenceTestEndpointService,
            ScoreEndpointService scoreEndpointService,
            JobQueueInfoService jobQueueInfoService) {
        this.causalRestProperties = causalRestProperties;
        this.userAccountService = userAccountService;
        this.dataFileService = dataFileService;
        this.algorithmEndpointService = algorithmEndpointService;
        this.independenceTestEndpointService = independenceTestEndpointService;
        this.scoreEndpointService = scoreEndpointService;
        this.jobQueueInfoService = jobQueueInfoService;
    }
    
    public JobInfoDTO addNewJob(Long uid, NewJob newJob) {
        // When we can get here vai AuthFilterSerice, it means the user exists
        // so no need to check if (userAccount == null) and throw UserNotFoundException(uid)
        UserAccount userAccount = userAccountService.findById(uid);

        String username = userAccount.getUsername();

        // Paths
        Map<String, String> pathsMap = new HashMap<>();

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

        // The following keys will be used when each search
        pathsMap.put("algorithmJarPath", algorithmJarPath.toString());
        pathsMap.put("dataDir", dataDir.toAbsolutePath().toString());
        pathsMap.put("tmpDir", tmpDir.toAbsolutePath().toString());
        pathsMap.put("resultDir", resultDir.toAbsolutePath().toString());
 
        // Get algoId, testId, scoreId
        String algoId = newJob.getAlgoId();
        String testId = (newJob.getTestId() == null) ? null : newJob.getTestId();
        String scoreId = (newJob.getScoreId() == null) ? null : newJob.getScoreId();
        
        // Make sure this algoId is valid
        algorithmEndpointService.validateUserProvidedAlgorithm(algoId);
        
        Class clazz = algorithmEndpointService.getAlgorithmClass(algoId);

        boolean algoRequireTest = AlgorithmAnnotations.getInstance().requireIndependenceTest(clazz);
        boolean algoRequireScore = AlgorithmAnnotations.getInstance().requireScore(clazz);
        boolean algoAcceptKnowledge = AlgorithmAnnotations.getInstance().acceptKnowledge(clazz);

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
        commands.add(pathsMap.get("algorithmJarPath"));

        // Add algorithm
        commands.add(CmdOptions.ALGORITHM);
        commands.add(algoId);

        // Get dataset file name by file id
        Long datasetFileId = newJob.getDatasetFileId();
        
        if (datasetFileId != null) {
            DataFile datasetFile = dataFileService.findByIdAndUserAccount(datasetFileId, userAccount);
            if (datasetFile == null) {
                throw new NotFoundByIdException(datasetFileId);
            }
            
            // Specify data type
            String dataType = datasetFile.getDataFileInfo().getVariableType().getName();
            commands.add(CmdOptions.DATATYPE);
            commands.add(dataType);
            
            // Specify dataset file path
            Path datasetPath = Paths.get(pathsMap.get("dataDir"), datasetFile.getName());

            commands.add(CmdOptions.DATASET);
            commands.add(datasetPath.toAbsolutePath().toString());
        
            // Set delimiter
            commands.add(CmdOptions.DELIMITER);
            commands.add(datasetFile.getDataFileInfo().getFileDelimiter().getName());
        }
        
        // Validate user provided test/score and throws exception if invalid
        algorithmEndpointService.validateUserProvidedTest(algoRequireTest, testId);
        algorithmEndpointService.validateUserProvidedScore(algoRequireScore, scoreId);

        if (algoRequireTest) {
            commands.add(CmdOptions.TEST);
            commands.add(testId);
        }

        if (algoRequireScore) {
            commands.add(CmdOptions.SCORE);
            commands.add(scoreId);
        }

        // Create tetrad graph json for HPC?
        commands.add(CmdOptions.JSON_GRAPH);
        
        // Add prior knowloedge file if this algo accepts it and it's provided
        if (newJob.getPriorKnowledgeFileId() != null) {
            if (algoAcceptKnowledge) {
                // Get prior knowledge file name by file id
                Long priorKnowledgeFileId = newJob.getPriorKnowledgeFileId();
                DataFile priorKnowledgeFile = dataFileService.findByIdAndUserAccount(priorKnowledgeFileId, userAccount);
                if (priorKnowledgeFile == null) {
                    throw new NotFoundByIdException(priorKnowledgeFileId);
                }
                Path priorKnowledgePath = Paths.get(pathsMap.get("dataDir"), priorKnowledgeFile.getName());

                commands.add(CmdOptions.KNOWLEDGE);
                commands.add(priorKnowledgePath.toAbsolutePath().toString());
            } else {
                throw new BadRequestException("Algorithm " + algoId + " doesn't accept knowledge file.");
            }
        }

        // Algorithm parameters from user request
        Set<AlgoParameter> algorithmParameters = newJob.getAlgoParameters();

        // Full list of parameters
        List<String> algoParametersAll = algorithmEndpointService.getAlgoParameters(algoId, testId, scoreId);
        
        if (algoParametersAll != null) {
            // Get key-value from algo parameters
            algorithmParameters.forEach(param -> {
                if (!algoParametersAll.contains(param.getKey())) {
                    throw new BadRequestException("Unrecognized algorithm parameter: " + param.getKey());
                }

                commands.add("--" + param.getKey());
                commands.add(param.getValue());
            });
        }

        long currentTime = System.currentTimeMillis();
        // Algorithm result file name
        String fileName;

        DataFile df = dataFileService.findByIdAndUserAccount(datasetFileId, userAccount);
        // The algorithm name can be different from the value of causalRestProperties.getAlgoFgesCont()
        fileName = String.format("%s_%s_%d", algoId, df.getName(), currentTime);

        // Output file name prefix
        commands.add(CmdOptions.FILE_PREFIX);
        commands.add(fileName);

        // Skip data validation?
        if (newJob.isSkipDataValidation()) {
            commands.add(CmdOptions.SKIP_VALIDATION);
        } 
        
        // Then separate those commands with ; and store the whole string into database
        // ccd-job-queue will assemble the command line again
        String cmd = listToSeparatedValues(commands, ";");

        // Insert to database table `job_queue_info`
        JobQueueInfo jobQueueInfo = new JobQueueInfo();
        jobQueueInfo.setAddedTime(new Date(System.currentTimeMillis()));
        jobQueueInfo.setAlgorName(algoId); // use algoId as Algorithm Name in db, for now
        jobQueueInfo.setCommands(cmd);
        jobQueueInfo.setFileName(fileName);
        jobQueueInfo.setOutputDirectory(pathsMap.get("resultDir"));
        jobQueueInfo.setStatus(0);
        jobQueueInfo.setTmpDirectory(pathsMap.get("tmpDir"));
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

        // This needs to be consistent with Causal CMD generated graph JSON filename
        String resultJsonFileName = fileName + "_graph.json";
        fileName = fileName + ".txt";
        String errorFileName = String.format("error_%s", fileName);

        JobInfoDTO jobInfo = new JobInfoDTO();
        jobInfo.setStatus(0);
        jobInfo.setAddedTime(jobQueueInfo.getAddedTime());
        jobInfo.setAlgoId(algoId);
        jobInfo.setResultFileName(fileName);
        jobInfo.setResultJsonFileName(resultJsonFileName);
        jobInfo.setErrorResultFileName(errorFileName);
        jobInfo.setId(jobQueueInfo.getId());

        return jobInfo;
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
            jobInfoDTO.setAlgoId(job.getAlgorName());
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
        jobInfoDTO.setAlgoId(job.getAlgorName());
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
