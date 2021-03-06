package com.parse.starter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Image;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by samee on 2016-02-01.
 */
public class SendDataTesting extends Activity {

    //Our 3 routers SSID
    private static final String Router95A8 = "dlink-95A8";
    private static final String Router7D28 = "dlink-7D28";
    private static final String Router7D8C = "dlink-7D8C";

    //consts for range check
    private static final float k_ZERO = 0.0f;
    private static final float k_THREE = 3.0f;
    private static final float k_ITERATION = .30f;

    //constant X Point
    private static final int K_OUR_POINT = 26;

    //range between last 2 Positions is 3 feets
    private static final int rangeBT2YPos = 8;

    //number of real time data threads
    private static final int numberOfTimesRTData = 5;

    //median point
    private static final int medianIndex = 2;

    //accuracy
    double leastDistanceRange = 10.0;

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
    //fist start for threads
    boolean firstStart = false;
    //
    int countOutOfRangePositionCalculated = 0;

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
    float[] possibleXValues= {0.0f,0.1f,0.2f,0.3f,0.4f,0.5f,0.6f,0.7f,0.8f,0.9f,1.0f,1.1f,1.2f,1.3f,1.4f,1.5f,1.6f,1.7f,1.8f,1.9f,1.95f,2.0f,2.1f,2.2f,
            2.3f,2.4f,2.5f,2.6f,2.7f,2.8f,2.9f,3.0f};

    int[] fineNumbers = {0,1,2,3};

    //path draw varaiables
    boolean pathDraw = false;
    int[] pathArray;
    int[] createdArray;
    int globalNumberOfPaths = 0;
    float xPositionPath = 0;
    int yPositionPath = 0;
    boolean letUserClickPath = false;
    List<Integer> drawOnY ;

    List<Float> drawOnX;


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

            new MeanTask().execute();

        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }





    MotionEvent finalEvent;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finalEvent = event;

        SendDataTesting.this.runOnUiThread(new Runnable() {

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            public void run() {

                // if(!gotInitialPosition) {
                boolean isTouchError = false;

                int x = (int) finalEvent.getX();
                int y = (int) finalEvent.getY() - offSetTopBorder;
                ImageView map = (ImageView) findViewById(R.id.Map);
                float mapX = map.getLeft();
                float mapY = map.getTop();


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
                    //change to 2 decimal places
                    xPos = (int)(xPos*100);
                    xPos = (float)xPos/(float)100d;

//math the numbers to the closest of the possible number
                    xPos = findNearestNumber(possibleXValues,xPos);

                    if (badDeskNumbers.contains(xPos) )
                    {

                        //check if the touch was on the desks
                        if (yPos < deskBorder) {
                            isTouchError = true;

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

                        if(!gotInitialPosition) {
                            globalX = xPos;
                            globalY = (int) yPos;
/////////////////////////////////////////////////////////////


                            Log.i("X Pos...", String.valueOf(globalX));
                            Log.i("Y Pos...", String.valueOf(globalY));
                            gotInitialPosition = true;
                        }

                        //initilize the path

                        if(letUserClickPath == true)
                        {
                            pathDraw = true;

                            xPositionPath = xPos;
                            yPositionPath = (int)yPos;

                            int count =0;
                            //first delete the old images
                            if (createdArray != null) {
                                while (count < createdArray.length) {
                                    ImageView pathView = (ImageView) findViewById(createdArray[count]);
                                    pathView.setVisibility(View.INVISIBLE);

                                    count++;
                                }
                                createdArray = null;
                                pathArray = null;
                            }

                            int amountOfPaths = 0;
                            if(globalX == xPositionPath)
                            {
                                amountOfPaths += getYDistance(globalY, yPositionPath);
                            }
                            else
                            {
                                amountOfPaths += getYDistance(globalY, 26);
                                amountOfPaths += getYDistance(yPositionPath, 26);
                                amountOfPaths += getXDistance(globalX, xPositionPath);
                            }
                            globalNumberOfPaths = amountOfPaths;
                            pathArray = new int[amountOfPaths];
                            drawOnX = new ArrayList<Float>(amountOfPaths);
                            drawOnY =new ArrayList<Integer>(amountOfPaths);
                            count = 0;

                            SendDataTesting.this.runOnUiThread(new Runnable() {

                                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                                public void run()
                                {
                                    int count = 0;
                                    while (count < pathArray.length)
                                    {
                                        ImageView pathView = new ImageView(SendDataTesting.this);
                                        pathView.setImageResource(R.drawable.path);
                                        RelativeLayout rl = (RelativeLayout) findViewById(R.id.Background);

                                        pathView.setId(View.generateViewId());
                                        pathArray[count] = pathView.getId();
                                        pathView.setLeft(100 * (1 + count) + pathView.getLeft());
                                        pathView.setRight(100 * (1 + count) + pathView.getRight());
                                        pathView.setBottom(pathView.getBottom());
                                        pathView.setTop(pathView.getTop());
                                        rl.addView(pathView);

                                        count++;
                                    }

                                    createdArray = pathArray;
                                    letUserClickPath = false;
                                }
                            });
                        }

                        if(!firstStart) {
                            //map thread
                            ScheduledThreadPoolExecutor mapThread = new ScheduledThreadPoolExecutor(5);
                            mapThread.scheduleWithFixedDelay(new MapTask(), 0, 100, TimeUnit.MILLISECONDS);

                            //can be in one single thread instead of calling it every 2 seconds.

                            //wifi scan thread
                            ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
                            long delay = 100;
                            exec.scheduleWithFixedDelay(new UpdateLocationTask(), 0, delay, TimeUnit.MILLISECONDS);
                            firstStart = true;
                        }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    } else {
                        Toast.makeText(getBaseContext(), "Desk Touch Error", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    Toast.makeText(getBaseContext(), "Position Clicked Out Of Range", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return false;
    }

    //this function is to match numbers with their feet such as 0.5 as 7 feet
    public int NumberMatch(String number) {
        int x = 2;
        int oddNumberOut = 33;
        int lastPossibleNumber = 34;

        for (int i = 0; i < possibleXValues.length; i++) {
            // Log.i("MyActivity", "x= " + x);
            if (number.equals(String.valueOf(possibleXValues[i]))) {
                //    Log.i("MyActivity", "got x= " + x);
                break;
            }
            x++;
        }
        if (x == oddNumberOut) {
            //Log.i("MyActivity", "changed x= " + x);
            x = lastPossibleNumber;
            //Log.i("MyActivity", "to x= " + x);
        }
        return  x;
    }


    int numberOfClicks =0;
    private void DrawPath()
    {

        SendDataTesting.this.runOnUiThread(new Runnable() {

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            public void run()
            {

                int count = 0;
                int amountOfPaths = 0;
                RelativeLayout deleteView = (RelativeLayout) findViewById(R.id.Background);
                boolean drawSecondOption = false;

                if(numberOfClicks >=0)
                {

////////////uP DATE the path finder if the user is moving
                    if(pathDraw == true)
                    {
                        pathDraw = true;

                        count =0;
                        //first delete the old images
                        if (createdArray != null) {
                            while (count < createdArray.length) {
                                ImageView pathView = (ImageView) findViewById(createdArray[count]);

                                deleteView.removeView(pathView);
                                count++;
                            }
                            ImageView destImage = (ImageView) findViewById(R.id.FinalDestinationImage);
                            destImage.setVisibility(View.INVISIBLE);
                            createdArray = null;
                            pathArray = null;
                        }

                        amountOfPaths = 0;

                        //get the number of spots till destination
                        if(globalX == xPositionPath)
                        {
                            amountOfPaths += getYDistance(globalY, yPositionPath);
                        }
                        else
                        {
                            amountOfPaths += getYDistance(globalY, 26);
                            amountOfPaths += getYDistance(yPositionPath, 26);
                            amountOfPaths += getXDistance(globalX, xPositionPath);
                        }
                        globalNumberOfPaths = amountOfPaths;
                        pathArray = new int[amountOfPaths];
                        drawOnX = new ArrayList<Float>(amountOfPaths);
                        drawOnY =new ArrayList<Integer>(amountOfPaths);
                        count = 0;

                        SendDataTesting.this.runOnUiThread(new Runnable() {

                            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                            public void run()
                            {
                                int count = 0;

                                //create new path way images for the user
                                while (count < pathArray.length)
                                {
                                    ImageView pathView = new ImageView(SendDataTesting.this);
                                    pathView.setImageResource(R.drawable.path);
                                    RelativeLayout rl = (RelativeLayout) findViewById(R.id.Background);

                                    pathView.setId(View.generateViewId());
                                    pathArray[count] = pathView.getId();
                                    rl.addView(pathView);
                                    count++;
                                }
                                createdArray = pathArray;
                                letUserClickPath = false;
                            }
                        });
                    }
                    /////////////////////////////////////// ///////////////////////////////////////
                    numberOfClicks =0;
                }
                numberOfClicks++;

                count = 0;
                if(pathDraw ==false && createdArray != null && pathArray != null)
                {
                    while (count < createdArray.length) {
                        ImageView pathView = (ImageView) findViewById(createdArray[count]);
                        deleteView.removeView(pathView);
                        count++;
                    }
                    ImageView destImage = (ImageView) findViewById(R.id.FinalDestinationImage);
                    destImage.setVisibility(View.INVISIBLE);
                    globalNumberOfPaths =0;
                    createdArray = null;
                    pathArray = null;
                    xPositionPath = 0;
                    yPositionPath = 0;

                    letUserClickPath = false;
                    pathDraw = false;
                    Button button = (Button)findViewById(R.id.letPathFind);
                    button.setText("Find Path");
                }

                count =0;
                if(globalNumberOfPaths <= 2 && createdArray != null && pathArray != null )
                {
                    while (count < createdArray.length) {
                        ImageView pathView = (ImageView) findViewById(createdArray[count]);
                        deleteView.removeView(pathView);
                        count++;
                    }
                    ImageView destImage = (ImageView) findViewById(R.id.FinalDestinationImage);
                    destImage.setVisibility(View.INVISIBLE);
                    globalNumberOfPaths =0;
                    createdArray = null;
                    pathArray = null;
                    xPositionPath = 0;
                    yPositionPath = 0;

                    letUserClickPath = false;
                    pathDraw = false;
                    Button button = (Button)findViewById(R.id.letPathFind);
                    button.setText("Find Path");
                    Toast.makeText(SendDataTesting.this, "You Have Reached Your Destination", Toast.LENGTH_SHORT).show();
                }
                count = 0;
                if (pathArray != null && globalNumberOfPaths !=0)
                {

                    if(globalX == xPositionPath)
                    {
                        setPathY(globalY, yPositionPath,globalX);
                    }
                    else
                    {
                        setPathY(globalY, 26, globalX);
                        setPathY(yPositionPath, 26, xPositionPath);
                        setPathX(globalX, xPositionPath);
                        // getXDistance(globalPersonX, xPositionPath);
                    }
                    while (count < pathArray.length) {
                        int x = 0;
                        int y = 0;
                        ImageView map = (ImageView) findViewById(R.id.Map);
                        Xratio = (map.getRight() - map.getLeft()) / roomYLength;
                        Yratio = (map.getBottom() - map.getTop()) / roomXLength;
                        ImageView pathView = (ImageView) findViewById(pathArray[count]);

                        //x and y the user has entered converted to integer
                        String tmpX = drawOnX.get(count).toString();
                        y = drawOnY.get(count);
                        x = 0;

                        x = NumberMatch(tmpX);
//Draw the image to final spot
                        float compareX = Float.parseFloat(tmpX);
                        compareX = findNearestNumber(possibleXValues, compareX);

                        ImageView destImage = (ImageView) findViewById(R.id.FinalDestinationImage);
                        destImage.setVisibility(View.INVISIBLE);
                        if(numberOfClicks >= 1)
                        {
                            if(y == yPositionPath && compareX == xPositionPath)
                            {
                                pathView.setImageResource(R.drawable.pathdest);
                                destImage.setVisibility(View.INVISIBLE);
                                drawSecondOption = false;
                                numberOfClicks =0;
                            }
                            else
                            {
                                drawSecondOption = true;

                            }
                        }
                        //do the math to get ratio to put on map
                        x = x*Yratio;
                        y = y*Xratio;

                        //location of the person that is currently.
                        int pathX = pathView.getLeft();
                        int pathY = pathView.getTop();
                        int pathXS = pathView.getRight();
                        int pathYS = pathView.getBottom();

                        int middleX = pathView.getLeft() + ((pathView.getRight() - pathView.getLeft()) / 2);
                        int radX = ((pathView.getRight() - pathView.getLeft()) / 2);
                        int middleY = pathView.getTop() + ((pathView.getBottom() - pathView.getTop()) / 2);
                        int radY = ((pathView.getBottom() - pathView.getTop()) / 2);

                        middleX = y;
                        middleY = x;

                        pathX = middleX - radX + map.getLeft();
                        pathXS = middleX + radX + map.getLeft();
                        pathY = middleY - radY + map.getTop();
                        pathYS = middleY + radY + map.getTop();

                        pathView.setX(pathX);
                        pathView.setY(pathY);
                        count++;
                    }
                    if(drawSecondOption)
                    {
                        ImageView destImage = (ImageView) findViewById(R.id.FinalDestinationImage);
                        ImageView map = (ImageView) findViewById(R.id.Map);
                        Xratio = (map.getRight() - map.getLeft()) / roomYLength;
                        Yratio = (map.getBottom() - map.getTop()) / roomXLength;

                        destImage.setVisibility(View.VISIBLE);
                                   /* x = 12;
                                    y = 1+(count*1);*/
                        //x and y the user has entered converted to integer
                        String tmpX = String.valueOf(xPositionPath);
                        int y = yPositionPath;
                        int x = NumberMatch(tmpX);



                        //do the math to get ratio to put on map
                        x = x*Yratio;
                        y = y*Xratio;

                        //location of the person that is currently.
                        int pathX = destImage.getLeft();
                        int pathY = destImage.getTop();

                        int middleX = destImage.getLeft() + ((destImage.getRight() - destImage.getLeft()) / 2);
                        int radX = ((destImage.getRight() - destImage.getLeft()) / 2);
                        int middleY = destImage.getTop() + ((destImage.getBottom() - destImage.getTop()) / 2);
                        int radY = ((destImage.getBottom() - destImage.getTop()) / 2);


                        middleX = y;
                        middleY = x;

                        pathX = middleX - radX + map.getLeft();
                        pathY = middleY - radY + map.getTop();

                        destImage.setX(pathX);
                        destImage.setY(pathY);
                        drawSecondOption = false;
                    }

                    int numberofchilds = deleteView.getChildCount();
                    Log.i("MainActivity", "count = " + numberofchilds);
                }
            }
        });
    }


    Button PathButton;
    public void PathButtonOnOff(View v) {

        Button button = (Button) v;
        PathButton = button;
        SendDataTesting.this.runOnUiThread(new Runnable() {

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            public void run() {

                if(PathButton.getText().toString() != "Delete Path" )
                {
                    letUserClickPath = true;
                    PathButton.setText("Delete Path");
                }
                else
                {
                    letUserClickPath = false;
                    pathDraw = false;
                    PathButton.setText("Find Path");
                }
                drawPerson();
            }
        });

    }
    public void setPathY(int d1, int d2, float xP)
    {
        int count = 0;
        while (d1 != d2)
        {
            if (d1 > d2)
            {

                drawOnX.add(xP);
                drawOnY.add(d2);
                count++;
                d2++;
            }
            else if( d1 < d2)
            {
                count++;
                d1++;
                drawOnX.add(xP);
                drawOnY.add(d1);
            }
        }

    }
    public void setPathX(float person, float destination)
    {
        int feets = 0;
        if (person > destination)
        {
            while (destination < person)
            {
                if (Float.compare(destination,1.9f) == 0)
                {
                    feets++;
                    if(Float.compare(person,1.95f) == 0)
                    {
                        drawOnX.add(person);
                        drawOnY.add(26);
                        break;
                    }
                }
                if(Float.compare(destination,1.9f) == 0 && Float.compare(destination + 0.1f,2.0f) ==0)
                {
                    drawOnX.add(1.95f);
                    drawOnY.add(26);
                }
                destination += 0.1f;
                feets++;
                destination = (int)(destination * 100);
                destination = (float)destination / (float)100d;
                destination = findNearestNumber(possibleXValues,(float)destination);
                drawOnX.add(destination);
                drawOnY.add(26);
            }
        }
        else if (destination > person)
        {
            while (person < destination)
            {
                if (Float.compare(person,1.9f) == 0)
                {
                    feets++;
                    if (1.95 == destination)
                    {
                        drawOnX.add(1.95f);
                        drawOnY.add(26);
                        break;
                    }
                }
                if(Float.compare(person,1.9f) == 0 && Float.compare(person + 0.1f,2.0f) ==0)
                {
                    drawOnX.add(1.95f);
                    drawOnY.add(26);
                }
                person += 0.1f;
                feets++;
                person = (int)(person * 100);
                person = (float)person / (float)100d;
                person = findNearestNumber(possibleXValues,(float)person);
                drawOnX.add(person);
                drawOnY.add(26);
            }
        }
        //  return feets;
    }

    public float findNearestNumber(float[] array,float myNumber)
    {

        float min=0,max=0,nearestNumber;

        for(int i=0;i<array.length;i++)
        {
            if(array[i]<myNumber)
            {
                if(min==0)
                {
                    min=array[i];
                }
                else if(array[i]>min)
                {
                    min=array[i];
                }
            }
            else if(array[i]>myNumber)
            {
                if(max==0)
                {
                    max=array[i];
                }
                else if(array[i]<max)
                {
                    max=array[i];
                }
            }
            else
            {
                return array[i];
            }
        }
        if(Math.abs(myNumber-min)<Math.abs(myNumber-max))
        {
            nearestNumber=min;
        }
        else
        {
            nearestNumber=max;
        }
        return nearestNumber;
    }



    private class MapTask implements Runnable {
        @Override
        public void run() {

            SendDataTesting.this.runOnUiThread(new Runnable() {

                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                public void run() {

                    drawPerson();

                    //draw path accoriding to user's current position : gloabal x and globaly
                    DrawPath();
                }
            });
        }
    }


    void drawPerson(){
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
                globalX = findNearestNumber(possibleXValues, globalX);
                String tmpX = String.valueOf(globalX);
                int y = globalY;// this is the number that comes from the data
                int x = 0;
                x = NumberMatch(tmpX);

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
                person.setX(personX);
                person.setY(personY);

            }
        });







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

                        List<EuclideanDistance> Euc_Distance_List = new ArrayList<>();

                        //make a copy of RT mean of routers for this thread
                        double threadMeanOfRouter7D28 = meanOfRouter7D28;
                        double threadMeanOfRouter7D8C = meanOfRouter7D8C;
                        double threadMeanOfRouter95A8 = meanOfRouter95A8;

                        @Override
                        public void run() {
                            //compare RT mean with the off-line mean data and update the position on the map
                            for (DataPoint dp : dataPointsThread) {
                                //do linear search and range checks
                                for (float rangeIteration = k_ZERO; rangeIteration <= k_THREE; rangeIteration += k_ITERATION) {
                                    if (rangeCheck(dp.dlink7D28, threadMeanOfRouter7D28, rangeIteration) && rangeCheck(dp.dlink7D8C, threadMeanOfRouter7D8C, rangeIteration)
                                            && rangeCheck(dp.dlink95A8, threadMeanOfRouter95A8, rangeIteration)) {

/////new code

                                        //calculating least distance
                                        double distX=0.0;
                                        double distY=0.0;
                                        double EuclDistance = 0.0;
                                        LeastDistance ldist = new LeastDistance();
                                        EuclideanDistance eucD = new EuclideanDistance();

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
                                        //offline x and y
                                        ldist.x = dp.xPos;
                                        ldist.y = dp.yPos;
                                        ldist.dlink7D28 = dp.dlink7D28;
                                        ldist.dlink7D8C = dp.dlink7D8C;
                                        ldist.dlink95A8 = dp.dlink95A8;
                                        EuclDistance = calculateEuclideanDistance(threadMeanOfRouter7D28,threadMeanOfRouter7D8C,threadMeanOfRouter95A8,dp.dlink7D28,dp.dlink7D8C,dp.dlink95A8);
                                        eucD.distance = EuclDistance;
                                        eucD.x = dp.xPos;
                                        eucD.y = dp.yPos;

                                        //current x and y
                                        ldist.globalX = globalX;
                                        ldist.globalY = globalY;
                                        Euc_Distance_List.add(eucD);
                                        Least_Distance.add(ldist);

                                        break;
                                    }
                                }
                            }

                            //checking the size of the LeastDistance list
                            if (Least_Distance.size() > 0) {

                                //sort the euclidean list and ge the 3 least distances and get their mean, compare it with offline and get the position
                                Collections.sort(Least_Distance, new Comparator<LeastDistance>() {
                                    @Override
                                    public int compare(LeastDistance lhs, LeastDistance rhs) {
                                        return lhs.distance < rhs.distance ? -1
                                                : lhs.distance > rhs.distance ? 1
                                                : 0;
                                    }
                                });

                                for ( LeastDistance ld:Least_Distance) {
                                    Log.i(" DP : " , "Dist :  " + String.valueOf(ld.distance) + "X : " + String.valueOf(ld.x) + " Y : " + String.valueOf(ld.y));
                                }

                                if(Least_Distance.size()>=2)
                                {
                                    //if both nearest distances are less than 15
                                    if (Least_Distance.get(0).distance < leastDistanceRange && Least_Distance.get(1).distance < leastDistanceRange)
                                    {

                                        //calculate the euclidean distance for 2 least distance points
                                        double ed0 = 0.0;
                                        double ed1 = 0.0;
                                        ed0 = calculateEuclideanDistance(threadMeanOfRouter7D28, threadMeanOfRouter7D8C, threadMeanOfRouter95A8, Least_Distance.get(0).dlink7D28, Least_Distance.get(0).dlink7D8C, Least_Distance.get(0).dlink95A8);
                                        ;
                                        ed1 = calculateEuclideanDistance(threadMeanOfRouter7D28, threadMeanOfRouter7D8C, threadMeanOfRouter95A8, Least_Distance.get(1).dlink7D28, Least_Distance.get(1).dlink7D8C, Least_Distance.get(1).dlink95A8);
                                        ;
                                        if (ed0 >= ed1) {
                                            globalX = Least_Distance.get(0).x;
                                            globalY = Least_Distance.get(0).y;
                                        } else {
                                            globalX = Least_Distance.get(1).x;
                                            globalY = Least_Distance.get(1).y;
                                        }
                                        leastDistanceRange = 10.0;
                                        countOutOfRangePositionCalculated=0;
                                    }
                                    else
                                    {
                                        ///////////try with euclidean distance

                                        countOutOfRangePositionCalculated++;

                                        if(countOutOfRangePositionCalculated>=2)
                                        {
                                            countOutOfRangePositionCalculated=0;
                                            leastDistanceRange+=5;
                                        }
                                    }
                                }
                                else {
                                    //list is less than 2 so, show the nearest position
                                    if (Least_Distance.get(0).distance < leastDistanceRange) {
                                        globalX = Least_Distance.get(0).x;
                                        globalY = Least_Distance.get(0).y;
                                    }
                                }
                            }
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
                dp = findNearestNumber(possibleXValues,(float)dp);
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
                global = findNearestNumber(possibleXValues,(float)global);
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

            //router A8
            Collections.sort(reatTime95A8ScanValues);
            medianOfRouter95A8 = reatTime95A8ScanValues.get(medianIndex);

            //router 28
            Collections.sort(reatTime7D28ScanValues);
            medianOfRouter7D28 = reatTime7D28ScanValues.get(medianIndex);

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

                    }
                }
                //-53 < -50
                else if (ass <= median) {
                    //
                    if ((median - ass <= 6)) {
                        //didnt pass the range check, delete this from the array
                        list.add(ass);

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

                        reatTime7D8CScanValues.add(wifiScanList.get(i).level);
                    } else if (ssid.equals(Router95A8)) {

                        reatTime95A8ScanValues.add(wifiScanList.get(i).level );
                    } else if ((ssid.equals(Router7D28))) {

                        reatTime7D28ScanValues.add(wifiScanList.get(i).level );
                    }
                }
                calculationsForDataPointControl = true;

            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "Error Occurred...", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private class MeanTask extends AsyncTask<Void,Void,String> {
        @Override
        protected String doInBackground(Void... params) {

            try {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Final0");
                query.setLimit(544);
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> scoreList, ParseException e) {
                        if (e == null) {
                            for (ParseObject po : scoreList) {
                                String xPos = po.getString("xPos");
                                String yPos = po.getString("yPos");
                                String direction = po.getString("direction");
                                String dlink7d28 = po.getString("dlink7D28");
                                String dlink95A8 = po.getString("dlink95A8");
                                String dlink7d8c = po.getString("dlink7D8C");


                                DataPoint dataPoint = new DataPoint();

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
            Toast.makeText(getBaseContext(), "Give us your position by clicking on map...", Toast.LENGTH_SHORT).show();
        }
    }
}

