package com.apm4all.tracy;

public class HttpProxyConfig {

	private boolean enabled;
	private String host;
	private int port;
	private String username;
	private String password;

	public HttpProxyConfig(String host, int port, String username, String password) {
		super();
		this.enabled = true;
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}
	
	public HttpProxyConfig() {
		this.enabled = false;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}
