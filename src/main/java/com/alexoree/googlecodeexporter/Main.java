/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alexoree.googlecodeexporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 *
 * @author alex
 */
public class Main {

     public static void main(String[] args) throws Exception {
          String project = "osmdroid";
          String saveDir = ".";
          

          int BUFFER_SIZE = 1024;
          byte[] buffer = new byte[BUFFER_SIZE];
          
          int n = 1;
          while (true) {

               String urlj = "https://www.googleapis.com/storage/v1/b/google-code-archive/o/v2%2Fcode.google.com%2F" + project + "%2Fissues%2Fissue-" + n + ".json?alt=media&stripTrailingSlashes=false";
               URL url = new URL(urlj);
               HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
               int responseCode = httpConn.getResponseCode();

               // always check HTTP response code first
               if (responseCode == HttpURLConnection.HTTP_OK) {
                    String fileName = project + "_issue" + n + ".json";
                    String disposition = httpConn.getHeaderField("Content-Disposition");
                    String contentType = httpConn.getContentType();
                    int contentLength = httpConn.getContentLength();

                    System.out.println("Content-Type = " + contentType);
                    System.out.println("Content-Disposition = " + disposition);
                    System.out.println("Content-Length = " + contentLength);
                    System.out.println("fileName = " + fileName);

                    // opens input stream from the HTTP connection
                    InputStream inputStream = httpConn.getInputStream();
                    String saveFilePath = saveDir + File.separator + fileName;

                    // opens an output stream to save into file
                    FileOutputStream outputStream = new FileOutputStream(saveFilePath);

                    int bytesRead = -1;

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                         outputStream.write(buffer, 0, bytesRead);
                    }

                    outputStream.close();
                    inputStream.close();

                    System.out.println("File downloaded");
               } else {
                    System.out.println("No file to download. Server replied HTTP code: " + responseCode);
                    break;
               }
               httpConn.disconnect();
               n++;
          }
          
          
          //pretty print the files
          File[] files = new File(".").listFiles(new FilenameFilter() {
               @Override
               public boolean accept(File dir, String name) {
                    if (name.endsWith(".json")) {
                         return true;
                    }
                    return false;
               }
          });
          ObjectMapper mapper = new ObjectMapper();
          for (int i = 0; i < files.length; i++) {
               String test = readFile(files[i].getAbsolutePath(), Charset.defaultCharset());
               Object json = mapper.readValue(test, Object.class);
               test=(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
               Files.write(files[i].toPath(), test.getBytes(Charset.defaultCharset()), StandardOpenOption.WRITE);
          }
     }
     
     
     
     static String readFile(String path, Charset encoding)
          throws IOException {
          byte[] encoded = Files.readAllBytes(Paths.get(path));
          return new String(encoded, encoding);
     }

   

}
