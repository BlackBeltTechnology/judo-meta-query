#!/bin/sh
mvn validate -Dtycho.mode=maven -P update-target-versions -f targetdefinition/pom.xml
