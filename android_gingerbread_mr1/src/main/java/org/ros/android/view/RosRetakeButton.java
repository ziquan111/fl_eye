package org.ros.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

import org.jboss.netty.buffer.ChannelBuffer;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import java.util.concurrent.Callable;

import sensor_msgs.CompressedImage;
import std_msgs.Float32MultiArray;

/**
 * Created by ziquan on 3/1/15.
 */
public class RosRetakeButton extends ImageButton implements NodeMain {
    private Publisher<Float32MultiArray> publisher;
    private Callable<float[]> callable;
    private Runnable runnable;

    public RosRetakeButton(Context context) {
        super(context);
    }

    public RosRetakeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RosRetakeButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("ros_screen_shot_btn");
    }

    public void setCallable(Callable<float[]> callable) {
        this.callable = callable;
    }

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        publisher = connectedNode.newPublisher("fleye/gesture", Float32MultiArray._TYPE);
        setOnClickListener(new OnClickListener() {
            public void onClick(View unused) {
                try {
                    Float32MultiArray array = publisher.newMessage();
                    array.setData(callable.call());
                    System.out.println("=================" + callable.call()[0] + " " + callable.call()[1] + " " + callable.call()[2] + " " + callable.call()[3]);
                    publisher.publish(array);
                    runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
