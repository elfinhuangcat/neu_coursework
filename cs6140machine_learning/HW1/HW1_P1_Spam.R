#==============================================================================
# Yaxin Huang - CS 6140
# Spam Data, Problem 2
# ReadData.R
# This file provides methods to read the data and return the corresponding 
# normalized data frame.
#==============================================================================

# CONSTANTS
kSpamUrl <- paste0("http://archive.ics.uci.edu/ml/",
                   "machine-learning-databases/spambase/spambase.data")
kSpamNamesUrl <- paste0("http://archive.ics.uci.edu/ml/",
                        "machine-learning-databases/spambase/spambase.names")

# The decription of the dataset can be downloaded from this Url:
# <http://archive.ics.uci.edu/ml/machine-learning-databases
#  /spambase/spambase.names>
# You can download the file by executing:
# download.file(paste0("http://archive.ics.uci.edu/ml/",
#                      "machine-learning-databases/spambase/spambase.names"),
#               "spambase.name","auto")

# FUNCTION DEFINITIONS
# TODO(Yaxin): Check the number of instances.
ReadAndNormalizeSpamData <- function(file.path = kSpamUrl, 
                                     names.file.path = kSpamNamesUrl) {
    #==================================================================
    # This function reads the Spam data and normalize the non-label
    # columns, and return the result data frame. To call the function,
    # you may run this command:
    # `data <- ReadAndNormalizeSpamData(kSpamUrl, 1:57, kSpamNamesUrl)`
    # Args:
    #     file.path: The file path of the data file.
    #     names.file.path: The file path of the description file.
    # Returns:
    #     A data frame transformed from the given file path.
    #     The last column stores the labels. 
    #==================================================================
    # Read the file as a data frame
    data <- read.table(file.path, sep = ",")
    feature.num <- ncol(data) - 1    
    # Fetch the feature names
    names.vector <- ReadFeatureNames(feature.num, names.file.path)
    # Assign column names, we consider the labels lie in the 1st col.
    names(data) <- c(names.vector, "label")
    # Normalize the table (except the label)
    indices.to.be.norm <- names(data)[names(data) != "label"]
    for (col.index in indices.to.be.norm) {
        # We normalize column by column
        data[,col.index] <- NormalizeVector(data[,col.index])
    }
    # Set the label column as factor variable:
    data$label <- as.factor(data$label)
    # Return the result data frame
    data
}

ReadFeatureNames <- function(feature.num, 
                             names.file.path = kSpamNamesUrl) {
    #===============================================================
    # Given the number of features and the path of the description 
    # file, returns a vector storing all the feature names.
    #===============================================================
    # Read the names of the features from description file
    names.vec <- tail(readLines(names.file.path), n = feature.num)
    # Fetch the names by trimming and return it.
    gsub(":.*", "", names.vec)
}

NormalizeVector <- function(vector.to.norm) {
    #===============================================================
    # Normalize the given vector using the Shift-and-scale method.
    # Args:
    #     vector.to.norm: The vector to be normalized.
    # Returns:
    #     A normalized vector.
    # The Shift-and-scale normalization:
    # 1. Subtract the minimum
    # 2. Divide by new maximum
    #===============================================================
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

