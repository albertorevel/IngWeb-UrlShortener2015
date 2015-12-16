package urlshortener2015.fuzzywuzzy.web;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.Map;

import static org.apache.tomcat.util.codec.binary.Base64.encodeBase64String;

/**
 * Created by Alberto on 16/12/2015.
 */

// tamaño = chs
// correction = chld
// content = chl
//encoding = choe

public class QrGenerator {
    private enum correction {
        L,
        M,
        Q,
        H
    }

    private final String googleApiBase = "https://chart.googleapis.com/chart?&cht=qr";
    private int xSize = 150;
    private int ySize = 150;
    private String encoding;
    private correction correctionLevel = correction.L;
    private String uri;
    private String vCardName = null;
    private String content = null;
    private String urlContent = null;
    private int backGroundColor = 0xFFFFFF;
    private int foreGroundColor = 0;

    /**
     * @param xSize    width of the QrCode
     * @param ySize    height of the QrCode
     * @param encoding encoding for the QR (UTF-8, for example)
     * @param uri      the uri to be encoded
     */
    public QrGenerator(int xSize, int ySize, String encoding, String uri) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.encoding = encoding;
        this.uri = uri;
        setContent();
    }

    public QrGenerator(int xSize, int ySize, String encoding, String uri, char correctionLevel) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.encoding = encoding;
        this.uri = uri;
        this.correctionLevel = getCorrection();
        setContent();
    }

    public QrGenerator(int xSize, int ySize, String encoding, String uri, String vCardName) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.encoding = encoding;
        this.uri = uri;
        this.vCardName = vCardName;
        setContent();
    }

    public QrGenerator(int xSize, int ySize, String encoding, char correctionLevel, String uri, String vCardName) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.encoding = encoding;
        this.correctionLevel = getCorrection();
        this.uri = uri;
        this.vCardName = vCardName;
        setContent();
    }

    public QrGenerator(int xSize, int ySize, String encoding, String uri, int backGroundColor, int foreGroundColor) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.encoding = encoding;
        this.uri = uri;
        this.backGroundColor = backGroundColor;
        this.foreGroundColor = foreGroundColor;
        setContent();
    }

    public QrGenerator(int xSize, int ySize, String encoding, String uri, int backGroundColor, int foreGroundColor, char correctionLevel) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.encoding = encoding;
        this.uri = uri;
        this.backGroundColor = backGroundColor;
        this.foreGroundColor = foreGroundColor;
        this.correctionLevel = getCorrection();
        setContent();
    }

    public QrGenerator(int xSize, int ySize, String encoding, String uri, String vCardName, int backGroundColor, int foreGroundColor) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.encoding = encoding;
        this.uri = uri;
        this.vCardName = vCardName;
        this.backGroundColor = backGroundColor;
        this.foreGroundColor = foreGroundColor;
        setContent();
    }


    /**
     * @param xSize           width of the QrCode
     * @param ySize           height of the QrCode
     * @param encoding        encoding for the QR (UTF-8, for example)
     * @param correctionLevel the correction level
     * @param uri             the uri to be encoded
     * @param vCardName       the name of the vCard to be encoded
     * @param backGroundColor background color of the QrCode
     * @param foreGroundColor foreground color of the QrCode
     */
    public QrGenerator(int xSize, int ySize, String encoding, char correctionLevel, String uri, String vCardName,
                       int backGroundColor, int foreGroundColor) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.encoding = encoding;
        this.correctionLevel = getCorrection();
        this.uri = uri;
        this.vCardName = vCardName;
        this.backGroundColor = backGroundColor;
        this.foreGroundColor = foreGroundColor;
        setContent();
    }

    /**
     * @return an Google Charts Api query to create a QR with specified information
     */
    public String getQrApi() {
        String api = googleApiBase;             // Api base
        api += "&chs=" + xSize + "x" + ySize;   // Qr size
        api += "&choe" + encoding;              // Qr encoding
        if (correctionLevel != null) {
            api += "&chld=" + correctionLevel;      // Qr correction level
        }
        api += "&chl=" + urlContent;            // Qr content
        return api;
    }

    /**
     * It creates teh Qr code
     *
     * @return the Qr code encoded in Base64
     */
    public String getEncodedQr() {
        BitMatrix matrix = null;
        Writer writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new Hashtable<>();
        ErrorCorrectionLevel errorCorrectionLevel;
        switch (correctionLevel) {
            case M:
                    errorCorrectionLevel = ErrorCorrectionLevel.M;
                    break;
                case Q:
                    errorCorrectionLevel = ErrorCorrectionLevel.Q;
                    break;
                case H:
                    errorCorrectionLevel = ErrorCorrectionLevel.H;
                    break;
                default:
                    errorCorrectionLevel = ErrorCorrectionLevel.L;
                    break;
            }

        hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel);



        try {
            matrix = writer.encode(content, BarcodeFormat.QR_CODE, xSize, ySize, hints);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        BufferedImage image = new BufferedImage(xSize, ySize, BufferedImage.TYPE_INT_RGB);
        // Iterate through the matrix and draw the pixels to the image
        for (int y = 0; y < ySize; y++) {
            for (int x = 0; x < xSize; x++) {
                int grayValue = (matrix.get(x, y) ? 0 : 1) & 0xff;
                image.setRGB(x, y, (grayValue == 0 ? foreGroundColor : backGroundColor));
            }
        }
//
//        try {
//            FileOutputStream qrCode = new FileOutputStream("W:/qrcode.png");
//            ImageIO.write(image, "png", qrCode);
//            qrCode.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        byte[] byteArray = new byte[0];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
            byteArray = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return encodeBase64String(byteArray);
    }

    /**
     *
     */
    private correction getCorrection() {
        switch (correctionLevel) {
            case M:
                return correction.M;
            case Q:
                return correction.Q;
            case H:
                return correction.H;
            default:
                return correction.L;
        }
    }

    /**
     * Method that set the data to be encoded in the QR code.
     * If there's info to create a vCard, the info would be putted to be encoded in the QR.
     * If not, only the URL would be putted.
     */
    private void setContent() {

        if (vCardName != null) {
            content = "BEGIN:VCARD\nVERSION:4.0\nN:" + vCardName + "\nURL:" + uri + "\nEND:VCARD";
        } else {
            content = uri;
        }

        try {
            urlContent = URLEncoder.encode(content, encoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            urlContent = "";
        }
    }
}
