# CMakeLists.txt for WireGuard GoBackend native library
cmake_minimum_required(VERSION 3.22.1)

project("wireguard-go-jni")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Find required packages
find_library(log-lib log)

# Add source files
add_library(wireguard-go-jni SHARED
    wireguard_go_jni.cpp
)

# Link libraries
target_link_libraries(wireguard-go-jni
    ${log-lib}
)

# Include directories
target_include_directories(wireguard-go-jni PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
)

# Compiler flags
target_compile_options(wireguard-go-jni PRIVATE
    -Wall
    -Wextra
    -fexceptions
    -frtti
)

# Set output name
set_target_properties(wireguard-go-jni PROPERTIES
    OUTPUT_NAME "wireguard-go"
)
