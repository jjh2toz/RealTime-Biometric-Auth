import scipy.io
import numpy as np
import tensorflow as tf
from sklearn.preprocessing import StandardScaler

import random

DATA_SIZE = 480

# pub_data = scipy.io.loadmat('static/Motor_Imagery.mat')['data']
pub_data = np.zeros((100, 2))
public_sub_size = pub_data.shape[1] // 480
public_sub_cnt = pub_data.shape[0]
pub_data = pub_data.reshape(-1, 1)


def make_public_dataset(scaler):
    global pub_data, public_sub_size, public_sub_cnt

    public_data = scaler.transform(pub_data)
    public_data = public_data.reshape(public_sub_cnt * public_sub_size, DATA_SIZE, 2)

    public_data_each = []
    for i in range(public_sub_cnt):
        public_data_each.insert(i, public_data[i * public_sub_size:(i + 1) * public_sub_size, :, :])

    return public_data_each


# 데이터를 받아서 train_data로 만들고 label과 함께 return
def make_data(eeg_data, data_type='test'):
    data = scipy.io.loadmat(eeg_data)['data']
    cut_size = len(data) // DATA_SIZE * DATA_SIZE  # 나누어 떨어지도록 만듦
    data = data[0:cut_size, :]
    sub_size = data.shape[0] // 480

    sd_scaler = StandardScaler()
    # 스케일링
    if data_type == 'train':
        data = data.reshape(-1, 1)
        sd_scaler.fit(data)
        data = sd_scaler.transform(data)

    # 최종 reshape data
    data = data.reshape(sub_size, DATA_SIZE, 2)

    # scaler를 주는 이유는 public 데이터마다 다르게 적용해야 하기 때문
    return data, sd_scaler


def make_train_dataset(eeg_data):
    train_data, scaler = make_data(eeg_data, data_type='train')

    # train_data마다 public data의 scale 다르게 설정
    public_data_each = make_public_dataset(scaler)

    # public_data에서 train_date의 길이만큼 고르기
    train_data_size = train_data.shape[0]
    one_size = train_data_size // 109
    remain_size = train_data_size % 109
    temp_list1 = [one_size] * (109 - remain_size)
    temp_list2 = [one_size + 1] * remain_size
    num_list = temp_list1 + temp_list2
    # num_list = [9, 9, 10, 9, 10, 10, 10, 9 ....]
    random.shuffle(num_list)

    resize_public_data = np.zeros([train_data_size, DATA_SIZE, 2])
    cnt = 0

    for i, num in enumerate(num_list):
        cnt = cnt + num
        resize_public_data[cnt - num:cnt, :, :] = random.sample(list(public_data_each[i]), num)

    train_data_n = np.concatenate([train_data, resize_public_data], axis=0)
    train_label = np.ones([train_data_size])
    public_label = np.zeros([train_data_size])
    train_label_n = np.concatenate([train_label, public_label], axis=0)

    return train_data_n, train_label_n


def make_model(input_shape):
    inputs = tf.keras.Input(shape=input_shape)

    layers = tf.keras.layers.Conv1D(filters=16, kernel_size=3, strides=1, padding='same', activation='relu')(inputs)
    layers = tf.keras.layers.MaxPool1D(3)(layers)

    layers = tf.keras.layers.Conv1D(filters=32, kernel_size=3, strides=1, padding='same', activation='relu')(layers)
    layers = tf.keras.layers.MaxPool1D(3)(layers)

    layers = tf.keras.layers.Conv1D(filters=64, kernel_size=3, strides=1, padding='same', activation='relu')(layers)
    layers = tf.keras.layers.MaxPool1D(3)(layers)

    # LSTM 층
    layers = tf.keras.layers.LSTM(64)(layers)

    # Dense 층
    layers = tf.keras.layers.Dropout(0.3)(layers)
    layers = tf.keras.layers.Dense(128, activation='relu')(layers)
    layers = tf.keras.layers.Dense(64, activation='relu')(layers)
    layers = tf.keras.layers.Dense(1, activation='sigmoid')(layers)

    model = tf.keras.models.Model(inputs=inputs, outputs=layers)
    model.compile(loss='binary_crossentropy', optimizer=tf.keras.optimizers.Adam(0.005), metrics=['accuracy'])

    return model
