# Causal REST API v1.0.0

This RESTful API is designed for causal web. And it implements the [JAX-RS](https://en.wikipedia.org/wiki/Java_API_for_RESTful_Web_Services) specifications using Jersey.

**Table of Contents**

* auto-gen TOC:
{:toc}

## API Usage and Examples

In the following sections, we'll demonstrate the API usage with examples using the API server that is running on Pittsburgh Super Computing. The API base URI is https://ccd4.vm.bridges.psc.edu/ccd-api.

This API requires user to be authenticated. Before using this API, the user will need to go to [Causal-Web App](https://ccd4.vm.bridges.psc.edu/ccd/) and create an account. 

### Getting JSON Web Token(JWT)

After registration in Causal Web App, the email and password can be used to authenticate against the Causal REST API to get the access token (we use JWT) via **HTTP Basic Auth**. 

API Endpoint URI pattern:

````
GET https://ccd4.vm.bridges.psc.edu/ccd-api/jwt
````

In basic auth, the user provides the username and password, which the HTTP client concatenates (username + ":" + password), and base64 encodes it. This encoded string is then sent using a `Authorization` header with the "Basic" schema. For instance user email `demo@pitt.edu` whose password is `123`.

````
POST /ccd-api/jwt HTTP/1.1
Host: ccd4.vm.bridges.psc.edu
Authorization: Basic ZGVtb0BwaXR0LmVkdToxMjM=
````

Once the request is processed successfully, the user ID together with a JWT will be returned in the response for further API queries.

````
{
  "userId": 22,
  "jwt": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA0Mjg1OTcsImlhdCI6MTQ3NTg0NjgyODU5N30.FcE7aEpg0u2c-gUVugIjJkzjhlDu5qav_XHtgLu3c6E",
  "issuedTime": 1475846828597,
  "lifetime": 3600,
  "expireTime": 1475850428597,
  "wallTime": [
    1,
    3,
    6
  ]
}
````

We'll need to use this `userId` in the URI path of all subsequent requests. And this `jwt` expires in 3600 seconds(1 hour), so the API consumer will need to request for another JWT otherwise the API query to other API endpoints will be denied. And this JWT will need to be sent via the HTTP `Authorization` header as well, but using the `Bearer` schema.

The `wallTime` field is designed for users who want to specify the the maximum CPU time when Slurm handles the jobs on PSC. Normally, a job is expected to finish before the specified maximum walltime.  After the walltime reaches the maximum, the job terminates regardless whether the job processes are still running or not. In this example, you can pick 1 hour, 3 or 6 hours as the wallTime.

Note: querying the JWT endpoint again before the current JWT expires will generate a new JWT, which makes the old JWT expired automatically. And this newly generated JWT will be valid in another hour unless there's another new JWT being queried.

Since this API is developed with Jersey, which supports [WADL](https://en.wikipedia.org/wiki/Web_Application_Description_Language). So you can view the generated WADL by going to `https://ccd4.vm.bridges.psc.edu/ccd-api/application.wadl?detail=true` and see all resource available in the application. Accessing to this endpoint doesn't require authentication.

Basically, all the API usage examples are grouped into three categories: 

1. Data Management
2. Causal Discovery
3. Result Management

And all the following examples will be issued by user `22` whose password is `123`.

### 1. Data Management

#### Upload small data file

At this point, you can upload two types of data files: tabular dataset file(either tab delimited or comma delimited) and prior knowledge file.

API Endpoint URI pattern:

````
POST https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/dataset/upload
````

This is a multipart file upload via an HTML form, and the client is required to use `name="file"` to name their file upload field in their form.

Generated HTTP request code example:

````
POST /ccd-api/22/dataset/upload HTTP/1.1
Host: ccd4.vm.bridges.psc.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
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
  "path": "/22/dataset/upload"
}
````

This POST request will upload the dataset file to the target server location and add corresponding records into database. And the response will contain the following pieces:

````javascript
{
    "id": 6,
    "name": "Lung-tetrad_hv.txt",
    "creationTime": 1466622267000,
    "lastModifiedTime": 1466622267000,
    "fileSize": 3309465,
    "md5checkSum": "b1db7511ee293d297e3055d9a7b46c5e",
    "fileSummary": {
      "variableType": null,
      "fileDelimiter": null,
      "numOfRows": null,
      "numOfColumns": null
    }
  }
````

The prior knowledge file upload uses a similar API endpoint:

````
POST https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/priorknowledge/upload
````

Due to there's no need to summarize a prior knowledge file, the response of a successful prior knowledge file upload will look like:


````javascript
{
    "id": 6,
    "name": "Lung-tetrad_hv.txt",
    "creationTime": 1466622267000,
    "lastModifiedTime": 1466622267000,
    "fileSize": 3309465,
    "md5checkSum": "ugdb7511rt293d29ke3055d9a7b46c9k"
  }
````

#### Resumable data file upload

In addition to the regular file upload described in Example 6, we also provide the option of stable and resumable large file upload. It requires the client side to have a resumable upload implementation. We currently support client integrated with [Resumable.js](http://resumablejs.com/), whihc provides multiple simultaneous, stable 
and resumable uploads via the HTML5 File API. You can also create your own client as long as al the following parameters are set correctly.

API Endpoint URI pattern:

````
GET https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/chunkupload

POST https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/chunkupload
````

In this example, the data file is splited into 3 chunks. The upload of each chunk consists of a GET request and a POST request. To handle the state of upload chunks, a number of extra parameters are sent along with all requests:

* `resumableChunkNumber`: The index of the chunk in the current upload. First chunk is `1` (no base-0 counting here).
* `resumableChunkSize`: The general chunk size. Using this value and `resumableTotalSize` you can calculate the total number of chunks. Please note that the size of the data received in the HTTP might be lower than `resumableChunkSize` of this for the last chunk for a file.
* `resumableCurrentChunkSize`: The size of the current resumable chuck.
* `resumableTotalSize`: The total file size.
* `resumableType`: The file type of the resumable chuck, e.e., "text/plain".
* `resumableIdentifier`: A unique identifier for the file contained in the request.
* `resumableFilename`: The original file name (since a bug in Firefox results in the file name not being transmitted in chunk multipart posts).
* `resumableRelativePath`: The file's relative path when selecting a directory (defaults to file name in all browsers except Chrome).
* `resumableTotalChunks`: The total number of chunks.  

Generated HTTP request code example:

````
GET /ccd-api/22/chunkupload?resumableChunkNumber=2&resumableChunkSize=1048576&resumableCurrentChunkSize=1048576&resumableTotalSize=3309465&resumableType=text%2Fplain&resumableIdentifier=3309465-large-datatxt&resumableFilename=large-data.txt&resumableRelativePath=large-data.txt&resumableTotalChunks=3 HTTP/1.1
Host: ccd4.vm.bridges.psc.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
````

This GET request checks if the data chunk is already on the server side. If the target file chunk is not found on the server, the client will issue a POST request to upload the actual data.

Generated HTTP request code example:

````
POST /ccd-api/22/chunkupload HTTP/1.1
Host: ccd4.vm.bridges.psc.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
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

#### List all dataset files of a user

API Endpoint URI pattern:

````
GET https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/dataset
````

Generated HTTP request code example:

````
GET /ccd-api/22/dataset HTTP/1.1
Host: ccd4.vm.bridges.psc.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
Accept: application/json
````

A `JSON` formatted list of all the input dataset files that are associated with user `22` will be returned.

````json
[
  {
    "id": 8,
    "name": "data_small.txt",
    "creationTime": 1467132449000,
    "lastModifiedTime": 1467132449000,
    "fileSize": 278428,
    "md5checkSum": "ed5f27a2cf94fe3735a5d9ed9191c382",
    "fileSummary": {
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
    "md5checkSum": "b1db7511ee293d297e3055d9a7b46c5e",
    "fileSummary": {
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
    "md5checkSum": "b1db7511ee293d297e3055d9a7b46c5e",
    "fileSummary": {
      "variableType": "continuous",
      "fileDelimiter": "tab",
      "numOfRows": 302,
      "numOfColumns": 608
    }
  }
]
````

You can also specify the response format as XML in your request

Generated HTTP request code example:

````
GET /ccd-api/22/dataset HTTP/1.1
Host: ccd4.vm.bridges.psc.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
Accept: application/xml
````

And the response will look like this:

````xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<datasetFileDTOes>
    <datasetFile>
        <id>8</id>
        <name>data_small.txt</name>
        <creationTime>2016-06-28T12:47:29-04:00</creationTime>
        <lastModifiedTime>2016-06-28T12:47:29-04:00</lastModifiedTime>
        <fileSize>278428</fileSize>
        <md5checkSum>ed5f27a2cf94fe3735a5d9ed9191c382</md5checkSum>
        <fileSummary>
            <fileDelimiter>tab</fileDelimiter>
            <numOfColumns>123</numOfColumns>
            <numOfRows>302</numOfRows>
            <variableType>continuous</variableType>
        </fileSummary>
    </datasetFile>
    <datasetFile>
        <id>10</id>
        <name>large-data.txt</name>
        <creationTime>2016-06-28T13:14:08-04:00</creationTime>
        <lastModifiedTime>2016-06-28T13:14:08-04:00</lastModifiedTime>
        <fileSize>3309465</fileSize>
        <md5checkSum>b1db7511ee293d297e3055d9a7b46c5e</md5checkSum>
        <fileSummary>
            <variableType xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true"/>
            <fileDelimiter xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true"/>
            <numOfRows xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true"/>
            <numOfColumns xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true"/>
        </fileSummary>
    </datasetFile>
    <datasetFile>
        <id>11</id>
        <name>Lung-tetrad_hv (copy).txt</name>
        <creationTime>2016-06-28T15:00:15-04:00</creationTime>
        <lastModifiedTime>2016-06-28T15:00:15-04:00</lastModifiedTime>
        <fileSize>3309465</fileSize>
        <md5checkSum>b1db7511ee293d297e3055d9a7b46c5e</md5checkSum>
        <fileSummary>
            <fileDelimiter>tab</fileDelimiter>
            <numOfColumns>608</numOfColumns>
            <numOfRows>302</numOfRows>
            <variableType>continuous</variableType>
        </fileSummary>
    </datasetFile>
</datasetFileDTOes>
````

Form the above output, we can also tell that data file with ID 10 doesn't have all the `fileSummary` field values set, we'll cover this in the dataset summarization section.

#### Get the detail information of a dataset file based on ID

API Endpoint URI pattern:

````
GET https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/dataset/{id}
````

Generated HTTP request code example:

````
GET /ccd-api/22/dataset/8 HTTP/1.1
Host: ccd4.vm.bridges.psc.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
````

And the resulting response looks like this:

````json
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

#### Delete physical dataset file and all records from database for a given file ID

API Endpoint URI pattern:

````
DELETE https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/dataset/{id}
````

Generated HTTP request code example:

````
DELETE /ccd-api/22/dataset/8 HTTP/1.1
Host: ccd4.vm.bridges.psc.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
````

And this will result a HTTP 204 No Content status in response on success, which means the server successfully processed the deletion request but there's no content to response.


#### Summarize dataset file

So from the first example we can tell that file with ID 10 doesn't have `variableType`, `fileDelimiter`, `numOfRows`, and `numOfColumns` specified under `fileSummary`. Among these attributes, variableType` and `fileDelimiter` are the ones that users will need to provide during this summarization process.

Before we can go ahead to run the desired algorithm with the newly uploaded data file, we'll need to summarize the data by specifing the variable type and file delimiter.

| Required Fields | Description |
| --- | --- |
| id | The data file ID |
| variableType | discrete or continuous |
| fileDelimiter | tab or comma |

API Endpoint URI pattern:

````
POST https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/dataset/summarize
````

Generated HTTP request code example:

````
POST /ccd-api/22/dataset/summarize HTTP/1.1
Host: ccd4.vm.bridges.psc.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
Content-Type: application/json

{
    "id": 1,
    "variableType": "continuous",
    "fileDelimiter": "comma"
}
````

This POST request will summarize the dataset file and generate a response (JSON or XML) like below:

````json
{
  "id": 10,
  "name": "large-data.txt",
  "creationTime": 1467134048000,
  "lastModifiedTime": 1467134048000,
  "fileSize": 3309465,
  "md5checkSum": "b1db7511ee293d297e3055d9a7b46c5e",
  "fileSummary": {
    "variableType": "continuous",
    "fileDelimiter": "tab",
    "numOfRows": 302,
    "numOfColumns": 608
  }
}
````

#### List all prior knowledge files of a given user

API Endpoint URI pattern:

````
GET https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/priorknowledge
````

Generated HTTP request code example:

````
GET /ccd-api/22/priorknowledge HTTP/1.1
Host: ccd4.vm.bridges.psc.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
Accept: application/json
````

A `JSON` formatted list of all the input dataset files that are associated with user `22` will be returned.

````json
[
  {
    "id": 9,
    "name": "data_small.prior",
    "creationTime": 1467132449000,
    "lastModifiedTime": 1467132449000,
    "fileSize": 278428,
    "md5checkSum": "ed5f27a2cf94fe3735a5d9ed9191c382"
  },
  {
    "id": 12,
    "name": "large-data.prior",
    "creationTime": 1467134048000,
    "lastModifiedTime": 1467134048000,
    "fileSize": 3309465,
    "md5checkSum": "b1db7511ee293d297e3055d9a7b46c5e"
  }
]
````

#### Get the detail information of a prior knowledge file based on ID

API Endpoint URI pattern:

````
GET https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/priorknowledge/{id}
````

Generated HTTP request code example:

````
GET /ccd-api/22/priorknowledge/9 HTTP/1.1
Host: ccd4.vm.bridges.psc.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
````

And the resulting response looks like this:

````json
{
  "id": 9,
  "name": "data_small.prior",
  "creationTime": 1467132449000,
  "lastModifiedTime": 1467132449000,
  "fileSize": 278428,
  "md5checkSum": "ed5f27a2cf94fe3735a5d9ed9191c382"
}
````

#### Delete physical prior knowledge file and all records from database for a given file ID

API Endpoint URI pattern:

````
DELETE https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/priorknowledge/{id}
````

Generated HTTP request code example:

````
DELETE /ccd-api/22/priorknowledge/9 HTTP/1.1
Host: ccd4.vm.bridges.psc.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
````

And this will result a HTTP 204 No Content status in response on success, which means the server successfully processed the deletion request but there's no content to response.

### 2. Causal Discovery

Once the data file is uploaded and summaried, you can start running a Causal Discovery Algorithm on the uploaded data file.

#### List all the available causal discovery algorithms

API Endpoint URI pattern:

````
GET https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/algorithms
````

Generated HTTP request code example:

````
GET /ccd-api/22/algorithms HTTP/1.1
Host: ccd4.vm.bridges.psc.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
````

````json
[
    {
        "id": "r4",
        "name": "R4",
        "description": "These are algorithms that orient edges X—Y for continuous variables pairwise based on non-Gaussian information. (If the variables are all Gaussian, one cannot orient these edges. That is, these rules will orient left or right randomly.) For EB, RSkew, RSkewE, Skew and SkewE, see Hyvarinen and Smith (2013). For R1, R2, R3 and R4, see Ramsey et al., 2014.\n\nThe principles governing these rules vary. R1 and R2 appeal directly to the Central Limit Theorem to judge which of various conditioning sets yields the greatest non-Gaussianity measure. (The measure for non-Gaussianity measure used is Anderson-Darling.) R4 does as well, but allows coefficients for relevant parameters to be adjusted to achieve maximum non-Gaussianity. EB works by appealing to entropy for the orientation. R3 uses the same rule as EB except using the Anderson-Darling score for a measure of non-Gaussianity. RSkew and Skew appeal to measures of skew for the variables and assume positive skewness for all variables. The rules for the two are different; please see Hyvarinen and Smith for details. SkewE and RSkewE adjust the signs of variables by the signs of their skewnesses to ensure that the assumption of positive skewness holds. \n\nA comparison of all of these methods is given in Ramsey et al., 2014. In general, for fMRI data, we find that the RSkew method works the best, followed by the R3 method. Cycles can be oriented using these methods, since each edge is oriented independently of the others.\n\nInput Assumptions: Continuous data in which the variables are non-Gaussian. Non-Gaussianity can be assessed using the Anderson-Darling score, which is available in the Data box.\n\nOutput Format: Orients all of the edges in the input graph using the selected score. \n\nParameters:\n- Cutoff for p-values (alpha). Conditional independence tests with p-values greater than this will be judged to be independent (H0).\n- Maximum size of conditioning set (depth). PC in the adjacency phase will consider conditioning sets for conditional independences of increasing size, up to this value. For instance, for depth 3, the maximum size of a conditioning set considered will be 3.",
        "requireTest": false,
        "requireScore": false,
        "acceptKnowledge": false
    },
    {
        "id": "r1",
        "name": "R1",
        "description": "These are algorithms that orient edges X—Y for continuous variables pairwise based on non-Gaussian information. (If the variables are all Gaussian, one cannot orient these edges. That is, these rules will orient left or right randomly.) For EB, RSkew, RSkewE, Skew and SkewE, see Hyvarinen and Smith (2013). For R1, R2, R3 and R4, see Ramsey et al., 2014.\n\nThe principles governing these rules vary. R1 and R2 appeal directly to the Central Limit Theorem to judge which of various conditioning sets yields the greatest non-Gaussianity measure. (The measure for non-Gaussianity measure used is Anderson-Darling.) R4 does as well, but allows coefficients for relevant parameters to be adjusted to achieve maximum non-Gaussianity. EB works by appealing to entropy for the orientation. R3 uses the same rule as EB except using the Anderson-Darling score for a measure of non-Gaussianity. RSkew and Skew appeal to measures of skew for the variables and assume positive skewness for all variables. The rules for the two are different; please see Hyvarinen and Smith for details. SkewE and RSkewE adjust the signs of variables by the signs of their skewnesses to ensure that the assumption of positive skewness holds. \n\nA comparison of all of these methods is given in Ramsey et al., 2014. In general, for fMRI data, we find that the RSkew method works the best, followed by the R3 method. Cycles can be oriented using these methods, since each edge is oriented independently of the others.\n\nInput Assumptions: Continuous data in which the variables are non-Gaussian. Non-Gaussianity can be assessed using the Anderson-Darling score, which is available in the Data box.\n\nOutput Format: Orients all of the edges in the input graph using the selected score. \n\nParameters:\n- Cutoff for p-values (alpha). Conditional independence tests with p-values greater than this will be judged to be independent (H0).\n- Maximum size of conditioning set (depth). PC in the adjacency phase will consider conditioning sets for conditional independences of increasing size, up to this value. For instance, for depth 3, the maximum size of a conditioning set considered will be 3.",
        "requireTest": false,
        "requireScore": false,
        "acceptKnowledge": false
    },
    {
        "id": "fci",
        "name": "FCI",
        "description": "This method extends the Fast Causal Inference algorithm given in Spirtes, Glymour and Scheines, Causation, Prediction and Search with Jiji Zhang's Augmented FCI rules (found in sec. 4.1 of Zhang's 2006 PhD dissertation, \"Causal Inference and Reasoning in Causally Insufficient Systems\").\n\nThis class is based off a copy of Fci.java taken from the repository on 2008/12/16, revision 7306. The extension is done by extending doFinalOrientation() with methods for Zhang's rules R5-R10 which implements the augmented search. (By a remark of Zhang's, the rule applications can be staged in this way.)\n\nFor more detail about fci implementation, please visit http://cmu-phil.github.io/tetrad/tetrad-lib-apidocs/edu/cmu/tetrad/search/Fci.html",
        "requireTest": true,
        "requireScore": false,
        "acceptKnowledge": true
    },
    {
        "id": "r3",
        "name": "R3",
        "description": "These are algorithms that orient edges X—Y for continuous variables pairwise based on non-Gaussian information. (If the variables are all Gaussian, one cannot orient these edges. That is, these rules will orient left or right randomly.) For EB, RSkew, RSkewE, Skew and SkewE, see Hyvarinen and Smith (2013). For R1, R2, R3 and R4, see Ramsey et al., 2014.\n\nThe principles governing these rules vary. R1 and R2 appeal directly to the Central Limit Theorem to judge which of various conditioning sets yields the greatest non-Gaussianity measure. (The measure for non-Gaussianity measure used is Anderson-Darling.) R4 does as well, but allows coefficients for relevant parameters to be adjusted to achieve maximum non-Gaussianity. EB works by appealing to entropy for the orientation. R3 uses the same rule as EB except using the Anderson-Darling score for a measure of non-Gaussianity. RSkew and Skew appeal to measures of skew for the variables and assume positive skewness for all variables. The rules for the two are different; please see Hyvarinen and Smith for details. SkewE and RSkewE adjust the signs of variables by the signs of their skewnesses to ensure that the assumption of positive skewness holds. \n\nA comparison of all of these methods is given in Ramsey et al., 2014. In general, for fMRI data, we find that the RSkew method works the best, followed by the R3 method. Cycles can be oriented using these methods, since each edge is oriented independently of the others.\n\nInput Assumptions: Continuous data in which the variables are non-Gaussian. Non-Gaussianity can be assessed using the Anderson-Darling score, which is available in the Data box.\n\nOutput Format: Orients all of the edges in the input graph using the selected score. \n\nParameters:\n- Cutoff for p-values (alpha). Conditional independence tests with p-values greater than this will be judged to be independent (H0).\n- Maximum size of conditioning set (depth). PC in the adjacency phase will consider conditioning sets for conditional independences of increasing size, up to this value. For instance, for depth 3, the maximum size of a conditioning set considered will be 3.",
        "requireTest": false,
        "requireScore": false,
        "acceptKnowledge": false
    },
    {
        "id": "pc-all",
        "name": "PC All",
        "description": "PcAll presents a range of PC (\"Peter and Clark\") based algorithms. For the PC algorithm, see Spirtes, Glymour and Scheines, \ncausation, Prediction and Search. For the conservative version, see Ramsey et al. (2012). The adjacency search from PC-Stable is available; the reference for that is Colombo and Maathuis (2014). Three collider resolution rules are available. \"Priority\" overwrites previous colliders; \"Overwrite\" keeps previous orientation information while orienting new colliders; \"Bidirected\" allows bidirected edges to be oriented when orienting new colliders.",
        "requireTest": true,
        "requireScore": false,
        "acceptKnowledge": true
    },
    {
        "id": "ts-imgs",
        "name": "TsImages",
        "description": "tsIMAGES is a version of tsGFCI which averages BIC scores across multiple data sets. Thus, it is used to search for a PAG (partial ancestral graph) from time series data from multiple units (subjects, countries, etc). tsIMAGES allows both for unmeasured (hidden, latent) variables and the possibility that different subjects have different causal parameters, though they share the same qualitative causal structure. As with IMAGES, the user can specify a “penalty score” to produce more sparse models. For the traditional definition of the BIC score, set the penalty to 1.0. See the documentation for IMAGES and tsGFCI.\n\nInput Assumptions: The (continuous) data has been generated by a time series. \n\nOutput Format: \n\nParameters: Uses the parameters of IMaGES (see).",
        "requireTest": false,
        "requireScore": true,
        "acceptKnowledge": true
    },
    {
        "id": "bpc",
        "name": "Bpc",
        "description": "BPC (Build Pure Clusters) searches for causal structure over latent variables, where the true models are Multiple Indicator Models (MIM’s). The idea is this. There is a set of latent (unmeasured) variables over which a directed acyclic model has been defined. Then for each of these latent L there are 3 (preferably 4) or more measures of that variable—that is, measured variables that are all children of L. Under these conditions, one may define tetrad constraints (see Spirtes et al., 2000). There is a theorem to the effect that if certain patterns of these tetrad constraints hold, there must be a latent common cause of all of them (the Tetrad Representation Theorem, see Spirtes, Glymour, and Scheines (1993), where the BPC (“Build Pure Clusters”) algorithm is defined and discussed.) Moreover, once one has such a “measurement model,” once can estimate a covariance matrix over the latent variables that are parents of the measures and use some algorithm such as PC or GES to estimate a pattern over the latents. The algorithm to run PC or GES on this covariance matrix is called MimBuild (“MIM” is the the graph, Multiple Indicator Model; “Build” means build). In this way, one may recover causal structure over the latents. The more measures one has for each latent, the better the result is, generally. The larger the sample size the better. One important issue is that the algorithm is sensitive to so-called “impurities”—that is, causal edges among the measured variables, or between measured variables and unintended latent. The algorithm will in effect remove one measure in each impure pair from consideration.\n\nInput Assumptions: Continuous data, a collection of measurements in the above sense, excluding the latent variables (which are to be learned).\n\nOutput Format: For BPC, a measurement model, in the above sense. This is represented as a clustering of variables; it may be inferred that there is a single latent for each output cluster. For MimBuild, a pattern over the latent variables, one for each cluster.\n\nParameters: \n- Cutoff for p-values (alpha). Conditional independence tests with p-values greater\nthan this will be judged to be independent (H0). Default 0.01.\n- Yes if the Wishart test should be used. No if the Delta test should be used. These are two tests of whether a set of four variables constitutes a pure tetrad—that is, if all tetrads for this set of four variables vanish. For the notion of a vanishing tetrad, see Spirtes et al., 2000. For the Delta test, see ??. Default No (Delta test).",
        "requireTest": false,
        "requireScore": false,
        "acceptKnowledge": true
    },
    {
        "id": "r2",
        "name": "R2",
        "description": "These are algorithms that orient edges X—Y for continuous variables pairwise based on non-Gaussian information. (If the variables are all Gaussian, one cannot orient these edges. That is, these rules will orient left or right randomly.) For EB, RSkew, RSkewE, Skew and SkewE, see Hyvarinen and Smith (2013). For R1, R2, R3 and R4, see Ramsey et al., 2014.\n\nThe principles governing these rules vary. R1 and R2 appeal directly to the Central Limit Theorem to judge which of various conditioning sets yields the greatest non-Gaussianity measure. (The measure for non-Gaussianity measure used is Anderson-Darling.) R4 does as well, but allows coefficients for relevant parameters to be adjusted to achieve maximum non-Gaussianity. EB works by appealing to entropy for the orientation. R3 uses the same rule as EB except using the Anderson-Darling score for a measure of non-Gaussianity. RSkew and Skew appeal to measures of skew for the variables and assume positive skewness for all variables. The rules for the two are different; please see Hyvarinen and Smith for details. SkewE and RSkewE adjust the signs of variables by the signs of their skewnesses to ensure that the assumption of positive skewness holds. \n\nA comparison of all of these methods is given in Ramsey et al., 2014. In general, for fMRI data, we find that the RSkew method works the best, followed by the R3 method. Cycles can be oriented using these methods, since each edge is oriented independently of the others.\n\nInput Assumptions: Continuous data in which the variables are non-Gaussian. Non-Gaussianity can be assessed using the Anderson-Darling score, which is available in the Data box.\n\nOutput Format: Orients all of the edges in the input graph using the selected score. \n\nParameters:\n- Cutoff for p-values (alpha). Conditional independence tests with p-values greater than this will be judged to be independent (H0).\n- Maximum size of conditioning set (depth). PC in the adjacency phase will consider conditioning sets for conditional independences of increasing size, up to this value. For instance, for depth 3, the maximum size of a conditioning set considered will be 3.",
        "requireTest": false,
        "requireScore": false,
        "acceptKnowledge": false
    },
    {
        "id": "eb",
        "name": "EB",
        "description": "These are algorithms that orient edges X—Y for continuous variables pairwise based on non-Gaussian information. (If the variables are all Gaussian, one cannot orient these edges. That is, these rules will orient left or right randomly.) For EB, RSkew, RSkewE, Skew and SkewE, see Hyvarinen and Smith (2013). For R1, R2, R3 and R4, see Ramsey et al., 2014.\n\nThe principles governing these rules vary. R1 and R2 appeal directly to the Central Limit Theorem to judge which of various conditioning sets yields the greatest non-Gaussianity measure. (The measure for non-Gaussianity measure used is Anderson-Darling.) R4 does as well, but allows coefficients for relevant parameters to be adjusted to achieve maximum non-Gaussianity. EB works by appealing to entropy for the orientation. R3 uses the same rule as EB except using the Anderson-Darling score for a measure of non-Gaussianity. RSkew and Skew appeal to measures of skew for the variables and assume positive skewness for all variables. The rules for the two are different; please see Hyvarinen and Smith for details. SkewE and RSkewE adjust the signs of variables by the signs of their skewnesses to ensure that the assumption of positive skewness holds. \n\nA comparison of all of these methods is given in Ramsey et al., 2014. In general, for fMRI data, we find that the RSkew method works the best, followed by the R3 method. Cycles can be oriented using these methods, since each edge is oriented independently of the others.\n\nInput Assumptions: Continuous data in which the variables are non-Gaussian. Non-Gaussianity can be assessed using the Anderson-Darling score, which is available in the Data box.\n\nOutput Format: Orients all of the edges in the input graph using the selected score. \n\nParameters:\n- Cutoff for p-values (alpha). Conditional independence tests with p-values greater than this will be judged to be independent (H0).\n- Maximum size of conditioning set (depth). PC in the adjacency phase will consider conditioning sets for conditional independences of increasing size, up to this value. For instance, for depth 3, the maximum size of a conditioning set considered will be 3.",
        "requireTest": false,
        "requireScore": false,
        "acceptKnowledge": false
    },
    {
        "id": "fask",
        "name": "FASK",
        "description": "Searches single continuous datasets for models with possible cycles and 2-cycles, assuming the variables are skewed. Latent common causes are not supported. Uses the Fast Adjacency Search (FAS, that is, the adjacency search of the PC algorithms) with the linear, Gaussian BIC score as a test of conditional independence. One may adjust sparsity of the graph by adjusting the 'penaltyDiscount' parameter. The orientation procedure assumes the variables are skewed. Sensitivity for detection of 2-cycles may be adjusted using the 2-cycle alpha parameter.",
        "requireTest": false,
        "requireScore": true,
        "acceptKnowledge": true
    },
    {
        "id": "mbfs",
        "name": "MBFS",
        "description": "Markov blanket fan search. Similar to FGES-MB but using PC as the basic search instead of FGES. The rules of the PC search are restricted to just the variables in the Markov blanket of a target T, including T; the result is a graph that is a pattern over these variables.\n\nInput Assumptions: Same as for PC\n\nOutput Format: A pattern over a selected group of nodes that includes the target and each node in the Markov blanket of the target. \n\nParameters: Uses the parameters of PC.\n- Target Name. The name of the target variables for the Markov blanket one wishes to construct. Default blank (that is, unspecified.) A variable must be specified here to run the algorithm.",
        "requireTest": true,
        "requireScore": false,
        "acceptKnowledge": true
    },
    {
        "id": "fofc",
        "name": "Fofc",
        "description": "The FOFC (Find One Factor Clusters) is an alternative method that achieves the same goal as BPC; in testing, it seems to scale better with somewhat better accuracy (Kummerfeld and Ramsey, 2016). The basic idea is to build up clusters one at a time by adding variables that keep them pure, in the sense that all relevant tetrad constraints still hold. There are different ways of going about this. One could try to build one cluster up as far as possible, then remove all of those variables from the set, and try to make a another cluster using the remaining variables (SAG, i.e., Seed and Grow). Or one can try in parallel to grow all possible clusters and then choose among the grown clusters using some criterion such as cluster size (GAP, Grow and Pick). In general, GAP is more accurate. The result is a clustering of variables. Similarly to BPC, MimBuild may be run on a covariance matrix estimated over the latents for the resulting clusters to find a pattern over the latents that represents the causal structure over the latents.\n\nInput Assumptions: Continuous data containing as many measures as are available.\n\nOutput Format: For FOFC, a clustering of variables. For MimBuild, a pattern over latents. \n\nParameters:\n- Cutoff for p-values (alpha). Conditional independence tests with p-values greater than this will be judged to be independent (H0). Default 0.01.\n- Yes if the Wishart test should be used. No if the Delta test should be used. These are two tests of whether a set of four variables constitutes a pure tetrad—that is, if all tetrads for this set of four variables vanish. For the notion of a vanishing tetrad, see Spirtes et al., 2000. For the Delta test, see ??. Default No (Delta test).\n- Yes if the GAP algorithm should be used. No if the SAG algorithm should be used.",
        "requireTest": false,
        "requireScore": false,
        "acceptKnowledge": true
    },
    {
        "id": "skew-e",
        "name": "SkewE",
        "description": "These are algorithms that orient edges X—Y for continuous variables pairwise based on non-Gaussian information. (If the variables are all Gaussian, one cannot orient these edges. That is, these rules will orient left or right randomly.) For EB, RSkew, RSkewE, Skew and SkewE, see Hyvarinen and Smith (2013). For R1, R2, R3 and R4, see Ramsey et al., 2014.\n\nThe principles governing these rules vary. R1 and R2 appeal directly to the Central Limit Theorem to judge which of various conditioning sets yields the greatest non-Gaussianity measure. (The measure for non-Gaussianity measure used is Anderson-Darling.) R4 does as well, but allows coefficients for relevant parameters to be adjusted to achieve maximum non-Gaussianity. EB works by appealing to entropy for the orientation. R3 uses the same rule as EB except using the Anderson-Darling score for a measure of non-Gaussianity. RSkew and Skew appeal to measures of skew for the variables and assume positive skewness for all variables. The rules for the two are different; please see Hyvarinen and Smith for details. SkewE and RSkewE adjust the signs of variables by the signs of their skewnesses to ensure that the assumption of positive skewness holds. \n\nA comparison of all of these methods is given in Ramsey et al., 2014. In general, for fMRI data, we find that the RSkew method works the best, followed by the R3 method. Cycles can be oriented using these methods, since each edge is oriented independently of the others.\n\nInput Assumptions: Continuous data in which the variables are non-Gaussian. Non-Gaussianity can be assessed using the Anderson-Darling score, which is available in the Data box.\n\nOutput Format: Orients all of the edges in the input graph using the selected score. \n\nParameters:\n- Cutoff for p-values (alpha). Conditional independence tests with p-values greater than this will be judged to be independent (H0).\n- Maximum size of conditioning set (depth). PC in the adjacency phase will consider conditioning sets for conditional independences of increasing size, up to this value. For instance, for depth 3, the maximum size of a conditioning set considered will be 3.",
        "requireTest": false,
        "requireScore": false,
        "acceptKnowledge": false
    },
    {
        "id": "imgs_cont",
        "name": "IMaGES Continuous",
        "description": "Description: Adjusts the continuous variable score (SEM BIC) of FGES so allow for multiple datasets as input. The linear, Gaussian BIC scores for each data set are averaged at each step of the algorithm, producing a model for al data sets that assumes they have the same graphical structure across dataset.\n\nInput Assumptions: A set of continuous datasets with the same variables and sample sizes. \n\nOutput Format: A pattern, interpreted as a common model for all datasets.\n\nParameters: All of the parameters from FGES are available for IMaGES. Additionally:\n- The number of runs. The number of times the algorithm should select data sets and run the algorithm. Default 1.\n- The number of datasets that should be taken in each random sample. IMaGES will randomly select a set of datasets to run, so that on different runs one can be an estimate of the consistency of results. To use all variables, set this to the total number of datasets. Default 1.\n",
        "requireTest": false,
        "requireScore": false,
        "acceptKnowledge": true
    },
    {
        "id": "ts-fci",
        "name": "TsFCI",
        "description": "The tsFCI algorithm is a version of GFCI for time series data. See the GFCI documentation for a description of the GFCI algorithm, which allows for unmeasured (hidden, latent) variables in the data-generating process and produces a PAG (partial ancestral graph). tsGFCI takes as input a “time lag data set,” i.e., a data set which includes time series observations of variables X1, X2, X3, ..., and their lags X1:1, X2:1, X3:1, ..., X1:2, X2:2,X3:2, ... and so on. X1:n is the nth-lag of the variable X1. To create a time lag data set from a standard tabular data set (i.e., a matrix of observations of X1, X2, X3, ...), use the “create time lag data” function in the data manipulation toolbox. The user will be prompted to specify the number of lags (n), and a new data set will be created with the above naming convention. The new sample size will be the old sample size minus n.\nInput Assumptions: The (continuous) data has been generated by a time series. Output Format: (Need to get this from Dan.)\n\nParameters:\n- Cutoff for p-values (alpha). Conditional independence tests with p-values greater than this will be judged to be independent (H0). Default 0.01.",
        "requireTest": true,
        "requireScore": false,
        "acceptKnowledge": true
    },
    {
        "id": "r-skew-e",
        "name": "RSkewE",
        "description": "These are algorithms that orient edges X—Y for continuous variables pairwise based on non-Gaussian information. (If the variables are all Gaussian, one cannot orient these edges. That is, these rules will orient left or right randomly.) For EB, RSkew, RSkewE, Skew and SkewE, see Hyvarinen and Smith (2013). For R1, R2, R3 and R4, see Ramsey et al., 2014.\n\nThe principles governing these rules vary. R1 and R2 appeal directly to the Central Limit Theorem to judge which of various conditioning sets yields the greatest non-Gaussianity measure. (The measure for non-Gaussianity measure used is Anderson-Darling.) R4 does as well, but allows coefficients for relevant parameters to be adjusted to achieve maximum non-Gaussianity. EB works by appealing to entropy for the orientation. R3 uses the same rule as EB except using the Anderson-Darling score for a measure of non-Gaussianity. RSkew and Skew appeal to measures of skew for the variables and assume positive skewness for all variables. The rules for the two are different; please see Hyvarinen and Smith for details. SkewE and RSkewE adjust the signs of variables by the signs of their skewnesses to ensure that the assumption of positive skewness holds. \n\nA comparison of all of these methods is given in Ramsey et al., 2014. In general, for fMRI data, we find that the RSkew method works the best, followed by the R3 method. Cycles can be oriented using these methods, since each edge is oriented independently of the others.\n\nInput Assumptions: Continuous data in which the variables are non-Gaussian. Non-Gaussianity can be assessed using the Anderson-Darling score, which is available in the Data box.\n\nOutput Format: Orients all of the edges in the input graph using the selected score. \n\nParameters:\n- Cutoff for p-values (alpha). Conditional independence tests with p-values greater than this will be judged to be independent (H0).\n- Maximum size of conditioning set (depth). PC in the adjacency phase will consider conditioning sets for conditional independences of increasing size, up to this value. For instance, for depth 3, the maximum size of a conditioning set considered will be 3.",
        "requireTest": false,
        "requireScore": false,
        "acceptKnowledge": false
    },
    {
        "id": "fask-concatenated",
        "name": "FASK Concatenated",
        "description": "Searches multiple continuous datasets for models with possible cycles and 2-cycles, assuming the variables are skewed. Latent common causes are not supported. Uses the Fast Adjacency Search (FAS, that is, the adjacency search of the PC algorithms) with the linear, Gaussian BIC score as a test of conditional independence. One may adjust sparsity of the graph by adjusting the 'penaltyDiscount' parameter. The orientation procedure assumes the variables are skewed. Sensitivity for detection of 2-cycles may be adjusted using the 2-cycle alpha parameter. Data from different datasets are centered and concatenated, then given to\" +\nFASK for search.",
        "requireTest": false,
        "requireScore": true,
        "acceptKnowledge": true
    },
    {
        "id": "imgs_disc",
        "name": "IMaGES Discrete",
        "description": "Adjusts the discrete BDeu variable score of FGES so allow for multiple datasets as input. The BDeu scores for each data set are averaged at each step of the algorithm, producing a model for al data sets that assumes they have the same graphical structure across dataset. Note that in order to use this algorithm in a nontrivial way, one needs to have loaded or simulated multiple dataset.\n\nInput Assumptions: A set of discrete datasets with the same variables and sample sizes. \n\nOutput Format: A pattern, interpreted as a common model for all datasets. \n\nParameters: All of the parameters from FGES are available for IMaGES. Additionally:\n- The number of runs. The number of times the algorithm should select data sets and 90 run the algorithm. Default 1.\n- The number of datasets that should be taken in each random sample. IMaGES will randomly select a set of datasets to run, so that on different runs one can be an estimate of the consistency of results. To use all variables, set this to the total number of datasets. Default 1.\n",
        "requireTest": false,
        "requireScore": false,
        "acceptKnowledge": true
    },
    {
        "id": "fas",
        "name": "FAS",
        "description": "FAS is the adjacency search of the PC algorithm, used as an adjacency search in many algorithms and sometimes useful in it own right as an undirected search that avoids marrying of parents. See Spirtes, Glymour and Scheines, Causation, Prediction and Search.",
        "requireTest": true,
        "requireScore": false,
        "acceptKnowledge": true
    },
    {
        "id": "mgm",
        "name": "MGM",
        "description": "Finds a Markov random field (with parents married) for a dataset in which continuous and discrete variables are mixed together. For example, if X->Y<-Z, the output will be X—Y—Z with X—Z. The parents of Y will be joined by an undirected edge, morally, even though this edge does not occur in the true model.\n\nInput Assumptions: Data are mixed.\n\nOutput Format: A Markov random field for the data. \n\nParameters:\n- MGM Tuning Parameters #1, #2, #3. Defaults for these are 0.1, though they can be adjusted. ",
        "requireTest": false,
        "requireScore": false,
        "acceptKnowledge": false
    },
    {
        "id": "r-skew",
        "name": "RSkew",
        "description": "These are algorithms that orient edges X—Y for continuous variables pairwise based on non-Gaussian information. (If the variables are all Gaussian, one cannot orient these edges. That is, these rules will orient left or right randomly.) For EB, RSkew, RSkewE, Skew and SkewE, see Hyvarinen and Smith (2013). For R1, R2, R3 and R4, see Ramsey et al., 2014.\n\nThe principles governing these rules vary. R1 and R2 appeal directly to the Central Limit Theorem to judge which of various conditioning sets yields the greatest non-Gaussianity measure. (The measure for non-Gaussianity measure used is Anderson-Darling.) R4 does as well, but allows coefficients for relevant parameters to be adjusted to achieve maximum non-Gaussianity. EB works by appealing to entropy for the orientation. R3 uses the same rule as EB except using the Anderson-Darling score for a measure of non-Gaussianity. RSkew and Skew appeal to measures of skew for the variables and assume positive skewness for all variables. The rules for the two are different; please see Hyvarinen and Smith for details. SkewE and RSkewE adjust the signs of variables by the signs of their skewnesses to ensure that the assumption of positive skewness holds. \n\nA comparison of all of these methods is given in Ramsey et al., 2014. In general, for fMRI data, we find that the RSkew method works the best, followed by the R3 method. Cycles can be oriented using these methods, since each edge is oriented independently of the others.\n\nInput Assumptions: Continuous data in which the variables are non-Gaussian. Non-Gaussianity can be assessed using the Anderson-Darling score, which is available in the Data box.\n\nOutput Format: Orients all of the edges in the input graph using the selected score. \n\nParameters:\n- Cutoff for p-values (alpha). Conditional independence tests with p-values greater than this will be judged to be independent (H0).\n- Maximum size of conditioning set (depth). PC in the adjacency phase will consider conditioning sets for conditional independences of increasing size, up to this value. For instance, for depth 3, the maximum size of a conditioning set considered will be 3.",
        "requireTest": false,
        "requireScore": false,
        "acceptKnowledge": false
    },
    {
        "id": "rfci",
        "name": "RFCI",
        "description": "This method extends Fast Causal Inference algorithm from Spirtes, Glymour and Scheines, Causation Prediction and Search, with Jiji Zhang's Augmented FCI rules (found in sec. 4.1 of Zhang's 2006 PhD dissertation, \"Causal Inference and Reasoning in Causally Insufficient Systems\").\n\nThis class is based off a copy of Fci.java taken from the repository on 2008/12/16, revision 7306. The extension is done by extending doFinalOrientation() with methods for Zhang's rules R5-R10 which implements the augmented search. (By a remark of Zhang's, the rule applications can be staged in this way.)\n\nFor more detail about rfci implementation, please visit http://cmu-phil.github.io/tetrad/tetrad-lib-apidocs/edu/cmu/tetrad/search/Rfci.html",
        "requireTest": true,
        "requireScore": false,
        "acceptKnowledge": true
    },
    {
        "id": "ts-gfci",
        "name": "TsGFCI",
        "description": "tsGFCI uses a BIC score to search for a skeleton. Thus, the only user-specified parameter is an optional “penalty score” to bias the search in favor of more sparse models. See the description of the GES algorithm for discussion of the penalty score. For the traditional definition of the BIC score, set the penalty to 1.0. The orientation rules are the same as for FCI. As is the case with tsFCI, tsGFCI will automatically respect the time order of the variables and impose a repeating structure. Firstly, it puts lagged variables in appropriate tiers so, e.g., X3:2 can cause X3:1 and X3 but X3:1 cannot cause X3:2 and X3 cannot cause either X3:1 or X3:2. Also, it will assume that the causal structure is the same across time, so that if the edge between X1 and X2 is removed because this increases the BIC score, then also the edge between X1:1 and X2:1 is removed, and so on for additional lags if they exist. When some edge is removed as the result of a score increase, all similar (or “homologous”) edges are also removed.\n\nInput Assumptions: The (continuous) data has been generated by a time series. Output Format: (Need to get this from Dan.)\n\nParameters: Uses the parameters of FCI and FGES.",
        "requireTest": true,
        "requireScore": true,
        "acceptKnowledge": true
    },
    {
        "id": "fges",
        "name": "FGES",
        "description": "Fast Greedy Equivalence Search (FGES) is an implementation of the revised GES algorithm. See Ramsey et al., 2017 for details. It works for both BayesNets and SEMs.\n\nSome code optimization could be done for the scoring part of the graph for discrete models (method scoreGraphChange). Some of Andrew Moore's approachesfor caching sufficient statistics, for instance.\n\n To speed things up, it has been assumed that variables X and Y with zero correlation do not correspond to edges in the graph. This is a restricted form of the faithfulness assumption, something FGES does not assume.\n\n For more detail about Fges implementation, please visit http://cmu-phil.github.io/tetrad/tetrad-lib-apidocs/edu/cmu/tetrad/search/Fgs.html",
        "requireTest": false,
        "requireScore": true,
        "acceptKnowledge": true
    },
    {
        "id": "glasso",
        "name": "GLASSO",
        "description": "A translation of the Fortran code for GLASSO - Friedman, Hastie and Tibshirani (2007) \n\nLike MGM, this produces an undirected graph in which parents are always married.\nInput Assumptions: The data are continuous. \n\nOutput Format: A Markov random field. \n\nParameters:\n- MAXIT, IA, IS, ITR, IPEN, THR. These are parameters in the translated Fortan code. Defaults are given in the interface and are the same as for the Fortran code.\n",
        "requireTest": false,
        "requireScore": false,
        "acceptKnowledge": false
    },
    {
        "id": "fges-mb",
        "name": "FgesMb",
        "description": "This is a restriction of the FGES algorithm to union of edges over the combined Markov blankets of a set of targets, including the targets. In the interface, just one target may be specified. See Ramsey et al., 2017 for details. In the general case, finding the graph over the Markov blanket variables of a target (including the target) is far faster than finding the pattern for all of the variables.\n\nInput Assumptions: The same as FGES\n\nOutput Format: A graph over a selected group of nodes that includes the target and each node in the Markov blanket of the target. This will be the same as if FGES were run and the result restricted to just these variables, so some edges may be oriented in the returned graph that may not have been oriented in a pattern over the selected nodes.\n\nParameters: Uses the parameters of FGES.\n- Target Name. The name of the target variables for the Markov blanket one wishes to construct. Default blank (that is, unspecified.) A variable must be specified here to run the algorithm.",
        "requireTest": false,
        "requireScore": true,
        "acceptKnowledge": true
    },
    {
        "id": "skew",
        "name": "Skew",
        "description": "These are algorithms that orient edges X—Y for continuous variables pairwise based on non-Gaussian information. (If the variables are all Gaussian, one cannot orient these edges. That is, these rules will orient left or right randomly.) For EB, RSkew, RSkewE, Skew and SkewE, see Hyvarinen and Smith (2013). For R1, R2, R3 and R4, see Ramsey et al., 2014.\n\nThe principles governing these rules vary. R1 and R2 appeal directly to the Central Limit Theorem to judge which of various conditioning sets yields the greatest non-Gaussianity measure. (The measure for non-Gaussianity measure used is Anderson-Darling.) R4 does as well, but allows coefficients for relevant parameters to be adjusted to achieve maximum non-Gaussianity. EB works by appealing to entropy for the orientation. R3 uses the same rule as EB except using the Anderson-Darling score for a measure of non-Gaussianity. RSkew and Skew appeal to measures of skew for the variables and assume positive skewness for all variables. The rules for the two are different; please see Hyvarinen and Smith for details. SkewE and RSkewE adjust the signs of variables by the signs of their skewnesses to ensure that the assumption of positive skewness holds. \n\nA comparison of all of these methods is given in Ramsey et al., 2014. In general, for fMRI data, we find that the RSkew method works the best, followed by the R3 method. Cycles can be oriented using these methods, since each edge is oriented independently of the others.\n\nInput Assumptions: Continuous data in which the variables are non-Gaussian. Non-Gaussianity can be assessed using the Anderson-Darling score, which is available in the Data box.\n\nOutput Format: Orients all of the edges in the input graph using the selected score. \n\nParameters:\n- Cutoff for p-values (alpha). Conditional independence tests with p-values greater than this will be judged to be independent (H0).\n- Maximum size of conditioning set (depth). PC in the adjacency phase will consider conditioning sets for conditional independences of increasing size, up to this value. For instance, for depth 3, the maximum size of a conditioning set considered will be 3.",
        "requireTest": false,
        "requireScore": false,
        "acceptKnowledge": false
    },
    {
        "id": "gfci",
        "name": "GFCI",
        "description": "Greedy Fast Causal Inference Search (GFCI) is an implementation of the revised FCI algorithm.It uses FGES followed by PC adjacency removals. Uses conservative collider orientation. Gets sepsets for X---Y from among adjacents of X or of Y.\n\nFollowing an idea developed by Spirtes, now it uses more of the information in FGES, to calculating possible d-separation paths and to utilize unshielded colliders found by FGES.\n\nFor more detail about GFci implementation, please visit http://cmu-phil.github.io/tetrad/tetrad-lib-apidocs/edu/cmu/tetrad/search/GFci.html",
        "requireTest": true,
        "requireScore": true,
        "acceptKnowledge": true
    },
    {
        "id": "ftfc",
        "name": "Ftfc",
        "description": "FTFC (Find Two Factor Clusters) is similar to FOFC, but instead of each cluster having one latent that is the parent of all of the measure in the cluster, it instead has two such latents. So each measure has two latent parents; these are two “factors.” Similarly to FOFC, constraints are checked for, but in this case, the constraints must be sextad constraints, and more of them must be satisfied for each pure cluster (see Kummerfelt et al., 2014) Thus, the number of measures in each cluster, once impure edges have been taken into account, must be at least six, preferably more.\n\nInput Assumptions: Continuous data over the measures with at least six variable variables in each cluster once variables involve in impure edges have been removed.\n\nOutput Format: A clustering of measures. It may be assumed that each cluster has at least two factors and that the clusters are pure.\n\nParameters:\n- Cutoff for p-values (alpha). Conditional independence tests with p-values greater\nthan this will be judged to be independent (H0). Default 0.01.\n- Yes if the Wishart test should be used. No if the Delta test should be used. These are two tests of whether a set of four variables constitutes a pure tetrad—that is, if all tetrads for this set of four variables vanish. For the notion of a vanishing tetrad, see Spirtes et al., 2000. Default No (Delta test).\n- Yes if the GAP algorithm should be used. No if the SAG algorithm should be used (faster, less accurate).",
        "requireTest": false,
        "requireScore": false,
        "acceptKnowledge": true
    }
]
````

#### List details of given algorithm by ID

You can also just query the details of a specific algorithm:

API Endpoint URI pattern:

````
GET https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/algorithms/{algoId}
````


#### List all data types

API Endpoint URI pattern:

````
POST https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/datatypes
````

Generated HTTP request code example:

````json
[
    {
        "name": "Continuous"
    },
    {
        "name": "Discrete"
    },
    {
        "name": "Mixed"
    },
    {
        "name": "Graph"
    },
    {
        "name": "Covariance"
    }
]
````

#### List all independent tests

API Endpoint URI pattern:

````
POST https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/tests
````

Generated HTTP request code example:

````json
[
    {
        "id": "cond-correlation",
        "name": "Conditional Correlation Test",
        "supportedDataTypes": [
            "Continuous"
        ]
    },
    {
        "id": "fisher-z",
        "name": "Fisher Z Test",
        "supportedDataTypes": [
            "Continuous",
            "Covariance"
        ]
    },
    {
        "id": "sem-bic",
        "name": "SEM BIC Test",
        "supportedDataTypes": [
            "Continuous",
            "Covariance"
        ]
    },
    {
        "id": "cond-gauss-lrt",
        "name": "Conditional Gaussian Likelihood Ratio Test",
        "supportedDataTypes": [
            "Mixed"
        ]
    },
    {
        "id": "correlation-t",
        "name": "Correlation T Test",
        "supportedDataTypes": [
            "Continuous"
        ]
    },
    {
        "id": "bdeu",
        "name": "BDeu Test",
        "supportedDataTypes": [
            "Discrete"
        ]
    },
    {
        "id": "disc-bic",
        "name": "Discrete BIC Test",
        "supportedDataTypes": [
            "Discrete"
        ]
    },
    {
        "id": "d-sep",
        "name": "D-Separation Test",
        "supportedDataTypes": [
            "Graph"
        ]
    },
    {
        "id": "g-square",
        "name": "G Square Test",
        "supportedDataTypes": [
            "Discrete"
        ]
    },
    {
        "id": "multinomial-logistic-regression-wald",
        "name": "Multinomial Logistic Retression Wald Test",
        "supportedDataTypes": [
            "Mixed"
        ]
    },
    {
        "id": "chi-square",
        "name": "Chi Square Test",
        "supportedDataTypes": [
            "Discrete"
        ]
    }
]
````

#### List tests based on a given data type

API Endpoint URI pattern:

````
POST https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/tests/Continuous
````

Generated HTTP request code example:

````json
[
    {
        "id": "cond-correlation",
        "name": "Conditional Correlation Test",
        "supportedDataTypes": [
            "Continuous"
        ]
    },
    {
        "id": "fisher-z",
        "name": "Fisher Z Test",
        "supportedDataTypes": [
            "Continuous",
            "Covariance"
        ]
    },
    {
        "id": "sem-bic",
        "name": "SEM BIC Test",
        "supportedDataTypes": [
            "Continuous",
            "Covariance"
        ]
    },
    {
        "id": "correlation-t",
        "name": "Correlation T Test",
        "supportedDataTypes": [
            "Continuous"
        ]
    }
]
````

#### Liat all scores

API Endpoint URI pattern:

````
POST https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/scores
````

Generated HTTP request code example:

````json
[
    {
        "id": "disc-bic",
        "name": "Discrete BIC Score",
        "supportedDataTypes": [
            "Discrete"
        ]
    },
    {
        "id": "fisher-z",
        "name": "Fisher Z Score",
        "supportedDataTypes": [
            "Continuous"
        ]
    },
    {
        "id": "sem-bic-deterministic",
        "name": "Sem BIC Score Deterministic",
        "supportedDataTypes": [
            "Continuous",
            "Covariance"
        ]
    },
    {
        "id": "d-separation",
        "name": "D-separation Score",
        "supportedDataTypes": [
            "Graph"
        ]
    },
    {
        "id": "cond-gauss-bic",
        "name": "Conditional Gaussian BIC Score",
        "supportedDataTypes": [
            "Mixed"
        ]
    },
    {
        "id": "sem-bic",
        "name": "Sem BIC Score",
        "supportedDataTypes": [
            "Continuous",
            "Covariance"
        ]
    },
    {
        "id": "bdeu",
        "name": "BDeu Score",
        "supportedDataTypes": [
            "Discrete"
        ]
    }
]
````

#### List scores based on a given data type

API Endpoint URI pattern:

````
POST https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/scores/Discrete
````

Generated HTTP request code example:

````json
[
    {
        "id": "disc-bic",
        "name": "Discrete BIC Score",
        "supportedDataTypes": [
            "Discrete"
        ]
    },
    {
        "id": "bdeu",
        "name": "BDeu Score",
        "supportedDataTypes": [
            "Discrete"
        ]
    }
]
````

#### List all algorithm parameters based on the provided test and score

API Endpoint URI pattern:

````
POST https://ccd4.vm.bridges.psc.edu/ccd-api/algorithmParameters
````

Generated HTTP request code example:

````
POST /ccd-api/1/algorithmParameters/ HTTP/1.1
Host: ccd4.vm.bridges.psc.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0LyIsInVpZCI6MSwiZXhwIjoxNTE1NjIwNjQ4NTA2LCJpYXQiOjE1MTU2MTcwNDg1MDZ9.OvS1DNCRtgqNuOw0EX3TAxZxb998gvL84ZIRb3CykY0
Content-Type: application/json

{
    "algoId": "fges",
    "testId": "",
    "scoreId": "fisher-z"
}
````

````json
[
    {
        "name": "alpha",
        "description": "Cutoff for p values (alpha) (min = 0.0)",
        "valueType": "Double",
        "defaultValue": 0.01
    },
    {
        "name": "faithfulnessAssumed",
        "description": "Yes if (one edge) faithfulness should be assumed",
        "valueType": "Boolean",
        "defaultValue": true
    },
    {
        "name": "symmetricFirstStep",
        "description": "Yes if the first step step for FGES should do scoring for both X->Y and Y->X",
        "valueType": "Boolean",
        "defaultValue": false
    },
    {
        "name": "maxDegree",
        "description": "The maximum degree of the graph (min = -1)",
        "valueType": "Integer",
        "defaultValue": 100
    },
    {
        "name": "verbose",
        "description": "Yes if verbose output should be printed or logged",
        "valueType": "Boolean",
        "defaultValue": false
    },
    {
        "name": "bootstrapSampleSize",
        "description": "The number of bootstraps (min = 0)",
        "valueType": "Integer",
        "defaultValue": 0
    },
    {
        "name": "bootstrapEnsemble",
        "description": "Ensemble method: Preserved (0), Highest (1), Majority (2)",
        "valueType": "Integer",
        "defaultValue": 1
    }
]
````

#### Add a new job to run the desired algorithm on a given data file

This is a POST request and the algorithm details and data file id will need to be specified in the POST body as a JSON when you make the request.

API Endpoint URI pattern:

````
POST https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/newjob
````

Generated HTTP request code example:

````
POST /ccd-api/1/newjob HTTP/1.1
Host: localhost:9000
Authorization: Bearer faithfulnessAssumed
Content-Type: application/json

{
    "algoId": "fges",
    "scoreId": "fisher-z",
    "datasetFileId": 1,
    "algoParameters": [
    {
      "key": "alpha",
      "value": 0.4
    },
    {
      "key": "faithfulnessAssumed",
      "value": false
    },
    {
      "key": "maxDegree",
      "value": 10
    }
    ],
    "jvmOptions": {
      "maxHeapSize": 100
    }
}
````

In this example, we are running the "FGES" algorithm with "BDeu Test" and "BDeu Score" on the file of ID 8. We also set the wallTime as 1 hour. And this call will return the job info with a 201 Created response status code.

````
{
    "id": 25,
    "algoId": "fges",
    "status": 0,
    "addedTime": 1515617400509,
    "resultFileName": "fges_Retention.txt_1515617400499.txt",
    "resultJsonFileName": "fges_Retention.txt_1515617400499.json",
    "errorResultFileName": "error_fges_Retention.txt_1515617400499.txt"
}
````

From this response we can tell that the job ID is 25, and the result file name will be `fges_Retention.txt_1515617400499.txt` if everything goes well. If something is wrong an error result file with name `error_fges_Retention.txt_1515617400499.txt` will be created.


#### List all running jobs

API Endpoint URI pattern:

````
GET https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/jobs
````

Generated HTTP request code example:

````
GET /ccd-api/22/jobs/ HTTP/1.1
Host: ccd4.vm.bridges.psc.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
Content-Type: application/json

````

Then you'll see the information of all jobs that are currently running:

````javascript
[
  {
    "id": 32,
    "algorithmName": "FGESc",
    "addedTime": 1468436085000
  },
  {
    "id": 33,
    "algorithmName": "FGESd",
    "addedTime": 1468436087000
  }
]
````

#### Check the job status for a given job ID

Once the new job is submitted, it takes time and resources to run the algorithm on the server. During the waiting, you can check the status of a given job ID:

API Endpoint URI pattern:

````
GET https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/jobs/{id}
````

Generated HTTP request code example:

````
GET /ccd-api/22/jobs/32 HTTP/1.1
Host: ccd4.vm.bridges.psc.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
````

This will either return "Pending" or "Completed".

#### Cancel a running job

Sometimes you may want to cancel a submitted job.

API Endpoint URI pattern:

````
DELETE https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/jobs/{id}
````

Generated HTTP request code example:

````
DELETE /ccd-api/22/jobs/8 HTTP/1.1
Host: ccd4.vm.bridges.psc.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
````

This call will response either "Job 8 has been canceled" or "Unable to cancel job 8". It's not guranteed that the system can always cencal a job successfully.

### 3. Result Management

#### List all result files generated by the algorithm

API Endpoint URI pattern:

````
GET https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/results
````

Generated HTTP request code example:

````
GET /ccd-api/22/results HTTP/1.1
Host: ccd4.vm.bridges.psc.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
````

The response to this request will look like this:

````javascript
[
  {
    "name": "FGESc_sim_data_20vars_100cases.csv_1466171729046.txt",
    "creationTime": 1466171732000,
    "lastModifiedTime": 1466171732000,
    "fileSize": 1660
  },
  {
    "name": "FGESc_data_small.txt_1466172140585.txt",
    "creationTime": 1466172145000,
    "lastModifiedTime": 1466172145000,
    "fileSize": 39559
  }
]
````

#### Download a speific result file generated by the algorithm based on file name

API Endpoint URI pattern:

````
GET https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/results/{result_file_name}
````

Generated HTTP request code example:

````
GET /ccd-api/22/results/FGESc_data_small.txt_1466172140585.txt HTTP/1.1
Host: ccd4.vm.bridges.psc.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
````
On success, you will get the result file back as text file content. If there's a typo in file name of the that file doesn't exist, you'll get either a JSON or XML message based on the `accept` header in your request:

The response to this request will look like this:

````javascript
{
  "timestamp": 1467210996233,
  "status": 404,
  "error": "Not Found",
  "message": "Resource not found.",
  "path": "/22/results/FGESc_data_small.txt_146172140585.txt"
}
````


#### Compare algorithm result files

Since we can list all the algorithm result files, based on the results, we can also choose multiple files and run a comparison. 

API Endpoint URI pattern:

````
POST https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/results/compare
````

The request body is a JSON that contains an array of result files to be compared.

Generated HTTP request code example:

````
POST /ccd-api/22/results/compare HTTP/1.1
Host: ccd4.vm.bridges.psc.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY

{
  "resultFiles": [
    "FGESc_sim_data_20vars_100cases.csv_1466171729046.txt",
    "FGESc_data_small.txt_1467305104859.txt"
  ]
}
````
When you specify multiple file names, use the `!!` as a delimiter. This request will generate a result comparison file with the following content (shortened version):

````
FGESc_sim_data_20vars_100cases.csv_1466171729046.txt	FGESc_data_small.txt_1467305104859.txt
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

API Endpoint URI pattern:

````
GET https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/results/comparisons
````

Generated HTTP request code example:

````
GET /ccd-api/22/results/comparisons HTTP/1.1
Host: ccd4.vm.bridges.psc.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
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

API Endpoint URI pattern:

````
GET https://ccd4.vm.bridges.psc.edu/ccd-api/{userId}/results/comparisons/{comparison_file_name}
````

Generated HTTP request code example:

````
GET /ccd-api/22/results/comparisons/result_comparison_1467388042261.txt HTTP/1.1
Host: ccd4.vm.bridges.psc.edu
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2Nsb3VkLmNjZC5waXR0LmVkdS8iLCJuYW1lIjoiemh5MTkiLCJleHAiOjE0NzU4NTA2NzY4MDQsImlhdCI6MTQ3NTg0NzA3NjgwNH0.8azVEoNPfETczXb-vn7dfyDd98eRt7iiLBXehGpPGzY
````

Then it returns the content of that comparison file (shorted version):

````
FGESc_sim_data_20vars_100cases.csv_1466171729046.txt	FGESc_data_small.txt_1467305104859.txt
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
