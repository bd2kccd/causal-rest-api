# Causal-Web REST API

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
This will start the API server, and you'll be able to access the API endpoints via the URL of `http://localhost:[port]/causal/api/`

# Usage Examples

This API requires user to be authenticated. Before using this API, the user will need to go to [Causal-Web App](https://dxcvm26.psc.edu/ccd/login) and create an account. After that, the username and password can be used to authenticate against the REST API via HTTP Basic Auth. The username will need to be specified in the requesting URI and password provided in the HTTP request `Authorization` header.

Since this API is developed with Jersey, which supports [WADL](https://en.wikipedia.org/wiki/Web_Application_Description_Language). So you can view the generated WADL by going to `http://localhost:9000/causal/api/v1.0/application.wadl?detail=true` and see all resource available in the application. And below are some examples.

## Example 1

````
GET /causal/api/usr/zhy19/data HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
Accept: application/json
````

This `GET` request to the endpoint `http://localhost:9000/causal/api/usr/zhy19/data` with `Basic Auth` will return a `JSON` formatted list of all the input data files that are associated with user `zhy19`

````
[
  {
    "id": 88,
    "name": "121_2016-5-24.csv",
    "creationTime": 1464115438000,
    "lastModifiedTime": 1464115438000,
    "fileSize": 35843
  },
  {
    "id": 89,
    "name": "123_2016-5-26.csv",
    "creationTime": 1464269440000,
    "lastModifiedTime": 1464269440000,
    "fileSize": 33697
  },
  {
    "id": 90,
    "name": "124_2016-5-26.json",
    "creationTime": 1464270215000,
    "lastModifiedTime": 1464270215000,
    "fileSize": 298285
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
GET /causal/api/usr/zhy19/data HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
Accept: application/xml
````

And the response will look like this:

````
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<dataFileDTOes>
    <dataFile>
        <id>88</id>
        <name>121_2016-5-24.csv</name>
        <creationTime>2016-05-24T14:43:58-04:00</creationTime>
        <lastModifiedTime>2016-05-24T14:43:58-04:00</lastModifiedTime>
        <fileSize>35843</fileSize>
    </dataFile>
    <dataFile>
        <id>89</id>
        <name>123_2016-5-26.csv</name>
        <creationTime>2016-05-26T09:30:40-04:00</creationTime>
        <lastModifiedTime>2016-05-26T09:30:40-04:00</lastModifiedTime>
        <fileSize>33697</fileSize>
    </dataFile>
    <dataFile>
        <id>90</id>
        <name>124_2016-5-26.json</name>
        <creationTime>2016-05-26T09:43:35-04:00</creationTime>
        <lastModifiedTime>2016-05-26T09:43:35-04:00</lastModifiedTime>
        <fileSize>298285</fileSize>
    </dataFile>
</dataFileDTOes>
````

## Example 2

You can also query the data file info for a given file id

````
GET /causal/api/usr/zhy19/data/id/88 HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
````

And the resulting response looks like this:

````
{
  "id": 88,
  "name": "121_2016-5-24.csv",
  "creationTime": 1464115438000,
  "lastModifiedTime": 1464115438000,
  "fileSize": 35843
}
````


## Example 3

Delete physical data file and all records from database for a given file id

````
DELETE /causal/api/usr/zhy19/data/id/88 HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
````

And this will result a HTTP 204 No Content status in response on success, which means the server successfully processed the deletion request but there's no content to response.