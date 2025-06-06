/*
 * Copyright (C) 2025 AlexMofer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// 使用代理了 Canny 算法的检测器
// Created by Alex on 2025/5/24.
//

#ifndef DOCUMENTSKEWCORRECTION_DOCUMENTSKEWDETECTORDELEGATED_HPP
#define DOCUMENTSKEWCORRECTION_DOCUMENTSKEWDETECTORDELEGATED_HPP

#include <jni.h>
#include "DocumentSkewDetector.hpp"

namespace DR {
    class DocumentSkewDetectorDelegated final : public DocumentSkewDetector {

    public:
        explicit DocumentSkewDetectorDelegated(JNIEnv *env,
                                               jint width, jint height, jbyteArray pixels) {
            env->GetJavaVM(&vm);
            mPixels = (jbyteArray) env->NewGlobalRef(pixels);
            mData = env->GetByteArrayElements(mPixels, JNI_FALSE);
            mImage = cv::Mat(height, width, CV_8UC1, mData);
        }

        ~DocumentSkewDetectorDelegated() override {
            mImage.release();
            JNIEnv *env = nullptr;
            vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6);
            env->ReleaseByteArrayElements(mPixels, mData, 0);
            env->DeleteGlobalRef(mPixels);
        }

        bool detect(int &ltx, int &lty, int &rtx, int &rty,
                    int &lbx, int &lby, int &rbx, int &rby) const override {
            return calculateBounds(mImage, ltx, lty, rtx, rty, lbx, lby, rbx, rby);
        }

    private:
        cv::Mat mImage;
        JavaVM *vm;
        jbyteArray mPixels;
        jbyte *mData;
    };
}

#endif //DOCUMENTSKEWCORRECTION_DOCUMENTSKEWDETECTORDELEGATED_HPP
