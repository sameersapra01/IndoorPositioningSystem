package com.parse.starter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import android.os.Handler;

/**
 * Created by samee on 2016-02-01.
 */
public class SendDataTesting extends Activity {

    //Our 3 routers SSID
    private static final String Router95A8 = "dlink-95A8";
    private static final String Router7D28 = "dlink-7D28";
    private static final String Router7D8C = "dlink-7D8C";
    
    //range between last 2 Positions is 3feets
    private static final int rangeBT2YPos = 15;

    //number of real time data
    private static final int numberOfTimesRTData = 5;

    //variables required for wifi scanning
    WifiManager wifi;
    WifiScanReceiver wifiReciever;

    //list of class containg 6 properties.
    List<DataPoint> dataPoints;

    //level of each router.
    double Router95A8Level = 0;
    double Router7D28Level = 0;
    double Router7D8CLevel = 0;

    //mean and sum of real time values.
    double meanOfRouter95A8 = 0;
    double meanOfRouter7D28 = 0;
    double meanOfRouter7D8C = 0;

    //bool values to manage the threads
    boolean calculationsForDataPointControl = false;
    boolean RTDataThreadControl = true;

/*    //counter for gettin off-line mean data
    int countOffLineMean = 0;*/

    //Real Time values range check
    List<Integer> reatTime7D28ScanValues = new ArrayList<>();
    List<Integer> reatTime7D8CScanValues = new ArrayList<>();
    List<Integer> reatTime95A8ScanValues = new ArrayList<>();

    int medianOfRouter7D8C = 0;
    int medianOfRouter7D28 = 0;
    int medianOfRouter95A8 = 0;


    //map drawing variables
    int Xratio = 0;
    int Yratio = 0;
    int globalY = 0;
    float globalX = 0.0f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.data_testing);

            //initializing list of DataPoint class.
            dataPoints = new ArrayList<>();

            //setting wifi manger
            wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            wifiReciever = new WifiScanReceiver();

            HandlerThread handlerThread = new HandlerThread("ht");
            handlerThread.start();
            Looper looper = handlerThread.getLooper();
            Handler handler = new Handler(looper);

            registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION), null, handler);

            //get the mean from parse and store it in a list of DataPoint class in a thread
            Thread th1 = new Thread(new getMeanFromParse());
            //start the thread
            th1.start();

            //wait for this thread to get all data and die
            th1.join();

/*            Toast.makeText(this,"Got all data",Toast.LENGTH_SHORT).show();*/




/*            //Update Location thread
            Thread updateLocationThread = new Thread( new UpdateLocation());
            updateLocationThread.start();*/



/*            //problems are the old created threads will show the old location for a new position...
            Thread realTimeDP = new Thread(new CreateNewUpdateLocationThreads());
            realTimeDP.start();*/

            //map thread
            ScheduledThreadPoolExecutor mapThread = new ScheduledThreadPoolExecutor(5);
            mapThread.scheduleWithFixedDelay(new MyTask(), 0, 200, TimeUnit.MILLISECONDS);

            //can be in one single thread instead of calling it every 2 seconds.
            ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
            long delay = 200;
            exec.scheduleWithFixedDelay(new UpdateLocation(), 0, delay, TimeUnit.MILLISECONDS);

        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private class MyTask implements Runnable {
        @Override
        public void run() {

            SendDataTesting.this.runOnUiThread(new Runnable() {

                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                public void run() {
                    ImageView map = (ImageView) findViewById(R.id.Map);
                    Xratio = (map.getRight() - map.getLeft()) / 28;
                    Yratio = (map.getBottom() - map.getTop()) / 38;


                    ImageView person = (ImageView) findViewById(R.id.Person);

                    int y = globalY; // this is the number that comes from the data
                    int x = 0;
                    float tmpX = globalX; // this is the number that comes from the data
                    if (tmpX == 0) {
                        x = 2;
                    } else if (tmpX == 0.5) {
                        x = 7;
                    } else if (tmpX == 1) {
                        x = 12;
                    } else if (tmpX == 1.5) {
                        x = 17;
                    } else if (tmpX == 2) {
                        x = 23;
                    } else if (tmpX == 2.5) {
                        x = 28;
                    } else if (tmpX == 3) {
                        x = 34;
                    }

                    //do the math to get ratio to put on map
                    x = x * Yratio;
                    y = y * Xratio;

                    //location of the person that is currently.
                    int personX = person.getLeft();
                    int personY = person.getTop();
                    int personXS = person.getRight();
                    int personYS = person.getBottom();

                    int middleX = person.getLeft() + ((person.getRight() - person.getLeft()) / 2);
                    int radX = ((person.getRight() - person.getLeft()) / 2);
                    int middleY = person.getTop() + ((person.getBottom() - person.getTop()) / 2);
                    int radY = ((person.getBottom() - person.getTop()) / 2);


                    middleX = y;
                    middleY = x;

                    personX = middleX - radX + map.getLeft();
                    personXS = middleX + radX + map.getLeft();
                    personY = middleY - radY + map.getTop();
                    personYS = middleY + radY + map.getTop();


                    person.setLeft(personX);
                    person.setTop(personY);
                    person.setRight(personXS);
                    person.setBottom(personYS);
                  /*  if (person.getVisibility() == View.VISIBLE) {
                        person.setVisibility(View.INVISIBLE);
                    } else {
                        person.setVisibility(View.VISIBLE);
                    }*/

                }
            });
        }
    }


    @Override
    protected void onResume() {
        // registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    protected void onPause() {
        try {
            // unregisterReceiver(wifiReciever);
            super.onPause();
        } catch (Exception ex) {
            Toast.makeText(this,"Error on pause..." ,Toast.LENGTH_SHORT).show();
        }
    }


    public class UpdateLocation implements Runnable {
        @Override
        public void run() {
            try {
                int i = 0;
                if (RTDataThreadControl) {
                    //setting the value of all 3 router's level to 0
                    Router7D28Level = 0;
                    Router7D8CLevel = 0;
                    Router95A8Level = 0;

                    reatTime7D28ScanValues.clear();
                    reatTime95A8ScanValues.clear();
                    reatTime7D8CScanValues.clear();



                    //compare the real time data with mean data and update the position
                    while (i < numberOfTimesRTData) {
                        wifi.startScan();
                        if (calculationsForDataPointControl) {
                            //get the mean of 3 routers fo 5 rows
                            i++;
                            calculationsForDataPointControl = false;
                            RTDataThreadControl = false;
                        }
                    }

                    sortAndRangeCheckRealTimeValues();


    /*                //calculating the mean of 3 different RT routers.
                    meanOfRouter7D28 = Router7D28Level / numberOfTimesRTData;
                    meanOfRouter7D8C = Router7D8CLevel / numberOfTimesRTData;
                    meanOfRouter95A8 = Router95A8Level / numberOfTimesRTData;*/

                    // new thread...
                    new Thread() {

                        //List to store all off-line mean datapoints from parse into a list of class DataPoint
                        List<DataPoint> dataPointsThread = new ArrayList<>(dataPoints);

                        //This list is used to store all positions found using range check into a list of EuclideanDistance class.
                        List<EuclideanDistance> Euclidean_Distance = new ArrayList<>();

                        //make a copy of RT mean of routers for this thread
                        double threadMeanOfRouter7D28 = meanOfRouter7D28;
                        double threadMeanOfRouter7D8C = meanOfRouter7D8C;
                        double threadMeanOfRouter95A8 = meanOfRouter95A8;

                        //
                        float x = 0.0f;
                        int y = 0;

                        @Override
                        public void run() {
                            //compare RT mean with the off-line mean data and update the position on the map
                            for (DataPoint dp : dataPointsThread) {
                                //do linear search and range checks
                                for (float rangeIteration = 0.0f; rangeIteration <= 3.0f; rangeIteration += 0.15f) {
                                    if (rangeCheck(dp.dlink7D28, threadMeanOfRouter7D28, rangeIteration) && rangeCheck(dp.dlink7D8C, threadMeanOfRouter7D8C, rangeIteration)
                                            && rangeCheck(dp.dlink95A8, threadMeanOfRouter95A8, rangeIteration)) {

                                        double euclideanDist = 0.0;

                                        //create an instance of EuclideanDistance class
                                        EuclideanDistance ed = new EuclideanDistance();

                                        //calculate the Euclidean distance between observed and recorded values
                                        euclideanDist = calculateEuclideanDistance(threadMeanOfRouter7D28, threadMeanOfRouter7D8C, threadMeanOfRouter95A8, dp.dlink7D28, dp.dlink7D8C, dp.dlink95A8);

                                        ed.distance = euclideanDist;
                                        ed.x = dp.xPos;
                                        ed.y = dp.yPos;
                                        Euclidean_Distance.add(ed);

                                        //did modify this
                                        break;
                                    }
                                }
                            }


                            //cat tracker

                            //new thread to get the least distance from the Euclidean_Distance list.
                            new Thread() {
                                @Override
                                public void run() {
                                    List<EuclideanDistance> edListThreadCopy = new ArrayList<>(Euclidean_Distance);
                                    //checking if range checks returned any positions or not
                                    if (edListThreadCopy.size() > 0) {
                                        double leastDist = edListThreadCopy.get(0).distance;
                                        for (EuclideanDistance edLeast : Euclidean_Distance) {
                                            if (!(leastDist < edLeast.distance)) {
                                                leastDist = edLeast.distance;
                                                x = edLeast.x;
                                                y = edLeast.y;
                                            }
                                        }
                                       /* globalX = x;
                                        globalY = y;*/

                                        //check if position jumped too much, should be less than 3 feets
                                        //should be greater than 0, because now globalY contains last position
                                        if(globalY > 0) {
                                            if(checkTwoPositionsRange(globalY, y)){
                                                globalX = x;
                                                globalY = y;
                                            }
                                        }
                                        //showing position for the first time
                                        else
                                        {
                                            //show user's first position : can be asked from user or show our algo pos
                                            globalX = x;
                                            globalY = y;
                                        }
                                    }
                                }
                            }.start();
                        }
                    }.start();
                    //start another thread
                    RTDataThreadControl = true;
                }
            } catch (Exception ex) {
                Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }



    public void sortAndRangeCheckRealTimeValues()
    {
        medianOfRouter7D8C = 0;
        medianOfRouter7D28 = 0;
        medianOfRouter95A8 = 0;

        //sort all 3 arrays and get the median
        if(reatTime7D8CScanValues.size()==5)
        {
            Collections.sort(reatTime7D8CScanValues);
            medianOfRouter7D8C = reatTime7D8CScanValues.get(2);
            Log.i("8C Values", reatTime7D8CScanValues.get(0) + " " + reatTime7D8CScanValues.get(1) + " " + reatTime7D8CScanValues.get(2)
                    + reatTime7D8CScanValues.get(3) + " " + reatTime7D8CScanValues.get(4) + " " + String.valueOf(medianOfRouter7D8C));
        }
        if(reatTime95A8ScanValues.size()==5)
        {
            Collections.sort(reatTime95A8ScanValues);
            medianOfRouter95A8 = reatTime95A8ScanValues.get(2);
            Log.i("A8 Values", reatTime95A8ScanValues.get(0) + " " + reatTime95A8ScanValues.get(1) + " " + reatTime95A8ScanValues.get(2)
                    + reatTime95A8ScanValues.get(3) + " " + reatTime95A8ScanValues.get(4) + " " + String.valueOf(medianOfRouter95A8));
        }
        if(reatTime7D28ScanValues.size()==5)
        {
            Collections.sort(reatTime7D28ScanValues);
            medianOfRouter7D28 = reatTime7D28ScanValues.get(2);
            Log.i("28 Values", reatTime7D28ScanValues.get(0) + " " + reatTime7D28ScanValues.get(1) + " " + reatTime7D28ScanValues.get(2)
                    + reatTime7D28ScanValues.get(3) + " " + reatTime7D28ScanValues.get(4) + " " + String.valueOf(medianOfRouter7D28));
        }

        routerRangeCheck(reatTime7D28ScanValues,medianOfRouter7D28 ,"28");
        routerRangeCheck(reatTime95A8ScanValues,medianOfRouter95A8 ,"A8");
        routerRangeCheck(reatTime7D8CScanValues,medianOfRouter7D8C ,"8C");
    }

    void routerRangeCheck(List<Integer> arry , int median , String router )
    {
        List<Integer> list = new ArrayList<>();
        for (Integer ass: arry) {
            if(ass!=null) {
                // -50 > -53
                if (ass >= median) {
                    //-50 - (-53)
                    if ((ass - median <= 6)) {
                        //didnt pass the range check, delete this from the array
                        list.add(ass);
                        Log.i("1...",  router + String.valueOf(median) +  String.valueOf(ass));
                        //arry.remove(iii);
                        //routerRangeCheck(arry,median,router);
                    }
                }
                //-53 < -50
                else if (ass <= median) {
                    //
                    if ((median - ass <= 6)) {
                        //didnt pass the range check, delete this from the array
                        list.add(ass);
                        Log.i("2...",  router + String.valueOf(median) + String.valueOf(ass));
                        //arry.remove(iii);
                        //routerRangeCheck(arry,median,router);
                    }
                }
            }
        }

        switch (router)
        {
            case "28":
                Log.i("Size of 28" , String.valueOf(list.size()));
                meanOfRouter7D28 = sumRTRouterLevels(list);
                break;
            case "8C":
                Log.i("Size of 8C" , String.valueOf(list.size()));
                meanOfRouter7D8C = sumRTRouterLevels(list);
                break;
            case "A8":
                Log.i("Size of A8" , String.valueOf(list.size()));
                meanOfRouter95A8 = sumRTRouterLevels(list);
                break;
        }

        /*if(router.equals("28"))
        {
            Log.i("Size of 28" , String.valueOf(list.size()));
            meanOfRouter7D28 = sumRTRouterLevels(list);
        }
        else if(router.equals("8C"))
        {
            Log.i("Size of 8C" , String.valueOf(list.size()));
            meanOfRouter7D8C = sumRTRouterLevels(list);
        }else if(router.equals("A8"))
        {
            Log.i("Size of A8" , String.valueOf(list.size()));
            meanOfRouter95A8 = sumRTRouterLevels(list);
        }*/
    }

    public double sumRTRouterLevels(List<Integer> ar)
    {
        double mean = 0.0;
        for (Integer dd: ar) {
            mean += dd;
        }
        mean /= ar.size();
        Log.i("mean" , String.valueOf(mean));
        return  mean;
    }

    public boolean checkTwoPositionsRange(int gY, int yPos)
    {
        // 5 > 3
        if(gY >= yPos) {
            // 5 - 3 < range
            if (gY - yPos <= rangeBT2YPos) {
                return true;
            }
        }
        // 3 < 5
        else if(gY <= yPos) {
            //5 - 3 < range
            if (yPos - gY <= rangeBT2YPos) {
                return true;
            }
        }
        return  false;
    }

    public double calculateEuclideanDistance(double router7D28Observed, double router7D8CObserved, double router95A8Observed, double router7D28Recorded, double router7D8CRecorded, double router95A8Recorded) {
        double dist = 0.0f;
        dist = Math.sqrt(Math.pow((router7D28Observed - router7D28Recorded), 2) + Math.pow((router7D8CObserved - router7D8CRecorded), 2) + Math.pow((router95A8Observed - router95A8Recorded), 2));
        //Log.i("Distance : " ,String.valueOf(  dist));
        return dist;
    }


    public boolean rangeCheck(double offLineMean, double realTimeMean, double range) {
        //checking +-range
        // e.g : offlineMean < realTimeMean
        //e.g : -60 < -55
        if (offLineMean < realTimeMean) {
            if (realTimeMean - offLineMean <= range) {
                return true;
            }
        }
        //e.g : -55 > -60
        else if (offLineMean > realTimeMean) {
            if (offLineMean - realTimeMean <= range) {
                return true;
            }
        }
        return false;
    }


    public class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            try {
                List<ScanResult> wifiScanList = wifi.getScanResults();

                for (int i = 0; i < wifiScanList.size(); i++) {
                    String ssid = (wifiScanList.get(i).SSID).toString();
                    if ((ssid.equals(Router7D8C))) {
                        //Router7D8CLevel += wifiScanList.get(i).level;
                        reatTime7D8CScanValues.add(wifiScanList.get(i).level);
                    } else if (ssid.equals(Router95A8)) {
                        //Router95A8Level += wifiScanList.get(i).level;
                        reatTime95A8ScanValues.add(wifiScanList.get(i).level );
                    } else if ((ssid.equals(Router7D28))) {
                        //Router7D28Level += wifiScanList.get(i).level;
                        reatTime7D28ScanValues.add(wifiScanList.get(i).level );
                    }
                }
                calculationsForDataPointControl = true;
                //i++;
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "Error Occurred...", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public class getMeanFromParse implements Runnable {

        int y = 1;

        @Override
        public void run() {

            try {
                while (y < 29) {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("DecMeanData");
                    query.whereEqualTo("xPos", "1");
                    query.whereEqualTo("yPos", String.valueOf(y));
                    query.setLimit(4);
                    query.findInBackground(new FindCallback<ParseObject>() {

                        public void done(List<ParseObject> scoreList, ParseException e) {
                            if (e == null) {
                                Log.i("score", "Retrieved " + scoreList.size() + " scores");

                                //for rows for each direction representing a single location
                                for (int i = 0; i < 4; i++) {
                                    //collect data
                                    String xPos = scoreList.get(i).getString("xPos");
                                    String yPos = scoreList.get(i).getString("yPos");
                                    String direction = scoreList.get(i).getString("direction");
                                    String dlink7d28 = scoreList.get(i).getString("dlink7D28");
                                    String dlink95A8 = scoreList.get(i).getString("dlink95A8");
                                    String dlink7d8c = scoreList.get(i).getString("dlink7D8C");

                                    DataPoint dataPoint = new DataPoint();
                                    //dataPoint.xPos = Integer.parseInt(xPos);
                                    dataPoint.xPos = Float.parseFloat(xPos);
                                    dataPoint.yPos = Integer.parseInt(yPos);
                                    dataPoint.direction = direction;
                                    dataPoint.dlink7D28 = Double.parseDouble(dlink7d28);
                                    dataPoint.dlink7D8C = Double.parseDouble(dlink7d8c);
                                    dataPoint.dlink95A8 = Double.parseDouble(dlink95A8);
                                    dataPoints.add(dataPoint);

                               /*     countOffLineMean++;*/
                                }
                            } else {
                                Log.d("score", "Error: " + e.getMessage());
                            }
                        }
                    });
                    y++;
                }

            } catch (Exception ex) {
                Toast.makeText(getBaseContext(), "wrong wrong..", Toast.LENGTH_SHORT).show();
            }
        }
    }
}




    /*public class UpdateLocation implements  Runnable{

        //level of each router.
        int Router95A8Level =0;
        int Router7D28Level=0;
        int Router7D8CLevel=0;

        //mean and sum of real time values.
        double meanOfRouter95A8 = 0;
        double meanOfRouter7D28 = 0;
        double meanOfRouter7D8C = 0;

        @Override
        public void run() {
            try {
                int i = 0;

                //compare the real time data with mean data and update the position
                while (i < numberOfTimesRTData) {
                    wifi.startScan();
                    if (calculationsForDataPointControl) {
                        //get the mean of 3 routers fo 5 rows
                        //Log.i("sum 3", String.valueOf(i) + " " + String.valueOf(Router95A8Level));
                        i++;
                        calculationsForDataPointControl = false;
                        RTDataThreadControl = false;
                    }
                }
                //calculating the mean of 3 different RT routers.
                meanOfRouter7D28 = Router7D28Level / numberOfTimesRTData;
                meanOfRouter7D8C = Router7D8CLevel / numberOfTimesRTData;
                meanOfRouter95A8 = Router95A8Level / numberOfTimesRTData;

                Log.i("mean of 7D28", String.valueOf(meanOfRouter7D28));
                Log.i("mean of 7D8C", String.valueOf(meanOfRouter7D8C));
                Log.i("mean of 95A8", String.valueOf(meanOfRouter95A8));

             *//*   SendDataTesting.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ch1.setText(String.valueOf(Router7D28Level) + " " + String.valueOf(Router7D8CLevel) + " " + String.valueOf(Router95A8Level));
                        ch.setText(String.valueOf(meanOfRouter7D28) + " " + String.valueOf(meanOfRouter7D8C) + " " + String.valueOf(meanOfRouter95A8));
                    }
                });*//*


                    //compare RT mean with the off-line mean data and update the position on the map
               *//* for (DataPoint dp:dataPoints) {

                    //do linear search

                    //exact search
                    if(dp.dlink95A8.equals(String.valueOf(meanOfRouter95A8))&&dp.dlink7D28.equals(String.valueOf(meanOfRouter7D28))
                            &&dp.dlink7D8C.equals(String.valueOf(meanOfRouter7D8C)))
                    {
                        //found the location and update it on the map
                        Toast.makeText(getBaseContext(),dp.xPos + "  " + dp.yPos,Toast.LENGTH_SHORT).show();
                    }

                    //find the possible positions from off-line mean
                    if(true)
                    {

                    }
                }
*//*

            }
            catch (Exception ex)
            {
                Toast.makeText(getBaseContext(),ex.getMessage().toString(),Toast.LENGTH_SHORT).show();
            }
        }

        public class WifiScanReceiver extends BroadcastReceiver {
            public void onReceive(Context c, Intent intent) {
                try {
                    List<ScanResult> wifiScanList = wifi.getScanResults();

                    for (int i = 0; i < wifiScanList.size(); i++) {
                        String ssid = (wifiScanList.get(i).SSID).toString();
                        if ((ssid.equals(Router7D8C))){

                            Router7D8CLevel += wifiScanList.get(i).level;
                        }
                        else if(ssid.equals(Router95A8)) {
                            Router95A8Level += wifiScanList.get(i).level;
                        }
                        else if((ssid.equals(Router7D28))){
                            Router7D28Level += wifiScanList.get(i).level;
                        }
                    }
                    calculationsForDataPointControl = true;
                    //i++;
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), "Error Occurred...", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }*/

 /*private class MeanTask extends AsyncTask<Void,Void,String>{

        int y = 1;
        @Override
        protected String doInBackground(Void... params) {
            //int x = 1;

            while ( y <29) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("MeanData");
                query.whereEqualTo("xPos", "1");
                query.whereEqualTo("yPos", String.valueOf(y));
                query.setLimit(4);
                query.findInBackground(new FindCallback<ParseObject>() {

                    public void done(List<ParseObject> scoreList, ParseException e) {
                        if (e == null) {
                            Log.d("score", "Retrieved " + scoreList.size() + " scores");

                            //for rows for each direction representing a single location
                            for (int i = 0; i < 4; i++) {
                                //collect data
                                String xPos = scoreList.get(i).getString("xPos");
                                String yPos = scoreList.get(i).getString("yPos");
                                String direction = scoreList.get(i).getString("direction");
                                String dlink7d28 = scoreList.get(i).getString("dlink7D28");
                                String dlink95A8 = scoreList.get(i).getString("dlink95A8");
                                String dlink7d8c = scoreList.get(i).getString("dlink7D8C");

                                DataPoint dataPoint = new DataPoint();
                                dataPoint.xPos = "1";
                                dataPoint.yPos = String.valueOf(y);
                                dataPoint.direction = direction;
                                dataPoint.dlink7D28 = dlink7d28;
                                dataPoint.dlink7D8C = dlink7d8c;
                                dataPoint.dlink95A8 = dlink95A8;
                                dataPoints.add(dataPoint);


                            }
                        } else {
                            Log.d("score", "Error: " + e.getMessage());
                        }
                    }
                });
                y++;
     //           break;
            }
            return String.valueOf(dataPoints.size());
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(getBaseContext(), s.toString(), Toast.LENGTH_SHORT).show();
        }
    }
*/

//get the nearest router
//meanOfRouter95A8 is neareset to the user
/*

if (meanOfRouter95A8 > meanOfRouter7D28 && meanOfRouter95A8 > meanOfRouter7D8C) {
        //meanOfRouter95A8 is neareset to the user
        //Log.i("correct11111", String.valueOf(meanOfRouter95A8));
        globalX = 1.0f;
        globalY = 5;
        break;
        }
        //meanOfRouter7D8C is nearest to the user
        else if (meanOfRouter7D8C > meanOfRouter7D28 && meanOfRouter7D8C > meanOfRouter95A8) {
        //meanOfRouter7D8C is nearest to the user
        //Log.i("correct22222", String.valueOf(meanOfRouter7D8C));
        globalX = 2.0f;
        globalY = 27;
        break;
        }

        //meanOfRouter7D28 is nearest to the user
        else if (meanOfRouter7D28 > meanOfRouter95A8 && meanOfRouter7D28 > meanOfRouter7D8C) {
        //meanOfRouter7D28 is nearest to the user
        //Log.i("correct333333", String.valueOf(meanOfRouter7D28));
        globalX = 1.0f;
        globalY = 27;
        break;
        }*/



   /* else if ( rangeCheck(dp.dlink95A8 , threadMeanOfRouter95A8 )   && rangeCheck(dp.dlink7D28, threadMeanOfRouter7D28))
                                {
                                    globalX = dp.xPos;
                                    globalY = dp.yPos;
                                    // countRTLocs++;
                                    break;
                                }
                                else if(rangeCheck(dp.dlink7D28 , threadMeanOfRouter7D28) && rangeCheck(dp.dlink7D8C , threadMeanOfRouter7D8C))
                                {
                                    globalX = dp.xPos;
                                    globalY = dp.yPos;
                                    // countRTLocs++;
                                    break;
                                }
                                else if((rangeCheck(dp.dlink95A8 , threadMeanOfRouter95A8) && rangeCheck(dp.dlink7D8C , threadMeanOfRouter7D8C)))
                                {
                                    globalX = dp.xPos;
                                    globalY = dp.yPos;
                                    // countRTLocs++;
                                    break;
                                }*/



         /* //if you dont get any location then, dont update
                            if(countRTLocs>0)
                            {
                                globalX = x;

                                //got only 1 position
                                if (countRTLocs == 0)
                                {
                                    globalY = y;
                                }
                                //got more than 1 position
                                else
                                {
                                    globalY = y / countRTLocs;
                                }
                            }*/

      /*SendDataTesting.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView txt1 = (TextView) findViewById(R.id.Mean1);
                        TextView txt2 = (TextView)findViewById(R.id.Mean2);
                        TextView txt3 = (TextView) findViewById(R.id.Mean3);
                        int one = (int)meanOfRouter7D28;
                        int two = (int)meanOfRouter7D8C;
                        int three = (int)meanOfRouter95A8;
                        txt1.setText("28" + one);
                        txt2.setText("8C" + two);
                        txt3.setText("A8" + three);
                    }
                });*/


/*

//exact search found
if (dp.dlink95A8 == threadMeanOfRouter95A8 && dp.dlink7D28 == threadMeanOfRouter7D28
        && dp.dlink7D8C == threadMeanOfRouter7D8C) {

        double dist=0.0;
        EuclideanDistance ed = new EuclideanDistance();
        dist = calculateEuclideanDistance(threadMeanOfRouter7D28, threadMeanOfRouter7D8C, threadMeanOfRouter95A8, dp.dlink7D28, dp.dlink7D8C, dp.dlink95A8);

        ed.distance = dist;
        ed.x = dp.xPos;
        ed.y = dp.yPos;
        Euclidean_Distance.add(ed);
        }

        // range check of (+-).40
        else if (rangeCheck(dp.dlink7D28,threadMeanOfRouter7D28,rangeCheck1) && rangeCheck(dp.dlink7D8C,threadMeanOfRouter7D8C,rangeCheck1)
        && rangeCheck(dp.dlink95A8,threadMeanOfRouter95A8,rangeCheck1)) {

        double dist=0.0;

        //create an instance of EuclideanDistance class
        EuclideanDistance ed = new EuclideanDistance();

        //calculate the Euclidean distance between observed and recorded values
        dist = calculateEuclideanDistance(threadMeanOfRouter7D28, threadMeanOfRouter7D8C, threadMeanOfRouter95A8, dp.dlink7D28, dp.dlink7D8C, dp.dlink95A8);

        ed.distance = dist;
        ed.x = dp.xPos;
        ed.y = dp.yPos;
        Euclidean_Distance.add(ed);
        }


        // range check of (+-).80
        else if (rangeCheck(dp.dlink7D28,threadMeanOfRouter7D28,rangeCheck2) && rangeCheck(dp.dlink7D8C,threadMeanOfRouter7D8C,rangeCheck2)
        && rangeCheck(dp.dlink95A8,threadMeanOfRouter95A8,rangeCheck2)) {

        double dist=0.0;

        //create an instance of EuclideanDistance class
        EuclideanDistance ed = new EuclideanDistance();

        //calculate the Euclidean distance between observed and recorded values
        dist = calculateEuclideanDistance(threadMeanOfRouter7D28, threadMeanOfRouter7D8C, threadMeanOfRouter95A8, dp.dlink7D28, dp.dlink7D8C, dp.dlink95A8);

        ed.distance = dist;
        ed.x = dp.xPos;
        ed.y = dp.yPos;
        Euclidean_Distance.add(ed);
        }
        //another range check of (+-)1.20
        else if (rangeCheck(dp.dlink7D28,threadMeanOfRouter7D28,rangeCheck3) && rangeCheck(dp.dlink7D8C,threadMeanOfRouter7D8C,rangeCheck3)
        && rangeCheck(dp.dlink95A8,threadMeanOfRouter95A8,rangeCheck3)) {

        double dist=0.0;

        //create an instance of EuclideanDistance class
        EuclideanDistance ed = new EuclideanDistance();

        //calculate the Euclidean distance between observed and recorded values
        dist = calculateEuclideanDistance(threadMeanOfRouter7D28, threadMeanOfRouter7D8C, threadMeanOfRouter95A8, dp.dlink7D28, dp.dlink7D8C, dp.dlink95A8);

        ed.distance = dist;
        ed.x = dp.xPos;
        ed.y = dp.yPos;
        Euclidean_Distance.add(ed);
        }

        //another range check of (+-)1.60
        else if (rangeCheck(dp.dlink7D28,threadMeanOfRouter7D28,rangeCheck4) && rangeCheck(dp.dlink7D8C,threadMeanOfRouter7D8C,rangeCheck4)
        && rangeCheck(dp.dlink95A8,threadMeanOfRouter95A8,rangeCheck4)) {

        double dist=0.0;

        //create an instance of EuclideanDistance class
        EuclideanDistance ed = new EuclideanDistance();

        //calculate the Euclidean distance between observed and recorded values
        dist = calculateEuclideanDistance(threadMeanOfRouter7D28, threadMeanOfRouter7D8C, threadMeanOfRouter95A8, dp.dlink7D28, dp.dlink7D8C, dp.dlink95A8);

        ed.distance = dist;
        ed.x = dp.xPos;
        ed.y = dp.yPos;
        Euclidean_Distance.add(ed);
        }

        //another range check of (+-)2.0
        else if (rangeCheck(dp.dlink7D28,threadMeanOfRouter7D28,rangeCheck5) && rangeCheck(dp.dlink7D8C,threadMeanOfRouter7D8C,rangeCheck5)
        && rangeCheck(dp.dlink95A8,threadMeanOfRouter95A8,rangeCheck5)) {

        double dist=0.0;

        //create an instance of EuclideanDistance class
        EuclideanDistance ed = new EuclideanDistance();

        //calculate the Euclidean distance between observed and recorded values
        dist = calculateEuclideanDistance(threadMeanOfRouter7D28, threadMeanOfRouter7D8C, threadMeanOfRouter95A8, dp.dlink7D28, dp.dlink7D8C, dp.dlink95A8);

        ed.distance = dist;
        ed.x = dp.xPos;
        ed.y = dp.yPos;
        Euclidean_Distance.add(ed);
        }*/


/*
    public class CreateNewUpdateLocationThreads implements Runnable{
        @Override
        public void run() {
            try {
                int numberOfThreads = 0;
                while (numberOfThreads < 3) {
                    Thread locationThread = new Thread(new UpdateLocation());
                    locationThread.start();
                    numberOfThreads++;
                }

            }
            catch (Exception ex)
            {
                Toast.makeText(getBaseContext(),"Error occured in creating thread...", Toast.LENGTH_SHORT).show();
            }
        }
    }*/