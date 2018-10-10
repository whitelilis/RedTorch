#!/bin/bash
sed  -e 's@fake@sim@'  -e 's@9999.simnow724.187.10030@9999.simnow1.187.10000@' $1 > $2

