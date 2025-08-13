# CMakeLists.txt for WireGuard GoBackend native library
cmake_minimum_required(VERSION 3.22.1)

project("wireguard-go")

# Build as pure C to avoid unnecessary C++ toolchain checks
set(CMAKE_C_STANDARD 11)
set(CMAKE_C_EXTENSIONS ON)

# Find required packages
find_library(log-lib log)

# Add library for WireGuard GoBackend
add_library(wireguard-go SHARED
        wireguard-go.c
        )

# Link libraries
target_link_libraries(wireguard-go
        ${log-lib}
        )
