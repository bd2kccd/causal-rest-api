# Causal REST API V1

This RESTful API is designed for causal web. And it implements the [JAX-RS](https://en.wikipedia.org/wiki/Java_API_for_RESTful_Web_Services) specifications using Jersey.

## Installation

### Dependencies

If you want to run this API server and expose the API to your users, you'll first need to have the [Causal Web Application](https://github.com/bd2kccd/causal-web) installed and running. Your API users will use this web app to create their user accounts before they can consume the API.

In order to build the API server, you'll need the released version of [ccd-commons-0.3.1](https://github.com/bd2kccd/ccd-commons/releases/tag/v0.3.1) by going to the repo and checkout this specific release version:

````
git clone https://github.com/bd2kccd/ccd-commons.git
cd ccd-commons
git checkout tags/v0.3.1
mvn clean install
````

You'll also need to download [ccd-db-0.6.2](https://github.com/bd2kccd/ccd-db) branch:

````
git clone https://github.com/bd2kccd/ccd-db.git
cd ccd-db
git checkout v0.6.2
mvn clean install
````

** Note: we'll use the the 0.6.2 tagged release once it's released, only use the branch for now.**

And the last piece is [ccd-job-queue-0.1.4](https://github.com/bd2kccd/ccd-job-queue)

````
git clone https://github.com/bd2kccd/ccd-job-queue.git
cd ccd-job-queue
git checkout tags/v0.1.4
mvn clean install
````

Then you can go get and install `causal-rest-api`:

````
git clone https://github.com/bd2kccd/causal-rest-api.git
cd causal-rest-api
mvn clean package
````

### Configuration

There are 4 configuration files to configure located at `causal-rest-api/src/main/resources`:
- **application-hsqldb.properties**: HSQLDB database configurations (for testing only).
- **application-mysql.properties**: MySQL database configurations
- **application.properties**: Spring Boot application settings
- **causal.properties**: Data file directory path and folder settings

Befor editing the `causal.properties` file, you need to create a workspace for the application to work in. Create a directory called workspace, for an example `/home/zhy19/ccd/workspace`. Inside the workspace directory, create another folder called `lib`. Then build the jar file of Tetred using the [tetrad-5.3.0-20160624](https://github.com/cmu-phil/tetrad/releases/tag/v5.3.0-20160624) pre-release version. After that, copy the jar file to the `lib` folder created earlier.

### Start the API Server

Once you have all the settings configured, go to `causal-rest-api/target` and you will find the jar file named `causal-rest-api.jar`. Then simply run 

```bash
java -jar causal-rest-api.jar
```
This will start the API server, and you'll be able to access the API endpoints via the URL of `http://localhost:[port]/causal/api/v1/`

## API Usage and Examples

This API requires user to be authenticated. Before using this API, the user will need to go to [Causal-Web App](https://dxcvm26.psc.edu/ccd/login) and create an account. After that, the username and password can be used to authenticate against the REST API via HTTP Basic Auth. The username will need to be specified in the requesting URI and password provided in the HTTP request `Authorization` header.

Since this API is developed with Jersey, which supports [WADL](https://en.wikipedia.org/wiki/Web_Application_Description_Language). So you can view the generated WADL by going to `http://localhost:9000/causal/api/v1/application.wadl?detail=true` and see all resource available in the application. And below are some examples.

Basically, all the API usage examples are grouped into three categories: 

1. Data Management
2. Causal Discovery
3. Result Management

### 1. Data Management

#### Upload small data file

This is a multipart file upload via an HTML form, and the client is required to use `name="file"` to name their file upload field in their form.

````
POST /causal/api/v1/zhy19/data/upload HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW

----WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="file"; filename=""
Content-Type: 


----WebKitFormBoundary7MA4YWxkTrZu0gW
````

If the Authorization header is not provided, the response will look like this:

````javascript
{
  "timestamp": 1465414501443,
  "status": 401,
  "error": "Unauthorized",
  "message": "User credentials are required.",
  "path": "/zhy19/data/upload"
}
````

This POST request will upload the data file to the target server location and add corresponding records into database. And the response will contain the following pieces:

````javascript
{
  "id": 6,
  "name": "Lung-tetrad_hv.txt",
  "creationTime": 1466622267000,
  "lastModifiedTime": 1466622267000,
  "fileSize": 3309465,
  "md5checkSum": "b1db7511ee293d297e3055d9a7b46c5e"
}
````

#### Resumable data file upload

In addition to the regular file upload described in Example 6, we also provide the option of stable and resumable large file upload. It requires the client side to have a resumable upload implementation. We currently support client integrated with [Resumable.js](http://resumablejs.com/), whihc provides multiple simultaneous, stable 
and resumable uploads via the HTML5 File API.

In this example, the data file is splited into 3 chunks. The upload of each chunk consists of a GET request and a POST request. 

````
GET /causal/api/v1/zhy19/data/chunkUpload?resumableChunkNumber=2&resumableChunkSize=1048576&resumableCurrentChunkSize=1048576&resumableTotalSize=3309465&resumableType=text%2Fplain&resumableIdentifier=3309465-large-datatxt&resumableFilename=large-data.txt&resumableRelativePath=large-data.txt&resumableTotalChunks=3 HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
````

This GET request checks if the data chunk is already on the server side. If nothing there, the client will issue another POST request to upload the actual data.

````
POST /causal/api/v1/zhy19/data/chunkUpload HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
Content-Type: multipart/form-data; boundary=----WebKitFormBoundaryMFjgApg56XGyeTnZ

------WebKitFormBoundaryMFjgApg56XGyeTnZ
Content-Disposition: form-data; name="resumableChunkNumber"

2
------WebKitFormBoundaryMFjgApg56XGyeTnZ
Content-Disposition: form-data; name="resumableChunkSize"

1048576
------WebKitFormBoundaryMFjgApg56XGyeTnZ
Content-Disposition: form-data; name="resumableCurrentChunkSize"

1048576
------WebKitFormBoundaryMFjgApg56XGyeTnZ
Content-Disposition: form-data; name="resumableTotalSize"

3309465
------WebKitFormBoundaryMFjgApg56XGyeTnZ
Content-Disposition: form-data; name="resumableType"

text/plain
------WebKitFormBoundaryMFjgApg56XGyeTnZ
Content-Disposition: form-data; name="resumableIdentifier"

3309465-large-datatxt
------WebKitFormBoundaryMFjgApg56XGyeTnZ
Content-Disposition: form-data; name="resumableFilename"

large-data.txt
------WebKitFormBoundaryMFjgApg56XGyeTnZ
Content-Disposition: form-data; name="resumableRelativePath"

large-data.txt
------WebKitFormBoundaryMFjgApg56XGyeTnZ
Content-Disposition: form-data; name="resumableTotalChunks"

3
------WebKitFormBoundaryMFjgApg56XGyeTnZ
Content-Disposition: form-data; name="file"; filename="blob"
Content-Type: application/octet-stream


------WebKitFormBoundaryMFjgApg56XGyeTnZ--
````

Each chunk upload POST will get a 200 status code from response if everything works fine.


And finally the md5checkSum string of the reassemabled file will be returned once the whole file has been uploaded successfully. In this example, the POST request that uploads the third chunk will response this:

````
b1db7511ee293d297e3055d9a7b46c5e
````

#### List all data files of a user

````
GET /causal/api/v1/zhy19/data HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
Accept: application/json
````

This `GET` request to the endpoint `http://localhost:9000/causal/api/v1/zhy19/data` with `Basic Auth` will return a `JSON` formatted list of all the input data files that are associated with user `zhy19`

````javascript
[
  {
    "id": 8,
    "name": "data_small.txt",
    "creationTime": 1467132449000,
    "lastModifiedTime": 1467132449000,
    "fileSize": 278428,
    "fileSummary": {
      "md5checkSum": "ed5f27a2cf94fe3735a5d9ed9191c382",
      "variableType": "continuous",
      "fileDelimiter": "tab",
      "numOfRows": 302,
      "numOfColumns": 123
    }
  },
  {
    "id": 10,
    "name": "large-data.txt",
    "creationTime": 1467134048000,
    "lastModifiedTime": 1467134048000,
    "fileSize": 3309465,
    "fileSummary": {
      "md5checkSum": "b1db7511ee293d297e3055d9a7b46c5e",
      "variableType": null,
      "fileDelimiter": null,
      "numOfRows": null,
      "numOfColumns": null
    }
  },
  {
    "id": 11,
    "name": "Lung-tetrad_hv (copy).txt",
    "creationTime": 1467140415000,
    "lastModifiedTime": 1467140415000,
    "fileSize": 3309465,
    "fileSummary": {
      "md5checkSum": "b1db7511ee293d297e3055d9a7b46c5e",
      "variableType": "continuous",
      "fileDelimiter": "tab",
      "numOfRows": 302,
      "numOfColumns": 608
    }
  }
]
````

You can also specify the response format as XML in your request

````
GET /causal/api/v1/zhy19/data HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
Accept: application/xml
````

And the response will look like this:

````xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<dataFileDTOes>
    <dataFile>
        <id>8</id>
        <name>data_small.txt</name>
        <creationTime>2016-06-28T12:47:29-04:00</creationTime>
        <lastModifiedTime>2016-06-28T12:47:29-04:00</lastModifiedTime>
        <fileSize>278428</fileSize>
        <fileSummary>
            <fileDelimiter>tab</fileDelimiter>
            <md5checkSum>ed5f27a2cf94fe3735a5d9ed9191c382</md5checkSum>
            <numOfColumns>123</numOfColumns>
            <numOfRows>302</numOfRows>
            <variableType>continuous</variableType>
        </fileSummary>
    </dataFile>
    <dataFile>
        <id>10</id>
        <name>large-data.txt</name>
        <creationTime>2016-06-28T13:14:08-04:00</creationTime>
        <lastModifiedTime>2016-06-28T13:14:08-04:00</lastModifiedTime>
        <fileSize>3309465</fileSize>
        <fileSummary>
            <md5checkSum>b1db7511ee293d297e3055d9a7b46c5e</md5checkSum>
            <variableType xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true"/>
            <fileDelimiter xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true"/>
            <numOfRows xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true"/>
            <numOfColumns xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true"/>
        </fileSummary>
    </dataFile>
    <dataFile>
        <id>11</id>
        <name>Lung-tetrad_hv (copy).txt</name>
        <creationTime>2016-06-28T15:00:15-04:00</creationTime>
        <lastModifiedTime>2016-06-28T15:00:15-04:00</lastModifiedTime>
        <fileSize>3309465</fileSize>
        <fileSummary>
            <fileDelimiter>tab</fileDelimiter>
            <md5checkSum>b1db7511ee293d297e3055d9a7b46c5e</md5checkSum>
            <numOfColumns>608</numOfColumns>
            <numOfRows>302</numOfRows>
            <variableType>continuous</variableType>
        </fileSummary>
    </dataFile>
</dataFileDTOes>
````

Form the above output, we can also tell that data file with ID 10 doesn't have all the `fileSummary` field values set, we'll cover this in the data summarization section.

#### Get the deatil information of a data file based on ID

You can also query the data file info for a given file id

````
GET /causal/api/v1/zhy19/data/8 HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
````

And the resulting response looks like this:

````javascript
{
  "id": 8,
  "name": "data_small.txt",
  "creationTime": 1467132449000,
  "lastModifiedTime": 1467132449000,
  "fileSize": 278428,
  "fileSummary": {
    "md5checkSum": "ed5f27a2cf94fe3735a5d9ed9191c382",
    "variableType": "continuous",
    "fileDelimiter": "tab",
    "numOfRows": 302,
    "numOfColumns": 123
  }
}
````

#### Delete physical data file and all records from database for a given file ID

````
DELETE /causal/api/v1/zhy19/data/8 HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
````

And this will result a HTTP 204 No Content status in response on success, which means the server successfully processed the deletion request but there's no content to response.


#### Summarize data file

So from the first example we can tell that file with ID 10 doesn't have `variableType`, `fileDelimiter`, `numOfRows`, and `numOfColumns` specified under `fileSummary`. Among these attributes, variableType` and `fileDelimiter` are the ones that users will need to provide during this summarization process.

Before we can go ahead to run the desired algorithm with the newly uploaded data file, we'll need to summarize the data by specifing the variable type and file delimiter.

| Required Fields | Description |
| --- | --- |
| id | The data file ID |
| variableType | discrete or continuous |
| fileDelimiter | tab or comma |

````
POST /causal/api/v1/zhy19/data/summarize HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
Content-Type: application/json

{
    "id": 1,
    "variableType": "continuous",
    "fileDelimiter": "comma"
}
````

This POST request will summarize the data file and generate a response (JSON or XML) like below:

````javascript
{
  "id": 10,
  "name": "large-data.txt",
  "creationTime": 1467134048000,
  "lastModifiedTime": 1467134048000,
  "fileSize": 3309465,
  "fileSummary": {
    "md5checkSum": "b1db7511ee293d297e3055d9a7b46c5e",
    "variableType": "continuous",
    "fileDelimiter": "tab",
    "numOfRows": 302,
    "numOfColumns": 608
  }
}
````

### 2. Causal Discovery

Once the data file is uploaded and summaried, you can start running a Causal Discovery Algorithm on the uploaded data file.

#### List all the available causal discovery algorithms

````
GET /causal/api/v1/zhy19/algorithms HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
````
Currently we support "FGS continuous" and "FGS discrete".

````javascript
[
  {
    "id": 1,
    "name": "fgs",
    "description": "FGS continuous"
  },
  {
    "id": 2,
    "name": "fgs-discrete",
    "description": "FGS discrete"
  }
]
````

Currently we support "FGS continuous" and "FGS discrete" and they share a common JSON structure as of their input, for example:

| Input JSON Fields | Description |
| --- | --- |
| `dataFileId` | The data file ID, integer |
| `dataValidation` | Algorithm specific input data validation flags, JSON object |
| `algorithmParameters` | Algorithm specific parameters, JSON object |
| `jvmOptions` | Advanced Options For Java Virtual Machine (JVM), JSON object. Currently only support `maxHeapSize` (Gigabyte, max value is 100) |

Below are the data validation flags and parameters that you can use for each algorithm.

**FGS continuous** 

Data validation:

| Parameters        | Description           | Default Value  |
| ------------- | ------------- | ----- |
| `nonZeroVariance`      | Non-zero Variance. Ensure that each variable has non-zero variance | true |
| `uniqueVarName`      | Unique Variable Name. Ensure that there are no duplicated variable names      |  true |

Algorithm parameters:

| Parameters        | Description           | Default Value  |
| ------------- | ------------- | ----- |
| `depth`      | Search depth. Integer value |  |
| `penaltyDiscount`      | Penalty discount      |   4.0 |
| `ignoreLinearDependence` | Ignore linear dependence      |    true |
| `heuristicSpeedup` | Heuristic speedup. All conditional independence relations that hold in the distribution are entailed by the Causal Markov Assumption      |    true |
| `verbose` | Print additional information      |    true |

**FGS discrete** 

Data validation:

| Parameters        | Description           | Default Value  |
| ------------- | ------------- | ----- |
| `nonZeroVariance`      | Non-zero Variance. Ensure that each variable has non-zero variance | true |
| `uniqueVarName`      | Unique Variable Name. Ensure that there are no duplicated variable names      |  true |
| `limitNumOfCategory`      | Limit Number of Categories - ensure the number of categories of a variable does not exceed 10 | true |


Algorithm parameters:

| Parameters        | Description           | Default Value  |
| ------------- | ------------- | ----- |
| `depth`      | Search depth. Integer value |  |
| `structurePrior`      | Penalty discount      |  |
| `samplePrior` | Sample prior      |  |
| `heuristicSpeedup` | Heuristic speedup. All conditional independence relations that hold in the distribution are entailed by the Causal Markov Assumption      |    true |
| `verbose` | Print additional information      |    true |

#### Add a new job to run the desired algorithm on a given data file

This is a POST request and the algorithm and data file id will need to be specified in the POST body as a JSON when you make the request.

````
POST /causal/api/v1/zhy19/jobs/fgs HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
Content-Type: application/json

{
    "dataFileId": 8,
    "dataValidation": {
      "nonZeroVariance": false,
      "uniqueVarName": false
    },
    "algorithmParameters": {
      "depth": 3,
      "penaltyDiscount": 5.0
    },
    "jvmOptions": {
      "maxHeapSize": 100
    }
}
````

In this example, we are running the "FGS continuous" algorithm on the file with ID 8. And this call will return the job ID number with a 201 Created response status code.

When you need to run "FGS discrete", just send the request to a different endpont URI:

````
POST /causal/api/v1/zhy19/jobs/fgs-discrete HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
Content-Type: application/json

{
    "dataFileId": 9,
    "dataValidation": {
      "nonZeroVariance": false,
      "uniqueVarName": false,
      "limitNumOfCategory": flase
    },
    "algorithmParameters": {
      "depth": 3,
      "structurePrior": 1.0,
      "samplePrior": 1.0
    },
    "jvmOptions": {
      "maxHeapSize": 100
    }
}
````

#### List all running jobs

````
GET /causal/api/v1/zhy19/jobs/ HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
Content-Type: application/json

````

Then you'll see the information of all jobs that are currently running:

````javascript
[
  {
    "id": 32,
    "algorithmName": "fgs",
    "addedTime": 1468436085000
  },
  {
    "id": 33,
    "algorithmName": "fgs",
    "addedTime": 1468436087000
  }
]
````

#### Check the job status for a given job ID

Once the new job is submitted, it takes time and resources to run the algorithm on the server. During the waiting, you can check the status of a given job ID:

````
GET /causal/api/v1/zhy19/jobs/32 HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
````

This will either return "Pending" or "Completed".

#### Cancel a running job

Sometimes you may want to cancel a submitted job.

````
DELETE /causal/api/v1/zhy19/jobs/8 HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
````

This call will response either "Job 8 has been canceled" or "Unable to cancel job 8". It's not guranteed that the system can always cencal a job successfully.

### 3. Result Management

#### List all result files generated by the algorithm

````
GET /causal/api/v1/zhy19/results HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
````

The response to this request will look like this:

````javascript
[
  {
    "name": "fgs_sim_data_20vars_100cases.csv_1466171729046.txt",
    "creationTime": 1466171732000,
    "lastModifiedTime": 1466171732000,
    "fileSize": 1660
  },
  {
    "name": "fgs_data_small.txt_1466172140585.txt",
    "creationTime": 1466172145000,
    "lastModifiedTime": 1466172145000,
    "fileSize": 39559
  }
]
````

#### Download a speific result file generated by the algorithm based on file name

````
GET /causal/api/v1/zhy19/results/fgs_data_small.txt_1466172140585.txt HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
````
On success, you will get the result file back as text file content. If there's a typo in file name of the that file doesn't exist, you'll get either a JSON or XML message based on the `accept` header in your request:

The response to this request will look like this:

````javascript
{
  "timestamp": 1467210996233,
  "status": 404,
  "error": "Not Found",
  "message": "Resource not found.",
  "path": "/zhy19/results/fgs_data_small.txt_146172140585.txt"
}
````


#### Compare algorithm result files

From Example 4 we can list all the algorithm result files, based on the results, we can also choose multiple files and run a comparison. 

````
GET /causal/api/v1/zhy19/results/compare/fgs_sim_data_20vars_100cases.csv_1466171729046.txt!!fgs_data_small.txt_1467305104859.txt HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
````
When you specify multiple file names, use the `!!` as a delimiter. This request will generate a result comparison file with the following content (shortened version):

````
fgs_sim_data_20vars_100cases.csv_1466171729046.txt	fgs_data_small.txt_1467305104859.txt
Edges	In All	Same End Point
NR4A2,FOS	0	0
X5,X17	0	0
MMP11,ASB5	0	0
X12,X8	0	0
hsa_miR_654_3p,hsa_miR_337_3p	0	0
RND1,FGA	0	0
HHLA2,UBXN10	0	0
HS6ST2,RND1	0	0
SCRG1,hsa_miR_377	0	0
CDH3,diag	0	0
SERPINI2,FGG	0	0
hsa_miR_451,hsa_miR_136_	0	0
````

From this comparison, you can see if the two algorithm graphs have common edges and endpoints.

#### List all the comparison files

````
GET /causal/api/v1/zhy19/results/comparisons HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
````

The response will show a list of comparison files:

````javascript
[
  {
    "name": "result_comparison_1467385923407.txt",
    "creationTime": 1467385923000,
    "lastModifiedTime": 1467385923000,
    "fileSize": 7505
  },
  {
    "name": "result_comparison_1467387034358.txt",
    "creationTime": 1467387034000,
    "lastModifiedTime": 1467387034000,
    "fileSize": 7505
  },
  {
    "name": "result_comparison_1467388042261.txt",
    "creationTime": 1467388042000,
    "lastModifiedTime": 1467388042000,
    "fileSize": 7533
  }
]
````

#### Download a speific comparison file based on file name

````
GET /causal/api/v1/zhy19/results/comparisons/result_comparison_1467388042261.txt HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
````

Then it returns the content of that comparison file (shorted version):

````
fgs_sim_data_20vars_100cases.csv_1466171729046.txt	fgs_data_small.txt_1467305104859.txt
Edges	In All	Same End Point
NR4A2,FOS	0	0
X5,X17	0	0
MMP11,ASB5	0	0
X12,X8	0	0
hsa_miR_654_3p,hsa_miR_337_3p	0	0
RND1,FGA	0	0
HHLA2,UBXN10	0	0
HS6ST2,RND1	0	0
SCRG1,hsa_miR_377	0	0
CDH3,diag	0	0
SERPINI2,FGG	0	0
````

