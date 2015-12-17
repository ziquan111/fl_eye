package org.ros.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.google.common.collect.Maps;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import java.lang.String;
import java.util.Map;

import std_msgs.*;

/**
 * Created by ziquan on 2/27/15.
 */
public class RosButton extends RosImageView {

    private char id, idOnLongPress;
    private Publisher<Float32MultiArray> publisher; // all RosButtons publish to a fixed topic "fleye/gesture"
    private String topicName;                       // but subscribe to different topics
    private String resourceName;
    private Map<String, Integer> map;

    public RosButton(Context context) {
        super(context);
        map = Maps.newHashMap();
        this.resourceName = "";
    }

    public RosButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        map = Maps.newHashMap();
        this.resourceName = "";
    }

    public RosButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        map = Maps.newHashMap();
        this.resourceName = "";
    }

    public void setIds(char id, char idOnLongPress) {
        this.id = id;
        this.idOnLongPress = idOnLongPress;
    }

    @Override
    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }


    public void addResource(String name, int resId) {
        this.map.put(name, resId);
    }

    public String getResourceName() {
        return this.resourceName;
    }

    public void setResourceName(String resourceName) {
        System.out.println(this.resourceName);
        if (resourceName.equals("")) {
            setVisibility(View.INVISIBLE);
        }
        else {
            setVisibility(View.VISIBLE);
            setImageResource(map.get(resourceName));
        }
    }

//    public void setMessageType(String messageType) {
//        this.messageType = messageType;
//    }

//    @Override
//    public void setMessageToStringCallable(MessageCallable callable) {
//        this.callable = callable;
//    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("android_gingerbread/ros_button");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        publisher = connectedNode.newPublisher("fleye/gesture", Float32MultiArray._TYPE);
        setOnClickListener(new OnClickListener() {
            public void onClick(View unused) {
                Float32MultiArray array = publisher.newMessage();
                array.setData(new float[]{id});
                publisher.publish(array);
            }
        });

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View unused) {
                Float32MultiArray array = publisher.newMessage();
                array.setData(new float[]{idOnLongPress});
                publisher.publish(array);
                return true;
            }
        });

        Subscriber<std_msgs.String> subscriber = connectedNode.newSubscriber(topicName, std_msgs.String._TYPE);
        subscriber.addMessageListener(new MessageListener<std_msgs.String>() {
            @Override
            public void onNewMessage(final std_msgs.String message) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        setResourceName(message.getData().toString());
//                        setVisibility(message.getData().toString().equals("") ? View.INVISIBLE : View.VISIBLE);
//                        setImageResource(R.drawable.);
//                        setText(message.getData().toString());
                    }
                });
                postInvalidate();
            }
        });
    }
}
