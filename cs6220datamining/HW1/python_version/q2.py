# -*- coding: utf-8 -*-
"""
Implement L2 regularized linear regression algorithm with 
Lamda ranging from 0 to 150 (integers only)
For each of the 6 dataset, plot both the training set MSE 
and the test set MSE as a function of λ (x-axis) in one graph.
"""

import numpy as np
import matplotlib.pyplot as plt
import pylab
import random

import q1

def learning_curve_mse(trainmx, testmx, lamda, fileName):
    STEP = 10
    REPEAT = 10

    size = STEP    
    mse = np.zeros((2, trainmx.shape[0]/STEP))
    while size <= len(trainmx):
        trainMse = 0
        testMse = 0
        for i in range(REPEAT):
            # indices is used to marked sample indices
            indices = random.sample(range(trainmx.shape[0]), size)
            tempTrainMX = trainmx[tuple(indices),:]
            W = q1.solve_with_penalty(tempTrainMX, lamda)
            trainMse += q1.mean_square_error(tempTrainMX, W)
            testMse += q1.mean_square_error(testmx, W)
        mse[0, size/STEP - 1] = trainMse/REPEAT
        mse[1, size/STEP - 1] = testMse/REPEAT
        size += STEP
    # Draw plot:
    draw_plot(mse, STEP, fileName)

def draw_plot(mse, step, fileName):
    x = range(mse.shape[1])
    for i in range(len(x)):
        x[i] = x[i] * step
    plt.plot(x, mse[0,:], 'r-', x, mse[1,:], 'b-')
    plt.xlabel('Training Size')
    plt.ylabel('Mean Square Error')
    pylab.savefig(fileName, bbox_inches='tight')
    plt.close()


if __name__ == '__main__':
    M1000_100_TRAIN = q1.read_csv_file('input_data/train-1000-100.csv')
    
    M1000_100_TEST = q1.read_csv_file('input_data/test-1000-100.csv')

    learning_curve_mse(M1000_100_TRAIN, M1000_100_TEST, 1, 
                       'pic/q2/lamda1.png')
    
    learning_curve_mse(M1000_100_TRAIN, M1000_100_TEST, 25, 
                       'pic/q2/lamda25.png')

    learning_curve_mse(M1000_100_TRAIN, M1000_100_TEST, 150, 
                       'pic/q2/lamda150.png')



