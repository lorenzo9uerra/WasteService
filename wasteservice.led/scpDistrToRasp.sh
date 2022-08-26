#!/bin/sh

cd build/distributions/
tar -xf wasteservice.led-1.0.tar
rsync -avh wasteservice.led-1.0 raspi:
rm -rf wasteservice.led-1.0/
cd ../..
rsync -avh distresources/*.sh raspi:wasteservice.led-1.0/bin/bash/
rsync -avh distresources/LedConfiguration.real.json raspi:wasteservice.led-1.0/bin/LedConfiguration.json
rsync -avh distresources/SystemConfig.json raspi:wasteservice.led-1.0/bin/

