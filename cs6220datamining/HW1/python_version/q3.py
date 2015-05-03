"""
From the plots in question 1, we can tell which value of lamda is best for
each dataset once we know the test data and its labels. This is not 
realistic in real world applications. In this part, we use cross 
validation to set the value for lamda. Implement the CV technique given in 
the class slides. For each dataset, compared the values of lamda and MSE with
the values in question 1).
"""

import numpy as np
import matplotlib.pyplot as plt
import pylab
import q1

def best_lamda_cv(trainmx):
    FOLDNUM = 10
    LAMDA_MAX = 150
    foldSize = trainmx.shape[0] / FOLDNUM
    testMse = list()
    
    for lamda in range(LAMDA_MAX+1):
        testMseLamda = 0
        for i in range(FOLDNUM):
            testfoldBeginInd = foldSize * i
            testfoldEndInd = foldSize * (i+1)
            
            # Train on all fold except the test fold:
            trainmxThisFold = np.append(trainmx[:testfoldBeginInd,:],
                                        trainmx[testfoldEndInd:, :],
                                        axis = 0)
            W = q1.solve_with_penalty(trainmxThisFold,lamda)
            # Test on the test fold and record the MSE:
            testfold = trainmx[testfoldBeginInd:testfoldEndInd, :]
            testMseLamda += q1.mean_square_error(testfold, W)
        # Compute the average performance for this lamda:
        testMse.append(testMseLamda / FOLDNUM)
    
    # Pick the value of lamda with the best average performance
    minMse = min(testMse)
    for i in range(len(testMse)):
        if testMse[i] == minMse:
            return i

if __name__ == '__main__':
    MH50_100_TRAIN = q1.read_csv_file('input_data/50(1000)_100_train.csv')
    MH100_100_TRAIN = q1.read_csv_file('input_data/100(1000)_100_train.csv')
    MH150_100_TRAIN = q1.read_csv_file('input_data/150(1000)_100_train.csv')
    M1000_100_TRAIN = q1.read_csv_file('input_data/train-1000-100.csv')
    M100_10_TRAIN = q1.read_csv_file('input_data/train-100-10.csv')
    M100_100_TRAIN = q1.read_csv_file('input_data/train-100-100.csv')
    
    M100_10_TEST = q1.read_csv_file('input_data/test-100-10.csv')
    M100_100_TEST = q1.read_csv_file('input_data/test-100-100.csv')
    M1000_100_TEST = q1.read_csv_file('input_data/test-1000-100.csv')

    log = open('log/log_q3.txt', 'w')
    
    # MH50_100_TRAIN vs M1000_100_TEST
    lamda = best_lamda_cv(MH50_100_TRAIN)
    print('Best lamda for H50_100_TRAIN: ' + str(lamda))
    log.write('Best lamda for H50_100_TRAIN: ' + str(lamda) + '\n')
    W = q1.solve_with_penalty(MH50_100_TRAIN, lamda)
    mse = q1.mean_square_error(M1000_100_TEST, W)
    print('1000_100_TEST MSE for this lamda: ' + str(mse))
    log.write('1000_100_TEST MSE for this lamda: ' + str(mse) + '\n\n')

    # MH100_100_TRAIN vs M1000_100_TEST
    lamda = best_lamda_cv(MH100_100_TRAIN)
    print('Best lamda for H100_100_TRAIN: ' + str(lamda))
    log.write('Best lamda for H100_100_TRAIN: ' + str(lamda) + '\n')
    W = q1.solve_with_penalty(MH100_100_TRAIN, lamda)
    mse = q1.mean_square_error(M1000_100_TEST, W)
    print('1000_100_TEST MSE for this lamda: ' + str(mse))
    log.write('1000_100_TEST MSE for this lamda: ' + str(mse) + '\n\n')

    # MH150_100_TRAIN vs M1000_100_TEST
    lamda = best_lamda_cv(MH150_100_TRAIN)
    print('Best lamda for H150_100_TRAIN: ' + str(lamda))
    log.write('Best lamda for H150_100_TRAIN: ' + str(lamda) + '\n')
    W = q1.solve_with_penalty(MH150_100_TRAIN, lamda)
    mse = q1.mean_square_error(M1000_100_TEST, W)
    print('1000_100_TEST MSE for this lamda: ' + str(mse))
    log.write('1000_100_TEST MSE for this lamda: ' + str(mse) + '\n\n')

    # M1000_100_TRAIN vs M1000_100_TEST
    lamda = best_lamda_cv(M1000_100_TRAIN)
    print('Best lamda for 1000_100_TRAIN: ' + str(lamda))
    log.write('Best lamda for 1000_100_TRAIN: ' + str(lamda) + '\n')
    W = q1.solve_with_penalty(M1000_100_TRAIN, lamda)
    mse = q1.mean_square_error(M1000_100_TEST, W)
    print('1000_100_TEST MSE for this lamda: ' + str(mse))
    log.write('1000_100_TEST MSE for this lamda: ' + str(mse) + '\n\n')

    # M100_10_TRAIN vs M100_10_TEST
    lamda = best_lamda_cv(M100_10_TRAIN)
    print('Best lamda for 100_10_TRAIN: ' + str(lamda))
    log.write('Best lamda for 100_10_TRAIN: ' + str(lamda) + '\n')
    W = q1.solve_with_penalty(M100_10_TRAIN, lamda)
    mse = q1.mean_square_error(M100_10_TEST, W)
    print('100_10_TEST MSE for this lamda: ' + str(mse))
    log.write('100_10_TEST MSE for this lamda: ' + str(mse) + '\n\n')

    # M100_100_TRAIN vs M100_100_TEST
    lamda = best_lamda_cv(M100_100_TRAIN)
    print('Best lamda for 100_100_TRAIN: ' + str(lamda))
    log.write('Best lamda for 100_100_TRAIN: ' + str(lamda) + '\n')
    W = q1.solve_with_penalty(M100_100_TRAIN, lamda)
    mse = q1.mean_square_error(M100_100_TEST, W)
    print('100_100_TEST MSE for this lamda: ' + str(mse))
    log.write('100_100_TEST MSE for this lamda: ' + str(mse) + '\n\n')

    log.close()
