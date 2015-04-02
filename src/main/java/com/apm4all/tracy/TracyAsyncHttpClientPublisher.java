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
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;


public class TracyAsyncHttpClientPublisher implements AutoCloseable {
	static final String TRACY_CONTENT_TYPE = MediaType.APPLICATION_JSON 
			+ ";charset=" + StandardCharsets.UTF_8;
	public static boolean WAIT_FOR_RESPONSE = true;
	public static boolean DONT_WAIT_FOR_RESPONSE = false;
	private String uri;
	CloseableHttpAsyncClient httpClient;
	
	// Construction without parameters means noop
    public TracyAsyncHttpClientPublisher()	{
    	uri = null;
    }
    
    public TracyAsyncHttpClientPublisher(String hostname, int port) {
    	uri = "http://" + hostname + ":" + port + "/tracy/segment";
        this.httpClient = HttpAsyncClients.custom().build();
        this.httpClient.start();
	}

	@SuppressWarnings("unused")
	private String extractPostResponse(HttpResponse response) throws ParseException, IOException	{
    	StringBuilder sb = new StringBuilder(1024);
    	HttpEntity entity = response.getEntity();
    	sb.append(response.getStatusLine());
    	sb.append(" ");
    	sb.append(EntityUtils.toString(entity, StandardCharsets.UTF_8));
		EntityUtils.consume(entity);
    	return sb.toString();
    }
   
    public boolean publish(String tracySegment, boolean waitForResponse) {
    	boolean published = true;
    	if (null != this.uri) {
    		HttpPost httpPost = new HttpPost(uri);
    		StringEntity se;
    		try {
				se = new StringEntity(tracySegment, StandardCharsets.UTF_8);
				se.setContentType(MediaType.APPLICATION_JSON);
				httpPost.setEntity(se);
				httpPost.setHeader(HttpHeaders.CONTENT_TYPE,TRACY_CONTENT_TYPE);
				Future<HttpResponse> future = httpClient.execute(httpPost, null);
				if (waitForResponse)	{
					HttpResponse response = future.get();
					published = (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
//					System.out.println(extractPostResponse(response));
				}
			} catch (Exception e) {
			}
    	}
		return published;
    }

	public void close() throws Exception {
		this.httpClient.close();
	}
}
