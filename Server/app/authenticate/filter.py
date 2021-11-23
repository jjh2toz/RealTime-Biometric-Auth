from scipy import signal


def fir_filter(fs, numtaps, fstop1, fpass1, fpass2, fstop2):
    '''
    :param fs: 주파수 sample rate
    :param numtaps: 차수
    :param fstop1: trans_width1
    :param fpass1: pass band1
    :param fpass2: pass band2
    :param fstop2: trans_width2
    :return filter: 만들어진 filter
    '''

    edges = [0, fstop1, fpass1, fpass2, fstop2, 0.5 * fs]
    res = signal.remez(numtaps + 1, edges, [0, 1, 0], Hz=fs)

    return res
