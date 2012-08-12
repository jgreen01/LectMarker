package edu.umd.umich.lectmarker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import edu.umd.umich.lectmarker.libs.NumberPicker;
import edu.umd.umich.lectmarker.libs.StopWatch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Xml;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

public class RecordActivity extends Activity implements OnTouchListener,OnClickListener, Runnable, SeekBar.OnSeekBarChangeListener, OnItemClickListener, OnItemLongClickListener, OnLongClickListener{

	MediaRecorder recorder;
	Button recordButton;
	Button bMarkButton;
	Button backButton;
	TextView recName;
	TextView nTime;
	TextView nTime2;
	ListView fileList;
	ArrayAdapter<String> adapter;
	String[] bmList;
	String xmlPath;
	String audioFolder = Environment.getExternalStorageDirectory()+"/lectmarker/audio";
	String newPath;
	String defName= "LectRec1";
	String bMarkName;
	String End;
	boolean blankN;
	boolean blankL;
	boolean cameBack = false;
	boolean pressed = false;
	int Time;
	int p;
	boolean started = false;
	boolean firstStart = true;
	final int MSG_START_TIMER = 0;
    final int MSG_STOP_TIMER = 1;
    final int MSG_UPDATE_TIMER = 2;
    final int MSG_RESUME_TIMER = 3;

    StopWatch timer = new StopWatch();
    final int REFRESH_RATE = 100;

    
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.record_view);
		
		Bundle extras = getIntent().getExtras(); 
		if(extras !=null) {
			newPath = audioFolder+"/"+defaultPath(extras.getStringArray("track_list"))+".m4a";
		}
		
		xmlPath = audioFolder+"/"+defName+".m4a.xml";
		createXml(defName);
		Log.i("in","record");
		recName = (TextView) this.findViewById(R.id.recordingName);
		recName.setText(defName);
		recName.setOnClickListener(this);
		
		nTime = (TextView)this.findViewById(R.id.RnowTime);
		//nTime2 = (TextView)this.findViewById(R.id.RallTime);
		
		recordButton= (Button)this.findViewById(R.id.ButtonRec);
		recordButton.setOnClickListener(this);
		
		backButton = (Button)this.findViewById(R.id.ButtonBack2);
		backButton.setOnClickListener(this);
		
		bMarkButton = (Button) this.findViewById(R.id.ButtonBookmarkR); 
		bMarkButton.setOnClickListener(this);
		bMarkButton.setOnLongClickListener(this);
		
		createList();
		
		
		
		
		
	
	}
	
	Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case MSG_START_TIMER:
                timer.start(); //start timer
                
                mHandler.sendEmptyMessage(MSG_UPDATE_TIMER);
                break;
            
            case MSG_RESUME_TIMER:
                timer.start(); //start timer
                timer.startTime = timer.stopTime;
                mHandler.sendEmptyMessage(MSG_UPDATE_TIMER);
                break;

            case MSG_UPDATE_TIMER:
                Time = ((int) timer.getElapsedTime())/100;
                String sTime = (Time < 3600) ? String.format("%02d:%02d:%02d", Time/600, (Time % 600)/10,Time%10) 
        				:	String.format("%d:%02d:%02d", Time/3600, (Time % 3600)/60, Time % 60);
                
            	nTime.setText(sTime);
            	
                mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIMER,REFRESH_RATE); //text view is updated every second, 
                break;                                  //though the timer is still running
            case MSG_STOP_TIMER:
                mHandler.removeMessages(MSG_UPDATE_TIMER); // no more updates.
                timer.stop();//stop timer
                Time = ((int) timer.getElapsedTime())/100;
                sTime = (Time < 3600) ? String.format("%02d:%02d:%02d", Time/600, (Time % 600)/10,Time%10) 
        				:	String.format("%d:%02d:%02d", Time/3600, (Time % 3600)/60, Time % 60);
                
                nTime.setText(sTime);
               
                break;

            default:
                break;
            }
        }
    };
	
	public String defaultPath(String[] list){
		int n = 0;
		
		boolean exists = checkPath(defName, list);
		while(exists){
			n++;
			defName = "LectRec"+n;
			exists = checkPath(defName,list);
			if (!exists)
			  break;
		}
		
		return defName;
	}
	
	public boolean checkPath(String string, String[] list){
		boolean exists = false;
		for(int i=0;i<list.length;i++){
			  if(list[i].contains(string)){
				  exists =true;
			  }
			  
		}
		return exists;
	}
	
	private void startRecording() {
        
		recorder = new MediaRecorder();
		recorder.reset();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(newPath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setAudioEncodingBitRate(16);
        recorder.setAudioSamplingRate(44100);
        
        

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("", "prepare() failed");
        }

        recorder.start();
    }
	
	private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }
	
	
	private void onRecord(boolean start) {
        if (!start) {
            startRecording();
            recordButton.setBackgroundResource(R.drawable.s_stop);
            
            	mHandler.sendEmptyMessage(MSG_START_TIMER);
            
        } else {
            stopRecording();
            recordButton.setBackgroundResource(R.drawable.s_rec);
            mHandler.sendEmptyMessage(MSG_STOP_TIMER);
            
            cameBack = true;
            Intent i = new Intent("edu.umd.umich.lectmarker.PLAY");
    		i.putExtra("track", defName+".m4a");
    		boolean PON = false;
    		i.putExtra("PON", PON);
    		startActivity(i);
        }
    }
	
	public void nameAudioAlert(){

        AlertDialog.Builder alert = new AlertDialog.Builder(this); 

        alert.setTitle("New Recording");  
        // Set an EditText view to get user input  
        alert.setMessage("Enter audio name");
        final EditText field = new EditText(this);
        alert.setView(field);
        
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			
			File nPath = new File(audioFolder +"/"+field.getText().toString()+".m4a");
			File defPath = new File(audioFolder+"/"+defName+".m4a");
			File nPathxml = new File(audioFolder +"/"+field.getText().toString()+".m4a.xml");
			File defPathxml = new File(audioFolder +"/"+defName+".m4a.xml");
			defPath.renameTo(nPath);
			defPathxml.renameTo(nPathxml);
			
			defName = field.getText().toString();
			newPath = audioFolder+"/"+defName+".m4a"; 
			xmlPath = audioFolder+"/"+defName+".m4a.xml";
			recName.setText(defName);
			
			
		}});
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    		@Override
			public void onClick(DialogInterface dialog, int whichButton) {
    			
    			
    			
    		}});
        alert.show();
        
        
	}
	
	
	
	
	
	@Override
	public boolean onLongClick(View v) {
		if (v.getId() == R.id.ButtonBookmarkR){
			try {
				bookmark();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		
		final String listItem =(String) ((TextView) arg1).getText();
		editBMInflater(arg1);
	
		
	}

	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId()== R.id.ButtonRec){
			onRecord(started);
			started = !started;
			
		}
		
		if (v.getId()==R.id.ButtonBack2){
			if (started)
				stopRecording();
			finish();
			
		}
		if (v.getId()==R.id.recordingName){
			nameAudioAlert();
		}
		
		if (v.getId()==R.id.ButtonBookmarkR){
			pressed = !pressed;
			if (pressed){
				bMarkButton.setBackgroundResource(R.drawable.bookmark_icon_on);
				try {
					
					quickBM();
					
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (XmlPullParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else{
				bMarkButton.setBackgroundResource(R.drawable.s_bookmark);
				End = Integer.toString(((int)(timer.getElapsedTime())/1000)-(p/1000)+7);
				Log.i("END",End);
				Log.i("NAME",bMarkName);
				
				editBookMark(bMarkName,"",End,true);
			}
			
		}
		
			
		
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		return false;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if (cameBack)
			finish();
	}
	
	//ListView stuff
	public void createList(){
		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		
		//it will only use TheFileList1 if it's not rotated
		Log.d("Lectmarker","Roation "+display.getRotation());
		if (display.getRotation () == 0)	// possible values 0 1 2?
		{
			Log.d("Lectmarker","not rotated creating RBList");
			FileList filesPresent = new FileList(this);
			filesPresent.pwdir = new File("/sdcard");
			filesPresent.populate_list();
			fileList = (ListView) findViewById(R.id.RBList);
			try {
				adapter = new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1,
						bmNameList(xmlPath));
			} catch (XmlPullParserException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			fileList.setAdapter(adapter);
			fileList.setOnItemClickListener(this);
			//fileList.setOnItemLongClickListener(this);
		}
	}
	
	public class FileList {
		
		public File 	pwdir;			// present working directory
		public String[]	contents_pwd;	// doesn't really have a use anymore...
		//public File[]	files_pwd;
		
		public FileList(RecordActivity recordActivity) {
			pwdir = new File("/");
			contents_pwd = pwdir.list();
			//files_pwd = pwdir.listFiles();
		}
		
		// checks pwdir exists and if /audio and /bookmarks are in it
		public void check_path()	
		{
			//pwdir = new File("/sdcard/LectMarker");
			String mainFolder = Environment.getExternalStorageDirectory()+"/lectmarker";
			String audioFolder = Environment.getExternalStorageDirectory()+"/lectmarker/audio";
			String bookmarkFolder = Environment.getExternalStorageDirectory()+"/lectmarker/bookmarks";
			File maindir = new File(mainFolder);
			File audiodir = new File(audioFolder);
			File bookmarksdir = new File(bookmarkFolder);
			
			Log.v("LectMarker","Checking if " + maindir.toString() + " exists.");
			
			//if(maindir == null)
			//	Log.e("LectMarker","pwdir is null! Moron!");
			
			if(!maindir.exists())
			{
				Log.v("LectMarker",maindir.toString() + " doesn't exists creating.");
				maindir.mkdir();
				audiodir.mkdir();
				bookmarksdir.mkdir();
				if(!maindir.exists())
					Log.v("LectMarker",maindir.toString() + " created successfully.");
			}
			else if (!audiodir.exists())
				audiodir.mkdir();
			else if (!bookmarksdir.exists())
				bookmarksdir.mkdir();
		}
		
		public String[] audioFiles()
		{
			//pwdir = new File("/sdcard/LectMarker/");
			check_path();
			File audiodir = new File(pwdir.toString() + "audio/");
			
			return audiodir.list();	// assuming .../audio only has audio files...	
		}
		
		public void populate_list()	// mostly useless
		{
			contents_pwd = pwdir.list();
			Log.i("LectMarker", "FileList class: Inside populate_list");
			
			if(contents_pwd == null)
			{
				pwdir = new File ("/");
				populate_list();
				Log.i("LectMarker", "FileList class: Inside populate_list conditional");
			}
		}
		
	}
	
	
	
	//XML STUFF
	
	public String[] bmNameList(String fPath) throws XmlPullParserException, IOException
	{
		File myxml = new File(fPath);
		FileInputStream fileis = new FileInputStream(myxml);
		
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        
        bmList = new String[0];
        String[] temp;
        int size = 0;
        
        xpp.setInput(fileis, "UTF-8");
        
        int eventType = xpp.getEventType();
        
        while (eventType != XmlPullParser.END_DOCUMENT)	// loops til EOF
        {
        	
        if(eventType == XmlPullParser.START_DOCUMENT)
        { 
        	
        	
        }	// why do we need all the if's?
        else if(eventType == XmlPullParser.START_TAG)
        {
        	size++;
        	if (size ==1){
        		bmList = new String[size];
        		bmList[size-1]=xpp.getAttributeValue(null,"name");
        		Log.i("made it","");
        	}
        	else{
        		temp = copyArray(bmList,bmList.length);
				bmList = new String[size];
				bmList = copyArray(temp,size);
				bmList[size-1]=xpp.getAttributeValue(null,"name");
        	}
        	Log.d("LectMarker","Attribute count " + xpp.getAttributeCount());
        	/*bmList = new String[0];
        	for (int i=1;i<xpp.getAttributeCount();i++) {
        			
        			bmList[i-1] = xpp.getAttributeValue(i);
        			
           	}*/
        } 
        else if(eventType == XmlPullParser.END_TAG)
        { } 
        else if(eventType == XmlPullParser.TEXT)
        { }
         eventType = xpp.next();	// goes to next eventtype.
        }
		
		return bmList;
	}
	
	public String[] copyArray(String[] array,int size){
		String[] newArray = new String[size];
		if (array.length != size){
			newArray[size-1]=null;
		}
		Log.i("in","copyArray");
		for (int i=0; i<array.length;i++){
			newArray[i] = array[i];
		}
		return newArray;
	}
	
	public void addToXml(String path,String name,boolean start, String val) throws IllegalArgumentException, IllegalStateException, IOException, XmlPullParserException{
		
		File tempxml = new File(Environment.getExternalStorageDirectory()+ "/lectmarker/temp.xml");
		File myxml = new File(path);
		boolean end = false;
		int parses= 0;
		
		if (myxml.exists()){
			
			FileInputStream fileis = new FileInputStream(myxml);
			
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
	        XmlPullParser xpp = factory.newPullParser();
	        XmlPullParser tpp = factory.newPullParser();
	        
	        xpp.setInput(fileis, "UTF-8");
	        
	        int eventType = xpp.getEventType();
			
			
			try{
                tempxml.createNewFile();
			}catch(IOException e){
                Log.e("IOException", "exception in createNewFile() method");
			}
			
			//we have to bind the new file with a FileOutputStream
			FileOutputStream fileos = null;        
			try{
                fileos = new FileOutputStream(tempxml);
			}catch(FileNotFoundException e){
                Log.e("Lectmarker", "can't create FileOutputStream");
			}
			XmlSerializer serializer = Xml.newSerializer();
	        
	                //we set the FileOutputStream as output for the serializer, using UTF-8 encoding
	        serializer.setOutput(fileos, "UTF-8");
	                        //Write <?xml declaration with encoding (if encoding not null) and standalone flag (if standalone not null)
	        serializer.startDocument(null, Boolean.valueOf(true));
	                        //set indentation option
	        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
	                        
	        while (eventType != XmlPullParser.END_DOCUMENT) {
	         if(eventType == XmlPullParser.START_DOCUMENT) {
	        	if(bmList.length==0){
	        		if (start){
		            	 serializer.startTag(null,"bookmark"); 
		            	 serializer.attribute(null, "name",name);
		            	 serializer.attribute(null, "start", val);
		            	 serializer.endTag(null, "bookmark");
		             }
	        	}
	             
	             
	            /* TODO by Jon
	             * fix this code so... the tag name isn't the the bookmark name...
	             * this will been a pain... 
	             * TODO
	             * also simplify and combine addToXml and deleteFromXml these functions
	             * look very similar and share alot of code... Maybe we should just put
	             * all the xml functions and put them in there own class :/
	             */
	        	 
	         }
	         else if(eventType == XmlPullParser.START_TAG) {
	        	 parses++;
	        	 serializer.startTag(null, xpp.getName());
	        	 for(int i=0;i<xpp.getAttributeCount();i++){
	        		 serializer.attribute(null, xpp.getAttributeName(i), xpp.getAttributeValue(i)); 
	        	 }
	        	 if(parses == bmList.length ){
	        		 end = true;
	        	 }
	        	 if (xpp.getAttributeValue(null, "name").equals(name)){
	        		 if (start){
		            	 serializer.attribute(null, "start", val);
		             }
		             else{
		            	 serializer.attribute(null, "end",val);
		             }
	        	 }
	        	 
	        	 
	         } else if(eventType == XmlPullParser.END_TAG) {
	        	 serializer.endTag(null,xpp.getName());
	        	 if (end){
	        		 if (start){
		            	 serializer.startTag(null,"bookmark"); 
		            	 serializer.attribute(null, "name",name);
		            	 serializer.attribute(null, "start", val);
		            	 serializer.endTag(null, "bookmark");
		             }
	        	 }
	             
	         } else if(eventType == XmlPullParser.TEXT) {
	             
	         } 
	         
	         eventType = xpp.next();
	        }
	        
	        serializer.endDocument();
            //write xml data into the FileOutputStream
            serializer.flush();
            //finally we close the file stream
            fileos.close();
            fileis.close();
            
            //Write back to original path
            
            fileis = new FileInputStream(tempxml);
			
			factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
	        xpp = factory.newPullParser();
	        
	        xpp.setInput(fileis, "UTF-8");
	        
	        eventType = xpp.getEventType();
			
			
			try{
                myxml.createNewFile();
			}catch(IOException e){
                Log.e("IOException", "exception in createNewFile() method");
			}
			
			//we have to bind the new file with a FileOutputStream
			fileos = null;        
			try{
                fileos = new FileOutputStream(myxml);
			}catch(FileNotFoundException e){
                Log.e("FileNotFoundException", "can't create FileOutputStream");
			}
			serializer = Xml.newSerializer();
	        
	                //we set the FileOutputStream as output for the serializer, using UTF-8 encoding
	        serializer.setOutput(fileos, "UTF-8");
	                        //Write <?xml declaration with encoding (if encoding not null) and standalone flag (if standalone not null)
	        serializer.startDocument(null, Boolean.valueOf(true));
	                        //set indentation option
	        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
	                        
	        while (eventType != XmlPullParser.END_DOCUMENT) {
	         if(eventType == XmlPullParser.START_DOCUMENT) {
	        	 
	         } else if(eventType == XmlPullParser.START_TAG) {
	        	 serializer.startTag(null, xpp.getName());
	        	 for(int i=0;i<xpp.getAttributeCount();i++){
	        		 serializer.attribute(null, xpp.getAttributeName(i), xpp.getAttributeValue(i)); 
	        	 }
	         } else if(eventType == XmlPullParser.END_TAG) {
	             serializer.endTag(null,xpp.getName());
	         } else if(eventType == XmlPullParser.TEXT) {
	             
	         }
	         eventType = xpp.next();
	        }
	        
	        serializer.endDocument();
            //write xml data into the FileOutputStream
            serializer.flush();
            //finally we close the file stream
            fileos.close();
            fileis.close();
		}
	}
	
	public void deleteFromXml(String path,String name) throws IllegalArgumentException, IllegalStateException, IOException, XmlPullParserException{
		boolean thisOne = false;
		File tempxml = new File(Environment.getExternalStorageDirectory()+ "/lectmarker/temp.xml");
		File myxml = new File(path);
		Log.i("path",path);
		if (myxml.exists()){
			Log.i("in","exists");
			FileInputStream fileis = new FileInputStream(myxml);
			Log.i("part1","");
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
	        XmlPullParser xpp = factory.newPullParser();
	        
	        
	        xpp.setInput(fileis, "UTF-8");
	        
	        int eventType = xpp.getEventType();
			
			
			try{
	            tempxml.createNewFile();
			}catch(IOException e){
	            Log.e("IOException", "exception in createNewFile() method");
			}
			
			//we have to bind the new file with a FileOutputStream
			FileOutputStream fileos = null;        
			try{
	            fileos = new FileOutputStream(tempxml);
			}catch(FileNotFoundException e){
	            Log.e("FileNotFoundException", "can't create FileOutputStream");
			}
			XmlSerializer serializer = Xml.newSerializer();
			Log.i("start","");
	                //we set the FileOutputStream as output for the serializer, using UTF-8 encoding
	        serializer.setOutput(fileos, "UTF-8");
	        Log.i("end","");
	        //Write <?xml declaration with encoding (if encoding not null) and standalone flag (if standalone not null)
	        serializer.startDocument(null, Boolean.valueOf(true));
	                        //set indentation option
	        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
	        Log.i("!!","");                
	        while (eventType != XmlPullParser.END_DOCUMENT) {
	         if(eventType == XmlPullParser.START_DOCUMENT) {
	        	 Log.i("part1","");
	         } else if(eventType == XmlPullParser.START_TAG) {
	        	 Log.i("part1","");
	        	 if (!xpp.getAttributeValue(null,"name").equals(name)){
	        		 serializer.startTag(null, xpp.getName());
	        	 
	        		 for(int i=0;i<xpp.getAttributeCount();i++){
	        			 serializer.attribute(null, xpp.getAttributeName(i), xpp.getAttributeValue(i)); 
	        		 }
	        	}
	        	 else
	        		 thisOne = true;
	        	 
	         } else if(eventType == XmlPullParser.END_TAG) {
	        	 Log.i("part1","");
	        	 if (!thisOne){
	        		 Log.i("part2","");
	        		 serializer.endTag(null,xpp.getName());
	        		 Log.i("part3","");
	        	 }
	        	 else
	        		 thisOne = false;
	         } else if(eventType == XmlPullParser.TEXT) {
	             
	         } 
	         
	         eventType = xpp.next();
	        }
	        
	        serializer.endDocument();
	        //write xml data into the FileOutputStream
	        serializer.flush();
	        //finally we close the file stream
	        fileos.close();
	        fileis.close();
	        
	        //Write back to original path
	        
	        fileis = new FileInputStream(tempxml);
			
			factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
	        xpp = factory.newPullParser();
	        
	        xpp.setInput(fileis, "UTF-8");
	        
	        eventType = xpp.getEventType();
			
			
			try{
	            myxml.createNewFile();
			}catch(IOException e){
	            Log.e("IOException", "exception in createNewFile() method");
			}
			
			//we have to bind the new file with a FileOutputStream
			fileos = null;        
			try{
	            fileos = new FileOutputStream(myxml);
			}catch(FileNotFoundException e){
	            Log.e("FileNotFoundException", "can't create FileOutputStream");
			}
			serializer = Xml.newSerializer();
	        
	                //we set the FileOutputStream as output for the serializer, using UTF-8 encoding
	        serializer.setOutput(fileos, "UTF-8");
	                        //Write <?xml declaration with encoding (if encoding not null) and standalone flag (if standalone not null)
	        serializer.startDocument(null, Boolean.valueOf(true));
	                        //set indentation option
	        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
	                        
	        while (eventType != XmlPullParser.END_DOCUMENT) {
	         if(eventType == XmlPullParser.START_DOCUMENT) {
	        	 
	         } else if(eventType == XmlPullParser.START_TAG) {
	        	 serializer.startTag(null, xpp.getName());
	        	 for(int i=0;i<xpp.getAttributeCount();i++){
	        		 serializer.attribute(null, xpp.getAttributeName(i), xpp.getAttributeValue(i)); 
	        	 }
	         } else if(eventType == XmlPullParser.END_TAG) {
	             serializer.endTag(null,xpp.getName());
	         } else if(eventType == XmlPullParser.TEXT) {
	             
	         }
	         eventType = xpp.next();
	        }
	        
	        serializer.endDocument();
	        //write xml data into the FileOutputStream
	        serializer.flush();
	        //finally we close the file stream
	        fileos.close();
	        fileis.close();
		}
		}
	
	public void quickBM() throws IllegalArgumentException, IllegalStateException, IOException, XmlPullParserException{
		p = (int)timer.getElapsedTime()+6;
		int p1 = p/1000;
		final String formatP = p1 < 3600 ? String.format("%d:%02d", p1/60, p1 % 60) 
				:	String.format("%d:%02d:%02d", p1/3600, (p1 % 3600)/60, p1 % 60);
        
		int bmLength1 = (p+30000)/1000;
		String formatEnd =  bmLength1 < 3600 ? String.format("%d:%02d", bmLength1/60, bmLength1 % 60) 
 				:	String.format("%d:%02d:%02d", bmLength1/3600, (bmLength1 % 3600)/60, bmLength1 % 60);
		
		String timespace = " ("+formatP+" - "+")";
		int n = (bmList.length+1);
		String getBMN = "Bookmark"+n;
		boolean exists = checkBM(getBMN);
		while(exists){
			n++;
			getBMN = "Bookmark"+n;
			exists = checkBM(getBMN);
			if (!exists)
			  break;
		}
		bMarkName = getBMN+timespace;
		addToXml(xmlPath,getBMN+timespace,true,Integer.toString(p));
		addToXml(xmlPath,getBMN+timespace,false,Integer.toString(p+30000));
	
		adapter = null;
		fileList = null;
		createList();
	}
	
public void bookmark() throws IllegalArgumentException, IllegalStateException, IOException, XmlPullParserException{
		
		p = (int)timer.getElapsedTime()+6;
		bmInflater();
		 
		/*		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Bookmark");
		alert.setMessage("Enter Bookmark Name");

		// Set an EditText view to get user input 
		final EditText BMinput = new EditText(this);
		alert.setView(BMinput);
		

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  bMarkName = BMinput.getText().toString();
		  if (bMarkName.equals(""))
			  bMarkName = "Bookmark ("+((p < 3600) ? String.format("%d:%02d", p/60, p % 60) 
	    				:	String.format("%d:%02d:%02d", p/3600, (p % 3600)/60, p % 60))+")";
		  if (bMarkName.contains(" "))
			  bMarkName = bMarkName.replaceAll(" ", "_");
		  try {
			addToXml(xmlPath,bMarkName,true,Integer.toString(p));
			lengthWindow();
			
			adapter = null;
			fileList = null;
			createList();
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  // Do something with value!
		  }
		});
		
		

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});

		alert.show();*/

		
		
	}
	
public void bmInflater(){
	LayoutInflater factory = LayoutInflater.from(this);            
    final View textEntryView = factory.inflate(R.layout.bookmarkname, null);
    int p1 = p/1000;
    final String formatP = p1 < 3600 ? String.format("%d:%02d", p1/60, p1 % 60) 
			:	String.format("%d:%02d:%02d", p1/3600, (p1 % 3600)/60, p1 % 60);
    
   
    AlertDialog.Builder alert = new AlertDialog.Builder(this); 

    alert.setTitle("Bookmark"); 
    //alert.setMessage("Leave blank for quick save"); 
    // Set an EditText view to get user input  
    alert.setView(textEntryView); 
   

    final EditText input1 = (EditText) textEntryView.findViewById(R.id.nameInput);
    final NumberPicker min = (NumberPicker) textEntryView.findViewById(R.id.min);
    final NumberPicker sec = (NumberPicker) textEntryView.findViewById(R.id.sec);
    
    //final EditText input2 = (EditText) textEntryView.findViewById(R.id.lengthInput);

    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int whichButton){ 
			
			  End = Integer.toString((min.getValue()*60)+sec.getValue());
			  if (End.equals("")||End.equals(null))
				  End = "30";
			  int bmLength = Integer.parseInt(End)*1000;
			  int bmLength1 = (p+bmLength)/1000;
			  String formatEnd =  bmLength1 < 3600 ? String.format("%d:%02d", bmLength1/60, bmLength1 % 60) 
  	 				:	String.format("%d:%02d:%02d", bmLength1/3600, (bmLength1 % 3600)/60, bmLength1 % 60);
			
			  String timespace = " ("+formatP+" - "+formatEnd+")";
			  String getBMN = input1.getText().toString();
			   
		  if (getBMN.equals("")||getBMN.equals(null)){
			 
			  int n = (bmList.length+1);
			  getBMN = "Bookmark"+n;
			  boolean exists = checkBM(getBMN);
			  while(exists){
				  n++;
				  getBMN = "Bookmark"+n;
				  exists = checkBM(getBMN);
				  if (!exists)
					  break;
			  }
			 
				  
		  }
		  bMarkName = getBMN+ timespace;
		  //if (bMarkName.contains(" "))
			 // bMarkName = bMarkName.replaceAll(" ", "_");
		 
	  
		
		  try {
			addToXml(xmlPath,bMarkName,true,Integer.toString(p));
			addToXml(xmlPath,bMarkName,false,Integer.toString(p+bmLength));
			
			adapter = null;
			fileList = null;
			createList();
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  // Do something with value!
		  }
		});

    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { 
      @Override
	public void onClick(DialogInterface dialog, int whichButton) { 
        // Canceled. 
      } 
    }); 
    
    alert.show();
}

public void editBMInflater(View arg1){
	final String listItem = (String) ((TextView) arg1).getText();
	final View arg = arg1;
	int p1 = p/1000;
    final String formatP = p1 < 3600 ? String.format("%d:%02d", p1/60, p1 % 60) 
			:	String.format("%d:%02d:%02d", p1/3600, (p1 % 3600)/60, p1 % 60);
	
	try {
		p = getBookmark(xmlPath,listItem);
	} catch (XmlPullParserException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	
	LayoutInflater factory = LayoutInflater.from(this);            
    final View textEntryView = factory.inflate(R.layout.bookmarkname, null);
    blankN = false;
    blankL = false;
    
    AlertDialog.Builder alert = new AlertDialog.Builder(this); 

    alert.setTitle("Edit/Delete");  
    // Set an EditText view to get user input  
    alert.setView(textEntryView); 
   

    final EditText input1 = (EditText) textEntryView.findViewById(R.id.nameInput);
    final NumberPicker min = (NumberPicker) textEntryView.findViewById(R.id.min);
    final NumberPicker sec = (NumberPicker) textEntryView.findViewById(R.id.sec);
    
    //final EditText input2 = (EditText) textEntryView.findViewById(R.id.lengthInput);

    alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int whichButton){ 
			End = Integer.toString((min.getValue()*60)+sec.getValue());
			String getBMN =input1.getText().toString();
			editBookMark(listItem,getBMN,End,false);
			/*
			try {
				if (End.equals("")||End.equals(null)||End.equals(Integer.toString((getEndBookmark(xmlPath,listItem)-p)/1000))){
					  blankL = true;
					  try {
						End = Integer.toString((getEndBookmark(xmlPath,listItem)-p)/1000);
					} catch (XmlPullParserException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (XmlPullParserException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
				  
			int currentEnd = 0;
			try {
				currentEnd = getEndBookmark(xmlPath,listItem)/1000;
			} catch (XmlPullParserException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			String formatCurrentEnd = currentEnd < 3600 ? String.format("%d:%02d", currentEnd/60, currentEnd % 60) 
  	 				:	String.format("%d:%02d:%02d", currentEnd/3600, (currentEnd % 3600)/60, currentEnd % 60);
			String currentTS = " ("+formatP+" - "+formatCurrentEnd+")";
			
			int bmLength = Integer.parseInt(End)*1000;
			
			int bmLength1 = (p+bmLength)/1000;
			String formatEnd =  bmLength1 < 3600 ? String.format("%d:%02d", bmLength1/60, bmLength1 % 60) 
  	 				:	String.format("%d:%02d:%02d", bmLength1/3600, (bmLength1 % 3600)/60, bmLength1 % 60);
			
			String timespace = " ("+formatP+" - "+formatEnd+")";
			
			
		  if (getBMN.equals("")||getBMN.equals(null)||getBMN.equals(listItem)){
			  Log.i("check",listItem);
			  Log.i("check",currentTS);
			  getBMN = listItem;
			  blankN = true;
			  bMarkName = getBMN.replace(currentTS, timespace);
				  
		  }
		  else
			  bMarkName = getBMN + timespace;
		  //if (bMarkName.contains(" "))
			//  bMarkName = bMarkName.replaceAll(" ", "_");
		  
	  
		
		  try {
			
			if (!blankN){	
			  	addToXml(xmlPath,bMarkName,true,Integer.toString(p));
				if (blankL)
					addToXml(xmlPath,bMarkName,false,Integer.toString(getEndBookmark(xmlPath,listItem)));
				else
					addToXml(xmlPath,bMarkName,false,Integer.toString(p+bmLength));
				deleteFromXml(xmlPath,listItem);
			}
			else{
				if (!blankL){
					addToXml(xmlPath,bMarkName,true,Integer.toString(p));
					addToXml(xmlPath,bMarkName,false,Integer.toString(p+bmLength));
					Log.i(listItem,bMarkName);
					deleteFromXml(xmlPath,listItem);
				}
			}
			
			adapter = null;
			fileList = null;
			createList();
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  // Do something with value!
		  */}
		});

    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { 
      @Override
	public void onClick(DialogInterface dialog, int whichButton) { 
        // Canceled. 
      } 
    }); 
    alert.setNeutralButton("Delete", new DialogInterface.OnClickListener() { 
        @Override
		public void onClick(DialogInterface dialog, int whichButton) { 
          confirmDelete(listItem,arg); 
        	
        } 
      }); 
    
    alert.show();
}
	
	public void editBookMark(String listItem, String name,String End,boolean open){
		int p1 = p/1000;
        final String formatP = p1 < 3600 ? String.format("%d:%02d", p1/60, p1 % 60) 
				:	String.format("%d:%02d:%02d", p1/3600, (p1 % 3600)/60, p1 % 60);
		
		try {
			p = getBookmark(xmlPath,listItem);
		} catch (XmlPullParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			if (End.equals("")||End.equals(null)||End.equals(Integer.toString((getEndBookmark(xmlPath,listItem)-p)/1000))){
				  blankL = true;
				  try {
					End = Integer.toString((getEndBookmark(xmlPath,listItem)-p)/1000);
				} catch (XmlPullParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (XmlPullParserException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
			  
		int currentEnd = 0;
		try {
			currentEnd = getEndBookmark(xmlPath,listItem)/1000;
		} catch (XmlPullParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String formatCurrentEnd = currentEnd < 3600 ? String.format("%d:%02d", currentEnd/60, currentEnd % 60) 
	 				:	String.format("%d:%02d:%02d", currentEnd/3600, (currentEnd % 3600)/60, currentEnd % 60);
		String currentTS;
		if (!open)
			currentTS = " ("+formatP+" - "+formatCurrentEnd+")";
		else
			currentTS = " ("+formatP+" - "+")";
		
		int bmLength = Integer.parseInt(End)*1000;
		
		int bmLength1 = (p+bmLength)/1000;
		String formatEnd =  bmLength1 < 3600 ? String.format("%d:%02d", bmLength1/60, bmLength1 % 60) 
	 				:	String.format("%d:%02d:%02d", bmLength1/3600, (bmLength1 % 3600)/60, bmLength1 % 60);
		
		String timespace = " ("+formatP+" - "+formatEnd+")";
		String getBMN =name;
		
	  if (getBMN.equals("")||getBMN.equals(null)||getBMN.equals(listItem)){
		  Log.i("check",listItem);
		  Log.i("check",currentTS);
		  getBMN = listItem;
		  blankN = true;
		  bMarkName = getBMN.replace(currentTS, timespace);
			  
	  }
	  else
		  bMarkName = getBMN + timespace;
	  //if (bMarkName.contains(" "))
		//  bMarkName = bMarkName.replaceAll(" ", "_");
	  
  
	
	  try {
		
		if (!blankN){	
		  	addToXml(xmlPath,bMarkName,true,Integer.toString(p));
			if (blankL)
				addToXml(xmlPath,bMarkName,false,Integer.toString(getEndBookmark(xmlPath,listItem)));
			else
				addToXml(xmlPath,bMarkName,false,Integer.toString(p+bmLength));
			deleteFromXml(xmlPath,listItem);
		}
		else{
			if (!blankL){
				addToXml(xmlPath,bMarkName,true,Integer.toString(p));
				addToXml(xmlPath,bMarkName,false,Integer.toString(p+bmLength));
				Log.i(listItem,bMarkName);
				deleteFromXml(xmlPath,listItem);
			}
		}
		
		adapter = null;
		fileList = null;
		createList();
		
	} catch (IllegalArgumentException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IllegalStateException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (XmlPullParserException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  // Do something with value!
	}
	
	
	
	public void confirmDelete(String BM, View arg1){
		final String listItem = BM;
		final View arg = arg1;
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("DELETE");
		alert.setMessage("Delete Bookmark?");

		// Set an EditText view to get user input 
		

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
		  try {
			deleteFromXml(xmlPath,listItem);
			adapter = null;
			fileList = null;
			createList();
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  // Do something with value!
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  @Override
		public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
			  editBMInflater(arg);
		  }
		});

		alert.show();
	}
	
	public boolean checkBM(String string){
		boolean exists = false;
		for(int i=0;i<bmList.length;i++){
			  if(bmList[i].contains(string)){
				  exists =true;
			  }
			  
		}
		return exists;
	}
	
	public int getBookmark(String fPath, String bmName) throws XmlPullParserException, IOException{
		int time = 0;
		File myxml = new File(fPath);
		FileInputStream fileis = new FileInputStream(myxml);
		
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        
        xpp.setInput(fileis, "UTF-8");
        
        int eventType = xpp.getEventType();
        
        while (eventType != XmlPullParser.END_DOCUMENT) {
         if(eventType == XmlPullParser.START_DOCUMENT) {
             
         } else if(eventType == XmlPullParser.START_TAG) {
             if (xpp.getAttributeValue(null,"name").equals(bmName)){
            	 
            	 time = Integer.parseInt(xpp.getAttributeValue(null,"start"));    
             }
        	 /*for (int i=0;i<xpp.getAttributeCount();i++){
            	 if (xpp.getAttributeValue(i).equals(bmName)){
            		 time = Integer.parseInt(xpp.getAttributeName(i));
            		 break;
            	 }
             }*/
         } else if(eventType == XmlPullParser.END_TAG) {
             
         } else if(eventType == XmlPullParser.TEXT) {
             
         }
         eventType = xpp.next();
        }
        
        return time;
	}
	
	public int getEndBookmark(String fPath, String bmName) throws XmlPullParserException, IOException{
		int time = 0;
		File myxml = new File(fPath);
		FileInputStream fileis = new FileInputStream(myxml);
		
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        
        xpp.setInput(fileis, "UTF-8");
        
        int eventType = xpp.getEventType();
        
        while (eventType != XmlPullParser.END_DOCUMENT) {
         if(eventType == XmlPullParser.START_DOCUMENT) {
             
         } else if(eventType == XmlPullParser.START_TAG) {
             if (xpp.getAttributeValue(null,"name").equals(bmName)){
            	 
            	 time = Integer.parseInt(xpp.getAttributeValue(null,"end"));    
             }
        	 /*for (int i=0;i<xpp.getAttributeCount();i++){
            	 if (xpp.getAttributeValue(i).equals(bmName)){
            		 time = Integer.parseInt(xpp.getAttributeName(i));
            		 break;
            	 }
             }*/
         } else if(eventType == XmlPullParser.END_TAG) {
             
         } else if(eventType == XmlPullParser.TEXT) {
             
         }
         eventType = xpp.next();
        }
        
        return time;
	}
	
public void createXml(String name){
		
		
		File newxmlfile = new File(audioFolder+"/"+name+".m4a.xml");
        
		if (!newxmlfile.exists()){
		try{
                newxmlfile.createNewFile();
        }catch(IOException e){
                Log.e("IOException", "exception in createNewFile() method");
        }
		
        //we have to bind the new file with a FileOutputStream
        FileOutputStream fileos = null;        
        try{
                fileos = new FileOutputStream(newxmlfile);
        }catch(FileNotFoundException e){
                Log.e("FileNotFoundException", "can't create FileOutputStream");
        }
        //we create a XmlSerializer in order to write xml data
        XmlSerializer serializer = Xml.newSerializer();
        try {
                //we set the FileOutputStream as output for the serializer, using UTF-8 encoding
                        serializer.setOutput(fileos, "UTF-8");
                        //Write <?xml declaration with encoding (if encoding not null) and standalone flag (if standalone not null)
                        serializer.startDocument(null, Boolean.valueOf(true));
                        //set indentation option
                        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                        //start a tag called "root"
                        //serializer.startTag(null, "root");
                        //i indent code just to have a view similar to xml-tree
                                
                               
                                //serializer.startTag(null, "Bookmarks");
                                //set an attribute called "attribute" with a "value" for <child2>
                                //serializer.attribute(null, "head","head");
                                //serializer.endTag(null, "Bookmarks");
                       
                                /*serializer.startTag(null, "child3");
                                //write some text inside <child3>
                                serializer.text("some text inside child3");
                                serializer.endTag(null, "child3");*/
                               
                        //serializer.endTag(null, "root");
                        serializer.endDocument();
                        //write xml data into the FileOutputStream
                        serializer.flush();
                        //finally we close the file stream
                        fileos.close();
                       
                
                } catch (Exception e) {
                        Log.e("Exception","error occurred while creating xml file");
                }
                
		}
	}

	@Override 
	public void onConfigurationChanged(Configuration newConfig) { 
		super.onConfigurationChanged(newConfig); 
    //---code to redraw your activity here---
    //...
	}


}
