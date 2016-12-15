library(dplyr)
library(cvTools)
library(deepboost)
library(reshape2)
library(stringr)
library(caret)


#importing data


df <- read.csv("file:///C:/Users/Rolf/Desktop/Uni/NYU/Social Networks/SN project/Data/dataCleanResult.csv")


# change variables and dropping inconsistent


df$Result <- ifelse(df$Result>0,1,0)
df$Date <- as.character(df$Date)


year  <- paste("20",str_sub(df$Date,nchar(df$Date)-1,nchar(df$Date)),sep="")
day   <- str_sub(df$Date,1,2) %>% gsub("/","",.)
month <- str_extract(df$Date,"/[0-9]*/") %>%  gsub("/","",.)


df <- df[order(as.Date(paste(year,month,day,sep="-"))),]
df <- df[,c(df[nrow(df),]!="X")*col(df)[1,]]


df$Date <- as.Date(paste(year,month,day,sep="-"))
df <- df[order(df$Date),]


colnames(df)[4] <-"Y"


df.prob <- cbind(df[,1:4],df[,str_sub(names(df),nchar(names(df))-1,nchar(names(df)))=="WP"])
df.prob <- df.prob[,!(names(df.prob) %in% c("PSWP","PSDP","PSLP","VCWP","VCDP","VCLP","WHWP","WHDP","WHLP"))]
df.prob[,5:8] <- apply(df.prob[,5:8],2,function(x) ifelse(as.numeric(x)>0.5,1,0)) %>% ifelse(is.na(.),0,.)


df.prob <- df.prob[order(df.prob$Date),]
df.prob <- cbind(df.prob[,1:3],sapply(df.prob[,4:8], function(x) as.numeric(as.character(x))))




# implementing Majority vote algorithm




y.hat <- list()
w <- rep(1000000,times=ncol(df.prob)-4)
b <- 0.98
l <- rep(0,times=ncol(df.prob)-4)


for (i in 1:nrow(df.prob)){
  
  x <- df.prob[i,5:8]    
  y <- ifelse(sum(x*w)>-sum((x-1)*w),1,0)
  
  y.hat[i] <- y 
  
  if (y !=df.prob$Y[i]){ 
    
    w <- ifelse(x==df.prob$Y[i],w,((1-b)/i+b)*w)
    
  }
  
}




win.odds  <- df[,str_sub(names(df),nchar(names(df)),nchar(names(df)))=="W"]
best.odds <- apply(win.odds,2,function(x) replace(x,x=="X",0))
best.odds <- apply(best.odds,1,max)


pred <- data.frame(Date=df$Date,pred=unlist(y.hat),label=df$Y,win.odds=best.odds)
predtest <- pred[format(pred$Date,"%Y") %in% c("2016","2015"),]


test.error <-1- mean(predtest$pred==predtest$label)


return <- predtest[predtest$pred==1,]


mean(as.numeric(as.character(return$label))*as.numeric(as.character(return$win.odds)))


# IMplementing deepboost. Predictiong wins for the home team


df.odds <- df[,str_sub(names(df),nchar(names(df)),nchar(names(df)))!="P"]
df.odds.train <- df.odds[!(format(df.odds$Date,"%Y") %in% c("2015","2016")), ]
df.odds.test  <- df.odds[format(df.odds$Date,"%Y") %in% c("2015","2016"), ]






boost.cv <- function (df,cv,T,K,beta,lambda){
  
  error <- list()
  folds <- cvFolds(nrow(df),cv)
  
  for (j in 1:cv){
    
    df.valid  <- df[folds$subsets[folds$which==j],]
    df.train  <- df[folds$subsets[folds$which!=j],]
    
    ada   <- deepboost.default(df.train[,names(df.train)!="Y"],ifelse(df.train$Y==1,1,0),beta=beta,lambda = lambda,loss="l",
      tree_depth = K,num_iter = T,verbose = TRUE)
    
    error[[j]] <- mean(deepboost.predict(ada,df.valid[,names(df.valid)!="Y"])!=ifelse(df.valid$Y==1,1,0))
    
  }
  
  return(mean(unlist(error)))
  
}


lambda <- c(0,10^(-c(1,3,5)))
beta   <- c(0,10^(-c(1,3,5)))
iter   <- c(100,200)
K      <- c(1,3,6)


parameters <- expand.grid(lambda,beta,iter,K)
names(parameters) <- c("lambda","beta","iter","K")


cv <- 5


deepboost <- mapply(function(t,k,b,l) boost.cv(df.odds.train[,!(names(df.odds.train) %in% c("HomeTeam","AwayTeam","Date"))],cv,t,k,b,l),
  t=parameters$iter,k=parameters$K, b=parameters$beta,l=parameters$lambda)


deepboost.cv <- cbind(parameters,deepboost)


optim.values <- deepboost.cv[deepboost.cv$deepboost==min(deepboost.cv$deepboost),]


#  lambda beta iter K deepboost
#  0.1    0   200 6 0.2965944


optim.values <- c(0.1,0,200,6)
names(optim.values) <- c("lambda","beta","iter","K")


ada <- deepboost.default(df.odds.train[,!(names(df.odds.train) %in% c("Y","HomeTeam","AwayTeam","Date"))],
  ifelse(df.odds.train$Y==1,1,0),beta=optim.values["beta"],lambda = optim.values["lambda"],loss="l",
  tree_depth = optim.values["K"],num_iter = optim.values["iter"])


test.error <- 1-mean(df.odds.test$Y==deepboost.predict(ada,df.odds.test[,!(names(df.odds.test) %in% c("Y","HomeTeam","AwayTeam","Date"))]))




deepboost.return <- data.frame(Date=df.odds.test$Date,pred=deepboost.predict(ada,df.odds.test[,!(names(df.odds.test) %in% c("Y","HomeTeam","AwayTeam","Date"))])
  ,label=df.odds.test$Y,odds=best.odds[(nrow(df.odds.train)+1):nrow(df)])


deepboost.return <- deepboost.return[deepboost.return$pred==1,]
mean(as.numeric(as.character(deepboost.return$label))*as.numeric(as.character(deepboost.return$odds)))








