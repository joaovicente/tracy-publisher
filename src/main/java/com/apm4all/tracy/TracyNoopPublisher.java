package com.apm4all.tracy;

public class TracyNoopPublisher implements TracyPublisher {

	@Override
	public boolean publish(String tracySegment) {
		return true;
	}
}
