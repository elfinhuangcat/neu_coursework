#!/usr/bin/env python
"""This module implements the k-means algorithm."""

import numpy as np
import math
from data_process import *
from tool_methods import *
import random
import time
import sys


class KMeans:
    """ Given the data and the k, run the k-means and return the clusters.
        Class members:
        self.data
        self.DATA_COL
        self.DATA_ROW
        self.K
        self.MAX_ITER
        self.cluster
        self.centers
        self.sse
    """
    def __init__(self, k, data_info, iteration):
        """
        @param k: the cluster number
        @param data_info: the instance of DataInfo, storing the data
        @param iteration: 1 <= An integer <= 25.
               iteration helps to decide the starting centers.
        """
        self.data_info = data_info
        self.data = data_info.get_data()
        self.DATA_COL = data_info.get_data_col()
        self.DATA_ROW = data_info.get_data_row()

        self.K = k
        self.MAX_ITER = 50
        self.iteration = iteration

        # cluster is a list of sets, where each set represents a cluster.
        # Each set contains the index to the original dataset.
        self.cluster = list()
        self.__init_cluster()
        # initialize centroids:
        self.centers = list()
        self.__init_centers(
            data_info.get_random_ind()[(self.iteration-1)*k : self.iteration*k])
        # Sum of squared errors
        self.sse = 0

    def clear(self):
        self.cluster = list()
        self.__init_cluster()
        self.centers = list()
        self.__init_centers(
            self.data_info.get_random_ind()[(self.iteration-1)*self.K : self.iteration*self.K])
        self.sse = 0
    
    def get_K(self):
        return self.K
    def get_data(self):
        return self.data
    def get_DATA_COL(self):
        return self.DATA_COL
    def get_DATA_ROW(self):
        return self.DATA_ROW
    def get_cluster(self):
        return self.cluster
    def get_centers(self):
        return self.centers
    def get_sse(self):
        return self.sse

    def __init_cluster(self):
        for dummy_i in range(self.K):
            self.cluster.append(list())

    def __init_centers(self, init_inds):
        """ @param init_inds: 
            a list of indices pointing to the initial centers"""
        for ind in init_inds:
            self.centers.append(self.data[ind,:])

    def assign_membership(self):
        temp_cluster = list()
        for dummy_i in range(self.K):
            temp_cluster.append(list())
        for i in range(self.DATA_ROW):
            # distances store the distances between this point and all other
            # centers.
            min_distance = sys.maxint
            min_ind = 0
            for j in range(self.K):
                distance = self.compute_distance_given_src_ind_fast(i, self.centers[j])
                if distance < min_distance:
                    min_distance = distance
                    min_ind = j
            # Select the closest one to join:
            temp_cluster[min_ind].append(i)
        return temp_cluster
                      
    
    def compute_distance_given_src_ind(self, src_ind, tgt_point):
        """Compute the Euclidean distance given two points.
           @param src_ind: the source point index in dataset.
           @param tgt_point: the point represented as an array."""
        src = self.data[src_ind,:]
        distance = 0
        for i in range(0, len(src)):
            distance += (src[i] - tgt_point[i]) ** 2
        return math.sqrt(distance)
    def compute_distance_given_src_ind_fast(self, src_ind, tgt_point):
        src = self.data[src_ind, :]
        minus = src - tgt_point
        return math.sqrt(np.sum(minus * minus))

 
    def compute_centers(self):
        """Compute the centers of the current self.cluster,
           and update the self.centers."""
        for i in range(self.K):
            self.centers[i] = self.compute_a_center(i)

    def compute_a_center(self, i):
        """Computer the center of the ith cluster in self.cluster."""
        if len(self.cluster[i]) == 0:
            # Randomly return a center if the empty cluster happens:
            ind = random.randint(0, self.DATA_ROW-1)
            return self.data[ind, :]

        center = np.zeros(self.DATA_COL)
        for col in range(self.DATA_COL):
            center[col] = mean_of_a_list(
                self.data[list(self.cluster[i]), col])
        return center


    def start_kmeans(self):
        """ Return a list of sets, each set containing the indices of 
            records who belongs to this cluster."""
        iter_num = 0
        while iter_num < self.MAX_ITER:
            temp_cluster = self.assign_membership()
            converge_flag = True
            
            for i in range(self.K):
                if temp_cluster[i] != self.cluster[i]:
                    converge_flag = False
                    break
            if converge_flag:
                break
            self.cluster = temp_cluster
            # compute new centroids:
            self.compute_centers()
            iter_num += 1
        # Converged:
        self.compute_sse()
        
        print("K = " + str(self.K))
        print("Iterations over the data set: " + str(iter_num))
        print("SSE of this iteration: " + str(self.sse))
        print("***************************************")

    def compute_sse(self):
        for i in range(self.K):
            # For each cluster:
            for point in self.cluster[i]:
                self.sse += self.compute_distance_given_src_ind_fast(
                    point, self.centers[i]) ** 2



def run(data_path, k, iteration):
    datainfo = DataInfo(data_path)
    kmeans_obj = KMeans(k, datainfo, iteration)
    kmeans.start_kmeans()


if __name__ == '__main__':
    # Tests:
    datainfo = DataInfo('segment.arff')
    kmeans = KMeans(1, datainfo, 1) #k=1, iteration=1
    print("========Test start_kmeans()===========")
    kmeans.start_kmeans()

    print()
    print()
    print("==========================another====")
    datainfo = DataInfo('segment.arff')
    kmeans = KMeans(1, datainfo, 2) #k=1, iteration=2
    print("========Test start_kmeans()===========")
    kmeans.start_kmeans()

    print()
    print()
    print("==========================another====")
    datainfo = DataInfo('segment.arff')
    kmeans = KMeans(1, datainfo, 3) #k=1, iteration=3
    print("========Test start_kmeans()===========")
    kmeans.start_kmeans()

    print()
    print()
    print("==========================another====")
    datainfo = DataInfo('segment.arff')
    kmeans = KMeans(4, datainfo, 25) #k=4, iteration=3
    print("========Test start_kmeans()===========")
    kmeans.start_kmeans()
