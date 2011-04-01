package ds.android.tasknet.activity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import ds.android.tasknet.R;
import ds.android.tasknet.config.Node;
import ds.android.tasknet.config.Preferences;
import ds.android.tasknet.msgpasser.Message;
import ds.android.tasknet.msgpasser.MessagePasser;
import ds.android.tasknet.msgpasser.MulticastMessage;
import ds.android.tasknet.task.Task;
import ds.android.tasknet.clock.*;
import ds.android.tasknet.exceptions.InvalidMessageException;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SimulateEvent extends Activity {
	MessagePasser mp;
	String host_name;
	Properties prop;
	public static int taskNum = 0;
	HashMap<String, ArrayList<Node>> taskGroup;
	EditText taMessages;
	Button taskButton;
	Handler guiRefresh;
	final int REFRESH = 1;
	final String TXTMSG = "txtmsg";
	boolean canSendMsg = true;
	long cpuTotal;
	long cpuIdle;
	float cpuLoad;
	int batteryLevel;;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tasknet);
		((Button) findViewById(R.id.btnExitTaskNet))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						finish();
						System.exit(0);
					}
				});
		host_name = getIntent().getStringExtra("NodeName");
		taskGroup = new HashMap<String, ArrayList<Node>>();

		Preferences.setHostDetails(Preferences.conf_file, host_name);
		mp = new MessagePasser(Preferences.conf_file, host_name,
				ClockFactory.ClockType.VECTOR, Preferences.nodes.size());
		mp.start();

		taMessages = (EditText) findViewById(R.id.taMsgs);
		taskButton = (Button) findViewById(R.id.btnTaskAdv);

		taskButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				String node = "";
				String kind = "";
				String msgid = "";
				taskNum++;
				String taskId = host_name + taskNum;
				Task newTask = new Task(new Integer(10000), taskId);
				taMessages.append("Sent Task advertisement: " + taskId + "\n");
				taskGroup.put(taskId, new ArrayList<Node>());
				MulticastMessage mMsg = new MulticastMessage(node, kind, msgid,
						newTask, mp.getClock(), true,
						MulticastMessage.MessageType.TASK_ADV, host_name);
				Preferences.crashNode = "";
				// This will send message to self... for testing purposes
				// If it doesn't work, check with first argument as "bob"
				// Message msg = new Message("bob", "kind", "id", taskDetails
				// .toXML(newTask));
				try {
					mp.send(mMsg);
				} catch (InvalidMessageException e) {
					e.printStackTrace();
				}
			}
		});

		guiRefresh = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case REFRESH:
					/* Refresh UI */
					String m = msg.getData().getString(TXTMSG);
					taMessages.append(m);
					break;
				}
			}
		};

		listenForIncomingMessages();
		keepSendingProfileUpdates();
	}

	private void listenForIncomingMessages() {
		/*
		 * This thread keeps polling for any incoming messages and displays them
		 * to user
		 */

		(new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(10);
						Message msg = mp.receive();
						if (msg != null) {
							final android.os.Message guiMessage = android.os.Message
									.obtain(guiRefresh, REFRESH);
							final Bundle msgBundle = new Bundle();
							if (msg instanceof MulticastMessage) {
								switch (((MulticastMessage) msg).getMessageType()) {
								case TASK_ADV:
									msgBundle.putString(TXTMSG,"Received task advertisement\n");
									guiMessage.setData(msgBundle);
									guiRefresh.sendMessage(guiMessage);
									Message profileMsg = new Message(((MulticastMessage) msg).getSource(),
											"", "", Preferences.nodes.get(host_name));
									profileMsg.setNormalMsgType(Message.NormalMsgType.PROFILE_XCHG);
									try {
										mp.send(profileMsg);
									} catch (InvalidMessageException ex) {
										ex.printStackTrace();
									}
									break;
								}
							} else {
								switch (msg.getNormalMsgType()) {
								case NORMAL:
									msgBundle.putString(TXTMSG, msg.getData() + "\n");
									guiMessage.setData(msgBundle);
									try {
										guiRefresh.sendMessage(guiMessage);
									} catch (Exception e) {
										e.printStackTrace();
									}
									break;
								case PROFILE_XCHG:
									Node profileOfNode = (Node) msg.getData();
									msgBundle.putString(TXTMSG, profileOfNode
											+ "\n");
									guiMessage.setData(msgBundle);
									guiRefresh.sendMessage(guiMessage);
									// taMessages.append(profileOfNode + "\n");
									synchronized (taskGroup) {
										String taskId = profileOfNode
												.getTaskid();
										ArrayList<Node> taskNodes = taskGroup
												.get(taskId);
										if (taskNodes == null) {
											taskNodes = new ArrayList<Node>();
										}
										taskNodes.add(profileOfNode);
										taskGroup.put(taskId, taskNodes);
									}
									break;
								}
							}
						}
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
			}
		}).start();
	}

	private void keepSendingProfileUpdates() {
		(new Thread() {

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(Preferences.PROFILE_UPDATE_TIME_PERIOD);
						try {
							synchronized (Preferences.nodes) {
								Node updateNode = Preferences.nodes.get(host_name);
								updateNode.update(getCurrentRAM(),getCPUusage(),getBatteryLevel());
								Message profileUpdate = new Message(Preferences.COORDINATOR, "", "",updateNode);
								profileUpdate.setNormalMsgType(Message.NormalMsgType.PROFILE_UPDATE);
								mp.send(profileUpdate);
							}
						} catch (InvalidMessageException ex) {
							ex.printStackTrace();
						}
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
			}
		}).start();
	}
    /*************************Claire's code**********************************/
    public int getBatteryLevel() {//
//        final int batteryLevel;
    	BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver(){
    		public void onReceive(Context context, Intent intent) {
                context.unregisterReceiver(this);
                int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                if (rawlevel >= 0 && scale > 0) 
                    batteryLevel = (rawlevel * 100) / scale;               
            }
        };
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelReceiver, batteryLevelFilter);
        return batteryLevel;
    }
    
    private long getCurrentRAM(){
        MemoryInfo mi = new MemoryInfo();
        ActivityManager activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        return mi.availMem / 1048576L;
     }
   
	private float getCPUusage()
	{
	    try
	    {
	        BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( "/proc/stat" ) ), 1000 );
	        String load = reader.readLine();
	        reader.close();     
	
	        String[] toks = load.split(" ");
	
	        long currTotal = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4]);
	        long currIdle = Long.parseLong(toks[5]);
	
	        cpuLoad = (currTotal - cpuTotal) * 100.0f / (currTotal - cpuTotal + currIdle - cpuIdle);
	        cpuTotal = currTotal;
	        cpuIdle = currIdle;
	    }
	    catch(IOException ex)
	    {
	        ex.printStackTrace();           
	    }
	    return cpuLoad;
	}
    
    /********************************************************************************/
}