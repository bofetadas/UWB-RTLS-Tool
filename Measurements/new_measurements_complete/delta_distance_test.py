import numpy

def distance_between_two_points2D(measurement_point, reference_point):
    if reference_point == None:
        return
    return numpy.sqrt(pow(measurement_point[0] - reference_point[0], 2) + pow(measurement_point[1] - reference_point[1], 2))

def distance_between_two_points3D(measurement_point, reference_point):
    if reference_point == None:
        return
    return numpy.sqrt(pow(measurement_point[0] - reference_point[0], 2) + pow(measurement_point[1] - reference_point[1], 2) + pow(measurement_point[2] - reference_point[2], 2))


def delta_distances(fun, points):
    return map(fun, points, points[1:])[:len(points)-1]

l = [[10,2,5], [3,4,9], [2,3,5], [1,2,6], [1,-7,6]]

rms_test = [1, 2, 3, 4]
print(numpy.sqrt(numpy.mean(numpy.square(rms_test))))
print(numpy.mean(rms_test))
print(numpy.std(rms_test))
print(numpy.sqrt(7.5))

#print(type(delta_distances(distance_between_two_points2D, l)))
#print(type(delta_distances(distance_between_two_points3D, l)))