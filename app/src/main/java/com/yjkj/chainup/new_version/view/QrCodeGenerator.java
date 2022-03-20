package com.yjkj.chainup.new_version.view;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

/**
 * @Author lianshangljl
 * @Date 2020/11/16-10:14 AM
 * @Email buptjinlong@163.com
 * @description
 */
public class QrCodeGenerator {
    public static Bitmap generateBitmap(String content, int width, int height, boolean needDeleteWhiteBorder) {
        try {
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            if (needDeleteWhiteBorder) {
                matrix = deleteWhite(matrix);//删除白边
            }

            width = matrix.getWidth();
            height = matrix.getHeight();
            int[] pixels = new int[width * height];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (matrix.get(x, y)) {
                        pixels[y * width + x] = Color.BLACK;
                    } else {
                        pixels[y * width + x] = Color.WHITE;
                    }
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    private static BitMatrix deleteWhite(BitMatrix matrix) {
        int[] rec = matrix.getEnclosingRectangle();
        int resWidth = rec[2] + 1;
        int resHeight = rec[3] + 1;

        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
        resMatrix.clear();
        for (int i = 0; i < resWidth; i++) {
            for (int j = 0; j < resHeight; j++) {
                if (matrix.get(i + rec[0], j + rec[1]))
                    resMatrix.set(i, j);
            }
        }
        return resMatrix;
    }

    public static String decodeQrCode(Bitmap bm) {
        String contents = null;

        int[] intArray = new int[bm.getWidth() * bm.getHeight()];
        bm.getPixels(intArray, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bm.getWidth(), bm.getHeight(), intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Reader reader = new MultiFormatReader();// use this otherwise ChecksumException
        try {
            Result result = reader.decode(bitmap);
            contents = result.getText();
            //byte[] rawBytes = result.getRawBytes();
            //BarcodeFormat format = result.getBarcodeFormat();
            //ResultPoint[] points = result.getResultPoints();
        } catch (Exception ignored) {
        }
        return contents;
    }
}
