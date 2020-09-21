import matplotlib.pyplot as plt
import sys

def print_no_document_found_error():
    print("ERROR: No .txt document found")
    print("Please add a .txt document as first argument when calling this script")
    print("Note: That .txt document has had to be created by the related \'LocationApp\' for Android")
    print("Exiting")
    print("\n")

# Returns the amount of positions recorded
def get_position_count(filename):
    with open(filename) as f:
        for i, l in enumerate(f):
            pass
    return i + 1

def get_positions(filename):
    uwb_positions = []
    filtered_positions = []

    with open(filename) as f:
        for line in f:
            uwb_position = line.split('|')[0]
            filtered_position = line.split('|')[1]
            uwb_x = float(uwb_position.split(',')[0])
            uwb_y = float(uwb_position.split(',')[1])
            uwb_z = float(uwb_position.split(',')[2])
            filtered_x = float(filtered_position.split(',')[0])
            filtered_y = float(filtered_position.split(',')[1])
            filtered_z = float(filtered_position.split(',')[2])

            # Add positions to lists
            uwb_positions.append([uwb_x, uwb_y, uwb_z])
            filtered_positions.append([filtered_x, filtered_y, filtered_z])
    
    return uwb_positions, filtered_positions

def get_coordinates(uwb_positions, filtered_positions):
    uwb_x_coordinates = []
    uwb_y_coordinates = []
    uwb_z_coordinates = []
    filtered_x_coordinates = []
    filtered_y_coordinates = []
    filtered_z_coordinates = []

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

    return uwb_x_coordinates, uwb_y_coordinates, uwb_z_coordinates, filtered_x_coordinates, filtered_y_coordinates, filtered_z_coordinates

def plot(uwb_positions, filtered_positions, positions_count):
    fig = plt.figure(figsize=(7, 13))
    ax0 = plt.subplot(211)
    ax1 = plt.subplot(212, projection='3d')
    plt.title("Raw UWB and filtered positions")
    plot_2D_cartesian(uwb_positions, filtered_positions, ax0)
    plot_3D(uwb_positions, filtered_positions, ax1)
    plt.show()
    plot_coordinates(uwb_positions, filtered_positions, positions_count)

def plot_2D_cartesian(uwb_positions, filtered_positions, axs):
    plt.xlabel = "X Axis"
    plt.ylabel = "Y Axis"
    # Plot 2D raw UWB positions
    for x, y, z in uwb_positions:
        axs.scatter(x, y, c='b', marker='^')
    # Plot 2D filtered positions
    for x, y, z in filtered_positions:
        axs.scatter(x, y, c='r', marker='x')

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

def plot_coordinates(uwb_positions, filtered_positions, positions_count):
    fig = plt.figure()
    uwb_x_coordinates, uwb_y_coordinates, uwb_z_coordinates, filtered_x_coordinates, filtered_y_coordinates, filtered_z_coordinates = get_coordinates(uwb_positions, filtered_positions)
    
    ax1 = fig.add_subplot(311)
    ax1.plot(range(positions_count), uwb_x_coordinates, label='UWB X', c='b')
    ax1.plot(range(positions_count), filtered_x_coordinates, label='Filtered X', c='r')
    ax1.legend()

    ax2 = fig.add_subplot(312)
    ax2.plot(range(positions_count), uwb_y_coordinates, label='UWB Y', c='b')
    ax2.plot(range(positions_count), filtered_y_coordinates, label='Filtered Y', c='r')
    ax2.legend()

    ax3 = fig.add_subplot(313)
    ax3.plot(range(positions_count), uwb_z_coordinates, label='UWB Z', c='b')
    ax3.plot(range(positions_count), filtered_z_coordinates, label='Filtered Z', c='r')
    ax3.axhline(1.76, 0, 1, label='User Height', c='g')
    ax3.legend()

    plt.show()

if __name__ == "__main__":
    try:
        filename = sys.argv[1]
    except IndexError:
        print_no_document_found_error()
        exit(1)
    
    positions_count = get_position_count(filename)
    uwb_positions, filtered_positions = get_positions(filename)
    plot(uwb_positions, filtered_positions, positions_count)
