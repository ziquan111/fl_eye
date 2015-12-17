package org.ros.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

import org.jboss.netty.buffer.ChannelBuffer;
import org.ros.android.BitmapFromCompressedImage;
import org.ros.android.MessageCallable;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import java.lang.String;
import java.util.concurrent.Callable;

import sensor_msgs.CompressedImage;
import sensor_msgs.Image;
import std_msgs.*;

/**
 * Created by ziquan on 3/1/15.
 */
public class RosScreenShotButton extends ImageButton implements NodeMain {
    private Publisher<Float32MultiArray> publisher;
    private Runnable runnable;
    private Bitmap screenShot;
    private float[] pose;

    public RosScreenShotButton(Context context) {
        super(context);
    }

    public RosScreenShotButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RosScreenShotButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("ros_screen_shot_btn");
    }

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    public Bitmap getScreenShot() {
        return screenShot;
    }

    public float[] getPose() {
        return pose;
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        publisher = connectedNode.newPublisher("fleye/gesture", Float32MultiArray._TYPE);
        setOnClickListener(new OnClickListener() {
            public void onClick(View unused) {
                Float32MultiArray array = publisher.newMessage();
                array.setData(new float[]{'s'});
                publisher.publish(array);
            }
        });

        Subscriber<CompressedImage> subscriber = connectedNode.newSubscriber("fleye/screen_shot", CompressedImage._TYPE);
        subscriber.addMessageListener(new MessageListener<CompressedImage>() {
            @Override
            public void onNewMessage(final CompressedImage message) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        ChannelBuffer buffer = message.getData();
                        screenShot = BitmapFactory.decodeByteArray(buffer.array(), buffer.arrayOffset(), buffer.readableBytes());
                        runnable.run();
                    }
                });
                postInvalidate();
            }
        });

        Subscriber<Float32MultiArray> subscriber2 = connectedNode.newSubscriber("fleye/pose", Float32MultiArray._TYPE);
        subscriber2.addMessageListener(new MessageListener<Float32MultiArray>() {
            @Override
            public void onNewMessage(final Float32MultiArray message) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        pose = message.getData();
                    }
                });
                postInvalidate();
            }
        });
    }

    @Override
    public void onShutdown(Node node) {
    }

    @Override
    public void onShutdownComplete(Node node) {
    }

    @Override
    public void onError(Node node, Throwable throwable) {
    }

}
