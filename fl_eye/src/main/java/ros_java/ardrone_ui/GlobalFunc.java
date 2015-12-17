package ros_java.ardrone_ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.GridView;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.ros.node.topic.Publisher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;

import std_msgs.Empty;

/**
 * Created by ziquan on 1/23/15.
 */
public class GlobalFunc {

    private final static String SD_CARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    private final static String APP_FOLDER = "fleye";
    private final static File APP_DIR = new File(SD_CARD_PATH, APP_FOLDER);
    private static int IMAGE_NAME_COUNT = 0;
    private static Map<Integer, Bitmap> images; // maps from position to image
    private static Map<String, float[]> poses; // maps from image_name to pose

    static GridView gridView;

    static {
        if (!APP_DIR.exists())
            APP_DIR.mkdirs();
        images = Maps.newHashMap();
        poses = Maps.newHashMap();
    }

    static int getImageCount() {
        return IMAGE_NAME_COUNT;
    }

    static String saveImageToExternalStorage(Context context, Bitmap image) {
        try {
            String fileName = APP_DIR + "/" + (IMAGE_NAME_COUNT++) + ".png";

            OutputStream fOut = null;
            File file = new File(fileName);
            file.createNewFile();
            fOut = new FileOutputStream(file);

            image.compress(Bitmap.CompressFormat.PNG, 100, fOut);   // 100 means no compression, the lower you go, the stronger the compression
            fOut.flush();
            fOut.close();

            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());

            return fileName;

        } catch (Exception e) {
            e.printStackTrace();
//            Log.e("saveImageToExternalStorage()", e.getMessage());
            return null;
        }
    }

    static void savePose(String imageName, float[] pose) {
        poses.put(imageName, pose);
    }

    static float[] getPose(int position) {
        return poses.get(APP_DIR + "/" + position + ".png");
    }

    static Bitmap loadImageFromExternalStorage(int position, int reqWidth, int reqHeight) {
        // cache
        if (images.containsKey(position)) {
            return images.get(position);
        }

        File imgFile = new File(APP_DIR, position + ".png");
        if (imgFile.exists()) {
            images.put(position, decodeSampledBitmapFromFile(APP_DIR + "/" + position + ".png", reqWidth, reqHeight));
            return images.get(position);
        }

        return null;
    }

    private static Bitmap decodeSampledBitmapFromFile(String pathName, int reqWidth, int reqHeight) {
        final  BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        return BitmapFactory.decodeFile(pathName, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}
