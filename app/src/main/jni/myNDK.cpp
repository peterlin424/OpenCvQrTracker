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

JNIEXPORT jint JNICALL Java_helloopencv_peter_com_opencvqrtracker_myNDK_jni_1QrTracking
        (JNIEnv *env, jobject obj, jlong orgImage, jlong cvImage, jlong resultImage){

    Mat* orgMat = (Mat*) orgImage;
    Mat* cvMat = (Mat*) cvImage;
    Mat* resultMat = (Mat*) resultImage;
    Mat matColor,matBin;
    vector<Mat> planes;

    // 轉 YCrCb 取 三色通道 並 二值化
    cvtColor((*orgMat), matColor, COLOR_BGR2YCrCb);
    split(matColor, planes);
    threshold(planes[1], matBin, 155, 255, THRESH_BINARY);

    // 取得輪廓點 和 繪製輪廓
    vector< vector<Point> > contours;
    Canny( matBin, matBin, 50, 100, 3 );
    findContours(matBin, contours, RETR_EXTERNAL, CHAIN_APPROX_NONE);
    matBin.release();

    vector<vector<cv::Point> > poly(contours.size());
    vector<vector<cv::Point> > marker;
    matColor = Mat::zeros(matColor.size(), CV_8UC3);

    for( int i = 0; i< contours.size(); i++ )
    {
        Scalar color = Scalar(255, 255, 0);
        double eps = contours[i].size()*0.05;

        approxPolyDP(contours[i], poly[i], eps, true); // 計算方形區域

        if (poly[i].size() != 4)                       // 去除非四角型的內容
            continue;

        if (!isContourConvex(poly[i]))                 // 去除有缺口的內容
            continue;

        drawContours( matColor, contours, i, color, CV_FILLED, 8, vector<Vec4i>() );

        if (abs(poly[i][0].x - poly[i][2].x) < 1.3 * abs(poly[i][0].y - poly[i][2].y)) { //
            drawContours(matColor, poly, i, Scalar(0, 0, 255), 2, 8, vector<Vec4i>(), 0, Point());
            marker.push_back(poly[i]);
        }
        else{
            drawContours(matColor, poly, i, Scalar(255, 0, 0), 2, 8, vector<Vec4i>(), 0, Point());
        }
    }
    *cvMat = matColor;
    matColor.release();

    //
    Point2f ptsT[] = { Point2f(0, 0), Point2f(0, 79), Point2f(79, 79), Point2f(79, 0) };

    for (size_t i = 0; i < marker.size(); i++){
        Point2f ptsS[] = { Point2f(marker[i][0].x, marker[i][0].y),
                           Point2f(marker[i][1].x, marker[i][1].y),
                           Point2f(marker[i][2].x, marker[i][2].y),
                           Point2f(marker[i][3].x, marker[i][3].y) };
        Mat m = getPerspectiveTransform(ptsS, ptsT);
        Mat matResult = Mat::zeros(80, 80, CV_8UC3);

        warpPerspective(*orgMat, matResult, m, matResult.size(), INTER_LINEAR);

        *resultMat = matResult;
    }

    return marker.size();
}