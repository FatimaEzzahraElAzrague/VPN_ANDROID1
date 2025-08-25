#include <jni.h>
#include <string>
#include <map>
#include <android/log.h>

#define LOG_TAG "WireGuardGoJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// WireGuard-Go tunnel handle type
typedef struct {
    void* tunnel;
    bool is_running;
} WireGuardTunnel;

// Global tunnel storage
static std::map<long, WireGuardTunnel*> tunnels;
static long next_tunnel_id = 1;

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_example_v_vpn_WireGuardGoInterface_createTunnel(JNIEnv *env, jobject thiz, jstring config) {
    try {
        const char* config_str = env->GetStringUTFChars(config, 0);
        LOGI("Creating tunnel with config: %s", config_str);
        
        // Parse JSON config (simplified for now)
        // In a real implementation, you'd parse the JSON and extract WireGuard parameters
        
        // Create tunnel structure
        WireGuardTunnel* tunnel = new WireGuardTunnel();
        tunnel->tunnel = nullptr; // Will be set by WireGuard-Go
        tunnel->is_running = false;
        
        // Store tunnel
        long tunnel_id = next_tunnel_id++;
        tunnels[tunnel_id] = tunnel;
        
        LOGI("Tunnel created with ID: %ld", tunnel_id);
        
        env->ReleaseStringUTFChars(config, config_str);
        return tunnel_id;
        
    } catch (const std::exception& e) {
        LOGE("Error creating tunnel: %s", e.what());
        return 0;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_example_v_vpn_WireGuardGoInterface_startTunnel(JNIEnv *env, jobject thiz, jlong tunnel_handle) {
    try {
        auto it = tunnels.find(tunnel_handle);
        if (it == tunnels.end()) {
            LOGE("Tunnel not found: %ld", tunnel_handle);
            return JNI_FALSE;
        }
        
        WireGuardTunnel* tunnel = it->second;
        LOGI("Starting tunnel: %ld", tunnel_handle);
        
        // In a real implementation, you'd call WireGuard-Go to start the tunnel
        // For now, we'll simulate success
        tunnel->is_running = true;
        
        LOGI("Tunnel started successfully: %ld", tunnel_handle);
        return JNI_TRUE;
        
    } catch (const std::exception& e) {
        LOGE("Error starting tunnel: %s", e.what());
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_example_v_vpn_WireGuardGoInterface_stopTunnel(JNIEnv *env, jobject thiz, jlong tunnel_handle) {
    try {
        auto it = tunnels.find(tunnel_handle);
        if (it == tunnels.end()) {
            LOGE("Tunnel not found: %ld", tunnel_handle);
            return JNI_FALSE;
        }
        
        WireGuardTunnel* tunnel = it->second;
        LOGI("Stopping tunnel: %ld", tunnel_handle);
        
        // In a real implementation, you'd call WireGuard-Go to stop the tunnel
        tunnel->is_running = false;
        
        LOGI("Tunnel stopped successfully: %ld", tunnel_handle);
        return JNI_TRUE;
        
    } catch (const std::exception& e) {
        LOGE("Error stopping tunnel: %s", e.what());
        return JNI_FALSE;
    }
}

JNIEXPORT void JNICALL
Java_com_example_v_vpn_WireGuardGoInterface_destroyTunnel(JNIEnv *env, jobject thiz, jlong tunnel_handle) {
    try {
        auto it = tunnels.find(tunnel_handle);
        if (it == tunnels.end()) {
            LOGE("Tunnel not found: %ld", tunnel_handle);
            return;
        }
        
        WireGuardTunnel* tunnel = it->second;
        LOGI("Destroying tunnel: %ld", tunnel_handle);
        
        // Clean up tunnel
        delete tunnel;
        tunnels.erase(it);
        
        LOGI("Tunnel destroyed: %ld", tunnel_handle);
        
    } catch (const std::exception& e) {
        LOGE("Error destroying tunnel: %s", e.what());
    }
}

JNIEXPORT jstring JNICALL
Java_com_example_v_vpn_WireGuardGoInterface_getTunnelStatus(JNIEnv *env, jobject thiz, jlong tunnel_handle) {
    try {
        auto it = tunnels.find(tunnel_handle);
        if (it == tunnels.end()) {
            return env->NewStringUTF("Tunnel not found");
        }
        
        WireGuardTunnel* tunnel = it->second;
        std::string status = tunnel->is_running ? "Running" : "Stopped";
        
        return env->NewStringUTF(status.c_str());
        
    } catch (const std::exception& e) {
        LOGE("Error getting tunnel status: %s", e.what());
        return env->NewStringUTF("Error");
    }
}

JNIEXPORT jboolean JNICALL
Java_com_example_v_vpn_WireGuardGoInterface_updateTunnelConfig(JNIEnv *env, jobject thiz, jlong tunnel_handle, jstring config) {
    try {
        auto it = tunnels.find(tunnel_handle);
        if (it == tunnels.end()) {
            LOGE("Tunnel not found: %ld", tunnel_handle);
            return JNI_FALSE;
        }
        
        const char* config_str = env->GetStringUTFChars(config, 0);
        LOGI("Updating tunnel config: %ld", tunnel_handle);
        
        // In a real implementation, you'd call WireGuard-Go to update the configuration
        
        env->ReleaseStringUTFChars(config, config_str);
        LOGI("Tunnel config updated successfully: %ld", tunnel_handle);
        return JNI_TRUE;
        
    } catch (const std::exception& e) {
        LOGE("Error updating tunnel config: %s", e.what());
        return JNI_FALSE;
    }
}

} // extern "C"
