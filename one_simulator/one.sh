#! /bin/sh
java -Xmx512M -cp target:lib/ECLA.jar:lib/DTNConsoleConnection.jar:lib/json-simple-1.1.1.jar core.DTNSim $*
