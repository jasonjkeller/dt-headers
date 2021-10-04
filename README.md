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

This results in correct DT headers being added to the Request by the agent `httpclient-4.0` instrumentation applies:

```
Request Headers Pre Agent Instrumentation

Request Headers Post Agent Instrumentation
tracestate: 1939595@nr=0-0-2212864-12345678909-a44b269bcf5c4929-5281783e494f91d5-1-1.161721-1633389144094
traceparent: 00-fff74532138a29908c11b23130d5972a-1234567890c4929-01
newrelic: eyJkIjp7ImFjIjoiMjIxMjg2NCIsInByIjoxLjE2MTcyMSwid1234567890gxNzgzZTQ5NGY5MWQ1IiwidGkiOjE2MzMzODkxNDQwOTQsInR5IjoiQXBwIiwidGsiOiIxOTM5NTk1IiwiaWQiOiJhNDRiMjY5YmNmNWM0OTI5IiwidHIiOiJmZmY3NDUzMjEzOGEyOTkwOGMxMWIyMzEzMGQ1OTcyYSIsInNhIjp0cnVlLCJhcCI6IjE3NjQyNzM0NTkifSwidiI6WzAsMV19
```

### Irregular Use Case
This endpoint makes a single external HTTP call. Like the normal use case, the agent automatically instruments the Apache HttpClient and adds distributed
tracing headers once `client.execute(...)` is invoked, however, in addition, this case uses the New Relic DT APIs to manually add multiple copies of DT headers
to the Request object prior to the instrumentation applying. This results in multiple copies of the same DT headers with different values being reported for a
single trace.

```http request
http://localhost:8080/external-custom-headers
```

This results in three copies of the same DT headers being added to the Request by calling the agent APIs three times and 
then one of those entries being overwritten by different values after the `httpclient-4.0` instrumentation applies:

```
Request Headers Pre Agent Instrumentation
traceparent: 00-0525c6cf4db0123456789268ec34e9-54b742f99a6d0fe9-01
traceparent: 00-0525c6cf4db0123456789268ec34e9-54b742f99a6d0fe9-01
traceparent: 00-0525c6cf4db0123456789268ec34e9-54b742f99a6d0fe9-01
tracestate: 1939595@nr=0-0-2212864-1764273459-54b742f99a6d0fe9-a478fba5b58ef588-1-1.483293-1633389111715
tracestate: 1939595@nr=0-0-2212864-1764273459-54b742f99a6d0fe9-a478fba5b58ef588-1-1.483293-1633389111715
tracestate: 1939595@nr=0-0-2212864-1764273459-54b742f99a6d0fe9-a478fba5b58ef588-1-1.483293-1633389111715
newrelic: eyJkIjp7ImFjIjoiMjIxMjg2NCIsInByIjoxLjQ4MzI5MywidHgiOiJhNDc4ZmJhNWI1OGVmNTg4IiwidGkiOjE2MzMzODkxMTE3MTUsInR5IjoiQXBwIiwidGsiOiIxOTM5NTk1IiwiaWQiOiI1NGI3ND0123456789IiwidHIiOiIwNTI1YzZjZjRkY0123456789OGVjMzRlOSIsInNhIjp0cnVlLCJhcCI6IjE3NjQyNzM0NTkifSwidiI6WzAsMV19
newrelic: eyJkIjp7ImFjIjoiMjIxMjg2NCIsInByIjoxLjQ4MzI5MywidHgiOiJhNDc4ZmJhNWI1OGVmNTg4IiwidGkiOjE2MzMzODkxMTE3MTUsInR5IjoiQXBwIiwidGsiOiIxOTM5NTk1IiwiaWQiOiI1NGI3ND0123456789IiwidHIiOiIwNTI1YzZjZjRkY0123456789OGVjMzRlOSIsInNhIjp0cnVlLCJhcCI6IjE3NjQyNzM0NTkifSwidiI6WzAsMV19
newrelic: eyJkIjp7ImFjIjoiMjIxMjg2NCIsInByIjoxLjQ4MzI5MywidHgiOiJhNDc4ZmJhNWI1OGVmNTg4IiwidGkiOjE2MzMzODkxMTE3MTUsInR5IjoiQXBwIiwidGsiOiIxOTM5NTk1IiwiaWQiOiI1NGI3ND0123456789IiwidHIiOiIwNTI1YzZjZjRkY0123456789OGVjMzRlOSIsInNhIjp0cnVlLCJhcCI6IjE3NjQyNzM0NTkifSwidiI6WzAsMV19

Request Headers Post Agent Instrumentation
traceparent: 00-0525c6cf4db0123456789268ec34e9-ff3fe19f598f1f55-01
traceparent: 00-0525c6cf4db0123456789268ec34e9-54b742f99a6d0fe9-01
traceparent: 00-0525c6cf4db0123456789268ec34e9-54b742f99a6d0fe9-01
tracestate: 1939595@nr=0-0-2212864-1764273459-54b742f99a6d0fe9-a478fba5b58ef588-1-1.483293-1633389111715
tracestate: 1939595@nr=0-0-2212864-1764273459-54b742f99a6d0fe9-a478fba5b58ef588-1-1.483293-1633389111715
tracestate: 1939595@nr=0-0-2212864-1764273459-54b742f99a6d0fe9-a478fba5b58ef588-1-1.483293-1633389111715
newrelic: eyJkIjp7ImFjIjoiMjIxMjg2NCIsInByIjoxLjQ4MzI5MywidHgiOiJhNDc4ZmJhNWI1OGVmNTg4IiwidGkiOjE2MzMzODkxMTE3MjIsInR5IjoiQXBwIiwidGsiOiIxOTM5NTk1IiwiaWQiOiJmZjNmZT0123456789jU1IiwidHIiOiIwNTI1YzZjZjRkYjgyZDQ1MWZmMGFkMSIsInNhIjp0cnVlLCJhcCI6IjE3NjQyNzM0NTkifSwidiI6WzAsMV19
newrelic: eyJkIjp7ImFjIjoiMjIxMjg2NCIsInByIjoxLjQ4MzI5MywidHgiOiJhNDc4ZmJhNWI1OGVmNTg4IiwidGkiOjE2MzMzODkxMTE3MTUsInR5IjoiQXBwIiwidGsiOiIxOTM5NTk1IiwiaWQiOiI1NGI3ND0123456789IiwidHIiOiIwNTI1YzZjZjRkY0123456789OGVjMzRlOSIsInNhIjp0cnVlLCJhcCI6IjE3NjQyNzM0NTkifSwidiI6WzAsMV19
newrelic: eyJkIjp7ImFjIjoiMjIxMjg2NCIsInByIjoxLjQ4MzI5MywidHgiOiJhNDc4ZmJhNWI1OGVmNTg4IiwidGkiOjE2MzMzODkxMTE3MTUsInR5IjoiQXBwIiwidGsiOiIxOTM5NTk1IiwiaWQiOiI1NGI3ND0123456789IiwidHIiOiIwNTI1YzZjZjRkY0123456789OGVjMzRlOSIsInNhIjp0cnVlLCJhcCI6IjE3NjQyNzM0NTkifSwidiI6WzAsMV19
```
