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

import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.ros.address.InetAddressFactory;
import org.ros.android.BitmapFromCompressedImage;
import org.ros.android.MessageCallable;
import org.ros.android.RosActivity;
import org.ros.android.view.RosButton;
import org.ros.android.view.RosHybridImageView;
import org.ros.android.view.RosImageView;
import org.ros.android.view.RosTextView;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import sensor_msgs.CompressedImage;
import std_msgs.Float32;
import std_msgs.Int32;

/**
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class GalleryActivity extends RosActivity {

//    private RosImageView<CompressedImage> image;
    private RosTextView<Int32> batteryPercent;
    private RosButton tleBtn;// sprBtn;

    public GalleryActivity() {
        super("GalleryTicker", "GalleryTitle");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery);

//        image = (RosImageView<CompressedImage>) findViewById(R.id.rosImageView_controlMode);
//        image.setTopicName("/ardrone/image_raw/compressed");
//        image.setMessageType(CompressedImage._TYPE);
//        image.setMessageToBitmapCallable(new BitmapFromCompressedImage());

//        batteryPercent = (RosTextView<Int32>) findViewById(R.id.rosTextView_batteryPercent);
//        batteryPercent.setTopicName("bebop/battery");
//        batteryPercent.setMessageType(Int32._TYPE);
//        batteryPercent.setMessageToStringCallable(new MessageCallable<String, Int32>() {
//            @Override
//            public java.lang.String call(Int32 message) {
//               return "Battery : " + (int)message.getData() + "%";
//            }
//        });

        tleBtn = (RosButton) findViewById(R.id.button_takeoff_land_emergency);
        tleBtn.setTopicName("fleye/takeoff_land_emergency");
        tleBtn.setIds('l', 'L');
        tleBtn.addResource("takeoff", R.drawable.takeoff_120x120);
        tleBtn.addResource("land", R.drawable.land_120x120);

//        sprBtn = (RosButton) findViewById(R.id.button_start_pause_resume);
//        sprBtn.setTopicName("fleye/start_pause_resume");
//        sprBtn.setIds('r', 'R');

        tleBtn.setResourceName(getIntent().getExtras().getString("tle"));
//        sprBtn.setText(getIntent().getExtras().getString("spr"));
//        sprBtn.setVisibility(getIntent().getExtras().getBoolean("isSprVisible") ? View.VISIBLE : View.INVISIBLE);

        GlobalFunc.gridView = (GridView) findViewById(R.id.gridView);
        GlobalFunc.gridView.setAdapter(new ImageAdapter(this));

        GlobalFunc.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Toast.makeText(GalleryActivity.this, "" + position, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), PhotoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtra("tle", tleBtn.getResourceName());
                intent.putExtra("photo", position);
//              intent.putExtra("spr", sprBtn.getText());
//              intent.putExtra("isSprVisible", sprBtn.getVisibility() == View.VISIBLE);
                startActivity(intent);
            }
        });

        if (nodeMainExecutorService != null) {
            NodeConfiguration nodeConfiguration =
                    NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress(),
                            getMasterUri());
//            nodeMainExecutorService.execute(batteryPercent, nodeConfiguration.setNodeName("android/batteryPercent2"));
            nodeMainExecutorService.execute(tleBtn, nodeConfiguration.setNodeName("android/tleBtn2"));
        }

        super.splashScreen();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    public void controlModeClicked(View unused) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("tle", tleBtn.getResourceName());
//        intent.putExtra("spr", sprBtn.getText());
//        intent.putExtra("isSprVisible", sprBtn.getVisibility() == View.VISIBLE);
        startActivity(intent);
    }

//    public void shootButtonClicked(View unused) {
//        GlobalFunc.saveImageToExternalStorage(this, ((BitmapDrawable)image.getDrawable()).getBitmap());
//        ((ImageAdapter)GlobalFunc.gridView.getAdapter()).notifyDataSetChanged();
//    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) { return ;}

    @Override
    public void onResume() {
        super.onResume();
        super.splashScreen();
        tleBtn.setResourceName(getIntent().getExtras().getString("tle"));
//        sprBtn.setText(getIntent().getExtras().getString("spr"));
//        sprBtn.setVisibility(getIntent().getExtras().getBoolean("isSprVisible") ? View.VISIBLE : View.INVISIBLE);
    }
}

