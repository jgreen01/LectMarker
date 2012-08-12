package edu.umd.umich.lectmarker;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import edu.umd.umich.lectmarker.lib.FileList;
import edu.umd.umich.lectmarker.lib.xmlManager;

public class AudioList extends Activity implements Runnable, OnItemClickListener, OnItemLongClickListener, OnClickListener{
	
	Button goBack;
	ListView fileList;
	String[] filtered;
	String mainFolder = Environment.getExternalStorageDirectory()+"/lectmarker";
	String audioFolder = Environment.getExternalStorageDirectory()+"/lectmarker/audio";
	ArrayAdapter<String> adapter = null;
	//String bookmarkFolder = Environment.getExternalStorageDirectory()+"/lectmarker/bookmarks";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_row);
		
		goBack = (Button)this.findViewById(R.id.ButtonBack3);
		goBack.setOnClickListener(this);
	    
		FileList filesPresent = new FileList();
		filesPresent.check_path();
		filesPresent.pwdir = new File (audioFolder);
		createList();
		
		Bundle extras = getIntent().getExtras(); // not used...
		if(extras!=null){
			if (extras.getBoolean("goRec")==true)
			{
				Intent i = new Intent("edu.umd.umich.lectmarker.REC");
				i.putExtra("track_list",filtered);
		
				startActivity(i);
			}
		}
		

		Thread currentThread = new Thread(this);
		currentThread.start();
	}
	
	public void createList(){
		FileList filesPresent = new FileList();
		filesPresent.pwdir = new File (audioFolder);
		
		
		filesPresent.populate_list();
		fileList = (ListView) findViewById(R.id.audioList);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, filteredList(filesPresent.contents_pwd));
		fileList.setAdapter(adapter);
		fileList.setOnItemClickListener(this);
		fileList.setOnItemLongClickListener(this);
	}
	
	public String[] filteredList(String[] list){	// filters out xml files.... 
		filtered = new String[0];	// why not just make a list of supported audio files
		String[] temp = new String[0];
		int size = 0;
		Log.i("in","filtered");
		for(int i=0;i<list.length;i++){
			if(!list[i].contains(".xml")){
				size++;
				if (i == 0){
					filtered = new String[size];
					filtered[size-1]=list[i];	
					Log.i("got",filtered[size-1]);
				}
				else{
					temp = copyArray(filtered,filtered.length);
					filtered = new String[size];
					Log.i("size",Integer.toString(size));
					filtered = copyArray(temp,size);
					filtered[size-1]=list[i];
					Log.i("got",Integer.toString(filtered.length));
				}
				
			}
		}Log.i("got",Integer.toString(filtered.length));
		Log.i("got",filtered[size-1]);
		return filtered;
		
	}
	
	public String[] copyArray(String[] array,int size){	// do we even need this?...
		String[] newArray = new String[size];
		if (array.length != size){
			newArray[size-1]="";
		}
		Log.i("in","copyArray");
		for (int i=0; i<array.length;i++){
			newArray[i] = array[i];
		}
		return newArray;
	}
	
	public void onClick(View v) {
		if (v.getId() == R.id.ButtonBack3){
			finish();
			
		}
	
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		String selectedID = (String) ((TextView) arg1).getText();
		if (selectedID.equals("Please add audio files"))
		{
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Add Files");
			alert.setMessage("Please add audio files to the lectmarker>audio folder found on the root of your sdcard or record a new audio");

			// Set an EditText view to get user input 
			

			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				
			  }
			});
			
			alert.show();
		}
		else
		{
		
		@SuppressWarnings("unused")
		String modifiedString = selectedID.replace(".mp3", "");
		
		String newxmlfile = new String(audioFolder+"/"+selectedID+".xml");
		xmlManager bmCreate = new xmlManager(newxmlfile);
		bmCreate.createXml();
		
		Intent i = new Intent("edu.umd.umich.lectmarker.PLAY");
		i.putExtra("track", selectedID);
		boolean PON = true;
		i.putExtra("PON", PON);
		startActivity(i);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		//String selectedID = (String)((TextView)arg1).getText();
		//final File selectedFile = new File(audioFolder+"/"+selectedID);
		editFileInflator(arg1);
		return false;
	}
	
	public void editFileInflator(View arg1){	// why not turn this into a class... like editFile or something
		String selectedID = (String)((TextView)arg1).getText();
		final File selectedFile = new File(audioFolder+"/"+selectedID);
		final File selectedXmlFile = new File(audioFolder+"/"+selectedID+".xml");
		int dotpos = selectedID.indexOf(".");
		final String ext = selectedID.substring(dotpos);
		Log.i("ext",ext);
		final View arg = arg1;
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this); 

        alert.setTitle("Rename");  
        // Set an EditText view to get user input  
        alert.setMessage("Enter new name:");
        final EditText field = new EditText(this);
        alert.setView(field);
  
        
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
			File newFile = new File(audioFolder+"/"+field.getText().toString()+ext);
			File newXmlFile = new File(audioFolder+"/"+field.getText().toString()+ext+".xml");
			selectedFile.renameTo(newFile);
			selectedXmlFile.renameTo(newXmlFile);
			adapter = null;
			fileList = null;
			createList();
		
		}});
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog, int whichButton) {
    			
    	}});
        
        alert.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int whichButton) {
    			//selectedFile.delete();
    			confirmDelete(selectedFile,selectedXmlFile,arg);
    			adapter = null;
    			fileList = null;
    			createList();
    		
    		}});
        
        
        alert.show();
	}
	
	public void confirmDelete(File file1,File filexml, View arg1){	// add to editFile class
		final File file = file1;
		final File xmlfile = filexml;
		final View arg = arg1;
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("DELETE");
		alert.setMessage("Delete File?");

		// Set an EditText view to get user input 
		

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  
			xmlfile.delete();
			file.delete();
			adapter = null;
			fileList = null;
			createList();
			
		
		  // Do something with value!
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
			  editFileInflator(arg);
		  }
		});

		alert.show();
	}
	
	public void onResume(){
		super.onResume();
		adapter = null;
		fileList = null;
		createList();
	}

	@Override
	public void run() {
		// not used... but eclipse wants it
	}
}
