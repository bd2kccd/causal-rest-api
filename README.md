# Causal-Web REST API

This RESTful API is designed for causal web.

# Dependencies

You'll need to download [ccd-db-0.6.1](https://github.com/bd2kccd/ccd-db) branch and run `mvn clean install` before building the `causal-rest-api`.

# Usage Examples

````
GET /causal/api/v1.0/usr/zhy19/data HTTP/1.1
Host: localhost:9000
Authorization: Basic emh5MTk6MTIzNDU2
Accept: application/json
````

This `GET` request to the endpoint `http://localhost:9000/causal/api/v1.0/usr/zhy19/data` with `Basic Auth` will return a `JSON` formatted list of all the input data files that are associated with user `zhy19`

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
GET /causal/api/v1.0/usr/zhy19/data HTTP/1.1
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

