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

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class TracyPublisher {
    public TracyPublisher()	{
    }
    
    @SuppressWarnings("unused")
	private String extractPostResponse(CloseableHttpResponse response) throws ParseException, IOException	{
    	StringBuilder sb = new StringBuilder(1024);
    	HttpEntity entity = response.getEntity();
    	sb.append(response.getStatusLine());
    	sb.append(" ");
    	sb.append(EntityUtils.toString(entity, "UTF-8"));
    	return sb.toString();
    }
    
    public boolean publish(String tracySegment) {
    	boolean published = false;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse response;
		HttpPost httpPost = new HttpPost("http://localhost:8080/tracy/segment");
		StringEntity se;
		
		try {
			se = new StringEntity(tracySegment,"UTF-8");
			se.setContentType("application/json");
			httpPost.setEntity(se);
			httpPost.setHeader("Content-Type","application/json;charset=UTF-8");
			response = httpclient.execute(httpPost);
//			System.out.println(extractPostResponse(response));
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)	{
				published = true;
			}
			response.close();
		} catch (Exception e) {
		}
		return published;
    }
}
