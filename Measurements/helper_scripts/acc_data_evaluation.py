import fnmatch
import numpy as np
import os
import sys
from matplotlib import pyplot as plt
from scipy.stats import norm


def get_sample_count(filename):
    with open(filename) as f:
        for i, l in enumerate(f):
            pass
    return i + 1

def evaulate_and_plot_data(directory):
    files = fnmatch.filter(os.listdir(directory), '*.txt')
    accelerations_array = []
    x_array = []
    y_array = []
    z_array = []
    sample_count = 0

    for filename in files:
        print(filename)
        sample_count += get_sample_count(filename)
        with open(filename) as f:
            for line in f:
                x_acc = round(float(line.split(',')[0]), 3)
                x_array.append(x_acc)
                y_acc = round(float(line.split(',')[1]), 3)
                y_array.append(y_acc)
                z_acc = round(float(line.split(',')[2]), 3)
                z_array.append(z_acc)
                accelerations_array.append([x_acc, y_acc, z_acc])
    coordinate_matrix = np.matrix(accelerations_array)
    x_array = np.array(x_array)
    y_array = np.array(y_array)
    z_array = np.array(z_array)
    cov_matrix = np.cov(coordinate_matrix, rowvar=False, ddof=0)
    np.set_printoptions(threshold=np.inf, formatter={'float_kind':'{:f}'.format})
    print("Mean acceleration on X axis: {}".format(np.mean(x_array)))
    print("Mean acceleration on Y axis: {}".format(np.mean(y_array)))
    print("Mean acceleration on Z axis: {}".format(np.mean(z_array)))
    print("\n")
    print("Standard deviation of accelerations on X axis: {}".format(np.std(x_array)))
    print("Standard deviation of accelerations on Y axis: {}".format(np.std(y_array)))
    print("Standard deviation of accelerations on Z axis: {}".format(np.std(z_array)))
    print("\n")
    print("Covariance matrix of accelerations on X, Y, Z axis: ")
    print(cov_matrix)

    plot_distribution(x_array)
    plot_distribution(y_array)
    plot_distribution(z_array)
    plot_accelerations(sample_count, x_array, y_array, z_array)

def plot_distribution(data):
    n, bins, patches = plt.hist(data, 100)
    mu = np.mean(data)
    sigma = np.std(data)
    plt.plot(bins, norm.pdf(bins, mu, sigma))
    plt.show()

def plot_accelerations(sample_count, data_x, data_y, data_z):
    fig = plt.figure()
    
    ax1 = fig.add_subplot(311)
    ax1.plot(range(sample_count), data_x, label='Accelerations X', c='r')
    ax1.axhline(np.mean(data_x), 0, 1, label='Measurement Mean X', c='y')
    ax1.axhline(0, 0, 1, label='Actual X')
    ax1.legend()

    ax2 = fig.add_subplot(312)
    ax2.plot(range(sample_count), data_y, label='Accelerations Y', c='r')
    ax2.axhline(np.mean(data_y), 0, 1, label='Measurement Mean Y', c='y')
    ax2.axhline(0, 0, 1, label='Actual Y')
    ax2.legend()

    ax3 = fig.add_subplot(313)
    ax3.plot(range(sample_count), data_z, label='Accelerations Z', c='r')
    ax3.axhline(np.mean(data_z), 0, 1, label='Measurement Mean Z', c='y')
    ax3.axhline(0, 0, 1, label='Actual Z')
    ax3.legend()

    plt.show()

if __name__ == "__main__":
    try:
        pwd = sys.argv[1]
    except IndexError:
        print("No directory provided. Please specify a directory of files containing accelerometer readings.")
        sys.exit(1)

    evaulate_and_plot_data(pwd)