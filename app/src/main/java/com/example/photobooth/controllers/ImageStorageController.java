package com.example.photobooth.controllers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageStorageController {
    private static final String TAG = "ImageStorageController";
    private static final int MAX_IMAGE_DIMENSION = 2048;
    private final Context context;
    private final File storageDir;

    public ImageStorageController(Context context) {
        this.context = context;
        // Create a directory for storing images in app's private storage
        this.storageDir = new File(context.getFilesDir(), "photos");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
    }

    public String saveImage(Uri imageUri) throws IOException {
        // Generate a unique filename using timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "PHOTO_" + timeStamp + ".jpg";
        File imageFile = new File(storageDir, imageFileName);

        try {
            // Get image dimensions first
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            BitmapFactory.decodeStream(inputStream, null, options);
            if (inputStream != null) {
                inputStream.close();
            }

            // Calculate sample size
            options.inSampleSize = calculateInSampleSize(options, MAX_IMAGE_DIMENSION, MAX_IMAGE_DIMENSION);
            options.inJustDecodeBounds = false;

            // Load the sampled bitmap
            Bitmap bitmap = null;
            try {
                inputStream = context.getContentResolver().openInputStream(imageUri);
                bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (OutOfMemoryError e) {
                Log.e(TAG, "OutOfMemoryError while loading bitmap", e);
                // Try with higher sample size if OOM occurs
                options.inSampleSize *= 2;
                inputStream = context.getContentResolver().openInputStream(imageUri);
                bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                if (inputStream != null) {
                    inputStream.close();
                }
            }

            if (bitmap == null) {
                throw new IOException("Failed to decode bitmap");
            }

            // Save the bitmap to a file
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.close();

            // Clean up
            bitmap.recycle();

            Log.d(TAG, "Image saved successfully: " + imageFile.getAbsolutePath());
            return imageFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "Error saving image", e);
            if (imageFile.exists()) {
                imageFile.delete();
            }
            throw new IOException("Error saving image: " + e.getMessage(), e);
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public boolean deleteImage(String imagePath) {
        try {
            File file = new File(imagePath);
            if (file.exists()) {
                return file.delete();
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting image", e);
            return false;
        }
    }

    public File getStorageDir() {
        return storageDir;
    }

    /**
     * Lưu ảnh đã chỉnh sửa
     * @param editedBitmap Bitmap đã chỉnh sửa
     * @return Đường dẫn của ảnh đã lưu, null nếu lưu thất bại
     */
    public String saveEditedImage(Bitmap editedBitmap) {
        if (editedBitmap == null) {
            return null;
        }

        try {
            // Tạo tên file mới với timestamp
            String fileName = "edited_" + System.currentTimeMillis() + ".jpg";
            File outputFile = new File(getStorageDir(), fileName);

            // Lưu bitmap vào file
            FileOutputStream out = new FileOutputStream(outputFile);
            editedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            return outputFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
} 