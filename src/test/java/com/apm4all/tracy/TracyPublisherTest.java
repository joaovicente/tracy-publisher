package com.apm4all.tracy;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TracyPublisherTest {
	final String HOSTNAME = "localhost";
	final int PORT = 8050;
	TracyAsyncHttpClientPublisher pub;
//	TracyCloseableHttpClientPublisher pub;
	Tomcat tomcat;
	
	@Before
	public void setUp() throws Exception {
		pub = new TracyAsyncHttpClientPublisher(HOSTNAME, PORT);
//		pub = new TracyCloseableHttpClientPublisher(HOSTNAME, PORT);
		// Start Tomcat
		tomcat = new Tomcat();
		tomcat.setPort(PORT);
		File base = new File(System.getProperty("java.io.tmpdir"));
		Context rootCtx = tomcat.addContext("/tracy", base.getAbsolutePath());
		Tomcat.addServlet(rootCtx, "segment", new TracySegmentLightServlet());
		rootCtx.addServletMapping("/segment", "segment");
		tomcat.start();
//		tomcat.getServer().await();
	}
	
	@After
	public void tearDown() throws LifecycleException, InterruptedException {
		tomcat.stop();
		tomcat.destroy();
//		Thread.sleep(5000);
	}

	@SuppressWarnings("unused")
	private String post(String tracySegment) throws ClientProtocolException, IOException	{
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost("http://localhost:8050/tracy/segment");
		StringEntity se=new StringEntity(tracySegment,"UTF-8");
		se.setContentType("application/json");
		httpPost.setEntity(se);
		httpPost.setHeader("Content-Type","application/json;charset=UTF-8");
		String responseString;
		
		CloseableHttpResponse response = httpclient.execute(httpPost);
		
		try {
		    System.out.println(response.getStatusLine());
		    HttpEntity entity = response.getEntity();
		    responseString = EntityUtils.toString(entity, "UTF-8");
		    System.out.println(responseString);
		} finally {
		    response.close();
		}
		return(responseString);
	}
	
	@Test
	public void testSingleTracySegmentPost() throws LifecycleException, ClientProtocolException, IOException, InterruptedException, ExecutionException {
		System.out.println("testSingleTracySegmentPost");
		Tracy.setContext("MyTask", "null", "MyComponent");
		Tracy.before("myLabel1");
		Tracy.after("myLabel1");
		// FIXME: replace with Tracy.getEventsAsJson()
		String tracySegment = Tracy.getEventsAsJson().get(0);
		assertTrue(pub.publish(tracySegment, TracyAsyncHttpClientPublisher.WAIT_FOR_RESPONSE));
		Tracy.clearContext();
		Thread.sleep(1000);
	}

	@Test
	public void testMultipleTracySegmentPost() throws LifecycleException, ClientProtocolException, IOException, InterruptedException, ExecutionException {
		// FIXME: replace with Tracy.getEventsAsJson()
		String label;
		String tracySegment;
		
		System.out.println("testMultipleTracySegmentPost");
		int i = 0;
		while (i<1000) {
			label = "label-" + Integer.toString(i);
			Tracy.setContext("MyTask", "null", "MyComponent");
			Tracy.before(label);
			Tracy.after(label);
			tracySegment = Tracy.getEventsAsJson().get(0);
			assertTrue(pub.publish(tracySegment, TracyAsyncHttpClientPublisher.WAIT_FOR_RESPONSE));
			i++;
			Tracy.clearContext();
			System.out.println("Publishing Tracy segment " + i);
		}
		Thread.sleep(5000);
	}
}
