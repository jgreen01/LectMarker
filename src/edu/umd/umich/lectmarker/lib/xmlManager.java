package edu.umd.umich.lectmarker.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.os.Environment;
import android.util.Log;
import android.util.Xml;

// the createXml method works fine but modifyXml doesn't :?
// the code is a little complex will look at later

	public class xmlManager{
		private File tempxml;
		private	String xmlFile;
		public int bmNumber;
		
		public xmlManager(String xFile){
			//bmNumber = numberoBM;
			tempxml = new File(Environment.getExternalStorageDirectory()+ "/lectmarker/temp.xml");
			xmlFile = xFile;
		}
		
		public void createXml(){
			
			File newxmlfile = new File(xmlFile);
			
			if (!newxmlfile.exists()){
				try {
						newxmlfile.createNewFile();
					} catch(IOException e) {
						Log.e("IOException", "exception in createNewFile() method");
					}
				
				FileOutputStream fileos = null;        
				
				try {
	                	fileos = new FileOutputStream(newxmlfile);
					} catch(FileNotFoundException e) {
						Log.e("FileNotFoundException", "can't create FileOutputStream");
					}
				
				XmlSerializer serializer = Xml.newSerializer();
	        
				try {
	        			serializer.setOutput(fileos, "UTF-8");
	        			serializer.startDocument(null, Boolean.valueOf(true));
	        			serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
	        			serializer.endDocument();
	        			serializer.flush();
	        			fileos.close();
	                } catch (Exception e) {
	                	Log.e("Exception","error occurred while creating xml file");
	                }
	                
			}
		}
		
		private void modifyXml(String name, String startEndVal, boolean addNotDelete, boolean startNotEnd) throws IllegalArgumentException, IllegalStateException, IOException, XmlPullParserException {
			File myxml = new File(xmlFile);
			boolean end = false;		// don't really know what this is...
			boolean thisOne = false;	// don't really know what this is...
			boolean nextIsRN = false; //next start tag is the correct recNote tag
			int parses= 0;
			XmlSerializer serializer = Xml.newSerializer();
			
			createXml();
				
			FileInputStream fileis = new FileInputStream(myxml);
				
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		    factory.setNamespaceAware(true);
		    XmlPullParser xpp = factory.newPullParser();

		    xpp.setInput(fileis, "UTF-8");
		        
		    int eventType = xpp.getEventType();
				
				
			try {
					tempxml.createNewFile();
				} catch(IOException e) {
					Log.e("IOException", "exception in createNewFile() method");
				}

			FileOutputStream fileos = null;        
				
			try {
	               	fileos = new FileOutputStream(tempxml);
				} catch(FileNotFoundException e) {
					Log.e("Lectmarker", "can't create FileOutputStream");
				}

		    serializer.setOutput(fileos, "UTF-8");
		    serializer.startDocument(null, Boolean.valueOf(true));
		    serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
		                        
		    while ((eventType != XmlPullParser.END_DOCUMENT) && addNotDelete) { //adding
		       	if(eventType == XmlPullParser.START_DOCUMENT) {
		       		if((bmNumber == 0) && startNotEnd){	// if nothing in xml then just add
		       			serializer.startTag(null,"bookmark"); 
		       			serializer.attribute(null, "name",name);
		       			serializer.attribute(null, "start", startEndVal);
		       			//serializer.attribute(null, "end", endVal);
		       			serializer.endTag(null, "bookmark");
		       		}
		       	 
		       	} else if(eventType == XmlPullParser.START_TAG) {
		       			parses++;
		       			serializer.startTag(null, xpp.getName());
		       		
		       			for(int i=0;i<xpp.getAttributeCount();i++)	// copying over all data
		       				serializer.attribute(null, xpp.getAttributeName(i), xpp.getAttributeValue(i));
		       			
		       			if(parses == bmNumber )	// is all data copied?
		       				end = true;
		       			
		       			/*if (nextIsRN){
		       				serializer.attribute(null, "nName", nName);
		       				serializer.attribute(null, "path", npath);
		       				nextIsRN = false;
		       			}*/
		       			
		       			if (xpp.getAttributeValue(null, "name").equals(name)){
			        		/*if (recNote)
			        			nextIsRN = true;*/
		       				
		       				if (startNotEnd){
				            	 serializer.attribute(null, "start", startEndVal);
				             }
			        		 
				             else{
				            	 serializer.attribute(null, "end",startEndVal);
				             }
			        	 }
		       	 
		       	} else if(eventType == XmlPullParser.END_TAG) {	// adds new bookmark
		       		serializer.endTag(null,xpp.getName());
		       		if (end && startNotEnd) {	// isn't this in the END_TAG anyways..?
		       			serializer.startTag(null,"bookmark"); 
		       			serializer.attribute(null, "name",name);
		       			serializer.attribute(null, "start", startEndVal);
		       			//serializer.attribute(null, "end", endVal);
		       			serializer.endTag(null, "bookmark");
		       		}
		            
		       	} else if(eventType == XmlPullParser.TEXT) {} 
		        
		       	eventType = xpp.next();
		    }
		    
		    while ((eventType != XmlPullParser.END_DOCUMENT) && !addNotDelete) { //deleting
				if(eventType == XmlPullParser.START_DOCUMENT) {}
				else if(eventType == XmlPullParser.START_TAG) {
					if (!xpp.getAttributeValue(null,"name").equals(name)){
						serializer.startTag(null, xpp.getName());
        	 
						for(int i=0;i<xpp.getAttributeCount();i++){
							serializer.attribute(null, xpp.getAttributeName(i), xpp.getAttributeValue(i)); 
						}
					}
					else
						thisOne = true;
        	 
				} else if(eventType == XmlPullParser.END_TAG) {
					if (!thisOne){
						serializer.endTag(null,xpp.getName());
					}
					else
						thisOne = false;
				} else if(eventType == XmlPullParser.TEXT) {} 
         
				eventType = xpp.next();
		    }
		       
		    serializer.endDocument();
	        serializer.flush();
	        fileos.close();
	        fileis.close();	// end of copying file and adding/deleting
	            
	        // now copying tempxml to myxml
	        FileReader in = new FileReader(tempxml);
	        FileWriter out = new FileWriter(myxml);
	        int c;

	        while ((c = in.read()) != -1)
	           	out.write(c);

	        in.close();
	        out.close();
	        
		}
		
		public void addToXml(String name ,String startEndVal ,boolean startNotEnd) {
			try {
				modifyXml(name, startEndVal, true, startNotEnd);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			}
		}
		
		public void deleteFromXml(String name) {
			try {
				modifyXml(name, "404", false, true);	// any bogus value would do
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			}
		}
	}