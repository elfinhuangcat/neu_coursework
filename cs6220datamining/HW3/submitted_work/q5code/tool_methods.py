import math
def mean_of_a_list(nums):
    """Given a list of numbers, return the mean."""
    return float(sum(nums)) / len(nums)

def sample_sd_of_a_list(nums, mean):
    #mean = mean_of_a_list(nums)
    temp_sum = 0
    for num in nums:
        temp_sum += (num - mean) ** 2
    return math.sqrt(float(temp_sum) / (len(nums)-1))

def sd_of_a_list(nums, mean):
    temp_sum = 0
    for num in nums:
        temp_sum += (num - mean) ** 2
    return math.sqrt(float(temp_sum) / len(nums))

def standard_deviation_with_smooth(mean, values):
    std = 0
    for value in values:
        std += (value - mean) ** 2
    if std == 0:
        return 0.0001
    else: 
        return math.sqrt(std/len(values))

def which_min(array):
    """Given an array-like structure, return the index of minimum element 
       in the list."""
    min_ind = 0
    for i in range(0, len(array)):
        if array[i] < array[min_ind]:
            min_ind = i
    return min_ind
