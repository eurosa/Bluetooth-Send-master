package com.token.scan;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.exifinterface.media.ExifInterface;

import com.google.android.gms.instantapps.InstantApps;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static android.content.ContentValues.TAG;


public class DeviceList extends AppCompatActivity implements  View.OnClickListener  , NavigationView.OnNavigationItemSelectedListener
{
    private static final String MY_PREFS_NAME = "MyTxtFile";;
    //  private CameraKitView cameraKitView;
    //==============================To Connect Bluetooth Device=============================
    private ProgressDialog progress;
    private boolean isBtConnected = false;
    BluetoothSocket btSocket = null;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String address = null;
    private Button sendBtn;

    boolean listViewFlag=true;
   
    
    //==============================To connect bluetooth devices=============================
    //-----------------------------Camera----------------------------------------------------
    private static final String LOG_TAG = "JBCamera";
    private static final int REQUEST_CAMERA_PERMISSION = 21;
    private static final int REQUEST_STORAGE_PERMISSION = 22;
    private int cameraId = 1;
    private Camera camera = null;
    private boolean waitForPermission = false;
    private boolean waitForStoragePermission = false;
    //-----------------------------------Camera-----------------------------------------------
    Button On, Off, Discnt, Abt;

    //widgets
    Button btnPaired,scanDevices;
    ListView devicelist;
    //Bluetooth
    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";

    //screenshott
    private final static String[] requestWritePermission =
            { Manifest.permission.WRITE_EXTERNAL_STORAGE };
    private ImageView imageView;
    private Bitmap bitmap;

    private String str_celcius;
    private String str_fahrenheit;
    private ImageView bmpView;
    //=================================For temperature limit count==================================


    private String double_str_fahrenheit;


    private ImageView iv_your_image;
    private String str_cel,str_fah;

    //=========================================End temperature limit count==========================
    String[] permissions = new String[]{

            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    private    FrameLayout   layout;

    private TextView one, two, three, four, five, six, seven, eight, nine, zero, div, multi, sub, plus, dot, equals, display, clear;
    private ImageButton backDelete;

    /***************************************************************************************
     * Navigation Drawer Layout
     *
     ***************************************************************************************/
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView mNavigationView;
    private ListView idList,sndList,digitList,typeList;
    private ArrayAdapter<String> typeAdapter;
    private ArrayAdapter<String> sndAdapter;
    private ArrayAdapter<String> idAdapter;
    private ArrayAdapter<String> digitAdapter;
    private Dialog idDialog;
    private Dialog digitDialog;
    private Dialog sndDialog;
    private Dialog typeDialog;
    private DatabaseHandler dbHandler;
    private DataModel dataModel;

    /****************************************************************************************
    * End of Navigation Drawer
    *
    * */

    //screenshot
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
//----------------------------Grant storage permission--------------------------------------------------
        /*********************************************************************************
        * Initialize Database
        *
        * *******************************************************************************/
        dataModel = new DataModel();
        dbHandler = new DatabaseHandler(this);
        dbHandler.getQmsUtilityById("1",dataModel);
        /*********************************************************************************
         * Initialize Database
         *
         * *******************************************************************************/
        //----------------------------------------------------------------------------------------------
        //=========================Adding Toolbar in android layout======================================
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       // getSupportActionBar().setDisplayShowHomeEnabled(true);
        //=========================Toolbar End============================================================


        /***************************************************************************************
         * Navigation Drawer Layout
         *
         ***************************************************************************************/
        // drawer layout instance to toggle the menu icon to open
        // drawer and back button to close drawer
        drawerLayout = findViewById(R.id.draw_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        mNavigationView =  findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        // to make the Navigation drawer icon always appear on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       /***************************************************************************************
        * Navigation Drawer Layout
        *
        ***************************************************************************************/
        //cameraKitView = findViewById(R.id.camera);
        //-------------------------------------To Receive device address from background==================

        bmpView = findViewById(R.id.bitmap_view);

        Discnt = (Button)findViewById(R.id.dis_btn);
        sendBtn=findViewById(R.id.send_btn);
        //iv_your_image=findViewById(R.id.iv_your_image);
        //============================Keyboard=====================================================//
        //commands to be sent to bluetooth
        sendBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendData();      //method to turn on
            }
        });

        Discnt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect(); //close connection
            }
        });

        one = findViewById(R.id.one);
        two = findViewById(R.id.two);
        three = findViewById(R.id.three);
        four = findViewById(R.id.four);
        five = findViewById(R.id.five);
        six = findViewById(R.id.six);
        seven = findViewById(R.id.seven);
        eight = findViewById(R.id.eight);
        nine = findViewById(R.id.nine);
        zero = findViewById(R.id.zero);
        display =findViewById(R.id.display);
        clear = findViewById(R.id.clear);
        backDelete = findViewById(R.id.backDelete);

        one.setOnClickListener(this);
        two.setOnClickListener(this);
        three.setOnClickListener(this);
        four.setOnClickListener(this);
        five.setOnClickListener(this);
        six.setOnClickListener(this);
        seven.setOnClickListener(this);
        eight.setOnClickListener(this);
        nine.setOnClickListener(this);
        zero.setOnClickListener(this);
        display.setOnClickListener(this);
        clear.setOnClickListener(this);
        backDelete.setOnClickListener(this);

        Typeface tf = Typeface.createFromAsset(getApplicationContext().getAssets(), "SevenSegment.ttf");
        display.setTypeface(tf);


        //============================Keyboard====================================================//


        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); //receive the address of the bluetooth device
        //new ConnectBT().execute(); //Call the class to connect
        //-------------------------------------To Receive device address from background==================
        //====================================Camera======================================================

        boolean isInstantApp = InstantApps.getPackageManagerCompat(this).isInstantApp();
        Log.d(LOG_TAG, "are we instant?" + isInstantApp);

        //findViewById(R.id.capture_button).setOnClickListener(clickListener);
    //    findViewById(R.id.switch_button).setOnClickListener(clickListener);

        //=======================================Camera=============================================
        //============================Create Button Programmatically================================
        //the layout on which you are working
        // layout =  findViewById(R.id.cameraLayout);

        //set the properties for button



        //===================================================End Create Button Programmatically=====

        //Calling widgets
        btnPaired = findViewById(R.id.button);
     //   devicelist = findViewById(R.id.listView);
        scanDevices=findViewById(R.id.scanDevice);
     //   devicelist.setVisibility(View.GONE);

        //if the device has bluetooth
        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if(myBluetooth == null)
        {
            //Show a Mensag. That the device has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();

            //finish apk
            finish();
        }
        else if(!myBluetooth.isEnabled())
        {
                //Ask to the user turn the bluetooth on
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon,1);
        }

        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {


                pairedDevicesList();

            }

        }

        );



        scanDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ScanDevicesList();
            }
        });





        //Camera screenshot
        final boolean hasWritePermission = RuntimePermissionUtil.checkPermissonGranted(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        imageView = findViewById(R.id.imageView);


//----------------------------------screen_shot xml view-----------------------------------------
        //Camera screenshot

        //=================================FileExposed============================
        /*
        *
        *
        * android.os.FileUriExposedException: file:///storage/emulated/0/test.txt exposed beyond app through Intent.getData()
        * solved using this
        * */
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
        //=======================================================================



    }


    private void sendData()
    {
        if (btSocket!=null)
        {
            try
            {


                final Handler handler=new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // myLabel.setText(sendEditText.getText().toString());
                        handler.postDelayed(this,100);
                    }
                },100);
                String data="$134"+display.getText().toString()+";";
                btSocket.getOutputStream().write(data.getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }


    private void ScanDevicesList(){

        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }

    private void pairedDevicesList()
    {
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size()>0)
        {
            for(BluetoothDevice bt : pairedDevices)
            {
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }



        //--------------------------------------------------------------------------------------------------------------
       Dialog dialog = new Dialog(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a paired device for connecting");

        LinearLayout parent = new LinearLayout(DeviceList.this);

        parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        parent.setOrientation(LinearLayout.VERTICAL);

        ListView modeList = new ListView(this);



        //------------------To fixed height of listView------------------------------------
        setListViewHeightBasedOnItems(modeList);
        //RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(200, 0);
        //modeList.setLayoutParams(params);
        //modeList.requestLayout();
        /******************************************************************************************************************
        *
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.height = 20;
        modeList.setLayoutParams(params);
        modeList.requestLayout();

        *
        * */
       // ViewGroup.LayoutParams listViewParams = (ViewGroup.LayoutParams)modeList.getLayoutParams();
        //listViewParams.height = 20;
       // modeList.smoothScrollToPosition(3);
/*
        ListAdapter listadp = modeList.getAdapter();
        if (listadp != null) {
            int totalHeight = 0;
            for (int i = 0; i < listadp.getCount(); i++) {
                View listItem = listadp.getView(i, null, modeList);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = modeList.getLayoutParams();
            params.height = totalHeight + (modeList.getDividerHeight() * (listadp.getCount() - 1));
            modeList.setLayoutParams(params);
            modeList.requestLayout();
        }
*/
        //------------------End of fixed height of listView---------------------------------

        final ArrayAdapter modeAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        modeList.setAdapter(modeAdapter);
        modeList.setOnItemClickListener(myListClickListener);
        builder.setView(modeList);
      //  builder.show();
        dialog = builder.create();
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 600); //Controlling width and height.


        //-------------------------------------------------------------------------------------------------------------



    }


    private void pairedDevicesListOriginal()
    {
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size()>0)
        {
            for(BluetoothDevice bt : pairedDevices)
            {
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked



    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView<?> av, View v, int arg2, long arg3)
        {



            //Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            //Make an intent to start next activity.
            //Intent i = new Intent(DeviceList.this, DeviceList.class);

            //Change the activity.
            //i.putExtra(EXTRA_ADDRESS, address); //this will be received at DataControl (class) Activity
            //startActivity(i);
            new ConnectBT(address,info).execute(); //Call the class to connect
        }
    };


    @Override
    public boolean onNavigationItemSelected(@NonNull  MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {

            case R.id.nav_id:
                idList();
                break;
            case R.id.nav_exit:
                exitApplication();
                break;
            case R.id.nav_digits:
                noOfDigits();
                break;
            case R.id.nav_sound:
                selSound();
                break;
            case R.id.nav_type:
                Toast.makeText(getApplicationContext(), " Digit No: "+dataModel.getDigitNo(), Toast.LENGTH_LONG).show();
                selType();
                break;

        }
        //close navigation drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_list, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        /*******************************************************************************
        *
        * Navigation Menu Item
        * ******************************************************************************/
       if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
           //Toast.makeText(getApplicationContext(), "nav_exit"+item.getItemId(), Toast.LENGTH_LONG).show();
          /*  int n_id= item.getItemId();
            switch (n_id) {
                case R.id.nav_id:
                    pairedDevicesList();
                    return true;
                case R.id.nav_exit:
                    Toast.makeText(getApplicationContext(), "nav_exit", Toast.LENGTH_LONG).show();
                    return true;
                case R.id.nav_digits:
                    finish();
                    return true;
                case R.id.nav_sound:
                    Toast.makeText(getApplicationContext(), "nav_sound", Toast.LENGTH_LONG).show();
                    return true;
                case R.id.nav_type:
                    Toast.makeText(getApplicationContext(), "nav_type", Toast.LENGTH_LONG).show();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }*/
            return  true;
        }

        switch (id) {

            case R.id.action_delete:
                deleteFile();
                return true;
            case R.id.nav_id:
                Toast.makeText(getApplicationContext(), "nav_exit", Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_settings:
                Toast.makeText(getApplicationContext(), "action_settings", Toast.LENGTH_LONG).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }


    }


    private void idList()
    {
        idDialog = new Dialog(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a id");

        idList = new ListView(this);
        String[] stringArray = new String[] { "1", "2","3","4","5","6","7","8","9" };
        idAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, stringArray);
        idList.setAdapter(idAdapter);

        builder.setView(idList);
        idDialog = builder.create();
        idDialog.show();

        idList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String s = idList.getItemAtPosition(i).toString();
                dataModel.setDevId(s);
                dbHandler.up_nav_id(dataModel);

                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                  // If you want to close the adapter
                idDialog.dismiss();
            }
        });




    }

   private void noOfDigits(){

       digitDialog = new Dialog(this);
       AlertDialog.Builder builder = new AlertDialog.Builder(this);
       builder.setTitle("Select a digit");

       digitList = new ListView(this);
       String[] stringArray = new String[] { "2","3","4" };
       digitAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, stringArray);
       digitList.setAdapter(digitAdapter);


       digitList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

               String s = digitList.getItemAtPosition(i).toString();
               dbHandler.updateDigitNo(dataModel);
               Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
               digitDialog.dismiss(); // If you want to close the adapter
           }
       });


       builder.setView(digitList);
       digitDialog = builder.create();
       digitDialog.show();
   }

    private void selSound(){
        sndDialog = new Dialog(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a sound");
        sndList = new ListView(this);
        String[] stringArray = new String[] { "Ding Dong","English","Hindi","Hindi/English" };
        sndAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, stringArray);
        sndList.setAdapter(sndAdapter);


        sndList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String s = sndList.getItemAtPosition(i).toString();
                dbHandler.updateSound(dataModel);
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                sndDialog.dismiss(); // If you want to close the adapter
            }
        });


        builder.setView(sndList);
        sndDialog = builder.create();
        sndDialog.show();
    }

    private void selType(){

        dbHandler.Get_Total_QmsUtility();
        typeDialog = new Dialog(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a type");

        typeList = new ListView(this);
        String[] stringArray = new String[] { "1", "2","3","4"};
        typeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, stringArray);
        typeList.setAdapter(typeAdapter);


        typeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String s = typeList.getItemAtPosition(i).toString();

                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                typeDialog.dismiss(); // If you want to close the adapter
            }
        });


        builder.setView(typeList);
        typeDialog = builder.create();
        typeDialog.show();


    }




    public void exitApplication(){
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        // adb.setView(Integer.parseInt("Delete Folder"));
        adb.setTitle("Exit");
        adb.setMessage("Are you sure you want to exit application?");
        adb.setIcon(android.R.drawable.ic_dialog_alert);
        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

              finish();
            }
        });
        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(DeviceList.this, "Cancel",
                        Toast.LENGTH_SHORT).show();
                //finish();
            }
        });
        adb.show();

    }

public void deleteFile(){

    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    // adb.setView(Integer.parseInt("Delete Folder"));
    adb.setTitle("Delete Folder");
    adb.setMessage("Are you sure you want to delete this Folder?");
    adb.setIcon(android.R.drawable.ic_dialog_alert);
    adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {

            // File dir =new File( getApplicationContext().getFilesDir()+"/Temperature_Scan_Folder");
            File dir = new File(Environment.getExternalStorageDirectory()+"/Temperature_Scan_Folder");
            boolean success = deleteDir(dir);
            if(success) {
                Toast.makeText(DeviceList.this, "Successfully Deleted the Folder ",
                        Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(DeviceList.this, "Folder Not Found",
                        Toast.LENGTH_SHORT).show();

            }
        }
    });
    adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            Toast.makeText(DeviceList.this, "Cancel",
                    Toast.LENGTH_SHORT).show();
            //finish();
        }
    });
    adb.show();
}


//----------------------------------------------Cmamera-----------------------------------------------------------

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (camera == null) {
                return;
            } else if (v.getId() == R.id.preview_surface) {
                try {
                 //   switchCamera();
                //    camera.takePicture(null, null, pictureCallback);
                } catch (RuntimeException e) {
                    Log.e(LOG_TAG, "preview_surface", e);
                }
            }
            /*else if (v.getId() == R.id.capture_button) {
                try {
                  //  takeScreenshot();
                    camera.takePicture(null, null, pictureCallback);
                } catch (RuntimeException e) {
                    Log.e(LOG_TAG, "capture_button", e);
                }
            } */
/*
            else if (v.getId() == R.id.switch_button) {
                switchCamera();
            }*/
        }
    };



    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    protected void onStart() {
        super.onStart();
      //  checkPermissions();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED&&checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                waitForPermission = true;
                requestCameraPermission();

            }
/*
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED&&checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                waitForStoragePermission = true;
                requestStoragePermission();

            }

 */

        }

     //   cameraKitView.onStart();
    }
    @Override
    protected void onStop() {
       // cameraKitView.onStop();
        super.onStop();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)&&shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)&&shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AlertDialog confirmationDialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.request_permission)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},
                                    REQUEST_CAMERA_PERMISSION);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                    .create();
            confirmationDialog.show();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestStoragePermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)&&shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AlertDialog confirmationDialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.request_permission)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},
                                    REQUEST_STORAGE_PERMISSION);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                    .create();
            confirmationDialog.show();
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull final int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 3 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                AlertDialog errorDialog = new AlertDialog.Builder(this)
                        .setMessage(R.string.request_permission).create();
                errorDialog.show();
               finish();
            } else {
                waitForPermission = false;

            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
//------------------------------------
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                AlertDialog errorDialog = new AlertDialog.Builder(this)
                        .setMessage(R.string.request_permission).create();
                errorDialog.show();
                finish();
            } else {
                waitForStoragePermission = false;
               // startCamera(cameraId);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

//---------------------------------------
        switch (requestCode) {
            case 100: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do something
                }


                RuntimePermissionUtil.onRequestPermissionsResult(grantResults, new RPResultListener() {


                    @Override
                    public void onPermissionGranted() {
                        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                        }
                    }

                    @Override
                    public void onPermissionDenied() {
                        Toast.makeText(DeviceList.this, "Permission Denied! You cannot save image!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                break;



            }
        }
    //    cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
//---------------------------------------------------------------------------------------
    }


    private SurfaceHolder.Callback shCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.i(LOG_TAG, "surfaceDestroyed callback");
            if (camera != null) {
                camera.stopPreview();
                camera.release();
            }
            camera = null;
        }



        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.i(LOG_TAG, "surfaceCreated callback");

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            Log.i(LOG_TAG, "surfaceChanged callback " + width + "x" + height);

        }

    };

    @Override
    public void onClick(View v) {


        if (v.findViewById(R.id.one) == one) {
            if (!display.getText().equals("")) {
                display.append("1");
                limitDigit(display.getText().toString());
            } else {
                display.setText("1");
            }
        } else if (v.findViewById(R.id.two) == two) {
            if (!display.getText().equals("")) {
                display.append("2");
                limitDigit(display.getText().toString());
            } else {
                display.setText("2");
            }
        } else if (v.findViewById(R.id.three) == three) {
            if (!display.getText().equals("")) {
                display.append("3");
                limitDigit(display.getText().toString());
            } else {
                display.setText("3");
            }
        } else if (v.findViewById(R.id.four) == four) {
            if (!display.getText().equals("")) {
                display.append("4");
                limitDigit(display.getText().toString());
            } else {
                display.setText("4");
            }
        } else if (v.findViewById(R.id.five) == five) {
            if (!display.getText().equals("")) {
                display.append("5");
                limitDigit(display.getText().toString());
            } else {
                display.setText("5");
            }
        } else if (v.findViewById(R.id.six) == six) {
            if (!display.getText().equals("")) {
                display.append("6");
                limitDigit(display.getText().toString());
            } else {
                display.setText("6");
            }
        } else if (v.findViewById(R.id.seven) == seven) {
            if (!display.getText().equals("")) {
                display.append("7");
                limitDigit(display.getText().toString());
            } else {
                display.setText("7");
            }
        } else if (v.findViewById(R.id.eight) == eight) {
            if (!display.getText().equals("")) {
                display.append("8");
                limitDigit(display.getText().toString());
            } else {
                display.setText("8");
            }
        } else if (v.findViewById(R.id.nine) == nine) {
            if (!display.getText().equals("")) {
                display.append("9");
                limitDigit(display.getText().toString());
            } else {
                display.setText("9");
            }
        } else if (v.findViewById(R.id.zero) == zero) {
            if (!display.getText().equals("")) {
                display.append("0");
                limitDigit(display.getText().toString());
            } else {
                display.setText("0");
            }
        } else if (v.findViewById(R.id.display) == display) {

        } else if (v.findViewById(R.id.clear) == clear) {
            display.setText(null);
        } else if (v.findViewById(R.id.backDelete) == backDelete) {
            if (!display.getText().equals("")) {
                String s = display.getText().toString();
                if (s.length() > 0) {
                    display.setText(s.substring(0, s.length() - 1));
                } else {
                    // Toast.makeText(this, "Nothing to remove", Toast.LENGTH_SHORT).show();
                }
            } else {
                //Toast.makeText(this, "nothing to remove", Toast.LENGTH_SHORT).show();
            }

        }

    }


    public  void limitDigit(String input)
    {

        String lastFourDigits = "";     //substring containing last 4 characters

        if (input.length() > 4)
        {
            lastFourDigits = input.substring(input.length() - 4);
        }
        else
        {
            lastFourDigits = input;
        }
        display.setText(lastFourDigits);

    }



    //=================================To Connect Bluetooth Device====================================
    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        String addressBLE=null;
        String infoBLE=null;
        private boolean ConnectSuccess = true; //if it's here, it's almost connected
        public ConnectBT(String address,String info) {
            super();
            addressBLE=address;
            infoBLE=info;
            //Do stuff
        }


        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(DeviceList.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {

            if(btSocket!=null){
                try {
                    btSocket.close();
                    btSocket=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(addressBLE);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                    mmOutputStream = btSocket.getOutputStream();
                    mmInputStream = btSocket.getInputStream();

                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);


            if (!ConnectSuccess)
            {
                Intent intent = getIntent();
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                // finish();
              //  startActivity(intent);
               // Disconnect();
                getSupportActionBar().setTitle(R.string.app_name);
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
                getSupportActionBar().setTitle(infoBLE);



/*
                try {
                    receiveData();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }
            progress.dismiss();

            //beginListenForData();

            new Thread(new Runnable() {
                public void run(){
                    try {
                     //   receiveData();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //beginListenForData();
                }
            }).start();
            /*try {
                receiveData();
            } catch (IOException e) {
                e.printStackTrace();
            }

            /*if(isBtConnected){

                try {
                    receiveData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/
        }
    }





    // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }
    //=================================To Connect Bluetooth Device====================================
    private void tryDrawing(SurfaceHolder holder) {
        Log.i(TAG, "Trying to draw...");

        Canvas canvas = holder.lockCanvas();
        if (canvas == null) {
            Log.e(TAG, "Cannot draw onto the canvas as it's null");
        } else {
            drawMyStuff(canvas);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawMyStuff(final Canvas canvas) {
        Random random = new Random();
        Log.i(TAG, "Drawing...");
        canvas.drawRGB(255, 128, 128);
    }





    /*
    public void clearViewData(){
        txt_celcius.setText("\u00B0C");
        txt_celcius.setText("\u00B0F");
        bmpView.setImageResource(0);
        //bmpView.setImageBitmap(null);
        bmpView.destroyDrawingCache();
    }*/


    //------------------------To check storage Permission--------------------------------------------------------------------------------------------

    //======================To resize and rotate image===========================================================
    /**
     * This method is responsible for solving the rotation issue if exist. Also scale the images to
     * 1024x1024 resolution
     *
     * @param context       The current context
     * @param selectedImage The Image URI
     * @return Bitmap image results
     * @throws IOException
     */


    /**
     * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object when decoding
     * bitmaps using the decode* methods from {@link BitmapFactory}. This implementation calculates
     * the closest inSampleSize that will result in the final decoded bitmap having a width and
     * height equal to or larger than the requested width and height. This implementation does not
     * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
     * results in a larger bitmap which isn't as useful for caching purposes.
     *
     * @param options   An options object with out* params already populated (run through a decode*
     *                  method with inJustDecodeBounds==true
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */



    /**
     * Rotate an image if required.
     *
     * @param img           The image bitmap
     * @param selectedImage Image URI
     * @return The resulted Bitmap after manipulation
     */
    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }
    //======================End of resize and rotate image=======================================================

    //=====================================Start Delete Folder===================================================
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
    //=====================================End Delete Folder=====================================================




    public static boolean setListViewHeightBasedOnItems(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            return true;

        } else {
            return false;
        }

    }

    private void Disconnect()
    {
     if (btSocket!=null) //If the btSocket is busy
         {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
         }
  //    finish(); //return to the first layout

    }



    @Override
    public void onBackPressed() {
// TODO Auto-generated method stub
        AlertDialog.Builder builder=new AlertDialog.Builder(DeviceList.this);
        // builder.setCancelable(false);
        builder.setTitle("Rate Us if u like this");
        builder.setMessage("Do you want to Exit?");
        builder.setPositiveButton("yes",new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Toast.makeText(DeviceList.this, "Yes i wanna exit", Toast.LENGTH_LONG).show();

                finish();
            }
        });
        builder.setNegativeButton("No",new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Toast.makeText(DeviceList.this, "i wanna stay on this", Toast.LENGTH_LONG).show();
                dialog.cancel();

            }
        });
        builder.setNeutralButton("Rate",new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                }

            }
        });
        AlertDialog alert=builder.create();
        alert.show();
        //super.onBackPressed();
    }



}
