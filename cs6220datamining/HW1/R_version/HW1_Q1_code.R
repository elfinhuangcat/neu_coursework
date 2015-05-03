## Implement L2 regularized linear regression algorithm with 
## λ range from 0 to 150 (integers only)
## For each of the 6 dataset, plot both the training set MSE 
## and the test set MSE as a function of λ (x-axis) in one graph.

## install.packages(c("ggplot2","MASS"))

#-----------------------------------------------------------------------------#
# PART I: Read Training/Testing Data

# How to name the data frames:
# "train/test" - training / testing set
# "rXXX" - How many records are included.
# "cXXX" - How many columns(attributes) are included.
train.r1000.c100 <- read.csv(file="train-1000-100.csv")
train.r50.c100 <- head(n=50, train.r1000.c100)
train.r100.c100.2 <- head(n=100, train.r1000.c100)
train.r150.c100 <- head(n=150, train.r1000.c100)

# Generate the 50(1000)_100_train.csv
write.csv(x=train.r50.c100, file="50(1000)_100_train.csv")
# Generate the 100(1000)_100_train.csv
write.csv(x=train.r100.c100.2, file="100(1000)_100_train.csv")
# Generate the 150(1000)_100_train.csv
write.csv(x=train.r150.c100, file="150(1000)_100_train.csv")

train.r100.c100 <- read.csv(file="train-100-100.csv")
train.r100.c10 <- read.csv(file="train-100-10.csv")

test.r100.c10 <- read.csv(file="test-100-10.csv")
test.r100.c100 <- read.csv(file="test-100-100.csv")
test.r1000.c100 <- read.csv(file="test-1000-100.csv")
#-----------------------------------------------------------------------------#
library(MASS)
# PART II: Function Definitions
old.SolveW <- function(X,Y) {
    ## This function uses the close form solution (without lamda)
    ## Given: X and labels Y
    ## X(a rows * b columns)
    ## Y(a rows * 1 column)
    ## Returns: W (b rows * 1 column)
    solve(t(X) %*% X) %*% t(X) %*% Y
}

old.SolveWPenalty <- function(X,Y,lamda) {
    ## This function uses the close form solution (WITH lamda)
    ## Given: X, labels Y and value of lamda
    ## X(a rows * b columns)
    ## Y(a rows * 1 column)
    ## lamda: An integer
    ## Returns: W (b rows * 1 column)
    solve(t(X) %*% X + lamda * diag(ncol(X))) %*% t(X) %*% Y
}

MSE <- function(X, Y, W) {
    ## X: matrix(a*b)
    ## Y: matrix(a*1)
    ## W: matrix(b*1)
    norm(X%*%W - Y)^2 / nrow(X)
}

SolveW <- function(X,Y) {
    ## This function uses the close form solution (without lamda)
    ## Given: X and labels Y
    ## X(a rows * b columns)
    ## Y(a rows * 1 column)
    ## Returns: W (b rows * 1 column)
    ginv(t(X) %*% X) %*% t(X) %*% Y
}

SolveWPenalty <- function(X,Y,lamda) {
    ## This function uses the close form solution (WITH lamda)
    ## Given: X, labels Y and value of lamda
    ## X(a rows * b columns)
    ## Y(a rows * 1 column)
    ## lamda: An integer
    ## Returns: W (b rows * 1 column)
    ginv(t(X) %*% X + lamda * diag(ncol(X))) %*% t(X) %*% Y
}

#-----------------------------------------------------------------------------#

# Training and Testing pairs:
# -------------------------------------------
## Training Set         |  Testing Set
# -------------------------------------------
## train.r1000.c100     |  test.r1000.c100
## train.r50.c100       |  test.r1000.c100
## train.r100.c100.2    |  test.r1000.c100
## train.r150.c100      |  test.r1000.c100
## train.r100.c100      |  test.r100.c100
## train.r100.c10       |  test.r100.c10
#-----------------------------------------------------------------------------#
# PART II: Generate MSE Graph for each pair of train-test
TestLamda <- function(train.df, test.df) {
    ## This function takes the training and testing data frame and use the L2
    ## regularized linear regression to compute the W. For each loop, the 
    ## training error and testing error will be computed and the function will 
    ## output the graph as png files.
    ## Given: Training data frame and Testing data frame,
    ##        where the last column is the label values.
    ## Returns: A matrix where the first row is Training MSE and second is 
    ##          Testing MSE.
    train.mse <- rep(0,151) # Vector to store Train MSE for each lamda
    test.mse <- rep(0,151) # Vector to store Test MSE for each lamda
    X <- as.matrix(train.df[,-ncol(train.df)])
    Y <- as.vector(train.df[,ncol(train.df)])
    for (lamda in 0:150) {
        W <- SolveWPenalty(X, Y, lamda)
        train.mse[lamda+1] <- MSE(X,Y,W)
        test.mse[lamda+1] <- MSE(as.matrix(test.df[,-ncol(test.df)]),
                                 as.matrix(test.df[,ncol(test.df)]),
                                 W)
    }
    rbind(train.mse, test.mse)
}

# Generate directory to store graphs:
pic.dir <- paste0(getwd(), "/pic/")
q1.pic.dir <- paste0(pic.dir,"q1/")
if (!file.exists(pic.dir)){
    dir.create(file.path(pic.dir))
}
if (!file.exists(q1.pic.dir)){
    dir.create(file.path(q1.pic.dir))
}

# 1. train.r1000.c100     |  test.r1000.c100
library("ggplot2")
mse <- TestLamda(train.r1000.c100, test.r1000.c100)
png(paste0(q1.pic.dir,"train(1000_100)_test(1000_100).png"))
mse <- as.data.frame(t(mse))
ggplot(mse, aes(0:150)) + 
    geom_line(aes(y = train.mse, colour = "train.mse")) + 
    geom_line(aes(y = test.mse, colour = "test.mse")) +
    theme(legend.title=element_blank()) +
    xlab("Lamda") + ylab("Mean Square Error") +
    ggtitle("Train(1000_100) vs Test(1000_100)")
dev.off()

# 2. train.r50.c100       |  test.r1000.c100
mse <- TestLamda(train.r50.c100, test.r1000.c100)
png(paste0(q1.pic.dir,"train(h50_100)_test(1000_100).png"))
mse <- as.data.frame(t(mse))
ggplot(mse, aes(0:150)) + 
    geom_line(aes(y = train.mse, colour = "train.mse")) + 
    geom_line(aes(y = test.mse, colour = "test.mse")) +
    theme(legend.title=element_blank()) +
    xlab("Lamda") + ylab("Mean Square Error") +
    ggtitle("Train(Head50_100) vs Test(1000_100)")
dev.off()

# 3. train.r100.c100.2    |  test.r1000.c100
mse <- TestLamda(train.r100.c100.2, test.r1000.c100)
png(paste0(q1.pic.dir,"train(h100_100)_test(1000_100).png"))
mse <- as.data.frame(t(mse))
ggplot(mse, aes(0:150)) + 
    geom_line(aes(y = train.mse, colour = "train.mse")) + 
    geom_line(aes(y = test.mse, colour = "test.mse")) +
    theme(legend.title=element_blank()) +
    xlab("Lamda") + ylab("Mean Square Error") +
    ggtitle("Train(Head100_100) vs Test(1000_100)")
dev.off()

# 4. train.r150.c100      |  test.r1000.c100
mse <- TestLamda(train.r150.c100, test.r1000.c100)
png(paste0(q1.pic.dir,"train(h150_100)_test(1000_100).png"))
mse <- as.data.frame(t(mse))
ggplot(mse, aes(0:150)) + 
    geom_line(aes(y = train.mse, colour = "train.mse")) + 
    geom_line(aes(y = test.mse, colour = "test.mse")) +
    theme(legend.title=element_blank()) +
    xlab("Lamda") + ylab("Mean Square Error") +
    ggtitle("Train(Head150_100) vs Test(1000_100)")
dev.off()

# 5. train.r100.c100      |  test.r100.c100
mse <- TestLamda(train.r100.c100, test.r100.c100)
png(paste0(q1.pic.dir,"train(100_100)_test(100_100).png"))
mse <- as.data.frame(t(mse))
ggplot(mse, aes(0:150)) + 
    geom_line(aes(y = train.mse, colour = "train.mse")) + 
    geom_line(aes(y = test.mse, colour = "test.mse")) +
    theme(legend.title=element_blank()) +
    xlab("Lamda") + ylab("Mean Square Error") +
    ggtitle("Train(100_100) vs Test(100_100)")
dev.off()

# 6. train.r100.c10       |  test.r100.c10
mse <- TestLamda(train.r100.c10, test.r100.c10)
png(paste0(q1.pic.dir,"train(100_10)_test(100_10).png"))
mse <- as.data.frame(t(mse))
ggplot(mse, aes(0:150)) + 
    geom_line(aes(y = train.mse, colour = "train.mse")) + 
    geom_line(aes(y = test.mse, colour = "test.mse")) +
    theme(legend.title=element_blank()) +
    xlab("Lamda") + ylab("Mean Square Error") +
    ggtitle("Train(100_10) vs Test(100_10)")
dev.off()

#-----------------------------------------------------------------------------#
# PART III: Create Learning Curves When lamda = 1, 25, 150
#           Here we use the dataframe train.r1000.c100 / test.r1000.c100
sample.times <- 10 # For each size, sample "sample.times" times
step <- 2 # The sample training set size increases by 2.

VaryTrainSizeMSE <- function(train.df, test.df, lamda) {
    ## This function takes the training and testing data frames and the 
    ## lamda and calculate the Train/Test MSE.
    ## Given: Training data frame and Testing data frame,
    ##        where the last column is the label values.
    ##        TRAINING DATA HAVE 1000 RECORDS.
    ##        lamda.
    ## Returns: A matrix where the first row is Training MSE and second is 
    ##          Testing MSE.
    
    
    # Vector to store Train MSE for each lamda:
    train.mse <- rep(0, nrow(train.df)/step)
    # Vector to store Test MSE for each lamda:
    test.mse <- rep(0, nrow(train.df)/step) 
    
    X <- as.matrix(train.df[,-ncol(train.df)])
    Y <- as.vector(train.df[,ncol(train.df)])
    test.X <- as.matrix(test.df[,-ncol(test.df)])
    test.Y <- as.matrix(test.df[,ncol(test.df)])
    
    indices <- 1:nrow(X) # for sampling
    
    size <- step
    pointer <- 1 # To point to the current index of train.mse/test.mse
    while (size <= nrow(train.df)) {
        temp.train.mse.vec <- rep(0, sample.times)
        temp.test.mse.vec <- rep(0, sample.times)
        for (i in 1:sample.times) {
            temp.ind <- sample(indices, size)
            temp.X <- X[temp.ind,]
            temp.Y <- Y[temp.ind]
            temp.W <- SolveWPenalty(temp.X, temp.Y, lamda)
            temp.train.mse.vec[i] <- MSE(temp.X, temp.Y, temp.W)
            temp.test.mse.vec[i] <- MSE(test.X, test.Y, temp.W)
        }
        train.mse[pointer] <- mean(temp.train.mse.vec)
        test.mse[pointer] <- mean(temp.test.mse.vec)
        pointer <- pointer + 1
        size <- size + step
    }
    rbind(train.mse, test.mse)
}


q2.pic.dir <- paste0(pic.dir,"q2/")
if (!file.exists(q2.pic.dir)){
    dir.create(file.path(q2.pic.dir))
}

# 1. lamda = 1
mse <- VaryTrainSizeMSE(train.r1000.c100, test.r1000.c100, 1)
png(paste0(q2.pic.dir,"lamda1.png"))
mse <- as.data.frame(t(mse))
ggplot(mse, aes(seq_along(train.mse) * step)) + 
    geom_line(aes(y = train.mse, colour = "train.mse")) + 
    geom_line(aes(y = test.mse, colour = "test.mse")) +
    theme(legend.title=element_blank()) +
    xlab("Training Set Size") + ylab("Mean Square Error") +
    ggtitle("Learning Curve (Lamda = 1)")
dev.off()
# 2. lamda = 25
mse <- VaryTrainSizeMSE(train.r1000.c100, test.r1000.c100, 25)
png(paste0(q2.pic.dir,"lamda25.png"))
mse <- as.data.frame(t(mse))
ggplot(mse, aes(seq_along(train.mse) * step)) + 
    geom_line(aes(y = train.mse, colour = "train.mse")) + 
    geom_line(aes(y = test.mse, colour = "test.mse")) +
    theme(legend.title=element_blank()) +
    xlab("Training Set Size") + ylab("Mean Square Error") +
    ggtitle("Learning Curve (Lamda = 25)")
dev.off()
# 3. lamda = 150
mse <- VaryTrainSizeMSE(train.r1000.c100, test.r1000.c100, 150)
png(paste0(q2.pic.dir,"lamda1.png"))
mse <- as.data.frame(t(mse))
ggplot(mse, aes(seq_along(train.mse) * step)) + 
    geom_line(aes(y = train.mse, colour = "train.mse")) + 
    geom_line(aes(y = test.mse, colour = "test.mse")) +
    theme(legend.title=element_blank()) +
    xlab("Training Set Size") + ylab("Mean Square Error") +
    ggtitle("Learning Curve (Lamda = 150)")
dev.off()

#-----------------------------------------------------------------------------#
# PART IV: 