#!/bin/bash

# Extracts all I frames from passed video creating timestamp files for each
# Creates img_%d.jpeg and timestamp_%d.txt files where %d is an incrementing integer starting at 1.
# Not the nicest of scripts, but works (so far)
#
input_video=$1
output_dir=$2
ffmpeg -i $input_video -vf "select=eq(pict_type\,I)" -an -vsync 0 img_%d.jpeg -loglevel debug 2>&1 | grep "select:1" | awk '{print $6}' | cut -d":" -f2 | awk -v odir="$output_dir" '{print $0 > odir"/metadata_"NR".txt"}'




ffmpeg -i $input_video -vf "select=eq(pict_type\,I)" -an -vsync 0 img_%d.jpeg -loglevel debug 2>&1