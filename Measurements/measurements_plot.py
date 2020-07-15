from mpl_toolkits.mplot3d import Axes3D
import sys
import matplotlib.pyplot as plt
import numpy as np

quiver_directions = {'N': [0, 1], 'E': [1, 0], 'S': [0, -1], 'W': [-1, 0]}

def print_usage():
    print("Usage: python3 measurements_plot.py <your_doc.txt> <[2d | 3d]> <[both | cartesian | polar]>")

def print_no_document_found_error():
    print("ERROR: No .txt document found")
    print("Please add a .txt document as first argument when calling this script")
    print("Note: That .txt document has had to be created by the related \'LocationApp\' for Android")
    print_usage()
    print("Exiting")
    print("\n")

def print_invalid_argument_error(argument):
    print("Invalid parameter: {}".format(argument))
    print_usage()
    print("Exiting")
    print("\n")

def print_2d_hint():
    print("No plotting mode parameter specified")
    print("Plotting values in 3D space per default")
    print("If instead plotting on a 2D plane is desired, type: python3 measurements_plot.py <your_doc.txt> 2d <both | cartesian | polar>")
    print("\n")

def print_coord_systems_hint():
    print("No coordinate system was provided by the user")
    print("Plotting in 2D is possible in two coordinate systems: cartesian and polar")
    print("Plotting in both coordinate systems per default")
    print("If instead plotting only in a cartesian or polar coordinate system is desired, type: python3 measurements_plot.py <your_doc.txt> 2d <cartesian | polar>")
    print("\n")

def get_user_args():
    try:
        filename = sys.argv[1]
    except IndexError:
        print_no_document_found_error()
        sys.exit(1)
    
    try:
        mode = str(sys.argv[2])
        if (mode != "2d" and mode != "3d"):
            print_invalid_argument_error(mode)
            sys.exit(1)
    except IndexError:
        print_2d_hint()
        mode = '3d'

    try:
        coord_system = str(sys.argv[3])
        if mode == '3d':
            print("Useless coordinate system parameter \'{}\' because of 3D plotting".format(coord_system))
            coord_system = None
        else :
            if (coord_system != "both" and coord_system != "cartesian" and coord_system != "polar"):
                print_invalid_argument_error(coord_system)
                sys.exit(1)
    except IndexError:
        if mode == '3d':
            coord_system = None
        else:
            print_coord_systems_hint()
            coord_system = 'both'
    
    return filename, mode, coord_system

def get_sample_points(filename):
    points = []
    x_points = []
    y_points = []
    z_points = []

    with open(filename) as file:
        for line in file:
            x = float(line.split(',')[0])
            y = float(line.split(',')[1])
            z = float(line.split(',')[2])

            # Add sample point to list
            x_points.append(x)
            y_points.append(y)
            z_points.append(z)
            sample_point = [x, y, z]
            points.append(sample_point)
    
    return x_points, y_points, z_points, points

# Values are plotted in a centered system
# That is, the reference point is set to (0,0,0) and the samples are scattered around it accordingly
def plot(direction, reference_point, points, mode, coord_system=None):
    # Plot 2D
    if mode == '2d':
        if coord_system == 'both':
            fig = plt.figure(figsize=(7, 13))
            ax0 = plt.subplot(211)
            ax1 = plt.subplot(212, projection='polar')
            plt.title("2D measurements around {}, {} in {} direction".format(reference_point[0], reference_point[1], direction))
            plot2DCartesian(points, reference_point, direction, ax0)
            plot2DPolar(points, reference_point, direction, ax1)
        elif coord_system == 'cartesian':   
            fig, axs = plt.subplots(1)
            plt.title("2D measurements around {}, {} in {} direction".format(reference_point[0], reference_point[1], direction))
            plot2DCartesian(points, reference_point, direction, axs)
        elif coord_system == 'polar':
            fig = plt.figure()
            axs = fig.add_subplot(111, projection='polar')
            plt.title("2D measurements around {}, {} in {} direction".format(reference_point[0], reference_point[1], direction))
            plot2DPolar(points, reference_point, direction, axs)
    # Plot 3D
    elif mode == '3d':
        plot3D(points, reference_point, direction)
    else:
        raise NameError

    plt.show()

def plot2DCartesian(points, reference_point, direction, axs):
    plt.xlabel = "X AXIS"
    plt.ylabel = "Y AXIS"
    # Plot samples
    for x, y, z in points:
        axs.scatter(x, y, c='b', marker='^')
    # Plot reference point
    axs.scatter(reference_point[0], reference_point[1], c='r', marker='o')

def plot2DPolar(points, reference_point, d, axs):
    d = quiver_directions[direction]
    axs.set_thetalim(0, np.pi * 2)
    axs.set_xticks(np.linspace(0, np.pi*2, 4, endpoint=False))
    # Plot samples
    for x, y, z in points:
        r, theta = cart2pol(x - reference_point[0], y - reference_point[1])
        axs.scatter(theta, r, marker='^', c='b')
    # Plot reference point
    r, theta = cart2pol(0, 0)
    axs.scatter(theta, r, marker='o', c='r')
    axs.quiver(0, 0, d[0], d[1], color='orange', scale=10, width=0.01)

def plot3D(points, reference_point, direction):
    fig = plt.figure()
    ax = fig.add_subplot(111, projection='3d')
    ax.set_xlabel('X Label')
    ax.set_ylabel('Y Label')
    ax.set_zlabel('Z Label')
    plt.title("3D measurements around {}, {}, {} in {} direction".format(reference_point[0], reference_point[1], reference_point[2], direction))

    # Plot sample points
    for x, y, z in points:
        ax.scatter(x, y, z, c='b', marker='^')
    # Plot reference point
    ax.scatter(reference_point[0], reference_point[1], reference_point[2], c='r', marker='o')

def cart2pol(x, y):
    rho = np.sqrt(x**2 + y**2)
    phi = np.arctan2(y, x)
    return(rho, phi)

if __name__ == "__main__":
    filename, mode, coord_system = get_user_args()

    direction = filename.split('(')[0]
    x_reference = float((filename.split('(')[1].split(')')[0].split('_')[0]).replace(',', '.'))
    y_reference = float((filename.split('(')[1].split(')')[0].split('_')[1]).replace(',', '.'))
    z_reference = float((filename.split('(')[1].split(')')[0].split('_')[2]).replace(',', '.'))
    
    reference_point = [x_reference, y_reference, z_reference]
    x_points, y_points, z_points, sample_points = get_sample_points(filename)
    plot(direction, reference_point, sample_points, mode, coord_system)
