package com.zelinskyi.maksym;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    private static String link = "https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos?sol=10&api_key=DEMO_KEY";

    public static void main(String[] args) {
        try {
            System.out.println(getLargestPhoto(link));
        }catch(Exception e){
            e.printStackTrace();
        }
        }

    public static String getLargestPhoto(String link) throws MalformedURLException, URISyntaxException {

            URL url = getURL(link);
            URLConnection connection = null;
           String jsonContent = "";
           try(BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()))) {
              while(reader.ready()){
                  jsonContent += reader.readLine();
              }
           }catch(IOException e){
               e.printStackTrace();
           }
           List<String> links = getImageLinks(jsonContent);
           return findLargest(links);
    }

    public static List<String> getImageLinks(String jsonContent){
        List<String> img_srcs = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"img_src\":\".+?\"");
        Matcher matcher = pattern.matcher(jsonContent);
        while(matcher.find()){
            img_srcs.add(matcher.group().replaceAll("\"img_src\":", "").replaceAll("\"", ""));
        }
        return img_srcs;
    }

    private static String findLargest(List<String> img_srcs) throws MalformedURLException, URISyntaxException {
    String largest = "";
    int maxSize = 0;
    for(String img_src : img_srcs){
        URL imgURL = getURL(img_src);
        int size = 0;
       try{
        HttpURLConnection connection = (HttpURLConnection) imgURL.openConnection();
          connection.setRequestMethod("GET");
        String header1 =  connection.getHeaderField(0);
        int firstSpace = header1.indexOf(" ");
        int code = Integer.parseInt(header1.substring(firstSpace+1, header1.indexOf(" ", firstSpace+1)));

        if((code==301)||(code==302)){
            connection = (HttpURLConnection) getURL(connection.getHeaderField("Location")).openConnection();
        }
        size = Integer.parseInt(connection.getHeaderField("Content-Length"));
        }catch(IOException e){}
        if(size>maxSize){
            maxSize = size;
            largest = img_src;
        }
    }
    return largest;
    }

    private static URL getURL(String string) throws MalformedURLException, URISyntaxException{
            return new URI(string).toURL();
    }
}