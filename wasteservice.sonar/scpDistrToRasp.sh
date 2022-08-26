#!/bin/sh

cd build/distributions/
tar -xf wasteservice.sonar-1.0.tar
rsync -avh wasteservice.sonar-1.0 raspi:
rm -rf wasteservice.sonar-1.0/
cd ../..
rsync -avh distresources/SonarAlone.c raspi:wasteservice.sonar-1.0/device-helpers/c/
rsync -avh distresources/SonarAlone raspi:wasteservice.sonar-1.0/device-helpers/c/
rsync -avh distresources/SonarConfig.json raspi:wasteservice.sonar-1.0/bin/
rsync -avh distresources/SystemConfig.json raspi:wasteservice.sonar-1.0/bin/
rsync -avh CommSystemConfig.json raspi:wasteservice.sonar-1.0/bin/
rsync -avh *.pl raspi:wasteservice.sonar-1.0/bin/

