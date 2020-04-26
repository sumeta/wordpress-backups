package com.tpsoft.wordpress.backups;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        
        createFile();
        
    }

	private static void createFile()
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
      	            } else {
      	                System.out.println("Failed to create file!");
      	            }
                  }
              }
        }catch (Exception e) {
        	 System.out.println("Failed to create directories!");
		}
	}
	
}
