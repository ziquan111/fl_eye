/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package ros_java.ardrone_ui;

import android.graphics.RectF;
import android.os.Bundle;
import org.ros.address.InetAddressFactory;
import org.ros.android.BitmapFromCompressedImage;
import org.ros.android.MessageCallable;
import org.ros.android.RosActivity;
import org.ros.android.view.RosButton;
import org.ros.android.view.RosHybridImageView;
import org.ros.android.view.RosScreenShotButton;
import org.ros.android.view.RosTextView;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import sensor_msgs.CompressedImage;
import std_msgs.*;

import android.view.View;
import android.content.Intent;
import android.widget.ImageView;

import com.google.common.collect.Lists;

import java.lang.String;
import java.util.ArrayList;

/**
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MainActivity extends RosActivity {

    private static final int RETAKE_REQUEST_CODE = 1;

//    private RosImageView<sensor_msgs.CompressedImage> image;
    private RosHybridImageView<CompressedImage, Float32MultiArray, Float32MultiArray> image;
    private RosScreenShotButton shootBtn;
    private RosTextView<Int32> batteryPercent;
    private RosButton tleBtn, sprBtn;   // stl: start land emergency; spr: start pause resume
    private RosButton plusBtn, minusBtn;
    private RosButton orbitBtn, panoBtn, zigzagBtn;
    private FlEyeGestureListener flEyeGestureListener;
    private ImageView galleryBtn;

    public MainActivity() {
        super("MainActivityTicker", "MainActivityTitle");
//        thisContext = this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        flEyeGestureListener = new FlEyeGestureListener(this);

        image = (RosHybridImageView<CompressedImage, Float32MultiArray, Float32MultiArray>) findViewById(R.id.image);
        image.setTopicNames("bebop/image/compressed", "draw_points", "draw_roundRect");//"/ardrone/image_raw/compressed", "draw_points", "draw_roundRect");
        image.setMessageTypes(sensor_msgs.CompressedImage._TYPE, Float32MultiArray._TYPE, Float32MultiArray._TYPE);
        image.setMessageToBitmapCallable(new BitmapFromCompressedImage());
        image.setMessageToPointsCallable(new MessageCallable<float[], Float32MultiArray>() {
            @Override
            public float[] call(Float32MultiArray message) {
                return message.getData();
            }
        });
        image.setMessageToRectsCallable(new MessageCallable<ArrayList<RectF>, Float32MultiArray>() {
            @Override
            public ArrayList<RectF> call(Float32MultiArray message) {
                ArrayList<RectF> result = Lists.newArrayList();
                for (int i = 0; i < message.getData().length / 4; i++) {
                    result.add(new RectF(message.getData()[4 * i], message.getData()[4 * i + 1], message.getData()[4 * i + 2], message.getData()[4 * i + 3]));
                }
                return result;
            }
        });

        image.setOnTouchListener(flEyeGestureListener);

        galleryBtn = (ImageView) findViewById(R.id.button_gallery);

//        batteryPercent = (RosTextView<Int32>) findViewById(R.id.rosTextView_batteryPercent);
//        batteryPercent.setTopicName("bebop/battery");//"fleye/battery");
//        batteryPercent.setMessageType(Int32._TYPE);
//        batteryPercent.setMessageToStringCallable(new MessageCallable<java.lang.String, Int32>() {
//            @Override
//            public java.lang.String call(Int32 message) {
//                return "Battery : " + (int) message.getData() + "%";
//            }
//        });

        tleBtn = (RosButton) findViewById(R.id.button_takeoff_land_emergency);
        tleBtn.setTopicName("fleye/takeoff_land_emergency");
        tleBtn.setIds('l', 'L');
        tleBtn.addResource("takeoff", R.drawable.takeoff_120x120);
        tleBtn.addResource("land", R.drawable.land_120x120);
//
//        sprBtn = (RosButton) findViewById(R.id.button_start_pause_resume);
//        sprBtn.setTopicName("fleye/start_pause_resume");
//        sprBtn.setIds('r', 'R');
//        sprBtn.addResource("go", R.drawable.start_120x120);
//        sprBtn.addResource("orbit", R.drawable.orbit_120x120);
//        sprBtn.addResource("pano", R.drawable.pano_120x120);
//        sprBtn.addResource("zigzag", R.drawable.zigzag_120x120);
//
//        plusBtn = (RosButton) findViewById(R.id.button_plus);
//        plusBtn.setTopicName("fleye/plus");
//        plusBtn.setIds('p', 'P');
//        plusBtn.addResource("plus", R.drawable.plus_120x120);
//
//        minusBtn = (RosButton) findViewById(R.id.button_minus);
//        minusBtn.setTopicName("fleye/minus");
//        minusBtn.setIds('m', 'M');
//        minusBtn.addResource("minus", R.drawable.minus_120x120);

        orbitBtn = (RosButton) findViewById(R.id.button_orbit);
        orbitBtn.setTopicName("fleye/orbit");
        orbitBtn.setIds('o', 'O');
        orbitBtn.addResource("orbit", R.drawable.orbit_120x120);

        panoBtn = (RosButton) findViewById(R.id.button_pano);
        panoBtn.setTopicName("fleye/pano");
        panoBtn.setIds('p', 'P');
        panoBtn.addResource("pano", R.drawable.pano_120x120);

        zigzagBtn = (RosButton) findViewById(R.id.button_zigzag);
        zigzagBtn.setTopicName("fleye/zigzag");
        zigzagBtn.setIds('g', 'G');
        zigzagBtn.addResource("zigzag", R.drawable.zigzag_120x120);

        shootBtn = (RosScreenShotButton) findViewById(R.id.button_shoot);
        shootBtn.setRunnable(new Runnable() {
            @Override
            public void run() {
                String fileName = GlobalFunc.saveImageToExternalStorage(MainActivity.this, shootBtn.getScreenShot());
                if (fileName != null) {
                    GlobalFunc.savePose(fileName, shootBtn.getPose());
                    galleryBtn.setImageBitmap(GlobalFunc.loadImageFromExternalStorage(GlobalFunc.getImageCount() - 1, 400, 225));
                }
                if (GlobalFunc.gridView != null) {
                    ((ImageAdapter) GlobalFunc.gridView.getAdapter()).notifyDataSetChanged();
                }
            }
        });
    }

    public void galleryButtonClicked(View unused) {
        Intent intent = new Intent(getApplicationContext(), GalleryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("tle", tleBtn.getResourceName());
//        intent.putExtra("spr", sprBtn.getText());
//        intent.putExtra("isSprVisible", sprBtn.getVisibility() == View.VISIBLE);
        startActivity(intent);
        overridePendingTransition(0,0);
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        NodeConfiguration nodeConfiguration =
                NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress(),
                        getMasterUri());
//        nodeMainExecutor.execute(batteryPercent, nodeConfiguration.setNodeName("android/battery"));
        nodeMainExecutor.execute(image, nodeConfiguration.setNodeName("android/videoView"));
        nodeMainExecutor.execute(tleBtn, nodeConfiguration.setNodeName("android/tleBtn"));
//        nodeMainExecutor.execute(sprBtn, nodeConfiguration.setNodeName("android/sprBtn"));
//        nodeMainExecutor.execute(plusBtn, nodeConfiguration.setNodeName("android/plusBtn"));
//        nodeMainExecutor.execute(minusBtn, nodeConfiguration.setNodeName("android/minusBtn"));
        nodeMainExecutor.execute(panoBtn, nodeConfiguration.setNodeName("android/panoBtn"));
        nodeMainExecutor.execute(orbitBtn, nodeConfiguration.setNodeName("android/orbitBtn"));
        nodeMainExecutor.execute(zigzagBtn, nodeConfiguration.setNodeName("android/zigzagBtn"));
        nodeMainExecutor.execute(shootBtn, nodeConfiguration.setNodeName("android/shootBtn"));
        nodeMainExecutor.execute(flEyeGestureListener, nodeConfiguration.setNodeName("android/gesture"));
    }

    @Override
    public void onResume() {
        super.onResume();
        super.splashScreen();
        if (getIntent().getExtras() != null) {
            tleBtn.setResourceName(getIntent().getExtras().getString("tle"));
//            sprBtn.setText(getIntent().getExtras().getString("spr"));
//            sprBtn.setVisibility(getIntent().getExtras().getBoolean("isSprVisible") ? View.VISIBLE : View.INVISIBLE);
        }
    }
}

