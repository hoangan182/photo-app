package com.example.photobooth.controllers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.database.Cursor;

import com.example.photobooth.models.PhotoItem;
import com.example.photobooth.services.ImageUploadService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImageStorageController {
    private static final String TAG = "ImageStorageController";
    private static final int MAX_IMAGE_DIMENSION = 2048;
    private final Context context;
    private final File storageDir;
    private final ImageUploadService imageUploadService;

    public ImageStorageController(Context context) {
        this.context = context;
        // Create a directory for storing images in app's private storage
        this.storageDir = new File(context.getFilesDir(), "photos");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        this.imageUploadService = new ImageUploadService(context);
    }

    public File getStorageDir() {
        return storageDir;
    }

    public List<PhotoItem> loadImages() {
        List<PhotoItem> photos = new ArrayList<>();
        File[] files = storageDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && isImageFile(file.getName())) {
                    long captureDate = getImageCaptureDate(file);
                    photos.add(new PhotoItem(file.getAbsolutePath(), captureDate));
                }
            }
        }
        return photos;
    }

    private long getImageCaptureDate(File file) {
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            String dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);
            if (dateTime == null) {
                dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME);
            }
            if (dateTime != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());
                try {
                    Date date = sdf.parse(dateTime);
                    if (date != null) {
                        return date.getTime();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing date: " + dateTime, e);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading EXIF data", e);
        }
        return file.lastModified();
    }

    private boolean isImageFile(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".jpg") || lowerCaseName.endsWith(".jpeg") || 
               lowerCaseName.endsWith(".png") || lowerCaseName.endsWith(".gif");
    }

    public void saveImage(Uri imageUri, ImageUploadService.UploadCallback callback) {
        try {
            String mimeType = context.getContentResolver().getType(imageUri);
            imageUploadService.uploadImage(imageUri, mimeType, callback);
        } catch (Exception e) {
            Log.e(TAG, "Error saving image", e);
            callback.onError("Error saving image: " + e.getMessage());
        }
    }

    private long getImageCaptureDate(Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                ExifInterface exif = new ExifInterface(inputStream);
                String dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);
                if (dateTime == null) {
                    dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME);
                }
                if (dateTime != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());
                    try {
                        Date date = sdf.parse(dateTime);
                        if (date != null) {
                            return date.getTime();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing date: " + dateTime, e);
                    }
                }
                inputStream.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading EXIF data from URI", e);
        }
        return 0;
    }

    public void deleteImage(String imagePath) {
        File file = new File(imagePath);
        if (file.exists()) {
            file.delete();
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float ratio = (float) width / height;

        if (width > height) {
            width = MAX_IMAGE_DIMENSION;
            height = (int) (width / ratio);
        } else {
            height = MAX_IMAGE_DIMENSION;
            width = (int) (height * ratio);
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true);
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