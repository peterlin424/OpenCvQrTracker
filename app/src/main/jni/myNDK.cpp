//
// Created by 林暐傑 on 7/12/16.
//

#include <jni.h>
#include <stdio.h>

#include "helloopencv_peter_com_opencvqrtracker_myNDK.h"

#include </Users/linweijie/DevelopKit/OpenCV/Android/2.4.11/OpenCV-android-sdk/sdk/native/jni/include/opencv2/core/core.hpp>
#include </Users/linweijie/DevelopKit/OpenCV/Android/2.4.11/OpenCV-android-sdk/sdk/native/jni/include/opencv2/contrib/detection_based_tracker.hpp>
#include </Users/linweijie/DevelopKit/OpenCV/Android/2.4.11/OpenCV-android-sdk/sdk/native/jni/include/opencv2/imgproc/imgproc.hpp>
#include </Users/linweijie/DevelopKit/OpenCV/Android/2.4.11/OpenCV-android-sdk/sdk/native/jni/include/opencv2/features2d/features2d.hpp>

using namespace cv;

JNIEXPORT jstring JNICALL Java_helloopencv_peter_com_opencvqrtracker_myNDK_jni_1HelloJni(
        JNIEnv* env, jobject obj, jstring str){
    const char* toWhat = env->GetStringUTFChars(str, JNI_FALSE);
    char hello[80];
    sprintf(hello,"Hello, %s!", toWhat);
    return env->NewStringUTF(hello);
}

JNIEXPORT void JNICALL Java_helloopencv_peter_com_opencvqrtracker_myNDK_jni_1FeatureDetector(
        JNIEnv* env, jobject obj, jlong addrGray, jlong addrRgba, jlong addrDescriptor){

    Mat* pMatGr=(Mat*)addrGray;
    Mat* pMatRgb=(Mat*)addrRgba;
    Mat* pMatDesc=(Mat*)addrDescriptor;
    vector<KeyPoint> v;

    //OrbFeatureDetector detector(50);
    OrbFeatureDetector detector;
    OrbDescriptorExtractor  extractor;
    detector.detect(*pMatGr, v);
    extractor.compute( *pMatGr, v, *pMatDesc );
    circle(*pMatRgb, Point(100,100), 10, Scalar(5,128,255,255));
    for( size_t i = 0; i < v.size(); i++ ) {
        circle(*pMatRgb, Point(v[i].pt.x, v[i].pt.y), 10, Scalar(255,128,0,255));
    }
}

int DELAY_CAPTION = 1500;
int DELAY_BLUR = 100;
int MAX_KERNEL_LENGTH = 11;
int THRESHOLD = 25;
int THRESHOLD_MAX = 255;

JNIEXPORT void JNICALL Java_helloopencv_peter_com_opencvqrtracker_myNDK_jni_1GrayDenoisingThresholdContour
        (JNIEnv * env, jobject obj, jlong orgImage){

    Mat* orgMat = (Mat*) orgImage;
    Mat dstMat = *orgMat;
//    Mat* gryMat = (Mat*) gryImage;

    // --------
    // 1.灰階
    // --------
    cvtColor(dstMat, dstMat, CV_BGR2GRAY);

    // --------
    // 2.去雜訊
    // --------
    // Homogeneous blur
//    for ( int i = 1; i < MAX_KERNEL_LENGTH; i = i + 2 )
//    {
//        blur( *gryMat, *gryMat, Size( i, i ), Point(-1,-1) );
//    }

    // Gaussian blur
    for ( int i = 1; i < MAX_KERNEL_LENGTH; i = i + 2 )
    {
        GaussianBlur( dstMat, dstMat, Size( i, i ), 0, 0 );
    }

    // Median blur
//    for ( int i = 1; i < MAX_KERNEL_LENGTH; i = i + 2 )
//    {
//        medianBlur ( *orgMat, *orgMat, i );
//    }

    // Bilateral Filter
//    for ( int i = 1; i < MAX_KERNEL_LENGTH; i = i + 2 )
//    {
//        bilateralFilter ( *orgMat, *orgMat, i, i*2, i/2 );
//    }

    // --------
    // 3.二值化
    // --------
    threshold( dstMat, dstMat, THRESHOLD, THRESHOLD_MAX, THRESH_BINARY );

    // --------
    // 4.邊緣偵測與尋找輪廓
    // --------
    Mat canny_output;
    vector<vector<Point> > contours;
    vector<Vec4i> hierarchy;

    // 邊緣偵測
    Canny( dstMat, canny_output, 50, 100, 3 );
    findContours( canny_output, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE, Point(0, 0) );

    // 描繪外框
//    RNG rng(12345);
//    Mat drawing = Mat::zeros( canny_output.size(), CV_8UC3 );
    for( int i = 0; i< contours.size(); i++ )
    {
//        Scalar color = Scalar( rng.uniform(0, 255), rng.uniform(0,255), rng.uniform(0,255) );
        Scalar color = Scalar( 255, 255, 255 );
        drawContours( *orgMat, contours, i, color, 2, 8, hierarchy, 0, Point() );
    }

//    *orgMat = drawing;

    canny_output.release();
//    drawing.release();
}


vector<vector<Point> > showMarker;

JNIEXPORT jint JNICALL Java_helloopencv_peter_com_opencvqrtracker_myNDK_jni_1QrTracking
        (JNIEnv * env, jobject obj, jlong orgImage, jlongArray qrImages){

    showMarker.clear();
    Mat* orgMat = (Mat*) orgImage;
    Mat dstMat = *orgMat;

    // 轉 YCrCb , Gaussian blur , 取 三色通道
    vector<Mat> planes;
    cvtColor(dstMat, dstMat, COLOR_BGR2YCrCb);

    int MAX_KERNEL_LENGTH = 5;
    for ( int i = 1; i < MAX_KERNEL_LENGTH; i = i + 2 )
    {
        GaussianBlur( dstMat, dstMat, Size( i, i ), 0, 0 );
    }

    split(dstMat, planes);

    // 二值化
    threshold(planes[1], dstMat, 140, 255, THRESH_BINARY);

    // 取得輪廓點
    vector< vector<Point> > contours;
    vector<Vec4i> hierarchy;
    Mat canny_output;

    Canny( dstMat, canny_output, 50, 100, 3 );
    findContours( canny_output, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE, Point(0, 0) );
    canny_output.release();
    dstMat.release();

    // 繪製標示框
    vector<vector<Point> > poly(contours.size());
    vector<vector<Point> > marker;
    vector<Rect> boundRect( contours.size() );

    for( int i = 0; i< contours.size(); i++ )
    {
        double eps = contours[i].size()*0.05;
        approxPolyDP(contours[i], poly[i], eps, true); // 計算方形區域
        boundRect[i] = boundingRect( Mat(poly[i]) );

        if (poly[i].size() != 4)                       // 去除非四角型的內容
            continue;
        if (!isContourConvex(poly[i]))                 // 去除有缺口的內容
            continue;
        if (boundRect[i].area() < 600)                 // 去除長方形區域面積低於600
            continue;
        if (abs(poly[i][0].x - poly[i][2].x) < 1.3 * abs(poly[i][0].y - poly[i][2].y))
            marker.push_back(poly[i]);

        Scalar color = Scalar( 0, 255, 0 );
        rectangle( *orgMat, boundRect[i].tl(), boundRect[i].br(), color, 2, 8, 0 );
    }

    // 取得透視結果
    jlong *imagesArrayData = env->GetLongArrayElements(qrImages, 0);
    int arrayLen = env->GetArrayLength(qrImages);

    Point2f ptsT[] = { Point2f(0, 0), Point2f(0, 79), Point2f(79, 79), Point2f(79, 0) };
    for (size_t i = 0; i < marker.size(); i++){
        Point2f ptsS[] = { Point2f(marker[i][0].x, marker[i][0].y),
                           Point2f(marker[i][1].x, marker[i][1].y),
                           Point2f(marker[i][2].x, marker[i][2].y),
                           Point2f(marker[i][3].x, marker[i][3].y) };
        Mat trsMat = getPerspectiveTransform(ptsS, ptsT);
        Mat aftMat = Mat::zeros(80, 80, CV_8UC3);

        warpPerspective(*orgMat, aftMat, trsMat, aftMat.size(), INTER_LINEAR);

        // todo 抓取 mark 透視結果
        int count = i/4;
        if (i%4!=0)
            continue;

        if (count < arrayLen){
            Mat& matImage = *(Mat*)imagesArrayData[count];
            matImage = aftMat;
            showMarker.push_back(marker[i]);
        }

        trsMat.release();
        aftMat.release();
    }

    return showMarker.size();
}

JNIEXPORT void JNICALL Java_helloopencv_peter_com_opencvqrtracker_myNDK_jni_1QrDrawing
        (JNIEnv * env, jobject obj, jlong orgImage, jint count, jstring qrCode){

    if (showMarker.size()>0){
        Mat* orgMat = (Mat*) orgImage;

        int x = showMarker[count][0].x;
        int y = showMarker[count][1].y;
        string str = env->GetStringUTFChars(qrCode, 0);

        putText(*orgMat, str, Point2f(x, y), FONT_HERSHEY_COMPLEX, 1,  Scalar(247,255,46));
    }
}
