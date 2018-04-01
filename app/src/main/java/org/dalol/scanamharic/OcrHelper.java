package org.dalol.scanamharic;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import com.cv4j.core.binary.Threshold;
import com.cv4j.core.datamodel.ByteProcessor;
import com.cv4j.core.datamodel.CV4JImage;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.content.ContentValues.TAG;

/**
 * @author Filippo Engidashet [filippo.eng@gmail.com]
 * @version 1.0.0
 * @since Saturday, 24/03/2018 at 12:41.
 */

public class OcrHelper {

    private static final String DATA_PATH = "TesseractSample";
    private static final String TESSDATA = "tessdata";
    private static final String LANG = "amh";

    private TessBaseAPI tessBaseApi;


    public void init(Context context) {
        try {
            File dir = context.getDir(DATA_PATH, Context.MODE_PRIVATE);
            String path = dir.getAbsolutePath() + File.separator + TESSDATA;
            prepareDirectory(path);
        } catch (Exception e) {
            e.printStackTrace();
        }

        copyTessDataFiles(context);
    }

    private void prepareDirectory(String path) {

        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "ERROR: Creation of directory " + path + " failed, check does Android Manifest have permission to write to external storage.");
            }
        } else {
            Log.i(TAG, "Created directory " + path);
        }
    }

    private void copyTessDataFiles(Context context) {
        try {
            AssetManager assets = context.getAssets();
            String fileList[] = assets.list(TESSDATA);

            File dir = context.getDir(DATA_PATH, Context.MODE_PRIVATE);
            String absolutePath = dir.getAbsolutePath() + File.separator + TESSDATA;

            for (String fileName : fileList) {


                // open file within the assets folder
                // if it is not already there copy it to the sdcard
                String pathToDataFile = absolutePath + "/" + fileName;
                if (!(new File(pathToDataFile)).exists()) {

                    InputStream in = assets.open(TESSDATA + "/" + fileName);

                    OutputStream out = new FileOutputStream(pathToDataFile);

                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;

                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();

                    Log.d(TAG, "Copied " + fileName + "to tessdata");
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to copy files to tessdata " + e.toString());
        }
    }

    public void recognize(Context context, Bitmap bitmap) {

        CV4JImage cv4JImage = new CV4JImage(bitmap);
        Threshold threshold = new Threshold();
        threshold.adaptiveThresh((ByteProcessor)(cv4JImage.convert2Gray().getProcessor()), Threshold.ADAPTIVE_C_MEANS_THRESH, 12, 30, Threshold.METHOD_THRESH_BINARY);
        Bitmap newBitmap = cv4JImage.getProcessor().getImage().toBitmap(Bitmap.Config.ARGB_8888);

        System.out.println("OutFilippo -- > " + extractText(context, newBitmap));
    }

    private String extractText(Context context, Bitmap bitmap) {
        try {
            tessBaseApi = new TessBaseAPI();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            if (tessBaseApi == null) {
                Log.e(TAG, "TessBaseAPI is null. TessFactory not returning tess object.");
            }
        }

        String absolutePath = context.getDir(DATA_PATH, Context.MODE_PRIVATE).getAbsolutePath();

        tessBaseApi.init(absolutePath, LANG);

        Log.d(TAG, "Training file loaded");
        tessBaseApi.setImage(bitmap);
        String extractedText = "empty result";
        try {
            extractedText = tessBaseApi.getUTF8Text();
        } catch (Exception e) {
            Log.e(TAG, "Error in recognizing text.");
        }
        tessBaseApi.end();
        return extractedText;
    }
}
