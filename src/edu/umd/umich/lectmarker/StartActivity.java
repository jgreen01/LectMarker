package edu.umd.umich.lectmarker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class StartActivity extends Activity implements OnClickListener{

	Button play;
	Button record;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.startpage);
		
		play = (Button)this.findViewById(R.id.buttonPL);
		play.setOnClickListener(this);
		
		record = (Button)this.findViewById(R.id.buttonRL);
		record.setOnClickListener(this);
		
		
	}
	
	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.buttonPL){
			Intent i = new Intent("edu.umd.umich.lectmarker.AL");
			startActivity(i);
		}
		if(v.getId()==R.id.buttonRL){
			Intent i = new Intent("edu.umd.umich.lectmarker.AL");
			i.putExtra("goRec", true);
			startActivity(i);
		}
		
	}

}
