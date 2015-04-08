package com.apm4all.tracy;

public class TracyPublisherFactory {
	static private TracyPublisher tracyPublisher = null;
	static public enum Type {
		NOOP,
		HTTP_CLIENT,
		HTTP_ASYNC_CLIENT
		// TODO: ,LOG_FILE
		}
	
	public static TracyPublisher getInstance()	{
		// This method should only be called after the TracyPublisher is built
		
		if (tracyPublisher != null)	{
			return tracyPublisher;
		}
		else	{
			tracyPublisher = new TracyNoopPublisher();
			
		}
		return tracyPublisher;
	}
	
	public static class Builder {
		private Type type;
		// HTTP_CLIENT and HTTP_ASYNC_CLIENT options
		private String hostname = null;
		private int port = 0; 
		// HTTP_ASYNC_CLIENT only options
		private  boolean waitForResponse = false;
		private boolean debug = false;

		public Builder(Type type)	{
			this.type = type;
		}
		private boolean validHostname()	{
			return (hostname != null);
		}
		private boolean validPort() {
			return (port != 0);
		}
		public Builder hostname(String val)	
			{ hostname = val; return this; }
		public Builder port(int val)	
			{ port = val; return this; }
		public Builder waitForResponse(boolean val)	
			{ waitForResponse = val; return this; }
		public Builder debug(boolean val) {
			{ debug = val; return this; }
		}
		
		public TracyPublisher build()	{
			switch (type)	{
			case HTTP_ASYNC_CLIENT:
				if (validHostname() && validPort())	{
					TracyPublisherFactory.tracyPublisher = 
							new TracyAsyncHttpClientPublisher(hostname, port, waitForResponse, debug);
				}
				else	{
					// Create a noop published in case the client ignores the exception
					TracyPublisherFactory.tracyPublisher = new TracyNoopPublisher();
					if (!validHostname())	{
						throw new IllegalStateException("No/Invalid host supplied");
					}
					if (!validPort())	{
						throw new IllegalStateException("No/Invalid port supplied");
					}
				}
				break;
				
			case HTTP_CLIENT:
				if (validHostname() && validPort())	{
					TracyPublisherFactory.tracyPublisher = 
							new TracyCloseableHttpClientPublisher(hostname, port, debug);
				}
				else	{
					// Create a noop published in case the client ignores the exception
					TracyPublisherFactory.tracyPublisher = new TracyNoopPublisher();
					if (!validHostname())	{
						throw new IllegalStateException("No/Invalid host supplied");
					}
					if (!validPort())	{
						throw new IllegalStateException("No/Invalid port supplied");
					}
				}
				break;
				
			case NOOP:
				TracyPublisherFactory.tracyPublisher = new TracyNoopPublisher();
				break;
				
			default:
				break;
			}
			return null;
		}

	}
}
