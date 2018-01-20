# Dynamock Server
A mock-server designed to replicate the classic unit-test mocking experience. Setup an API expectation and response and receive the registered response when an API call matching the registered expectation is made. 

###### Basic Usage
When designing automated tests for a service with external web dependencies simply:
1. [Spin-up](#deployment) a DynamockServer instance.
1. Configure the hosts and ports for the dependent services on the service under test, to point to the Dynamock Server.
1. Setup the expected API calls along with desired responses. (see [PUT /expectations](#put-expectations) or [POST /expectations/load](#post-expectationsload))
1. Run your tests, i.e. make http requests to DynamockServer as if it were the dependent service of interest. When a request matches a setup expectation DynamockServer will respond with the registered response. 

## Deployment
- Ensure Java 8 or higher is installed.
- Download the JAR file of the latest [release](releases/README.md).
- Run `java -jar DynamockServer-x.y.z.jar [-http.port=:<port-number>] [-dynamock.path.base=<dynamock-path-base>]`, where `x.y.z` is the version number. The optional arguments are as follows:
    - **http.port**: An integer in the range [2, 65534], prefixed with `:`, specifying the http port the server runs on. For example, providing `-http.port=:1234` deploys a Dynamock instance listening on port `1234`. If not provided this value defaults to `:8888`. This feature can be used to deploy multiple DynamockServer instances for different consumers, to avoid collisions. 
    - **dynamock.path.base**: This value prefixes Dynamock API url-paths. For example, `-dynamock.path.base=dynamock/test` or `-dynamock.path.base=/dynamock/test` results in a net url path `/dynamock/test/expectations` for the Dynamock API url-path `<dynamock-path-base>/expectations`. This feature can be used to avoid collisions on mocked http requests and the dynamock API.  

## Dynamock API

### PUT <dynamock-path-base>/expectations
Setup a mocked response by registering an expectation and the response to return when the expectation is positively matched. 

**Content-Type:** application/json

**Request Body Parameters:**
- expectation_responses: Array of [NamedExpectationResponse](#namedexpectationresponse-object) Objects

**Response Body Parameters:**
- expectations_info: Array of [ExpectationInfo](#expectationinfo-object) Objects

###### Example Request Body:

    {
        "expectation_responses": [{
            "expectation_name": "Some value that is meaningful to the client",
            "expectation": {
                "method": "POST",
                "path": "/some/url/path",
                "query_parameters": {
                    "some_query_parm": "SomeValue"
                },
                "included_header_parameters": {
                    "some_included_header_param": "SomeValue"
                },
                "excluded_header_parameters": {
                    "some_excluded_header_param": "SomeValue"
                },
                "content": "Some Content (Possibly Json wrapped in a string)"
            },
            "response": {
                "status": 200,
                "content": "Some Content",
                "header_map": {
                    "some_header_param": "SomeValue"
                }
            }
        }]
    }

### DELETE <dynamock-path-base>/expectations
Clear all registered mock setups.

### GET <dynamock-path-base>/expectations
List all registered mock setups.

**Response Body Parameters:**
- expectation_responses: Array of [ExpectationResponse](#expectationresponse-object) Objects

### POST <dynamock-path-base>/expectations-suite/store
Save the state of registered expectations into an expectations-suite that can be restored at a later point in time.

**Query Parameters:**
- suite_name: Name of the expectations-suite.

### POST <dynamock-path-base>/expectations-suite/load
Restore the state of registered expectations to a stored expectations-suite.

**Query Parameters:**
- suite_name: Name of the expectations-suite.

----------------------------------------------

### Definitions
##### NamedExpectationResponse Object:
- properties:
    - expectation_name:
        - type: String
        - required: true
        - description: A value for the client to associate the resulting expectation id with the provided expectation.  
    - expectation:
        - type: [Expectation](#expectation-object) Object
        - required: true
    - response:
        - type: [Response](#response-object) Object
        - required: true
        
##### ExpectationInfo Object:
- properties:
    - expectation_name:
        - type: String
        - required: true
        - description: The value provided in the request with the associated expectation.
    - expectation_id:
        - type: String
        - required: true
        - description: The unique id assigned to the expectation provided in the request.
    - did_overwrite_response:
        - type: boolean
        - required: true
        - description: Indicates if the response provided overwrites a response previously registered with the expectation provided.

##### ExpectationResponse Object:
- properties:  
    - expectation:
        - type: [Expectation](#expectation-object) Object
        - required: true
    - response:
        - type: [Response](#response-object) Object
        - required: true

##### Expectation Object:
- properties:
    - method:
        - type: string
        - description: The HTTP method of the expected request (i.e. GET, PUT, POST, ect.).
        - required: true
        - matching rule: Exact case-insensitive match with the incoming request. 
    - path:
        - type: string
        - description: The url path of the expected request.
        - required: true
        - matching rule: Exact case-sensitive match.
    - queryParameters:
        - type: map of string to string
        - description: The url/query parameters of the expected request.
        - required: false, when not specified it is treated as if an empty map is provided.
        - matching rule: Exact case-sensitive match on all key-value pairs.
    - includedHeaderParameters:
        - type: map of string to string
        - description: Header parameters expected to be included in the request.
        - required: false, when not specified it is treated as if an empty map is provided.
        - matching rule: Exact case-sensitive match on all key-value pairs. A positive match occurs when all of the specified key-value pairs are found in the request's header map.
    - excludedHeaderParameters:
        - type: map of string to string
        - description: Header parameters expected to be excluded from the request.
        - required: false, when not specified it is treated as if an empty map is provided.
        - matching rule: Exact case-sensitive match on all key-value pairs. A positive match occurs when none of the specified key-value pairs are found in the request's header map.
    - content:
        - type: string
        - description: The string content expected to be included in the request.
        - required: false, when not specified it is treated as if an empty string is provided.
        - matching rule: If the string is valid Json then a positive match occurs on a request with equivalent Json, Json property names are matched case-sensitive. When the specified content is not valid Json then a positive match occurs on exact case-sensitive match.
        
##### Response Object:
- properties:
    - status:
        - type: integer
        - description: The Http status code of the response. 
        - required: true
    - content:
        - type: string
        - description: The body of the response.
        - required: false, when not specified it is treated as if an empty string is provided.
    - headerMap:
        - type: map of string to string
        - description: Header parameters to be included in the response's header map.
        - required: false, when not specified it is treated as if an empty map is provided.

## Planned work
- Expectation endpoints return id info.
- Targeted expectation deletion.
- `/expectation-suite/list` endpoint
- `/expectation-suite` DELETE endpoint
- Expectation hit-count support, for validating the number of times an expectation is matched.
- Regex matching on expectation matching.
