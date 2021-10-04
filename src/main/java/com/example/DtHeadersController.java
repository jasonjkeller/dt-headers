/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import com.newrelic.api.agent.ConcurrentHashMapHeaders;
import com.newrelic.api.agent.HeaderType;
import com.newrelic.api.agent.Headers;
import com.newrelic.api.agent.NewRelic;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Controller
class DtHeadersController {
    private static final String URL = "https://example.com/";
    private static final String W3C_TRACE_PARENT_HEADER_KEY = "traceparent";
    private static final String W3C_TRACE_STATE_HEADER_KEY = "tracestate";
    private static final String NEWRELIC_HEADER_KEY = "newrelic";
    private final HttpClient httpClient = HttpClientBuilder.create().build();

    /**
     * Makes a request to an external URI using Apache HttpClient.
     * Distributed tracing headers are automatically by the NR Java agent instrumentation.
     *
     * @param model used by view
     * @return external thymeleaf template
     * @throws IOException in rare cases
     */
    @GetMapping("/external")
    public String external(Model model) throws IOException {
        HttpGet request = new HttpGet(URL);

        // Display request headers before out of the box agent instrumentation is applied.
        model.addAttribute("request_headers_pre", request.getAllHeaders());

        /*
         * NR Java agent will automatically add distributed tracing headers to the request object
         * when httpClient.execute(request) is invoked based on the httpclient-4.0 instrumentation.
         * If you examine the request headers before the instrumented execute method is invoked the
         * headers won't yet be set.
         */
        HttpResponse response = httpClient.execute(request);
        addViewModelAttributes(model, request, response);
        return "external";
    }

    /**
     * Makes a request to an external URI using Apache HttpClient.
     * Distributed tracing headers are automatically by the NR Java agent instrumentation.
     * <p>
     * This is not an expected use case of the DT APIs and results in a problematic scenario
     * where multiple DT headers are reported with conflicting values for a single trace.
     *
     * @param model used by view
     * @return external thymeleaf template
     * @throws IOException in rare cases
     */
    @GetMapping("/external-custom-headers")
    public String externalCustomHeaders(Model model) throws IOException {
        HttpGet request = new HttpGet(URL);

        // ConcurrentHashMapHeaders provides a concrete implementation of com.newrelic.api.agent.Headers
        Headers distributedTraceHeaders = ConcurrentHashMapHeaders.build(HeaderType.HTTP);
        // Generate DT headers and insert them into the distributedTraceHeaders map
        NewRelic.getAgent().getTransaction().insertDistributedTraceHeaders(distributedTraceHeaders);

        // Retrieve the generated DT headers and them to the request object headers
        if (distributedTraceHeaders.containsHeader(W3C_TRACE_PARENT_HEADER_KEY)) {
            // Purposely add the same header multiple times. I wouldn't expect this to be possible but the underlying Apache HeaderGroup
            // allows it: "This class allows for multiple headers with the same name and keeps track of the order in which headers were added."
            request.addHeader(W3C_TRACE_PARENT_HEADER_KEY, distributedTraceHeaders.getHeader(W3C_TRACE_PARENT_HEADER_KEY));
            request.addHeader(W3C_TRACE_PARENT_HEADER_KEY, distributedTraceHeaders.getHeader(W3C_TRACE_PARENT_HEADER_KEY));
            request.addHeader(W3C_TRACE_PARENT_HEADER_KEY, distributedTraceHeaders.getHeader(W3C_TRACE_PARENT_HEADER_KEY));
        }

        if (distributedTraceHeaders.containsHeader(W3C_TRACE_STATE_HEADER_KEY)) {
            // Purposely add the same header multiple times. I wouldn't expect this to be possible but the underlying Apache HeaderGroup
            // allows it: "This class allows for multiple headers with the same name and keeps track of the order in which headers were added."
            request.addHeader(W3C_TRACE_STATE_HEADER_KEY, distributedTraceHeaders.getHeader(W3C_TRACE_STATE_HEADER_KEY));
            request.addHeader(W3C_TRACE_STATE_HEADER_KEY, distributedTraceHeaders.getHeader(W3C_TRACE_STATE_HEADER_KEY));
            request.addHeader(W3C_TRACE_STATE_HEADER_KEY, distributedTraceHeaders.getHeader(W3C_TRACE_STATE_HEADER_KEY));
        }

        if (distributedTraceHeaders.containsHeader(NEWRELIC_HEADER_KEY)) {
            // Purposely add the same header multiple times. I wouldn't expect this to be possible but the underlying Apache HeaderGroup
            // allows it: "This class allows for multiple headers with the same name and keeps track of the order in which headers were added."
            request.addHeader(NEWRELIC_HEADER_KEY, distributedTraceHeaders.getHeader(NEWRELIC_HEADER_KEY));
            request.addHeader(NEWRELIC_HEADER_KEY, distributedTraceHeaders.getHeader(NEWRELIC_HEADER_KEY));
            request.addHeader(NEWRELIC_HEADER_KEY, distributedTraceHeaders.getHeader(NEWRELIC_HEADER_KEY));
        }

        // Display request headers before out of the box agent instrumentation is applied.
        model.addAttribute("request_headers_pre", request.getAllHeaders());

        /*
         * NR Java agent will automatically add distributed tracing headers to the request object
         * when httpClient.execute(request) is invoked based on the httpclient-4.0 instrumentation.
         * If you examine the request headers before the instrumented execute method is invoked the
         * headers won't yet be set.
         */
        HttpResponse response = httpClient.execute(request);
        addViewModelAttributes(model, request, response);
        return "external";
    }

    /**
     * Parses and returns the HttpResponse body as a String.
     *
     * @param response HttpResponse
     * @return String representing the HttpResponse body
     * @throws IOException in rare cases
     */
    private String getResponseBody(HttpResponse response) throws IOException {
        final StringBuilder output = new StringBuilder();
        // Get the response content
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        String line;
        while ((line = rd.readLine()) != null) {
            output.append(line);
        }

        return output.toString();
    }

    /**
     * Add request/response attributes to the Model used by the thymeleaf view.
     *
     * @param model    Model for view
     * @param request  HttpGet
     * @param response HttpResponse
     * @throws IOException in rare cases
     */
    private void addViewModelAttributes(Model model, HttpGet request, HttpResponse response) throws IOException {
        model.addAttribute("external_url", URL);

        // Get request info
        model.addAttribute("request_headers_post", request.getAllHeaders());

        // Get response info
        model.addAttribute("status_code", response.getStatusLine().getStatusCode());
        model.addAttribute("status_message", response.getStatusLine().getReasonPhrase());
        model.addAttribute("response", response);
        model.addAttribute("response_body", getResponseBody(response));
        model.addAttribute("response_headers", response.getAllHeaders());
    }

}
