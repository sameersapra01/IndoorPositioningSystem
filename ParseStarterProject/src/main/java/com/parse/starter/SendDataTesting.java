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
import android.view.MotionEvent;
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    //constant X Point
    private static final int K_OUR_POINT = 26;
    
    //range between last 2 Positions is 3 feets
    private static final int rangeBT2YPos = 8;

    //number of real time data
    private static final int numberOfTimesRTData = 5;

    //median point
    private static final int medianIndex = 2;

    //accuracy
    private static final int leastDistanceRange = 15;

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

    //get initial user position
    boolean gotInitialPosition = false;

//desk variables
    int offSetTopBorder = 72;
    int roomYLength = 28;
    int roomXLength = 38;
    int kZERO = 0;
    int sideYPos = 26;
    int deskBorder = 22;
    int specialCase = 22;
    int startOfRowThree = 33;
    int maxOfRowZerro = 2;
    float additionToXPos = 0.1f;
    private Set<Float> badDeskNumbers = new HashSet<Float>(Arrays.asList(
            new Float[]{0.3f,0.4f,0.5f,0.6f,1.3f,1.4f,1.5f,1.6f,1.7f,1.8f,2.3f,2.4f,2.5f,2.6f,2.7f,2.8f}
    ));

    int[] fineNumbers = {0,1,2,3};


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

/*            //get the mean from parse and store it in a list of DataPoint class in a thread
            Thread th1 = new Thread(new getMeanFromParse());
            //start the thread
            th1.start();*/

            new MeanTask().execute();

            //wait for this thread to get all data and die
            //th1.join();

/*            Toast.makeText(this,"Got all data",Toast.LENGTH_SHORT).show();*/




/*            //Update Location thread
            Thread updateLocationThread = new Thread( new UpdateLocation());
            updateLocationThread.start();*/



      /*      //problems are the old created threads will show the old location for a new position...
            Thread realTimeDP = new Thread(new CreateNewUpdateLocationThreads());
            realTimeDP.start();*/

/*            //map thread
            ScheduledThreadPoolExecutor mapThread = new ScheduledThreadPoolExecutor(5);
            mapThread.scheduleWithFixedDelay(new MyTask(), 0, 200, TimeUnit.MILLISECONDS);

            //can be in one single thread instead of calling it every 2 seconds.
            ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
            long delay = 200;
            exec.scheduleWithFixedDelay(new UpdateLocation(), 0, delay, TimeUnit.MILLISECONDS);*/

        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }




    @Override

    public boolean onTouchEvent(MotionEvent event) {

        if(!gotInitialPosition) {
            boolean isTouchError = false;
            int x = (int) event.getX();
            int y = (int) event.getY() - offSetTopBorder;
            ImageView map = (ImageView) findViewById(R.id.Map);
            float mapX = map.getLeft();
            float mapY = map.getTop();
//        if(pathDraw == true)
//        {
//            pathArray = new int[3];
//            int count = 0;
//
//            while (count < 3) {
//                ImageView iv = new ImageView(this);
//                iv.setImageResource(R.drawable.path);
//                RelativeLayout rl = (RelativeLayout) findViewById(R.id.BackGround);
//
//                iv.setId(View.generateViewId());
//                pathArray[count] = iv.getId();
//                rl.addView(iv);
//
//                count++;
//            }
//
//
//            pathDraw = false;
//        }

            Xratio = (map.getRight() - map.getLeft()) / roomYLength;
            Yratio = (map.getBottom() - map.getTop()) / roomXLength;
            float feetX = (x - mapX) / Xratio;
            float feetY = (y - mapY) / Yratio;
            if (feetX >= kZERO && feetX <= roomYLength && feetY >= kZERO && feetY <= roomXLength) {
                float xPos = kZERO;
                float yPos = feetX;
                feetY = (int) feetY;

                float countXPos = 0f;
                for (int i = 0; i < roomXLength; i++) {
                    //set the borders of row 0 and 2
                    if (i >= kZERO && i <= maxOfRowZerro && feetY <= maxOfRowZerro) {
                        xPos = 0;//row 0
                        break;
                    } else if (i >= startOfRowThree && feetY >= startOfRowThree) {
                        xPos = 3; //row 3
                        break;
                    } else if (i == specialCase && feetY == specialCase) //setting special case between 1.9 &2.0 = 1.95
                    {
                        xPos = 1.95f;
                        break;
                    } else if (i == feetY) {
                        xPos = countXPos;
                        break;
                    }
                    if (i >= maxOfRowZerro && i != specialCase) {
                        countXPos = countXPos + additionToXPos;
                    }
                }
                xPos = (int) (xPos * 100);
                xPos = (float) xPos / (float) 100d;
                if (badDeskNumbers.contains(xPos)) {
                    //check if the touch was on the desks
                    if (yPos < deskBorder) {
                        isTouchError = true;
                        gotInitialPosition = false;
                    } else // if not default set to 26 for yPosition
                    {
                        yPos = sideYPos;
                    }
                } else {

                    //the spot is between desks check if touch is less 22 on y
                    // then default it the the rows between desks
                    if (yPos < deskBorder) {
                        if (xPos < 0.5) {
                            xPos = fineNumbers[0];
                        } else if (xPos > 0.5 && xPos < 1.5) {
                            xPos = fineNumbers[1];
                        } else if (xPos > 1.5 && xPos < 2.5) {
                            xPos = fineNumbers[2];
                        } else if (xPos > 2.5) {
                            xPos = fineNumbers[3];
                        }
                    } else if (xPos != fineNumbers[0] && xPos != fineNumbers[1] &&
                            xPos != fineNumbers[2] && xPos != fineNumbers[3])// if number is over desk line then default to 26 feet on y this does not include normal rows 0,1,2,3
                    {
                        yPos = sideYPos;
                    }
                }
                if (isTouchError != true) {
                    globalX = xPos;
                    globalY = (int) yPos;
/////////////////////////////////////////////////////////////


                    Log.i("X Pos...", String.valueOf(globalX));
                    Log.i("Y Pos...", String.valueOf(globalY));
                    gotInitialPosition = true;

                    //map thread
                    ScheduledThreadPoolExecutor mapThread = new ScheduledThreadPoolExecutor(5);
                    mapThread.scheduleWithFixedDelay(new MapTask(), 0, 100, TimeUnit.MILLISECONDS);

                    //can be in one single thread instead of calling it every 2 seconds.

                    //wifi scan thread
                    ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
                    long delay = 100;
                    exec.scheduleWithFixedDelay(new UpdateLocationTask(), 0, delay, TimeUnit.MILLISECONDS);
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                } else {
                    Toast.makeText(getBaseContext(), "Desk Touch Error", Toast.LENGTH_SHORT).show();
                }


            } else {
                Toast.makeText(getBaseContext(), "Position Clicked Out Of Range", Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }


/*
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!gotInitialPosition) {
            int x = (int)event.getX();
            int y = (int)event.getY()-72;
            ImageView map = (ImageView)findViewById(R.id.Map);
            float mapX = map.getLeft();
            float mapY = map.getTop();
            Xratio = (map.getRight() - map.getLeft()) / 28;
            Yratio = (map.getBottom() - map.getTop()) / 38;

            float feetX = (x- mapX)/Xratio;
            float feetY = (y - mapY)/Yratio ;
            if(feetX >=0 && feetX <= 28 && feetY >=0 && feetY <=38)
            {
                float xPos = 0;
                float yPos = feetX;
                if (feetY >= 0 && feetY <= 5) {
                    xPos = 0;
                } else if (feetY > 5 && feetY <= 10) {
                    xPos = 0.5f;
                } else if (feetY > 10 && feetY <= 15) {
                    xPos = 1;
                } else if (feetY > 15 && feetY <= 20) {
                    xPos = 1.5f;
                } else if (feetY > 20 && feetY <= 26) {
                    xPos = 2;
                } else if (feetY > 26 && feetY <= 31) {
                    xPos = 2.5f;
                } else if (feetY > 31 && feetY <= 38) {
                    xPos = 3;
                }
                if(xPos == 0.5f && yPos >= 22|| xPos == 1.5f && yPos >= 22 || xPos == 2.5f && yPos >= 22 ||
                        xPos == 0 || xPos == 1 || xPos == 2 || xPos == 3 )
                {
                    if(yPos >=26 && feetY >= 32 && feetY <=35)
                    {
                        Toast.makeText(getBaseContext(),"Please try again",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        globalX = xPos;
                        globalY = (int) yPos;

                        Log.i("X Pos...", String.valueOf(globalX));
                        Log.i("Y Pos...", String.valueOf(globalY));
                        gotInitialPosition = true;

                        //map thread
                        ScheduledThreadPoolExecutor mapThread = new ScheduledThreadPoolExecutor(5);
                        mapThread.scheduleWithFixedDelay(new MapTask(), 0, 100, TimeUnit.MILLISECONDS);

                        //can be in one single thread instead of calling it every 2 seconds.

                        //wifi scan thread
                        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
                        long delay = 100;
                        exec.scheduleWithFixedDelay(new UpdateLocationTask(), 0, delay, TimeUnit.MILLISECONDS);
                    }
                }
                else
                {
                    Toast.makeText(getBaseContext(),"Erro teacher...",Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(getBaseContext(),"Position Clicked Out Of Range",Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }
*/


    private class MapTask implements Runnable {
        @Override
        public void run() {

            SendDataTesting.this.runOnUiThread(new Runnable() {

                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                public void run() {
                    ImageView map = (ImageView) findViewById(R.id.Map);
                    Xratio = (map.getRight() - map.getLeft()) / 28;
                    Yratio = (map.getBottom() - map.getTop()) / 38;


                    ImageView person = (ImageView) findViewById(R.id.Person);

                    //x and y the user has entered converted to integer
                    // this is the number that comes from the data
                    globalX = (int) (globalX * 100);
                    globalX = (float) globalX / (float) 100d;
                    String tmpX = String.valueOf(globalX);
                    int y = globalY;// this is the number that comes from the data
                    int x = 0;
                    if (tmpX.equals("0") || tmpX.equals("0.0"))
                    {
                        x = 2;
                    }
                    else if (tmpX.equals("0.1"))
                    {
                        x = 3;
                    }
                    else if (tmpX.equals("0.2"))
                    {
                        x = 4;
                    }
                    else if (tmpX.equals("0.3"))
                    {
                        x = 5;
                    }
                    else if (tmpX.equals("0.4"))
                    {
                        x = 6;
                    }
                    else if (tmpX.equals("0.5"))
                    {
                        x = 7;
                    }
                    else if (tmpX.equals("0.6"))
                    {
                        x = 8;
                    }
                    else if (tmpX.equals("0.7"))
                    {
                        x = 9;
                    }
                    else if (tmpX.equals("0.8"))
                    {
                        x = 10;
                    }
                    else if (tmpX.equals("0.9"))
                    {
                        x = 11;
                    }
                    else if (tmpX.equals("1") || tmpX.equals("1.0"))
                    {
                        x = 12;
                    }
                    else if (tmpX.equals("1.1"))
                    {
                        x = 13;
                    }
                    else if (tmpX.equals("1.2"))
                    {
                        x = 14;
                    }
                    else if (tmpX.equals("1.3"))
                    {
                        x = 15;
                    }
                    else if (tmpX.equals("1.4"))
                    {
                        x = 16;
                    }
                    else if (tmpX.equals("1.5"))
                    {
                        x = 17;
                    }
                    else if (tmpX.equals("1.6"))
                    {
                        x = 18;
                    }
                    else if (tmpX.equals("1.7"))
                    {
                        x = 19;
                    }
                    else if (tmpX.equals("1.8"))
                    {
                        x = 20;
                    }
                    else if (tmpX.equals("1.9"))
                    {
                        x = 21;
                    }
                    else if (tmpX.equals("2") || tmpX.equals("2.0"))
                    {
                        x = 23;
                    }
                    else if (tmpX.equals("2.1"))
                    {
                        x = 24;
                    }
                    else if (tmpX.equals("2.2"))
                    {
                        x = 25;
                    }
                    else if (tmpX.equals("2.3"))
                    {
                        x = 26;
                    }
                    else if (tmpX.equals("2.4"))
                    {
                        x = 27;
                    }
                    else if (tmpX.equals("2.5"))
                    {
                        x = 28;
                    }
                    else if (tmpX.equals("2.6"))
                    {
                        x = 29;
                    }
                    else if (tmpX.equals("2.7"))
                    {
                        x = 30;
                    }
                    else if (tmpX.equals("2.8"))
                    {
                        x = 31;
                    }
                    else if (tmpX.equals("2.9"))
                    {
                        x = 32;
                    }
                    else if (tmpX.equals("3") || tmpX.equals("3.0"))
                    {
                        x = 34;
                    }

                  /*  if (tmpX == 0) {
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
                    }*/

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




    public class UpdateLocationTask implements Runnable {
        @Override
        public void run() {
            try {
                int i = 0;
                if (RTDataThreadControl) {
                    //setting the value of all 3 router's level to 0
                    Router7D28Level = 0;
                    Router7D8CLevel = 0;
                    Router95A8Level = 0;

                    //clearing the real time value list
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

                    // new thread...
                    new Thread() {

                        //List to store all off-line mean datapoints from parse into a list of class DataPoint
                        List<DataPoint> dataPointsThread = new ArrayList<>(dataPoints);

                        //This list is used to store all positions found using range check into a list of LeastDistance class.
                        List<LeastDistance> Least_Distance = new ArrayList<>();
                        List<EuclideanDistance> Euclidean_Distance = new ArrayList<>();

                        //make a copy of RT mean of routers for this thread
                        double threadMeanOfRouter7D28 = meanOfRouter7D28;
                        double threadMeanOfRouter7D8C = meanOfRouter7D8C;
                        double threadMeanOfRouter95A8 = meanOfRouter95A8;

                    /*    //
                        float x = 0.0f;
                        int y = 0;*/

                        @Override
                        public void run() {
                            //compare RT mean with the off-line mean data and update the position on the map
                            for (DataPoint dp : dataPointsThread) {
                                //do linear search and range checks
                                for (float rangeIteration = 0.0f; rangeIteration <= 3.0f; rangeIteration += 1.0f) {
                                    if (rangeCheck(dp.dlink7D28, threadMeanOfRouter7D28, rangeIteration) && rangeCheck(dp.dlink7D8C, threadMeanOfRouter7D8C, rangeIteration)
                                            && rangeCheck(dp.dlink95A8, threadMeanOfRouter95A8, rangeIteration)) {

/////new code
                                        double distX=0.0;
                                        double distY=0.0;
                                        LeastDistance ldist = new LeastDistance();

                                        //if x is same
                                        if(globalX==dp.xPos)
                                        {
                                            distY = getYDistance(globalY,dp.yPos);
                                            ldist.distance = distY;
                                        }
                                        //if x is different , 0,10(dp) and 2.5,16(global/current pos)
                                        else {
                                            distY = getYDistance(globalY, K_OUR_POINT);
                                            distY += getYDistance(dp.yPos,K_OUR_POINT);
                                            distX = getXDistance(globalX, dp.xPos);

                                            ldist.distance = distX+distY;
                                        }
                                        ldist.x = dp.xPos;
                                        ldist.y = dp.yPos;
                                        ldist.globalX = globalX;
                                        ldist.globalY = globalY;

                                        Least_Distance.add(ldist);
                                       // break;

/////new code

                                        //Eculidean Distance Code
                                        double euclideanDist = 0.0;

                                        //create an instance of EuclideanDistance class
                                        EuclideanDistance ed = new EuclideanDistance();

                                        //calculate the Euclidean distance between observed and recorded values
                                        euclideanDist = calculateEuclideanDistance(threadMeanOfRouter7D28, threadMeanOfRouter7D8C, threadMeanOfRouter95A8, dp.dlink7D28, dp.dlink7D8C, dp.dlink95A8);

                                        ed.distance = euclideanDist;
                                        ed.x = dp.xPos;
                                        ed.y = dp.yPos;
                                        ed.dlink7D28 = threadMeanOfRouter7D28;
                                        ed.dlink7D8C = threadMeanOfRouter7D8C;
                                        ed.dlink95A8 = threadMeanOfRouter95A8;
                                        Euclidean_Distance.add(ed);

                                        //this line could fuck up, think about it
                                        //did modify this
                                        break;
                                    }
                                }
                            }

                            if (Least_Distance.size() > 0) {
                                boolean gotPositionFromLeastDist = false;
                                double leastDist = Least_Distance.get(0).distance;
                                for (LeastDistance ld : Least_Distance) {

                                    //Log.i("DP : " , String.valueOf( edLeast.distance ) + " " + String.valueOf(edLeast.x) + " " + String.valueOf(edLeast.y) );
                                    //getting least euclidean distance from euclidean list
                                    if (!(leastDist < ld.distance)) {
                                        leastDist = ld.distance;
                                        //assign x and y to update position/location
                                       // globalX = edLeast.x;
                                        // globalY = edLeast.y;
                                        if(ld.distance <=leastDistanceRange)
                                        {
                                            globalX = ld.x;
                                            globalY = ld.y;
                                            gotPositionFromLeastDist = true;
                                        }
                                    }
                                }

                                if(!gotPositionFromLeastDist) {
                                    //then compare the euclidean distance points
                                    //List to store all off-line mean datapoints from parse into a list of class DataPoint
                                    //checking if range checks returned any positions or not
                                    if (Euclidean_Distance.size() > 0) {
                                        //Log.i("IF  worked...", String.valueOf(Euclidean_Distance.size()));
                                        //double leastDist = Euclidean_Distance.get(0).distance;
                                        for (EuclideanDistance edLeast : Euclidean_Distance) {
                                            //getting least euclidean distance from euclidean list
                                            if (!(leastDist < edLeast.distance)) {
                                                leastDist = edLeast.distance;
                                                //assign x and y
                                                globalX = edLeast.x;
                                                globalY = edLeast.y;
                                            }
                                        }
                                    }
                                }
                            }





 /////new code

/////new code



////////////sapra

                            /*//cat tracker

                            //new thread to get the least distance from the Euclidean_Distance list.
                            new Thread() {
                                @Override
                                public void run() {

                                    double avgRouter8C = 0.0;
                                    double avgRouter28 = 0.0;
                                    double avgRouterA8 = 0.0;

                                    double iter1 = 0.0;

                                    boolean positionFound = false;

                                    //List to store all off-line mean datapoints from parse into a list of class DataPoint
                                    List<DataPoint> dataPointsThread2 = new ArrayList<>(dataPointsThread);
                                    List<EuclideanDistance> edListThreadCopy = new ArrayList<>(Euclidean_Distance);

                                    //checking if range checks returned any positions or not
                                    if (edListThreadCopy.size() > 0) {
                                        Log.i("IF  worked...",String.valueOf(edListThreadCopy.size()));
                                        double leastDist = edListThreadCopy.get(0).distance;
                                        for (EuclideanDistance edLeast : edListThreadCopy) {
                                            //getting least euclidean distance from euclidean list
                                            if (!(leastDist < edLeast.distance)) {
                                                leastDist = edLeast.distance;
                                                //assign x and y
                                                x = edLeast.x;
                                                y = edLeast.y;
                                            }
                                        }


////sameer sameer

                                        //sort the euclidean list and ge the 3 least distances and get their mean, compare it with offline and get the position
                                        Collections.sort(edListThreadCopy, new Comparator<EuclideanDistance>() {
                                            @Override
                                            public int compare(EuclideanDistance lhs, EuclideanDistance rhs) {
                                                return lhs.distance < rhs.distance ? -1
                                                        : lhs.distance > rhs.distance ? 1
                                                        : 0;
                                            }
                                        });

                                        for( iter1 = 0; iter1<3; ++iter1) {
                                            // 2(index) < 2
                                            if(iter1 < edListThreadCopy.size())
                                            {
                                                avgRouter8C += edListThreadCopy.get((int)iter1).dlink7D8C;
                                                avgRouter28 += edListThreadCopy.get((int)iter1).dlink7D28;
                                                avgRouterA8 += edListThreadCopy.get((int)iter1).dlink95A8;
                                            }
                                            else
                                            {
                                                break;
                                            }
                                        }

                                        Log.i("iter 1 : " , String.valueOf(iter1));
                                        Log.i("edThreadsize : " , String.valueOf(edListThreadCopy.size()));

                                        //get the mean
                                        avgRouter28 = avgRouter28 / iter1;
                                        avgRouter8C = avgRouter8C / iter1;
                                        avgRouterA8 = avgRouterA8 / iter1;

                                        Log.i("Router 28 : " , String.valueOf(avgRouter28) );
                                        Log.i("Router 8C : " , String.valueOf(avgRouter8C) );
                                        Log.i("Router A8 : " , String.valueOf(avgRouterA8) ) ;

                                        //compare this new mean with offline dp
                                        for (DataPoint dpp:dataPointsThread2) {

                                            if (!positionFound) {
                                                //range check to find exact search
                                                for (float rangeIteration1 = 0.0f; rangeIteration1 <= 1.0f; rangeIteration1 += 0.05f) {
                                                    if (rangeCheck(dpp.dlink7D28, avgRouter28, rangeIteration1) && rangeCheck(dpp.dlink7D8C, avgRouter8C, rangeIteration1)
                                                            && rangeCheck(dpp.dlink95A8, avgRouterA8, rangeIteration1)) {
                                                        if (checkTwoPositionsRange(globalY, dpp.yPos)) {
                                                            globalX = dpp.xPos;
                                                            globalY = dpp.yPos;
                                                            Log.i("Position found : " , String.valueOf(globalY) + String.valueOf(globalX));
                                                        }
                                                        positionFound = true;
                                                        break;
                                                    }
                                                }

                                            }
                                        }
////sameer sameer



                                        //displaying each data point
                                        for (EuclideanDistance eee: edListThreadCopy) {
                                            Log.i("DP : " , String.valueOf( eee.distance ) + " " + String.valueOf(eee.x) + " " + String.valueOf(eee.y) );
                                        }

                                        globalX = x;
                                        globalY = y;

                                        //check if position jumped too much, should be less than 3 feets
                                        //should be greater than 0, because now globalY contains last position
                                        if(globalY > 0) {
                                            if(checkTwoPositionsRange(globalY, y)){
                                                globalX = x;
                                                globalY = y;
                                            }
                                            else{
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
                                    else{
                                        Log.i("IF didnt work...",String.valueOf(edListThreadCopy.size()));
                                    }
                                }
                            }.start();
*/
////////////sapra


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

    int getYDistance(int d1, int d2)
    {
        if (d1 >= d2)
        {
            return d1 - d2;
        }
        else
        {
            return d2 - d1;
        }
    }

    int getXDistance(double global, double dp)
    {
        int feets = 0;
        if (global > dp)
        {
            while (dp < global)
            {
                if (dp == 1.9)
                {
                    feets++;
                    if(1.95 == global)
                    {
                        break;
                    }
                }
                dp += 0.1;
                feets++;
                dp = (int)(dp * 100);
                dp = (float)dp / (float)100d;
            }
        }
        else if (dp > global)
        {
            while (global < dp)
            {
                if (global == 1.9)
                {
                    feets++;
                    if (1.95 == dp)
                    {
                        break;
                    }
                }
                global += 0.1;
                feets++;
                global = (int)(global * 100);
                global = (float)global / (float)100d;
            }
        }
        return feets;
    }




    public void sortAndRangeCheckRealTimeValues()
    {
        medianOfRouter7D8C = 0;
        medianOfRouter7D28 = 0;
        medianOfRouter95A8 = 0;

        //sort all 3 arrays and get the median
        if((reatTime7D8CScanValues.size()==numberOfTimesRTData) && (reatTime95A8ScanValues.size()==numberOfTimesRTData) && (reatTime7D28ScanValues.size()==numberOfTimesRTData) )
        {
            //router 8C
            Collections.sort(reatTime7D8CScanValues);
            medianOfRouter7D8C = reatTime7D8CScanValues.get(medianIndex);
/*            Log.i("8C Values", reatTime7D8CScanValues.get(0) + " " + reatTime7D8CScanValues.get(1) + " " + reatTime7D8CScanValues.get(2)
                    + reatTime7D8CScanValues.get(3) + " " + reatTime7D8CScanValues.get(4) + " " + String.valueOf(medianOfRouter7D8C));*/

            //router A8
            Collections.sort(reatTime95A8ScanValues);
            medianOfRouter95A8 = reatTime95A8ScanValues.get(medianIndex);
       /*     Log.i("A8 Values", reatTime95A8ScanValues.get(0) + " " + reatTime95A8ScanValues.get(1) + " " + reatTime95A8ScanValues.get(2)
                    + reatTime95A8ScanValues.get(3) + " " + reatTime95A8ScanValues.get(4) + " " + String.valueOf(medianOfRouter95A8));*/

            //router 28
            Collections.sort(reatTime7D28ScanValues);
            medianOfRouter7D28 = reatTime7D28ScanValues.get(medianIndex);
/*            Log.i("28 Values", reatTime7D28ScanValues.get(0) + " " + reatTime7D28ScanValues.get(1) + " " + reatTime7D28ScanValues.get(2)
                    + reatTime7D28ScanValues.get(3) + " " + reatTime7D28ScanValues.get(4) + " " + String.valueOf(medianOfRouter7D28));*/

            routerRangeCheck(reatTime7D28ScanValues,medianOfRouter7D28 ,"28");
            routerRangeCheck(reatTime95A8ScanValues,medianOfRouter95A8 ,"A8");
            routerRangeCheck(reatTime7D8CScanValues,medianOfRouter7D8C ,"8C");
        }
    }

    //To check whether all real time 5 values of a router are in range of +-6
    void routerRangeCheck(List<Integer> arry , int median , String router )
    {
        //new list will contain checked values
        List<Integer> list = new ArrayList<>();
        for (Integer ass: arry) {
            if(ass!=null) {
                // -50 > -53
                if (ass >= median) {
                    //-50 - (-53)
                    if ((ass - median <= 6)) {
                        //didnt pass the range check, delete this from the array
                        list.add(ass);
                        //Log.i("1...",  router + String.valueOf(median) +  String.valueOf(ass));
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
                        //Log.i("2...",  router + String.valueOf(median) + String.valueOf(ass));
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
    }

    public double sumRTRouterLevels(List<Integer> ar)
    {
        double mean = 0.0;
        for (Integer dd: ar) {
            mean += dd;
        }
        mean /= ar.size();
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


     private class MeanTask extends AsyncTask<Void,Void,String>{
        @Override
        protected String doInBackground(Void... params) {

            try {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Final0");
                query.setLimit(544);
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> scoreList, ParseException e) {
                        if (e == null) {
                            for (ParseObject po: scoreList) {
                                String xPos = po.getString("xPos");
                                String yPos = po.getString("yPos");
                                String direction = po.getString("direction");
                                String dlink7d28 = po.getString("dlink7D28");
                                String dlink95A8 = po.getString("dlink95A8");
                                String dlink7d8c = po.getString("dlink7D8C");


                                DataPoint dataPoint = new DataPoint();
                                //dataPoint.xPos = Integer.parseInt(xPos);
                                dataPoint.xPos = Float.parseFloat(xPos);
                                dataPoint.yPos = Integer.parseInt(yPos);
                                dataPoint.direction = direction;
                                dataPoint.dlink7D28 = Double.parseDouble(dlink7d28);
                                dataPoint.dlink7D8C = Double.parseDouble(dlink7d8c);
                                dataPoint.dlink95A8 = Double.parseDouble(dlink95A8);
                                dataPoints.add(dataPoint);
                            }
                        } else {
                            Log.d("Error", "ErrorMessagedd : " + e.getMessage());
                        }
                    }
                });
            } catch (Exception ex) {
                Toast.makeText(getBaseContext(), "wrong wrong..", Toast.LENGTH_SHORT).show();
            }
            return "Got all data.";
        }

        @Override
        protected void onPostExecute(String msg) {
            Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
            Toast.makeText(getBaseContext(), "Give us your position by clicking on map..." , Toast.LENGTH_SHORT).show();
        }
    }

    /*public class getMeanFromParse implements Runnable {
        @Override
        public void run() {

            try {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Final0");
                query.setLimit(436);
                query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> scoreList, ParseException e) {
                    if (e == null) {
                        Log.i("List Size", String.valueOf(scoreList.size()));
                        Toast.makeText(getBaseContext() , "Got all data : " +  String.valueOf(scoreList.size()) , Toast.LENGTH_LONG).show();
                        for (ParseObject po: scoreList) {
                            String xPos = po.getString("xPos");
                            String yPos = po.getString("yPos");
                            String direction = po.getString("direction");
                            String dlink7d28 = po.getString("dlink7D28");
                            String dlink95A8 = po.getString("dlink95A8");
                            String dlink7d8c = po.getString("dlink7D8C");


                            DataPoint dataPoint = new DataPoint();
                            //dataPoint.xPos = Integer.parseInt(xPos);
                            dataPoint.xPos = Float.parseFloat(xPos);
                            dataPoint.yPos = Integer.parseInt(yPos);
                            dataPoint.direction = direction;
                            dataPoint.dlink7D28 = Double.parseDouble(dlink7d28);
                            dataPoint.dlink7D8C = Double.parseDouble(dlink7d8c);
                            dataPoint.dlink95A8 = Double.parseDouble(dlink95A8);
                            dataPoints.add(dataPoint);
                        }
                    } else {
                        Log.d("Error", "ErrorMessagedd : " + e.getMessage());
                    }
                }
                });
            } catch (Exception ex) {
                Toast.makeText(getBaseContext(), "wrong wrong..", Toast.LENGTH_SHORT).show();
            }
        }
    }*/




    /*public class getMeanFromParse implements Runnable {

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

                                 *//*   countOffLineMean++;*//*
                                }
                            } else {
                                Log.d("Error", "ErrorMessage : " + e.getMessage());
                            }
                        }
                    });
                    y++;
                }

            } catch (Exception ex) {
                Toast.makeText(getBaseContext(), "wrong wrong..", Toast.LENGTH_SHORT).show();
            }
        }
    }*/
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