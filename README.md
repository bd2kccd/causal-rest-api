# Causal-Web REST API V1

This RESTful API is designed for causal web. And it implements the [JAX-RS](https://en.wikipedia.org/wiki/Java_API_for_RESTful_Web_Services) specifications using Jersey.

# Dependencies

If you want to run this API server and expose the API to your users, you'll first need to have the [Causal Web Application](https://github.com/bd2kccd/causal-web) installed and running. Your API users will use this web app to create their user accounts before they can consume the API.

In order to build the API server, you'll need the released version of [ccd-commons-0.3.0](https://github.com/bd2kccd/ccd-commons/releases/tag/v0.3.0) by going to the repo and checkout this specific release version:

````
git clone https://github.com/bd2kccd/ccd-commons.git
cd ccd-commons
git checkout tags/v0.3.0
mvn clean install
````

You'll also need to download [ccd-db-0.6.1](https://github.com/bd2kccd/ccd-db) branch:

````
git clone https://github.com/bd2kccd/ccd-db.git
cd ccd-db
git checkout v0.6.1
mvn clean install
````

Then you can go get and install `causal-rest-api`:

````
git clone https://github.com/bd2kccd/causal-rest-api.git
cd causal-rest-api
mvn clean package
````

# Configuration

There are 4 configuration files to configure located at `causal-rest-api/src/main/resources`:
- **application-hsqldb.properties**: HSQLDB database configurations (for testing only).
- **application-mysql.properties**: MySQL database configurations
- **application.properties**: Spring Boot application settings
- **causal.properties**: Data file directory path and folder settings

# Start the API Server

Once you have all the settings configured, go to `causal-rest-api/target` and you will find the jar file named `causal-rest-api.jar`. Then simply run 

```java
java -jar causal-rest-api.jar
```
This will start the API server, and you'll be able to access the API endpoints via the URL of `http://localhost:[port]/causal/api/v1/`

# Usage Examples

This API requires user to be authenticated. Before using this API, the user will need to go to [Causal-Web App](https://dxcvm26.psc.edu/ccd/login) and create an account. After that, the username and password can be used to authenticate against the REST API via HTTP Basic Auth. The username will need to be specified in the requesting URI and password provided in the HTTP request `Authorization` header.

Since this API is developed with Jersey, which supports [WADL](https://en.wikipedia.org/wiki/Web_Application_Description_Language). So you can view the generated WADL by going to `http://localhost:9000/causal/api/v1/application.wadl?detail=true` and see all resource available in the application. And below are some examples.

## Example 1: List all data files of a user

````
GET /causal/api/v1/usr/zhy19/data HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
Accept: application/json
````

This `GET` request to the endpoint `http://localhost:9000/causal/api/v1/usr/zhy19/data` with `Basic Auth` will return a `JSON` formatted list of all the input data files that are associated with user `zhy19`

````
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

If the Authorization header is not provided, the response will look like this:

````
{
  "timestamp": 1465414501443,
  "status": 401,
  "error": "Unauthorized",
  "message": "User credentials are required.",
  "path": "/usr/zhy19/data"
}
````

You can also specify the response format to XML in your request

````
GET /causal/api/v1/usr/zhy19/data HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
Accept: application/xml
````

And the response will look like this:

````
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

## Example 2: Get the deatil information of a data file based on ID

You can also query the data file info for a given file id

````
GET /causal/api/v1/usr/zhy19/data/id/8 HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
````

And the resulting response looks like this:

````
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


## Example 3: Delete physical data file and all records from database for a given file ID


````
DELETE /causal/api/v1/usr/zhy19/data/id/8 HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
````

And this will result a HTTP 204 No Content status in response on success, which means the server successfully processed the deletion request but there's no content to response.

## Example 4: List all result files generated by the algorithm

````
GET /causal/api/v1/usr/zhy19/results/algorithm HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
````

The response to this request will look like this:

````
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

## Example 5: Download a speific result file generated by the algorithm based on file name

````
GET /causal/api/v1/usr/zhy19/results/algorithm/fgs_data_small.txt_1466172140585.txt HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
````
On success, you will get the result file back as text file content. If there's a typo in file name of the that file doesn't exist, you'll get either a JSON or XML message based on the `accept` header in your request:

The response to this request will look like this:

````
{
  "timestamp": 1467210996233,
  "status": 404,
  "error": "Not Found",
  "message": "Resource not found.",
  "path": "/usr/zhy19/results/algorithm/fgs_data_small.txt_146172140585.txt"
}
````

## Example 6: Upload small data file


````
POST /causal/api/v1/usr/zhy19/data/upload HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW

----WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="file"; filename=""
Content-Type: 


----WebKitFormBoundary7MA4YWxkTrZu0gW
````

This POST request will upload the data file to the target server location and add corresponding records into database. And the response will contain the following pieces:

````
{
  "id": 6,
  "name": "Lung-tetrad_hv.txt",
  "creationTime": 1466622267000,
  "lastModifiedTime": 1466622267000,
  "fileSize": 3309465,
  "md5checkSum": "b1db7511ee293d297e3055d9a7b46c5e"
}
````

## Example 7: Resumable data file upload

In addition to the regular file upload described in Example 6, we also provide the option of stable and resumable large file upload. It requires the client side to have a resumable upload implementation. We currently support client integrated with R[esumable.js](http://resumablejs.com/), whihc provides multiple simultaneous, stable 
and resumable uploads via the HTML5 File API.

In this example, the data file is splited into 3 chunks. The upload of each chunk consists of a GET request and a POST request. 

````
GET /causal/api/v1/usr/zhy19/data/chunkUpload?resumableChunkNumber=2&resumableChunkSize=1048576&resumableCurrentChunkSize=1048576&resumableTotalSize=3309465&resumableType=text%2Fplain&resumableIdentifier=3309465-large-datatxt&resumableFilename=large-data.txt&resumableRelativePath=large-data.txt&resumableTotalChunks=3 HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
````

This GET request checks if the data chunk is already on the server side. If nothing there, the client will issue another POST request to upload the actual data.

````
POST /causal/api/v1/usr/zhy19/data/chunkUpload HTTP/1.1
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

## Example 8: Summarize data file

So from the second example we can tell that file with ID 10 doesn't have `variableType`, `fileDelimiter`, `numOfRows`, and `numOfColumns` specified under `fileSummary`. Among these attributes, variableType` and `fileDelimiter` are the ones that users will need to provide during this summarization process.

Before we can go ahead to run the desired algorithm with the newly uploaded data file, we'll need to summarize the data by specifing the variable type and file delimiter.

| Required Fields | Description |
| --- | --- |
| id | The data file ID |
| variableType | discrete or continuous |
| fileDelimiter | tab or comma |

````
POST /causal/api/v1/usr/zhy19/data/summarize HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
Content-Type: application/x-www-form-urlencoded

id=10&variableType=continuous&fileDelimiter=tab
````

This POST request will summarize the data file and generate a response (JSON or XML) like below:

````
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