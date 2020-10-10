import fnmatch
import matplotlib.pyplot as plt
import os
import sys
from numpy import arctan2, diff, linspace, mean, pi, std, sqrt, square

def print_no_document_found_error():
    print("ERROR: No .txt document found")
    print("Please add a .txt document as first argument when calling this script")
    print("Note: That .txt document has had to be created by the related \'LocationApp\' for Android")
    print("Exiting")
    print("\n")

# Returns the amount of samples recorded
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
    #print("Self std: {}".format(standard_deviation))
    #print("Numpy std: {}".format(std(samples)))
    #print("RMS: {}".format(root_mean_square_error(l1)))
    #return std
    return std(samples)

def root_mean_square_error(data):
    return sqrt(mean(square(data)))

def evaluate_and_plot_data(directory):
    files = fnmatch.filter(os.listdir(directory), '*.txt')
    reference_positions = []
    uwb_mean_positions = []
    filtered_mean_positions = []

    # Accuracy
    uwb_mean_distances_to_reference_point_2D = []
    filtered_mean_distances_to_reference_point_2D = []
    uwb_mean_distances_to_reference_point_3D = []
    filtered_mean_distances_to_reference_point_3D = []
    uwb_mean_distances_to_reference_point_stds_2D = []
    filtered_mean_distances_to_reference_point_stds_2D = []
    uwb_mean_distances_to_reference_point_stds_3D = []
    filtered_mean_distances_to_reference_point_stds_3D = []

    # Precision
    uwb_mean_distances_to_measurement_mean_2D = []
    filtered_mean_distances_to_measurement_mean_2D = []
    uwb_mean_distances_to_measurement_mean_3D = []
    filtered_mean_distances_to_measurement_mean_3D = []
    uwb_mean_distances_to_measurement_mean_stds_2D = []
    filtered_mean_distances_to_measurement_mean_stds_2D = []
    uwb_mean_distances_to_measurement_mean_stds_3D = []
    filtered_mean_distances_to_measurement_mean_stds_3D = []

    # Motion Sickness
    uwb_mean_delta_distances_2D = []
    filtered_mean_delta_distances_2D = []
    uwb_mean_delta_distances_3D = []
    filtered_mean_delta_distances_3D = []

    for filename in files:
        direction = filename.split('(')[0]
        x_reference = float((filename.split('(')[1].split(')')[0].split('_')[0]).replace(',', '.'))
        y_reference = float((filename.split('(')[1].split(')')[0].split('_')[1]).replace(',', '.'))
        z_reference = float((filename.split('(')[1].split(')')[0].split('_')[2]).replace(',', '.'))
        reference_position = [x_reference, y_reference, z_reference]
        reference_positions.append(reference_position)
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
        with open(filename) as f:
            for line in f:
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
                uwb_axis_distance_x = uwb_x - reference_position[0]
                uwb_axis_distance_y = uwb_y - reference_position[1]
                uwb_axis_distance_z = uwb_z - reference_position[2]
                uwb_distances_x.append(uwb_axis_distance_x)
                uwb_distances_y.append(uwb_axis_distance_y)
                uwb_distances_z.append(uwb_axis_distance_z)
                filtered_axis_distance_x = filtered_x - reference_position[0]
                filtered_axis_distance_y = filtered_y - reference_position[1]
                filtered_axis_distance_z = filtered_z - reference_position[2]
                filtered_distances_x.append(filtered_axis_distance_x)
                filtered_distances_y.append(filtered_axis_distance_y)
                filtered_distances_z.append(filtered_axis_distance_z)

                # Make up coordinate and add to list
                uwb_sample_point = [uwb_x, uwb_y, uwb_z]
                uwb_points.append(uwb_sample_point)
                filtered_sample_point = [filtered_x, filtered_y, filtered_z]
                filtered_points.append(filtered_sample_point)

                # Calculate distance of sample point to reference point in 2D and 3D and add to distances lists
                uwb_distance_to_ref_point_2D = distance_between_two_points2D(uwb_sample_point, reference_position)
                uwb_distances_to_ref_point_2D.append(uwb_distance_to_ref_point_2D)
                uwb_distance_to_ref_point_3D = distance_between_two_points3D(uwb_sample_point, reference_position)
                uwb_distances_to_ref_point_3D.append(uwb_distance_to_ref_point_3D)
                filtered_distance_to_ref_point_2D = distance_between_two_points2D(filtered_sample_point, reference_position)
                filtered_distances_to_ref_point_2D.append(filtered_distance_to_ref_point_2D)
                filtered_distance_to_ref_point_3D = distance_between_two_points3D(filtered_sample_point, reference_position)
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

        # Calculate mean distance of samples to reference point
        uwb_mean_distance_to_ref_point_2D = sum(uwb_distances_to_ref_point_2D) / sample_count
        uwb_mean_distances_to_reference_point_2D.append(uwb_mean_distance_to_ref_point_2D)
        uwb_mean_distance_to_ref_point_3D = sum(uwb_distances_to_ref_point_3D) / sample_count
        uwb_mean_distances_to_reference_point_3D.append(uwb_mean_distance_to_ref_point_3D)
        filtered_mean_distance_to_ref_point_2D = sum(filtered_distances_to_ref_point_2D) / sample_count
        filtered_mean_distances_to_reference_point_2D.append(filtered_mean_distance_to_ref_point_2D)
        filtered_mean_distance_to_ref_point_3D = sum(filtered_distances_to_ref_point_3D) / sample_count
        filtered_mean_distances_to_reference_point_3D.append(filtered_mean_distance_to_ref_point_3D)

        # Get standard deviation of samples on each axis x, y and z
        # sample_x_std = standard_deviation(sample_x_coords, sample_x_mean, sample_count)
        # sample_y_std = standard_deviation(sample_y_coords, sample_y_mean, sample_count)
        # sample_z_std = standard_deviation(sample_z_coords, sample_z_mean, sample_count)

        # Get distance standard deviations of distance to reference point
        uwb_std_2D_distances_to_ref_point = standard_deviation(uwb_distances_to_ref_point_2D, uwb_mean_distance_to_ref_point_2D, sample_count)
        uwb_mean_distances_to_reference_point_stds_2D.append(uwb_std_2D_distances_to_ref_point)
        uwb_std_3D_distances_to_ref_point = standard_deviation(uwb_distances_to_ref_point_3D, uwb_mean_distance_to_ref_point_3D, sample_count)
        uwb_mean_distances_to_reference_point_stds_3D.append(uwb_std_3D_distances_to_ref_point)
        filtered_std_2D_distances_to_ref_point = standard_deviation(filtered_distances_to_ref_point_2D, filtered_mean_distance_to_ref_point_2D, sample_count)
        filtered_mean_distances_to_reference_point_stds_2D.append(filtered_std_2D_distances_to_ref_point)
        filtered_std_3D_distances_to_ref_point = standard_deviation(filtered_distances_to_ref_point_3D, filtered_mean_distance_to_ref_point_3D, sample_count)
        filtered_mean_distances_to_reference_point_stds_3D.append(filtered_std_3D_distances_to_ref_point)

        '''#############################################################
        #################### PRECISION EVALUATION ###################
        #############################################################'''
        # Get samples center coordinates
        uwb_x_mean /= sample_count
        uwb_y_mean /= sample_count
        uwb_z_mean /= sample_count
        uwb_mean_point = [uwb_x_mean, uwb_y_mean, uwb_z_mean]
        uwb_mean_positions.append(uwb_mean_point)
        filtered_x_mean /= sample_count
        filtered_y_mean /= sample_count
        filtered_z_mean /= sample_count
        filtered_mean_point = [filtered_x_mean, filtered_y_mean, filtered_z_mean]
        filtered_mean_positions.append(filtered_mean_point)

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

        # Calculate mean distance of samples to samples center point
        uwb_mean_distance_to_samples_center_point_2D = sum(uwb_distances_to_samples_center_point_2D) / sample_count
        uwb_mean_distances_to_measurement_mean_2D.append(uwb_mean_distance_to_samples_center_point_2D)
        uwb_mean_distance_to_samples_center_point_3D = sum(uwb_distances_to_samples_center_point_3D) / sample_count
        uwb_mean_distances_to_measurement_mean_3D.append(uwb_mean_distance_to_samples_center_point_3D)
        filtered_mean_distance_to_samples_center_point_2D = sum(filtered_distances_to_samples_center_point_2D) / sample_count
        filtered_mean_distances_to_measurement_mean_2D.append(filtered_mean_distance_to_samples_center_point_2D)
        filtered_mean_distance_to_samples_center_point_3D = sum(filtered_distances_to_samples_center_point_3D) / sample_count
        filtered_mean_distances_to_measurement_mean_3D.append(filtered_mean_distance_to_samples_center_point_3D)

        # Calculate distance standard deviations of distance to samples center point
        uwb_std_2D_distances_to_samples_center_point = standard_deviation(uwb_distances_to_samples_center_point_2D, uwb_mean_distance_to_samples_center_point_2D, sample_count)
        uwb_mean_distances_to_measurement_mean_stds_2D.append(uwb_std_2D_distances_to_samples_center_point)
        uwb_std_3D_distances_to_samples_center_point = standard_deviation(uwb_distances_to_samples_center_point_3D, uwb_mean_distance_to_samples_center_point_3D, sample_count)
        uwb_mean_distances_to_measurement_mean_stds_3D.append(uwb_std_3D_distances_to_samples_center_point)
        filtered_std_2D_distances_to_samples_center_point = standard_deviation(filtered_distances_to_samples_center_point_2D, filtered_mean_distance_to_samples_center_point_2D, sample_count)
        filtered_mean_distances_to_measurement_mean_stds_2D.append(filtered_std_2D_distances_to_samples_center_point)
        filtered_std_3D_distances_to_samples_center_point = standard_deviation(filtered_distances_to_samples_center_point_3D, filtered_mean_distance_to_samples_center_point_3D, sample_count)
        filtered_mean_distances_to_measurement_mean_stds_3D.append(filtered_std_3D_distances_to_samples_center_point)

        ''' TODO: Take mean distance or distance from centroid to reference point? '''
        # Calculate distances from samples center point to reference point
        uwb_distance_samples_center_point_to_ref_point_2D = distance_between_two_points2D(uwb_mean_point, reference_position)
        uwb_distance_samples_center_point_to_ref_point_3D = distance_between_two_points3D(uwb_mean_point, reference_position)
        filtered_distance_samples_center_point_to_ref_point_2D = distance_between_two_points2D(filtered_mean_point, reference_position)
        filtered_distance_samples_center_point_to_ref_point_3D = distance_between_two_points3D(filtered_mean_point, reference_position)

        # Experimental
        '''###################################################################
        #################### MOTION SICKNESS EVALUATION ###################
        ###################################################################'''
        # 2D Get mean, min and max distance differences from samples to their next ones
        uwb_delta_distances_2D = diff(uwb_distances_to_ref_point_2D)
        uwb_delta_distances_2D =  [abs(n) for n in uwb_delta_distances_2D]
        uwb_mean_delta_distance_2D = sum(uwb_delta_distances_2D) / (sample_count - 1)
        uwb_mean_delta_distances_2D.append(uwb_mean_delta_distance_2D)
        filtered_delta_distances_2D = diff(filtered_distances_to_ref_point_2D)
        filtered_delta_distances_2D =  [abs(n) for n in filtered_delta_distances_2D]
        filtered_mean_delta_distance_2D = sum(filtered_delta_distances_2D) / (sample_count - 1)
        filtered_mean_delta_distances_2D.append(filtered_mean_delta_distance_2D)

        # 3D Get mean, min and max distance differences from samples to their next ones
        uwb_delta_distances_3D = diff(uwb_distances_to_ref_point_3D)
        uwb_delta_distances_3D = [abs(n) for n in uwb_delta_distances_3D]
        uwb_mean_delta_distance_3D = sum(uwb_delta_distances_3D) / (sample_count - 1)
        uwb_mean_delta_distances_3D.append(uwb_mean_delta_distance_3D)
        filtered_delta_distances_3D = diff(filtered_distances_to_ref_point_3D)
        filtered_delta_distances_3D = [abs(n) for n in filtered_delta_distances_3D]
        filtered_mean_delta_distance_3D = sum(filtered_delta_distances_3D) / (sample_count - 1)
        filtered_mean_delta_distances_3D.append(filtered_mean_delta_distance_3D)
    
    
    plot(reference_positions, uwb_mean_positions, filtered_mean_positions)
    

def get_data(filename):
    uwb_positions = []
    filtered_positions = []
    raw_accelerations = []
    filtered_accelerations = []

    with open(filename) as f:
        for line in f:
            uwb_position = line.split('|')[0]
            filtered_position = line.split('|')[1]
            raw_acceleration = line.split('|')[2]
            filtered_acceleration = line.split('|')[3]

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

            # Add positions to lists
            uwb_positions.append([uwb_x, uwb_y, uwb_z])
            filtered_positions.append([filtered_x, filtered_y, filtered_z])
            raw_accelerations.append([raw_acceleration_x, raw_acceleration_y, raw_acceleration_z])
            filtered_accelerations.append([filtered_acceleration_x, filtered_acceleration_y, filtered_acceleration_z])
    
    return uwb_positions, filtered_positions, raw_accelerations, filtered_accelerations

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

def plot(reference_positions, uwb_positions, filtered_positions):
    fig = plt.figure(figsize=(7, 13))
    ax0 = plt.subplot(211)
    plt.title("Positions accuracy visualization")
    plot_2d_cartesian(reference_positions, uwb_positions, filtered_positions, ax0)
    #plot_3D(uwb_positions, filtered_positions, ax1)
    plt.show()
    #plot_line_chart(uwb_positions, filtered_positions, raw_accelerations, filtered_accelerations, sample_count)

def plot_2d_cartesian(reference_positions, uwb_positions, filtered_positions, axs):
    plt.xlabel = "X Axis"
    plt.ylabel = "Y Axis"
    # Plot 2D reference positions
    for x, y, z in reference_positions:
        axs.scatter(x, y, label='Reference positions', c='g', marker='o')
    # Plot 2D raw UWB positions
    for x, y, z in uwb_positions:
        axs.scatter(x, y, label='UWB positions', c='b', marker='^')
    # Plot 2D filtered positions
    for x, y, z in filtered_positions:
        axs.scatter(x, y, label='Filtered positions', c='r', marker='x')
    axs.grid(True)
    legend(axs)

def plot_3D(uwb_positions, filtered_positions, axs):
    axs.set_xlabel('X Axis')
    axs.set_ylabel('Y Axis')
    axs.set_zlabel('Z Axis')

    # Plot 3D raw uwb positions
    for x, y, z in uwb_positions:
        axs.scatter(x, y, z, c='b', marker='^')
    # Plot 3D filtered positions
    for x, y, z in filtered_positions:
        axs.scatter(x, y, z, c='r', marker='x')

def plot_line_chart(uwb_positions, filtered_positions, raw_accelerations, filtered_accelerations, sample_count):
    fig = plt.figure()
    uwb_x_coordinates, uwb_y_coordinates, uwb_z_coordinates, filtered_x_coordinates, filtered_y_coordinates, filtered_z_coordinates, raw_x_accelerations, raw_y_accelerations, raw_z_accelerations, filtered_x_accelerations, filtered_y_accelerations, filtered_z_accelerations = get_values(uwb_positions, filtered_positions, raw_accelerations, filtered_accelerations)
    plt.title("Raw UWB and filtered positions")
    # Plot coordinates
    ax1 = fig.add_subplot(311)
    ax1.plot(range(sample_count), uwb_x_coordinates, label='UWB X', c='b')
    ax1.plot(range(sample_count), filtered_x_coordinates, label='Filtered X', c='r')
    ax1.legend()

    ax2 = fig.add_subplot(312)
    ax2.plot(range(sample_count), uwb_y_coordinates, label='UWB Y', c='b')
    ax2.plot(range(sample_count), filtered_y_coordinates, label='Filtered Y', c='r')
    ax2.legend()

    ax3 = fig.add_subplot(313)
    ax3.plot(range(sample_count), uwb_z_coordinates, label='UWB Z', c='b')
    ax3.plot(range(sample_count), filtered_z_coordinates, label='Filtered Z', c='r')
    ax3.axhline(1.67, 0, 1, label='User Height', c='g')
    ax3.legend()

    plt.show()

    # Plot accelerations
    fig = plt.figure()
    plt.title("Raw and filtered accelerations")
    ax1 = fig.add_subplot(311)
    ax1.plot(range(sample_count), raw_x_accelerations, label='Raw X', c='b')
    ax1.plot(range(sample_count), filtered_x_accelerations, label='Filtered X', c='r')
    ax1.legend()

    ax2 = fig.add_subplot(312)
    ax2.plot(range(sample_count), raw_y_accelerations, label='Raw Y', c='b')
    ax2.plot(range(sample_count), filtered_y_accelerations, label='Filtered Y', c='r')
    ax2.legend()

    ax3 = fig.add_subplot(313)
    ax3.plot(range(sample_count), raw_z_accelerations, label='Raw Z', c='b')
    ax3.plot(range(sample_count), filtered_z_accelerations, label='Filtered Z', c='r')
    ax3.axhline(2.0, 0, 1, label='Z Acc Threshold', c='g')
    ax3.axhline(-2.0, 0, 1, c='g')
    ax3.legend()

    plt.show()

# Plot a legend and remove duplicate legend elements
def legend(axs):
    handles, labels = axs.get_legend_handles_labels()
    # Python verison >= 3.7 only
    by_label = dict(zip(labels, handles))
    # For Python versions < 3.7 use below 2 code lines
    # from collections import OrderedDict
    # by_label = OrderedDict(zip(labels, handles))
    axs.legend(by_label.values(), by_label.keys())

if __name__ == "__main__":
    try:
        directory = sys.argv[1]
    except IndexError:
        print_no_document_found_error()
        exit(1)

    evaluate_and_plot_data(directory)
    
    #sample_count = get_sample_count(filename)
    #uwb_positions, filtered_positions, raw_accelerations, filtered_accelerations = get_data(filename)
    #plot(uwb_positions, filtered_positions, raw_accelerations, filtered_accelerations, sample_count)
