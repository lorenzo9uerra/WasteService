package it.unibo.lenziguerra.wasteservice.utils

object ApplData {
    /*
	 * MESSAGGI in cril
	*/
    private fun crilCmd(move: String, time: Int): String {
        //ColorsOut.out( "ClientNaiveUsingPost |  buildCrilCmd:" + crilCmd );
        return "{\"robotmove\":\"$move\" , \"time\": $time}"
    }

    fun moveForward(duration: Int): String {
        return crilCmd("moveForward", duration)
    }

    fun turnLeft(duration: Int): String {
        return crilCmd("turnLeft", duration)
    }

    fun turnRight(duration: Int): String {
        return crilCmd("turnRight", duration)
    }

    fun moveBackward(duration: Int): String {
        return crilCmd("moveBackward", duration)
    }
}