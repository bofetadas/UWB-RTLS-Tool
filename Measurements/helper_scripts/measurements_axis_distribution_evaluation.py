import numpy as np
import sys
from math import sqrt, pow
from matplotlib import pyplot as plt
from scipy.stats import norm
from scipy.stats import shapiro, normaltest, anderson
from statsmodels.graphics.gofplots import qqplot


def print_no_document_found_error():
    print("ERROR: No .txt document found.")
    print("Please add a .txt document as first argument when calling this script.")
    print("Note that this document has had to be created by the related \"LocationApp\" for Android.")
    print("Usage: python3 measurements_evaluation.py <your_doc.txt>")
    print("Exiting")

# Returns the amount of samples collected - necessary for mean and standard deviation calculations
def get_sample_count(filename):
    with open(filename) as f:
        for i, l in enumerate(f):
            pass
    return i + 1

def distance_between_two_points2D(sample_point, reference_point):
    return sqrt(pow(sample_point[0] - reference_point[0], 2) + pow(sample_point[1] - reference_point[1], 2))

def distance_between_two_points3D(sample_point, reference_point):
    return sqrt(pow(sample_point[0] - reference_point[0], 2) + pow(sample_point[1] - reference_point[1], 2) + pow(sample_point[2] - reference_point[2], 2))

def standard_deviation(samples, samples_mean, samples_count):
    l = []
    for s in samples:
        squared = pow(s - samples_mean, 2)
        l.append(squared)
    std = sqrt(sum(l) / samples_count)
    return std

def evaluate_data(filename, reference_point):
    ''' Local variables '''

    # Lists holding all coordinates on axis
    sample_x_coords = []
    sample_y_coords = []
    sample_z_coords = []

    sample_distances_x = []
    sample_distances_y = []
    sample_distances_z = []

    ''' Go! '''
    # Get amount of samples collected
    sample_count = get_sample_count(filename)

    # Go through samples
    with open(filename) as file:
        for line in file:
            # Extract x, y and z coordinates out of each line
            x = round(float(line.split(',')[0]), 3)
            y = round(float(line.split(',')[1]), 3)
            z = round(float(line.split(',')[2]), 3)

            # Add individual coordinates to coord's lists (necessary for later standard deviation calculation)
            sample_x_coords.append(x)
            sample_y_coords.append(y)
            sample_z_coords.append(z)

            sample_distance_x = x - reference_point[0]
            sample_distance_y = y - reference_point[1]
            sample_distance_z = z - reference_point[2]

            sample_distances_x.append(sample_distance_x)
            sample_distances_y.append(sample_distance_y)
            sample_distances_z.append(sample_distance_z)

    return sample_count, sample_x_coords, sample_y_coords, sample_z_coords, sample_distances_x, sample_distances_y, sample_distances_z
  
def plot_distribution(data):
    n, bins, patches = plt.hist(np.array(data), 100)
    mu = np.mean(data)
    sigma = np.std(data)
    print("Mean: {}, std: {}".format(mu, sigma))
    # Shapiro test
    stat, p = shapiro(np.array(data))
    if p > 0.05:
        print("Shapiro: Data is normally distributed")
    else:
        print("Shapiro: Data is NOT normally distributed, p-value: {}".format(p))
    stat, p = normaltest(np.array(data))
    if p > 0.05:
        print("D'Agostino: Data is normally distributed")
    else:
        print("D'Agostino: Data is NOT normally distributed, p-value: {}".format(p))
    result = anderson(np.array(data))
    p = 0
    for i in range(len(result.critical_values)):
	    sl, cv = result.significance_level[i], result.critical_values[i]
	    if result.statistic < result.critical_values[i]:
		    print('Anderson: %.3f: %.3f, data looks normal (fail to reject H0)' % (sl, cv))
	    else:
		    print('Anderson: %.3f: %.3f, data does not look normal (reject H0)' % (sl, cv))
    plt.plot(bins, norm.pdf(bins, mu, sigma))
    qqplot(np.array(data), line='s')
    plt.show()

def plot_coordinates(reference_point, sample_count, data_x, data_y, data_z):
    fig = plt.figure()
    
    ax1 = fig.add_subplot(311)
    ax1.plot(range(sample_count), data_x, label='Distances X', c='r')
    ax1.axhline(np.mean(data_x), 0, 1, label='Measurement Mean X', c='y')
    ax1.axhline(0, 0, 1, label='Actual X')
    ax1.legend()

    ax2 = fig.add_subplot(312)
    ax2.plot(range(sample_count), data_y, label='Distances Y', c='r')
    ax2.axhline(np.mean(data_y), 0, 1, label='Measurement Mean Y', c='y')
    ax2.axhline(0, 0, 1, label='Actual Y')
    ax2.legend()

    ax3 = fig.add_subplot(313)
    ax3.plot(range(sample_count), data_z, label='Distances Z', c='r')
    ax3.axhline(np.mean(data_z), 0, 1, label='Measurement Mean Z', c='y')
    ax3.axhline(0, 0, 1, label='Actual Z')
    ax3.legend()

    plt.show()

if __name__ == "__main__":
    try:
        filename = sys.argv[1]
    except IndexError:
        print_no_document_found_error()
        sys.exit(1)
    
    direction = filename.split('(')[0]
    x_reference = float((filename.split('(')[1].split(')')[0].split('_')[0]).replace(',', '.'))
    y_reference = float((filename.split('(')[1].split(')')[0].split('_')[1]).replace(',', '.'))
    z_reference = float((filename.split('(')[1].split(')')[0].split('_')[2]).replace(',', '.'))
    reference_point = [x_reference, y_reference, z_reference]
    
    sample_count, axis_coords_x, axis_coords_y, axis_coords_z, sample_distances_x, sample_distances_y, sample_distances_z = evaluate_data(filename, reference_point)
    plot_distribution(axis_coords_x)
    plot_distribution(axis_coords_y)
    plot_distribution(axis_coords_z)
    plot_distribution(sample_distances_x)
    plot_distribution(sample_distances_y)
    plot_distribution(sample_distances_z)

    #plot_coordinates(reference_point, sample_count, axis_coords_x, axis_coords_y, axis_coords_z)
    #plot_coordinates(reference_point, sample_count, sample_distances_x, sample_distances_y, sample_distances_z)
