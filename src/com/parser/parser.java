/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.parser;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.select.*;
import org.jsoup.nodes.*;

/**
 *
 * @author vigneshm
 */
public class parser {
    static Map<String,String> predata;
    static void getPreData(){
        try {
            BufferedReader r  = new BufferedReader(new FileReader("/Users/vigneshm/NetBeansProjects/parser/src/com/parser/data"));
            StringBuilder sb=new StringBuilder("");
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line);
            }
            String[] strs=sb.toString().split("::-::");
            predata=new HashMap<>();
            for(String s:strs){
                String[] pair=s.split("::::");
                predata.put(pair[0].trim(),pair[1]);
            }
            //System.out.println(sb);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    static String getHTML(String url) throws Exception {
        getPreData();
        URLConnection connection = new URL(url).openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
        connection.connect();

        BufferedReader r  = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            sb.append(line);
        }
        if(predata.containsKey(url.trim())) return  predata.get(url.trim());
        return sb.toString();
    }
    public static String searchString(int option,String name,int start,int numperpage){
        if(option==1){
            //author search
            name=name.replace(" ","%20");
            String str="http://scholar.google.com/scholar?start="+start+"&q=author:"+name+"&hl=en&num="+numperpage;
            return str;
        }
        return null;
    }

    public static EntryList parsePage (int option,String name,int num) throws Exception{
        Elements entries;
        EntryList entryList=new EntryList();
            int st;
            for(st=0;st<=num-10;st+=10){
                entries=(Jsoup.parse(getHTML(searchString(option,name,st,10)))).getElementsByClass("gs_r");
                for(Element el:entries){
                    Entry entry=new Entry(el);
                    if(entry.isProper==1) entryList.add(entry);
                }
            }
            entries=(Jsoup.parse(getHTML(searchString(option,name,st,num-st)))).getElementsByClass("gs_r");
                for(Element el:entries){
                    Entry entry=new Entry(el);
                    if(entry.isProper==1) entryList.add(entry);
                }
            return entryList;
    }
    public static void main(String[] args) throws Exception{
        EntryList list=parsePage(1,"einstein",10);
        System.out.println(list.size());
        for(Entry i:list) System.out.println(i);
    }
}
