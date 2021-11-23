import os
import pandas as pd
import numpy as np
import scipy
import torch
import neurokit2 as nk
import scipy.io as sio
import matplotlib.pyplot as plt
import csv

from sklearn.preprocessing import MaxAbsScaler, StandardScaler, MinMaxScaler
from keras.models import load_model


# ECG feature 추출
def getFeature(data):
    rate = 200

    np_data = np.array(data)

    suspicious_index = np.where(np_data >= 3)[0]

    for si in suspicious_index:
        count = 1
        # 다음 값도 노이즈면
        while np_data[si + count] >= 3:
            count += 1

        # 혹시 마지막 요소면
        if si == len(np_data):
            np_data[si] = np_data[si - 1]

        # 또는 첫 요소면
        elif si == 0:
            np_data[si] = np_data[si + count]

        else:
            median = (np_data[si - 1] + np_data[si + count]) / 2
            np_data[si] = median

    data = np_data.reshape(-1)
    sig = nk.ecg_clean(data, sampling_rate=rate)  # filtering

    _, rpeaks = nk.ecg_peaks(sig, sampling_rate=rate)  # rpeak 추출
    _, waves_peak = nk.ecg_delineate(sig, rpeaks, sampling_rate=rate, method="peak")  # p,q,s,t 추출

    R_peaks = []
    T_peaks = []
    P_peaks = []
    Q_peaks = []
    S_peaks = []
    R_peaks = rpeaks['ECG_R_Peaks']  # dict에서 결과 numpy만 가져옴
    T_peaks = waves_peak['ECG_T_Peaks']
    P_peaks = waves_peak['ECG_P_Peaks']
    Q_peaks = waves_peak['ECG_Q_Peaks']
    S_peaks = waves_peak['ECG_S_Peaks']

    amp_feature = []

    for i in range(len(R_peaks) - 1):
        feature = []
        if ((not Q_peaks) | (not S_peaks) | (not P_peaks) | (not T_peaks)):  # 데이터 빈었는지
            continue
        else:  # 데이터의 nan 있는지 확인
            if ((not (np.isnan(R_peaks[i]))) & (not (np.isnan(Q_peaks[i]))) & (not (np.isnan(S_peaks[i]))) & (
                    not (np.isnan(P_peaks[i]))) & (not (np.isnan(T_peaks[i])))):
                PR_amp = sig[R_peaks[i]] - sig[P_peaks[i]]
                QR_amp = sig[R_peaks[i]] - sig[Q_peaks[i]]
                RS_amp = sig[R_peaks[i]] - sig[S_peaks[i]]
                RT_amp = abs(sig[R_peaks[i]] - sig[T_peaks[i]])
                PR_inter = abs(P_peaks[i] - R_peaks[i])
                QR_inter = (abs(R_peaks[i] - Q_peaks[i]))
                RS_inter = (abs(R_peaks[i] - S_peaks[i]))
                RT_inter = abs(T_peaks[i] - R_peaks[i])
                RR_inter = R_peaks[i + 1] - R_peaks[i]
                ST_inter = abs(T_peaks[i] - S_peaks[i])

                feature.append(PR_amp)
                feature.append(QR_amp)
                feature.append(RS_amp)
                feature.append(RT_amp)
                feature.append(PR_inter)
                feature.append(QR_inter)
                feature.append(RS_inter)
                feature.append(RT_inter)
                feature.append(RR_inter)
                feature.append(ST_inter)
                amp_feature.append(feature)  # (추출한 peak수, 10)으로 저장

    amp_feature = amp_feature[:10]
    return amp_feature


# 전처리
def preprocess(measure_data):
    feature_count = len(measure_data) // 10

    feature_data = measure_data[:feature_count * 10]
    np_data = np.array(feature_data)

    input_data_2D = np_data.reshape(-1, 1)

    SDscaler = StandardScaler()
    SDscaler.fit(input_data_2D)
    input_scaled = SDscaler.transform(input_data_2D)

    data = input_scaled.reshape(1, 10, 10)

    return data


# 전처리한 데이터를 모델에 넣어서 인증 진행
def authentication(data, username):
    # 2. 모델 불러오기
    model = load_model(f'../media/{username}.h5')

    # 3. 모델 사용하기 (정확도가 출력됨)
    results = model.predict(data)

    print('Predict : ' + str(results))
