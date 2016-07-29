/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class helloopencv_peter_com_opencvqrtracker_myNDK */

#ifndef _Included_helloopencv_peter_com_opencvqrtracker_myNDK
#define _Included_helloopencv_peter_com_opencvqrtracker_myNDK
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     helloopencv_peter_com_opencvqrtracker_myNDK
 * Method:    jni_HelloJni
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_helloopencv_peter_com_opencvqrtracker_myNDK_jni_1HelloJni
  (JNIEnv *, jobject, jstring);

/*
 * Class:     helloopencv_peter_com_opencvqrtracker_myNDK
 * Method:    jni_FeatureDetector
 * Signature: (JJJ)V
 */
JNIEXPORT void JNICALL Java_helloopencv_peter_com_opencvqrtracker_myNDK_jni_1FeatureDetector
  (JNIEnv *, jobject, jlong, jlong, jlong);

/*
 * Class:     helloopencv_peter_com_opencvqrtracker_myNDK
 * Method:    jni_GrayDenoisingThresholdContour
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_helloopencv_peter_com_opencvqrtracker_myNDK_jni_1GrayDenoisingThresholdContour
  (JNIEnv *, jobject, jlong);

/*
 * Class:     helloopencv_peter_com_opencvqrtracker_myNDK
 * Method:    jni_QrTracking
 * Signature: (J[JIIZZ)I
 */
JNIEXPORT jint JNICALL Java_helloopencv_peter_com_opencvqrtracker_myNDK_jni_1QrTracking
  (JNIEnv *, jobject, jlong, jlongArray, jint, jint, jboolean, jboolean);

/*
 * Class:     helloopencv_peter_com_opencvqrtracker_myNDK
 * Method:    jni_QrDrawing
 * Signature: (JILjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_helloopencv_peter_com_opencvqrtracker_myNDK_jni_1QrDrawing
  (JNIEnv *, jobject, jlong, jint, jstring);

/*
 * Class:     helloopencv_peter_com_opencvqrtracker_myNDK
 * Method:    jni_ImageMatching
 * Signature: (JJJ)Z
 */
JNIEXPORT jboolean JNICALL Java_helloopencv_peter_com_opencvqrtracker_myNDK_jni_1ImageMatching
  (JNIEnv *, jobject, jlong, jlong, jlong);

#ifdef __cplusplus
}
#endif
#endif
