#!/bin/bash
var1=$(lsof -i TCP:53248 | grep LISTEN)
#echo $var1
stringarray=($var1)
pid=${stringarray[1]}
echo $pid
kill -9 $pid
