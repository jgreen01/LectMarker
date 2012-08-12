package edu.umd.umich.lectmarker.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

	public class bmData{
		public	String[]	bmName;
		public	int[]	bmStartPoint;
		public	int[]	bmEndPoint;
		public  String[]   rnName;
		private	String	fPath;
		public int		bmNumber;
		public int      rnNumber;
		
		public bmData(String filePath){			
			fPath = filePath;
			
			try {
				bmNumber = length(true);
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				rnNumber = length(false);
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			bmName = new String[bmNumber];
			rnName = new String[rnNumber];
			bmStartPoint = new int[bmNumber];
			bmEndPoint = new int[bmNumber];
			
			try {
				getbmData();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private int length(boolean bmNotRn) throws XmlPullParserException, IOException{	// there might be a better way
			int i = 0;
			int j = 0;

			File myxml = new File(fPath);
			FileInputStream fileis = new FileInputStream(myxml);
			
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
	        XmlPullParser xpp = factory.newPullParser();
	        
	        int eventType = xpp.getEventType();
	        
	        xpp.setInput(fileis, "UTF-8");
			
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if(eventType == XmlPullParser.START_DOCUMENT) {}
	        	else if(eventType == XmlPullParser.START_TAG)
	        		if (xpp.getName().equals("bookmark"))
	        			i++;
	        		else{ 
	        			if (xpp.getAttributeCount()!=0)
	        			j++;
	        		}
	        	else if(eventType == XmlPullParser.END_TAG) {}
	        	else if(eventType == XmlPullParser.TEXT) {}	
				
				eventType = xpp.next();
			}
			if(!bmNotRn)
				i = j;
			
			return i;
			
				
		}
		
		private void getbmData() throws XmlPullParserException, IOException{
			int i = 0;
			int j = 0;
			File myxml = new File(fPath);
			FileInputStream fileis = new FileInputStream(myxml);
			
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
	        XmlPullParser xpp = factory.newPullParser();
	        
	        xpp.setInput(fileis, "UTF-8");
	        
	        int eventType = xpp.getEventType();
	        
	        while (eventType != XmlPullParser.END_DOCUMENT) {
	        	if(eventType == XmlPullParser.START_DOCUMENT) {}
	        	else if(eventType == XmlPullParser.START_TAG) {
	        		if (xpp.getName().equals("bookmark")){
	        			bmName[i] = xpp.getAttributeValue(null,"name");
	        			bmStartPoint[i] = Integer.parseInt(xpp.getAttributeValue(null,"start"));
	        			bmEndPoint[i] = Integer.parseInt(xpp.getAttributeValue(null,"end"));
	        			i++;
	        		}
	        		else{
	        			if (xpp.getAttributeCount()!=0){
	        				rnName[j] = xpp.getAttributeValue(null,"rnName");
	        				j++;
	        			}
	        			
	        		}
	        	} 
	        	else if(eventType == XmlPullParser.END_TAG) {}	// idk why we have all these 
	        	else if(eventType == XmlPullParser.TEXT) {}		// if's but Samer must have
	        	eventType = xpp.next();							// a reason
	        }
	        sortBM();
	        
	        for(i = 0; i < bmNumber; i++) {
				
				Log.d("Lectmarker","bmName["+i+"] = " + bmName[i]);
	    		Log.d("Lectmarker","bmStartPoint["+i+"] = " + bmStartPoint[i] 
	    				+ " bmEndPoint["+i+"] = " + bmEndPoint[i]);
			}
		}
		
		private void sortBM() {		// probably should set this up the way Alex said
			int i, j, minIndex, tempStart, tempEnd;
			String tempName,tempRnName;
		      int n = bmNumber;
		      for (i = 0; i < n - 1; i++) {
		            minIndex = i;
		            for (j = i + 1; j < n; j++)
		            	if ( bmStartPoint[j] <  bmStartPoint[minIndex])
		                        minIndex = j;
		            if (minIndex != i) {
		                  tempName = bmName[i];
		                  bmName[i] = bmName[minIndex];
		                  bmName[minIndex] = tempName;
		                  tempStart = bmStartPoint[i];
		                  bmStartPoint[i] = bmStartPoint[minIndex];
		                  bmStartPoint[minIndex] = tempStart;
		                  tempEnd = bmEndPoint[i];
		                  bmEndPoint[i] = bmEndPoint[minIndex];
		                  bmEndPoint[minIndex] = tempEnd;
		                  //tempRnName = rnName[i];
		                  //rnName[i] = rnName[minIndex];
		                  //rnName[minIndex] = tempRnName;
		            }
		      }
		}
		
	}