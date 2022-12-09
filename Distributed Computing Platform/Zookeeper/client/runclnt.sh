#!/bin/bash

if [[ -z "$ZOOBINDIR" ]]
then
	echo "Error!! ZOOBINDIR is not set" 1>&2
	exit 1
fi

. $ZOOBINDIR/zkEnv.sh

# TODO Include your ZooKeeper connection string here. Make sure there are no spaces.
# 	Replace with your server names and client ports.
# export ZKSERVER=lab2-10.cs.mcgill.ca:21842,lab2-11.cs.mcgill.ca:21842,lab2-7.cs.mcgill.ca:21842
export ZKSERVER=open-gpu-1.cs.mcgill.ca:21842,open-gpu-6.cs.mcgill.ca:21842,open-gpu-23.cs.mcgill.ca:21842
java -cp $CLASSPATH:../task:.: DistClient "$@"
