package com.mit.bodhiq.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for QR code generation
 */
public class QrUtil {
    
    private static final int DEFAULT_SIZE = 1000;
    private static final int MARGIN = 1;
    private static final float LOGO_SIZE_RATIO = 0.15f; // 15% of QR width
    
    /**
     * Generate QR code bitmap
     */
    public static Bitmap generate(String payload, int sizePx, boolean addLogo, Context context) {
        try {
            // Configure QR code hints
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, MARGIN);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            
            // Generate QR code
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(payload, BarcodeFormat.QR_CODE, sizePx, sizePx, hints);
            
            // Create bitmap
            Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.RGB_565);
            for (int x = 0; x < sizePx; x++) {
                for (int y = 0; y < sizePx; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            
            // Add logo if requested
            if (addLogo && context != null) {
                bitmap = addLogoToQr(bitmap, context);
            }
            
            return bitmap;
            
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Generate QR code with default size
     */
    public static Bitmap generate(String payload, boolean addLogo, Context context) {
        return generate(payload, DEFAULT_SIZE, addLogo, context);
    }
    
    /**
     * Add logo overlay to QR code
     */
    private static Bitmap addLogoToQr(Bitmap qrBitmap, Context context) {
        try {
            // Load app icon as logo
            int logoResourceId = context.getApplicationInfo().icon;
            Bitmap logo = BitmapFactory.decodeResource(context.getResources(), logoResourceId);
            
            if (logo == null) {
                return qrBitmap;
            }
            
            // Calculate logo size (max 15% of QR width)
            int qrWidth = qrBitmap.getWidth();
            int logoSize = (int) (qrWidth * LOGO_SIZE_RATIO);
            
            // Scale logo
            Bitmap scaledLogo = Bitmap.createScaledBitmap(logo, logoSize, logoSize, true);
            
            // Create combined bitmap
            Bitmap combined = Bitmap.createBitmap(qrWidth, qrWidth, qrBitmap.getConfig());
            Canvas canvas = new Canvas(combined);
            
            // Draw QR code
            canvas.drawBitmap(qrBitmap, 0, 0, null);
            
            // Draw white background for logo
            int logoX = (qrWidth - logoSize) / 2;
            int logoY = (qrWidth - logoSize) / 2;
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            int padding = 8;
            canvas.drawRect(
                logoX - padding,
                logoY - padding,
                logoX + logoSize + padding,
                logoY + logoSize + padding,
                paint
            );
            
            // Draw logo
            canvas.drawBitmap(scaledLogo, logoX, logoY, null);
            
            return combined;
            
        } catch (Exception e) {
            e.printStackTrace();
            return qrBitmap;
        }
    }
    
    /**
     * Get recommended QR size based on payload
     */
    public static int getRecommendedSize(String payload) {
        int payloadSize = payload.getBytes().length;
        
        if (payloadSize < 500) {
            return 800;
        } else if (payloadSize < 1500) {
            return 1000;
        } else {
            return 1200;
        }
    }
}
