package org.ros.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import org.ros.android.BitmapFromCompressedImage;
import org.ros.android.MessageCallable;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;

import java.util.ArrayList;

/**
 * Created by ziquan on 2/13/15.
 */
public class RosHybridImageView<T, S, U> extends ImageView implements NodeMain {
    private String topicName1, topicName2, topicName3;
    private String messageType1, messageType2, messageType3;
    private MessageCallable<Bitmap, T> callable1;   // for image
    private MessageCallable<float[], S> callable2;  // for points
    private MessageCallable<ArrayList<RectF>, U> callable3;  // for rects

    private Bitmap bitmap_copy;

    private static float[] points;
    private static RectF targetRect;   // [left, top, right, bottom]
    private static ArrayList<RectF> otherRects;  // [left, top, right, bottom, ...]

    private final Paint pointPaint, targetRectPaint, otherRectPaint, crossPaint;
    private final Paint redMaskPaint, greenMaskPaint, blueMaskPaint, orangeMaskPaint;

    private final float crossSize = 14f, maskSize = 75f, maskVerticalMargin = 100f, maskHorizontalMargin = 180f;

    private final static boolean toDrawMask = false;

    public RosHybridImageView(Context context) {
        super(context);
        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setARGB(255,255,255,255);
        pointPaint.setStrokeWidth(1.5f);

        targetRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        targetRectPaint.setARGB(255,255,165,0);
        targetRectPaint.setStrokeWidth(1.5f);
        targetRectPaint.setStyle(Paint.Style.STROKE);

        otherRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        otherRectPaint.setARGB(255, 77, 166, 255);
        otherRectPaint.setStrokeWidth(1.5f);
        otherRectPaint.setStyle(Paint.Style.STROKE);
        otherRectPaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));

        crossPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        crossPaint.setARGB(255, 255, 0, 0);
        crossPaint.setStrokeWidth(1.5f);
        crossPaint.setStyle(Paint.Style.STROKE);

        redMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        redMaskPaint.setARGB(100, 255, 0, 0);
        redMaskPaint.setStrokeWidth(1.5f);
        redMaskPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        greenMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        greenMaskPaint.setARGB(100, 0, 255, 0);
        greenMaskPaint.setStrokeWidth(1.5f);
        greenMaskPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        blueMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blueMaskPaint.setARGB(100, 0, 0, 255);
        blueMaskPaint.setStrokeWidth(1.5f);
        blueMaskPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        orangeMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        orangeMaskPaint.setARGB(100, 255, 165, 0);
        orangeMaskPaint.setStrokeWidth(1.5f);
        orangeMaskPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    public RosHybridImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setARGB(255, 255, 255, 255);
        pointPaint.setStrokeWidth(1.5f);

        targetRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        targetRectPaint.setARGB(255,255,165,0);
        targetRectPaint.setStrokeWidth(1.5f);
        targetRectPaint.setStyle(Paint.Style.STROKE);

        otherRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        otherRectPaint.setARGB(255, 77, 166, 255);
        otherRectPaint.setStrokeWidth(1.5f);
        otherRectPaint.setStyle(Paint.Style.STROKE);
        otherRectPaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));

        crossPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        crossPaint.setARGB(255, 255, 0, 0);
        crossPaint.setStrokeWidth(1.5f);
        crossPaint.setStyle(Paint.Style.STROKE);

        redMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        redMaskPaint.setARGB(100, 255, 0, 0);
        redMaskPaint.setStrokeWidth(1.5f);
        redMaskPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        greenMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        greenMaskPaint.setARGB(100, 0, 255, 0);
        greenMaskPaint.setStrokeWidth(1.5f);
        greenMaskPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        blueMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blueMaskPaint.setARGB(100, 0, 0, 255);
        blueMaskPaint.setStrokeWidth(1.5f);
        blueMaskPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        orangeMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        orangeMaskPaint.setARGB(100, 255, 165, 0);
        orangeMaskPaint.setStrokeWidth(1.5f);
        orangeMaskPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    public RosHybridImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setARGB(255, 255, 255, 255);
        pointPaint.setStrokeWidth(1.5f);

        targetRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        targetRectPaint.setARGB(255, 255, 165, 0);
        targetRectPaint.setStrokeWidth(1.5f);
        targetRectPaint.setStyle(Paint.Style.STROKE);

        otherRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        otherRectPaint.setARGB(255, 77, 166, 255);
        otherRectPaint.setStrokeWidth(1.8f);
        otherRectPaint.setStyle(Paint.Style.STROKE);
        otherRectPaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));

        crossPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        crossPaint.setARGB(255, 255, 0, 0);
        crossPaint.setStrokeWidth(1.5f);
        crossPaint.setStyle(Paint.Style.STROKE);

        redMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        redMaskPaint.setARGB(100, 255, 0, 0);
        redMaskPaint.setStrokeWidth(1.5f);
        redMaskPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        greenMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        greenMaskPaint.setARGB(100, 0, 255, 0);
        greenMaskPaint.setStrokeWidth(1.5f);
        greenMaskPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        blueMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blueMaskPaint.setARGB(100, 0, 0, 255);
        blueMaskPaint.setStrokeWidth(1.5f);
        blueMaskPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        orangeMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        orangeMaskPaint.setARGB(100, 255, 165, 0);
        orangeMaskPaint.setStrokeWidth(1.5f);
        orangeMaskPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    public void setTopicNames(String topicName1, String topicName2, String topicName3) {
        this.topicName1 = topicName1;
        this.topicName2 = topicName2;
        this.topicName3 = topicName3;
    }

    public void setMessageTypes(String messageType1, String messageType2, String messageType3) {
        this.messageType1 = messageType1;
        this.messageType2 = messageType2;
        this.messageType3 = messageType3;
    }

    public void setMessageToBitmapCallable(MessageCallable<Bitmap, T> callable) {
        this.callable1 = callable;
    }

    public void setMessageToPointsCallable(MessageCallable<float[], S> callable) {
        this.callable2 = callable;
    }

    public void setMessageToRectsCallable(MessageCallable<ArrayList<RectF>, U> callable) {
        this.callable3 = callable;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("ros_hybrid_image_view");
    }

    public Bitmap getBitmap_copy() {
        return bitmap_copy;
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        Subscriber<T> subscriber1 = connectedNode.newSubscriber(topicName1, messageType1);
        Subscriber<S> subscriber2 = connectedNode.newSubscriber(topicName2, messageType2);
        Subscriber<U> subscriber3 = connectedNode.newSubscriber(topicName3, messageType3);
        subscriber1.addMessageListener(new MessageListener<T>() {
            @Override
            public void onNewMessage(final T message) {
                post(new Runnable() {
                    @Override
                    public void run() {
//            System.out.println("get message " + ((sensor_msgs.CompressedImage)message).getHeader().getSeq());
//            if (((sensor_msgs.CompressedImage)message).getHeader().getSeq() % 2 > 0)
//              return;
//            System.out.println("get message " + ((sensor_msgs.CompressedImage)message).getHeader().getSeq());
//            long timer = System.currentTimeMillis();
//            Bitmap temp = callable.call(message);
//            duration_1 = System.currentTimeMillis() - timer;
//            timer = System.currentTimeMillis();
//            setImageBitmap(temp);
//            duration_2 = System.currentTimeMillis() - timer;
//            System.out.println("Call " + duration_1 + "\tDraw " + duration_2);

//            timer = System.currentTimeMillis();
                        Bitmap bitmap = callable1.call(message);
                        ((BitmapFromCompressedImage) callable1).addBitmapBuffer(bitmap);
//                        bitmap_copy = bitmap.copy(bitmap.getConfig(), false);
                        Canvas canvas = new Canvas(bitmap);
                        if (points != null) {
//                            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//                            paint.setARGB(255,255,255,255);
//                            paint.setStrokeWidth(1.5f);
                            canvas.drawPoints(points, pointPaint);
                        }
                        if (targetRect != null) {
//                            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//                            paint.setARGB(255,255,165,0);
//                            paint.setStrokeWidth(1.5f);
//                            paint.setStyle(Paint.Style.STROKE);
                            if (toDrawMask) {
                                canvas.drawCircle(maskHorizontalMargin, maskVerticalMargin, maskSize, redMaskPaint);
                                canvas.drawCircle(640f - maskHorizontalMargin, maskVerticalMargin, maskSize, greenMaskPaint);
                                canvas.drawCircle(maskHorizontalMargin, 368f - maskVerticalMargin, maskSize, blueMaskPaint);
                                canvas.drawCircle(640f - maskHorizontalMargin, 368f - maskVerticalMargin, maskSize, orangeMaskPaint);
                            }

                            canvas.drawRoundRect(targetRect, 10f, 10f, targetRectPaint);
                            canvas.drawCircle(targetRect.right, targetRect.top, crossSize, crossPaint);
//                            canvas.drawRect(targetRect.right - crossSize, targetRect.top - crossSize, targetRect.right + crossSize, targetRect.top + crossSize, crossPaint);
                            canvas.drawLines(
                                    new float[]{targetRect.right - crossSize / (float) Math.sqrt(2), targetRect.top + crossSize / (float) Math.sqrt(2),
                                            targetRect.right + crossSize / (float)Math.sqrt(2), targetRect.top - crossSize / (float)Math.sqrt(2),
                                                targetRect.right - crossSize / (float)Math.sqrt(2), targetRect.top - crossSize / (float)Math.sqrt(2),
                                                targetRect.right + crossSize / (float)Math.sqrt(2), targetRect.top + crossSize / (float)Math.sqrt(2)},
                                    crossPaint);

                        }
                        if (otherRects != null) {
//                            for(RectF rectF : otherRects) {
//                                canvas.drawRoundRect(rectF, 10f, 10f, otherRectPaint);
//                            }
                        }
                        setImageBitmap(bitmap);
//            System.out.println("bitmap height " + bitmap.getHeight() + " width " + bitmap.getWidth());
//            System.out.println("draw " + ((sensor_msgs.CompressedImage)message).getHeader().getSeq() + " for " + (System.currentTimeMillis() - timer));

//            setImageBitmap(callable.call(message));
                    }
                });
                postInvalidate();
            }
        });

        subscriber2.addMessageListener(new MessageListener<S>() {
            @Override
            public void onNewMessage(final S message) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        points = callable2.call(message);
                    }
                });
            }
        });

        subscriber3.addMessageListener(new MessageListener<U>() {
            @Override
            public void onNewMessage(final U message) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        otherRects = callable3.call(message);
                        if (!otherRects.isEmpty()) {
                            targetRect = otherRects.get(0);
                            otherRects.remove(0);
                        } else {
                            targetRect = null;
                            otherRects = null;
                        }
                    }
                });
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
