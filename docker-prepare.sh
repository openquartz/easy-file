#! /bin/bash
###########################################
#
###########################################

# constants
baseDir=$(cd `dirname "$0"`;pwd)
mkdir -p -m 700 baseDir/../databse/mysql/data baseDir/../databse/redis/data baseDir/../data baseDir/../logs
rm -rf baseDir