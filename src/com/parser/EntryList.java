/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.parser;

import java.util.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.iterators.FilterIterator;

/**
 *
 * @author vigneshm
 */
public class EntryList implements Iterable< Entry >{
    private List<Entry> list;
    private Map<Entry,Boolean> taken;
    public Set<String> authorlist;
    private int sorttype;//0-no sort 1-date t 2-date f 3-cit t 4-cit f
    public EntryList(){
        list=new ArrayList<>();
        taken=new HashMap<>();
        authorlist=new HashSet<>();
        this.sorttype=0;
    }
    public void add(Entry e){
        boolean mark=false;
        list.add(e);
        for(String name:e.author){
            if(authorlist.contains(name)) {
                mark=true;
            }
            else{
                authorlist.add(name);
                mark=true;
            }
        }
        taken.put(e, mark);
    }
    @Override
    public Iterator<Entry> iterator(){
        return new FilterIterator(list.iterator(),new takenCheckPredicate());
    }
    public List<Entry> datesearchlist(int beginyr,int endyr){
        return (List<Entry>)CollectionUtils.select(list, new yearIntervalPredicate(beginyr,endyr));
    }
    class takenCheckPredicate implements Predicate<Entry>{
        public boolean evaluate(Entry e){
            return taken.get(e);
        }
    }
    class yearIntervalPredicate implements Predicate<Entry>{
        private int begin;
        private int end;
        public yearIntervalPredicate(int beginyr,int endyr){
            this.begin=beginyr;
            this.end=endyr;
        }
        @Override
        public boolean evaluate(Entry e){
            int yr=e.year;
            return (taken.get(e) && yr>=begin && yr<=end);
        }
    }
    public int size(){
        int size=0;
        for(Boolean b:taken.values()){
            if(b) size++;
        }
        return size;
        //return list.size();
    }
    public Entry get(int i){
        if(taken.get(list.get(i))) return list.get(i);
        //return null;
        return list.get(i);
    }
    public void sortByYear(boolean ascending){
        Collections.sort(list,new DateCompare(ascending));
        if(ascending) this.sorttype=1;
        else this.sorttype=2;
    }
    public void sortByCitations(boolean ascending){
        Collections.sort(list,new CitCompare(ascending));
        if(ascending) this.sorttype=3;
        else this.sorttype=4;
    }
    public static void sortListByYear(List<Entry> entrylist,boolean ascending){
        Collections.sort(entrylist,(new EntryList()).new DateCompare(ascending));
    }
    public static void sortListByCit(List<Entry> entrylist,boolean ascending){
        Collections.sort(entrylist,(new EntryList()).new CitCompare(ascending));
    }
    public class DateCompare implements Comparator<Entry>{
        public boolean ascending;
        @Override
        public int compare(Entry e1,Entry e2){
            if(e1.year==-1) return -1;
            if(e2.year==-1) return 1;
            if(ascending) return Integer.valueOf(e1.year).compareTo(e2.year);
            else return -(Integer.valueOf(e1.year).compareTo(e2.year));
        }
        public DateCompare(boolean a){
            this.ascending=a;
        }
    }
    public class CitCompare implements Comparator<Entry>{
        public boolean ascending;
        @Override
        public int compare(Entry e1,Entry e2){
            if(e1.citationcount==-1) return -1;
            if(e2.citationcount==-1) return 1;
            if(ascending) return Integer.valueOf(e1.citationcount).compareTo(e2.citationcount);
            else return -(Integer.valueOf(e1.citationcount).compareTo(e2.citationcount));
        }
        public CitCompare(boolean a){
            this.ascending=a;
        }
    }
    public void addAuthor(String name){
        if(!authorlist.contains(name)){
            authorlist.add(name);
            return;
        }
        for(Entry e:list){
            if(e.author.contains(name)) taken.put(e, Boolean.TRUE);
        }
        this.restoreSort(this.sorttype);
    }
    public void removeAuthor(String name){
        if(!authorlist.contains(name)){
            return;
        }
        authorlist.remove(name);
        for(Entry e:list){
            if(taken.get(e)){
                boolean mark=false;
                for(String an:e.author){
                    if(authorlist.contains(an)){
                        mark=true;
                        //System.out.println("mark true at "+name);
                        break;
                    }
                }
                taken.put(e,mark);
            }
        }
        this.restoreSort(this.sorttype);
    }
    public void restoreSort(int type){
        if(type==0) return;
        if(type==1) this.sortByYear(true);
        if(type==2) this.sortByYear(false);
        if(type==3) this.sortByCitations(true);
        if(type==4) this.sortByCitations(false);
        
    }
    public int getHIndex(){
        int countarr[] =new int[list.size()+1];
        for(Entry e:this.list) countarr[Math.min(e.year,list.size())]++;
        int sum = 0;
        for (int i = countarr.length - 1; i >= 0; i--)
        {
            sum += countarr[i];
            if (sum >= i)
                return i;
        }
        return list.size()-1;
    }
    public int getIIndex(){
        int oldtype=this.sorttype;
        int count=0;
        this.sortByYear(false);
        for(Entry e:list){
            if(e.citationcount<10) return count;
        }
        return this.size();
    }
}
