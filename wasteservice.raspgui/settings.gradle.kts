rootProject.name = "wasteservice.raspgui"

include(":wasteservice.shared")
project(":wasteservice.shared").projectDir = file("../wasteservice.shared")
include(":wasteservice.led")
project(":wasteservice.led").projectDir = file("../wasteservice.led")
include(":wasteservice.sonar")
project(":wasteservice.sonar").projectDir = file("../wasteservice.sonar")
