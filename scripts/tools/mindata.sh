#!/bin/bash
mongoimport --drop -d redtorch_j_tick_db -c rb1901.SHFE  --file $1
