package ros_java.ardrone_ui;

import android.content.Context;
import android.graphics.RectF;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import java.util.ArrayList;

import std_msgs.Float32MultiArray;

/**
 * Created by ziquan on 1/29/15.
 *
 * Message meaning
 * Z, X|Y|x|y, [1]          scale along x or y dimension, capital letters for zooming in, small letters for zooming out, 1 means sending gesture command once otherwise keeping sending until ESC
 * I, U|D|L|R|u|d|l|r, [1]  swipe up or down or left or right, capital letters for multi finger gesture, 1 means sending gesture command once
 * U                        U shape gesture
 * A                        A shape gesture
 * N                        double tap event
 * V, x_diff, y_diff        direct manipulation, x_diff = x_new - x_old, y_diff = y_new - y_old
 *
 */
public class FlEyeGestureListener implements View.OnTouchListener, NodeMain {

    private Context context;

    private GestureDetectorCompat gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private ArrayList<float[]> trace;
    private Publisher<Float32MultiArray> publisher;

    private float scaleFactor;
    private float scaleSpanX, scaleSpanY, scaleSpan;
    private boolean isMultifingers;
    private int track_id;

    private Toast toast;

//    private boolean isLongPress;
//    private CountDownTimer timerLongPress;

    private boolean canUnderstand;

    private RectF targetRect;
    private boolean isDirectManipulation, isScaling;

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
//        @Override
//        public boolean onDoubleTap(MotionEvent e) {
//            Float32MultiArray array = publisher.newMessage();
//            array.setData(new float[] {'N'});
//            publisher.publish(array);
//            System.out.println("onDoubleTap " + (int)'N');
//
//            toast.cancel();
//            toast = Toast.makeText(context, "Double Tap: CANCEL", Toast.LENGTH_SHORT);
//            toast.show();
//
//            canUnderstand = true;
//            return true;
//        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//            int action = MotionEventCompat.getActionMasked(e2);
            int index = MotionEventCompat.getActionIndex(e2);
            int id = MotionEventCompat.getPointerId(e2, index);
//            System.out.println("onscroll");
            if (!isDirectManipulation && track_id == id) {
                trace.add(new float[]{MotionEventCompat.getX(e2, index), MotionEventCompat.getY(e2, index)});
//                if (distanceX * distanceX + distanceY * distanceY > 25) {
//                    timerLongPress.cancel();
//                    timerLongPress.start();
//                }
            }
            return true;
        }
    }

    class MyScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleSpanX = detector.getCurrentSpanX();
            scaleSpanY = detector.getCurrentSpanY();
            scaleSpan = detector.getCurrentSpan();

            if (!isScaling) {
                if (scaleFactor > 1.1 || scaleFactor < 0.9){
                    isScaling = true;
                }
                else {
                    return false;
                }
            }

            Float32MultiArray array = publisher.newMessage();
//            if (isLongPress) {
//                array.setData(new float[]{'Z', 'X', scaleSpanX / 900f});
//            } else {
            array.setData(new float[]{'Z', 'Z', scaleFactor, 1});   // 1 means once
//            }
            publisher.publish(array);
//            System.out.println("spread X " + (int)'Z' + " " + (int)'X' + " " + scaleFactor);

            toast.cancel();
            toast = Toast.makeText(context, "Zooming: " + (scaleFactor > 1? "in" : "out"), Toast.LENGTH_SHORT);
            toast.show();

            return true;
        }
    }

    public FlEyeGestureListener(Context context) {
        gestureDetector = new GestureDetectorCompat(context, new MyGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(context, new MyScaleGestureListener());
        trace = new ArrayList<float[]>();
        this.context = context;

//        isLongPress = false;
//        timerLongPress = new CountDownTimer(500, 100) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                return;
//            }
//
//            @Override
//            public void onFinish() {
//                isLongPress = true;
////                System.out.println("timer finish");
////                parseGesture();
//            }
//        };
        isDirectManipulation = false;

        toast = Toast.makeText(context, "Welcome", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        int index = MotionEventCompat.getActionIndex(event);
        int id = MotionEventCompat.getPointerId(event, index);

        if (gestureDetector.onTouchEvent(event)) {

        }

        if (scaleGestureDetector.onTouchEvent(event)) {

        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                reset();
                track_id = id;
                float pointerX = MotionEventCompat.getX(event, index);
                float pointerY = MotionEventCompat.getY(event, index);
                trace.add(new float[]{pointerX, pointerY});
                // long press on the target rect
//                System.out.println("x " + pointerX + "\ty:" + pointerY + "\t");
//                if (targetRect != null) {
//                    System.out.println("left, right, top, bottom " + targetRect.left + "\t" + targetRect.right + "\t" + targetRect.top + "\t" + targetRect.bottom);
//                }

                if (targetRect != null && pointerX / 1792f > targetRect.left / 640f
                        && pointerX / 1792f < targetRect.right / 640f
                        && pointerY / 1008f > targetRect.top / 368f
                        && pointerY / 1008f < targetRect.bottom / 368f) {
                    isDirectManipulation = true;
//                    timerLongPress.start();
//                    System.out.println("timer started");
                }

                if (targetRect != null && distanceBetween(new float[] {pointerX / 1792f * 640f, pointerY / 1008f * 368f}, new float[] {targetRect.right, targetRect.top}) < 15f) {
                    Float32MultiArray array = publisher.newMessage();
                    array.setData(new float[]{'N'});
                    publisher.publish(array);

                    toast.cancel();
                    toast = Toast.makeText(context, "CANCEL", Toast.LENGTH_SHORT);
                    toast.show();
                }
                return true;
            case MotionEvent.ACTION_POINTER_DOWN:
                isMultifingers = true;
                isDirectManipulation = false;
                return true;
//            case MotionEvent.ACTION_MOVE:
//                if (!isLongPress && track_id == id) {
//                    trace.add(new float[]{MotionEventCompat.getX(event, index), MotionEventCompat.getY(event, index)});
//                    timerLongPress.cancel();
//                    timerLongPress.start();
//                }
//                return true;
            case MotionEvent.ACTION_MOVE:
                if (isDirectManipulation && track_id == id) {
                    trace.add(new float[]{MotionEventCompat.getX(event, index), MotionEventCompat.getY(event, index)});
                }
                return true;
            case MotionEvent.ACTION_UP:
                // send one time gesture event
                parseGesture();
//                if (!isLongPress) {
//                    parseGesture();
//                }
//                // stop the long press event
//                else if (canUnderstand) {
                Float32MultiArray array = publisher.newMessage();
                array.setData(new float[] {'Q'});
                publisher.publish(array);
//                }
                reset();
                return true;
        }
        return false;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("fleye_gesture");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        publisher = connectedNode.newPublisher("fleye/gesture", Float32MultiArray._TYPE);
        // the subscriber below is added for dragging
        Subscriber<Float32MultiArray> subscriber = connectedNode.newSubscriber("draw_roundRect", Float32MultiArray._TYPE);
        subscriber.addMessageListener(new MessageListener<Float32MultiArray>() {
            @Override
            public void onNewMessage(final Float32MultiArray message) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (message.getData().length >= 4) {
                            targetRect = new RectF(message.getData()[0], message.getData()[1], message.getData()[2], message.getData()[3]);
                        } else {
                            targetRect = null;
                        }
                    }
                }).start();
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

    private double distanceBetween(float[] p1, float[] p2) {
        return Math.sqrt((p1[0] - p2[0]) * (p1[0] - p2[0]) + (p1[1] - p2[1]) * (p1[1] - p2[1]));
    }

    private float distanceX(float[] p1, float[] p2) {
        return Math.abs(p1[0] - p2[0]);
    }

    private float distanceY(float[] p1, float[] p2) {
        return Math.abs(p1[1] - p2[1]);
    }

    private float averageY() {
//        float[] result = {0, 0};
        float result = 0;
        for (float[] p : trace) {
            result += p[1];
//            result[1] += p[1];
        }
        result /= trace.size();
//        result[1] /= trace.size();
        return result;
    }

    private float averageY(float[] p1, float[] p2) {
        return (p1[1] + p2[1]) / 2;
    }

    private void reset() {
        trace.clear();
        scaleFactor = 1;
        scaleSpanX = scaleSpanY = scaleSpan = 0;
        isMultifingers = false;
        track_id = -1;
//        isLongPress = false;
//        timerLongPress.cancel();
        isDirectManipulation = false;
        canUnderstand = false;
        isScaling = false;
    }

    private void parseGesture() {
        canUnderstand = true;
        if (isScaling) {

        }
        else if (isDirectManipulation) {
            System.out.println("trace size " + trace.size());
            if (trace.size() == 1)
                System.out.println(trace.get(0)[0] + "\t" + trace.get(0)[1]);
            if (trace.size() >= 2) {
                Float32MultiArray array = publisher.newMessage();
                float x_diff = trace.get(trace.size() - 1)[0] - trace.get(0)[0];
                float y_diff = trace.get(trace.size() - 1)[1] - trace.get(0)[1];
                array.setData(new float[] {'V', x_diff / 1792f , y_diff / 1008f, 1});    // here 1 is to follow the 'once' convention
                publisher.publish(array);

                toast.cancel();
                toast = Toast.makeText(context, "Direct Manipulation", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        // circle
        else if (!isMultifingers && trace.size() >= 20 && distanceBetween(trace.get(0), trace.get(trace.size() - 1)) < 300) {
            float[] traceData = new float[trace.size() * 2];
            for (int i = 0; i < trace.size(); i++) {
                traceData[2*i] = Math.max(0, Math.min(1, trace.get(i)[0] / 1600f));
                traceData[2*i + 1] = Math.max(0, Math.min(1, trace.get(i)[1] / 900f));
            }

            Float32MultiArray array = publisher.newMessage();
            array.setData(traceData);

            publisher.publish(array);
//            System.out.println("circle " + (int)'Q');

            toast.cancel();
            toast = Toast.makeText(context, "Circle", Toast.LENGTH_SHORT);
            toast.show();
        }
        // U shape
        else if (!isMultifingers && trace.size() >= 10 && distanceY(trace.get(0), trace.get(trace.size() - 1)) < 200 && distanceX(trace.get(0), trace.get(trace.size() - 1)) > 200 && averageY() - averageY(trace.get(0), trace.get(trace.size() - 1)) >= 100) {
            Float32MultiArray array = publisher.newMessage();
            array.setData(new float[] {'U'});
            publisher.publish(array);
//            System.out.println("ushape " + (int)'U');

            toast.cancel();
            toast = Toast.makeText(context, "U shape: orbit", Toast.LENGTH_SHORT);
            toast.show();
        }
        // A shape
        else if (!isMultifingers && trace.size() >= 10 && distanceY(trace.get(0), trace.get(trace.size() - 1)) < 200 && distanceX(trace.get(0), trace.get(trace.size() - 1)) > 200 &&  averageY(trace.get(0), trace.get(trace.size() - 1)) - averageY() >= 100) {
            Float32MultiArray array = publisher.newMessage();
            array.setData(new float[] {'A'});
            publisher.publish(array);
//            System.out.println("ashape " + (int)'A');

            toast.cancel();
            toast = Toast.makeText(context, "A shape: pano", Toast.LENGTH_SHORT);
            toast.show();
        }
        // Z shape
        else if (!isMultifingers && trace.size() >= 10 && distanceX(trace.get(0), trace.get(trace.size() - 1)) > 200 && distanceY(trace.get(0), trace.get(trace.size() -1)) > 200) {
            Float32MultiArray array = publisher.newMessage();
            array.setData(new float[] {'Z'});
            publisher.publish(array);

            toast.cancel();
            toast = Toast.makeText(context, "Z shape: zigzag scan", Toast.LENGTH_SHORT);
            toast.show();
        }
        // scale
//        else if (scaleFactor > 1.1 && scaleSpanX > scaleSpanY) {
//            Float32MultiArray array = publisher.newMessage();
////            if (isLongPress) {
////                array.setData(new float[]{'Z', 'X', scaleSpanX / 900f});
////            } else {
//            array.setData(new float[]{'Z', 'X', scaleSpanX / 900f, 1});   // 1 means once
////            }
//            publisher.publish(array);
////            System.out.println("spread X " + (int)'Z' + " " + (int)'X' + " " + scaleFactor);
//
//            toast.cancel();
//            toast = Toast.makeText(context, "Horizontally Zoom +", Toast.LENGTH_SHORT);
//            toast.show();
//        }
//        else if (scaleFactor > 1.1 && scaleSpanX <= scaleSpanY) {
//            Float32MultiArray array = publisher.newMessage();
////            if (isLongPress) {
////                array.setData(new float[]{'Z', 'Y', scaleSpanY / 1600f});
////            } else {
//            array.setData(new float[]{'Z', 'Y', scaleSpanY / 1600f, 1});   // 1 means once
////            }
//            publisher.publish(array);
////            System.out.println("spread Y " + (int)'Z' + " " + (int)'Y' + " " + scaleFactor);
//
//            toast.cancel();
//            toast = Toast.makeText(context, "Vertically Zoom +", Toast.LENGTH_SHORT);
//            toast.show();
//        }
//        else if (scaleFactor < 0.9 && scaleSpanX > scaleSpanY) {
//            Float32MultiArray array = publisher.newMessage();
////            if (isLongPress) {
////                array.setData(new float[] {'Z', 'x', scaleSpanX / 900f});
////            } else {
//            array.setData(new float[]{'Z', 'x', scaleSpanX / 900f, 1});   // 1 means once
////            }
//            publisher.publish(array);
////            System.out.println("pinch X " + (int)'Z' + " " + (int)'x' + " " + scaleFactor);
//
//            toast.cancel();
//            toast = Toast.makeText(context, "Horizontally Zoom -", Toast.LENGTH_SHORT);
//            toast.show();
//        }
//        else if (scaleFactor < 0.9 && scaleSpanX <= scaleSpanY) {
//            Float32MultiArray array = publisher.newMessage();
////            if (isLongPress) {
////                array.setData(new float[]{'Z', 'y', scaleSpanY / 1600f});
////            } else {
//            array.setData(new float[]{'Z', 'y', scaleSpanY / 1600f, 1});   // 1 means once
////            }
//            publisher.publish(array);
////            System.out.println("pinch Y " + (int)'Z' + " " + (int)'y' + " " + scaleFactor);
//
//            toast.cancel();
//            toast = Toast.makeText(context, "Vertically Zoom -", Toast.LENGTH_SHORT);
//            toast.show();
//        }
        // swipe
        else if (trace.size() > 2 && distanceX(trace.get(0), trace.get(trace.size() - 1)) < 100 && trace.get(trace.size() - 1)[1] - trace.get(0)[1] >= 100) {
            Float32MultiArray array = publisher.newMessage();
//            if (isLongPress) {
//                array.setData(new float[]{'I', (isMultifingers ? 'D' : 'd'), traceLength()});
//            } else {
            array.setData(new float[]{'I', (isMultifingers ? 'D' : 'd'), traceLength(), 1});   // 1 means once
//            }
            publisher.publish(array);
//            System.out.println(isMultifingers ? "double swap down " + (int)'I' + " " + (int)'D' : "swap down " + (int)'I' + " " + (int)'d');

            toast.cancel();
            toast = Toast.makeText(context, (isMultifingers ? "Multi-finger DOWN: moving backward" : "DOWN: moving upward"), Toast.LENGTH_SHORT);
            toast.show();
        }
        else if (trace.size() > 2 && distanceX(trace.get(0), trace.get(trace.size() - 1)) < 100 && trace.get(0)[1] - trace.get(trace.size() - 1)[1] >= 100) {
            Float32MultiArray array = publisher.newMessage();
//            if (isLongPress) {
//                array.setData(new float[]{'I', (isMultifingers ? 'U' : 'u'), traceLength()});
//            } else {
            array.setData(new float[]{'I', (isMultifingers ? 'U' : 'u'), traceLength(), 1});   // 1 means once
//            }
            publisher.publish(array);
//            System.out.println(isMultifingers ? "double swap up " + (int)'I' + " " + (int)'U' : "swap up " + (int)'I' + " " + (int)'u' );

            toast.cancel();
            toast = Toast.makeText(context, (isMultifingers ? "Multi-finger UP: moving forward" : "UP: moving downward"), Toast.LENGTH_SHORT);
            toast.show();
        }
        else if (trace.size() > 2 && distanceY(trace.get(0), trace.get(trace.size() - 1)) < 100 && trace.get(trace.size() - 1)[0] - trace.get(0)[0] >= 100) {
            Float32MultiArray array = publisher.newMessage();
//            if (isLongPress) {
//                array.setData(new float[]{'I', (isMultifingers ? 'R' : 'r'), traceLength()});
//            } else {
            array.setData(new float[]{'I', (isMultifingers ? 'R' : 'r'), traceLength(), 1});   // 1 means once
//            }
            publisher.publish(array);
//            System.out.println(isMultifingers ? "double swap right " + (int)'I' + " " + (int)'R' : "swap right " + (int)'I' + " " + (int)'r' );

            toast.cancel();
            toast = Toast.makeText(context, (isMultifingers ? "Multi-finger RIGHT: turning left" : "RIGHT: moving left"), Toast.LENGTH_SHORT);
            toast.show();
        }
        else if (trace.size() > 2 && distanceY(trace.get(0), trace.get(trace.size() - 1)) < 100 && trace.get(0)[0] - trace.get(trace.size() - 1)[0] >= 100) {
            Float32MultiArray array = publisher.newMessage();
//            if (isLongPress) {
//                array.setData(new float[]{'I', (isMultifingers ? 'L' : 'l'), traceLength()});
//            } else {
            array.setData(new float[]{'I', (isMultifingers ? 'L' : 'l'), traceLength(), 1});   // 1 means once
//            }
            publisher.publish(array);
//            System.out.println(isMultifingers ? "double swap left "  + (int)'I' + " " + (int)'L'  : "swap left " + (int)'I' + " " + (int)'l' );

            toast.cancel();
            toast = Toast.makeText(context, (isMultifingers ? "Multi-finger LEFT: turning right" : "LEFT: moving right"), Toast.LENGTH_SHORT);
            toast.show();
        }
        else {
            canUnderstand = false;
        }
    }

    private float traceLength() {
        float result = 0.f;
        for (int i = 0; i < this.trace.size() - 1; i++) {
            result += distanceBetween(trace.get(i), trace.get(i+1));
        }
        return result;
    }
}
