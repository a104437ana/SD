#!/bin/bash
if [ $# -eq 0 ]
    then
        rm *.class
elif [ $# -eq 1 ] && [ $1 = "tmp" ]
    then
        rm -R tmp/*.csv
fi
