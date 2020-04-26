package com.tpsoft.wordpress.backups;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {
	
	private String host;
	private String user;
	private String pass;
	
	
	public Configuration() {

		try {
			
			InputStream input = new FileInputStream("config.properties");
			
			Properties prop = new Properties();
            prop.load(input);
            
			this.host = prop.getProperty("ftp.host");
			this.user =  prop.getProperty("ftp.user");
			this.pass = prop.getProperty("ftp.password");

	        } catch (IOException ex) {
	            ex.printStackTrace();
	        }
	      

	}


	public String getHost() {
		return host;
	}


	public void setHost(String host) {
		this.host = host;
	}


	public String getUser() {
		return user;
	}


	public void setUser(String user) {
		this.user = user;
	}


	public String getPass() {
		return pass;
	}


	public void setPass(String pass) {
		this.pass = pass;
	}
		
	
	

}
