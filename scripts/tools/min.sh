#!/bin/bash
mongoimport --drop -d RtClientDemo -c StrategySetting --file $1
