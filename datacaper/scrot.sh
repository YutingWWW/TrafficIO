#!/bin/sh
LOCATION="$(date +/home/sun/shots/%Y/%m/%d)"
mkdir -p $LOCATION
cd $LOCATION
DISPLAY=:0 scrot '%Y-%m-%d-%H%M.png' -q 100
