#include <jni.h>
#include "vision_processor.h"
#include "camera_interface.h"
#include <memory>
#include <map>

// Global storage for processor instances
static std::map<jlong, std::shared_ptr<dough_vision::VisionProcessor>> processors;
static std::map<jlong, std::shared_ptr<dough_vision::CameraInterface>> cameras;
static jlong next_handle = 1;

extern "C" {

// Vision Processor JNI functions
JNIEXPORT jlong JNICALL Java_com_doughvision_VisionProcessor_nativeCreate(JNIEnv* env, jobject obj) {
    auto processor = std::make_shared<dough_vision::VisionProcessor>();
    jlong handle = next_handle++;
    processors[handle] = processor;
    return handle;
}

JNIEXPORT jboolean JNICALL Java_com_doughvision_VisionProcessor_nativeInitialize(
    JNIEnv* env, jobject obj, jlong handle, jstring config_path) {
    
    auto it = processors.find(handle);
    if (it == processors.end()) {
        return JNI_FALSE;
    }
    
    const char* path = env->GetStringUTFChars(config_path, nullptr);
    bool result = it->second->initialize(path);
    env->ReleaseStringUTFChars(config_path, path);
    
    return result ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_com_doughvision_VisionProcessor_nativeUpdateColorRange(
    JNIEnv* env, jobject obj, jlong handle,
    jdouble h1, jdouble s1, jdouble v1,
    jdouble h2, jdouble s2, jdouble v2) {
    
    auto it = processors.find(handle);
    if (it == processors.end()) {
        return;
    }
    
    cv::Scalar lower(h1, s1, v1);
    cv::Scalar upper(h2, s2, v2);
    it->second->updateColorRange(lower, upper);
}

JNIEXPORT void JNICALL Java_com_doughvision_VisionProcessor_nativeUpdateROI(
    JNIEnv* env, jobject obj, jlong handle,
    jint x, jint y, jint width, jint height) {
    
    auto it = processors.find(handle);
    if (it == processors.end()) {
        return;
    }
    
    cv::Rect roi(x, y, width, height);
    it->second->updateROI(roi);
}

JNIEXPORT void JNICALL Java_com_doughvision_VisionProcessor_nativeDestroy(
    JNIEnv* env, jobject obj, jlong handle) {
    
    processors.erase(handle);
}

// Camera Interface JNI functions
JNIEXPORT jlong JNICALL Java_com_doughvision_CameraInterface_nativeCreate(
    JNIEnv* env, jobject obj) {
    
    auto camera = std::make_shared<dough_vision::CameraInterface>();
    jlong handle = next_handle++;
    cameras[handle] = camera;
    return handle;
}

JNIEXPORT jboolean JNICALL Java_com_doughvision_CameraInterface_nativeInitialize(
    JNIEnv* env, jobject obj, jlong handle, jint camera_index) {
    
    auto it = cameras.find(handle);
    if (it == cameras.end()) {
        return JNI_FALSE;
    }
    
    bool result = it->second->initialize(camera_index);
    return result ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_com_doughvision_CameraInterface_nativeDestroy(
    JNIEnv* env, jobject obj, jlong handle) {
    
    cameras.erase(handle);
}

} // extern "C"
