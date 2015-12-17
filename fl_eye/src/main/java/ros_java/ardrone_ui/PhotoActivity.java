package ros_java.ardrone_ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;

import org.ros.address.InetAddressFactory;
import org.ros.android.MessageCallable;
import org.ros.android.RosActivity;
import org.ros.android.view.RosButton;
import org.ros.android.view.RosRetakeButton;
import org.ros.android.view.RosTextView;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import std_msgs.Float32;
import std_msgs.Int32;

/**
 * Created by ziquan on 3/2/15.
 */
public class PhotoActivity  extends RosActivity {

    //    private RosImageView<CompressedImage> image;
    private RosTextView<Int32> batteryPercent;
    private RosButton tleBtn;// sprBtn;
    private ImageView photo;
    private RosRetakeButton retakeBtn;
    private TextView photoHeader;

    private int photo_id;

    public PhotoActivity() {
        super("PhotoTicker", "PhotoTitle");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo);

//        batteryPercent = (RosTextView<Int32>) findViewById(R.id.rosTextView_batteryPercent);
//        batteryPercent.setTopicName("bebop/battery");
//        batteryPercent.setMessageType(Int32._TYPE);
//        batteryPercent.setMessageToStringCallable(new MessageCallable<String, Int32>() {
//            @Override
//            public java.lang.String call(Int32 message) {
//                return "Battery : " + (int)message.getData() + "%";
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

//        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//                Toast.makeText(GalleryActivity.this, "" + position, Toast.LENGTH_SHORT).show();
//            }
//        });

        photo = (ImageView) findViewById(R.id.photo);
        photo.setImageBitmap(GlobalFunc.loadImageFromExternalStorage(getIntent().getExtras().getInt("position"), photo.getWidth(), photo.getHeight()));
        photo.setOnTouchListener(new PhotoNavigationGestureListener(this) {
            public void onSwipeTop() {
//                Toast.makeText(PhotoActivity.this, "top", Toast.LENGTH_SHORT).show();
            }
            public void onSwipeRight() {
//                Toast.makeText(PhotoActivity.this, "right", Toast.LENGTH_SHORT).show();
                photo_id = Math.max(0, Math.min(GlobalFunc.getImageCount() - 1, photo_id - 1));
                photoHeader.setText("" + photo_id + ".png");
                photo.setImageBitmap(GlobalFunc.loadImageFromExternalStorage(photo_id, photo.getWidth(), photo.getHeight()));
            }
            public void onSwipeLeft() {
//                Toast.makeText(PhotoActivity.this, "left", Toast.LENGTH_SHORT).show();
                photo_id = Math.max(0, Math.min(GlobalFunc.getImageCount() - 1, photo_id + 1));
                photoHeader.setText("" + photo_id + ".png");
                photo.setImageBitmap(GlobalFunc.loadImageFromExternalStorage(photo_id, photo.getWidth(), photo.getHeight()));
            }
            public void onSwipeBottom() {
//                Toast.makeText(PhotoActivity.this, "bottom", Toast.LENGTH_SHORT).show();
            }
        });


        retakeBtn = (RosRetakeButton) findViewById(R.id.button_restore);
        retakeBtn.setCallable(new Callable<float[]>() {
            @Override
            public float[] call() throws Exception {
                float[] pose = GlobalFunc.getPose(photo_id);
                float[] result = new float[5];
                for (int i = 0; i < 4; i++) {
                    result[i] = pose[i];
                }
                result[4] = 0;
                return result;
            }
        });
        retakeBtn.setRunnable(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtra("tle", tleBtn.getResourceName());
                startActivity(intent);
            }
        });

        photoHeader = (TextView) findViewById(R.id.photoHeader);
        photoHeader.setText("" + photo_id + ".png");

        if (nodeMainExecutorService != null) {
            NodeConfiguration nodeConfiguration =
                    NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress(),
                            getMasterUri());
//            nodeMainExecutorService.execute(batteryPercent, nodeConfiguration.setNodeName("android/batteryPercent3"));
            nodeMainExecutorService.execute(tleBtn, nodeConfiguration.setNodeName("android/tleBtn3"));
            nodeMainExecutorService.execute(retakeBtn, nodeConfiguration.setNodeName("android/retakeBtn"));
        }

        super.splashScreen();
    }

    public void controlModeClicked(View unused) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("tle", tleBtn.getResourceName());
//        intent.putExtra("spr", sprBtn.getText());
//        intent.putExtra("isSprVisible", sprBtn.getVisibility() == View.VISIBLE);
        startActivity(intent);
    }

    public void galleryHeaderOnClicked(View unused) {
        Intent intent = new Intent(getApplicationContext(), GalleryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("tle", tleBtn.getResourceName());
        startActivity(intent);
    }

//    public void shootButtonClicked(View unused) {
//        GlobalFunc.saveImageToExternalStorage(this, ((BitmapDrawable)image.getDrawable()).getBitmap());
//        ((ImageAdapter)GlobalFunc.gridView.getAdapter()).notifyDataSetChanged();
//    }


//    public void retakeButtonOnClick(View unused) {
//        float[] pose = GlobalFunc.getPose(photo_id);
//        Toast.makeText(PhotoActivity.this, "" + pose[0] + " " + pose[1] + " " + pose[2] + " " + pose[3], Toast.LENGTH_SHORT).show();
//    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        return;
    }

    @Override
    public void onResume() {
        super.onResume();
        super.splashScreen();
        tleBtn.setResourceName(getIntent().getExtras().getString("tle"));
        photo_id = getIntent().getExtras().getInt("photo");
        photo.setImageBitmap(GlobalFunc.loadImageFromExternalStorage(photo_id, photo.getWidth(), photo.getHeight()));
        photoHeader.setText("" + photo_id + ".png");
//        sprBtn.setText(getIntent().getExtras().getString("spr"));
//        sprBtn.setVisibility(getIntent().getExtras().getBoolean("isSprVisible") ? View.VISIBLE : View.INVISIBLE);
    }
}