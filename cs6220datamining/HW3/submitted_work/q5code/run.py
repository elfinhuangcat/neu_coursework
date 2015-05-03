#!/usr/bin/env python
"""
k-means is sensitive to the starting positions of the cluster centroids. To try
to overcome this, we can run k-means 25 times with randomized starting posi-
tions for the cluster centroids.
For each value of k and for the 25 initial centroid sets, you will run
k-means until either the clusters no longer change or your program has con-
ducted 50 iterations over the data set, whichever comes first.
"""

import numpy as np
import math
import matplotlib.pyplot as plt
from kmeans import *
from tool_methods import *
import time

class KmeansEval:
    def __init__(self, file_path):
        self.MIN_K = 1
        self.MAX_K = 12
        self.ITERS = 25
        # Store the mean SSE for each K:
        self.means = list()
        # Store the sample standard deviation for each K:
        self.sample_sd = list()
        self.datainfo = DataInfo(file_path)
        self.debug = False
    
    def start_analysis(self):
        for k in range(self.MIN_K, self.MAX_K + 1):
            # sse_this_k:
            # sum of the squared error of each cluster for this K,
            # each element represents the SSE of the it ieration
            sse_this_k = list()
            kmeans_obj = KMeans(k, self.datainfo, 1)
            for iteration in range(1, self.ITERS+1):
                if self.debug:
                    start = time.time()
                    
                    kmeans_obj.clear()
                    kmeans_obj.iteration = iteration
                    
                    print "createObj" + str(time.time() - start)
                    kmeans_obj.start_kmeans()
                    print "startKMeans" + str(time.time() - start)
                    sse_this_k.append(kmeans_obj.get_sse())
                    print "getSSE" + str(time.time() - start)
                else:
                    kmeans_obj.clear()
                    kmeans_obj.iteration = iteration
                    kmeans_obj.start_kmeans()
                    sse_this_k.append(kmeans_obj.get_sse())
                    


            self.means.append(mean_of_a_list(sse_this_k))
            self.sample_sd.append(
                sample_sd_of_a_list(sse_this_k, self.means[k-1]))
        
        # Plot the mean sse as a function of k:
        self.plot_k_to_mean_sse()
        # Produce the table:
        self.produce_table()

    def produce_table(self):
        table = open('sse_table', 'w')
        table.write('%2s %15s %15s %15s\n' % ('K',
            'mean','mean - 2sd','mean + 2sd'))
        for i in range(self.MAX_K):
            mean_minus_2sd = self.means[i] - 2 * self.sample_sd[i]
            mean_plus_2sd = self.means[i] + 2 * self.sample_sd[i]
            table.write('%2s %15s %15s %15s\n' % (i+1, self.means[i],
                mean_minus_2sd, mean_plus_2sd))
        table.close()

    def plot_k_to_mean_sse(self):
        x = range(self.MIN_K, self.MAX_K + 1)
        # y = self.means
        yerror = list()
        for i in range(self.MAX_K):
            yerror.append(4 * self.sample_sd[i])
        plt.figure()
        plt.errorbar(x, self.means, yerr = yerror)
        plt.title('K - SSE with Error Bars')
        plt.xlim(0, self.MAX_K + 1)
        plt.xlabel('K')
        plt.ylabel('SSE')
        plt.savefig('plot1.png')



if __name__ == '__main__':
    start = time.time()
    evalobj = KmeansEval('segment.arff')
    evalobj.start_analysis()
    stop = time.time()
    print("Time elapsed: " + str(stop - start))
        

