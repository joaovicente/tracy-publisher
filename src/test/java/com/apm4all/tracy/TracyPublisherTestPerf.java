package com.apm4all.tracy;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.Required;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class TracyPublisherTestPerf {
	final String HOSTNAME = "localhost";
	// TODO: Find an unused TCP port at test time (in case 8050 is being used by something else)
	final int PORT = 8050;
	Tomcat tomcat;

	@Before
	public void setUp() throws Exception {

		new TracyPublisherFactory.Builder(TracyPublisherFactory.Type.HTTP_CLIENT)
			.hostname(HOSTNAME)
			.port(PORT)
			// waitForResponse allows testing publish() received a 2xx http status
//			.waitForResponse(true) 
			.debug(true)
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
	public void tearDown() throws Exception {
		tomcat.stop();
		tomcat.destroy();
	}

    @Rule
    public ContiPerfRule i = new ContiPerfRule();

    @PerfTest(threads=2, duration=5000, rampUp = 100)
    @Required(average = 1, percentile99=1, max = 2000)
	@Test
	public void testPublish() {
		String tracySegment;
		Tracy.setContext("MyTask", "null", "MyComponent");
		Tracy.before("label-a");
		Tracy.after("label-a");
		Tracy.before("label-b");
		Tracy.after("label-b");
		tracySegment = Tracy.getEventsAsJsonTracySegment();
		assertTrue(TracyPublisherFactory.getInstance().publish(tracySegment));
		Tracy.clearContext();
	}
}
