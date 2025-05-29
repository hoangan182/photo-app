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

    public ImageStorageController(Context context) {
        this.context = context;
        // Create a directory for storing images in app's private storage
        this.storageDir = new File(context.getFilesDir(), "photos");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
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

    public String saveImage(Uri imageUri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
        if (inputStream == null) {
            throw new IOException("Could not open input stream for image");
        }

        // Get image capture date from EXIF data
        long captureDate = getImageCaptureDate(imageUri);
        
        // Check if this is a camera photo or downloaded image
        boolean isCameraPhoto = false;
        if ("content".equals(imageUri.getScheme())) {
            String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED};
            try (Cursor cursor = context.getContentResolver().query(imageUri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    int dateAddedColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);
                    
                    if (dataColumn != -1) {
                        String path = cursor.getString(dataColumn);
                        // Check if the image is from camera directory
                        isCameraPhoto = path != null && path.contains("/DCIM/Camera/");
                    }
                    
                    // For non-camera photos, use DATE_ADDED if available
                    if (!isCameraPhoto && dateAddedColumn != -1) {
                        long dateAdded = cursor.getLong(dateAddedColumn) * 1000; // Convert seconds to milliseconds
                        captureDate = dateAdded;
                    }
                }
            }
        }

        // For downloaded images, use current time if no other date is available
        if (!isCameraPhoto && captureDate == 0) {
            captureDate = System.currentTimeMillis();
        }

        // Create a unique filename
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date(captureDate));
        String imageFileName = "IMG_" + timeStamp + ".jpg";
        File imageFile = new File(storageDir, imageFileName);

        // Save the image with EXIF data
        try (FileOutputStream outputStream = new FileOutputStream(imageFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } finally {
            inputStream.close();
        }

        // Copy EXIF data to the saved file
        try {
            ExifInterface sourceExif = new ExifInterface(context.getContentResolver().openInputStream(imageUri));
            ExifInterface destExif = new ExifInterface(imageFile.getAbsolutePath());
            
            // Copy all EXIF attributes
            for (String tag : new String[]{
                ExifInterface.TAG_DATETIME_ORIGINAL,
                ExifInterface.TAG_DATETIME,
                ExifInterface.TAG_MAKE,
                ExifInterface.TAG_MODEL,
                ExifInterface.TAG_EXPOSURE_TIME,
                ExifInterface.TAG_F_NUMBER,
                ExifInterface.TAG_ISO_SPEED_RATINGS,
                ExifInterface.TAG_FOCAL_LENGTH,
                ExifInterface.TAG_GPS_LATITUDE,
                ExifInterface.TAG_GPS_LONGITUDE,
                ExifInterface.TAG_GPS_ALTITUDE
            }) {
                String value = sourceExif.getAttribute(tag);
                if (value != null) {
                    destExif.setAttribute(tag, value);
                }
            }

            // For downloaded images, set the download time as capture date
            if (!isCameraPhoto) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());
                String dateStr = sdf.format(new Date(captureDate));
                destExif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, dateStr);
                destExif.setAttribute(ExifInterface.TAG_DATETIME, dateStr);
            }

            destExif.saveAttributes();
        } catch (Exception e) {
            Log.e(TAG, "Error copying EXIF data", e);
        }

        return imageFile.getAbsolutePath();
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