package kr.re.keti.mobiussampleapp_v25;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import static kr.re.keti.mobiussampleapp_v25.R.layout.activity_main;

public class MainActivity extends AppCompatActivity implements Button.OnClickListener, CompoundButton.OnCheckedChangeListener {
//    public Button btnRetrieve;
//    public ToggleButton btnControl_Green;
//    public ToggleButton btnControl_Blue;
    public Switch Switch_MQTT;
//    public TextView textViewData;
    public Handler handler;
    public ToggleButton btnAddr_Set;

    private static CSEBase csebase = new CSEBase();
    private static AE ae = new AE();
    private static String TAG = "MainActivity";
    private String MQTTPort = "1883";
    private String ServiceAEName = "edu4";
    private String MQTT_Req_Topic = "";
    private String MQTT_Resp_Topic = "";
    private MqttAndroidClient mqttClient = null;
//    private EditText EditText_Address =null;
    private String Mobius_Address ="";
    private ImageView mImageView;
    private int time = 0;//rc카 움직인 시간
    private int distance;
    private int direction=0;
    private int x_start = 0;
    private int x_stop = 0;
    private int y_start = 0;
    private int y_stop = 0;
    private int color = 6;
    Bitmap bitmap;
    Canvas canvas;

    private int temp;
    // Main
    public MainActivity() {
        handler = new Handler();
    }
    /* onCreate */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_main);
        temp = 0;
//        btnRetrieve = (Button) findViewById(R.id.btnRetrieve);
        Switch_MQTT = (Switch) findViewById(R.id.switch_mqtt);
//        btnControl_Green = (ToggleButton) findViewById(R.id.btnControl_Green);
//        btnControl_Blue = (ToggleButton) findViewById(R.id.btnControl_Blue);
//        textViewData = (TextView) findViewById(R.id.textViewData);
//        EditText_Address = (EditText) findViewById(R.id.editText);
        btnAddr_Set = (ToggleButton) findViewById(R.id.toggleButton_Addr);

//        btnRetrieve.setOnClickListener(this);
        Switch_MQTT.setOnCheckedChangeListener(this);
//        btnControl_Green.setOnClickListener(this);
//        btnControl_Blue.setOnClickListener(this);
        btnAddr_Set.setOnClickListener(this);

//        btnRetrieve.setVisibility(View.INVISIBLE);
        Switch_MQTT.setVisibility(View.INVISIBLE);
//        btnControl_Green.setVisibility(View.INVISIBLE);
//        btnControl_Blue.setVisibility(View.INVISIBLE);
        mImageView = (ImageView) findViewById(R.id.iv);
        btnAddr_Set.setFocusable(true);
        x_start = 600;
        y_start = 900;
        bitmap = Bitmap.createBitmap(
                1200, // Width
                1800, // Height
                Bitmap.Config.ARGB_8888 // Config
        );
        canvas = new Canvas(bitmap);
        canvas.drawColor(Color.LTGRAY);
        // Create AE and Get AEID
        //GetAEInfo();
    }
    /* AE Create for Androdi AE */
    public void GetAEInfo() {

//        Mobius_Address = EditText_Address.getText().toString();

//        csebase.setInfo(Mobius_Address,"7579","Mobius","1883");

        csebase.setInfo("192.168.43.243","7579","Mobius","1883");
        // AE Create for Android AE
        ae.setAppName("ncubeapp");
        aeCreateRequest aeCreate = new aeCreateRequest();
        aeCreate.setReceiver(new IReceived() {
            public void getResponseBody(final String msg) {
                handler.post(new Runnable() {
                    public void run() {
                        Log.d(TAG, "** AE Create ResponseCode[" + msg +"]");
                        if( Integer.parseInt(msg) == 201 ){
                            MQTT_Req_Topic = "/oneM2M/req/Mobius2/"+ae.getAEid()+"_sub"+"/#";
                            MQTT_Resp_Topic = "/oneM2M/resp/Mobius2/"+ae.getAEid()+"_sub"+"/json";
                            Log.d(TAG, "ReqTopic["+ MQTT_Req_Topic+"]");
                            Log.d(TAG, "ResTopic["+ MQTT_Resp_Topic+"]");
                        }
                        else { // If AE is Exist , GET AEID
                            aeRetrieveRequest aeRetrive = new aeRetrieveRequest();
                            aeRetrive.setReceiver(new IReceived() {
                                public void getResponseBody(final String resmsg) {
                                    handler.post(new Runnable() {
                                        public void run() {
                                            Log.d(TAG, "** AE Retrive ResponseCode[" + resmsg +"]");
                                            MQTT_Req_Topic = "/oneM2M/req/Mobius2/"+ae.getAEid()+"_sub"+"/#";
                                            MQTT_Resp_Topic = "/oneM2M/resp/Mobius2/"+ae.getAEid()+"_sub"+"/json";
                                            Log.d(TAG, "ReqTopic["+ MQTT_Req_Topic+"]");
                                            Log.d(TAG, "ResTopic["+ MQTT_Resp_Topic+"]");
                                        }
                                    });
                                }
                            });
                            aeRetrive.start();
                        }
                    }
                });
            }
        });
        aeCreate.start();
    }
    /* Switch - Get Co2 Data With MQTT */
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (isChecked) {
            Log.d(TAG, "MQTT Create");
            MQTT_Create(true);
        } else {
            Log.d(TAG, "MQTT Close");
            MQTT_Create(false);
        }
    }
    /* MQTT Subscription */
    public void MQTT_Create(boolean mtqqStart) {
        if (mtqqStart && mqttClient == null) {
            /* Subscription Resource Create to Yellow Turtle */
            SubscribeResource subcribeResource = new SubscribeResource();
            subcribeResource.setReceiver(new IReceived() {
                public void getResponseBody(final String msg) {
                    handler.post(new Runnable() {
                        public void run() {

//                            textViewData.setText("" + msg);
                        }
                    });
                }
            });
            subcribeResource.start();

            /* MQTT Subscribe */
            mqttClient = new MqttAndroidClient(this.getApplicationContext(), "tcp://" + csebase.getHost() + ":" + csebase.getMQTTPort(), MqttClient.generateClientId());
            mqttClient.setCallback(mainMqttCallback);
            try {
                IMqttToken token = mqttClient.connect();
                token.setActionCallback(mainIMqttActionListener);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            /* MQTT unSubscribe or Client Close */
            mqttClient.setCallback(null);
            mqttClient.close();
            mqttClient = null;
        }
    }
    /* MQTT Listener */
    private IMqttActionListener mainIMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            Log.d(TAG, "onSuccess");
            String payload = "";
            int mqttQos = 1; /* 0: NO QoS, 1: No Check , 2: Each Check */

            MqttMessage message = new MqttMessage(payload.getBytes());
            try {
                mqttClient.subscribe(MQTT_Req_Topic, mqttQos);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            Log.d(TAG, "onFailure");
        }
    };
    /* MQTT Broker Message Received */
    private MqttCallback mainMqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            Log.d(TAG, "connectionLost");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
//            JSONObject json = new JSONObject(message.toString());
//            String time_dir = json.getString("con");
//            Log.d("asd",time_dir);
            // Initialize a new Canvas instance


            // Draw a solid color on the canvas as background


            // Initialize a new Paint instance to draw the line



            // Set a pixels value to offset the line from canvas edge
            int offset = 50;

            JSONObject json = new JSONObject(message.toString());
//            Log.d("asd", "" + direction + "," + x_start + "," + x_stop + "," + y_start + "," + y_stop);
            // Draw a line on canvas at the center position
//            Log.d("asd",json.getString("pc"));
            JSONObject json2 = new JSONObject(json.getString("pc"));
//            Log.d("asd",json2.getString("m2m:sgn"));
            JSONObject json3 = new JSONObject(json2.getString("m2m:sgn"));
//            Log.d("asd",json3.getString("nev"));
            JSONObject json4 = new JSONObject(json3.getString("nev"));
//            Log.d("asd",json4.getString("rep"));
            JSONObject json5 = new JSONObject(json4.getString("rep"));
//            Log.d("asd",json5.getString("m2m:cin"));
            JSONObject json6 = new JSONObject(json5.getString("m2m:cin"));
//            Log.d("asd",json6.getString("con"));
            int time_dir = json6.getInt("con");
            color = time_dir/10;
            Paint paint = new Paint();
            // Line color
            if(color == 6) {
                paint.setColor(Color.BLACK);
            }else if(color == 5) {
                paint.setColor(Color.BLUE);
            }else if(color == 4){
                paint.setColor(Color.RED);
            }else{
                paint.setColor(Color.BLACK);
            }
            paint.setStyle(Paint.Style.STROKE);
            // Line width in pixels
            paint.setStrokeWidth(8);
            paint.setAntiAlias(true);

            Log.d("asd",""+time_dir);
//            if(temp == time_dir) {
//                time =0;
//                direction =0;
//
//            }else{
//
////                time = (time_dir / 1000) * 20;
//                time = 100;
//                direction = time_dir;
//            }
            time = 30;
            direction = time_dir%10;

            if (direction == 0) {
                x_stop = x_start;
                y_stop = y_start - time;
            } else if (direction == 1) {
                x_stop = x_start + time;
                y_stop = y_start;
            } else if (direction == 2) {
                x_stop = x_start;
                y_stop = y_start + time;
            } else if (direction == 3) {
                x_stop = x_start - time;
                y_stop = y_start;
            }



            canvas.drawLine(
                    x_start + 0, // startX
                    y_start + 0, // startY
                    x_stop + 0, // stopX
                    y_stop + 0, // stopY
                    paint // Paint
            );
            x_start = x_stop;
            y_start = y_stop;
            // Display the newly created bitmap on app interface

            mImageView.setImageBitmap(bitmap);
//            if (direction == 3) {
//                direction = 0;
//            } else {
//                direction = direction + 1;
//            }
            temp = time_dir;
            Log.d(TAG, "messageArrived");

//            textViewData.setText("");
//            textViewData.setText("" + message.toString().replaceAll(",", "\n"));
            Log.d(TAG, "Notify ResMessage:" + message.toString());

            /* Json Type Response Parsing */
            String retrqi = MqttClientRequestParser.notificationJsonParse(message.toString());
            Log.d(TAG, "RQI["+ retrqi +"]");

            String responseMessage = MqttClientRequest.notificationResponse(retrqi);
            Log.d(TAG, "Recv OK ResMessage ["+responseMessage+"]");

            /* Make json for MQTT Response Message */
            MqttMessage res_message = new MqttMessage(responseMessage.getBytes());

            try {
                mqttClient.publish(MQTT_Resp_Topic, res_message);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.d(TAG, "deliveryComplete");
        }

    };
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.btnRetrieve: {
//                RetrieveRequest req = new RetrieveRequest();
//                textViewData.setText("");
//                req.setReceiver(new IReceived() {
//                    public void getResponseBody(final String msg) {
//                        handler.post(new Runnable() {
//                            public void run() {
//                                textViewData.setText("" + msg);
//                            }
//                        });
//                    }
//                });
//                req.start();
//                break;
//            }
//            case R.id.btnControl_Green: {
//                if (((ToggleButton) v).isChecked()) {
//                    ControlRequest req = new ControlRequest("1");
//                    req.setReceiver(new IReceived() {
//                        public void getResponseBody(final String msg) {
//                            handler.post(new Runnable() {
//                                public void run() {
//                                    textViewData.setText("" + msg);
//                                }
//                            });
//                        }
//                    });
//                    req.start();
//                } else {
//                    ControlRequest req = new ControlRequest("2");
//                    req.setReceiver(new IReceived() {
//                        public void getResponseBody(final String msg) {
//                            handler.post(new Runnable() {
//                                public void run() {
//                                    textViewData.setText("n" + msg);
//                                }
//                            });
//                        }
//                    });
//                    req.start();
//                }
//                break;
//            }
//            case R.id.btnControl_Blue: {
//                if (((ToggleButton) v).isChecked()) {
//                    ControlRequest req = new ControlRequest("3");
//                    req.setReceiver(new IReceived() {
//                        public void getResponseBody(final String msg) {
//                            handler.post(new Runnable() {
//                                public void run() {
//                                    textViewData.setText("" + msg);
//                                }
//                            });
//                        }
//                    });
//                    req.start();
//                } else {
//                    ControlRequest req = new ControlRequest("4");
//                    req.setReceiver(new IReceived() {
//                        public void getResponseBody(final String msg) {
//                            handler.post(new Runnable() {
//                                public void run() {
//                                    textViewData.setText("" + msg);
//                                }
//                            });
//                        }
//                    });
//                    req.start();
//                }
//                break;
//            }
            case R.id.toggleButton_Addr: {
                if (((ToggleButton) v).isChecked()) {

//                    btnRetrieve.setVisibility(View.VISIBLE);
                    Switch_MQTT.setVisibility(View.VISIBLE);
//                    btnControl_Green.setVisibility(View.VISIBLE);
//                    btnControl_Blue.setVisibility(View.VISIBLE);

                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(EditText_Address.getWindowToken(), 0);//hide keyboard

                    GetAEInfo();

                } else {
//                    btnRetrieve.setVisibility(View.INVISIBLE);
                    Switch_MQTT.setVisibility(View.INVISIBLE);
//                    btnControl_Green.setVisibility(View.INVISIBLE);
//                    btnControl_Blue.setVisibility(View.INVISIBLE);

                }
                break;
            }
        }
    }
    @Override
    public void onStart() {
        super.onStart();

    }
    @Override
    public void onStop() {
        super.onStop();

    }

    /* Response callback Interface */
    public interface IReceived {
        void getResponseBody(String msg);
    }

    /* Retrieve Co2 Sensor */
    class RetrieveRequest extends Thread {
        private final Logger LOG = Logger.getLogger(RetrieveRequest.class.getName());
        private IReceived receiver;
        private String ContainerName = "co2";

        public RetrieveRequest(String containerName) {
            this.ContainerName = containerName;
        }
        public RetrieveRequest() {}
        public void setReceiver(IReceived hanlder) { this.receiver = hanlder; }

        @Override
        public void run() {
            try {
                String sb = csebase.getServiceUrl() + "/" + ServiceAEName + "/" + ContainerName + "/" + "latest";

                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(false);

                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", ae.getAEid() );
                conn.setRequestProperty("nmtype", "long");
                conn.connect();

                String strResp = "";
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String strLine= "";
                while ((strLine = in.readLine()) != null) {
                    strResp += strLine;
                }

                if ( strResp != "" ) {
                    receiver.getResponseBody(strResp);
                }
                conn.disconnect();

            } catch (Exception exp) {
                LOG.log(Level.WARNING, exp.getMessage());
            }
        }
    }
    /* Request Control LED */
    class ControlRequest extends Thread {
        private final Logger LOG = Logger.getLogger(ControlRequest.class.getName());
        private IReceived receiver;
        private String container_name = "led";

        public ContentInstanceObject contentinstance;
        public ControlRequest(String comm) {
            contentinstance = new ContentInstanceObject();
            contentinstance.setContent(comm);
        }
        public void setReceiver(IReceived hanlder) { this.receiver = hanlder; }

        @Override
        public void run() {
            try {
                String sb = csebase.getServiceUrl() +"/" + ServiceAEName + "/" + container_name;

                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setInstanceFollowRedirects(false);

                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("Content-Type", "application/vnd.onem2m-res+xml;ty=4");
                conn.setRequestProperty("locale", "ko");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", ae.getAEid() );

                String reqContent = contentinstance.makeXML();
                conn.setRequestProperty("Content-Length", String.valueOf(reqContent.length()));

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.write(reqContent.getBytes());
                dos.flush();
                dos.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String resp = "";
                String strLine="";
                while ((strLine = in.readLine()) != null) {
                    resp += strLine;
                }
                if (resp != "") {
                    receiver.getResponseBody(resp);
                }
                conn.disconnect();

            } catch (Exception exp) {
                LOG.log(Level.SEVERE, exp.getMessage());
            }
        }
    }
    /* Request AE Creation */
    class aeCreateRequest extends Thread {
        private final Logger LOG = Logger.getLogger(aeCreateRequest.class.getName());
        String TAG = aeCreateRequest.class.getName();
        private IReceived receiver;
        int responseCode=0;
        public ApplicationEntityObject applicationEntity;
        public void setReceiver(IReceived hanlder) { this.receiver = hanlder; }
        public aeCreateRequest(){
            applicationEntity = new ApplicationEntityObject();
            applicationEntity.setResourceName(ae.getappName());
        }
        @Override
        public void run() {
            try {

                String sb = csebase.getServiceUrl();
                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setInstanceFollowRedirects(false);

                conn.setRequestProperty("Content-Type", "application/vnd.onem2m-res+xml;ty=2");
                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("locale", "ko");
                conn.setRequestProperty("X-M2M-Origin", "S"+ae.getappName());
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-NM", ae.getappName() );

                String reqXml = applicationEntity.makeXML();
                conn.setRequestProperty("Content-Length", String.valueOf(reqXml.length()));

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.write(reqXml.getBytes());
                dos.flush();
                dos.close();

                responseCode = conn.getResponseCode();

                BufferedReader in = null;
                String aei = "";
                if (responseCode == 201) {
                    // Get AEID from Response Data
                    in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    String resp = "";
                    String strLine;
                    while ((strLine = in.readLine()) != null) {
                        resp += strLine;
                    }

                    ParseElementXml pxml = new ParseElementXml();
                    aei = pxml.GetElementXml(resp, "aei");
                    ae.setAEid( aei );
                    Log.d(TAG, "Create Get AEID[" + aei + "]");
                    in.close();
                }
                if (responseCode != 0) {
                    receiver.getResponseBody( Integer.toString(responseCode) );
                }
                conn.disconnect();
            } catch (Exception exp) {
                LOG.log(Level.SEVERE, exp.getMessage());
            }

        }
    }
    /* Retrieve AE-ID */
    class aeRetrieveRequest extends Thread {
        private final Logger LOG = Logger.getLogger(aeCreateRequest.class.getName());
        private IReceived receiver;
        int responseCode=0;

        public aeRetrieveRequest() {
        }
        public void setReceiver(IReceived hanlder) {
            this.receiver = hanlder;
        }

        @Override
        public void run() {
            try {
                String sb = csebase.getServiceUrl()+"/"+ ae.getappName();
                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(false);

                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", "Sandoroid");
                conn.setRequestProperty("nmtype", "short");
                conn.connect();

                responseCode = conn.getResponseCode();

                BufferedReader in = null;
                String aei = "";
                if (responseCode == 200) {
                    // Get AEID from Response Data
                    in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    String resp = "";
                    String strLine;
                    while ((strLine = in.readLine()) != null) {
                        resp += strLine;
                    }

                    ParseElementXml pxml = new ParseElementXml();
                    aei = pxml.GetElementXml(resp, "aei");
                    ae.setAEid( aei );
                    //Log.d(TAG, "Retrieve Get AEID[" + aei + "]");
                    in.close();
                }
                if (responseCode != 0) {
                    receiver.getResponseBody( Integer.toString(responseCode) );
                }
                conn.disconnect();
            } catch (Exception exp) {
                LOG.log(Level.SEVERE, exp.getMessage());
            }
        }
    }
    /* Subscribe Co2 Content Resource */
    class SubscribeResource extends Thread {
        private final Logger LOG = Logger.getLogger(SubscribeResource.class.getName());
        private IReceived receiver;
        private String container_name = "co2"; //change to control container name

        public ContentSubscribeObject subscribeInstance;
        public SubscribeResource() {
            subscribeInstance = new ContentSubscribeObject();
            subscribeInstance.setUrl(csebase.getHost());
            subscribeInstance.setResourceName(ae.getAEid()+"_rn");
            subscribeInstance.setPath(ae.getAEid()+"_sub");
            subscribeInstance.setOrigin_id(ae.getAEid());
        }
        public void setReceiver(IReceived hanlder) { this.receiver = hanlder; }

        @Override
        public void run() {
            try {
                String sb = csebase.getServiceUrl() + "/" + ServiceAEName + "/" + container_name;

                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setInstanceFollowRedirects(false);

                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("Content-Type", "application/vnd.onem2m-res+xml; ty=23");
                conn.setRequestProperty("locale", "ko");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", ae.getAEid());

                String reqmqttContent = subscribeInstance.makeXML();
                conn.setRequestProperty("Content-Length", String.valueOf(reqmqttContent.length()));

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.write(reqmqttContent.getBytes());
                dos.flush();
                dos.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String resp = "";
                String strLine="";
                while ((strLine = in.readLine()) != null) {
                    resp += strLine;
                }

                if (resp != "") {
                    receiver.getResponseBody(resp);
                }
                conn.disconnect();

            } catch (Exception exp) {
                LOG.log(Level.SEVERE, exp.getMessage());
            }
        }
    }
}