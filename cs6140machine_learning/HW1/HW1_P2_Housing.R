#==============================================================================
# Yaxin Huang - CS 6140
# Housing Data, Problem 1
# ReadData.R
# This file provides methods to read the housing data (train/test) 
# and return corresponding normalized data frames.
#==============================================================================

# Source and Library

# CONSTANTS
kHousingTrainUrl <- paste0("http://www.ccs.neu.edu/home/vip/teach/MLcourse/",
                           "data/housing_train.txt")
kHousingTestUrl <- paste0("http://www.ccs.neu.edu/home/vip/teach/MLcourse/",
                          "data/housing_test.txt")
kAttributeNames <- c("CRIM","ZN","INDUS","CHAS","NOX","RM","AGE",
                     "DIS","RAD","TAX","PTRATIO","B","LSTAT","MEDV")
kFeatureInd <- 1:13
kLabelInd <- 14

# The decription of the dataset can be downloaded from this Url:
# <http://www.ccs.neu.edu/home/vip/teach/MLcourse/data/housing_desc.txt>
# You can download the file by executing:
# download.file(paste0("http://www.ccs.neu.edu/home/vip/teach/",
#                      "MLcourse/data/housing_desc.txt"),
#               "housing_desc.txt","auto")

# FUNCTION DEFINITIONS
ReadAndNormalizeHousingData <- function(file.path,
                                        indices.to.be.norm = kFeatureInd,
                                        names.vector = kAttributeNames) {
    #==========================================================
    # This function reads the housing training or testing data
    # and return the result normalized data frame.
    # How to use this function:
    # 1. To read the training data:
    #    You may run the following command:
    #    train <- ReadAndNormalizeHousingData(kHousingTrainUrl,
    #                                         1:13, kAttributeNames)
    # 2. To read the testing data:
    #    You may run the following command:
    #    test <- ReadAndNormalizeHousingData(kHousingTestUrl,
    #                                        1:13, kAttributeNames)
    # Args:
    #     file.path: The file path of the train or test file.
    #     indices.to.be.norm: The indices of columns to normalize.
    #     names.vector: The vector storing the attribute names,
    #                   including the name of the label column.
    # Returns:
    #     A normalized data frame of the train of test data.
    #==========================================================
    # Read the file and trim the leading/tailing whitespaces
    file <- trim(readLines(file.path))
    # Delete extra rows
    file <- file[nchar(file) > 0]
    row.number <- length(file)
    # Transform the character vector to a data frame
    # to enable our analysis
    data <- data.frame(matrix(unlist(strsplit(file,"\\s+")), 
                              nrow = row.number, byrow = T),
                       stringsAsFactors = FALSE)
    # Assign column names:
    names(data) <- names.vector
    # Because now the values are treated as characters,
    # we transform them to numeric values.
    for (col in 1:ncol(data)) {
        data[,col] <- as.numeric(data[,col])
    }
    # Normalize the data except the label column
    for (col in indices.to.be.norm) {
        data[,col] <- NormalizeVector(data[,col])
    }
    data
}

NormalizeVector <- function(vector.to.norm = kFeatureInd) {
    # Normalize the given vector using the Shift-and-scale method.
    # Args:
    #     vector.to.norm: The vector to be normalized.
    # Returns:
    #     A normalized vector.
    min.before.subtract <- min(vector.to.norm)
    vector.to.norm <- vector.to.norm - min.before.subtract
    new.max <- max(vector.to.norm)
    vector.to.norm <- vector.to.norm / new.max
    vector.to.norm
}

trim <- function(x) {
    # This function trims the leading and tailing whitespaces
    # of a character vector
    # Args:
    #     x: A character vector
    # Returns:
    #     A character vector without leading and tailing whitespaces
    gsub("^\\s+|\\s+$", "", x)
}


