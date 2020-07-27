from matplotlib import pyplot as plt
import numpy as np
import sys

process_noise = 0.001
measurement_noise = 0.1
process_noise_variance = process_noise * process_noise
measurement_noise_variance = measurement_noise * measurement_noise
initial_state_estimate_x = 3
initial_state_estimate_error_x = 0.2
initial_state_estimate_y = 0
initial_state_estimate_error_y = 0.2
initial_state_estimate_z = 1.9
initial_state_estimate_error_z = 0.2
predicted_state_estimate_x = initial_state_estimate_x
predicted_state_estimate_error_x = initial_state_estimate_error_x + process_noise_variance
predicted_state_estimate_y = initial_state_estimate_y
predicted_state_estimate_error_y = initial_state_estimate_error_y + process_noise_variance
predicted_state_estimate_z = initial_state_estimate_z
predicted_state_estimate_error_z = initial_state_estimate_error_z + process_noise_variance

def get_sample_count(filename):
    with open(filename) as f:
        for i, l in enumerate(f):
            pass
    return i + 1

def kalman_filter(filename):
    global predicted_state_estimate_x, predicted_state_estimate_error_x, predicted_state_estimate_y, predicted_state_estimate_error_y, predicted_state_estimate_z, predicted_state_estimate_error_z
    measurements_x = []
    measurements_y = []
    measurements_z = []
    state_estimates_x = []
    state_estimates_y = []
    state_estimates_z = []
    state_estimates_errors_x = []
    state_estimates_errors_y = []
    state_estimates_errors_z = []
    with open(filename) as f:
        for line in f:
            # "Measure"
            x = round(float(line.split(',')[0]), 3)
            y = round(float(line.split(',')[1]), 3)
            z = round(float(line.split(',')[2]), 3)
            measurements_x.append(x)
            measurements_y.append(y)
            measurements_z.append(z)
            current_state_estimate_x, current_estimate_error_x = update(x, predicted_state_estimate_x, predicted_state_estimate_error_x)
            current_state_estimate_y, current_estimate_error_y = update(y, predicted_state_estimate_y, predicted_state_estimate_error_y)
            current_state_estimate_z, current_estimate_error_z = update(z, predicted_state_estimate_z, predicted_state_estimate_error_z)
            predicted_state_estimate_x, predicted_state_estimate_error_x = predict(current_state_estimate_x, current_estimate_error_x)
            predicted_state_estimate_y, predicted_state_estimate_error_y = predict(current_state_estimate_y, current_estimate_error_y)
            predicted_state_estimate_z, predicted_state_estimate_error_z = predict(current_state_estimate_z, current_estimate_error_z)
            state_estimates_x.append(predicted_state_estimate_x)
            state_estimates_errors_x.append(predicted_state_estimate_error_x)
            state_estimates_y.append(predicted_state_estimate_y)
            state_estimates_errors_y.append(predicted_state_estimate_error_y)
            state_estimates_z.append(predicted_state_estimate_z)
            state_estimates_errors_z.append(predicted_state_estimate_error_z)

    return measurements_x, measurements_y, measurements_z, state_estimates_x, state_estimates_y, state_estimates_z, state_estimates_errors_x, state_estimates_errors_y, state_estimates_errors_z


def update(measurement, state_estimate, state_estimate_error):
    k = state_estimate_error / (state_estimate_error + measurement_noise_variance)
    current_state_estimate = state_estimate + k * (measurement - state_estimate)
    current_state_estimate_error = (1 - k) * state_estimate_error
    return current_state_estimate, current_state_estimate_error

def predict(current_state_estimate, current_state_estimate_error):
    predicted_state_estimate = current_state_estimate
    predicted_estimate_error = current_state_estimate_error + process_noise_variance
    return predicted_state_estimate, predicted_estimate_error

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

def plot_coordinates(reference_point, sample_count, measurement_data_x, kalman_data_x, measurement_data_y, kalman_data_y, measurement_data_z, kalman_data_z):
    fig = plt.figure()
    
    ax1 = fig.add_subplot(311)
    ax1.plot(range(sample_count), measurement_data_x, label='Measurements X', c='r')
    ax1.plot(range(sample_count), kalman_data_x, label='Kalman X', c='b')
    ax1.axhline(np.mean(measurement_data_x), 0, 1, label='Measurement Mean X', c='y')
    ax1.axhline(np.mean(kalman_data_x), 0, 1, label='Kalman Mean X', c='g')
    ax1.axhline(reference_point[0], 0, 1, label='Actual X', c='black')
    ax1.legend()

    ax2 = fig.add_subplot(312)
    ax2.plot(range(sample_count), measurement_data_y, label='Measurements Y', c='r')
    ax2.plot(range(sample_count), kalman_data_y, label='Kalman Y', c='b')
    ax2.axhline(np.mean(measurement_data_y), 0, 1, label='Measurement Mean Y', c='y')
    ax2.axhline(np.mean(kalman_data_y), 0, 1, label='Kalman Mean Y', c='g')
    ax2.axhline(reference_point[1], 0, 1, label='Actual Y', c='black')
    ax2.legend()

    ax3 = fig.add_subplot(313)
    ax3.plot(range(sample_count), measurement_data_z, label='Measurements Z', c='r')
    ax3.plot(range(sample_count), kalman_data_z, label='Kalman Z', c='b')
    ax3.axhline(np.mean(measurement_data_z), 0, 1, label='Measurement Mean Z', c='y')
    ax3.axhline(np.mean(kalman_data_z), 0, 1, label='Kalman Mean Z', c='g')
    ax3.axhline(reference_point[2], 0, 1, label='Actual Z', c='black')
    ax3.legend()

    plt.show()

if __name__ == "__main__":
    try:
        filename = sys.argv[1]
    except IndexError:
        sys.exit(1)
    
    x_reference = float((filename.split('(')[1].split(')')[0].split('_')[0]).replace(',', '.'))
    y_reference = float((filename.split('(')[1].split(')')[0].split('_')[1]).replace(',', '.'))
    z_reference = float((filename.split('(')[1].split(')')[0].split('_')[2]).replace(',', '.'))
    reference_point = [x_reference, y_reference, z_reference]
    sample_count = get_sample_count(filename)

    measurements_x, measurements_y, measurements_z, state_estimates_x, state_estimates_y, state_estimates_z, state_estimates_errors_x, state_estimates_errors_y, state_estimates_errors_z = kalman_filter(filename)
    plot_coordinates(reference_point, sample_count, measurements_x, state_estimates_x, measurements_y, state_estimates_y, measurements_z, state_estimates_z)
