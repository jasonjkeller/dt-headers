# Distributed Tracing Headers
Spring boot app that makes an external HTTP call using the Apache HttpClient and renders an HTML page showing Request/Response info, including the headers on both.

## Usage
Add the New Relic Java agent when starting the service:
```shell
-javaagent:/path/to/newrelic/newrelic.jar
```

The Java agent automatically starts a transaction for each annotated Spring controller route.

### Normal Use Case
This endpoint makes a single external HTTP call. The agent automatically instruments the Apache HttpClient and adds distributed tracing headers once
`client.execute(...)` is invoked.

```http request
http://localhost:8080/external
```

### Irregular Use Case
This endpoint makes a single external HTTP call. Like the normal use case, the agent automatically instruments the Apache HttpClient and adds distributed
tracing headers once `client.execute(...)` is invoked, however, in addition, this case uses the New Relic DT APIs to manually add multiple copies of DT headers
to the Request object prior to the instrumentation applying. This results in multiple copies of the same DT headers with different values being reported for a
single trace.

```http request
http://localhost:8080/external-custom-headers
```
