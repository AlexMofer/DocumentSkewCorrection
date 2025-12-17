# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

cmake_minimum_required(VERSION 3.22.1)

project("core")

set(OpenCV_DIR ${CMAKE_SOURCE_DIR}/../../../OpenCV-android-sdk/sdk/native/jni)
FIND_PACKAGE(OpenCV REQUIRED core imgproc)
if (OpenCV_FOUND)
    include_directories(${OpenCV_INCLUDE_DIRS})
    message(STATUS "OpenCV library status:")
    message(STATUS "    version: ${OpenCV_VERSION}")
    message(STATUS "    libraries: ${OpenCV_LIBS}")
    message(STATUS "    include path: ${OpenCV_INCLUDE_DIRS}")
else (OpenCV_FOUND)
    message(FATAL_ERROR "OpenCV library not found")
endif (OpenCV_FOUND)

add_library(${CMAKE_PROJECT_NAME} SHARED
        core.cpp)

target_link_libraries(${CMAKE_PROJECT_NAME}
        android
        jnigraphics
        ${OpenCV_LIBS})