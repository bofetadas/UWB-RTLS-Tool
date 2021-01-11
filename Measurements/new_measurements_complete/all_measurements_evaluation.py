import fnmatch
import matplotlib.pyplot as plt
import os
import sys
from matplotlib import cm
from numpy import array, lexsort, mean, nan, ndarray, std, sqrt, square
from statistics import median


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

def distance_on_axis(measurement, reference):
    return measurement - reference

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

def standard_deviation(measurements):
    return std(measurements)

def root_mean_square(data):
    return sqrt(mean(square(data)))

def evaluate_and_plot_data(directory):
    files = fnmatch.filter(os.listdir(directory), '*.txt')

    reference_positions = []
    uwb_mean_positions = []
    filtered_mean_positions = []

    uwb_distances_on_x_axis_to_reference_x = []
    uwb_distances_on_y_axis_to_reference_y = []
    uwb_distances_on_z_axis_to_reference_z = []
    filtered_distances_on_x_axis_to_reference_x = []
    filtered_distances_on_y_axis_to_reference_y = []
    filtered_distances_on_z_axis_to_reference_z = []

    uwb_distances_on_x_axis_to_measurement_centroid_x = []
    uwb_distances_on_y_axis_to_measurement_centroid_y = []
    uwb_distances_on_z_axis_to_measurement_centroid_z = []
    filtered_distances_on_x_axis_to_measurement_centroid_x = []
    filtered_distances_on_y_axis_to_measurement_centroid_y = []
    filtered_distances_on_z_axis_to_measurement_centroid_z = []

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

    # Jitter
    uwb_mean_delta_distances_2D = []
    uwb_rms_delta_distances_2D = []
    filtered_mean_delta_distances_2D = []
    filtered_rms_delta_distances_2D = []
    uwb_mean_delta_distances_3D = []
    uwb_rms_delta_distances_3D = []
    filtered_mean_delta_distances_3D = []
    filtered_rms_delta_distances_3D = []

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

        uwb_x_values = []
        uwb_y_values = []
        uwb_z_values = []
        filtered_x_values = []
        filtered_y_values = []
        filtered_z_values = []

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
                
                # Add individual coordinates to coordniate's means
                uwb_x_mean += uwb_x
                uwb_y_mean += uwb_y
                uwb_z_mean += uwb_z
                filtered_x_mean += filtered_x
                filtered_y_mean += filtered_y
                filtered_z_mean += filtered_z

                uwb_x_values.append(uwb_x)
                uwb_y_values.append(uwb_y)
                uwb_z_values.append(uwb_z)
                filtered_x_values.append(filtered_x)
                filtered_y_values.append(filtered_y)
                filtered_z_values.append(filtered_z)

                # Make up coordinate and add to list
                uwb_measurement_point = [uwb_x, uwb_y, uwb_z]
                uwb_measurement_points.append(uwb_measurement_point)
                filtered_measurement_point = [filtered_x, filtered_y, filtered_z]
                filtered_measurement_points.append(filtered_measurement_point)

                # Calculate distance of measurement point to reference position in 2D and 3D
                uwb_distance_to_ref_point_2D = distance_between_two_points2D(uwb_measurement_point, reference_position)
                uwb_distances_to_ref_point_2D.append(uwb_distance_to_ref_point_2D)
                uwb_distance_to_ref_point_3D = distance_between_two_points3D(uwb_measurement_point, reference_position)
                uwb_distances_to_ref_point_3D.append(uwb_distance_to_ref_point_3D)
                filtered_distance_to_ref_point_2D = distance_between_two_points2D(filtered_measurement_point, reference_position)
                filtered_distances_to_ref_point_2D.append(filtered_distance_to_ref_point_2D)
                filtered_distance_to_ref_point_3D = distance_between_two_points3D(filtered_measurement_point, reference_position)
                filtered_distances_to_ref_point_3D.append(filtered_distance_to_ref_point_3D)

                # Calculate distances on each axis to reference position
                uwb_distance_x = uwb_x - reference_position[0]
                uwb_distances_on_x_axis_to_reference_x.append(uwb_distance_x)
                uwb_distance_y = uwb_y - reference_position[1]
                uwb_distances_on_y_axis_to_reference_y.append(uwb_distance_y)
                uwb_distance_z = uwb_z - reference_position[2]
                uwb_distances_on_z_axis_to_reference_z.append(uwb_distance_z)
                filtered_distance_x = filtered_x - reference_position[0]
                filtered_distances_on_x_axis_to_reference_x.append(filtered_distance_x)
                filtered_distance_y = filtered_y - reference_position[1]
                filtered_distances_on_y_axis_to_reference_y.append(filtered_distance_y)
                filtered_distance_z = filtered_z - reference_position[2]
                filtered_distances_on_z_axis_to_reference_z.append(filtered_distance_z)
        
        '''#############################################################
        #################### ACCURACY EVALUATION ####################
        #############################################################'''

        # Calculate mean distance of measurements to reference point
        uwb_mean_distance_to_ref_point_2D = mean(uwb_distances_to_ref_point_2D)
        uwb_mean_distances_to_reference_point_2D.append(uwb_mean_distance_to_ref_point_2D)
        uwb_mean_distance_to_ref_point_3D = mean(uwb_distances_to_ref_point_3D)
        uwb_mean_distances_to_reference_point_3D.append(uwb_mean_distance_to_ref_point_3D)
        filtered_mean_distance_to_ref_point_2D = mean(filtered_distances_to_ref_point_2D)
        filtered_mean_distances_to_reference_point_2D.append(filtered_mean_distance_to_ref_point_2D)
        filtered_mean_distance_to_ref_point_3D = mean(filtered_distances_to_ref_point_3D)
        filtered_mean_distances_to_reference_point_3D.append(filtered_mean_distance_to_ref_point_3D)

        # Get distance standard deviations of distance to reference point
        uwb_std_2D_distances_to_ref_point = standard_deviation(uwb_distances_to_ref_point_2D)
        uwb_mean_distances_to_reference_point_stds_2D.append(uwb_std_2D_distances_to_ref_point)
        uwb_std_3D_distances_to_ref_point = standard_deviation(uwb_distances_to_ref_point_3D)
        uwb_mean_distances_to_reference_point_stds_3D.append(uwb_std_3D_distances_to_ref_point)
        filtered_std_2D_distances_to_ref_point = standard_deviation(filtered_distances_to_ref_point_2D)
        filtered_mean_distances_to_reference_point_stds_2D.append(filtered_std_2D_distances_to_ref_point)
        filtered_std_3D_distances_to_ref_point = standard_deviation(filtered_distances_to_ref_point_3D)
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

        # Calculate distance of each measurement point to measurements' centroid
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
        
        # Calculate distance for each measured axis value to measurement centroid axis value
        for uwb_x in uwb_x_values:
            uwb_x_to_x_axis_mean_distance = distance_on_axis(uwb_x, uwb_x_mean)
            uwb_distances_on_x_axis_to_measurement_centroid_x.append(uwb_x_to_x_axis_mean_distance)
        for uwb_y in uwb_y_values:
            uwb_y_to_y_axis_mean_distance = distance_on_axis(uwb_y, uwb_y_mean)
            uwb_distances_on_y_axis_to_measurement_centroid_y.append(uwb_y_to_y_axis_mean_distance)
        for uwb_z in uwb_z_values:
            uwb_z_to_z_axis_mean_distance = distance_on_axis(uwb_z, uwb_z_mean)
            uwb_distances_on_z_axis_to_measurement_centroid_z.append(uwb_z_to_z_axis_mean_distance)

        for filtered_x in filtered_x_values:
            filtered_x_to_x_axis_mean_distance = distance_on_axis(filtered_x, filtered_x_mean)
            filtered_distances_on_x_axis_to_measurement_centroid_x.append(filtered_x_to_x_axis_mean_distance)
        for filtered_y in filtered_y_values:
            filtered_y_to_y_axis_mean_distance = distance_on_axis(filtered_y, filtered_y_mean)
            filtered_distances_on_y_axis_to_measurement_centroid_y.append(filtered_y_to_y_axis_mean_distance)
        for filtered_z in filtered_z_values:
            filtered_z_to_z_axis_mean_distance = distance_on_axis(filtered_z, filtered_z_mean)
            filtered_distances_on_z_axis_to_measurement_centroid_z.append(filtered_z_to_z_axis_mean_distance)

        # Calculate mean distance of measurements to measurement centroid
        uwb_mean_distance_to_measurement_centroid_2D = mean(uwb_distances_to_measurement_centroid_2D)
        uwb_mean_distances_to_measurement_centroid_2D.append(uwb_mean_distance_to_measurement_centroid_2D)
        uwb_mean_distance_to_measurement_centroid_3D = mean(uwb_distances_to_measurement_centroid_3D)
        uwb_mean_distances_to_measurement_centroid_3D.append(uwb_mean_distance_to_measurement_centroid_3D)
        filtered_mean_distance_to_measurement_centroid_2D = mean(filtered_distances_to_measurement_centroid_2D)
        filtered_mean_distances_to_measurement_centroid_2D.append(filtered_mean_distance_to_measurement_centroid_2D)
        filtered_mean_distance_to_measurement_centroid_3D = mean(filtered_distances_to_measurement_centroid_3D)
        filtered_mean_distances_to_measurement_centroid_3D.append(filtered_mean_distance_to_measurement_centroid_3D)

        # Calculate distance standard deviations of distance to measurement centroid
        uwb_std_2D_distances_to_measurement_centroid = standard_deviation(uwb_distances_to_measurement_centroid_2D)
        uwb_mean_distances_to_measurement_centroid_stds_2D.append(uwb_std_2D_distances_to_measurement_centroid)
        uwb_std_3D_distances_to_measurement_centroid = standard_deviation(uwb_distances_to_measurement_centroid_3D)
        uwb_mean_distances_to_measurement_centroid_stds_3D.append(uwb_std_3D_distances_to_measurement_centroid)
        filtered_std_2D_distances_to_measurement_centroid = standard_deviation(filtered_distances_to_measurement_centroid_2D)
        filtered_mean_distances_to_measurement_centroid_stds_2D.append(filtered_std_2D_distances_to_measurement_centroid)
        filtered_std_3D_distances_to_measurement_centroid = standard_deviation(filtered_distances_to_measurement_centroid_3D)
        filtered_mean_distances_to_measurement_centroid_stds_3D.append(filtered_std_3D_distances_to_measurement_centroid)

        ''' TODO: Take mean distance or distance from centroid to reference point? '''
        # Calculate distances from measurement centroid to reference point
        uwb_distance_measurement_centroid_to_ref_point_2D = distance_between_two_points2D(uwb_measurements_centroid, reference_position)
        uwb_distance_measurement_centroid_to_ref_point_3D = distance_between_two_points3D(uwb_measurements_centroid, reference_position)
        filtered_distance_measurement_centroid_to_ref_point_2D = distance_between_two_points2D(filtered_measurements_centroid, reference_position)
        filtered_distance_measurement_centroid_to_ref_point_3D = distance_between_two_points3D(filtered_measurements_centroid, reference_position)

        '''###################################################################
        ########################## JITTER EVALUATION #########################
        ###################################################################'''
        # 2D Get mean and rms distance differences from measurements to their next ones
        uwb_mean_mean_delta_distance_2D = mean(delta_distances(distance_between_two_points2D, uwb_measurement_points))
        uwb_mean_delta_distances_2D.append(uwb_mean_mean_delta_distance_2D)
        uwb_rms_delta_distance_2D = root_mean_square(delta_distances(distance_between_two_points2D, uwb_measurement_points))
        uwb_rms_delta_distances_2D.append(uwb_rms_delta_distance_2D)
        filtered_mean_mean_delta_distance_2D = mean(delta_distances(distance_between_two_points2D, filtered_measurement_points))
        filtered_mean_delta_distances_2D.append(filtered_mean_mean_delta_distance_2D)
        filtered_rms_delta_distance_2D = root_mean_square(delta_distances(distance_between_two_points2D, filtered_measurement_points))
        filtered_rms_delta_distances_2D.append(filtered_rms_delta_distance_2D)

        # 3D Get mean and rms distance differences from measurements to their next ones
        uwb_mean_mean_delta_distance_3D = mean(delta_distances(distance_between_two_points3D, uwb_measurement_points))
        uwb_mean_delta_distances_3D.append(uwb_mean_mean_delta_distance_3D)
        uwb_rms_delta_distance_3D = root_mean_square(delta_distances(distance_between_two_points3D, uwb_measurement_points))
        uwb_rms_delta_distances_3D.append(uwb_rms_delta_distance_3D)
        filtered_mean_mean_delta_distance_3D = mean(delta_distances(distance_between_two_points3D, filtered_measurement_points))
        filtered_mean_delta_distances_3D.append(filtered_mean_mean_delta_distance_3D)
        filtered_rms_delta_distance_3D = root_mean_square(delta_distances(distance_between_two_points3D, filtered_measurement_points))
        filtered_rms_delta_distances_3D.append(filtered_rms_delta_distance_3D)

    # Calculate the measurement centroid of all measurement centroids of each reference position
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
    # 2D and 3D position accuracy evaluation
    # UWB
    uwb_mean_distance_to_reference_point_2D = mean(uwb_mean_distances_to_reference_point_2D)
    uwb_median_distance_to_reference_point_2D = median(uwb_mean_distances_to_reference_point_2D)
    uwb_mean_std_of_distances_to_reference_point_2D = mean(uwb_mean_distances_to_reference_point_stds_2D)
    uwb_mean_distance_to_reference_point_3D = mean(uwb_mean_distances_to_reference_point_3D)
    uwb_median_distance_to_reference_point_3D = median(uwb_mean_distances_to_reference_point_3D)
    uwb_mean_std_of_distances_to_reference_point_3D = mean(uwb_mean_distances_to_reference_point_stds_3D)
    
    # Filtered
    filtered_mean_distance_to_reference_point_2D = mean(filtered_mean_distances_to_reference_point_2D)
    filtered_median_distance_to_reference_point_2D = median(filtered_mean_distances_to_reference_point_2D)
    filtered_mean_std_of_distances_to_reference_point_2D = mean(filtered_mean_distances_to_reference_point_stds_2D)
    filtered_mean_distance_to_reference_point_3D = mean(filtered_mean_distances_to_reference_point_3D)
    filtered_median_distance_to_reference_point_3D = median(filtered_mean_distances_to_reference_point_3D)
    filtered_mean_std_of_distances_to_reference_point_3D = mean(filtered_mean_distances_to_reference_point_stds_3D)

    # Axis accuracy evaluation
    # UWB
    uwb_mean_distance_on_x_axis_to_reference_x = mean(uwb_distances_on_x_axis_to_reference_x)
    uwb_median_distance_on_x_axis_to_reference_x = median(uwb_distances_on_x_axis_to_reference_x)
    uwb_std_of_distances_on_x_axis_to_reference_x = standard_deviation(uwb_distances_on_x_axis_to_reference_x)
    uwb_mean_distance_on_y_axis_to_reference_y = mean(uwb_distances_on_y_axis_to_reference_y)
    uwb_median_distance_on_y_axis_to_reference_y = median(uwb_distances_on_y_axis_to_reference_y)
    uwb_std_of_distances_on_y_axis_to_reference_y = standard_deviation(uwb_distances_on_y_axis_to_reference_y)
    uwb_mean_distance_on_z_axis_to_reference_z = mean(uwb_distances_on_z_axis_to_reference_z)
    uwb_median_distance_on_z_axis_to_reference_z = median(uwb_distances_on_z_axis_to_reference_z)
    uwb_std_of_distances_on_z_axis_to_reference_z = standard_deviation(uwb_distances_on_z_axis_to_reference_z)
    
    # Filtered
    filtered_mean_distance_on_x_axis_to_reference_x = mean(filtered_distances_on_x_axis_to_reference_x)
    filtered_median_distance_on_x_axis_to_reference_x = median(filtered_distances_on_x_axis_to_reference_x)
    filtered_std_of_distances_on_x_axis_to_reference_x = standard_deviation(filtered_distances_on_x_axis_to_reference_x)
    filtered_mean_distance_on_y_axis_to_reference_y = mean(filtered_distances_on_y_axis_to_reference_y)
    filtered_median_distance_on_y_axis_to_reference_y = median(filtered_distances_on_y_axis_to_reference_y)
    filtered_std_of_distances_on_y_axis_to_reference_y = standard_deviation(filtered_distances_on_y_axis_to_reference_y)
    filtered_mean_distance_on_z_axis_to_reference_z = mean(filtered_distances_on_z_axis_to_reference_z)
    filtered_median_distance_on_z_axis_to_reference_z = median(filtered_distances_on_z_axis_to_reference_z)
    filtered_std_of_distances_on_z_axis_to_reference_z = standard_deviation(filtered_distances_on_z_axis_to_reference_z)

    # Final precision evaluation
    # 2D and 3D position precision evaluation
    # UWB
    uwb_mean_distance_to_measurement_centroid_2D = mean(uwb_mean_distances_to_measurement_centroid_2D)
    uwb_median_distance_to_measurement_centroid_2D = median(uwb_mean_distances_to_measurement_centroid_2D)
    uwb_mean_std_of_distances_to_measurement_centroid_2D = mean(uwb_mean_distances_to_measurement_centroid_stds_2D)
    uwb_mean_distance_to_measurment_centroid_3D = mean(uwb_mean_distances_to_measurement_centroid_3D)
    uwb_median_distance_to_measurement_centroid_3D = median(uwb_mean_distances_to_measurement_centroid_3D)
    uwb_mean_std_of_distances_to_measurement_centroid_3D = mean(uwb_mean_distances_to_measurement_centroid_stds_3D)
    
    # Filtered
    filtered_mean_distance_to_measurment_centroid_2D = mean(filtered_mean_distances_to_measurement_centroid_2D)
    filtered_median_distance_to_measurement_centroid_2D = median(filtered_mean_distances_to_measurement_centroid_2D)
    filtered_mean_std_of_distances_to_measurement_centroid_2D = mean(filtered_mean_distances_to_measurement_centroid_stds_2D)
    filtered_mean_distance_to_measurment_centroid_3D = mean(filtered_mean_distances_to_measurement_centroid_3D)
    filtered_median_distance_to_measurement_centroid_3D = median(filtered_mean_distances_to_measurement_centroid_3D)
    filtered_mean_std_of_distances_to_measurement_centroid_3D = mean(filtered_mean_distances_to_measurement_centroid_stds_3D)

    # Axis precision evaluation
    # UWB
    uwb_mean_distance_on_x_axis_to_measurement_centroid_x = mean(uwb_distances_on_x_axis_to_measurement_centroid_x)
    uwb_median_distance_on_x_axis_to_measurement_centroid_x = median(uwb_distances_on_x_axis_to_measurement_centroid_x)
    uwb_std_of_distances_on_x_axis_to_measurement_centroid_x = standard_deviation(uwb_distances_on_x_axis_to_measurement_centroid_x)
    uwb_mean_distance_on_y_axis_to_measurement_centroid_y = mean(uwb_distances_on_y_axis_to_measurement_centroid_y)
    uwb_median_distance_on_y_axis_to_measurement_centroid_y = median(uwb_distances_on_y_axis_to_measurement_centroid_y)
    uwb_std_of_distances_on_y_axis_to_measurement_centroid_y = standard_deviation(uwb_distances_on_y_axis_to_measurement_centroid_y)
    uwb_mean_distance_on_z_axis_to_measurement_centroid_z = mean(uwb_distances_on_z_axis_to_measurement_centroid_z)
    uwb_median_distance_on_z_axis_to_measurement_centroid_z = median(uwb_distances_on_z_axis_to_measurement_centroid_z)
    uwb_std_of_distances_on_z_axis_to_measurement_centroid_z = standard_deviation(uwb_distances_on_z_axis_to_measurement_centroid_z)
    
    # Filtered
    filtered_mean_distance_on_x_axis_to_measurement_centroid_x = mean(filtered_distances_on_x_axis_to_measurement_centroid_x)
    filtered_median_distance_on_x_axis_to_measurement_centroid_x = median(filtered_distances_on_x_axis_to_measurement_centroid_x)
    filtered_std_of_distances_on_x_axis_to_measurement_centroid_x = standard_deviation(filtered_distances_on_x_axis_to_measurement_centroid_x)
    filtered_mean_distance_on_y_axis_to_measurement_centroid_y = mean(filtered_distances_on_y_axis_to_measurement_centroid_y)
    filtered_median_distance_on_y_axis_to_measurement_centroid_y = median(filtered_distances_on_y_axis_to_measurement_centroid_y)
    filtered_std_of_distances_on_y_axis_to_measurement_centroid_y = standard_deviation(filtered_distances_on_y_axis_to_measurement_centroid_y)
    filtered_mean_distance_on_z_axis_to_measurement_centroid_z = mean(filtered_distances_on_z_axis_to_measurement_centroid_z)
    filtered_median_distance_on_z_axis_to_measurement_centroid_z = median(filtered_distances_on_z_axis_to_measurement_centroid_z)
    filtered_std_of_distances_on_z_axis_to_measurement_centroid_z = standard_deviation(filtered_distances_on_z_axis_to_measurement_centroid_z)

    # Final jitter evaluation
    # UWB
    uwb_mean_mean_delta_distance_2D = mean(uwb_mean_delta_distances_2D)
    uwb_mean_rms_delta_distance_2D = mean(uwb_rms_delta_distances_2D)
    uwb_median_delta_distance_2D = median(uwb_mean_delta_distances_2D)
    uwb_std_of_delta_distances_2D = standard_deviation(uwb_mean_delta_distances_2D)
    uwb_mean_mean_delta_distance_3D = mean(uwb_mean_delta_distances_3D)
    uwb_mean_rms_delta_distance_3D = mean(uwb_rms_delta_distances_3D)
    uwb_median_delta_distance_3D = median(uwb_mean_delta_distances_3D)
    uwb_std_of_delta_distances_3D = standard_deviation(uwb_mean_delta_distances_3D)
    
    # Filtered
    filtered_mean_mean_delta_distance_2D = mean(filtered_mean_delta_distances_2D)
    filtered_mean_rms_delta_distance_2D = mean(filtered_rms_delta_distances_2D)
    filtered_median_delta_distance_2D = median(filtered_mean_delta_distances_2D)
    filtered_std_of_delta_distances_2D = standard_deviation(filtered_mean_delta_distances_2D)
    filtered_mean_mean_delta_distance_3D = mean(filtered_mean_delta_distances_3D)
    filtered_mean_rms_delta_distance_3D = mean(filtered_rms_delta_distances_3D)
    filtered_median_delta_distance_3D = median(filtered_mean_delta_distances_3D)
    filtered_std_of_delta_distances_3D = standard_deviation(filtered_mean_delta_distances_3D)

    print('')
    print("ACCURACY RESULTS")
    print("Mean | Median | Std raw distances to reference position 2D: {:.3f} | {:.3f} | {:.3f}m".format(uwb_mean_distance_to_reference_point_2D, uwb_median_distance_to_reference_point_2D, uwb_mean_std_of_distances_to_reference_point_2D))
    print("Mean | Median | Std raw distances to reference position 3D: {:.3f} | {:.3f} | {:.3f}m".format(uwb_mean_distance_to_reference_point_3D, uwb_median_distance_to_reference_point_3D, uwb_mean_std_of_distances_to_reference_point_3D))
    print("Mean | Median | Std raw distances on X axis to reference X: {:.3f} | {:.3f} | {:.3f}m".format(uwb_mean_distance_on_x_axis_to_reference_x, uwb_median_distance_on_x_axis_to_reference_x, uwb_std_of_distances_on_x_axis_to_reference_x))
    print("Mean | Median | Std raw distances on Y axis to reference Y: {:.3f} | {:.3f} | {:.3f}m".format(uwb_mean_distance_on_y_axis_to_reference_y, uwb_median_distance_on_y_axis_to_reference_y, uwb_std_of_distances_on_y_axis_to_reference_y))
    print("Mean | Median | Std raw distances on Z axis to reference Z: {:.3f} | {:.3f} | {:.3f}m".format(uwb_mean_distance_on_z_axis_to_reference_z, uwb_median_distance_on_z_axis_to_reference_z, uwb_std_of_distances_on_z_axis_to_reference_z))
    print("Mean | Median | Std filtered distances to reference position 2D: {:.3f} | {:.3f} | {:.3f}m".format(filtered_mean_distance_to_reference_point_2D, filtered_median_distance_to_reference_point_2D, filtered_mean_std_of_distances_to_reference_point_2D))
    print("Mean | Median | Std filtered distances to reference position 3D: {:.3f} | {:.3f} | {:.3f}m".format(filtered_mean_distance_to_reference_point_3D, filtered_median_distance_to_reference_point_3D, filtered_mean_std_of_distances_to_reference_point_3D))
    print("Mean | Median | Std filtered distances on X axis to reference position X: {:.3f} | {:.3f} | {:.3f}m".format(filtered_mean_distance_on_x_axis_to_reference_x, filtered_median_distance_on_x_axis_to_reference_x, filtered_std_of_distances_on_x_axis_to_reference_x))
    print("Mean | Median | Std filtered distances on Y axis to reference position Y: {:.3f} | {:.3f} | {:.3f}m".format(filtered_mean_distance_on_y_axis_to_reference_y, filtered_median_distance_on_y_axis_to_reference_y, filtered_std_of_distances_on_y_axis_to_reference_y))
    print("Mean | Median | Std filtered distances on Z axis to reference position Z: {:.3f} | {:.3f} | {:.3f}m".format(filtered_mean_distance_on_z_axis_to_reference_z, filtered_median_distance_on_z_axis_to_reference_z, filtered_std_of_distances_on_z_axis_to_reference_z))

    print('\n')
    print("PRECISION RESULTS")
    print("Mean | Median | Std raw distances to measurement centroid 2D: {:.3f} | {:.3f} | {:.3f}m".format(uwb_mean_distance_to_measurement_centroid_2D, uwb_median_distance_to_measurement_centroid_2D, uwb_mean_std_of_distances_to_measurement_centroid_2D))
    print("Mean | Median | Std raw distances to measurement centroid 3D: {:.3f} | {:.3f} | {:.3f}m".format(uwb_mean_distance_to_measurement_centroid_3D, uwb_median_distance_to_measurement_centroid_3D, uwb_mean_std_of_distances_to_measurement_centroid_3D))
    print("Mean | Median | Std raw distances on X axis to measurement centroid X: {:.3f} | {:.3f} | {:.3f}m".format(uwb_mean_distance_on_x_axis_to_measurement_centroid_x, uwb_median_distance_on_x_axis_to_measurement_centroid_x, uwb_std_of_distances_on_x_axis_to_measurement_centroid_x))
    print("Mean | Median | Std raw distances on Y axis to measurement centroid Y: {:.3f} | {:.3f} | {:.3f}m".format(uwb_mean_distance_on_y_axis_to_measurement_centroid_y, uwb_median_distance_on_y_axis_to_measurement_centroid_y, uwb_std_of_distances_on_y_axis_to_measurement_centroid_y))
    print("Mean | Median | Std raw distances on Z axis to measurement centroid Z: {:.3f} | {:.3f} | {:.3f}m".format(uwb_mean_distance_on_z_axis_to_measurement_centroid_z, uwb_median_distance_on_z_axis_to_measurement_centroid_z, uwb_std_of_distances_on_z_axis_to_measurement_centroid_z))
    print("Mean | Median | Std filtered distances to measurement centroid 2D: {:.3f} | {:.3f} | {:.3f}m".format(filtered_mean_distance_to_measurement_centroid_2D, filtered_median_distance_to_measurement_centroid_2D, filtered_mean_std_of_distances_to_measurement_centroid_2D))
    print("Mean | Median | Std filtered distances to measurement centroid 3D: {:.3f} | {:.3f} | {:.3f}m".format(filtered_mean_distance_to_measurement_centroid_3D, filtered_median_distance_to_measurement_centroid_3D, filtered_mean_std_of_distances_to_measurement_centroid_3D))
    print("Mean | Median | Std filtered distances on X axis to measurement centroid X: {:.3f} | {:.3f} | {:.3f}m".format(filtered_mean_distance_on_x_axis_to_measurement_centroid_x, filtered_median_distance_on_x_axis_to_measurement_centroid_x, filtered_std_of_distances_on_x_axis_to_measurement_centroid_x))
    print("Mean | Median | Std filtered distances on Y axis to measurement centroid Y: {:.3f} | {:.3f} | {:.3f}m".format(filtered_mean_distance_on_y_axis_to_measurement_centroid_y, filtered_median_distance_on_y_axis_to_measurement_centroid_y, filtered_std_of_distances_on_y_axis_to_measurement_centroid_y))
    print("Mean | Median | Std filtered distances on Z axis to measurement centroid Z: {:.3f} | {:.3f} | {:.3f}m".format(filtered_mean_distance_on_z_axis_to_measurement_centroid_z, filtered_median_distance_on_z_axis_to_measurement_centroid_z, filtered_std_of_distances_on_z_axis_to_measurement_centroid_z))
    
    print('\n')
    print("JITTER RESULTS")
    print("Mean | RMS | Median | Std raw delta distances 2D: {:.3f} | {:.3f} | {:.3f} | {:.3f}m".format(uwb_mean_mean_delta_distance_2D, uwb_mean_rms_delta_distance_2D, uwb_median_delta_distance_2D, uwb_std_of_delta_distances_2D))
    print("Mean | RMS | Median | Std raw delta distances 3D: {:.3f} | {:.3f} | {:.3f} | {:.3f}m".format(uwb_mean_mean_delta_distance_3D, uwb_mean_rms_delta_distance_3D, uwb_median_delta_distance_3D, uwb_std_of_delta_distances_3D))
    print("Mean | RMS | Median | Std filtered delta distances 2D: {:.3f} | {:.3f} | {:.3f} | {:.3f}m".format(filtered_mean_mean_delta_distance_2D, filtered_mean_rms_delta_distance_2D, filtered_median_delta_distance_2D, filtered_std_of_delta_distances_2D))
    print("Mean | RMS | Median | Std filtered delta distances 3D: {:.3f} | {:.3f} | {:.3f} | {:.3f}m".format(filtered_mean_mean_delta_distance_3D, filtered_mean_rms_delta_distance_3D, filtered_median_delta_distance_3D, filtered_std_of_delta_distances_3D))
    print('')
    print("All values in meter units")
    print('')
    
    plot_coordinates(reference_positions, uwb_mean_positions, filtered_mean_positions)
    plot_heat_maps(uwb_positions_dictionary, filtered_positions_dictionary)

def plot_coordinates(reference_positions, uwb_positions, filtered_positions):
    fig = plt.figure("2D and 3D visualization")
    ax0 = plt.subplot(111)
    #ax1 = plt.subplot(122, projection='3d')
    plt.title("Positions accuracy visualization")
    plot_2d_cartesian(reference_positions, uwb_positions, filtered_positions, ax0)
    #plot_3d(reference_positions, uwb_positions, filtered_positions, ax1)
    plt.show()

def plot_2d_cartesian(reference_positions, uwb_positions, filtered_positions, axs):
    axs.set_title("2D Plot of reference positions, raw positions and filtered positions")
    axs.set_xlabel('X')
    axs.set_ylabel('Y')
    # Plot 2D reference positions
    for x, y, z in reference_positions:
        axs.scatter(x, y, label='Reference positions', c='g', marker='o')
    # Plot 2D raw UWB positions
    for x, y, z in uwb_positions:
        axs.scatter(x, y, label='Raw positions', c='b', marker='^')
    # Plot 2D filtered positions
    for x, y, z in filtered_positions:
        axs.scatter(x, y, label='Filtered positions', c='r', marker='x')
    axs.grid(True)
    legend(axs)

def plot_3d(reference_positions, uwb_positions, filtered_positions, axs):
    axs.set_title("3D Plot of reference positions, raw positions and filtered positions")
    axs.set_xlabel('X')
    axs.set_ylabel('Y')
    axs.set_zlabel('Z')
    # Plot 3D reference_positions
    for x, y, z in reference_positions:
        axs.scatter(x, y, z, label='Reference positions', c='g', marker='o')
    # Plot 3D raw uwb positions
    for x, y, z in uwb_positions:
        axs.scatter(x, y, z, label="Raw positions", c='b', marker='^')
    # Plot 3D filtered positions
    for x, y, z in filtered_positions:
        axs.scatter(x, y, z, label="Filtered positions", c='r', marker='x')
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

def plot_heat_maps(uwb_positions_dictionary, filtered_positions_dictionary):
    uwb_data = []
    filtered_data = []
    for key, value in uwb_positions_dictionary.items():
        x = float(key.split(',')[0])
        y = float(key.split(',')[1])
        heights = []
        for position in value:
            heights.append(position[2])
        z = mean(heights)
        uwb_data.append([x, y, z])
    for key, value in filtered_positions_dictionary.items():
        x = float(key.split(',')[0])
        y = float(key.split(',')[1])
        heights = []
        for position in value:
            heights.append(position[2])
        z = mean(heights)
        filtered_data.append([x, y, z])

    # Add missing data
    # Remove if you do not have any "holes" in your measurements
    uwb_data.append([2.0, 3.0, nan])
    uwb_data.append([3.0, 3.0, nan])
    filtered_data.append([2.0, 3.0, nan])
    filtered_data.append([3.0, 3.0, nan])

    uwb_data = array(uwb_data)
    filtered_data = array(filtered_data)
    uwb_data = uwb_data[lexsort((uwb_data[:, 0], uwb_data[:, 1]))]
    filtered_data = filtered_data[lexsort((filtered_data[:, 0], filtered_data[:, 1]))]
    
    uwb_z_values = []
    filtered_z_values = []

    for i in range(len(uwb_data)):
        uwb_z_values.append(uwb_data[i][2])
    for i in range(len(filtered_data)):
        filtered_z_values.append(filtered_data[i][2])

    uwb_z_values = array(uwb_z_values)
    filtered_z_values = array(filtered_z_values)
    uwb_z_data = ndarray(buffer=uwb_z_values, shape=(4, 3))
    filtered_z_data = ndarray(buffer=filtered_z_values, shape=(4, 3))

    # Plot uwb and filtered z heat map
    fig = plt.figure("Height Map")
    ax0 = plt.subplot(111)
    ax0.set_title("Raw height map")
    ax0.set_xlabel('X')
    ax0.set_ylabel('Y')
    #ax1 = plt.subplot(122)
    #ax1.set_title("Filtered height map")
    #ax1.set_xlabel('X')
    #ax1.set_ylabel('Y')
    a0 = ax0.imshow(uwb_z_data, interpolation='None', origin='lower', cmap='jet', extent=(uwb_data.min(axis=0)[0]-.5 , uwb_data.max(axis=0)[0]+.5, uwb_data.min(axis=0)[1]-.5, uwb_data.max(axis=0)[1]+.5))
    #a1 = ax1.imshow(filtered_z_data, interpolation='None', origin='lower', cmap='jet', extent=(filtered_data.min(axis=0)[0]-.5 , filtered_data.max(axis=0)[0]+.5, filtered_data.min(axis=0)[1]-.5, filtered_data.max(axis=0)[1]+.5))
    a0_colorbar = fig.colorbar(a0, ax=ax0)
    a0_colorbar.ax.set_title("Z", size=18)
    #a1_colorbar = fig.colorbar(a1, ax=ax1)
    #a1_colorbar.ax.set_title("Z", size=18)
    current_cmap = cm.get_cmap()
    current_cmap.set_bad(color='black')
    plt.show()

if __name__ == "__main__":
    try:
        directory = sys.argv[1]
    except IndexError:
        print_no_document_found_error()
        exit(1)

    evaluate_and_plot_data(directory)
