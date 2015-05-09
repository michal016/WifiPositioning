package pl.maciejbartoszek.wifipositioning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ToggleButton;

public class MainActivityWifi extends Activity implements OnClickListener{

    public WifiManager myWifiManager;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    ToggleButton wifiToggleButton, startLocalize;
    Button positionSaveButton, startPositioning, showMapButton;
    TextView wifiStatus;
    EditText xCoordinateTextField, yCoordinateTextField, distanceAlgorithm;
    TextView macAddressRouter1, macAddressRouter2, macAddressRouter3;
    TextView signalStrengthRouter1, signalStrengthRouter2, signalStrengthRouter3;
    TextView label, positionText, compassText;
    ProgressBar wifiSwirl;
    ArrayList<String[]> positionDataBase;
    File databaseFile, imageFile, resultsFile, configFile;
    ImageView mapImage;
    View touchView;
    Sensor compass;
    SensorManager sensorManager;
    String pathToImage;
    Bitmap bMap;
    Bitmap previousMap, currentMap;
    ArrayList<int[]> pixelsList;
    static String mapText, mac1Text, mac2Text, mac3Text;
    static int minCoord = 20;
    static int maxCoord = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainlayout);

        myWifiManager = (WifiManager)getBaseContext().getSystemService(Context.WIFI_SERVICE);
        wifiToggleButton = (ToggleButton)findViewById(R.id.toggleButtonWiFi);
        wifiStatus = (TextView)findViewById(R.id.Status);
        positionSaveButton = (Button)findViewById(R.id.ButtonSave);
        xCoordinateTextField = (EditText)findViewById(R.id.xPositionText);
        yCoordinateTextField = (EditText)findViewById(R.id.yPositionText);
        distanceAlgorithm = (EditText)findViewById(R.id.distanceNumber);
        wifiSwirl = (ProgressBar)findViewById(R.id.Swirl);
        macAddressRouter1 = (TextView)findViewById(R.id.MACaddress1);
        macAddressRouter2 = (TextView)findViewById(R.id.MACaddress2);
        macAddressRouter3 = (TextView)findViewById(R.id.MACaddress3);
        signalStrengthRouter1 = (TextView)findViewById(R.id.RSSI1);
        signalStrengthRouter2 = (TextView)findViewById(R.id.RSSI2);
        signalStrengthRouter3 = (TextView)findViewById(R.id.RSSI3);
        label = (TextView)findViewById(R.id.textViewLabel);
        compassText = (TextView)findViewById(R.id.compassText);
        positionText = (TextView)findViewById(R.id.positionText);
        startPositioning = (Button)findViewById(R.id.buttonStart);
        showMapButton = (Button)findViewById(R.id.buttonShowMap);
        positionDataBase = new ArrayList<String[]>();
        databaseFile = new File(getApplicationContext().getExternalFilesDir(null), "databaseFile.txt");
        resultsFile = new File(getApplicationContext().getExternalFilesDir(null), "resultsFile.xls");
        configFile = new File(getApplicationContext().getExternalFilesDir(null), "configFile.txt");
        ReadConfigFile(configFile);
        pathToImage = getApplicationContext().getExternalFilesDir(null) + "/" + mapText + ".png";
        bMap = BitmapFactory.decodeFile(pathToImage);
        previousMap = bMap.copy(Config.ARGB_8888, true);
        currentMap = bMap.copy(Config.ARGB_8888, true);
        pixelsList = new ArrayList<int[]>();
        wifiList = null;

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        compass = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                compassText.setText(CalculateCompass(event.values[0]));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // TODO Auto-generated method stub

            }
        }, compass, SensorManager.SENSOR_DELAY_NORMAL);

        receiverWifi = new WifiReceiver();
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        if (myWifiManager.isWifiEnabled()){
            wifiToggleButton.setChecked(true);
        }
        else{
            wifiToggleButton.setChecked(false);
        }
        wifiSwirl.setVisibility(ProgressBar.INVISIBLE);
        positionText.setText("Unknown");

        wifiToggleButton.setOnClickListener(this);

        showMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.maplayout);

                signalStrengthRouter1 = (TextView)findViewById(R.id.signal1Text);
                signalStrengthRouter2 = (TextView)findViewById(R.id.signal2Text);
                signalStrengthRouter3 = (TextView)findViewById(R.id.signal3Text);
                positionSaveButton = (Button)findViewById(R.id.saveButton);
                startLocalize = (ToggleButton)findViewById(R.id.localizeButton);
                touchView = findViewById(R.id.touchView);
                xCoordinateTextField = (EditText)findViewById(R.id.xLabel);
                yCoordinateTextField = (EditText)findViewById(R.id.yLabel);
                distanceAlgorithm = (EditText)findViewById(R.id.DistanceAlgorithm);
                mapImage = (ImageView)findViewById(R.id.imageViewMap);
                mapImage.setImageBitmap(bMap);

                xCoordinateTextField.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        positionSaveButton.setEnabled(true);

                    }
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {
                        // TODO Auto-generated method stub

                    }
                    @Override
                    public void afterTextChanged(Editable s) {
                        // TODO Auto-generated method stub

                    }
                });

                startLocalize.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (startLocalize.isChecked()) {
                            signalStrengthRouter1.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    // TODO Auto-generated method stub
                                    if (!s.toString().equals(0) && startLocalize.isChecked()) {
                                        //setLocationOnMap();
                                        mapImage.setImageBitmap(currentMap);
                                    }
                                }

                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count,
                                                              int after) {
                                    // TODO Auto-generated method stub

                                }

                                @Override
                                public void afterTextChanged(Editable s) {
                                    // TODO Auto-generated method stub

                                }
                            });
                        }
                        else {
                            mapImage.setImageBitmap(bMap);
                            previousMap = bMap;
                            pixelsList.clear();
                        }
                    }
                });

                positionSaveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (startLocalize.isChecked()) {
                            ArrayList<double[]> positionList = positioningAlgorithm(Integer.parseInt(String.valueOf(distanceAlgorithm.getText())));
                            int pixelX = (int)positionList.get(0)[1];
                            int pixelY = (int)positionList.get(0)[2];

                            if (!String.valueOf(xCoordinateTextField.getText()).equals("") && !String.valueOf(yCoordinateTextField.getText()).equals("")) {
                                saveResults(pixelX, pixelY, Integer.parseInt(String.valueOf(xCoordinateTextField.getText())), Integer.parseInt(String.valueOf(yCoordinateTextField.getText())));
                            }
                        }
                        else {
                            savePosition(String.valueOf(xCoordinateTextField.getText()), String.valueOf(yCoordinateTextField.getText()));
                        }
                    }
                });

                touchView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        int action = event.getActionMasked();

                        switch(action) {
                            case MotionEvent.ACTION_DOWN:
                                xCoordinateTextField.setText(String.valueOf((int)event.getX()));
                                yCoordinateTextField.setText(String.valueOf((int)event.getY()));
                                break;
                            case MotionEvent.ACTION_MOVE:
                                int xCoord = (int)event.getX();
                                int yCoord = (int)event.getY();
                                if (xCoord >= minCoord && xCoord <= maxCoord && yCoord >= minCoord && yCoord <= maxCoord) {
                                    xCoordinateTextField.setText(String.valueOf(xCoord));
                                    yCoordinateTextField.setText(String.valueOf(yCoord));
                                }
                                else if (xCoord < minCoord && yCoord < minCoord) {
                                    xCoordinateTextField.setText(String.valueOf(minCoord));
                                    yCoordinateTextField.setText(String.valueOf(minCoord));
                                }
                                else if (xCoord < minCoord && yCoord > maxCoord) {
                                    xCoordinateTextField.setText(String.valueOf(minCoord));
                                    yCoordinateTextField.setText(String.valueOf(maxCoord));
                                }
                                else if (xCoord > maxCoord && yCoord < minCoord) {
                                    xCoordinateTextField.setText(String.valueOf(maxCoord));
                                    yCoordinateTextField.setText(String.valueOf(minCoord));
                                }
                                else if (xCoord > maxCoord && yCoord > maxCoord) {
                                    xCoordinateTextField.setText(String.valueOf(maxCoord));
                                    yCoordinateTextField.setText(String.valueOf(maxCoord));
                                }
                                else if (xCoord < minCoord) {
                                    xCoordinateTextField.setText(String.valueOf(minCoord));
                                    yCoordinateTextField.setText(String.valueOf(yCoord));
                                }
                                else if (xCoord > maxCoord) {
                                    xCoordinateTextField.setText(String.valueOf(maxCoord));
                                    yCoordinateTextField.setText(String.valueOf(yCoord));
                                }
                                else if (yCoord < minCoord) {
                                    xCoordinateTextField.setText(String.valueOf(xCoord));
                                    yCoordinateTextField.setText(String.valueOf(minCoord));
                                }
                                else if (yCoord > maxCoord) {
                                    xCoordinateTextField.setText(String.valueOf(xCoord));
                                    yCoordinateTextField.setText(String.valueOf(maxCoord));
                                }
                                break;
                            case MotionEvent.ACTION_UP:
                                int X = Integer.parseInt(String.valueOf(xCoordinateTextField.getText()));
                                int Y = Integer.parseInt(String.valueOf(yCoordinateTextField.getText()));

                                int moduloX = X % 20;
                                if (moduloX != 0) {
                                    if (moduloX < 10) {
                                        X -= moduloX;
                                    }
                                    else {
                                        X += 20 - moduloX;
                                    }
                                }

                                int moduloY = Y % 20;
                                if (moduloY != 0) {
                                    if (moduloY < 10) {
                                        Y -= moduloY;
                                    }
                                    else {
                                        Y += 20 - moduloY;
                                    }
                                }

                                xCoordinateTextField.setText(String.valueOf(X));
                                yCoordinateTextField.setText(String.valueOf(Y));
                                break;
                        }
                        return true;
                    }
                });
            }
        });

        positionSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int X = Integer.parseInt(String.valueOf(xCoordinateTextField.getText()));
                int Y = Integer.parseInt(String.valueOf(yCoordinateTextField.getText()));

                int moduloX = X % 20;
                if (moduloX != 0) {
                    if (moduloX < 10) {
                        X -= moduloX;
                    }
                    else {
                        X += 20 - moduloX;
                    }
                }

                int moduloY = Y % 20;
                if (moduloY != 0) {
                    if (moduloY < 10) {
                        Y -= moduloY;
                    }
                    else {
                        Y += 20 - moduloY;
                    }
                }
                String xValue = (Integer.parseInt(String.valueOf(xCoordinateTextField.getText())) < minCoord) ? String.valueOf(minCoord) :
                        (Integer.parseInt(String.valueOf(xCoordinateTextField.getText())) > maxCoord) ? String.valueOf(maxCoord) : null;
                String yValue = (Integer.parseInt(String.valueOf(yCoordinateTextField.getText())) < minCoord) ? String.valueOf(minCoord) :
                        (Integer.parseInt(String.valueOf(yCoordinateTextField.getText())) > maxCoord) ? String.valueOf(maxCoord) : null;

                if (xValue == null) {
                    xCoordinateTextField.setText(String.valueOf(X));
                }
                else {
                    xCoordinateTextField.setText(String.valueOf(xValue));
                }

                if (yValue == null) {
                    yCoordinateTextField.setText(String.valueOf(Y));
                }
                else {
                    yCoordinateTextField.setText(String.valueOf(yValue));
                }
                if (String.valueOf(xCoordinateTextField.getText()) != null && String.valueOf(yCoordinateTextField.getText()) != null) {
                    savePosition(String.valueOf(xCoordinateTextField.getText()), String.valueOf(yCoordinateTextField.getText()));
                }
            }
        });

        startPositioning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<double[]> positionList = positioningAlgorithm(Integer.parseInt(String.valueOf(distanceAlgorithm.getText())));
                //for (int i = 0; i < positionList.size(); i++) {
                //if (positionList.get(i)[3] == String.valueOf(compassText.getText()))
                positionText.setText("X: " + positionList.get(0)[1] + "; Y: " + positionList.get(0)[2]);
                saveResults((int)positionList.get(0)[1], (int)positionList.get(0)[2], Integer.parseInt(String.valueOf(xCoordinateTextField.getText())), Integer.parseInt(String.valueOf(yCoordinateTextField.getText())));
                //}
            }
        });
        this.registerReceiver(this.wifiStateChanged, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        startRegularScan();
    }

    public void startRegularScan()
    {
        startScanTimer();
    }

    public void startScanTimer()
    {
        new CountDownTimer(3600000,2000)
        {
            @Override
            public void onTick(long millisUntilFinished)
            {
                myWifiManager.startScan();
            }

            @Override
            public void onFinish()
            {
                startRegularScan();
            }

        }.start();
    }

    @Override
    public void onClick(View view) {
        if (wifiToggleButton.isChecked()){
            EnableWifi(true);
        }
        else
            EnableWifi(false);
    }

    private BroadcastReceiver wifiStateChanged = new BroadcastReceiver(){
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            int extraWifiState = arg1.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);

            switch (extraWifiState){
                case WifiManager.WIFI_STATE_DISABLED:
                    wifiStatus.setText("WiFi is disabled");
                    wifiToggleButton.setChecked(false);
                    positionSaveButton.setEnabled(false);
                    wifiSwirl.setVisibility(ProgressBar.INVISIBLE);
                    macAddressRouter1.setText(null);
                    signalStrengthRouter1.setText(null);
                    macAddressRouter2.setText(null);
                    signalStrengthRouter2.setText(null);
                    macAddressRouter3.setText(null);
                    signalStrengthRouter3.setText(null);
                    xCoordinateTextField.setEnabled(false);
                    yCoordinateTextField.setEnabled(false);
                    startPositioning.setEnabled(false);
                    showMapButton.setEnabled(false);
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    wifiStatus.setText("WiFi is disabling");
                    wifiToggleButton.setChecked(false);
                    positionSaveButton.setEnabled(false);
                    wifiSwirl.setVisibility(ProgressBar.VISIBLE);
                    xCoordinateTextField.setEnabled(false);
                    yCoordinateTextField.setEnabled(false);
                    startPositioning.setEnabled(false);
                    showMapButton.setEnabled(false);
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    wifiStatus.setText("WiFi is enabled");
                    wifiToggleButton.setChecked(true);
                    wifiSwirl.setVisibility(ProgressBar.INVISIBLE);
                    myWifiManager.startScan();
                    xCoordinateTextField.setEnabled(true);
                    yCoordinateTextField.setEnabled(true);
                    positionSaveButton.setEnabled(true);
                    startPositioning.setEnabled(true);
                    showMapButton.setEnabled(true);
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    wifiStatus.setText("WiFi is enabling");
                    wifiToggleButton.setChecked(true);
                    positionSaveButton.setEnabled(false);
                    wifiSwirl.setVisibility(ProgressBar.VISIBLE);
                    xCoordinateTextField.setEnabled(false);
                    yCoordinateTextField.setEnabled(false);
                    startPositioning.setEnabled(false);
                    showMapButton.setEnabled(false);
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN:
                    wifiStatus.setText("WiFi is in unknown state");
                    wifiToggleButton.setChecked(false);
                    positionSaveButton.setEnabled(false);
                    wifiSwirl.setVisibility(ProgressBar.INVISIBLE);
                    macAddressRouter1.setText(null);
                    signalStrengthRouter1.setText(null);
                    macAddressRouter2.setText(null);
                    signalStrengthRouter2.setText(null);
                    macAddressRouter3.setText(null);
                    signalStrengthRouter3.setText(null);
                    xCoordinateTextField.setEnabled(false);
                    yCoordinateTextField.setEnabled(false);
                    startPositioning.setEnabled(false);
                    showMapButton.setEnabled(false);
                    break;
            }
        }
    };

    private void EnableWifi(boolean state){
        myWifiManager.setWifiEnabled(state);
    }

    public String CalculateCompass(float azimuth) {
        String compassTxt = null;

        compassTxt = (azimuth >= 45 && azimuth < 135) ? "EAST" :
                (azimuth >= 135 && azimuth < 225) ? "SOUTH" :
                        (azimuth >= 225 && azimuth < 315) ? "WEST" : "NORTH";

        return compassTxt;
    }

    public Bitmap setLocationOnMap() {
        setPreviousLocation();
        pixelsList.clear();
        ArrayList<double[]> positionList = positioningAlgorithm(Integer.parseInt(String.valueOf(distanceAlgorithm.getText())));
        int pixelX = (int)positionList.get(0)[1];
        int pixelY = (int)positionList.get(0)[2];

        Bitmap newMap = previousMap.copy(Config.ARGB_8888, true);
        pixelsList.add(new int[] {pixelX, pixelY});
        pixelsList.add(new int[] {pixelX - 1, pixelY - 1});
        pixelsList.add(new int[] {pixelX - 1, pixelY + 1});
        pixelsList.add(new int[] {pixelX + 1, pixelY - 1});
        pixelsList.add(new int[] {pixelX + 1, pixelY + 1});
        for (int i = 1; i < 6; i++) {
            pixelsList.add(new int[] {pixelX, pixelY - i});
            pixelsList.add(new int[] {pixelX, pixelY + i});
        }
        for (int i = 1; i < 6; i++) {
            pixelsList.add(new int[] {pixelX - i, pixelY});
            pixelsList.add(new int[] {pixelX + i, pixelY});
        }
        for (int i = 0; i < pixelsList.size(); i++) {
            newMap.setPixel(pixelsList.get(i)[0], pixelsList.get(i)[1], Color.RED);
        }

        return newMap;
    }

    public void setPreviousLocation() {
        Bitmap newMap = previousMap.copy(Config.ARGB_8888, true);

        for (int i = 0; i < pixelsList.size(); i++) {
            newMap.setPixel(pixelsList.get(i)[0], pixelsList.get(i)[1], Color.BLUE);
        }
        previousMap = newMap;
    }

    public void saveResults(int xResult, int yResult, int xActual, int yActual) {
        int xDistance = Math.abs(xActual - xResult);
        int yDistance = Math.abs(yActual - yResult);
        FileWriter fw = null;
        try {
            if (isExternalStorageWritable()) {
                fw = new FileWriter(resultsFile, true);
                if (xDistance != 0 && yDistance != 0) {
                    double distance = Math.sqrt(Math.pow(xDistance, 2) + Math.pow(yDistance, 2));
                    fw.write(distance / 20 + "\t" + xActual + "\t" + yActual + "\t" + xResult + "\t" + yResult + "\n");
                }
                else if (yDistance == 0) {
                    fw.write((double)xDistance / 20 + "\t" + xActual + "\t" + yActual + "\t" + xResult + "\t" + yResult + "\n");
                }
                else {
                    fw.write((double)yDistance / 20 + "\t" + xActual + "\t" + yActual + "\t" + xResult + "\t" + yResult + "\n");
                }
                fw.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void savePosition(String xCoord, String yCoord) {
        FileWriter fw = null;
        try {
            if (isExternalStorageWritable()) {
                fw = new FileWriter(databaseFile, true);
                fw.write(xCoord + "," + yCoord + "," + String.valueOf(signalStrengthRouter1.getText()) + "," + String.valueOf(compassText.getText()) + ";");
                fw.write(xCoord + "," + yCoord + "," + String.valueOf(signalStrengthRouter2.getText()) + "," + String.valueOf(compassText.getText()) + ";");
                fw.write(xCoord + "," + yCoord + "," + String.valueOf(signalStrengthRouter3.getText()) + "," + String.valueOf(compassText.getText()) + "\n");
                fw.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void setRouterData(String macAddress, int router){
        ScanResult routerData = null;

        if (!wifiList.isEmpty()){
            for (int i = 0; i < wifiList.size(); i++){
                if (wifiList.get(i).BSSID.equals(macAddress)){
                    routerData = wifiList.get(i);
                    break;
                }
            }
        }

        if (routerData != null){
            switch (router){
                case 1:
                    macAddressRouter1.setText(routerData.BSSID);
                    signalStrengthRouter1.setText(Integer.toString(routerData.level));
                    break;
                case 2:
                    macAddressRouter2.setText(routerData.BSSID);
                    signalStrengthRouter2.setText(Integer.toString(routerData.level));
                    break;
                case 3:
                    macAddressRouter3.setText(routerData.BSSID);
                    signalStrengthRouter3.setText(Integer.toString(routerData.level));
                    break;
            }
        }
        else{
            switch (router){
                case 1:
                    macAddressRouter1.setText("Error");
                    signalStrengthRouter1.setText(Integer.toString(0));
                    break;
                case 2:
                    macAddressRouter2.setText("Error");
                    signalStrengthRouter2.setText(Integer.toString(0));
                    break;
                case 3:
                    macAddressRouter3.setText("Error");
                    signalStrengthRouter3.setText(Integer.toString(0));
                    break;
            }
        }
    }

    public ArrayList<double[]> positioningAlgorithm(int distanceValue) {
        FileReader fr = null;
        String line = "";
        try {
            fr = new FileReader(databaseFile);
        } catch (FileNotFoundException e) {
            System.out.println("File open error!");
            System.exit(1);
        }

        BufferedReader bfr = new BufferedReader(fr);
        try {
            while((line = bfr.readLine()) != null){
                String[] lineValues = line.split(";");
                String[] router1Values = lineValues[0].split(",");
                positionDataBase.add(router1Values);
                String[] router2Values = lineValues[1].split(",");
                positionDataBase.add(router2Values);
                String[] router3Values = lineValues[2].split(",");
                positionDataBase.add(router3Values);
            }
        } catch (IOException e) {
            System.out.println("File read error!");
            System.exit(2);
        }

        try {
            fr.close();
        } catch (IOException e) {
            System.out.println("File close error!");
            System.exit(3);
        }

        //optimizeDataBase();

        int rssi1 = Integer.parseInt(signalStrengthRouter1.getText().toString());
        int rssi2 = Integer.parseInt(signalStrengthRouter2.getText().toString());
        int rssi3 = Integer.parseInt(signalStrengthRouter3.getText().toString());
        ArrayList<double[]> positionList = new ArrayList<double[]>();

        for (int i = 0; i < positionDataBase.size(); i += 3) {
            int router1signal = Integer.parseInt(positionDataBase.get(i)[2]);
            int router2signal = Integer.parseInt(positionDataBase.get(i + 1)[2]);
            int router3signal = Integer.parseInt(positionDataBase.get(i + 2)[2]);
            double distance = 0;

            distance += Math.abs(Math.pow(rssi1 - router1signal, distanceValue));
            distance += Math.abs(Math.pow(rssi2 - router2signal, distanceValue));
            distance += Math.abs(Math.pow(rssi3 - router3signal, distanceValue));

            double euclideanDistance = Math.sqrt(distance);
            if (positionList.isEmpty()){
                positionList.add(new double[] {euclideanDistance, Double.parseDouble(positionDataBase.get(i)[0]), Double.parseDouble(positionDataBase.get(i)[1])});
            }
            else {
                boolean insertedFlag = false;
                for (int listCount = 0; listCount < positionList.size(); listCount++){
                    if (euclideanDistance <= positionList.get(listCount)[0]) {
                        positionList.add(listCount, new double[] {euclideanDistance, Double.parseDouble(positionDataBase.get(i)[0]), Double.parseDouble(positionDataBase.get(i)[1])});
                        insertedFlag = true;
                        break;
                    }
                }
                if (insertedFlag == false){
                    positionList.add(new double[] {euclideanDistance, Double.parseDouble(positionDataBase.get(i)[0]), Double.parseDouble(positionDataBase.get(i)[1])});
                }
            }
        }
        positionDataBase.clear();
        return positionList;
    }

    public void optimizeDataBase() {
        ArrayList<int[]> coordinatesList = new ArrayList<int[]>();

        for (int i = 0; i < positionDataBase.size(); i += 3) {
            int xCoord = Integer.parseInt(positionDataBase.get(i)[0]);
            int yCoord = Integer.parseInt(positionDataBase.get(i)[1]);
            int coordPosition = 0;
            boolean foundCoord = false;

            for (int j = 0; j < coordinatesList.size(); j += 3) {
                if (coordinatesList.get(j)[0] == xCoord && coordinatesList.get(j)[1] == yCoord) {
                    coordPosition = j;
                    foundCoord = true;
                    break;
                }
            }
            if (foundCoord == false) {
                coordinatesList.add(new int[] {xCoord, yCoord});
                coordinatesList.add(new int[] {xCoord, yCoord});
                coordinatesList.add(new int[] {xCoord, yCoord});
            }
            else {
                int newSignal1 = (Integer.parseInt(positionDataBase.get(i)[2]) + Integer.parseInt(positionDataBase.get(coordPosition)[2])) / 2;
                int newSignal2 = (Integer.parseInt(positionDataBase.get(i + 1)[2]) + Integer.parseInt(positionDataBase.get(coordPosition + 1)[2])) / 2;
                int newSignal3 = (Integer.parseInt(positionDataBase.get(i + 2)[2]) + Integer.parseInt(positionDataBase.get(coordPosition + 2)[2])) / 2;

                positionDataBase.set(coordPosition, new String[] {positionDataBase.get(i)[0], positionDataBase.get(i)[1], String.valueOf(newSignal1)});
                positionDataBase.set(coordPosition + 1, new String[] {positionDataBase.get(i)[0], positionDataBase.get(i)[1], String.valueOf(newSignal2)});
                positionDataBase.set(coordPosition + 2, new String[] {positionDataBase.get(i)[0], positionDataBase.get(i)[1], String.valueOf(newSignal3)});
                positionDataBase.remove(i);
                positionDataBase.remove(i + 1);
                positionDataBase.remove(i + 2);
                i -= 3;
            }
        }
    }

    public void ReadConfigFile(File fileName) {
        FileReader fr = null;
        String line = "";
        try {
            fr = new FileReader(fileName);
        } catch (FileNotFoundException e) {
            System.out.println("File open error!");
            System.exit(1);
        }

        BufferedReader bfr = new BufferedReader(fr);
        try {
            while((line = bfr.readLine()) != null){
                String[] lineValues = line.split(";");
                mapText = lineValues[0];
                mac1Text = lineValues[1];
                mac2Text = lineValues[2];
                mac3Text = lineValues[3];
            }
        } catch (IOException e) {
            System.out.println("File read error!");
            System.exit(2);
        }
        try {
            fr.close();
        } catch (IOException e) {
            System.out.println("File close error!");
            System.exit(3);
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_wifi, menu);
        return true;
    }

    protected void onResume() {
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }


    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            wifiList = myWifiManager.getScanResults();

            setRouterData(mac1Text, 1);
            setRouterData(mac2Text, 2);
            setRouterData(mac3Text, 3);
            if (databaseFile.exists()) {
                currentMap = setLocationOnMap();
            }
        }
    }
}
