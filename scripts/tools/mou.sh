#!/bin/bash
mongoexport --pretty -d RtClientDemo -c StrategySetting -o $1
