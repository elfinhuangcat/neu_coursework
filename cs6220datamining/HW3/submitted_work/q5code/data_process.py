#!/usr/bin/env python
"""This module reads the segment.arff file."""
import numpy as np
import math
from tool_methods import *

class DataInfo:
    """ This class provides the methods to read data from the provided arff
        file and structures to store the information we need.

        Information we need:
        1. Attribute names, and number of attributes.
           (We assume all features are real type and the last attribute is 
           the label.)
        2. The dataset itself.

        Class members:
        self.feature_names
        self.data
        self.random_ind
    """
    def __init__(self, file_path):
        """ @param: file_path is the path to the arff file."""
        self.feature_names = list()
        self.data = list()
        self.read_train_file(file_path)
        self.data = np.array(self.data)
        self.random_ind = [775, 1020, 200, 127, 329, 1626, 1515, 
            651, 658, 328, 1160, 108, 422, 88, 105, 261, 212, 
            1941, 1724, 704, 1469, 635, 867, 1187, 445, 222, 
            1283, 1288, 1766, 1168, 566, 1812, 214, 53, 423, 50, 
            705, 1284, 1356, 996, 1084, 1956, 254, 711, 1997,
            1378, 827, 1875, 424, 1790, 633, 208, 1670, 1517, 
            1902, 1476, 1716, 1709, 264, 1, 371, 758, 332, 542, 
            672, 483, 65, 92, 400, 1079, 1281, 145, 1410, 664, 155,
            166, 1900, 1134, 1462, 954, 1818, 1679, 832, 1627, 1760, 
            1330, 913, 234, 1635, 1078, 640, 833, 392, 1425, 610, 
            1353, 1772, 908, 1964, 1260, 784, 520, 1363, 544, 426, 
            1146, 987, 612, 1685, 1121, 1740, 287, 1383, 1923, 1665, 
            19, 1239, 251, 309, 245, 384, 1306, 786, 1814, 7, 1203, 
            1068, 1493, 859, 233, 1846, 1119, 469, 1869, 609, 385, 
            1182, 1949, 1622, 719, 643, 1692, 1389, 120, 1034, 805, 
            266, 339, 826, 530, 1173, 802, 1495, 504, 1241, 427, 
            1555, 1597, 692, 178, 774, 1623, 1641, 661, 1242, 1757, 
            553, 1377, 1419, 306, 1838, 211, 356, 541, 1455, 741, 
            583, 1464, 209, 1615, 475, 1903, 555, 1046, 379, 1938, 
            417, 1747, 342, 1148, 1697, 1785, 298, 1485, 945, 1097, 
            207, 857, 1758, 1390, 172, 587, 455, 1690, 1277, 345, 1166,
            1367, 1858, 1427, 1434, 953, 1992, 1140, 137, 64, 1448, 
            991, 1312, 1628, 167, 1042, 1887, 1825, 249, 240, 524, 
            1098, 311, 337, 220, 1913, 727, 1659, 1321, 130, 1904, 
            561, 1270, 1250, 613, 152, 1440, 473, 1834, 1387, 1656, 
            1028, 1106, 829, 1591, 1699, 1674, 947, 77, 468, 997, 
            611, 1776, 123, 979, 1471, 1300, 1007, 1443, 164, 1881, 
            1935, 280, 442, 1588, 1033, 79, 1686, 854, 257, 1460, 
            1380, 495, 1701, 1611, 804, 1609, 975, 1181, 582, 816, 
            1770, 663, 737, 1810, 523, 1243, 944, 1959, 78, 675, 
            135, 1381, 1472]
        # data normalization:
        self.normalize_data()


    def get_feature_names(self):
        return self.feature_names;
    def get_feature_number(self):
        return len(self.feature_names)
    def get_data(self):
        return self.data
    def get_data_row(self):
        return self.data.shape[0]
    def get_data_col(self):
        return self.data.shape[1]
    def get_random_ind(self):
        return self.random_ind
    def get_random_ind_len(self):
        return len(self.random_ind)


    def normalize_data(self):
        means = list()
        stds = list()
        for feature in range(0, self.get_data_col()):
            # Record the params of ith column
            means.append(np.mean(self.data[:,feature]))
            stds.append(standard_deviation_with_smooth(means[feature], 
                self.data[:,feature]))

        for i in range(0, self.get_data_row()):
            for j in range(0, self.get_data_col()):
                self.data[i,j] = (self.data[i,j] - means[j]) / stds[j]
        #END
       
    def read_train_file(self, file_path):
        file = open(file_path, 'r')
        line = file.readline()
        while '@data' not in line:
            if '@attribute' in line:
                self.feature_names.append(line.split(' ')[1])
            line = file.readline()
        # We don't use the class label
        self.feature_names.pop()
        self.feature_names = tuple(self.feature_names)
        # Read data:
        for line in file:
            self.data.append([float(x) for x in line.split(',')[:-1]])
        file.close()

if __name__ == '__main__':
    data = DataInfo('segment.arff')
    for i in range(data.get_data_row()):
        print(data.get_data()[i,:])
