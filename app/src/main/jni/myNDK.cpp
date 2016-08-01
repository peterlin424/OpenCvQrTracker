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

// reference http://www.ipol.im/pub/art/2011/llmps-scb/
void balance_white(cv::Mat mat) {
    double discard_ratio = 0.05;
    int hists[3][256];
    memset(hists, 0, 3*256*sizeof(int));

    for (int y = 0; y < mat.rows; ++y) {
        uchar* ptr = mat.ptr<uchar>(y);
        for (int x = 0; x < mat.cols; ++x) {
            for (int j = 0; j < 3; ++j) {
                hists[j][ptr[x * 3 + j]] += 1;
            }
        }
    }

    // cumulative hist
    int total = mat.cols*mat.rows;
    int vmin[3], vmax[3];
    for (int i = 0; i < 3; ++i) {
        for (int j = 0; j < 255; ++j) {
            hists[i][j + 1] += hists[i][j];
        }
        vmin[i] = 0;
        vmax[i] = 255;
        while (hists[i][vmin[i]] < discard_ratio * total)
            vmin[i] += 1;
        while (hists[i][vmax[i]] > (1 - discard_ratio) * total)
            vmax[i] -= 1;
        if (vmax[i] < 255 - 1)
            vmax[i] += 1;
    }


    for (int y = 0; y < mat.rows; ++y) {
        uchar* ptr = mat.ptr<uchar>(y);
        for (int x = 0; x < mat.cols; ++x) {
            for (int j = 0; j < 3; ++j) {
                int val = ptr[x * 3 + j];
                if (val < vmin[j])
                    val = vmin[j];
                if (val > vmax[j])
                    val = vmax[j];
                ptr[x * 3 + j] = static_cast<uchar>((val - vmin[j]) * 255.0 / (vmax[j] - vmin[j]));
            }
        }
    }
}

vector<vector<Point> > showMarker;

JNIEXPORT jint JNICALL Java_helloopencv_peter_com_opencvqrtracker_myNDK_jni_1QrTracking
        (JNIEnv * env, jobject obj, jlong orgImage, jlongArray qrImages, jint minThreshold, jint maxThreshold, jboolean isThreshold, jboolean isBalanceWhite){

    showMarker.clear();
    Mat* orgMat = (Mat*) orgImage;

    if (isBalanceWhite) balance_white(*orgMat);

    Mat dstMat = *orgMat;

    // 轉 YCrCb , Gaussian blur , 取 三色通道
    vector<Mat> planes;
    cvtColor(dstMat, dstMat, COLOR_BGR2YCrCb);

    split(dstMat, planes);

    // 二值化
    threshold(planes[1], dstMat, minThreshold, maxThreshold, THRESH_BINARY);
    if (isThreshold) *orgMat = dstMat;

    // 取得輪廓點
    vector< vector<Point> > contours;
    findContours( dstMat, contours, RETR_TREE, CHAIN_APPROX_SIMPLE, Point(0, 0) );
    dstMat.release();

    // 繪製標示框
    vector<vector<Point> > poly(contours.size());
    vector<vector<Point> > marker;

    for( int i = 0; i< contours.size(); i++ )
    {
        double eps = contours[i].size()*0.05;
        approxPolyDP(contours[i], poly[i], eps, true); // 計算方形區域

        double area=fabs(contourArea(contours[i], false));
        if (area<300)                                  // 去除小區域資料
            continue;
        if (poly[i].size() != 4)                       // 去除非四角型的內容
            continue;
        if (!isContourConvex(poly[i]))                 // 去除有缺口的內容
            continue;

        if (abs(poly[i][0].x - poly[i][2].x) < 1.3 * abs(poly[i][0].y - poly[i][2].y)){
            drawContours(*orgMat, poly, i, Scalar(0, 0, 255), 2, 8, vector<Vec4i>(), 0, Point());
            marker.push_back(poly[i]);
        } else {
            drawContours(*orgMat, poly, i, Scalar(255, 0, 0), 2, 8, vector<Vec4i>(), 0, Point());
        }
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
        if (i < arrayLen){
            Mat& matImage = *(Mat*)imagesArrayData[i];
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

        int x = (showMarker[count][0].x + showMarker[count][1].x + showMarker[count][2].x + showMarker[count][3].x)/4;
        int y = (showMarker[count][0].y + showMarker[count][1].y + showMarker[count][2].y + showMarker[count][3].y)/4;
        string str = env->GetStringUTFChars(qrCode, 0);

        putText(*orgMat, str, Point2f(x, y), FONT_HERSHEY_COMPLEX, 1,  Scalar(247,255,46));
    }
}

JNIEXPORT jboolean JNICALL Java_helloopencv_peter_com_opencvqrtracker_myNDK_jni_1ImageMatching
        (JNIEnv *env, jobject obj, jlong orgImage, jlong tmpImage){

    Mat* orgMat = (Mat*) orgImage;
    Mat* tmpMat = (Mat*) tmpImage;

    // Resize tmpImage
    Size size = Size(orgMat->cols, orgMat->rows);
    resize(*tmpMat, *tmpMat, size, INTER_LINEAR);

    // 模板匹配
    Mat dstMat;
    dstMat.create(orgMat->rows - orgMat->rows+1, orgMat->cols - orgMat->cols+1, CV_8UC4);
    matchTemplate(*orgMat, *tmpMat, dstMat, TM_SQDIFF_NORMED);

//    double min, max;
//    Point minLoc;
//    Point maxLoc;
//    minMaxLoc(dstMat, &min, &max, &minLoc, &maxLoc, Mat());
//
//    // if method is SQDIFF or SQDIFF_NORMED, top left point = minLoc
//    // else top left point = maxLoc
//    Point topLeft = minLoc;
//    Point bottomRight = Point(minLoc.x + 100, minLoc.y + 100);
//    Scalar color = Scalar( rectColorR, rectColorG, rectColorB );
//
//    rectangle( *orgMat, minLoc, bottomRight, color, 2, 8, 0 );

//    dstMat.release();
    return false;
}
