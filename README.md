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

This `GET` request to the endpoint `http://localhost:9000/causal/api/v1.0/usr/zhy19/data` with `Basic Auth` will return a `JSON` format of the list of input data files that is associated with user `zhy19`
