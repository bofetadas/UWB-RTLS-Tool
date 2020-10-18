import fnmatch
import matplotlib.pyplot as plt
import os
import sys
from numpy import diff, mean, std, sqrt, square

def print_no_document_found_error():
    print("ERROR: No .txt document found")
    print("Please add a .txt document as first argument when calling this script")
    print("Note: That .txt document has had to be created by the related \'LocationApp\' for Android")
    print("Exiting")
    print("\n")

# Returns the amount of measurements recorded
def get_measurement_count(filename):
    with open(filename) as f:
        for i, l in enumerate(f):
            pass
    return i + 1

def distance_between_two_points2D(measurement_point, reference_point):
    return sqrt(pow(measurement_point[0] - reference_point[0], 2) + pow(measurement_point[1] - reference_point[1], 2))

def distance_between_two_points3D(measurement_point, reference_point):
    return sqrt(pow(measurement_point[0] - reference_point[0], 2) + pow(measurement_point[1] - reference_point[1], 2) + pow(measurement_point[2] - reference_point[2], 2))

def standard_deviation(measurements, measurements_mean, measurement_count):
    l = []
    l1 = []
    for m in measurements:
        squared = pow(m - measurements_mean, 2)
        l.append(squared)
        l1.append(m - measurements_mean)
    standard_deviation = sqrt(sum(l) / measurement_count)
    #print("Self std: {}".format(standard_deviation))
    #print("Numpy std: {}".format(std(measurement)))
    #print("RMS: {}".format(root_mean_square_error(l1)))
    #return std
    return std(measurements)

def root_mean_square_error(data):
    return sqrt(mean(square(data)))

def evaluate_and_plot_data(directory):
    files = fnmatch.filter(os.listdir(directory), '*.txt')
    reference_positions = []
    uwb_mean_positions = []
    filtered_mean_positions = []
    uwb_distances_x = []
    uwb_distances_y = []
    uwb_distances_z = []
    filtered_distances_x = []
    filtered_distances_y = []
    filtered_distances_z = []
    uwb_positions_dictionary = {}
    filtered_positions_dictionary = {}

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
    uwb_mean_distances_to_measurement_centroid_2D = []
    filtered_mean_distances_to_measurement_centroid_2D = []
    uwb_mean_distances_to_measurement_centroid_3D = []
    filtered_mean_distances_to_measurement_centroid_3D = []
    uwb_mean_distances_to_measurement_centroid_stds_2D = []
    filtered_mean_distances_to_measurement_centroid_stds_2D = []
    uwb_mean_distances_to_measurement_centroid_stds_3D = []
    filtered_mean_distances_to_measurement_centroid_stds_3D = []

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

        # List holding all measurement points
        uwb_measurement_points = []
        filtered_measurement_points = []

        # Lists holding all distances from measurement points to reference point
        uwb_distances_to_ref_point_2D = []
        uwb_distances_to_ref_point_3D = []
        filtered_distances_to_ref_point_2D = []
        filtered_distances_to_ref_point_3D = []

        # Lists holding all distances from measurement points to measurement centroids
        uwb_distances_to_measurement_centroid_2D = []
        uwb_distances_to_measurement_centroid_3D = []
        filtered_distances_to_measurement_centroid_2D = []
        filtered_distances_to_measurement_centroid_3D = []
        
        # Experimental
        # Lists holding all distance differences from positions to their next one
        uwb_delta_distances_2D = []
        uwb_delta_distances_3D = []
        filtered_delta_distances_2D = []
        filtered_delta_distances_3D = []

        ''' Go! '''
        # Get amount of measurements collected
        measurement_count = get_measurement_count(filename)
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

                # Make up coordinate and add to list
                uwb_measurement_point = [uwb_x, uwb_y, uwb_z]
                uwb_measurement_points.append(uwb_measurement_point)
                filtered_measurement_point = [filtered_x, filtered_y, filtered_z]
                filtered_measurement_points.append(filtered_measurement_point)

                # Calculate distances on each axis
                uwb_distance_x = uwb_x - reference_position[0]
                uwb_distances_x.append(uwb_distance_x)
                uwb_distance_y = uwb_y - reference_position[1]
                uwb_distances_y.append(uwb_distance_y)
                uwb_distance_z = uwb_z - reference_position[2]
                uwb_distances_z.append(uwb_distance_z)
                filtered_distance_x = filtered_x - reference_position[0]
                filtered_distances_x.append(filtered_distance_x)
                filtered_distance_y = filtered_y - reference_position[1]
                filtered_distances_y.append(filtered_distance_y)
                filtered_distance_z = filtered_z - reference_position[2]
                filtered_distances_z.append(filtered_distance_z)

                # Calculate distance of measurement point to reference point in 2D and 3D and add to distances lists
                uwb_distance_to_ref_point_2D = distance_between_two_points2D(uwb_measurement_point, reference_position)
                uwb_distances_to_ref_point_2D.append(uwb_distance_to_ref_point_2D)
                uwb_distance_to_ref_point_3D = distance_between_two_points3D(uwb_measurement_point, reference_position)
                uwb_distances_to_ref_point_3D.append(uwb_distance_to_ref_point_3D)
                filtered_distance_to_ref_point_2D = distance_between_two_points2D(filtered_measurement_point, reference_position)
                filtered_distances_to_ref_point_2D.append(filtered_distance_to_ref_point_2D)
                filtered_distance_to_ref_point_3D = distance_between_two_points3D(filtered_measurement_point, reference_position)
                filtered_distances_to_ref_point_3D.append(filtered_distance_to_ref_point_3D)
        
        '''#############################################################
        #################### ACCURACY EVALUATION ####################
        #############################################################'''

        # Calculate mean distance of measurements to reference point
        uwb_mean_distance_to_ref_point_2D = sum(uwb_distances_to_ref_point_2D) / measurement_count
        uwb_mean_distances_to_reference_point_2D.append(uwb_mean_distance_to_ref_point_2D)
        uwb_mean_distance_to_ref_point_3D = sum(uwb_distances_to_ref_point_3D) / measurement_count
        uwb_mean_distances_to_reference_point_3D.append(uwb_mean_distance_to_ref_point_3D)
        filtered_mean_distance_to_ref_point_2D = sum(filtered_distances_to_ref_point_2D) / measurement_count
        filtered_mean_distances_to_reference_point_2D.append(filtered_mean_distance_to_ref_point_2D)
        filtered_mean_distance_to_ref_point_3D = sum(filtered_distances_to_ref_point_3D) / measurement_count
        filtered_mean_distances_to_reference_point_3D.append(filtered_mean_distance_to_ref_point_3D)

        # Get distance standard deviations of distance to reference point
        uwb_std_2D_distances_to_ref_point = standard_deviation(uwb_distances_to_ref_point_2D, uwb_mean_distance_to_ref_point_2D, measurement_count)
        uwb_mean_distances_to_reference_point_stds_2D.append(uwb_std_2D_distances_to_ref_point)
        uwb_std_3D_distances_to_ref_point = standard_deviation(uwb_distances_to_ref_point_3D, uwb_mean_distance_to_ref_point_3D, measurement_count)
        uwb_mean_distances_to_reference_point_stds_3D.append(uwb_std_3D_distances_to_ref_point)
        filtered_std_2D_distances_to_ref_point = standard_deviation(filtered_distances_to_ref_point_2D, filtered_mean_distance_to_ref_point_2D, measurement_count)
        filtered_mean_distances_to_reference_point_stds_2D.append(filtered_std_2D_distances_to_ref_point)
        filtered_std_3D_distances_to_ref_point = standard_deviation(filtered_distances_to_ref_point_3D, filtered_mean_distance_to_ref_point_3D, measurement_count)
        filtered_mean_distances_to_reference_point_stds_3D.append(filtered_std_3D_distances_to_ref_point)

        '''#############################################################
        #################### PRECISION EVALUATION ###################
        #############################################################'''
        # Get measurements' centroid and add to dictionary
        uwb_x_mean /= measurement_count
        uwb_y_mean /= measurement_count
        uwb_z_mean /= measurement_count
        uwb_measurements_centroid = [uwb_x_mean, uwb_y_mean, uwb_z_mean]
        uwb_reference_position_string = [str(coordinate) for coordinate in reference_position]
        uwb_reference_position_string = ','.join(uwb_reference_position_string)
        uwb_positions_dictionary.setdefault(uwb_reference_position_string, [])
        uwb_positions_dictionary[uwb_reference_position_string].append(uwb_measurements_centroid)

        filtered_x_mean /= measurement_count
        filtered_y_mean /= measurement_count
        filtered_z_mean /= measurement_count
        filtered_measurements_centroid = [filtered_x_mean, filtered_y_mean, filtered_z_mean]
        filtered_reference_position_string = [str(coordinate) for coordinate in reference_position]
        filtered_reference_position_string = ','.join(filtered_reference_position_string)
        filtered_positions_dictionary.setdefault(filtered_reference_position_string, [])
        filtered_positions_dictionary[filtered_reference_position_string].append(filtered_measurements_centroid)

        # Calculate distance of each measurement point to measurements centroid
        for uwb_measurement_point in uwb_measurement_points:
            uwb_distance_to_measurement_centroid_2D = distance_between_two_points2D(uwb_measurement_point, uwb_measurements_centroid)
            uwb_distances_to_measurement_centroid_2D.append(uwb_distance_to_measurement_centroid_2D)
            uwb_distance_to_measurement_centroid_3D = distance_between_two_points3D(uwb_measurement_point, uwb_measurements_centroid)
            uwb_distances_to_measurement_centroid_3D.append(uwb_distance_to_measurement_centroid_3D)
        for filtered_measurement_point in filtered_measurement_points:
            filtered_distance_to_measurement_centroid_2D = distance_between_two_points2D(filtered_measurement_point, filtered_measurements_centroid)
            filtered_distances_to_measurement_centroid_2D.append(filtered_distance_to_measurement_centroid_2D)
            filtered_distance_to_measurement_centroid_3D = distance_between_two_points3D(filtered_measurement_point, filtered_measurements_centroid)
            filtered_distances_to_measurement_centroid_3D.append(filtered_distance_to_measurement_centroid_3D)

        # Calculate mean distance of measurements to measurement centroid
        uwb_mean_distance_to_measurement_centroid_2D = sum(uwb_distances_to_measurement_centroid_2D) / measurement_count
        uwb_mean_distances_to_measurement_centroid_2D.append(uwb_mean_distance_to_measurement_centroid_2D)
        uwb_mean_distance_to_measurement_centroid_3D = sum(uwb_distances_to_measurement_centroid_3D) / measurement_count
        uwb_mean_distances_to_measurement_centroid_3D.append(uwb_mean_distance_to_measurement_centroid_3D)
        filtered_mean_distance_to_measurement_centroid_2D = sum(filtered_distances_to_measurement_centroid_2D) / measurement_count
        filtered_mean_distances_to_measurement_centroid_2D.append(filtered_mean_distance_to_measurement_centroid_2D)
        filtered_mean_distance_to_measurement_centroid_3D = sum(filtered_distances_to_measurement_centroid_3D) / measurement_count
        filtered_mean_distances_to_measurement_centroid_3D.append(filtered_mean_distance_to_measurement_centroid_3D)

        # Calculate distance standard deviations of distance to measurement centroid
        uwb_std_2D_distances_to_measurement_centroid = standard_deviation(uwb_distances_to_measurement_centroid_2D, uwb_mean_distance_to_measurement_centroid_2D, measurement_count)
        uwb_mean_distances_to_measurement_centroid_stds_2D.append(uwb_std_2D_distances_to_measurement_centroid)
        uwb_std_3D_distances_to_measurement_centroid = standard_deviation(uwb_distances_to_measurement_centroid_3D, uwb_mean_distance_to_measurement_centroid_3D, measurement_count)
        uwb_mean_distances_to_measurement_centroid_stds_3D.append(uwb_std_3D_distances_to_measurement_centroid)
        filtered_std_2D_distances_to_measurement_centroid = standard_deviation(filtered_distances_to_measurement_centroid_2D, filtered_mean_distance_to_measurement_centroid_2D, measurement_count)
        filtered_mean_distances_to_measurement_centroid_stds_2D.append(filtered_std_2D_distances_to_measurement_centroid)
        filtered_std_3D_distances_to_measurement_centroid = standard_deviation(filtered_distances_to_measurement_centroid_3D, filtered_mean_distance_to_measurement_centroid_3D, measurement_count)
        filtered_mean_distances_to_measurement_centroid_stds_3D.append(filtered_std_3D_distances_to_measurement_centroid)

        ''' TODO: Take mean distance or distance from centroid to reference point? '''
        # Calculate distances from measurement centroid to reference point
        uwb_distance_measurement_centroid_to_ref_point_2D = distance_between_two_points2D(uwb_measurements_centroid, reference_position)
        uwb_distance_measurement_centroid_to_ref_point_3D = distance_between_two_points3D(uwb_measurements_centroid, reference_position)
        filtered_distance_measurement_centroid_to_ref_point_2D = distance_between_two_points2D(filtered_measurements_centroid, reference_position)
        filtered_distance_measurement_centroid_to_ref_point_3D = distance_between_two_points3D(filtered_measurements_centroid, reference_position)

        # Experimental
        '''###################################################################
        #################### MOTION SICKNESS EVALUATION ###################
        ###################################################################'''
        # 2D Get mean, min and max distance differences from measurements to their next ones
        uwb_delta_distances_2D = diff(uwb_distances_to_ref_point_2D)
        uwb_delta_distances_2D =  [abs(n) for n in uwb_delta_distances_2D]
        uwb_mean_delta_distance_2D = sum(uwb_delta_distances_2D) / (measurement_count - 1)
        uwb_mean_delta_distances_2D.append(uwb_mean_delta_distance_2D)
        filtered_delta_distances_2D = diff(filtered_distances_to_ref_point_2D)
        filtered_delta_distances_2D =  [abs(n) for n in filtered_delta_distances_2D]
        filtered_mean_delta_distance_2D = sum(filtered_delta_distances_2D) / (measurement_count - 1)
        filtered_mean_delta_distances_2D.append(filtered_mean_delta_distance_2D)

        # 3D Get mean, min and max distance differences from measurements to their next ones
        uwb_delta_distances_3D = diff(uwb_distances_to_ref_point_3D)
        uwb_delta_distances_3D = [abs(n) for n in uwb_delta_distances_3D]
        uwb_mean_delta_distance_3D = sum(uwb_delta_distances_3D) / (measurement_count - 1)
        uwb_mean_delta_distances_3D.append(uwb_mean_delta_distance_3D)
        filtered_delta_distances_3D = diff(filtered_distances_to_ref_point_3D)
        filtered_delta_distances_3D = [abs(n) for n in filtered_delta_distances_3D]
        filtered_mean_delta_distance_3D = sum(filtered_delta_distances_3D) / (measurement_count - 1)
        filtered_mean_delta_distances_3D.append(filtered_mean_delta_distance_3D)

    for k, v in uwb_positions_dictionary.items():
        uwb_x_mean = 0
        uwb_y_mean = 0
        uwb_z_mean = 0
        for uwb_position in v:
            uwb_x_mean += uwb_position[0]
            uwb_y_mean += uwb_position[1]
            uwb_z_mean += uwb_position[2]
        uwb_x_mean /= len(v)
        uwb_y_mean /= len(v)
        uwb_z_mean /= len(v)
        uwb_mean_positions.append([uwb_x_mean, uwb_y_mean, uwb_z_mean])
    for k, v in filtered_positions_dictionary.items():
        filtered_x_mean = 0
        filtered_y_mean = 0
        filtered_z_mean = 0
        for filtered_position in v:
            filtered_x_mean += filtered_position[0]
            filtered_y_mean += filtered_position[1]
            filtered_z_mean += filtered_position[2]
        filtered_x_mean /= len(v)
        filtered_y_mean /= len(v)
        filtered_z_mean /= len(v)
        filtered_mean_positions.append([filtered_x_mean, filtered_y_mean, filtered_z_mean])
    
    # Final accuracy evaluation
    uwb_mean_distance_to_reference_point_2D = mean(uwb_mean_distances_to_reference_point_2D)
    filtered_mean_distance_to_reference_point_2D = mean(filtered_mean_distances_to_reference_point_2D)
    uwb_mean_distance_to_reference_point_3D = mean(uwb_mean_distances_to_reference_point_3D)
    filtered_mean_distance_to_reference_point_3D = mean(filtered_mean_distances_to_reference_point_3D)
    uwb_mean_std_distances_to_ref_point_2D = mean(uwb_mean_distances_to_reference_point_stds_2D)
    filtered_mean_std_distances_to_ref_point_2D = mean(filtered_mean_distances_to_reference_point_stds_2D)
    uwb_mean_std_distances_to_ref_point_3D = mean(uwb_mean_distances_to_reference_point_stds_3D)
    filtered_mean_std_distances_to_ref_point_3D = mean(filtered_mean_distances_to_reference_point_stds_3D)
    uwb_mean_distance_x_axis = mean(uwb_distances_x)
    uwb_distances_x_axis_std = standard_deviation(uwb_distances_x)
    uwb_mean_distance_y_axis = mean(uwb_distances_y)
    uwb_mean_distance_z_axis = mean(uwb_distances_z)
    filtered_mean_distance_x_axis = mean(filtered_distances_x)
    filtered_mean_distance_y_axis = mean(filtered_distances_y)
    filtered_mean_distance_z_axis = mean(filtered_distances_z)

    # Final precision evaluation
    uwb_mean_distance_to_measurment_mean_point_2D = mean(uwb_mean_distances_to_measurement_centroid_2D)
    filtered_mean_distance_to_measurment_mean_point_2D = mean(filtered_mean_distances_to_measurement_centroid_2D)
    uwb_mean_distance_to_measurment_mean_point_3D = mean(uwb_mean_distances_to_measurement_centroid_3D)
    filtered_mean_distance_to_measurment_mean_point_3D = mean(filtered_mean_distances_to_measurement_centroid_3D)
    uwb_mean_std_distances_to_measurement_mean_point_2D = mean(uwb_mean_distances_to_measurement_centroid_stds_2D)
    filtered_mean_std_distances_to_measurement_mean_point_2D = mean(filtered_mean_distances_to_measurement_centroid_stds_2D)
    uwb_mean_std_distances_to_measurement_mean_point_3D = mean(uwb_mean_distances_to_measurement_centroid_stds_3D)
    filtered_mean_std_distances_to_measurement_mean_point_3D = mean(filtered_mean_distances_to_measurement_centroid_stds_3D)

    # Final motion sickness evaluation
    uwb_mean_delta_distance_2D = mean(uwb_mean_delta_distances_2D)
    filtered_mean_delta_distance_2D = mean(filtered_mean_delta_distances_2D)
    uwb_mean_delta_distance_3D = mean(uwb_mean_delta_distances_3D)
    filtered_mean_delta_distance_3D = mean(filtered_mean_delta_distances_3D)

    print('\n')
    print("ACCURACY RESULTS")
    print("Average uwb distance to reference point 2D: {:.3f}m".format(uwb_mean_distance_to_reference_point_2D))
    print("Average filtered distance to reference point 2D: {:.3f}m".format(filtered_mean_distance_to_reference_point_2D))
    print("Average uwb distance to reference point 3D: {:.3f}m".format(uwb_mean_distance_to_reference_point_3D))
    print("Average filtered distance to reference point 3D: {:.3f}m".format(filtered_mean_distance_to_reference_point_3D))
    print("Average uwb distances to reference point standard deviation 2D: {:.3f}m".format(uwb_mean_std_distances_to_ref_point_2D))
    print("Average filtered distances to reference point standard deviation 2D: {:.3f}m".format(filtered_mean_std_distances_to_ref_point_2D))
    print("Average uwb distances to reference point standard deviation 3D: {:.3f}m".format(uwb_mean_std_distances_to_ref_point_3D))
    print("Average filtered distances to reference point standard deviation 3D: {:.3f}m".format(filtered_mean_std_distances_to_ref_point_3D))
    print('\n')
    print("PRECISION RESULTS")
    print("Average uwb distance to measurment centroid 2D: {:.3f}m".format(uwb_mean_distance_to_measurment_mean_point_2D))
    print("Average filtered distance to measurment centroid 2D: {:.3f}m".format(filtered_mean_distance_to_measurment_mean_point_2D))
    print("Average uwb distance to measurment centroid 3D: {:.3f}m".format(uwb_mean_distance_to_measurment_mean_point_3D))
    print("Average filtered distance to measurment centroid 3D: {:.3f}m".format(filtered_mean_distance_to_measurment_mean_point_3D))
    print("Average uwb distances to measurement centroid standard deviation 2D: {:.3f}m".format(uwb_mean_std_distances_to_measurement_mean_point_2D))
    print("Average filtered distances to measurement centroid standard deviation 2D: {:.3f}m".format(filtered_mean_std_distances_to_measurement_mean_point_2D))
    print("Average uwb distances to measurement centroid standard deviation 3D: {:.3f}m".format(uwb_mean_std_distances_to_measurement_mean_point_3D))
    print("Average filtered distances to measurement centroid standard deviation 3D: {:.3f}m".format(filtered_mean_std_distances_to_measurement_mean_point_3D))
    print('\n')
    print("MOTION SICKNESS RESULTS")
    print("Average uwb delta distance 2D: {:.3f}m".format(uwb_mean_delta_distance_2D))
    print("Average filtered delta distance 2D: {:.3f}m".format(filtered_mean_delta_distance_2D))
    print("Average uwb delta distance 3D: {:.3f}m".format(uwb_mean_delta_distance_3D))
    print("Average filtered delta distance 3D: {:.3f}m".format(filtered_mean_delta_distance_3D))

    plot(reference_positions, uwb_mean_positions, filtered_mean_positions)

def plot(reference_positions, uwb_positions, filtered_positions):
    fig = plt.figure(figsize=(7, 13))
    ax0 = plt.subplot(211)
    plt.title("Positions accuracy visualization")
    plot_2d_cartesian(reference_positions, uwb_positions, filtered_positions, ax0)
    #plot_3D(uwb_positions, filtered_positions, ax1)
    plt.show()
    #plot_line_chart(uwb_positions, filtered_positions, raw_accelerations, filtered_accelerations, measurement_count)

def plot_2d_cartesian(reference_positions, uwb_positions, filtered_positions, axs):
    plt.xlabel = "X Axis"
    plt.ylabel = "Y Axis"
    # Plot 2D reference positions
    for x, y, z in reference_positions:
        axs.scatter(x, y, label='Reference positions', c='g', marker='o')
    # Plot 2D raw UWB positions
    for x, y, z in uwb_positions:
        axs.scatter(x, y, label='UWB positions', c='b', marker='x')
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
