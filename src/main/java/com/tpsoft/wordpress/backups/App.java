package com.tpsoft.wordpress.backups;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

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
        	  File directory = new File("Backups\\"+date);
              if (!directory.exists()){
                  if (directory.mkdirs()){
                  	System.out.println("Directories are created!");
                  	File file = new File(directory+"\\Note.txt");
      	        	if (file.createNewFile()) {
      	                FileWriter writer = new FileWriter(file);
      	                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
      	                String dateStr = dateFormat.format(new Date());
      	                writer.write("Backup start date "+ dateStr);
      	                writer.close();
      	                ftp(directory);
      	            } else {
      	                System.out.println("Failed to create file!");
      	            }
                  }
              }
        }catch (Exception e) {
        	 System.out.println("Failed to create directories!");
		}
	}
	

	private static void ftp(File directory) {
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

            File sourcePath = new File(directory + "\\source\\"); 
            if (sourcePath.mkdirs()){
            	downloadAllFile(ftp,"/",sourcePath.toString());
            }

            ftp.logout();
            ftp.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
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
				 File subLocal = new File(localPath+"\\"+newDirectory);
            	if(subLocal.mkdirs()) {
            		// download
            		ftp.changeWorkingDirectory(newRemotePath);
            		System.out.println("Change remote path " + newRemotePath);
   				 	downloadAllFile(ftp,newRemotePath,subLocal.toString());
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
		        System.out.println("File downlaod to " + directory +"\\"+ file.getName());
		        //get output stream
		        OutputStream output;
		        output = new FileOutputStream(directory  +"\\"+ file.getName());
		        //get the file from the remote system
		        ftp.retrieveFile(file.getName(), output);
		        //close output stream
		        output.close();
		    }
		}
	}
	
}
