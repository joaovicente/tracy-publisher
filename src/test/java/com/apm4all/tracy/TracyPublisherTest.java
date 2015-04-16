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
	// TODO: Find an unused TCP port at test time (in case 8050 is being used by something else)
	final int PORT = 8050;
	Tomcat tomcat;
	
	@Before
	public void setUp() throws Exception {
		new TracyPublisherFactory.Builder(TracyPublisherFactory.Type.HTTP_CLIENT)
			.hostname(HOSTNAME)
			.port(PORT)
			.waitForResponse(true)
//			.debug(true)
			.build();
		// Start Tomcat
		tomcat = new Tomcat();
		tomcat.setPort(PORT);
		File base = new File(System.getProperty("java.io.tmpdir"));
		Context rootCtx = tomcat.addContext("/tracy", base.getAbsolutePath());
		Tomcat.addServlet(rootCtx, "segment", new TracySegmentLightServlet());
		rootCtx.addServletMapping("/segment", "segment");
		tomcat.start();
	}
	
	@After
	public void tearDown() throws LifecycleException, InterruptedException {
		tomcat.stop();
		tomcat.destroy();
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
		Tracy.setContext("MyTask", "null", "MyComponent");
		Tracy.before("myLabel");
		Tracy.after("myLabel");
		String tracySegment = Tracy.getEventsAsJsonTracySegment();
		System.out.println("Publishing Tracy segment");
		
		assertTrue(TracyPublisherFactory.getInstance().publish(tracySegment));
		Tracy.clearContext();
		Thread.sleep(1000);
	}

	@Test
	public void testMultipleTracySegmentPost() throws LifecycleException, ClientProtocolException, IOException, InterruptedException, ExecutionException {
		// FIXME: replace with Tracy.getEventsAsJson()
		String labelA, labelB;
		String tracySegment;
		int i = 0;
		while (i<1000) {
			labelA = "label-a-" + Integer.toString(i);
			labelB = "label-b-" + Integer.toString(i);
			Tracy.setContext("MyTask", "null", "MyComponent");
			Tracy.before(labelA);
			Tracy.after(labelA);
			Tracy.before(labelB);
			Tracy.after(labelB);
			tracySegment = Tracy.getEventsAsJsonTracySegment();
			assertTrue(TracyPublisherFactory.getInstance().publish(tracySegment));
			i++;
			Tracy.clearContext();
			if (i % 100 == 0)	{
				System.out.println("Publishing Tracy segment " + i);
			}
		}
		Thread.sleep(1000);
	}
}
