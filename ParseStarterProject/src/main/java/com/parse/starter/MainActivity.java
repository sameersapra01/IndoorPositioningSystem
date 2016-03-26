/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseAnalytics;
import com.parse.ParseObject;



import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener {

    WifiManager wifi;
    String wifis[];
    WifiScanReceiver wifiReciever;
    ParseObject testObject;
    TextView directionSelected;
    TextView yPosValue;
    TextView xPosValue;

    //to store direction and data point
    String yPos;
    String xPos;
    String direction;
    //spinner
    Spinner directionsList;

    ArrayAdapter<String> roomsAdapter;
    ArrayAdapter<String> directionAdapter;

    SendDataToParseTask sendDataToParseTask;

    boolean spinnerOnItemInit = false;
    boolean dataSentToParse = true;

    private static final double Y_DIMENSION   = 28.7;
    int jj = 0;


  @Override
  protected void onCreate(Bundle savedInstanceState) {

      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      ParseAnalytics.trackAppOpenedInBackground(getIntent());
      directionsList = (Spinner) findViewById(R.id.directionList);

      //directions adapter settings
      ArrayAdapter<CharSequence> adapterDirection = ArrayAdapter.createFromResource(this, R.array.directions, R.layout.support_simple_spinner_dropdown_item);
      adapterDirection.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
      directionsList.setAdapter(adapterDirection);
      directionsList.setOnItemSelectedListener(this);


      //wifi indoor code starts here
      //lv=(ListView)findViewById(R.id.listView);
      wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
      wifiReciever = new WifiScanReceiver();



    //real time code
   // wifi.startScan();

  /*  ScheduledThreadPoolExecutor exec  = new ScheduledThreadPoolExecutor(1);
    long delay = 2000;
    exec.scheduleWithFixedDelay(new MyTask(),0,delay, TimeUnit.MILLISECONDS);*/
  }
/*
    public class MyTask implements  Runnable{
        @Override
        public void run() {
            wifi.startScan();
        }
    }*/

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  protected void onPause() {
    unregisterReceiver(wifiReciever);
    super.onPause();
  }

  protected void onResume() {
    registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    super.onResume();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }



  //this code sends a sample data to pasre...Works-Tested.
  public void sendDataToParseForEachRoom(View view)
  {
        yPosValue = (EditText)findViewById(R.id.yPosValue);
        yPos = yPosValue.getText().toString();

        xPosValue = (EditText)findViewById(R.id.xPosValue);
        xPos = xPosValue.getText().toString();

        //instantiate the asynctask
        sendDataToParseTask = new SendDataToParseTask();
        sendDataToParseTask.execute();
  }


    public void dataTestingActivity(View view)
    {
        Intent intent = new Intent(this,SendDataTesting.class);
        startActivity(intent);
    }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    try {
      //because this listener is called on initialization, used bool to stop that init.
      if (spinnerOnItemInit) {
        int i = 0;
        directionSelected = (TextView) view;

        direction = directionSelected.getText().toString();
       Toast.makeText(this,direction.toString(),Toast.LENGTH_SHORT).show();
      }
      spinnerOnItemInit = true;
    }
    catch (Exception ex){
      Toast.makeText(getBaseContext(), ex.getMessage().toString(), Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {

  }

  private class SendDataToParseTask extends AsyncTask<Void,Void,String>{

    @Override
    protected String doInBackground(Void... params) {
        jj = 0;
        while (jj < 20) {
            if (dataSentToParse) {
                dataSentToParse = false;
                wifi.startScan();
                jj++;
            }
        }
        return "Data Sent";
    }

    @Override
    protected void onPostExecute(String s) {
      Toast.makeText(getBaseContext(), s.toString(), Toast.LENGTH_SHORT).show();
    }
  }




  //whenever wifi is receiver, it is intercepted here
  private class WifiScanReceiver extends BroadcastReceiver {
    public void onReceive(Context c, Intent intent) {
      try {
        List<ScanResult> wifiScanList = wifi.getScanResults();
        for (int i = 0; i < wifiScanList.size(); i++) {
            String ssid = (wifiScanList.get(i).SSID).toString();
            if ((ssid.equals("dlink-95A8"))||(ssid.equals("dlink-7D8C"))||(ssid.equals("dlink-7D28"))) {

                testObject = new ParseObject("NewDataPointsRow2");
                //sending data to parse
                testObject.put("xPos", "2");
             /*   testObject.put("xPos", xPos.toString());*/
               // testObject.put("yPos", "26");
                testObject.put("yPos", yPos.toString());
                testObject.put("direction", direction.toString());
                testObject.put("signalStrength",String.valueOf(wifiScanList.get(i).level) );
                testObject.put("SSID", String.valueOf(wifiScanList.get(i).SSID));
                testObject.saveInBackground();
          }
        }

          dataSentToParse=true;
        //lv.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, wifis));
      } catch (Exception e) {
        //Toast.makeText(getBaseContext(), "Error Occurred...", Toast.LENGTH_SHORT).show();
      }
    }
  }
}