package com.apm4all.tracy;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TracyPublisherTest {
	TracyPublisher pub;
	
	@Before
	public void setUp() throws Exception {
		pub = new TracyPublisher();
	}

	@Test
	public void test() {
		Tracy.setContext("MyTask", "null", "MyComponent");
		Tracy.before("myLabel1");
		Tracy.before("myLabel1");
		// FIXME: replace with Tracy.getEventsAsJson()
		String tracySegment = Tracy.getEventsAsJson().get(0);
		pub.publish(tracySegment);
		fail("Not yet implemented");
	}

}
