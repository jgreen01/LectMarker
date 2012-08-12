package edu.umd.umich.lectmarker.libs;

import java.io.File;

import android.os.Environment;
import android.util.Log;

	public class FileList {
		
		public File 	pwdir;			// present working directory
		public String[]	contents_pwd;	// doesn't really have a use anymore...
		//public File[]	files_pwd;
		
		public FileList() {
			pwdir = new File("/");
			if (pwdir.list().length == 0)
			{
				contents_pwd = new String[1];
				contents_pwd[0]="Please add audio files";
			}
			else if(pwdir.list()!=null)
				contents_pwd = pwdir.list();
			else
			{
				contents_pwd = new String[1];
				contents_pwd[0]="Directory Not present";
			}
				
			Log.i("LectMarker", "FileList class: Inside populate_list");
			
		}
		
		// checks pwdir exists and if /lectmarker and /lectmarker/audio are in it
		public void check_path() {
			String mainFolder = Environment.getExternalStorageDirectory()+"/lectmarker";	// why is this here again?
			String audioFolder = Environment.getExternalStorageDirectory()+"/lectmarker/audio";	// ''
			String RNFolder = Environment.getExternalStorageDirectory()+"/lectmarker/recNotes";
			File maindir = new File(mainFolder);
			File audiodir = new File(audioFolder);
			File RNdir = new File(RNFolder);
			
			Log.v("Lectmarker","Checking if " + maindir.toString() + " exists.");
			
			if(!maindir.exists())
			{
				Log.v("Lectmarker",maindir.toString() + " doesn't exists creating.");
				maindir.mkdir();
				audiodir.mkdir();
				RNdir.mkdir();
				if(!maindir.exists())
					Log.v("Lectmarker",maindir.toString() + " created successfully.");
			}
			else if (!audiodir.exists())
				audiodir.mkdir();
			else if (!RNdir.exists())
				RNdir.mkdir();
		}
		
		public String[] audioFiles()	// unused
		{
			check_path();
			File audiodir = new File(pwdir.toString() + "audio/");
			
			return audiodir.list();	// assuming .../audio only has audio files...	
		}
		
		public void populate_list()
		{
			if (pwdir.list().length == 0)
			{
				contents_pwd = new String[1];
				contents_pwd[0]="Please add audio files";
			}
			else if(pwdir.list()!=null)
				contents_pwd = pwdir.list();
			else
			{
				contents_pwd = new String[1];
				contents_pwd[0]="Directory Not present";	// shouldn't happen
			}
				
			Log.i("LectMarker", "FileList class: Inside populate_list");
			
		}
		
	}
