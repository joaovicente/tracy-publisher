/*
 * Copyright 2015 Joao Vicente
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.apm4all.tracy;

import java.io.IOException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.nio.charset.StandardCharsets;

public class TracyAsyncHttpClientPublisher {
	static final String TRACY_CONTENT_TYPE = MediaType.APPLICATION_JSON 
			+ ";charset=" + StandardCharsets.UTF_8;
	private String uri;
	CloseableHttpClient httpClient;
	
	// Construction without parameters means noop
    public TracyAsyncHttpClientPublisher()	{
    	uri = null;
    }
    
    public TracyAsyncHttpClientPublisher(String hostname, int port) {
    	uri = "http://" + hostname + ":" + port + "/tracy/segment";
    	httpClient = HttpClients.createDefault();
	}

	@SuppressWarnings("unused")
	private String extractPostResponse(CloseableHttpResponse response) throws ParseException, IOException	{
    	StringBuilder sb = new StringBuilder(1024);
    	HttpEntity entity = response.getEntity();
    	sb.append(response.getStatusLine());
    	sb.append(" ");
    	sb.append(EntityUtils.toString(entity, StandardCharsets.UTF_8));
		EntityUtils.consume(entity);
    	return sb.toString();
    }
    
    public boolean publish(String tracySegment) {
    	boolean published = false;
    	if (null != this.uri) {
    		CloseableHttpResponse response;
    		// TODO: Externalize configuration
    		HttpPost httpPost = new HttpPost(uri);
    		StringEntity se;

    		try {
    			se = new StringEntity(tracySegment, StandardCharsets.UTF_8);
    			se.setContentType(MediaType.APPLICATION_JSON);
    			httpPost.setEntity(se);
    			httpPost.setHeader(HttpHeaders.CONTENT_TYPE,TRACY_CONTENT_TYPE);
    			response = httpClient.execute(httpPost);
    			System.out.println(extractPostResponse(response));
    			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)	{
    				published = true;
    			}
    			response.close();
    		} catch (Exception e) {
    		}
    	}
		return published;
    }
}
