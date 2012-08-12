package edu.umd.umich.lectmarker;

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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import edu.umd.umich.lectmarker.lib.NumberPicker;
import edu.umd.umich.lectmarker.lib.bmData;
//import edu.umd.umich.lectmarker.lib.xmlManager; // idk why it doesn't work...

public class LectMarkerActivity extends Activity implements OnTouchListener,OnClickListener, Runnable, SeekBar.OnSeekBarChangeListener, OnItemClickListener, OnItemLongClickListener, OnLongClickListener{
	private static final String TAG = "AudioDemo";
	private static final String isPlaying = "Media is Playing"; 
	private static final String notPlaying = "Media has stopped Playing"; 
	//private Handler progressUpdate = new Handler();
	private boolean fwdThreadRunning = false;
	private boolean cancelFwdThread = false;
	private boolean rwdThreadRunning = false;
	private boolean cancelRwdThread = false;
	private Handler handler = new Handler();
	
	boolean land= false;
	boolean blankN;
	boolean blankL;
	boolean playOnStart;
	boolean pressed = false;
	boolean fromBM = false;
	boolean recordNotPlay;
	boolean bmNoteOpen= false;
	boolean inRNList = false;
	ArrayAdapter<String> adapter = null;
	ArrayAdapter<String> adapter2 = null;
	String audioFolder = Environment.getExternalStorageDirectory()+"/lectmarker/audio";
	String RNFolder = Environment.getExternalStorageDirectory()+"/lectmarker/recNotes";
	MediaPlayer player;
	MediaPlayer notePlayer;
	MediaRecorder recorder;
	SeekBar progressBar;
	bmData xmlData;
	Button pauseButton;
	Button bMarkButton;
	Button fwdButton;
	Button rwdButton;
	Button rcall;
	Button fileButton;
	Button saveButton;
	Button backButton;
	Button goToRec;
	Button playBM;
	Button goRN;
	Button recNewNote;
	Button playNote;
	EditText bmInput;
	TextView nowTime;
	TextView allTime;
	TextView track;
	TextView rnStatus;
	ListView fileList;
	ListView rnList;
	View rnListView;
	String track_name;
	String modifiedTrackName;
	String bMarkName;
	String End;
	String xmlPath;
	String recPath;
	String BM4RN; //bookmark to recordnote for
	String fBM4RN; //formatted to be used as RNName
	String currentTS;
	
	String[] bookMarkNames;		// TODO global variables here
	String[] recNoteNames;
	int[] bookMarkStart;
	int[] bookMarkEnd;
	int bookMarkNumber;
	int recNoteNumber;
	
	String trackPath;
	File newxmlfile;
	Toast bmNote;
	CountDownTimer noteTimer;
	
	final int MSG_UPDATE_POS = 0;
	final int MSG_START_PLAYBM = 1;
	final int MSG_STOP = 2;
	final int MSG_PAUSE =3;
	final int MSG_RESUME = 4;
	boolean bmPlaying=false;
	boolean bmStarted=false;
	boolean recording = false;
	
	int p;	// p is the current time (progress)...
	int[][] startEnd;	// probably don't need this now that we have bookMarkStart and bookMarkEnd

	public void onClick(View v) {
		Log.d(TAG, "onClick: " + v);
		if (v.getId() == R.id.ButtonPause) {
			fromBM = false;
			playPause();
		}	
		if (v.getId() == R.id.ButtonBookmark){
			pressed = !pressed;
			if (pressed){
				bMarkButton.setBackgroundResource(R.drawable.bookmark_icon_on);
				quickBM();
				
				if (!land){
					fileList.requestFocusFromTouch();
					
					/*String[] sortedBM = sortBM(bookMarkNames);
					int focusPos=0;
					for (int i =0;i<=bookMarkNames.length;i++){
						if (sortedBM[i].equals(bMarkName))
							focusPos = i;			
						
					}
					
					fileList.setSelection(focusPos);*/
				}
				LayoutInflater inflater = getLayoutInflater();
				View layout = inflater.inflate(R.layout.toast_layout,
				                               (ViewGroup) findViewById(R.id.toast_layout_root));

				
				TextView text = (TextView) layout.findViewById(R.id.text);
				text.setText("Press bookmark button again to end");

				bmNote = new Toast(getApplicationContext());
				bmNote.setGravity(Gravity.CENTER_VERTICAL, 0, -12);
				bmNote.setDuration(Toast.LENGTH_LONG);
				bmNote.setView(layout);
				
				//bmNote = Toast.makeText(getApplicationContext(), "Press bookmark button again to end",3600*1000);
				//bmNote.makeText(getApplicationContext(), "Press bookmark button again to end",player.getDuration());
				//bmNote.setGravity(Gravity.CENTER, 0, 0);
				bmNote.show();
				noteTimer = new CountDownTimer(90000, 1000)
				{

				    public void onTick(long millisUntilFinished) {bmNote.show();}
				    public void onFinish() {bmNote.show();}

				}.start();
				bmNoteOpen = true;
			}
			else{
				bMarkButton.setBackgroundResource(R.drawable.s_bookmark);
				End = Integer.toString(((int)(player.getCurrentPosition())/1000)-(p/1000));
				Log.i("END",End);
				Log.i("NAME",bMarkName);
				
				editBookMark(bMarkName,"",End,true);
				noteTimer.cancel();
				bmNote.cancel();
				bmNoteOpen = false;
				
				
				
				if (land){
					Intent i = getIntent();
					i.putExtra("trackLoc", Integer.toString(player.getCurrentPosition()));
					
					startActivity(i);
					finish();
				}
			}
		}
		if (v.getId() == R.id.ButtonFwd){
			fforward();
		}
		if (v.getId() == R.id.ButtonBack){
			finish();
		}
		
		if (v.getId()==R.id.ButtonPlayBook){
			if(bookMarkNumber != 0){
				fromBM = true;
				playBookMark();
			}
		}
		
		if (v.getId()==R.id.AudioNote){
			recNoteInflater();
		}
		
		if (v.getId()==R.id.RecNoteRecButton){
			
			if (recordNotPlay){	
				
				if (!recording){
					startRecording();
					recording = true;
				}
				else{
					stopRecording();
					recording = false;
				}
			}
			else{
				if (notePlayer.isPlaying()){
					notePlayer.pause();
					playNote.setText("Resume");
				}
				else{
					notePlayer.start();
					playNote.setText("Pause");
				}
			}
			
		}
		
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		Bundle extras = getIntent().getExtras(); 
		if(extras !=null) {
		   track_name = extras.getString("track");
		   playOnStart = extras.getBoolean("PON");
		   Log.i("playonstart",Boolean.toString(playOnStart));
		}
		
		trackPath = audioFolder+"/"+track_name;	// not good...
		
		//modifiedTrackName = track_name.replace(".mp3", "");
		xmlPath = audioFolder+"/"+track_name+".xml";
		
		//xmlCreator();
		
		player = new MediaPlayer();
		try {
			player.setDataSource(trackPath);
		} catch (IllegalArgumentException e) {
			
			e.printStackTrace();
		} catch (IllegalStateException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		try {
			player.prepare();
		} catch (IllegalStateException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		//Called when new bookmark created in land-view
		Bundle extras2 = getIntent().getExtras(); 
		if(extras2 !=null) {
		   if (extras2.getString("trackLoc") != null)
			   player.seekTo(Integer.parseInt(extras2.getString("trackLoc")));
		   
		}
		
		player.setLooping(false); // Set looping to false
		player.setOnCompletionListener(new OnCompletionListener(){
       	 // @Override
           public void onCompletion(MediaPlayer arg0) {
              player.seekTo(0);
              if (player.isPlaying())
              playPause();
              
           }
   });
		
		//resume play at current position in landscape view
		if (savedInstanceState!=null){ 
			player.seekTo(savedInstanceState.getInt("position"));
			playOnStart = savedInstanceState.getBoolean("playing");
		}

		// Get the buttons from the view
		pauseButton = (Button) this.findViewById(R.id.ButtonPause);
		pauseButton.setOnClickListener(this);

		bMarkButton = (Button) this.findViewById(R.id.ButtonBookmark); 
		bMarkButton.setOnClickListener(this);
		bMarkButton.setOnLongClickListener(this);

		fwdButton = (Button)this.findViewById(R.id.ButtonFwd);
		fwdButton.setOnTouchListener(this);

		rwdButton = (Button)this.findViewById(R.id.ButtonRwd);
		rwdButton.setOnTouchListener(this);
		
		backButton = (Button)this.findViewById(R.id.ButtonBack);
		backButton.setOnClickListener(this);
		
		playBM = (Button)this.findViewById(R.id.ButtonPlayBook);
		playBM.setOnClickListener(this);
		
		track = (TextView)this.findViewById(R.id.trackName);
		track.setText(track_name);

		progressBar = (SeekBar)this.findViewById(R.id.PBar);
		progressBar.setOnSeekBarChangeListener(this);

		nowTime = (TextView) findViewById(R.id.nowTime);
		allTime = (TextView) findViewById(R.id.allTime);
		trackLength();
		
		//rnStatus = (TextView) findViewById(R.id.PlayingPaused);
		
		//goRN = (Button)findViewById(R.id.AudioNote);
		//goRN.setOnClickListener(this);
		
		final LectMarkerActivity thisOnCreate = this;	// I know...
		
		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		
		if (display.getRotation() == 1){
			land = true;
			
			final Runnable runner = new Runnable() {
			    public void run() {
			    	FrameLayout fl = (FrameLayout)findViewById(R.id.frameLayout);

					seekMark lectMarks = new seekMark(thisOnCreate);
			    	lectMarks.setVisible(true);
					
			    	// runs in another thread to avoid the problem with calling
			    	// seekMark directly from onCreate
			    	
			    	fl.addView(lectMarks);
			    }
			};
			handler.postDelayed(runner, 1000);
			
		}
		
		else
			land = false;
		
		if (playOnStart)
			player.start();
		else
			pauseButton.setBackgroundResource(R.drawable.s_play);
		
		bmData xmlData = new bmData(xmlPath);
		
		
		bookMarkNames = new String[xmlData.bmNumber];
		bookMarkStart = new int[xmlData.bmNumber];
		bookMarkEnd = new int[xmlData.bmNumber];
		recNoteNames = new String[xmlData.rnNumber];
		
		bookMarkNames = xmlData.bmName;			// should add get and set methods :/
		bookMarkStart = xmlData.bmStartPoint;
		bookMarkEnd = xmlData.bmEndPoint;
		bookMarkNumber = xmlData.bmNumber;
		recNoteNames = xmlData.rnName;
		recNoteNumber = xmlData.rnNumber;
		
		
		
		createList();
		Thread currentThread = new Thread(this);
		currentThread.start();
	}
	
	/*public class seekMark extends View {
	    private int     seekX;    	// in pixels
	    private int     seekY;  	// in pixels
	    private float   pxOverMs;  	// in px/ms
	    private int[]	bmPos;		// in pixels
	    private String[]		bmNames;
	    private ShapeDrawable[]	lmark;

	    public seekMark(Context context){
	        super(context);

	        int[] xyLoc = new int[2];
	        progressBar.getLocationOnScreen(xyLoc); // the y is off by about 40 (on my phone)...
	        
	        seekX = xyLoc[0] + dpiCorrectedPx(3);
	        seekY = xyLoc[1] - pauseButton.getHeight() + dpiCorrectedPx(5);
	        pxOverMs = pxPerMs();
	        
			bmNames = bmNameList(xmlPath); // must come before bmPos
	
	        
	        try {
				bmPos = markPxList();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	        
	        lmark = new ShapeDrawable[bmPos.length];
	        
	        for(int i = 0; i < bmPos.length; i++){
	        	lmark[i] = new ShapeDrawable(new OvalShape());
	        }
	    }

	    private float pxPerMs(){
	        return ((float) progressBar.getWidth())/((float) player.getDuration());
	    }

	    private int[] markPxList() throws XmlPullParserException, IOException {

	        int bmStartTime = 0;
	        int[] bmPos = new int[bmNames.length];

	        for(int i=0; i < bmNames.length; i++){
	            bmStartTime = getBookmark(xmlPath, bmNames[i]);
	            bmPos[i] =  (int) (bmStartTime * pxOverMs);
	        }
	        return (bmPos); // this value shouldn't need dpi correction cause it's derived from progressBar length
	    }
	    
	    protected void onDraw(Canvas canvas) {
	    	int x = 0;
	    	int y = seekY;
	    	
	    	Paint textPaint = new Paint();
	    	textPaint.setAntiAlias(true);
	    	textPaint.setColor(Color.WHITE);
	    	textPaint.setTextSize(10);
	    	
	    	Paint linePaint = new Paint();
	    	linePaint.setStrokeWidth(1);
	    	linePaint.setColor(Color.WHITE);
	    	
	    	for(int i = 0; i < bmPos.length; i++){
	    		x = bmPos[i] + seekX + dpiCorrectedPx(5);
	            //Log.d("Lectmarker","i = " + i + " x = " + x + " y = " + y);
	            //Log.d("Lectmarker","dpiCorrectedPx(i) = " + dpiCorrectedPx(i));
	            lmark[i].getPaint().setColor(Color.RED);
	            lmark[i].setBounds(x-dpiCorrectedPx(5), y-dpiCorrectedPx(4), x+dpiCorrectedPx(5), y+dpiCorrectedPx(5));
	    		lmark[i].draw(canvas);
	    		
	    		x -= dpiCorrectedPx(1);
	    		canvas.drawLine(x, y , x, y-dpiCorrectedPx(47), linePaint); // starting x,y ending x,y
	    		if (bmNames[i].length() > 4)
	    			canvas.drawText(bmNames[i].substring(0, 4), x, y-dpiCorrectedPx(50), textPaint);
	    		else
	    			canvas.drawText(bmNames[i], x, y-dpiCorrectedPx(50), textPaint);
	    	}
	    }
	    
	    private int dpiCorrectedPx(int val){
	    	Resources r = getResources();
	    	return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, r.getDisplayMetrics()));
	    }
	    
	    public void setVisible(boolean isVisible){
	    	for(int i = 0; i < bmPos.length; i++){
	    		lmark[i].setVisible(isVisible, true);
	    	}
	    }

	}
	*/
	
	public class seekMark extends View {
	    private int     seekX;    	// in pixels
	    private int     seekY;  	// in pixels
	    private float   pxOverMs;  	// in px/ms
	    private int[]	bmPos;		// in pixels
	    private String[]		bmNames;
	    private ShapeDrawable[]	lmark;
	    private int 	correctedFontSize;
	    private int 	screenHeight;

	    public seekMark(Context context){
	        super(context);
	        
	        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
	    	screenHeight = display.getHeight();	// needed by some methods (KEEP AT TOP)

	        int[] xyLoc = new int[2];
	        progressBar.getLocationOnScreen(xyLoc); // the y is off by about 40 (on my phone)...
	        
	        seekX = xyLoc[0] + dpiCorrectedPx(3);
	        seekY = xyLoc[1] - pauseButton.getHeight() + dpiCorrectedPx(5);
	        pxOverMs = pxPerMs();
	        correctedFontSize = fontSize();
	        
			bmNames = bmNameList(xmlPath); // must come before bmPos
	        
	        try {
				bmPos = markPxList();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	        
	        lmark = new ShapeDrawable[bmPos.length];
	        for(int i = 0; i < bmPos.length; i++){
	        	lmark[i] = new ShapeDrawable(new OvalShape());
	        }
	        
	        Log.v("Lectmarker","correctedFontSize = " + correctedFontSize);
	        Log.v("Lectmarker","screenHeight = " + screenHeight);
	    }

	    private int[] markPxList() throws XmlPullParserException, IOException { // probably don't need the exceptions

	        int bmStartTime = 0;
	        int[] bmPos = new int[bmNames.length];

	        for(int i=0; i < bmNames.length; i++){
	            bmStartTime = getBookmark(xmlPath, bmNames[i]);
	            bmPos[i] =  (int) (bmStartTime * pxOverMs);
	        }
	        return (bmPos); // this value shouldn't need dpi correction cause it's derived from progressBar length
	    }
	    
	    protected void onDraw(Canvas canvas) {
	    	int x = 0;
	    	int y = seekY;
	    	
	    	Paint textPaint = new Paint();
	    	textPaint.setAntiAlias(true);
	    	textPaint.setColor(Color.WHITE);
	    	textPaint.setTextSize(correctedFontSize);
	    	
	    	Paint linePaint = new Paint();
	    	linePaint.setStrokeWidth(1);
	    	linePaint.setColor(Color.WHITE);
	    	
	    	for(int i = 0; i < bmPos.length; i++){
	    		x = bmPos[i] + seekX + dpiCorrectedPx(5);
	            lmark[i].getPaint().setColor(Color.RED);
	            lmark[i].setBounds(x-dpiCorrectedPx(5), y-dpiCorrectedPx(4), x+dpiCorrectedPx(5), y+dpiCorrectedPx(5));
	    		lmark[i].draw(canvas);
	    		
	    		x -= dpiCorrectedPx(1);
	    		canvas.drawRect(x, tabHeight(i), x+dpiCorrectedPx(1), y, linePaint);
	    		//canvas.drawLine(x, y , x, tabHeight(i), linePaint); // starting x,y ending x,y
	    		if (bmNames[i].substring(0, 8).equals("Bookmark"))
	    			canvas.drawText("BM"+bmNames[i].substring(8, bmNames[i].length()), x, tabHeight(i)-dpiCorrectedPx(3), textPaint);
	    		else
	    			canvas.drawText(bmNames[i], x, tabHeight(i)-dpiCorrectedPx(3), textPaint);
	    	}
	    }
	    
	    private int tabHeight(int count){
	    	// remember the top left corner is (0,0) *hoot*
	    	int availableYSpace = screenHeight - seekY - bMarkButton.getHeight() - dpiCorrectedPx(5); // might have to fine tune this...
	    	int levelHeight = availableYSpace/4;
	    	
	    	switch (count % 4){
	    		case 3:
	    			return(seekY-levelHeight);
	    		case 2:
	    			return(seekY-(2*levelHeight));
	    		case 1:
	    			return(seekY-(3*levelHeight));
	    		case 0:
	    			return(seekY-(4*levelHeight));
	    	}
			return levelHeight; // why do i need this ><...
	    }
	    
	    private int dpiCorrectedPx(int val){
	    	Resources r = getResources();
	    	int correctedPx = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, r.getDisplayMetrics()));
	    	if (correctedPx == 0)
	    		return 1;
	    	else
	    		return correctedPx;
	    }
	    
	    private float pxPerMs(){
	        return ((float) progressBar.getWidth())/((float) player.getDuration());
	    }
	    
	    private int fontSize(){
	    	// my phone's height is 240 and my perfect font size is 10, and we want a
	    	// factor x to multiple height by to get font size SO...
	    	// 240x=10; x=(1/24) or 0.04166666666666666667
	    	double heightFontFactor = 0.04166666666666666667;
	    	return (int)((double)(screenHeight)*(heightFontFactor)); // size=(height)(factor)
	    }
	    
	    public void setVisible(boolean isVisible){
	    	for(int i = 0; i < bmPos.length; i++){
	    		lmark[i].setVisible(isVisible, true);
	    	}
	    }

	}
	public void createList(){
		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		
		//it will only use TheFileList1 if it's not rotated
		Log.d("Lectmarker","Roation "+display.getRotation());
		if (display.getRotation () == 0)	// possible values 0 1 2?
		{
			Log.d("Lectmarker","not rotated creating TheFileList1");
			fileList = (ListView) findViewById(R.id.TheFileList1);
			Log.i("bmNumber1",Integer.toString(bookMarkNumber));
			adapter = new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1,
						sortBM(bmNameList(xmlPath)));
			fileList.setAdapter(adapter);
			Log.i("bmNumberL",Integer.toString(bookMarkNumber));
			fileList.setOnItemClickListener(this);
			fileList.setOnItemLongClickListener(this);
			fileList.setItemsCanFocus(true);
			fileList.requestFocusFromTouch();
		}
	}
	
	public void createRNList(View v){
		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		
		//it will only use TheFileList1 if it's not rotated
		Log.d("Lectmarker","Roation "+display.getRotation());
		if (display.getRotation () == 0)	// possible values 0 1 2?
		{
			Log.d("Lectmarker","not rotated creating RNList");
			rnList = (ListView) v.findViewById(R.id.RecNoteList);
			adapter2 = new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1,rnNameList());
			
			rnList.setAdapter(adapter2);
			
			rnList.setOnItemClickListener(this);
			rnList.setOnItemLongClickListener(this);
			rnList.setItemsCanFocus(true);
			rnList.requestFocusFromTouch();
		}
	}
	
	// TODO should rename functions to reflect they're true function
	// also to remove the dontCare's left in during the switch
	public int getBookmark(String dontCare, String bmSearch){
		int i = 0;
		for(; !bmSearch.equals(bookMarkNames[i]); i++);
		return bookMarkStart[i];
	}
	
	public int getBookmarkIND(String dontCare, String bmSearch){
		int i = 0;
		for(; !bmSearch.equals(bookMarkNames[i]); i++);
		return i;
	}
	
	
	public int getEndBookmark(String dontCare, String bmSearch){
		int i = 0;
		for(; !bmSearch.equals(bookMarkNames[i]); i++);
		return bookMarkEnd[i];
	}
	
	public String[] bmNameList(String dontCare){
		return bookMarkNames;
	}
	
	public String[] rnNameList(){
		
		int menuLength= 0;
		
		for (int i =0;i<recNoteNames.length;i++){
			if (recNoteNames[i].contains(fBM4RN))
				menuLength++;
		}
		
		String[] menuList = new String[menuLength+1];
		menuList[0] = "New Audio Note";
		
		
		
		if(menuLength > 0){
			int used = 999;
			int j = 0;
		for (int i = 1;i<menuList.length;i++){
			boolean gotOne = false;
			
			while (j<recNoteNames.length&&!gotOne){
				if (j!=used&&recNoteNames[j].contains(fBM4RN)){
					gotOne = true;
					used = j;
					menuList[i] = recNoteNames[j].replace(fBM4RN, "");
					
					Log.i("rNLength",Integer.toString(menuLength));
					Log.i("rnName",recNoteNames[i-1]);
				}
				j++;
			}
		}
		}
		
		
		return menuList;
	}
	
	public String[] sortBM(String[] dontCare) {
		Log.i("bmNameLength",Integer.toString(bookMarkNames.length));
		Log.i("bmNumber",Integer.toString(bookMarkNumber));
		return bookMarkNames;
		
	}
	
	// do we need this?
	public String[] copyArray(String[] array,int size) {
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
	
	public class xmlManager{	// TODO work on moving class completely to lib later...
		private File tempxml;
		private	String xmlFile; 
		
		
		public xmlManager(String xFile){
			tempxml = new File(Environment.getExternalStorageDirectory()+ "/lectmarker/temp.xml");
			xmlFile = xFile;
		}
		
		private void createXml(File newxmlfile){

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
		
		private void modifyXml(boolean recNote,String rnName,  String name, String startEndVal, boolean addNotDelete, boolean startNotEnd) throws IllegalArgumentException, IllegalStateException, IOException, XmlPullParserException {
			File myxml = new File(xmlFile);
			boolean end = false;		// don't really know what this is... *cough* -.-
			boolean thisOne = false;	// don't really know what this is...
			boolean nextIsRN = false; //next start tag is the correct recNote tag
			boolean keepNext = false; //keep the next start tag which is a recNote
			boolean newRN = false;
			int parses= 0;
			XmlSerializer serializer = Xml.newSerializer();
			
			createXml(myxml);
				
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
		       		if((bookMarkNumber == 0) && startNotEnd){	// if nothing in xml then just add
		       			serializer.startTag(null,"bookmark"); 
		       			serializer.attribute(null, "name",name);
		       			serializer.attribute(null, "start", startEndVal);
		       			/*serializer.startTag(null,"recNote");
		       			if (recNote){
		       				serializer.attribute(null, "rnName", rnName);
		       			}
		       			serializer.endTag(null, "recNote");*/
		       			//serializer.attribute(null, "end", endVal);
		       			serializer.endTag(null, "bookmark");
		       		}
		       	 
		       	} else if(eventType == XmlPullParser.START_TAG) {
		       			if (xpp.getName().equals("bookmark"))
		       				parses++;
		       			serializer.startTag(null, xpp.getName());
		       		
		       			for(int i=0;i<xpp.getAttributeCount();i++){	// copying over all data
		       				if(xpp.getAttributeCount()!=0)
		       				serializer.attribute(null, xpp.getAttributeName(i), xpp.getAttributeValue(i));
		       			}
		       			if(parses == bookMarkNumber )	// is all data copied?
		       				end = true;
		       			
		       			/*if (nextIsRN){
		       				if(xpp.getAttributeCount()==0)
		       					serializer.attribute(null, "rnName", rnName);
		       				else{
		       					newRN = true;
		       				}
		       				nextIsRN = false;
		       			}*/
		       			
		       			/*if (xpp.getName().equals("recNote") && xpp.getAttributeCount()!=0&&xpp.getAttributeValue(null, "rnName").equals(rnName)){
		       				serializer.attribute(null, "rnName", rnName);
		       			}*/
		       			
		       			if (xpp.getName().equals("bookmark") && xpp.getAttributeValue(null, "name").equals(name)){
			        		if (recNote)
			        			//nextIsRN = true;
			        			newRN=true;
			        			
			        		else if (startNotEnd){
				            	 serializer.attribute(null, "start", startEndVal);
				             }
				             else{
				            	 serializer.attribute(null, "end",startEndVal);
				             }
			        	 }
		       	 
		       	} else if(eventType == XmlPullParser.END_TAG) {	// adds new bookmark
		       		
		       		if (newRN&&xpp.getName().equals("bookmark")) {
		       			serializer.startTag(null, "recNote");
		       			serializer.attribute(null, "rnName", rnName);
		       			serializer.endTag(null, "recNote");
		       			newRN=false;
		       		}
		       		serializer.endTag(null,xpp.getName());
		       		
		       		
		       		if (end && startNotEnd &&xpp.getName().equals("bookmark")) {	// isn't this in the END_TAG anyways..?
		       			Log.i("new bookmark",name);
		       			serializer.startTag(null,"bookmark"); 
		       			serializer.attribute(null, "name",name);
		       			serializer.attribute(null, "start", startEndVal);
		       			//serializer.attribute(null, "end", endVal);
		       			/*serializer.startTag(null, "recNote");
		       			if (recNote){	
		       				serializer.attribute(null, "rnName", rnName);
			       		}
		       			serializer.endTag(null, "recNote");*/
		       			serializer.endTag(null, "bookmark");
		       		}
		            
		       	} else if(eventType == XmlPullParser.TEXT) {} 
		        
		       	eventType = xpp.next();
		    }
		    
		    while ((eventType != XmlPullParser.END_DOCUMENT) && !addNotDelete) { //deleting
				if(eventType == XmlPullParser.START_DOCUMENT) {}
				else if(eventType == XmlPullParser.START_TAG) {
					if ((xpp.getName().equals("bookmark")&&!xpp.getAttributeValue(null,"name").equals(name))){
						serializer.startTag(null, xpp.getName());
        	 
						for(int i=0;i<xpp.getAttributeCount();i++){
							serializer.attribute(null, xpp.getAttributeName(i), xpp.getAttributeValue(i)); 
						}
						keepNext = true;
						
					}
					if (xpp.getName().equals("recNote")&&keepNext){
						serializer.startTag(null, xpp.getName());
			        	 
						for(int i=0;i<xpp.getAttributeCount();i++){
							serializer.attribute(null, xpp.getAttributeName(i), xpp.getAttributeValue(i)); 
						}
					}
					if (xpp.getName().equals("bookmark")&&xpp.getAttributeValue(null,"name").equals(name)){
						thisOne = true;
						keepNext =false;
					}
								
        	 
				} else if(eventType == XmlPullParser.END_TAG) {
					if (!thisOne){
						serializer.endTag(null,xpp.getName());
					}
					else if(xpp.getName().equals("bookmark")){
						thisOne = false;
					}
					
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
	        
	        // regenerating xmlData
	        xmlData = new bmData(xmlPath);
	        
	        bookMarkNames = new String[xmlData.bmNumber];
			bookMarkStart = new int[xmlData.bmNumber];
			bookMarkEnd = new int[xmlData.bmNumber];
			recNoteNames = new String[xmlData.rnNumber];
			
			bookMarkNames = xmlData.bmName;
			bookMarkStart = xmlData.bmStartPoint;
			bookMarkEnd = xmlData.bmEndPoint;
			bookMarkNumber = xmlData.bmNumber;
			recNoteNames = xmlData.rnName;
			recNoteNumber = xmlData.rnNumber;
	        
		}
		
		public void addToXml(String name ,String startEndVal ,boolean startNotEnd) {
			try {
				modifyXml(false,"hi",name, startEndVal, true, startNotEnd);
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
		
		public void addToXml(String rnName,String name ,String startEndVal ,boolean startNotEnd) {
			try {
				modifyXml(true,rnName,name, startEndVal, true, startNotEnd);
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
				modifyXml(false,"hi",name, "404", false, true);	// any bogus value would do
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
	
	public void addToXml(String path, String name, boolean start, String val) {
		xmlManager editBM = new xmlManager(path);
		editBM.addToXml(name, val, start);
	}
	
	public void addRNToXml(String rnName,String name, String path) {
		xmlManager editBM = new xmlManager(path);
		editBM.addToXml(rnName,name, null, false);
	}
	
	
	public void deleteFromXml(String path, String name) {
		xmlManager editBM = new xmlManager(path);
		editBM.deleteFromXml(name);
	}
	
	public void playBookMark() {
		
		startEnd = new int[bookMarkNumber][2];	// probably could be replaced
		int endpos= 0;
		int startpos;
		for (int i=0;i<bookMarkNumber;i++){
				startpos = getBookmark(xmlPath, bookMarkNames[i]);
			
			if (endpos > startpos)
				startpos = endpos;
			
			startEnd[i][0] = startpos;
			endpos = getEndBookmark(xmlPath, bookMarkNames[i]);
			startEnd[i][1] = endpos;
		}
		
		if (!bmStarted){
			mHandler.sendEmptyMessage(MSG_START_PLAYBM);
			bmStarted = true;
			bmPlaying = true;
		}
		else if (bmPlaying){
			Log.i("in","pause");
			mHandler.sendEmptyMessage(MSG_PAUSE);
			bmPlaying = false;
		}
		else{
			Log.i("in","resume");
			mHandler.sendEmptyMessage(MSG_RESUME);
			bmPlaying = true;
		}
		Log.d("playBM","Finished");
		
	}
	int i=0;
	Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            
            
            int time;
            int nextstart;
            int end; 
            int icount = bookMarkNumber;
            switch (msg.what) {
            
            
            case MSG_START_PLAYBM:
            	if(!land)
            	{
            		fileList.requestFocusFromTouch();
            		fileList.setSelection(0);
            	}
            	player.seekTo(startEnd[0][0]);
            	player.start();
            	
            	playBM.setBackgroundResource(R.drawable.s_pausebm);
            	pauseButton.setBackgroundResource(R.drawable.s_pause);
                
                mHandler.sendEmptyMessage(MSG_UPDATE_POS);
                break;

            case MSG_UPDATE_POS:
            	time = player.getCurrentPosition();
            	
            	if (i+1<icount)
            		nextstart = startEnd[i+1][0];
            	else
            		nextstart = -1;
            	end = startEnd[i][1];
            	
                
                if (time >= end){
                	if (nextstart ==-1){
                		mHandler.sendEmptyMessage(MSG_STOP);
                		break;
                	
                	}
                	player.seekTo(nextstart);
                	
                	i++;
                	
                	if (!land)
                	{
                		fileList.requestFocusFromTouch();
                	
                		fileList.setSelection(i);
                	}
                	
                	
                }
                
                mHandler.sendEmptyMessageDelayed(MSG_UPDATE_POS,100); //text view is updated every second, 
                break; 
                
             
            case MSG_PAUSE:
            	//player.pause();
            	if(!land)
            	{
            		fileList.requestFocusFromTouch();
            		fileList.setSelection(i);
            	}
            	playPause();
            	playBM.setBackgroundResource(R.drawable.s_playbm);
            	
            	pauseButton.setBackgroundResource(R.drawable.s_play);
            	break;
            	
            case MSG_RESUME:
            	//player.start();
            	if (!land)
            	{
            		fileList.requestFocusFromTouch();
            		fileList.setSelection(i);
            	}
            	playPause();
            	playBM.setBackgroundResource(R.drawable.s_pausebm);
            	pauseButton.setBackgroundResource(R.drawable.s_pause);
            	mHandler.sendEmptyMessage(MSG_UPDATE_POS);
            	break;

            case MSG_STOP:
            	/*player.stop();
            	mHandler.removeMessages(MSG_UPDATE_POS); // no more updates.
            	*/
            	playBM.setBackgroundResource(R.drawable.s_playbm);
            	pauseButton.setBackgroundResource(R.drawable.s_play);
            	
            	playPause();
            	bmStarted = false;
            	i=0;
                
                break;

            default:
                break;
            }
        }
    };
	
	
	public void trackLength(){
		int length = player.getDuration()/1000;
		String sTime = (length < 3600) ? String.format("%d:%02d", length/60, length % 60) 
				:	String.format("%d:%02d:%02d", length/3600, (length % 3600)/60, length % 60);
			allTime.setText(sTime);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		player.pause();
	}

	// Initiate media player pause
	private void demoPause(){
    player.pause();
    pauseButton.setBackgroundResource(R.drawable.s_play);
    Log.d(TAG, notPlaying);
	}
	
	// Initiate playing the media player
	private void demoPlay(){
    player.start();
    pauseButton.setBackgroundResource(R.drawable.s_pause);
    Log.d(TAG, isPlaying);
	}
	
	// Toggle between the play and pause
	private void playPause() {
		if(player.isPlaying()) {
		  demoPause();
		  if(!fromBM)
			  bmStarted = false;
		  playBM.setBackgroundResource(R.drawable.s_playbm);
		} else {
		  demoPlay();
		  
		}	
	}
	
	private void fforward(){
		player.seekTo(player.getCurrentPosition()+1000);
	}
	
	private void rewind(){
		player.seekTo(player.getCurrentPosition()-1000);
	}
	
	public void quickBM() {
		p = player.getCurrentPosition();
		int p1 = p/1000;
		final String formatP = p1 < 3600 ? String.format("%d:%02d", p1/60, p1 % 60) 
				:	String.format("%d:%02d:%02d", p1/3600, (p1 % 3600)/60, p1 % 60);
        
		int bmLength1 = (p+30000)/1000;
		@SuppressWarnings("unused")
		String formatEnd =  bmLength1 < 3600 ? String.format("%d:%02d", bmLength1/60, bmLength1 % 60) 
 				:	String.format("%d:%02d:%02d", bmLength1/3600, (bmLength1 % 3600)/60, bmLength1 % 60);
 				
		String timespace = " ("+formatP+" - "+")";
		int n = (bookMarkNumber+1);
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
		
		Log.i("bmNumber",Integer.toString(bookMarkNumber));
		addToXml(xmlPath,getBMN+timespace,true,Integer.toString(p));
		addToXml(xmlPath,getBMN+timespace,false,Integer.toString(p+30000));
		
		adapter = null;
		fileList = null;
		Log.i("createdlist","this many times");
		createList();
		if (!land){
		fileList.requestFocusFromTouch();
		fileList.setSelection(getBookmarkIND("",getBMN+timespace));}
		
	}
	
	public void bookmark() {
		p = player.getCurrentPosition();
		bmInflater();
	}
	
	public void bmInflater(){
		LayoutInflater factory = LayoutInflater.from(this);            
        final View editBMView = factory.inflate(R.layout.bookmarkname, null);
        int p1 = p/1000;
        final String formatP = p1 < 3600 ? String.format("%d:%02d", p1/60, p1 % 60) 
				:	String.format("%d:%02d:%02d", p1/3600, (p1 % 3600)/60, p1 % 60);
        
       
        AlertDialog.Builder alert = new AlertDialog.Builder(this); 

        alert.setTitle("Bookmark"); 
        alert.setView(editBMView); 
       
        final EditText input1 = (EditText) editBMView.findViewById(R.id.nameInput);
        final NumberPicker min = (NumberPicker) editBMView.findViewById(R.id.min);
        final NumberPicker sec = (NumberPicker) editBMView.findViewById(R.id.sec);
        //final EditText input2 = (EditText) editBMView.findViewById(R.id.lengthInput);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
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
    			  if (land){
  					Intent i = getIntent();
  					i.putExtra("trackLoc", Integer.toString(player.getCurrentPosition()));
  					
  					startActivity(i);
  					finish();
  				}
    			   
  		  if (getBMN.equals("")||getBMN.equals(null)){
  			 
  			  int n = (bookMarkNumber+1);
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
  		 
  		  addToXml(xmlPath,bMarkName,true,Integer.toString(p));
  		  addToXml(xmlPath,bMarkName,false,Integer.toString(p+bmLength));
			
  			
  		adapter = null;
  		fileList = null;
  		createList();

  		  }
  		});

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { 
          public void onClick(DialogInterface dialog, int whichButton) { 
            // Canceled. 
          } 
        }); 
        
        alert.show();
	}
	
	public void editBookMark(String listItem, String name,String End,boolean open){
		int p1 = p/1000;
        final String formatP = p1 < 3600 ? String.format("%d:%02d", p1/60, p1 % 60) 
				:	String.format("%d:%02d:%02d", p1/3600, (p1 % 3600)/60, p1 % 60);
		
			p = getBookmark(xmlPath,listItem);
		
			if (End.equals("")||End.equals(null)||End.equals(Integer.toString((getEndBookmark(xmlPath,listItem)-p)/1000))){
				  blankL = true;
				  End = Integer.toString((getEndBookmark(xmlPath,listItem)-p)/1000);
			}
			  
		int currentEnd = 0;
			currentEnd = getEndBookmark(xmlPath,listItem)/1000;
		
		String formatCurrentEnd = currentEnd < 3600 ? String.format("%d:%02d", currentEnd/60, currentEnd % 60) 
	 				:	String.format("%d:%02d:%02d", currentEnd/3600, (currentEnd % 3600)/60, currentEnd % 60);
		//String currentTS;
		
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
		  getBMN = listItem;
		  blankN = true;
		  bMarkName = getBMN.replace(currentTS, timespace);
			  
	  }
	  else
		  bMarkName = getBMN + timespace;
	  
	  if (!blankN){	
		  	addToXml(xmlPath,bMarkName,true,Integer.toString(p));
			if (blankL)
					addToXml(xmlPath,bMarkName,false,Integer.toString(getEndBookmark(xmlPath,listItem)));
			else  // else needs brackets?
				addToXml(xmlPath,bMarkName,false,Integer.toString(p+bmLength));
				
			deleteFromXml(xmlPath,listItem);
		}
		else{
			if (!blankL){
				addToXml(xmlPath,bMarkName,true,Integer.toString(p));
				addToXml(xmlPath,bMarkName,false,Integer.toString(p+bmLength));
				deleteFromXml(xmlPath,listItem);
			}
		}
		
		adapter = null;
		fileList = null;
		createList();
		if (!land){
		fileList.requestFocusFromTouch();
		fileList.setSelection(getBookmarkIND("",bMarkName));}
	}
	
	public void editBMInflater(View arg1){
		final String listItem = (String) ((TextView) arg1).getText();
		//Creates formatted shortname of bm to be used as audio note file name
		BM4RN = listItem;
		fBM4RN = BM4RN.replaceAll(" ", "");
        int stophere = fBM4RN.indexOf("(");
        fBM4RN = fBM4RN.substring(0, stophere);
        Log.i("fBM4RN",fBM4RN);
		final View arg = arg1;
		int p1 = p/1000;
        @SuppressWarnings("unused")
		final String formatP = p1 < 3600 ? String.format("%d:%02d", p1/60, p1 % 60) 
				:	String.format("%d:%02d:%02d", p1/3600, (p1 % 3600)/60, p1 % 60);
		
				p = getBookmark(xmlPath,listItem);

		
		LayoutInflater factory = LayoutInflater.from(this);            
        final View editBMView = factory.inflate(R.layout.bookmarkname, null);
        blankN = false;
        blankL = false;
        
        AlertDialog.Builder alert = new AlertDialog.Builder(this); 

        alert.setTitle("Edit/Delete");  
        // Set an EditText view to get user input  
        alert.setView(editBMView); 
       

        final EditText input1 = (EditText) editBMView.findViewById(R.id.nameInput);
        final NumberPicker min = (NumberPicker) editBMView.findViewById(R.id.min);
        final NumberPicker sec = (NumberPicker) editBMView.findViewById(R.id.sec);
        goRN = (Button)editBMView.findViewById(R.id.AudioNote);
		goRN.setOnClickListener(this);
		
        
        //final EditText input2 = (EditText) editBMView.findViewById(R.id.lengthInput);

        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int whichButton){ 
    			End = Integer.toString((min.getValue()*60)+sec.getValue());
    			String getBMN =input1.getText().toString();
    			
    			editBookMark(listItem,getBMN,End,false);}
  		});

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { 
          public void onClick(DialogInterface dialog, int whichButton) { 
            // Canceled. 
          } 
        }); 
        alert.setNeutralButton("Delete", new DialogInterface.OnClickListener() { 
            public void onClick(DialogInterface dialog, int whichButton) { 
              confirmDelete(listItem,arg); 
            	
            } 
          }); 
        
        alert.show();
	}
	
	public void recNoteInflater(){
		inRNList = true;
		LayoutInflater factory = LayoutInflater.from(this);            
        //final View editBMView = factory.inflate(R.layout.rec_note1, null);
        rnListView = factory.inflate(R.layout.rec_note1, null);
               
        AlertDialog.Builder alert = new AlertDialog.Builder(this); 

        alert.setTitle("Audio Notes");  
        // Set an EditText view to get user input  
        alert.setView(rnListView);
        Log.i("LectMarker","recreating list");
        createRNList(rnListView);
        
        rnStatus = (TextView)rnListView.findViewById(R.id.RNStatus);
        
        alert.setNegativeButton("Close", new DialogInterface.OnClickListener() { 
            public void onClick(DialogInterface dialog, int whichButton) { 
              // Canceled.
            	inRNList = false;
            } 
          });
        alert.show();
	}
	
	public void newRN(){
		LayoutInflater factory = LayoutInflater.from(this);            
        final View newRec = factory.inflate(R.layout.rec_note_recorder, null);
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this); 

        alert.setTitle("New Recording");  
        alert.setView(newRec);
        alert.setOnCancelListener(new OnCancelListener(){
			@Override
			public void onCancel(DialogInterface arg0) {
				xmlData = new bmData(xmlPath);
				adapter2 = null;
                rnList = null;
            	createRNList(rnListView);
				
				
			}
        });
        
        recNewNote = (Button)newRec.findViewById(R.id.RecNoteRecButton);
        recNewNote.setOnClickListener(this);
        
        recordNotPlay = true;
        
        alert.setNegativeButton("Close", new DialogInterface.OnClickListener() { 
            public void onClick(DialogInterface dialog, int whichButton) { 
              // Canceled. 
            	xmlData = null;
            	xmlData = new bmData(xmlPath);
            	adapter2 = null;
                rnList = null;
            	createRNList(rnListView);
            } 
          });
        
        alert.show();
	}
	
	public void playRN(String name) throws IllegalArgumentException, IllegalStateException, IOException{
		LayoutInflater factory = LayoutInflater.from(this);            
        final View newRec = factory.inflate(R.layout.rec_note_recorder, null);
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this); 

        alert.setTitle("Play Audio Note");  
        alert.setView(newRec);
        
        playNote = (Button)newRec.findViewById(R.id.RecNoteRecButton);
        playNote.setOnClickListener(this);
        playNote.setText("Pause");
        
        recordNotPlay = false;
        
        notePlayer = new MediaPlayer();
        notePlayer.setDataSource(RNFolder+"/"+fBM4RN+name+".3gp");
        notePlayer.prepare();
        notePlayer.setLooping(false);
        notePlayer.setOnCompletionListener(new OnCompletionListener(){
        	 // @Override
            public void onCompletion(MediaPlayer arg0) {
               notePlayer.seekTo(0);
               //notePlayer.pause();
               playNote.setText("Play");
            }
    });
        notePlayer.start();
        
        alert.setNegativeButton("Close", new DialogInterface.OnClickListener() { 
            public void onClick(DialogInterface dialog, int whichButton) { 
            	notePlayer.stop();
            	notePlayer.release();
             
            } 
          });
        
        alert.show();
		
	}
	
	private void startRecording() {
        boolean gotPath = false;
        int n =1;
        String fullRNName = fBM4RN;
		while (!gotPath){
			
			if (new File(RNFolder + "/"+ fBM4RN+"-RecNote" +n+".3gp").exists())
				n++;
			else{
				fullRNName = fBM4RN+"-RecNote"+n;
				recPath = RNFolder + "/"+fBM4RN+"-RecNote" +n+".3gp";
				gotPath = true;
			}
			Log.i("in while","");
		}
		addRNToXml(fullRNName,BM4RN,xmlPath);
		Log.i("recPath",recPath);
		recorder = new MediaRecorder();
		recorder.reset();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(recPath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setAudioEncodingBitRate(24);
        recorder.setAudioSamplingRate(44100);
        
        

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("", "prepare() failed");
        }

        recorder.start();
        recNewNote.setText("Stop");
    }
	
	private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
        rnStatus.setText("Stopped");
        
        adapter2 = null;
        rnList = null;
        createRNList(rnListView);
        recNewNote.setText("Record");
        
    }
	
	public void confirmDelete(String BM, View arg1){
		final String listItem = BM;
		final View arg = arg1;
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("DELETE");
		alert.setMessage("Delete Bookmark?");

		// Set an EditText view to get user input 
		

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
			
			deleteFromXml(xmlPath,listItem);
			
			adapter = null;
			fileList = null;
			createList();
		  // Do something with value!
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
			  editBMInflater(arg);
		  }
		});

		alert.show();
	}
	
	
	public boolean checkBM(String string){
		boolean exists = false;
		for(int i=0;i<bookMarkNumber;i++){
			  if(bookMarkNames[i].contains(string)){
				  exists =true;
			  }
			  
		}
		return exists;
	}
	public void lengthWindow(){
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Bookmark");
		alert.setMessage("Bookmark Length (seconds)");

		// Set an EditText view to get user input 
		final EditText Linput = new EditText(this);
		alert.setView(Linput);
		

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  
		  End = Linput.getText().toString();
		  if (End.equals(null))
			  End = "10";
		  int bmLength = Integer.parseInt(End)*1000;
		  
		  addToXml(xmlPath,bMarkName,false,Integer.toString(p+bmLength));
		  
		  // Do something with value!
		  }
		});
		
		

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});

		alert.show();
	}
	
	private void playBack(String BMName) {
		int time;
		time = getBookmark(xmlPath,BMName);
		
		player.seekTo(time);

	}
	
	@Override
	public void run() {
        // progress is your ProgressBar
 
        int currentPosition = 0;
        int total = player.getDuration();
        progressBar.setMax(total);
        while (player != null && currentPosition < total) {
            try {
                Thread.sleep(1000);
                currentPosition = player.getCurrentPosition();
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                return;
            }
            progressBar.setProgress(currentPosition);
        }
    }
    
    
	@Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    	
    	if (fromUser) {
            player.seekTo(progress);
            progress = player.getCurrentPosition()/1000;
        	String sTime = (progress < 3600) ? String.format("%d:%02d", progress/60, progress % 60) 
    				:	String.format("%d:%02d:%02d", progress/3600, (progress % 3600)/60, progress % 60);
    			nowTime.setText(sTime);
        }
    	else{
    		progress = player.getCurrentPosition()/1000;
        	String sTime = (progress < 3600) ? String.format("%d:%02d", progress/60, progress % 60) 
    				:	String.format("%d:%02d:%02d", progress/3600, (progress % 3600)/60, progress % 60);
    			nowTime.setText(sTime);
    	}
    	
    }

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		
		switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            {
                if (arg0.getId()== R.id.ButtonFwd){
                	handleFwdDown();
                }
                if (arg0.getId()== R.id.ButtonRwd){
                	handleRwdDown();
                }
                
                return false;
            }

            case MotionEvent.ACTION_UP:
            {
            	if (arg0.getId()== R.id.ButtonFwd){
                	handleFwdUp();
                }
                if (arg0.getId()== R.id.ButtonRwd){
                	handleRwdUp();
                }
                return false;
            }

            default:
                return false;
        }
     }
	
	 private void handleFwdDown() {

         if (!fwdThreadRunning)
             startFwdThread();
     }

     private void startFwdThread() {

         Thread r = new Thread() {

             @Override
             public void run() {
                 try {

                     fwdThreadRunning = true;
                     while (!cancelFwdThread) {

                         handler.post(new Runnable() {   
                             @Override
                             public void run() {
                                 fforward();
                             }
                         });

                         try {
                             Thread.sleep(100);
                         } catch (InterruptedException e) {
                             throw new RuntimeException(
                                 "Could not wait between char delete.", e);
                         }
                     }
                 }
                 finally
                 {
                     fwdThreadRunning = false;
                     cancelFwdThread = false;
                 }
             }
         };

         // actually start the delete char thread
         r.start();
     }


	private void handleFwdUp() {
 	cancelFwdThread = true;
	}
	
	//RWD
	private void handleRwdDown() {

        if (!rwdThreadRunning)
            startRwdThread();
    }

    private void startRwdThread() {

        Thread r = new Thread() {

            @Override
            public void run() {
                try {

                    rwdThreadRunning = true;
                    while (!cancelRwdThread) {

                        handler.post(new Runnable() {   
                            @Override
                            public void run() {
                                rewind();
                            }
                        });

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(
                                "Could not wait between char delete.", e);
                        }
                    }
                }
                finally
                {
                    rwdThreadRunning = false;
                    cancelRwdThread = false;
                }
            }
        };

        // actually start the delete char thread
        r.start();
    }


	private void handleRwdUp() {
		cancelRwdThread = true;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		String listItem =(String) ((TextView) arg1).getText();
		Log.i("arg1",arg1.toString());
		Log.i("arg1",Integer.toString(arg1.getId()));
		Log.i("fileList",Integer.toString(fileList.getId()));
		if (!inRNList){
			
			playBack(listItem);
		}
		else{
			if (listItem.equals("New Audio Note"))
				newRN();
			else{
				try {
					playRN(listItem);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		
		
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		if(player.isPlaying())
			playPause();
		
		@SuppressWarnings("unused")
		final String listItem =(String) ((TextView) arg1).getText();
		editBMInflater(arg1);
		
		return false;
	}

	@Override
	public boolean onLongClick(View v) {
		if (v.getId() == R.id.ButtonBookmark){
			if (player.isPlaying())
				playPause();
			bookmark();
		}
		return false;
	}
	
	protected void onSaveInstanceState (Bundle outState){
		outState.putInt("position", player.getCurrentPosition());
		if(player.isPlaying())
			outState.putBoolean("playing", true);
		else
			outState.putBoolean("playing",false);
		super.onSaveInstanceState(outState);
	}
	public void onRestoreInstanceState(Bundle saved) {
	    super.onRestoreInstanceState(saved);
	}
	public void onDestroy(){
		super.onDestroy();
		if (bmNoteOpen){
		noteTimer.cancel();
		bmNote.cancel();
		}
	}
	
}

