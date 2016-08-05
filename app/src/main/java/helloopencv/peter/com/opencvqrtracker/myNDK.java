package helloopencv.peter.com.opencvqrtracker;

/**
 * Created by linweijie on 7/12/16.
 */
public class myNDK {

    // 讀取函式庫
    static {
        System.loadLibrary("myJNI");
    }

    // 宣告由C/C++實作的方法
    public native String jni_HelloJni(String toWhat);

    // 簡易特徵點標示
    public native void jni_FeatureDetector(long addrGray, long addrRgba, long addrDescriptor);

    // 繪製輪廓
    public native void jni_GrayDenoisingThresholdContour(long orgImage);

    // QrTracking
    public native int jni_QrTracking(long orgImage, long[] qrImages, int minThreshold, int maxThreshold, boolean isThreshold, boolean isBalanceWhite, double whiteBalance);

    public native void jni_QrDrawing(long orgImage, int count, String qrCode);


    public native boolean jni_ImageMatching(long orgImage, long tmpImage);


    // 開發測試
    public native double jni_ImageMatching_test(long orgImage, long tmpImage);

    public native boolean jni_FeatureMatching_test(long objImage, long sceneImage, long matchImage);

}
