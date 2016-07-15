LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#OPENCV_CAMERA_MODULES:=off
#OPENCV_INSTALL_MODULES:=off
#OPENCV_LIB_TYPE:=SHARED
include /Users/linweijie/DevelopKit/OpenCV/Android/2.4.11/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_C_INCLUDES += /Users/linweijie/DevelopKit/OpenCV/Android/2.4.11/OpenCV-android-sdk/sdk/native/jni/include/
LOCAL_SRC_FILES := myNDK.cpp

LOCAL_MODULE := myJNI

include $(BUILD_SHARED_LIBRARY)