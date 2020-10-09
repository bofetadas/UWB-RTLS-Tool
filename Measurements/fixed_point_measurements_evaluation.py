import sys
import matplotlib.pyplot as plt
from math import sqrt, pow
from numpy import arctan2, diff, linspace, mean, pi, std, sqrt, square

"""
This script evaluates and prints the accuracy of given position estimations in a .txt document.
The document must have been created by the related LocationApp for Android in order to comply with the algorithm implemented below.
"""

quiver_directions = {'N': [0, 1], 'E': [1, 0], 'S': [0, -1], 'W': [-1, 0]}

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
    l1 = []
    for s in samples:
        squared = pow(s - samples_mean, 2)
        l.append(squared)
        l1.append(s - samples_mean)
    standard_deviation = sqrt(sum(l) / samples_count)
    print("Self std: {}".format(standard_deviation))
    print("Numpy std: {}".format(std(samples)))
    print("RMS: {}".format(root_mean_square_error(l1)))
    #return std
    return std(samples)

def root_mean_square_error(data):
    return sqrt(mean(square(data)))

def cart2pol(x, y):
    rho = sqrt(x**2 + y**2)
    phi = arctan2(y, x)
    return(rho, phi)

def evaluate_data(filename, reference_point):
    ''' Local variables '''
    # Variables making up the mean coordinate
    uwb_x_mean = 0.0
    uwb_y_mean = 0.0
    uwb_z_mean = 0.0
    filtered_x_mean = 0.0
    filtered_y_mean = 0.0
    filtered_z_mean = 0.0

    # Lists holding all coordinates on axis
    uwb_x_coords = []
    uwb_y_coords = []
    uwb_z_coords = []
    filtered_x_coords = []
    filtered_y_coords = []
    filtered_z_coords = []

    # List holding all sample points
    uwb_points = []
    filtered_points = []

    # Lists holding distances on each axis of a sample to reference point
    uwb_distances_x = []
    uwb_distances_y = []
    uwb_distances_z = []
    filtered_distances_x = []
    filtered_distances_y = []
    filtered_distances_z = []

    # Lists holding all distances from sample points to reference point
    uwb_distances_to_ref_point_2D = []
    uwb_distances_to_ref_point_3D = []
    filtered_distances_to_ref_point_2D = []
    filtered_distances_to_ref_point_3D = []

    # Lists holding all distances from sample points to mean point
    uwb_distances_to_samples_center_point_2D = []
    uwb_distances_to_samples_center_point_3D = []
    filtered_distances_to_samples_center_point_2D = []
    filtered_distances_to_samples_center_point_3D = []
    
    # Experimental
    # Lists holding all distance differences from positions to their next one
    uwb_delta_distances_2D = []
    uwb_delta_distances_3D = []
    filtered_delta_distances_2D = []
    filtered_delta_distances_3D = []

    ''' Go! '''
    # Get amount of samples collected
    sample_count = get_sample_count(filename)

    # Go through samples
    with open(filename) as file:
        for line in file:
            uwb_position = line.split('|')[0]
            filtered_position = line.split('|')[1]

            # Extract x, y and z coordinates out of each line
            uwb_x = float(uwb_position.split(',')[0])
            uwb_y = float(uwb_position.split(',')[1])
            uwb_z = float(uwb_position.split(',')[2])
            filtered_x = float(filtered_position.split(',')[0])
            filtered_y = float(filtered_position.split(',')[1])
            filtered_z = float(filtered_position.split(',')[2])
            
            # Add individual coordinates to coord's means (necessary for later standard deviation calculation)
            uwb_x_mean += uwb_x
            uwb_y_mean += uwb_y
            uwb_z_mean += uwb_z
            filtered_x_mean += filtered_x
            filtered_y_mean += filtered_y
            filtered_z_mean += filtered_z

            # Add individual coordinates to coord's lists (necessary for later standard deviation calculation)
            uwb_x_coords.append(uwb_x)
            uwb_y_coords.append(uwb_y)
            uwb_z_coords.append(uwb_z)
            filtered_x_coords.append(filtered_x)
            filtered_y_coords.append(filtered_y)
            filtered_z_coords.append(filtered_z)

            # Calculate distances on each axis
            uwb_axis_distance_x = uwb_x - reference_point[0]
            uwb_axis_distance_y = uwb_y - reference_point[1]
            uwb_axis_distance_z = uwb_z - reference_point[2]
            uwb_distances_x.append(uwb_axis_distance_x)
            uwb_distances_y.append(uwb_axis_distance_y)
            uwb_distances_z.append(uwb_axis_distance_z)
            filtered_axis_distance_x = filtered_x - reference_point[0]
            filtered_axis_distance_y = filtered_y - reference_point[1]
            filtered_axis_distance_z = filtered_z - reference_point[2]
            filtered_distances_x.append(filtered_axis_distance_x)
            filtered_distances_y.append(filtered_axis_distance_y)
            filtered_distances_z.append(filtered_axis_distance_z)

            # Make up coordinate and add to list
            uwb_sample_point = [uwb_x, uwb_y, uwb_z]
            uwb_points.append(uwb_sample_point)
            filtered_sample_point = [filtered_x, filtered_y, filtered_z]
            filtered_points.append(filtered_sample_point)

            # Calculate distance of sample point to reference point in 2D and 3D and add to distances lists
            uwb_distance_to_ref_point_2D = distance_between_two_points2D(uwb_sample_point, reference_point)
            uwb_distances_to_ref_point_2D.append(uwb_distance_to_ref_point_2D)
            uwb_distance_to_ref_point_3D = distance_between_two_points3D(uwb_sample_point, reference_point)
            uwb_distances_to_ref_point_3D.append(uwb_distance_to_ref_point_3D)
            filtered_distance_to_ref_point_2D = distance_between_two_points2D(filtered_sample_point, reference_point)
            filtered_distances_to_ref_point_2D.append(filtered_distance_to_ref_point_2D)
            filtered_distance_to_ref_point_3D = distance_between_two_points3D(filtered_sample_point, reference_point)
            filtered_distances_to_ref_point_3D.append(filtered_distance_to_ref_point_3D)

    '''#############################################################
    #################### ACCURACY EVALUATION ####################
    #############################################################'''
    # Get sample mean distance on each axis
    # Variant 1
    # sample_x_mean_distance1 = sample_x_mean - reference_point[0]
    # sample_y_mean_distance1 = sample_y_mean - reference_point[1]
    # sample_z_mean_distance1 = sample_z_mean - reference_point[2]
    # Variant 2
    # sample_x_mean_distance2 = sum(distances_x) / sample_count
    # sample_y_mean_distance2 = sum(distances_y) / sample_count
    # sample_z_mean_distance2 = sum(distances_z) / sample_count

    # Calculate average, min and max distance of samples to reference point
    uwb_mean_distance_to_ref_point_2D = sum(uwb_distances_to_ref_point_2D) / sample_count
    uwb_mean_distance_to_ref_point_3D = sum(uwb_distances_to_ref_point_3D) / sample_count
    uwb_min_distance_to_ref_point_2D = min(uwb_distances_to_ref_point_2D)
    uwb_min_distance_to_ref_point_3D = min(uwb_distances_to_ref_point_3D)
    uwb_max_distance_to_ref_point_2D = max(uwb_distances_to_ref_point_2D)
    uwb_max_distance_to_ref_point_3D = max(uwb_distances_to_ref_point_3D)
    filtered_mean_distance_to_ref_point_2D = sum(filtered_distances_to_ref_point_2D) / sample_count
    filtered_mean_distance_to_ref_point_3D = sum(filtered_distances_to_ref_point_3D) / sample_count
    filtered_min_distance_to_ref_point_2D = min(filtered_distances_to_ref_point_2D)
    filtered_min_distance_to_ref_point_3D = min(filtered_distances_to_ref_point_3D)
    filtered_max_distance_to_ref_point_2D = max(filtered_distances_to_ref_point_2D)
    filtered_max_distance_to_ref_point_3D = max(filtered_distances_to_ref_point_3D)
    
    # Get standard deviation of samples on each axis x, y and z
    # sample_x_std = standard_deviation(sample_x_coords, sample_x_mean, sample_count)
    # sample_y_std = standard_deviation(sample_y_coords, sample_y_mean, sample_count)
    # sample_z_std = standard_deviation(sample_z_coords, sample_z_mean, sample_count)
    
    # Get distance standard deviations of distance to reference point
    uwb_std_2D_distances_to_ref_point = standard_deviation(uwb_distances_to_ref_point_2D, uwb_mean_distance_to_ref_point_2D, sample_count)
    uwb_std_3D_distances_to_ref_point = standard_deviation(uwb_distances_to_ref_point_3D, uwb_mean_distance_to_ref_point_3D, sample_count)
    filtered_std_2D_distances_to_ref_point = standard_deviation(filtered_distances_to_ref_point_2D, filtered_mean_distance_to_ref_point_2D, sample_count)
    filtered_std_3D_distances_to_ref_point = standard_deviation(filtered_distances_to_ref_point_3D, filtered_mean_distance_to_ref_point_3D, sample_count)
    
    '''#############################################################
    #################### PRECISION EVALUATION ###################
    #############################################################'''
    # Get samples center coordinates
    uwb_x_mean /= sample_count
    uwb_y_mean /= sample_count
    uwb_z_mean /= sample_count
    uwb_mean_point = [uwb_x_mean, uwb_y_mean, uwb_z_mean]
    filtered_x_mean /= sample_count
    filtered_y_mean /= sample_count
    filtered_z_mean /= sample_count
    filtered_mean_point = [filtered_x_mean, filtered_y_mean, filtered_z_mean]

    # Calculate distance of each sample point to samples center point
    for uwb_sample_point in uwb_points:
        uwb_distance_to_sample_center_point_2D = distance_between_two_points2D(uwb_sample_point, uwb_mean_point)
        uwb_distances_to_samples_center_point_2D.append(uwb_distance_to_sample_center_point_2D)
        uwb_distance_to_sample_center_point_3D = distance_between_two_points3D(uwb_sample_point, uwb_mean_point)
        uwb_distances_to_samples_center_point_3D.append(uwb_distance_to_sample_center_point_3D)
    for filtered_sample_point in filtered_points:
        filtered_distance_to_sample_center_point_2D = distance_between_two_points2D(filtered_sample_point, filtered_mean_point)
        filtered_distances_to_samples_center_point_2D.append(filtered_distance_to_sample_center_point_2D)
        filtered_distance_to_sample_center_point_3D = distance_between_two_points3D(filtered_sample_point, filtered_mean_point)
        filtered_distances_to_samples_center_point_3D.append(filtered_distance_to_sample_center_point_3D)
    
    # Calculate average, min and max distance of samples to samples center point
    uwb_mean_distance_to_samples_center_point_2D = sum(uwb_distances_to_samples_center_point_2D) / sample_count
    uwb_mean_distance_to_samples_center_point_3D = sum(uwb_distances_to_samples_center_point_3D) / sample_count
    uwb_min_distance_to_samples_center_point_2D = min(uwb_distances_to_samples_center_point_2D)
    uwb_min_distance_to_samples_center_point_3D = min(uwb_distances_to_samples_center_point_3D)
    uwb_max_distance_to_samples_center_point_2D = max(uwb_distances_to_samples_center_point_2D)
    uwb_max_distance_to_samples_center_point_3D = max(uwb_distances_to_samples_center_point_3D)
    filtered_mean_distance_to_samples_center_point_2D = sum(filtered_distances_to_samples_center_point_2D) / sample_count
    filtered_mean_distance_to_samples_center_point_3D = sum(filtered_distances_to_samples_center_point_3D) / sample_count
    filtered_min_distance_to_samples_center_point_2D = min(filtered_distances_to_samples_center_point_2D)
    filtered_min_distance_to_samples_center_point_3D = min(filtered_distances_to_samples_center_point_3D)
    filtered_max_distance_to_samples_center_point_2D = max(filtered_distances_to_samples_center_point_2D)
    filtered_max_distance_to_samples_center_point_3D = max(filtered_distances_to_samples_center_point_3D)

    # Calculate distance standard deviations of distance to samples center point
    uwb_std_2D_distances_to_samples_center_point = standard_deviation(uwb_distances_to_samples_center_point_2D, uwb_mean_distance_to_samples_center_point_2D, sample_count)
    uwb_std_3D_distances_to_samples_center_point = standard_deviation(uwb_distances_to_samples_center_point_3D, uwb_mean_distance_to_samples_center_point_3D, sample_count)
    filtered_std_2D_distances_to_samples_center_point = standard_deviation(filtered_distances_to_samples_center_point_2D, filtered_mean_distance_to_samples_center_point_2D, sample_count)
    filtered_std_3D_distances_to_samples_center_point = standard_deviation(filtered_distances_to_samples_center_point_3D, filtered_mean_distance_to_samples_center_point_3D, sample_count)

    # Calculate distances from samples center point to reference point
    uwb_distance_samples_center_point_to_ref_point_2D = distance_between_two_points2D(uwb_mean_point, reference_point)
    uwb_distance_samples_center_point_to_ref_point_3D = distance_between_two_points3D(uwb_mean_point, reference_point)
    filtered_distance_samples_center_point_to_ref_point_2D = distance_between_two_points2D(filtered_mean_point, reference_point)
    filtered_distance_samples_center_point_to_ref_point_3D = distance_between_two_points3D(filtered_mean_point, reference_point)

    # Experimental
    '''###################################################################
    #################### MOTION SICKNESS EVALUATION ###################
    ###################################################################'''
    # 2D Get mean, min and max distance differences from samples to their next ones
    uwb_delta_distances_2D = diff(uwb_distances_to_ref_point_2D)
    uwb_delta_distances_2D =  [abs(n) for n in uwb_delta_distances_2D]
    uwb_mean_delta_distance_2D = sum(uwb_delta_distances_2D) / (sample_count - 1)
    uwb_min_delta_distance_2D = min(uwb_delta_distances_2D)
    uwb_max_delta_distance_2D = max(uwb_delta_distances_2D)
    filtered_delta_distances_2D = diff(filtered_distances_to_ref_point_2D)
    filtered_delta_distances_2D =  [abs(n) for n in filtered_delta_distances_2D]
    filtered_mean_delta_distance_2D = sum(filtered_delta_distances_2D) / (sample_count - 1)
    filtered_min_delta_distance_2D = min(filtered_delta_distances_2D)
    filtered_max_delta_distance_2D = max(filtered_delta_distances_2D)

    # 3D Get mean, min and max distance differences from samples to their next ones
    uwb_delta_distances_3D = diff(uwb_distances_to_ref_point_3D)
    uwb_delta_distances_3D = [abs(n) for n in uwb_delta_distances_3D]
    uwb_mean_delta_distance_3D = sum(uwb_delta_distances_3D) / (sample_count - 1)
    uwb_min_delta_distance_3D = min(uwb_delta_distances_3D)
    uwb_max_delta_distance_3D = max(uwb_delta_distances_3D)
    filtered_delta_distances_3D = diff(filtered_distances_to_ref_point_3D)
    filtered_delta_distances_3D = [abs(n) for n in filtered_delta_distances_3D]
    filtered_mean_delta_distance_3D = sum(filtered_delta_distances_3D) / (sample_count - 1)
    filtered_min_delta_distance_3D = min(filtered_delta_distances_3D)
    filtered_max_delta_distance_3D = max(filtered_delta_distances_3D)
    
    #return sample_count, sample_x_center, sample_y_center, sample_z_center, average_distance_to_ref_point_2D, average_distance_to_ref_point_3D, min_distance_to_ref_point_2D, min_distance_to_ref_point_3D, max_distance_to_ref_point_2D, max_distance_to_ref_point_3D, std_2D_distances_to_ref_point, std_3D_distances_to_ref_point, average_distance_to_samples_center_point_2D, average_distance_to_samples_center_point_3D, min_distance_to_samples_center_point_2D, min_distance_to_samples_center_point_3D, max_distance_to_samples_center_point_2D, max_distance_to_samples_center_point_3D, std_2D_distances_to_samples_center_point, std_3D_distances_to_samples_center_point, average_delta_distance_2D, average_delta_distance_3D, min_delta_distance_2D, min_delta_distance_3D, max_delta_distance_2D, max_delta_distance_3D
    return sample_count, uwb_x_mean, uwb_y_mean, uwb_z_mean, filtered_x_mean, filtered_y_mean, filtered_z_mean, uwb_mean_distance_to_ref_point_2D, uwb_mean_distance_to_ref_point_3D, uwb_min_distance_to_ref_point_2D, uwb_min_distance_to_ref_point_3D, uwb_max_distance_to_ref_point_2D, uwb_max_distance_to_ref_point_3D, filtered_mean_distance_to_ref_point_2D, filtered_mean_distance_to_ref_point_3D, filtered_min_distance_to_ref_point_2D, filtered_min_distance_to_ref_point_3D, filtered_max_distance_to_ref_point_2D, filtered_max_distance_to_ref_point_3D, uwb_std_2D_distances_to_ref_point, uwb_std_3D_distances_to_ref_point, filtered_std_2D_distances_to_ref_point, filtered_std_3D_distances_to_ref_point, uwb_mean_distance_to_samples_center_point_2D, uwb_mean_distance_to_samples_center_point_3D, uwb_min_distance_to_samples_center_point_2D, uwb_min_distance_to_samples_center_point_3D, uwb_max_distance_to_samples_center_point_2D, uwb_max_distance_to_samples_center_point_3D, uwb_std_2D_distances_to_samples_center_point, uwb_std_3D_distances_to_samples_center_point, filtered_mean_distance_to_samples_center_point_2D, filtered_mean_distance_to_samples_center_point_3D, filtered_min_distance_to_samples_center_point_2D, filtered_min_distance_to_samples_center_point_3D, filtered_max_distance_to_samples_center_point_2D, filtered_max_distance_to_samples_center_point_3D, filtered_std_2D_distances_to_samples_center_point, filtered_std_3D_distances_to_samples_center_point, uwb_mean_delta_distance_2D, uwb_mean_delta_distance_3D, uwb_min_delta_distance_2D, uwb_min_delta_distance_3D, uwb_max_delta_distance_2D, uwb_max_delta_distance_3D, filtered_mean_delta_distance_2D, filtered_mean_delta_distance_3D, filtered_min_delta_distance_2D, filtered_min_delta_distance_3D, filtered_max_delta_distance_2D, filtered_max_delta_distance_3D, uwb_x_coords, uwb_y_coords, uwb_z_coords, filtered_x_coords, filtered_y_coords, filtered_z_coords, uwb_points, filtered_points, uwb_x_mean, uwb_y_mean, uwb_z_mean, uwb_mean_point, filtered_x_mean, filtered_y_mean, filtered_z_mean, filtered_mean_point

# Values are plotted in a centered system
# That is, the reference point is set to (0,0,0) and the samples are scattered around it accordingly
def plot_coordinates(direction, uwb_points, filtered_points, uwb_mean_point, filtered_mean_point, reference_point):
    # Plot 2D
    fig = plt.figure(figsize=(7, 13))
    ax0 = plt.subplot(211)
    ax1 = plt.subplot(212, projection='polar')
    plt.title("2D measurements around {}, {} in {} direction".format(reference_point[0], reference_point[1], direction))
    plot_2d_cartesian(uwb_points, filtered_points, uwb_mean_point, filtered_mean_point, reference_point, ax0)
    plot_2d_polar(uwb_points, filtered_points, uwb_mean_point, filtered_mean_point, reference_point, direction, ax1)

    plt.show()

def plot_2d_cartesian(uwb_points, filtered_points, uwb_mean_point, filtered_mean_point, reference_point, axs):
    plt.xlabel = "X AXIS"
    plt.ylabel = "Y AXIS"
    # Plot samples
    for x, y, z in uwb_points:
        axs.scatter(x, y, label='UWB Coordinates', c='b', marker='^')
    for x, y, z in filtered_points:
        axs.scatter(x, y, label='Filtered Coordinates', c='r', marker='x')
    # Plot reference point
    axs.scatter(uwb_mean_point[0], uwb_mean_point[1], label='UWB Mean Coordinate', c='g', marker='^')
    axs.scatter(filtered_mean_point[0], filtered_mean_point[1], label='Filtered Mean Coordinate', c='g', marker='x')
    axs.scatter(reference_point[0], reference_point[1], label='Reference Coordinate', c='g', marker='o')
    axs.grid(True)
    legend(axs)

def plot_2d_polar(uwb_points, filtered_points, uwb_mean_point, filtered_mean_point, reference_point, direction, axs):
    d = quiver_directions[direction]
    axs.set_thetalim(0, pi * 2)
    axs.set_xticks(linspace(0, pi * 2, 4, endpoint=False))
    # Plot samples
    for x, y, z in uwb_points:
        r, theta = cart2pol(x - reference_point[0], y - reference_point[1])
        axs.scatter(theta, r, label='UWB Coordinates', c='b', marker='^')
    for x, y, z in filtered_points:
        r, theta = cart2pol(x - reference_point[0], y - reference_point[1])
        axs.scatter(theta, r, label='Filtered Coordinates', c='r', marker='x')
    # Plot reference point
    r, theta = cart2pol(uwb_mean_point[0] - reference_point[0], uwb_mean_point[1] - reference_point[1])
    axs.scatter(theta, r, label='UWB Mean Coordinate', c='g', marker='^')
    r, theta = cart2pol(filtered_mean_point[0] - reference_point[0], filtered_mean_point[1] - reference_point[1])
    axs.scatter(theta, r, label='Filtered Mean Coordinate', c='g', marker='x')
    r, theta = cart2pol(0, 0)
    axs.scatter(theta, r, label='Reference point', c='g', marker='o')
    axs.quiver(0, 0, d[0], d[1], color='orange', scale=10, width=0.01)
    legend(axs)

# Plot a legend and remove duplicate legend elements
def legend(axs):
    handles, labels = axs.get_legend_handles_labels()
    # Python verison >= 3.7 only
    by_label = dict(zip(labels, handles))
    # For Python versions < 3.7 use below 2 code lines
    # from collections import OrderedDict
    # by_label = OrderedDict(zip(labels, handles))
    axs.legend(by_label.values(), by_label.keys())

def plot_line_charts(uwb_x_coords, uwb_y_coords, uwb_z_coords, filtered_x_coords, filtered_y_coords, filtered_z_coords, uwb_x_mean, uwb_y_mean, uwb_z_mean, filtered_x_mean, filtered_y_mean, filtered_z_mean, reference_point, sample_count):
    fig = plt.figure()
    plt.title("Raw UWB and filtered positions")
    # Plot coordinates
    ax1 = plt.subplot(311)
    ax1.plot(range(sample_count), uwb_x_coords, label='UWB X', c='b')
    ax1.plot(range(sample_count), filtered_x_coords, label='Filtered X', c='r')
    ax1.axhline(uwb_x_mean, 0, 1, label='UWB X Mean', c='b', linestyle='dashed')
    ax1.axhline(filtered_x_mean, 0, 1, label='Filtered X Mean', c='r', linestyle='dashed')
    ax1.axhline(reference_point[0], 0, 1, label='User X', c='g')
    ax1.legend()

    ax2 = plt.subplot(312)
    ax2.plot(range(sample_count), uwb_y_coords, label='UWB Y', c='b')
    ax2.plot(range(sample_count), filtered_y_coords, label='Filtered Y', c='r')
    ax2.axhline(uwb_y_mean, 0, 1, label='UWB Y Mean', c='b', linestyle='dashed')
    ax2.axhline(filtered_y_mean, 0, 1, label='Filtered Y Mean', c='r', linestyle='dashed')
    ax2.axhline(reference_point[1], 0, 1, label='User Y', c='g')
    ax2.legend()

    ax3 = plt.subplot(313)
    ax3.plot(range(sample_count), uwb_z_coords, label='UWB Z', c='b')
    ax3.plot(range(sample_count), filtered_z_coords, label='Filtered Z', c='r')
    ax3.axhline(uwb_z_mean, 0, 1, label='UWB Z Mean', c='b', linestyle='dashed')
    ax3.axhline(filtered_z_mean, 0, 1, label='Filtered Z Mean', c='r', linestyle='dashed')
    ax3.axhline(reference_point[2], 0, 1, label='User Z', c='g')
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
    
    #sample_count, sample_x_center, sample_y_center, sample_z_center, average_distance_to_ref_point_2D, average_distance_to_ref_point_3D, min_distance_to_ref_point_2D, min_distance_to_ref_point_3D, max_distance_to_ref_point_2D, max_distance_to_ref_point_3D, std_2D_distances_to_ref_point, std_3D_distances_to_ref_point, average_distance_to_samples_center_point_2D, average_distance_to_samples_center_point_3D, min_distance_to_samples_center_point_2D, min_distance_to_samples_center_point_3D, max_distance_to_samples_center_point_2D, max_distance_to_samples_center_point_3D, std_2D_distances_to_samples_center_point, std_3D_distances_to_samples_center_point, average_delta_distance_2D, average_delta_distance_3D, min_delta_distance_2D, min_delta_distance_3D, max_delta_distance_2D, max_delta_distance_3D = evaluate_data(filename, reference_point)
    sample_count, uwb_x_mean, uwb_y_mean, uwb_z_mean, filtered_x_mean, filtered_y_mean, filtered_z_mean, uwb_mean_distance_to_ref_point_2D, uwb_mean_distance_to_ref_point_3D, uwb_min_distance_to_ref_point_2D, uwb_min_distance_to_ref_point_3D, uwb_max_distance_to_ref_point_2D, uwb_max_distance_to_ref_point_3D, filtered_mean_distance_to_ref_point_2D, filtered_mean_distance_to_ref_point_3D, filtered_min_distance_to_ref_point_2D, filtered_min_distance_to_ref_point_3D, filtered_max_distance_to_ref_point_2D, filtered_max_distance_to_ref_point_3D, uwb_std_2D_distances_to_ref_point, uwb_std_3D_distances_to_ref_point, filtered_std_2D_distances_to_ref_point, filtered_std_3D_distances_to_ref_point, uwb_mean_distance_to_samples_center_point_2D, uwb_mean_distance_to_samples_center_point_3D, uwb_min_distance_to_samples_center_point_2D, uwb_min_distance_to_samples_center_point_3D, uwb_max_distance_to_samples_center_point_2D, uwb_max_distance_to_samples_center_point_3D, uwb_std_2D_distances_to_samples_center_point, uwb_std_3D_distances_to_samples_center_point, filtered_mean_distance_to_samples_center_point_2D, filtered_mean_distance_to_samples_center_point_3D, filtered_min_distance_to_samples_center_point_2D, filtered_min_distance_to_samples_center_point_3D, filtered_max_distance_to_samples_center_point_2D, filtered_max_distance_to_samples_center_point_3D, filtered_std_2D_distances_to_samples_center_point, filtered_std_3D_distances_to_samples_center_point, uwb_mean_delta_distance_2D, uwb_mean_delta_distance_3D, uwb_min_delta_distance_2D, uwb_min_delta_distance_3D, uwb_max_delta_distance_2D, uwb_max_delta_distance_3D, filtered_mean_delta_distance_2D, filtered_mean_delta_distance_3D, filtered_min_delta_distance_2D, filtered_min_delta_distance_3D, filtered_max_delta_distance_2D, filtered_max_delta_distance_3D, uwb_x_coords, uwb_y_coords, uwb_z_coords, filtered_x_coords, filtered_y_coords, filtered_z_coords, uwb_points, filtered_points, uwb_x_mean, uwb_y_mean, uwb_z_mean, uwb_mean_point, filtered_x_mean, filtered_y_mean, filtered_z_mean, filtered_mean_point = evaluate_data(filename, reference_point)
    print("\n")
    print("GENERAL INFORMATION")
    print("Direction: {}, Sample Count: {}".format(direction, sample_count))
    print("X Reference: {}, Y Reference: {}, Z Reference: {}".format(x_reference, y_reference, z_reference))
    print("UWB X Mean: {:.3f}, UWB Y Mean: {:.3f}, UWB Z Mean: {:.3f}".format(uwb_x_mean, uwb_y_mean, uwb_z_mean))
    print("Filtered X Mean: {:.3f}, Filtered Y Mean: {:.3f}, Filtered Z Mean: {:.3f}".format(filtered_x_mean, filtered_y_mean, filtered_z_mean))
    print("\n")
    print("UWB ACCURACY RESULTS")
    print("Average distance to reference point 2D/3D: {:.3f}m / {:.3f}m".format(uwb_mean_distance_to_ref_point_2D, uwb_mean_distance_to_ref_point_3D))
    print("Min distance to reference point 2D/3D: {:.3f}m / {:.3f}m".format(uwb_min_distance_to_ref_point_2D, uwb_min_distance_to_ref_point_3D))
    print("Max distance to reference point 2D/3D: {:.3f}m / {:.3f}m".format(uwb_max_distance_to_ref_point_2D, uwb_max_distance_to_ref_point_3D))
    print("Standard deviation of distances to reference point 2D/3D: {:.3f}m / {:.3f}m".format(uwb_std_2D_distances_to_ref_point, uwb_std_3D_distances_to_ref_point))
    print("\n")
    print("FILTERED ACCURACY RESULTS")
    print("Average distance to reference point 2D/3D: {:.3f}m / {:.3f}m".format(filtered_mean_distance_to_ref_point_2D, filtered_mean_distance_to_ref_point_3D))
    print("Min distance to reference point 2D/3D: {:.3f}m / {:.3f}m".format(filtered_min_distance_to_ref_point_2D, filtered_min_distance_to_ref_point_3D))
    print("Max distance to reference point 2D/3D: {:.3f}m / {:.3f}m".format(filtered_max_distance_to_ref_point_2D, filtered_max_distance_to_ref_point_3D))
    print("Standard deviation of distances to reference point 2D/3D: {:.3f}m / {:.3f}m".format(filtered_std_2D_distances_to_ref_point, filtered_std_3D_distances_to_ref_point))
    print("\n")
    print("UWB PRECISION RESULTS")
    print("Average distance to samples center point 2D/3D: {:.3f}m / {:.3f}m".format(uwb_mean_distance_to_samples_center_point_2D, uwb_mean_distance_to_samples_center_point_3D))
    print("Min distance to samples center point 2D/3D: {:.3f}m / {:.3f}m".format(uwb_min_distance_to_samples_center_point_2D, uwb_min_distance_to_samples_center_point_3D))
    print("Max distance to samples center point 2D/3D: {:.3f}m / {:.3f}m".format(uwb_max_distance_to_samples_center_point_2D, uwb_max_distance_to_samples_center_point_3D))
    print("Standard deviation of distances to samples center point 2D/3D: {:.3f}m / {:.3f}m".format(uwb_std_2D_distances_to_samples_center_point, uwb_std_3D_distances_to_samples_center_point))
    print("\n")
    print("FILTERED PRECISION RESULTS")
    print("Average distance to samples center point 2D/3D: {:.3f}m / {:.3f}m".format(filtered_mean_distance_to_samples_center_point_2D, filtered_mean_distance_to_samples_center_point_3D))
    print("Min distance to samples center point 2D/3D: {:.3f}m / {:.3f}m".format(filtered_min_distance_to_samples_center_point_2D, filtered_min_distance_to_samples_center_point_3D))
    print("Max distance to samples center point 2D/3D: {:.3f}m / {:.3f}m".format(filtered_max_distance_to_samples_center_point_2D, filtered_max_distance_to_samples_center_point_3D))
    print("Standard deviation of distances to samples center point 2D/3D: {:.3f}m / {:.3f}m".format(filtered_std_2D_distances_to_samples_center_point, filtered_std_3D_distances_to_samples_center_point))
    print("\n")
    print("UWB MOTION SICKNESS RESULTS")
    print("Average delta distance 2D/3D: {:.3f}m / {:.3f}m".format(uwb_mean_delta_distance_2D, uwb_mean_delta_distance_3D))
    print("Min delta distance 2D/3D: {:.3f}m / {:.3f}m".format(uwb_min_delta_distance_2D, uwb_min_delta_distance_3D))
    print("Max delta distance 2D/3D: {:.3f}m / {:.3f}m".format(uwb_max_delta_distance_2D, uwb_max_delta_distance_3D))
    print("\n")
    print("FILTERED MOTION SICKNESS RESULTS")
    print("Average delta distance 2D/3D: {:.3f}m / {:.3f}m".format(filtered_mean_delta_distance_2D, filtered_mean_delta_distance_3D))
    print("Min delta distance 2D/3D: {:.3f}m / {:.3f}m".format(filtered_min_delta_distance_2D, filtered_min_delta_distance_3D))
    print("Max delta distance 2D/3D: {:.3f}m / {:.3f}m".format(filtered_max_delta_distance_2D, filtered_max_delta_distance_3D))
    print("\n")

    # Plot 2D coordinates in cartesian and polar coordinate system
    plot_coordinates(direction, uwb_points, filtered_points, uwb_mean_point, filtered_mean_point, reference_point)
    # Plot axis line charts
    plot_line_charts(uwb_x_coords, uwb_y_coords, uwb_z_coords, filtered_x_coords, filtered_y_coords, filtered_z_coords, uwb_x_mean, uwb_y_mean, uwb_z_mean, filtered_x_mean, filtered_y_mean, filtered_z_mean, reference_point, sample_count)
