package ds.android.tasknet.activity;

import com.example.android.tasknet.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.net.Uri;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;

public class CaptureEventActivity extends Activity {

	static final int GET_AUDIO = 99;
	private static final int BUSY_DIALOG = 100;
	private static final int SUCCESS_DIALOG = 101;
	private static final int FAILURE_DIALOG = 102;
				
	@Override	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.capture_event);
        
        Button coughButton = (Button) findViewById(R.id.coughButton);
        Button distributeButton = (Button) findViewById(R.id.distributeButton);
        coughButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				
				Intent i = new Intent("org.openintents.action.PICK_FILE",Uri.parse("directory:///home/dilip/"));
				startActivityForResult(i, GET_AUDIO);
		}});
        distributeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(CaptureEventActivity.this, DistributeActivity.class);
				startActivity(i);
			}
		});
    }
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
	    // See which child activity is calling us back.
	    System.out.println("here");
	    if (requestCode == GET_AUDIO) {
	       if (resultCode == RESULT_OK) {			
				
				showDialog(SUCCESS_DIALOG);
			}
			else
				showDialog(FAILURE_DIALOG);
		}

	    	
	}
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		switch(id) {
			case BUSY_DIALOG:
				ProgressDialog dialog = new ProgressDialog(this);
				dialog.setMessage("Logging in");
				return dialog;
				
			case SUCCESS_DIALOG:
				AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
				builder2.setCancelable(true)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dismissDialog(SUCCESS_DIALOG);
					}
				})
				.setMessage("Audio Capture Successful");
				return builder2.create();
			
			case FAILURE_DIALOG:
				AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
				builder3.setCancelable(true)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dismissDialog(FAILURE_DIALOG);
					}
				})
				.setMessage("Audio Capture Failed");
				return builder3.create();
			default: return null;
		}
	}
	

	protected void onPreExecute() {
			showDialog(BUSY_DIALOG);
		}
	

}
