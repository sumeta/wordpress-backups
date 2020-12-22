package com.tpsoft.wordpress.backups;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import com.smattme.MysqlExportService;

public class App 
{
	
	private static Configuration config = null;
	
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        config = new Configuration();     
        
        run();
        
    }

	private static void run()
	{
		String pattern = "yyyyddMM-HHmmss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String date = simpleDateFormat.format(new Date());
        System.out.println(date);
        try {
        	  File directory = new File("Backups/"+date);
              if (!directory.exists()){
                  if (directory.mkdirs()){
                  	System.out.println("Directories are created!");
                  	backupDB(directory.getPath());
                  	File file = new File(directory+"/Note.txt");
      	        	if (file.createNewFile()) {
      	                FileWriter writer = new FileWriter(file);
      	                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
      	                String dateStr = dateFormat.format(new Date());
      	                writer.write("Backup start date "+ dateStr);
      	                writer.close();
      	                backupFile(directory);
      	            } else {
      	                System.out.println("Failed to create file!");
      	            }
                  }
              }
        }catch (Exception e) {
        	 System.out.println("Failed to create directories!");
		}
	}
	

	private static void backupFile(File directory) {
        try {
            //new ftp client
            FTPClient ftp = new FTPClient();
            //try to connect
            ftp.connect(config.getHost(),config.getPort());
            //login to server
            if (!ftp.login(config.getUser(),config.getPass())) {
                ftp.logout();
            }
            int reply = ftp.getReplyCode();
            //FTPReply stores a set of constants for FTP reply codes. 
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
            }
            //enter passive mode
            ftp.enterLocalPassiveMode();
            //get system name
            System.out.println("Remote system is " + ftp.getSystemType());
            //change current directory
            ftp.changeWorkingDirectory("/");
            System.out.println("Current directory is " + ftp.printWorkingDirectory());
            

            File sourcePath = new File(directory + "/source/"); 
            if (sourcePath.mkdirs()){
            	downloadAllFile(ftp,"/",sourcePath.toString());
            }

            ftp.logout();
            ftp.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

	private static void backupDB(String directory) throws IOException {
		String dbhost = "";
		String dbname = "";
		String dbuser = "";
		String dbpwd = "";
		
		//new ftp client
        FTPClient ftp = new FTPClient();
        //try to connect
        ftp.connect(config.getHost(),config.getPort());
        //login to server
        if (!ftp.login(config.getUser(),config.getPass())) {
            ftp.logout();
        }
        int reply = ftp.getReplyCode();
        //FTPReply stores a set of constants for FTP reply codes. 
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
        }
        //enter passive mode
        ftp.enterLocalPassiveMode();
        //get system name
        System.out.println("Remote system is " + ftp.getSystemType());
        //change current directory
        ftp.changeWorkingDirectory("/");
        System.out.println("Current directory is " + ftp.printWorkingDirectory());
        
		String remoteFilePath = "wp-config.php";
		InputStream inputStream = ftp.retrieveFileStream(remoteFilePath);
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
		String line = null;
		while ((line = br.readLine()) != null) {
			if(line.contains("define('DB_NAME'")) {
				Pattern p = Pattern.compile("'(.*?)'");
				Matcher m = p.matcher(line);
				while (m.find()) {
				  if(!m.group(1).equals("DB_NAME")) {
					  System.out.println("Name " + m.group(1));
					  dbname = m.group(1);
				  }
				}
			}
			if(line.contains("define('DB_USER'")) {
				Pattern p = Pattern.compile("'(.*?)'");
				Matcher m = p.matcher(line);
				while (m.find()) {
					if(!m.group(1).equals("DB_USER")) {
						System.out.println("User " + m.group(1));
						dbuser = m.group(1);
					}
				}
			}
			if(line.contains("define('DB_PASSWORD'")) {
				Pattern p = Pattern.compile("'(.*?)'");
				Matcher m = p.matcher(line);
				while (m.find()) {
					if(!m.group(1).equals("DB_PASSWORD")) {
						System.out.println("Password " + m.group(1));
						dbpwd = m.group(1);
					}
				}
			}
			if(line.contains("define('DB_HOST'")) {
				Pattern p = Pattern.compile("'(.*?)'");
				Matcher m = p.matcher(line);
				while (m.find()) {
					if(!m.group(1).equals("DB_HOST")) {
						System.out.println("DB_HOST " + m.group(1));
						dbhost = m.group(1);
					}
				}
			}
		}
		
//		DBUtil.backup(directory);
		Properties properties = new Properties();
    	properties.setProperty(MysqlExportService.JDBC_CONNECTION_STRING, "jdbc:mysql://"+dbhost+":3306/"+dbname+"?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false");
    	properties.setProperty(MysqlExportService.DB_NAME, dbname);
    	properties.setProperty(MysqlExportService.DB_USERNAME, dbuser);
    	properties.setProperty(MysqlExportService.DB_PASSWORD, dbpwd);
    	properties.setProperty(MysqlExportService.PRESERVE_GENERATED_ZIP, "true");

    	properties.setProperty(MysqlExportService.TEMP_DIR, new File(directory).getPath());

    	MysqlExportService mysqlExportService = new MysqlExportService(properties);
    	try {
			mysqlExportService.export();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	
	private static void downloadAllFile(FTPClient ftp,String remotePath,String localPath) {
		try {
			
			FTPFile[] ftpFiles = ftp.listFiles();
			downloadFile(localPath, ftp, ftpFiles);
			
			FTPFile[] directoryList = ftp.listDirectories();
			 for (FTPFile directory : directoryList) {
				 String newDirectory = directory.getName();
				 // crate directory
				 String newRemotePath = remotePath +"/"+ newDirectory;
				 File subLocal = new File(localPath+"/"+newDirectory);
            		// download
            		ftp.changeWorkingDirectory(newRemotePath);
            		System.out.println("Change remote path " + newRemotePath);
            		if(remotePath.equals("/")) {
            			if(directory.toString().contains("wp-")) {
            				if(subLocal.mkdirs()) {
           				 	downloadAllFile(ftp,newRemotePath,subLocal.toString());
            				}
            			}
        				
        			}else {
        				if(subLocal.mkdirs()) {
       				 		downloadAllFile(ftp,newRemotePath,subLocal.toString());
        				}
        			}
			 }
			 
		} catch (IOException e) {
		
			e.printStackTrace();
		}
			
		
	}
	

	private static void downloadFile(String directory, FTPClient ftp, FTPFile[] ftpFiles)
			throws FileNotFoundException, IOException {
		if (ftpFiles != null && ftpFiles.length > 0) {
		    //loop thru files
		    for (FTPFile file : ftpFiles) {
		        if (!file.isFile()) {
		            continue;
		        }
		        System.out.println("File downlaod to " + directory +"/"+ file.getName());
		        //get output stream
		        OutputStream output;
		        output = new FileOutputStream(directory  +"/"+ file.getName());
		        //get the file from the remote system
		        ftp.retrieveFile(file.getName(), output);
		        //close output stream
		        output.close();
		    }
		}
	}
	
}
