#!/bin/bash

#
# This script executes MOLGENIS/compute with the required jars added to the CLASSPATH.
#

set -e
set -u

#
# Get the absolute path to where this script is located and resolve any symlinks.
#
MCDIR=$( cd -P "$( dirname "$0" )" && pwd )

java -cp \
${MCDIR}:\
${MCDIR}/lib/spring-core-4.1.6.RELEASE.jar:\
${MCDIR}/lib/spring-context-4.1.6.RELEASE.jar:\
${MCDIR}/lib/spring-beans-4.1.6.RELEASE.jar:\
${MCDIR}/lib/molgenis-core-1.2.0.jar:\
${MCDIR}/lib/molgenis-data-1.2.0.jar:\
${MCDIR}/lib/molgenis-data-excel-1.2.0.jar:\
${MCDIR}/lib/molgenis-data-csv-1.2.0.jar:\
${MCDIR}/lib/molgenis-compute-core-1.4.0-SNAPSHOT.jar:\
${MCDIR}/lib/commons-cli-1.2.jar:\
${MCDIR}/lib/commons-io-2.4.jar:\
${MCDIR}/lib/freemarker-2.3.18.jar:\
${MCDIR}/lib/log4j-1.2.17.jar:\
${MCDIR}/lib/opencsv-2.3.jar:\
${MCDIR}/lib/httpclient-4.2.5.jar:\
${MCDIR}/lib/httpcore-4.2.4.jar:\
${MCDIR}/lib/guava-18.0.jar:\
${MCDIR}/lib/commons-logging-1.1.1.jar:\
${MCDIR}/lib/gson-2.2.4.jar:\
${MCDIR}/lib/commons-lang3-3.1.jar:\
${MCDIR}/lib/gs-collections-api-7.0.0.jar:\
${MCDIR}/lib/gs-collections-7.0.0.jar \
org.molgenis.compute.ComputeCommandLine \
$*
