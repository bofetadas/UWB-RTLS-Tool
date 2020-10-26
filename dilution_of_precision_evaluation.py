import numpy as np
import matplotlib.pyplot as plt
from matplotlib import cm
import sys

DEFAULT_Z = 1.73

def distance_between_two_points3D(reference_coordinate, anchor_coordinate):
    return np.sqrt(pow(reference_coordinate[0] - anchor_coordinate[0], 2) + pow(reference_coordinate[1] - anchor_coordinate[1], 2) + pow(reference_coordinate[2] - anchor_coordinate[2], 2))

def calculate_and_plot_dop(z):
    # Define anchor coordiantes here
    anchor_0_pos = [0.02, 0.20, 2.50]
    anchor_1_pos = [0.02, 3.41, 1.30]
    anchor_2_pos = [3.74, 3.48, 2.53]
    anchor_3_pos = [3.74, 0.05, 0.70]

    # Define reference position coordiantes here (reference positions are the positions for which you would like to calculate the DOP)
    ref_0_pos = [1.0, 0.0, z]
    ref_1_pos = [2.0, 0.0, z]
    ref_2_pos = [3.0, 0.0, z]
    ref_3_pos = [1.0, 1.0, z]
    ref_4_pos = [2.0, 1.0, z]
    ref_5_pos = [3.0, 1.0, z]
    ref_6_pos = [1.0, 2.0, z]
    ref_7_pos = [2.0, 2.0, z]
    ref_8_pos = [3.0, 2.0, z]
    ref_9_pos = [1.0, 3.0, z]

    # 'anchor_pos' should have a length of 4 elements
    anchor_pos = [anchor_0_pos, anchor_1_pos, anchor_2_pos, anchor_3_pos]
    ref_pos = [ref_0_pos, ref_1_pos, ref_2_pos, ref_3_pos, ref_4_pos, ref_5_pos, ref_6_pos, ref_7_pos, ref_8_pos, ref_9_pos]
    
    # Save calculated DOP values in these lists for later plotting purposes
    GDOPs = []
    PDOPs = []
    HDOPs = []
    VDOPs = []
    
    for ref in ref_pos:
        A = []
        for anchor in anchor_pos:
            distance = distance_between_two_points3D(ref, anchor)
            A.append((anchor[0] - ref[0]) / distance)
            A.append((anchor[1] - ref[1]) / distance)
            A.append((anchor[2] - ref[2]) / distance)
            A.append(-1.0)
        A = np.array(A)
        A = np.reshape(A, (4, len(anchor_pos)))
        C = np.linalg.inv(A.T.dot(A))

        x_variance = C[0][0]
        y_variance = C[1][1]
        z_variance = C[2][2]
        time_variance = C[3][3]

        # Calculate dilution of precision properties
        GDOP = np.sqrt(x_variance + y_variance + z_variance + time_variance)
        GDOPs.append(GDOP)

        TDOP = np.sqrt(time_variance)
        
        PDOP = np.sqrt(x_variance + y_variance + z_variance)
        PDOPs.append(PDOP)
        
        HDOP = np.sqrt(x_variance + y_variance)
        HDOPs.append(HDOP)
        
        VDOP = np.sqrt(z_variance)
        VDOPs.append(VDOP)

    GDOPs.append(np.nan)
    GDOPs.append(np.nan)
    PDOPs.append(np.nan)
    PDOPs.append(np.nan)
    HDOPs.append(np.nan)
    HDOPs.append(np.nan)
    VDOPs.append(np.nan)
    VDOPs.append(np.nan)

    GDOPs = np.ndarray(buffer=np.array(GDOPs), shape=(4, 3))
    PDOPs = np.ndarray(buffer=np.array(PDOPs), shape=(4, 3))
    HDOPs = np.ndarray(buffer=np.array(HDOPs), shape=(4, 3))
    VDOPs = np.ndarray(buffer=np.array(VDOPs), shape=(4, 3))

    # Plot DOPs
    fig = plt.figure("Dilution of Precision Evaluation", figsize=(23, 9))
    fig.suptitle("DOP values for every reference position at {}m altitude".format(z))
    ax0 = plt.subplot(141)
    ax1 = plt.subplot(142)
    ax2 = plt.subplot(143)
    ax3 = plt.subplot(144)

    ax0.set_title("GDOP")
    ax0.set_xlabel('X')
    ax0.set_ylabel('Y')
    ax1.set_title("PDOP")
    ax1.set_xlabel('X')
    ax1.set_ylabel('Y')
    ax2.set_title("HDOP")
    ax2.set_xlabel('X')
    ax2.set_ylabel('Y')
    ax3.set_title("VDOP")
    ax3.set_xlabel('X')
    ax3.set_ylabel('Y')

    a0 = ax0.imshow(GDOPs, interpolation='None', origin='lower', cmap='jet', extent=(.5 , 3.5, -.5, 3.5))
    a1 = ax1.imshow(PDOPs, interpolation='None', origin='lower', cmap='jet', extent=(.5 , 3.5, -.5, 3.5))
    a2 = ax2.imshow(HDOPs, interpolation='None', origin='lower', cmap='jet', extent=(.5 , 3.5, -.5, 3.5))
    a3 = ax3.imshow(VDOPs, interpolation='None', origin='lower', cmap='jet', extent=(.5 , 3.5, -.5, 3.5))
    
    a0_colorbar = fig.colorbar(a0, ax=ax0)
    a0_colorbar.ax.set_title("GDOP", size=18)
    a1_colorbar = fig.colorbar(a1, ax=ax1)
    a1_colorbar.ax.set_title("PDOP", size=18)
    a2_colorbar = fig.colorbar(a2, ax=ax2)
    a2_colorbar.ax.set_title("HDOP", size=18)
    a3_colorbar = fig.colorbar(a3, ax=ax3)
    a3_colorbar.ax.set_title("VDOP", size=18)
    
    current_cmap = cm.get_cmap()
    current_cmap.set_bad(color='black')
    plt.show()

if __name__ == "__main__":
    # See if any argument was given and use it as z value
    print("")
    try:
        z = float(sys.argv[1])
        print("Using custom z value of {}m".format(z))
    except IndexError:
        print("Using default z value of {}m.".format(DEFAULT_Z))
        print("Note: You can add a custom z value as argument when calling this script.")
        print("Example: python3 dilution_of_precision_evaluation.py 1.55")
        z = DEFAULT_Z
    except ValueError:
        print("Error: Unable to cast user argument to float.")
        sys.exit(-1)
    print("")
    
    calculate_and_plot_dop(z)
