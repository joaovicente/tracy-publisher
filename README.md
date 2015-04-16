# tracy-publisher
Library used to publish JVM generated Tracy to a Tracy store

## Usage
```
	new TracyPublisherFactory.Builder(TracyPublisherFactory.Type.HTTP_CLIENT)
		.hostname("localhost")
		.port(8050)
		// If you are using a proxy (only supported for dev)
		//.httpProxyConfig(proxyHost, proxyPort, username, password)
		.build();
	// In this example Tracy publisher server is expected to be listening 
	// on http://localhost:8050/tracy/segment
			
	final String NULL_PARENT_OPT_ID = null;
	Tracy.setContext("MyTask", PARENT_OPT_ID, "MyComponent");
	Tracy.before("myLabel");
	Tracy.after("myLabel")
	TracyPublisherFactory.getInstance().publish(Tracy.getEventsAsJsonTracySegment())
	Tracy.clearContext();
```


