package com.cst.aaron.cellfinder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

@SuppressLint("HandlerLeak") public class MainActivity extends Activity {
	TextView cid_tView,Rssi_tView,neighboringCellInfoTextView,currentLocation_tView;
	Button startButton;
    MyPhoneStateListener mylistener;
    List<NeighboringCellInfo> cellInfos;
    TelephonyManager tManager;
    String neighnoringcellinfo,locationInfoString;
    LocationListener myLocationListener;
    LocationManager myLocationManager;
    String connectcellsignalstrenthString;
    final int CELL_TYPE_GSM=1;
    final int CELL_TYPE_CDMA=2;
    final int CELL_TYPE_LTE=3;
    int cell_type=CELL_TYPE_GSM;
    int networkType;
    int lac,cid,psc;
    private Timer timer;
    private TimerTask task;
    private  Handler handler;
    GsmCellLocation gsmCellLocation;
    CdmaCellLocation cdmaCellLocation;
    
    static long LOCATION_UPTATE_FREQUENCY_TIME=500;
    static float LOCATION_UPDATE_FREQUENCY_DISTANCE=1;
    static final String FIEL_PATH_STRING=Environment.getExternalStorageDirectory().getPath();
    String file_name,device_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tManager=(TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        device_id=tManager.getDeviceId();
        cid_tView=(TextView)findViewById(R.id.Current_Cid);
        Rssi_tView=(TextView)findViewById(R.id.Current_Lac);
        neighboringCellInfoTextView=(TextView)findViewById(R.id.NeighboringCell);
        currentLocation_tView=(TextView)findViewById(R.id.Current_Location);
        startButton=(Button)findViewById(R.id.button_start);
        currentLocation_tView.setMovementMethod(new ScrollingMovementMethod());
        
        file_name=createDir(FIEL_PATH_STRING+"/cellFinerInfos/");
        // cell info 
        mylistener=new MyPhoneStateListener();
        tManager.listen(mylistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS|PhoneStateListener.LISTEN_SERVICE_STATE|PhoneStateListener.LISTEN_CELL_LOCATION);
        
        
        // get location info
        myLocationListener= new myLocationlistener();
        myLocationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
       // Location  location=getLocation();
       // showLocationInfo(location);
        
       
        // record document
        handler=new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				recordCellinfosTxt();
				super.handleMessage(msg);
			}
        	
        };
        
        
        
        // start or stop the document record
        startButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				enableGPS();
				if (startButton.getText().equals("Start")) {
					timer=new Timer();
					task=new TimerTask() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							Message message= new Message();
							message.what=1;
							handler.sendMessage(message);
						}
					};
					timer.schedule(task, 1000, 1000);
					startButton.setText(R.string.button_stop_recording);
				}
				else {
					timer.cancel();
					timer.purge();
					handler.removeCallbacks(task);
					startButton.setText(R.string.button_start_recording);
				}
				
			}
		});
        
       
    }
    
    public void enableGPS() {
    	 if (!myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 			AlertDialog.Builder builder=new Builder(MainActivity.this);
 			builder.setMessage("Please Open the GPS!");
 			builder.setTitle("Message");
 			builder.setPositiveButton("Ok", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
					Intent intent=new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		 			startActivityForResult(intent, 0);	
		 			return;
				}
			});
 			builder.setNegativeButton("No", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			});
 			builder.create().show();		
 		}
	}
 // create mkdir
    private String createDir(String path) {
		File file=new File(path);
		if (!file.exists()) {
			file.mkdir();
		}
		return path;
	}
    // get location
    private Location getLocation() {
    	 	
    	String best_provider=myLocationManager.getBestProvider(getCriteria(), true);
        
        Location mylocation=myLocationManager.getLastKnownLocation(best_provider);
        if (mylocation==null) {
       	
		String network_provider=LocationManager.NETWORK_PROVIDER;			
		mylocation=myLocationManager.getLastKnownLocation(network_provider);
		}
         
       if (mylocation!=null) {
    	   showLocationInfo(mylocation);
    	   myLocationManager.requestLocationUpdates(best_provider, LOCATION_UPTATE_FREQUENCY_TIME, LOCATION_UPDATE_FREQUENCY_DISTANCE, myLocationListener);  
       }
        
		return mylocation;
		
	}
    private Criteria getCriteria() {
		// TODO Auto-generated method stub
    	Criteria criteria=new Criteria();
    	criteria.setAccuracy(Criteria.ACCURACY_FINE);
    	criteria.setSpeedRequired(false);
    	criteria.setBearingRequired(false);
    	criteria.setAltitudeRequired(false);
    	criteria.setPowerRequirement(Criteria.POWER_LOW);
    	
		return criteria;
	}

	// show locationInfo
	private void showLocationInfo(Location location) {
		// TODO Auto-generated method stub
    	locationInfoString="Cell Location Info\n";
		locationInfoString=locationInfoString+"Time:"+location.getTime()+"\nLatitude:"+location.getLatitude()+"\nLongitude:"+location.getLongitude()+"\nProvider:"+location.getProvider();
		currentLocation_tView.setText(locationInfoString);
		
	}
    
	// update Neighboring CellInfo
	private void recordCellinfosTxt() {
    	cellInfos =(List<NeighboringCellInfo>)tManager.getNeighboringCellInfo();
    	neighnoringcellinfo="Neighboring Cell Info List\n CID/PSC, LAC, RSSI\n";
    	SimpleDateFormat dFormat=new SimpleDateFormat("MM_dd_yyyy_");
    	String nowdateString=dFormat.format(new Date());
        File cellinfoFile=new File(file_name+nowdateString+device_id+"cell.txt");
        File connectcellinfoFile=new File(file_name+nowdateString+device_id+"ConnectCell.txt");
        boolean cellFileExist=true, connectedFileExist=true;
        Location currentLocation=getLocation();
      //  long curentTime=System.currentTimeMillis();
       SimpleDateFormat simpleDateFormat=new SimpleDateFormat("MMddyyyy_HH:mm:ss");
       String curentTimestring=simpleDateFormat.format(new Date());
        try {
			
		// get the cells neighboring
        if (!cellinfoFile.exists()) {  
        	cellinfoFile.createNewFile();
        	cellFileExist=false;
        }
        OutputStream myoutputStream=new FileOutputStream(cellinfoFile,true);
        OutputStreamWriter myoutputStreamWriter=new OutputStreamWriter(myoutputStream);
        if (!cellFileExist) {
			myoutputStreamWriter.append("Time,Longitude,Latitude,Cid/Psc,Lac,Rssi\n");
		}
        String cellinfo_temp="";
       
        if (cellInfos.isEmpty()) {
			neighnoringcellinfo=neighnoringcellinfo+"Null";
		}else {
			for(NeighboringCellInfo cellInfo: cellInfos){
				Log.v("cell", "cid:"+cellInfo.getCid()+"lac:"+cellInfo.getLac());
				
				if (cellInfo.getCid()==-1) {
					cid=cellInfo.getPsc();
					lac=cellInfo.getLac();
				}
				else {
					cid=cellInfo.getCid();
					lac=cellInfo.getLac();
					
				}
				neighnoringcellinfo=neighnoringcellinfo+cid+","+lac+","+cellInfo.getRssi()+"dbm\n";
				cellinfo_temp=curentTimestring+","+currentLocation.getLongitude()+","+currentLocation.getLatitude()+","+cid+","+cellInfo.getLac()+","+Integer.toString(-113+(2*cellInfo.getRssi()))+"\n";				
				myoutputStreamWriter.append(cellinfo_temp);						
			}
			myoutputStreamWriter.close();
	        myoutputStream.close();
		}
        
        // get the current connected cellInfos
        if (!connectcellinfoFile.exists()) {
			connectcellinfoFile.createNewFile();
			connectedFileExist=false;
		}       
        OutputStream myoutputStream_connected=new FileOutputStream(connectcellinfoFile,true);
        OutputStreamWriter myoutputSteamWriter_connected=new OutputStreamWriter(myoutputStream_connected);
               
        if (!connectedFileExist) {
        	myoutputSteamWriter_connected.append("Time,Longitude,Latitude,Cid,short_Cid,Lac,Rssi,Did,Nci,Non,Ntt,Sns,Dsv,Pnl,Sop\n");
		}
        
        String connectedString="";
        if (cell_type==CELL_TYPE_GSM & gsmCellLocation!=null) {
			cid=gsmCellLocation.getCid();
			lac=gsmCellLocation.getLac();
							
		}else if (cell_type==CELL_TYPE_CDMA &cdmaCellLocation!=null) {
			cid=cdmaCellLocation.getBaseStationId();
			lac=cdmaCellLocation.getNetworkId();			
		}
       
        	connectedString=curentTimestring+","+currentLocation.getLongitude()+","+currentLocation.getLatitude()+","+Integer.toString(cid)+","+(cid & 0xffff)+","+Integer.toString(lac)+","+connectcellsignalstrenthString+","+
					tManager.getDeviceId()+","+tManager.getNetworkCountryIso()+","+tManager.getNetworkOperatorName()+","+Integer.toString(tManager.getNetworkType())+","+tManager.getSimSerialNumber()+","+
					tManager.getDeviceSoftwareVersion()+","+tManager.getLine1Number()+","+tManager.getSimOperator()+"\n";
			myoutputSteamWriter_connected.append(connectedString);
			
			myoutputSteamWriter_connected.close();
			myoutputStream_connected.close();
		
        
        } catch (Exception e) {
			// TODO: handle exception
        	e.printStackTrace();
        	
		}
        neighboringCellInfoTextView.setText(neighnoringcellinfo);
        cid_tView.setText("CID:"+Integer.toString(cid)+"\nShort_Cid:"+(cid & 0xffff)+"\nLAC:"+Integer.toString(lac)+"\nDeviceID:"+
				tManager.getDeviceId()+"\nNCI:"+tManager.getNetworkCountryIso()+"\nNON:"+tManager.getNetworkOperatorName()+"\nNTT:"+Integer.toString(tManager.getNetworkType())+"\nSNS:"+tManager.getSimSerialNumber()+"\nDSV:"+
				tManager.getDeviceSoftwareVersion()+"\nPNL:"+tManager.getLine1Number()+"\nSOP:"+tManager.getSimOperator());							      
	}
   
    
    
  private  class myLocationlistener implements LocationListener {
   	 
    	
    	@Override
    	public void onLocationChanged(Location location) {
    		// TODO Auto-generated method stub
    		showLocationInfo(location);
    		Log.v("cellloc", "location updated");
    	}

    	@Override
    	public void onProviderDisabled(String provider) {
    		// TODO Auto-generated method stub
    		currentLocation_tView.setText("Location provide is unable to work");
    	}

    	@Override
    	public void onProviderEnabled(String provider) {
    		// TODO Auto-generated method stub
    		
    	}

    	@Override
    	public void onStatusChanged(String provider, int status, Bundle extras) {
    		// TODO Auto-generated method stub
    		
    	}
    	
    }
  
    private class MyPhoneStateListener extends PhoneStateListener{

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// TODO Auto-generated method stub
			super.onCallStateChanged(state, incomingNumber);
		}

		@Override
		public void onCellLocationChanged(CellLocation location) {
			// TODO Auto-generated method stub
			super.onCellLocationChanged(location);		
			networkType=tManager.getNetworkType();
			Log.v("cellloc", "cellLocation update");
			if (networkType==TelephonyManager.NETWORK_TYPE_EDGE || networkType==TelephonyManager.NETWORK_TYPE_GPRS || networkType==TelephonyManager.NETWORK_TYPE_HSDPA || networkType==TelephonyManager.NETWORK_TYPE_UMTS || networkType==TelephonyManager.NETWORK_TYPE_LTE) {
				gsmCellLocation=(GsmCellLocation)location;
				cell_type=CELL_TYPE_GSM;
			}else if (networkType==TelephonyManager.NETWORK_TYPE_CDMA || networkType==TelephonyManager.NETWORK_TYPE_EVDO_0 || networkType==TelephonyManager.NETWORK_TYPE_EVDO_A || networkType==TelephonyManager.NETWORK_TYPE_1xRTT) {
				cell_type=CELL_TYPE_CDMA;
				cdmaCellLocation=(CdmaCellLocation)location;
			}

		}

		@Override
		public void onDataConnectionStateChanged(int state, int networkType) {
			// TODO Auto-generated method stub
			super.onDataConnectionStateChanged(state, networkType);
		
		}

		@Override
		public void onDataConnectionStateChanged(int state) {
			// TODO Auto-generated method stub
			super.onDataConnectionStateChanged(state);
			
		}

		@Override
		public void onServiceStateChanged(ServiceState serviceState) {
			// TODO Auto-generated method stub
			super.onServiceStateChanged(serviceState);
		}


		@Override
		public void onCellInfoChanged(List<CellInfo> cellInfos) {
			// TODO Auto-generated method stub
			super.onCellInfoChanged(cellInfos);
			
			
		}

		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			// TODO Auto-generated method stub
			super.onSignalStrengthsChanged(signalStrength);
			if (cell_type==CELL_TYPE_GSM) {
				connectcellsignalstrenthString=Integer.toString(-113+2*signalStrength.getGsmSignalStrength());
			
			} else {
				connectcellsignalstrenthString=Integer.toString(signalStrength.getCdmaDbm());

			}
			Rssi_tView.setText("RSSI:"+connectcellsignalstrenthString+"dbm");
		}
		
		
	}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
        
    }
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (timer!=null) {
			timer.cancel();
			timer.purge();
		}
		handler.removeCallbacks(task);
	}
	private void showTips() {
		AlertDialog alertDialog=new AlertDialog.Builder(MainActivity.this).setTitle("Tips").setMessage("Are you sure to exit? It will stop the data recording.")
				.setPositiveButton("Sure", new android.content.DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						MainActivity.this.finish();
					}
					
				}).setNegativeButton("No", new android.content.DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						return;
					}
				}).create();
		alertDialog.show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		if (keyCode==KeyEvent.KEYCODE_BACK & event.getRepeatCount()==0 ) {
			this.showTips();
			return false;
		}
		return false;
	}
	
    
 }



 