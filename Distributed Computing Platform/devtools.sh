#!/bin/bash
# a=$(pwd)
a=~
# b="zookeeper-local"

# do 
# export ZOOBINDIR=~/apache-zookeeper-3.6.2-bin/bin
# . $ZOOBINDIR/zkEnv.sh
# on each server

b="apache-zookeeper-3.6.2-bin"

if [ "$1" == "startensemble" ]; then
    cd ~/apache-zookeeper-3.6.2-bin/$(hostname)
    ~/apache-zookeeper-3.6.2-bin/bin/zkServer.sh start zoo-base.cfg
    cd $a
fi

if [ "$1" == "stopensemble" ]; then
    cd ~/apache-zookeeper-3.6.2-bin/$(hostname)
    ~/apache-zookeeper-3.6.2-bin/bin/zkServer.sh stop zoo-base.cfg
    cd $a
fi

if [ "$1" == "startclient" ]; then
    ~/$b/bin/zkCli.sh -server open-gpu-1.cs.mcgill.ca:21842,open-gpu-6.cs.mcgill.ca:21842,open-gpu-23.cs.mcgill.ca:21842  
    # ~/$b/bin/zkCli.sh -server lab2-10.cs.mcgill.ca:21842,lab2-11.cs.mcgill.ca:21842,lab2-7.cs.mcgill.ca:21842
fi

if [ "$1" == "clean" ]; then 
    rm ./zk/dist/*.class 
    rm ./zk/clnt/*.class
    rm ./zk/task/*.class 
    rm ./zk/dist/*.log
    rm ./zk/clnt/*.log
fi

if [ "$1" == "compile" ]; then
    cd zk/dist
    ./compilesrvr.sh 
    cd ../..
    cd zk/clnt 
    ./compileclnt.sh
    cd ../..
    cd zk/task
    ./compiletask.sh
    cd $a 
fi

if [ "$1" == "runserver" ]; then


    cd zk/dist 
    ./runsrvr.sh
    cd $a
fi


if [ "$1" == "startzkclient" ]; then
    cd zk/clnt
    ./runclnt.sh 
    cd $a

fi
# if [ "$1" == "client" ]; then
#     $a/$b/bin/zkCli.sh -server $(hostname):21842
# fi