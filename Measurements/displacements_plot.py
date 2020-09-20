import sys
import sys


def plot_displacements(file):
    uwb_x_values = []
    uwb_y_values = []
    uwb_z_values = []
    imu_x_values = []
    imu_y_values = []
    imu_z_values = []
    timestamps = []
    with open(file) as f:
        for line in f:
            uwb_x_values.append(float(line.split(',')[0]))
            uwb_y_values.append(float(line.split(',')[1]))
            uwb_z_values.append(float(line.split(',')[2]))
            imu_x_values.append(float(line.split(',')[3]))
            imu_y_values.append(float(line.split(',')[4]))
            imu_z_values.append(float(line.split(',')[5]))
            timestamps.append(float(line.split(',')[6]))

    fig = plt.figure()
    
    ax1 = fig.add_subplot(311)
    ax1.plot(timestamps, uwb_x_values, label='uwb_x')
    ax1.plot(timestamps, imu_x_values, label='imu_x')
    ax1.legend()

    ax2 = fig.add_subplot(312)
    ax2.plot(timestamps, uwb_y_values, label='uwb_y')
    ax2.plot(timestamps, imu_y_values, label='imu_y')
    ax2.legend()

    ax3 = fig.add_subplot(313)
    ax3.plot(timestamps, uwb_z_values, label='uwb_z')
    ax3.plot(timestamps, imu_z_values, label='imu_z')
    ax3.legend()

    #plt.legend()
    plt.show()

if __name__ == "__main__":
    try:
        file = sys.argv[1]
    except IndexError:
        print("Please add file to script call")
        sys.exit(1)

    plot_displacements(file)

    