/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.parser;

import java.util.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import java.util.regex.*;

/**
 *
 * @author vigneshm
 */
public class Entry {
    public Element entryData;
    public HeadLink sidelink;
    public String title;
    public Link titleLink;
    public List<String> author;
    public String source;
    public String abstText;
    public Link citations;
    public int citationcount;
    public Link related;
    public Link versions;
    public int year;
    public int isProper;//1-yes 2-user profile
    public boolean taken;
    class HeadLink{
        public String url;
        public String text;
        public String type;
        public HeadLink(Element a){
            this.url=a.attr("href");
            try{
                this.text=a.getElementsByClass("gs_ggsS").get(0).ownText();
            } catch(Exception e){
                this.text="";
            }
            try{
                this.type=a.getElementsByClass("gs_ctg2").get(0).ownText();
            } catch(Exception e){
                this.type="";
            }      
        }
        public String toString(){
            String str="";
            if(this.url.equals("")) return str;
            str+="URL : "+this.url+ " , "+this.text+" "+this.type;
            return str;
        }
    }
    class Link{
        public String url;
        public String text;
        public Link(Element a){
            url=a.attr("href");
            text=a.ownText();
        }
        public String toString(){
            String str="";
            str+="URL : "+url+ " , "+text;
            return str;
        }
    }
    public Entry(Element l){
        this.entryData=l;
        this.isProper=1;
        this.sidelink=this.new HeadLink(l.getElementsByTag("a").get(0));
        try{
            this.title=l.getElementsByClass("gs_rt").get(0).text();
            this.title=this.title.replaceAll("\\[.*\\]", "");
            if(title.contains("User profile for author") || title.contains("User profiles for author")) this.isProper=2;
        }catch(Exception e){
            this.title=null;
        }
        try{
            this.titleLink=this.new Link(l.getElementsByClass("gs_rt").get(0).getElementsByTag("a").get(0));
        }catch(Exception e){
            this.titleLink=null;
            //e.printStackTrace();
        }
        try{
            this.author=new ArrayList<>();
            String authortext=l.getElementsByClass("gs_a").get(0).text();
            String[] authortextarr=authortext.split("-");
            String authors=authortextarr[0];
            if(authortextarr.length>1) {
                String num=authortextarr[1].trim();
                Pattern yr=Pattern.compile("\\d{4}");
                Matcher m=yr.matcher(num);
                int yearpub=-1;
                while(m.find()){
                    for(int i=0;i<=m.groupCount();i++){
                        yearpub=Integer.parseInt(m.group(i));
                    }
                }
                if(yearpub==-1){
                    for(int idx=2;idx<authortextarr.length;idx++){
                        num=authortextarr[idx].trim();
                        m=yr.matcher(num);
                        while(m.find()){
                        for(int i=0;i<=m.groupCount();i++){
                                yearpub=Integer.parseInt(m.group(i));
                            }
                        }
                        if(yearpub>0) break;
                    }
                }
                this.year=yearpub;
            }
            if(authortextarr.length>2) this.source=authortextarr[2];
            for(String name:authors.split(",")) this.author.add(name.trim());
        }catch(Exception e){
            this.author=null;
            this.year=-1;
            this.source=null;
        }
        //if(authorElement.child(0)!=null) this.author+=" "+authorElement.child(0).ownText();
        //if(authorElement.textNodes().size()>1) this.source=authorElement.textNodes().get(1).text();
        try{
            this.abstText=l.getElementsByClass("gs_rs").get(0).text();
        }catch(Exception e){
            this.abstText=null;
        }
        this.citations=null;
        this.related=null;
        this.versions=null;
        Elements links=l.getElementsByClass("gs_fl");
        for(Element linkgrp:links){
            for(Element link:linkgrp.getElementsByTag("a")){
                if(link.text().contains("Cited by")){
                    this.citations=this.new Link(link);
                    try{
                        this.citationcount=Integer.parseInt((this.citations.text.split("Cited by"))[1].trim());
                    }catch (NumberFormatException e){
                        this.citationcount=-1;
                    }
                }
                if(link.text().contains("Related articles")){
                    this.related=this.new Link(link);
                }
                if(link.text().contains("versions") && link.text().contains("All")){
                    this.versions=this.new Link(link);
                }
            }
        }
    }
    
    @Override
    public String toString(){
        String str="";
        if(this.title!=null)str+="Title : "+this.title+"\n";
        if(this.author!=null){
            if(this.author.size()==1) str+="Author : "+this.author.get(0)+"\n";
            else{
                str+="Authors : ";
                for(int i=0;i<this.author.size();i++)
                    str+=this.author.get(i)+((i==this.author.size()-1)?"":" , ");
                str+="\n";
            }
        }
        if(this.source!=null)str+="Source : "+this.source+"\n";
        if(this.year>0)str+="Year of Publication : "+this.year+"\n";
        if(this.sidelink!=null && this.sidelink.url!=null && !this.sidelink.url.equals(""))
            str+="Link : "+this.sidelink.toString()+"\n";
        if(this.titleLink!=null)
            str+="Title Link : "+this.titleLink.toString()+"\n";
        if(this.abstText!=null)str+="Abstract : "+this.abstText+"\n";
        //if(this.citations!=null)str+=this.citations.text+"\n";
        if(this.citationcount>=0) str+="Number of Citations : "+this.citationcount+"\n"; 
        return str;
    }
    public String toString_basic(){
        String str="";
        if(this.title!=null)str+="Title : "+this.title+"\n";
        if(this.author!=null){
            if(this.author.size()==1) str+="Author : "+this.author.get(0)+"\n";
            else{
                str+="Authors : ";
                for(int i=0;i<this.author.size();i++)
                    str+=this.author.get(i)+((i==this.author.size()-1)?"":" , ");
                str+="\n";
            }
        }
        if(this.year>0)str+="Year of Publication : "+this.year+"\n";
        if(this.citationcount>=0) str+="Number of Citations : "+this.citationcount+"\n"; 
        return str;
    }
}
