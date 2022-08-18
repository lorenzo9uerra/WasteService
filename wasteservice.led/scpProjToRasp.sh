#!/bin/sh

rsync -avh . raspi:wasteservice/wasteservice.led/ --delete --exclude build --exclude bin
rsync -avh ../wasteservice.shared/* raspi:wasteservice/wasteservice.shared/ --delete --exclude build --exclude bin
