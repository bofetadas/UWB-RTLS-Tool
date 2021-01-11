import matplotlib.pyplot as plt
import sys
from math import pow
from numpy import arctan2, linspace, mean, pi, std, sqrt, square

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

# Returns the amount of samples collected
def get_sample_count(filename):
    with open(filename) as f:
        for i, l in enumerate(f):
            pass
    return i + 1

def distance_between_two_points2D(measurement_point, reference_point):
    if reference_point == None:
        return
    return sqrt(pow(measurement_point[0] - reference_point[0], 2) + pow(measurement_point[1] - reference_point[1], 2))

def distance_between_two_points3D(measurement_point, reference_point):
    if reference_point == None:
        return
    return sqrt(pow(measurement_point[0] - reference_point[0], 2) + pow(measurement_point[1] - reference_point[1], 2) + pow(measurement_point[2] - reference_point[2], 2))

# Calculates the distance between consecutive positions in a list. The parameter fun must be either distance_between_two_points_2D or distance_between_two_points_3D. The parameter points must be a list that stores positions in a form of [x, y, z].
def delta_distances(fun, points):
    return list(map(fun, points, points[1:]))[:len(points)-1]

def standard_deviation(samples):
    return std(samples)

def root_mean_square(data):
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
    raw_x_accs = []
    raw_y_accs = []
    raw_z_accs = []
    filtered_x_accs = []
    filtered_y_accs = []
    filtered_z_accs = []

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
            raw_acceleration = line.split('|')[2]
            filtered_acceleration = line.split('|')[3]

            # Extract x, y and z coordinates out of each line
            uwb_x = float(uwb_position.split(',')[0])
            uwb_y = float(uwb_position.split(',')[1])
            uwb_z = float(uwb_position.split(',')[2])

            filtered_x = float(filtered_position.split(',')[0])
            filtered_y = float(filtered_position.split(',')[1])
            filtered_z = float(filtered_position.split(',')[2])

            raw_acceleration_x = float(raw_acceleration.split(',')[0])
            raw_acceleration_y = float(raw_acceleration.split(',')[1])
            raw_acceleration_z = float(raw_acceleration.split(',')[2])

            filtered_acceleration_x = float(filtered_acceleration.split(',')[0])
            filtered_acceleration_y = float(filtered_acceleration.split(',')[1])
            filtered_acceleration_z = float(filtered_acceleration.split(',')[2])

            # Add individual coordinates to coord's means
            uwb_x_mean += uwb_x
            uwb_y_mean += uwb_y
            uwb_z_mean += uwb_z
            filtered_x_mean += filtered_x
            filtered_y_mean += filtered_y
            filtered_z_mean += filtered_z

            # Add individual coordinates and accelerations to lists
            uwb_x_coords.append(uwb_x)
            uwb_y_coords.append(uwb_y)
            uwb_z_coords.append(uwb_z)
            filtered_x_coords.append(filtered_x)
            filtered_y_coords.append(filtered_y)
            filtered_z_coords.append(filtered_z)
            raw_x_accs.append(raw_acceleration_x)
            raw_y_accs.append(raw_acceleration_y)
            raw_z_accs.append(raw_acceleration_z)
            filtered_x_accs.append(filtered_acceleration_x)
            filtered_y_accs.append(filtered_acceleration_y)
            filtered_z_accs.append(filtered_acceleration_z)

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

            # Make up coordinates and accelerations and add to list
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
    # Calculate average, min and max distance of samples to reference point
    uwb_mean_distance_to_ref_point_2D = mean(uwb_distances_to_ref_point_2D)
    uwb_rms_distance_to_ref_point_2D = root_mean_square(uwb_distances_to_ref_point_2D)
    uwb_max_distance_to_ref_point_2D = max(uwb_distances_to_ref_point_2D)
    uwb_std_2D_distances_to_ref_point = standard_deviation(uwb_distances_to_ref_point_2D)
    uwb_mean_distance_to_ref_point_3D = mean(uwb_distances_to_ref_point_3D)
    uwb_rms_distance_to_ref_point_3D = root_mean_square(uwb_distances_to_ref_point_3D)
    uwb_max_distance_to_ref_point_3D = max(uwb_distances_to_ref_point_3D)
    uwb_std_3D_distances_to_ref_point = standard_deviation(uwb_distances_to_ref_point_3D)

    filtered_mean_distance_to_ref_point_2D = mean(filtered_distances_to_ref_point_2D)
    filtered_rms_distance_to_ref_point_2D = root_mean_square(filtered_distances_to_ref_point_2D)
    filtered_max_distance_to_ref_point_2D = max(filtered_distances_to_ref_point_2D)
    filtered_std_2D_distances_to_ref_point = standard_deviation(filtered_distances_to_ref_point_2D)
    filtered_mean_distance_to_ref_point_3D = mean(filtered_distances_to_ref_point_3D)
    filtered_rms_distance_to_ref_point_3D = root_mean_square(filtered_distances_to_ref_point_3D)
    filtered_max_distance_to_ref_point_3D = max(filtered_distances_to_ref_point_3D)
    filtered_std_3D_distances_to_ref_point = standard_deviation(filtered_distances_to_ref_point_3D)

    '''#############################################################
    #################### PRECISION EVALUATION ###################
    #############################################################'''
    # Get measurement centroid coordinates
    uwb_x_mean /= sample_count
    uwb_y_mean /= sample_count
    uwb_z_mean /= sample_count
    uwb_mean_point = [uwb_x_mean, uwb_y_mean, uwb_z_mean]
    filtered_x_mean /= sample_count
    filtered_y_mean /= sample_count
    filtered_z_mean /= sample_count
    filtered_mean_point = [filtered_x_mean, filtered_y_mean, filtered_z_mean]

    # Calculate distance of each sample point to measurement centroid point
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

    # Calculate mean, rms, max and std distance of samples to samples center point
    uwb_mean_distance_to_samples_center_point_2D = mean(uwb_distances_to_samples_center_point_2D)
    uwb_rms_distance_to_samples_center_point_2D = root_mean_square(uwb_distances_to_samples_center_point_2D)
    uwb_max_distance_to_samples_center_point_2D = max(uwb_distances_to_samples_center_point_2D)
    uwb_std_2D_distances_to_samples_center_point = standard_deviation(uwb_distances_to_samples_center_point_2D)
    uwb_mean_distance_to_samples_center_point_3D = mean(uwb_distances_to_samples_center_point_3D)
    uwb_rms_distance_to_samples_center_point_3D = root_mean_square(uwb_distances_to_samples_center_point_3D)
    uwb_max_distance_to_samples_center_point_3D = max(uwb_distances_to_samples_center_point_3D)
    uwb_std_3D_distances_to_samples_center_point = standard_deviation(uwb_distances_to_samples_center_point_3D)

    filtered_mean_distance_to_samples_center_point_2D = mean(filtered_distances_to_samples_center_point_2D)
    filtered_rms_distance_to_samples_center_point_2D = root_mean_square(filtered_distances_to_samples_center_point_2D)
    filtered_max_distance_to_samples_center_point_2D = max(filtered_distances_to_samples_center_point_2D)
    filtered_std_2D_distances_to_samples_center_point = standard_deviation(filtered_distances_to_samples_center_point_2D)
    filtered_mean_distance_to_samples_center_point_3D = mean(filtered_distances_to_samples_center_point_3D)
    filtered_rms_distance_to_samples_center_point_3D = root_mean_square(filtered_distances_to_samples_center_point_3D)
    filtered_max_distance_to_samples_center_point_3D = max(filtered_distances_to_samples_center_point_3D)
    filtered_std_3D_distances_to_samples_center_point = standard_deviation(filtered_distances_to_samples_center_point_3D)

    '''###################################################################
    #################### JITTER EVALUATION ###################
    ###################################################################'''
    # 2D Get mean, rms, max and std distance differences from measurements to their next ones
    uwb_mean_delta_distance_2D = mean(delta_distances(distance_between_two_points2D, uwb_points))
    uwb_rms_delta_distance_2D = root_mean_square(delta_distances(distance_between_two_points2D, uwb_points))
    uwb_max_delta_distance_2D = max(delta_distances(distance_between_two_points2D, uwb_points))
    uwb_std_delta_distance_2D = standard_deviation(delta_distances(distance_between_two_points2D, uwb_points))
    filtered_mean_delta_distance_2D = mean(delta_distances(distance_between_two_points2D, filtered_points))
    filtered_rms_delta_distance_2D = root_mean_square(delta_distances(distance_between_two_points2D, filtered_points))
    filtered_max_delta_distance_2D = max(delta_distances(distance_between_two_points2D, filtered_points))
    filtered_std_delta_distance_2D = standard_deviation(delta_distances(distance_between_two_points2D, filtered_points))

    # 3D Get mean, rms, max and std distance differences from measurements to their next ones
    uwb_mean_delta_distance_3D = mean(delta_distances(distance_between_two_points3D, uwb_points))
    uwb_rms_delta_distance_3D = root_mean_square(delta_distances(distance_between_two_points3D, uwb_points))
    uwb_max_delta_distance_3D = max(delta_distances(distance_between_two_points3D, uwb_points))
    uwb_std_delta_distance_3D = standard_deviation(delta_distances(distance_between_two_points3D, uwb_points))
    filtered_mean_delta_distance_3D = mean(delta_distances(distance_between_two_points3D, filtered_points))
    filtered_rms_delta_distance_3D = root_mean_square(delta_distances(distance_between_two_points3D, filtered_points))
    filtered_max_delta_distance_3D = max(delta_distances(distance_between_two_points3D, filtered_points))
    filtered_std_delta_distance_3D = standard_deviation(delta_distances(distance_between_two_points3D, filtered_points))

    return sample_count, uwb_x_mean, uwb_y_mean, uwb_z_mean, filtered_x_mean, filtered_y_mean, filtered_z_mean, uwb_mean_distance_to_ref_point_2D, uwb_mean_distance_to_ref_point_3D, uwb_rms_distance_to_ref_point_2D, uwb_rms_distance_to_ref_point_3D, uwb_max_distance_to_ref_point_2D, uwb_max_distance_to_ref_point_3D, filtered_mean_distance_to_ref_point_2D, filtered_mean_distance_to_ref_point_3D, filtered_rms_distance_to_ref_point_2D, filtered_rms_distance_to_ref_point_3D, filtered_max_distance_to_ref_point_2D, filtered_max_distance_to_ref_point_3D, uwb_std_2D_distances_to_ref_point, uwb_std_3D_distances_to_ref_point, filtered_std_2D_distances_to_ref_point, filtered_std_3D_distances_to_ref_point, uwb_mean_distance_to_samples_center_point_2D, uwb_mean_distance_to_samples_center_point_3D, uwb_rms_distance_to_samples_center_point_2D, uwb_rms_distance_to_samples_center_point_3D, uwb_max_distance_to_samples_center_point_2D, uwb_max_distance_to_samples_center_point_3D, uwb_std_2D_distances_to_samples_center_point, uwb_std_3D_distances_to_samples_center_point, filtered_mean_distance_to_samples_center_point_2D, filtered_mean_distance_to_samples_center_point_3D, filtered_rms_distance_to_samples_center_point_2D, filtered_rms_distance_to_samples_center_point_3D, filtered_max_distance_to_samples_center_point_2D, filtered_max_distance_to_samples_center_point_3D, filtered_std_2D_distances_to_samples_center_point, filtered_std_3D_distances_to_samples_center_point, uwb_mean_delta_distance_2D, uwb_mean_delta_distance_3D, uwb_rms_delta_distance_2D, uwb_rms_delta_distance_3D, uwb_max_delta_distance_2D, uwb_max_delta_distance_3D, uwb_std_delta_distance_2D, uwb_std_delta_distance_3D, filtered_mean_delta_distance_2D, filtered_mean_delta_distance_3D, filtered_rms_delta_distance_2D, filtered_rms_delta_distance_3D, filtered_max_delta_distance_2D, filtered_max_delta_distance_3D, filtered_std_delta_distance_2D, filtered_std_delta_distance_3D, uwb_x_coords, uwb_y_coords, uwb_z_coords, filtered_x_coords, filtered_y_coords, filtered_z_coords, uwb_points, filtered_points, uwb_x_mean, uwb_y_mean, uwb_z_mean, uwb_mean_point, filtered_x_mean, filtered_y_mean, filtered_z_mean, filtered_mean_point, raw_x_accs, raw_y_accs, raw_z_accs, filtered_x_accs, filtered_y_accs, filtered_z_accs

def get_values(uwb_positions, filtered_positions, raw_accelerations, filtered_accelerations):
    uwb_x_coordinates = []
    uwb_y_coordinates = []
    uwb_z_coordinates = []
    filtered_x_coordinates = []
    filtered_y_coordinates = []
    filtered_z_coordinates = []
    raw_x_accelerations = []
    raw_y_accelerations = []
    raw_z_accelerations = []
    filtered_x_accelerations = []
    filtered_y_accelerations = []
    filtered_z_accelerations = []

    for p in uwb_positions:
        uwb_x_coordinate = p[0]
        uwb_x_coordinates.append(uwb_x_coordinate)
        uwb_y_coordinate = p[1]
        uwb_y_coordinates.append(uwb_y_coordinate)
        uwb_z_coordinate = p[2]
        uwb_z_coordinates.append(uwb_z_coordinate)

    for p in filtered_positions:
        filtered_x_coordinate = p[0]
        filtered_x_coordinates.append(filtered_x_coordinate)
        filtered_y_coordinate = p[1]
        filtered_y_coordinates.append(filtered_y_coordinate)
        filtered_z_coordinate = p[2]
        filtered_z_coordinates.append(filtered_z_coordinate)

    for a in raw_accelerations:
        raw_x_acceleration = a[0]
        raw_x_accelerations.append(raw_x_acceleration)
        raw_y_acceleration = a[1]
        raw_y_accelerations.append(raw_y_acceleration)
        raw_z_acceleration = a[2]
        raw_z_accelerations.append(raw_z_acceleration)

    for a in filtered_accelerations:
        filtered_x_acceleration = a[0]
        filtered_x_accelerations.append(filtered_x_acceleration)
        filtered_y_acceleration = a[1]
        filtered_y_accelerations.append(filtered_y_acceleration)
        filtered_z_acceleration = a[2]
        filtered_z_accelerations.append(filtered_z_acceleration)

    return uwb_x_coordinates, uwb_y_coordinates, uwb_z_coordinates, filtered_x_coordinates, filtered_y_coordinates, filtered_z_coordinates, raw_x_accelerations, raw_y_accelerations, raw_z_accelerations, filtered_x_accelerations, filtered_y_accelerations, filtered_z_accelerations

# Values are plotted in a centered system
# That is, the reference point is set to (0,0,0) and the samples are scattered around it accordingly
def plot_coordinates(direction, uwb_points, filtered_points, uwb_mean_point, filtered_mean_point, reference_point):
    # Plot 2D
    fig = plt.figure()
    #ax0 = plt.subplot(211)
    ax1 = plt.subplot(111, projection='polar')
    plt.title("2D measurements at {}, {} in {} direction".format(reference_point[0], reference_point[1], direction))
    #plot_2d_cartesian(uwb_points, filtered_points, uwb_mean_point, filtered_mean_point, reference_point, ax0)
    plot_2d_polar(uwb_points, filtered_points, uwb_mean_point, filtered_mean_point, reference_point, direction, ax1)

    plt.show()

def plot_2d_cartesian(uwb_points, filtered_points, uwb_mean_point, filtered_mean_point, reference_point, axs):
    plt.xlabel = "X AXIS"
    plt.ylabel = "Y AXIS"
    # Plot samples
    for x, y, z in uwb_points:
        axs.scatter(x, y, label='Raw Coordinates', c='b', marker='^')
    for x, y, z in filtered_points:
        axs.scatter(x, y, label='Filtered Coordinates', c='r', marker='x')
    # Plot reference point
    axs.scatter(uwb_mean_point[0], uwb_mean_point[1], label='Raw Mean Coordinate', c='g', marker='^')
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
        axs.scatter(theta, r, label='Raw Coordinates', c='b', marker='^')
    for x, y, z in filtered_points:
        r, theta = cart2pol(x - reference_point[0], y - reference_point[1])
        axs.scatter(theta, r, label='Filtered Coordinates', c='r', marker='x')
    # Plot reference point
    r, theta = cart2pol(uwb_mean_point[0] - reference_point[0], uwb_mean_point[1] - reference_point[1])
    axs.scatter(theta, r, label='Raw Mean Coordinate', c='g', marker='^')
    r, theta = cart2pol(filtered_mean_point[0] - reference_point[0], filtered_mean_point[1] - reference_point[1])
    axs.scatter(theta, r, label='Filtered Mean Coordinate', c='g', marker='x')
    r, theta = cart2pol(0, 0)
    axs.scatter(theta, r, label='Reference point', c='g', marker='o')
    axs.quiver(0, 0, d[0], d[1], color='orange', scale=10, width=0.01)
    axs.set_rlabel_position(-125) # Change according to measurements placement in plot
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

def plot_line_charts(uwb_x_coords, uwb_y_coords, uwb_z_coords, filtered_x_coords, filtered_y_coords, filtered_z_coords, uwb_x_mean, uwb_y_mean, uwb_z_mean, filtered_x_mean, filtered_y_mean, filtered_z_mean, raw_x_accs, raw_y_accs, raw_z_accs, filtered_x_accs, filtered_y_accs, filtered_z_accs, sample_count, reference_point):
    fig = plt.figure()
    # Plot coordinates
    ax1 = plt.subplot(311)
    ax1.set_title("Raw and filtered positions")
    ax1.plot(range(sample_count), uwb_x_coords, label='Raw', c='b')
    ax1.plot(range(sample_count), filtered_x_coords, label='Filtered', c='r')
    ax1.axhline(uwb_x_mean, 0, 1, label='Raw Mean', c='b', linestyle='dashed')
    ax1.axhline(filtered_x_mean, 0, 1, label='Filtered Mean', c='r', linestyle='dashed')
    ax1.axhline(reference_point[0], 0, 1, label='Reference', c='g')
    #ax1.legend()
    ax1.legend(bbox_to_anchor=(1, 1.5), loc='upper center', ncol=1)
    ax1.set_ylabel('X')

    ax2 = plt.subplot(312)
    ax2.plot(range(sample_count), uwb_y_coords, label='Raw Y', c='b')
    ax2.plot(range(sample_count), filtered_y_coords, label='Filtered Y', c='r')
    ax2.axhline(uwb_y_mean, 0, 1, label='Raw Y Mean', c='b', linestyle='dashed')
    ax2.axhline(filtered_y_mean, 0, 1, label='Filtered Y Mean', c='r', linestyle='dashed')
    ax2.axhline(reference_point[1], 0, 1, label='User Y', c='g')
    #ax2.legend()
    ax2.set_ylabel('Y')

    ax3 = plt.subplot(313)
    ax3.plot(range(sample_count), uwb_z_coords, label='Raw Z', c='b')
    ax3.plot(range(sample_count), filtered_z_coords, label='Filtered Z', c='r')
    ax3.axhline(uwb_z_mean, 0, 1, label='Raw Z Mean', c='b', linestyle='dashed')
    ax3.axhline(filtered_z_mean, 0, 1, label='Filtered Z Mean', c='r', linestyle='dashed')
    ax3.axhline(reference_point[2], 0, 1, label='User Z', c='g')
    #ax3.legend()
    ax3.set_xlabel('Time')
    ax3.set_ylabel('Z')

    # Make sure that the y-axis scales are equal so that the viewer gets a better image of the jitter
    x_axis_y_min, x_axis_y_max = ax1.get_ylim()
    x_axis_y_range = x_axis_y_max - x_axis_y_min
    y_axis_y_min, y_axis_y_max = ax2.get_ylim()
    y_axis_y_range = y_axis_y_max - y_axis_y_min
    z_axis_y_min, z_axis_y_max = ax3.get_ylim()
    z_axis_y_range = z_axis_y_max - z_axis_y_min

    highest_range = max([x_axis_y_range, y_axis_y_range, z_axis_y_range])
    ax1.set_ylim(x_axis_y_min - 0.1, x_axis_y_min + highest_range)
    ax2.set_ylim(y_axis_y_min - 0.1, y_axis_y_min + highest_range)
    ax3.set_ylim(z_axis_y_min - 0.1, z_axis_y_min + highest_range)

    plt.show()

    # Plot accelerations
    fig = plt.figure()
    ax1 = plt.subplot(311)
    ax1.set_title("Raw and filtered accelerations")
    ax1.plot(range(sample_count), raw_x_accs, label='Raw X', c='b')
    ax1.plot(range(sample_count), filtered_x_accs, label='Filtered X', c='r')
    #ax1.legend()
    ax1.legend(bbox_to_anchor=(1, 1.5), loc='upper center', ncol=1)
    ax1.set_ylabel('X')

    ax2 = plt.subplot(312)
    ax2.plot(range(sample_count), raw_y_accs, label='Raw Y', c='b')
    ax2.plot(range(sample_count), filtered_y_accs, label='Filtered Y', c='r')
    #ax2.legend()
    ax2.set_ylabel('Y')

    ax3 = plt.subplot(313)
    ax3.plot(range(sample_count), raw_z_accs, label='Raw Z', c='b')
    ax3.plot(range(sample_count), filtered_z_accs, label='Filtered Z', c='r')
    ax3.axhline(2.0, 0, 1, label='Z Acc Threshold', c='g')
    ax3.axhline(-2.0, 0, 1, c='g')
    #ax3.legend()
    ax3.set_xlabel('Time')
    ax3.set_ylabel('Z')

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

    sample_count, uwb_x_mean, uwb_y_mean, uwb_z_mean, filtered_x_mean, filtered_y_mean, filtered_z_mean, uwb_mean_distance_to_ref_point_2D, uwb_mean_distance_to_ref_point_3D, uwb_rms_distance_to_ref_point_2D, uwb_rms_distance_to_ref_point_3D, uwb_max_distance_to_ref_point_2D, uwb_max_distance_to_ref_point_3D, filtered_mean_distance_to_ref_point_2D, filtered_mean_distance_to_ref_point_3D, filtered_rms_distance_to_ref_point_2D, filtered_rms_distance_to_ref_point_3D, filtered_max_distance_to_ref_point_2D, filtered_max_distance_to_ref_point_3D, uwb_std_2D_distances_to_ref_point, uwb_std_3D_distances_to_ref_point, filtered_std_2D_distances_to_ref_point, filtered_std_3D_distances_to_ref_point, uwb_mean_distance_to_samples_center_point_2D, uwb_mean_distance_to_samples_center_point_3D, uwb_rms_distance_to_samples_center_point_2D, uwb_rms_distance_to_samples_center_point_3D, uwb_max_distance_to_samples_center_point_2D, uwb_max_distance_to_samples_center_point_3D, uwb_std_2D_distances_to_samples_center_point, uwb_std_3D_distances_to_samples_center_point, filtered_mean_distance_to_samples_center_point_2D, filtered_mean_distance_to_samples_center_point_3D, filtered_rms_distance_to_samples_center_point_2D, filtered_rms_distance_to_samples_center_point_3D, filtered_max_distance_to_samples_center_point_2D, filtered_max_distance_to_samples_center_point_3D, filtered_std_2D_distances_to_samples_center_point, filtered_std_3D_distances_to_samples_center_point, uwb_mean_delta_distance_2D, uwb_mean_delta_distance_3D, uwb_rms_delta_distance_2D, uwb_rms_delta_distance_3D, uwb_max_delta_distance_2D, uwb_max_delta_distance_3D, uwb_std_delta_distance_2D, uwb_std_delta_distance_3D, filtered_mean_delta_distance_2D, filtered_mean_delta_distance_3D, filtered_rms_delta_distance_2D, filtered_rms_delta_distance_3D, filtered_max_delta_distance_2D, filtered_max_delta_distance_3D, filtered_std_delta_distance_2D, filtered_std_delta_distance_3D, uwb_x_coords, uwb_y_coords, uwb_z_coords, filtered_x_coords, filtered_y_coords, filtered_z_coords, uwb_points, filtered_points, uwb_x_mean, uwb_y_mean, uwb_z_mean, uwb_mean_point, filtered_x_mean, filtered_y_mean, filtered_z_mean, filtered_mean_point, raw_x_accs, raw_y_accs, raw_z_accs, filtered_x_accs, filtered_y_accs, filtered_z_accs = evaluate_data(filename, reference_point)
    print("\n")
    print("GENERAL INFORMATION")
    print("Direction: {}, Samples collected: {}".format(direction, sample_count))
    print("X Reference: {}, Y Reference: {}, Z Reference: {}".format(x_reference, y_reference, z_reference))
    print("UWB X Mean: {:.3f}, UWB Y Mean: {:.3f}, UWB Z Mean: {:.3f}".format(uwb_x_mean, uwb_y_mean, uwb_z_mean))
    print("Filtered X Mean: {:.3f}, Filtered Y Mean: {:.3f}, Filtered Z Mean: {:.3f}".format(filtered_x_mean, filtered_y_mean, filtered_z_mean))
    print("\n")

    print("ACCURACY RESULTS")
    print("Mean | RMS | Max | Std raw distances to reference point 2D: {:.3f} | {:.3f} | {:.3f} | {:.3f}m".format(uwb_mean_distance_to_ref_point_2D, uwb_rms_distance_to_ref_point_2D, uwb_max_distance_to_ref_point_2D, uwb_std_2D_distances_to_ref_point))
    print("Mean | RMS | Max | Std raw distances to reference point 3D: {:.3f} | {:.3f} | {:.3f} | {:.3f}m".format(uwb_mean_distance_to_ref_point_3D, uwb_rms_distance_to_ref_point_3D, uwb_max_distance_to_ref_point_3D, uwb_std_3D_distances_to_ref_point))
    print("Mean | RMS | Max | Std filtered distances to reference point 2D: {:.3f} | {:.3f} | {:.3f} | {:.3f}m".format(filtered_mean_distance_to_ref_point_2D, filtered_rms_distance_to_ref_point_2D, filtered_max_distance_to_ref_point_2D, filtered_std_2D_distances_to_ref_point))
    print("Mean | RMS | Max | Std filtered distances to reference point 3D: {:.3f} | {:.3f} | {:.3f} | {:.3f}m".format(filtered_mean_distance_to_ref_point_3D, filtered_rms_distance_to_ref_point_3D, filtered_max_distance_to_ref_point_3D, filtered_std_3D_distances_to_ref_point))
    print("\n")

    print("PRECISION RESULTS")
    print("Mean | RMS | Max | Std raw distances to measurement centroid 2D: {:.3f} | {:.3f} | {:.3f} | {:.3f}m".format(uwb_mean_distance_to_samples_center_point_2D, uwb_rms_distance_to_samples_center_point_2D, uwb_max_distance_to_samples_center_point_2D, uwb_std_2D_distances_to_samples_center_point))
    print("Mean | RMS | Max | Std raw distances to measurement centroid 3D: {:.3f} | {:.3f} | {:.3f} | {:.3f}m".format(uwb_mean_distance_to_samples_center_point_3D, uwb_rms_distance_to_samples_center_point_3D, uwb_max_distance_to_samples_center_point_3D, uwb_std_3D_distances_to_samples_center_point))
    print("Mean | RMS | Max | Std filtered distances to measurement centroid 2D: {:.3f} | {:.3f} | {:.3f} | {:.3f}m".format(filtered_mean_distance_to_samples_center_point_2D, filtered_rms_distance_to_samples_center_point_2D, filtered_max_distance_to_samples_center_point_2D, filtered_std_2D_distances_to_samples_center_point))
    print("Mean | RMS | Max | Std filtered distances to measurement centroid 3D: {:.3f} | {:.3f} | {:.3f} | {:.3f}m".format(filtered_mean_distance_to_samples_center_point_3D, filtered_rms_distance_to_samples_center_point_3D, filtered_max_distance_to_samples_center_point_3D, filtered_std_3D_distances_to_samples_center_point))
    print("\n")

    print("JITTER RESULTS")
    print("Mean | RMS | Max | Std raw delta distances 2D: {:.3f} | {:.3f} | {:.3f} | {:.3f}m".format(uwb_mean_delta_distance_2D, uwb_rms_delta_distance_2D, uwb_max_delta_distance_2D, uwb_std_delta_distance_2D))
    print("Mean | RMS | Max | Std raw delta distances 3D: {:.3f} | {:.3f} | {:.3f} | {:.3f}m".format(uwb_mean_delta_distance_3D, uwb_rms_delta_distance_3D, uwb_max_delta_distance_3D, uwb_std_delta_distance_3D))
    print("Mean | RMS | Max | Std filtered delta distances 2D: {:.3f} | {:.3f} | {:.3f} | {:.3f}m".format(filtered_mean_delta_distance_2D, filtered_rms_delta_distance_2D, filtered_max_delta_distance_2D, filtered_std_delta_distance_2D))
    print("Mean | RMS | Max | Std filtered delta distances 3D: {:.3f} | {:.3f} | {:.3f} | {:.3f}m".format(filtered_mean_delta_distance_3D, filtered_rms_delta_distance_3D, filtered_max_delta_distance_3D, filtered_std_delta_distance_3D))
    print("\n")

    print("All values in meter units")

    # Plot 2D coordinates in cartesian and polar coordinate system
    plot_coordinates(direction, uwb_points, filtered_points, uwb_mean_point, filtered_mean_point, reference_point)

    # Plot axis line charts regardless of fixed or movement measurement
    plot_line_charts(uwb_x_coords, uwb_y_coords, uwb_z_coords, filtered_x_coords, filtered_y_coords, filtered_z_coords, uwb_x_mean, uwb_y_mean, uwb_z_mean, filtered_x_mean, filtered_y_mean, filtered_z_mean, raw_x_accs, raw_y_accs, raw_z_accs, filtered_x_accs, filtered_y_accs, filtered_z_accs, sample_count, reference_point)
