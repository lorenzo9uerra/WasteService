package it.unibo.lenziguerra.wasteservice

import it.unibo.kactor.ActorBasic
import it.unibo.kactor.IApplMessage
import it.unibo.kactor.MsgUtil
import it.unibo.lenziguerra.wasteservice.utils.LogUtils
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils
import unibo.comm22.utils.ColorsOut
import java.util.*

class PathExecDummyImmediate(name: String) : ActorBasic(name) {
    override suspend fun actorBody(msg: IApplMessage) {
        if (msg.msgId() == "dopath") {
            // answer doesn't work?
            val reply = MsgUtil.buildReply(name, "dopathdone", "dopathdone(ok)", msg.msgSender())
            sendMessageToActor(reply, msg.msgSender())
        }
    }
}

class PathExecDummyVerifyPath(name: String) : ActorBasic(name) {
    var robotPosition = arrayOf(0, 0)
    var robotDir = "down"

    companion object {
        val dirToOffset = mapOf(
            "left" to arrayOf(-1, 0),
            "up" to arrayOf(0, -1),
            "right" to arrayOf(1, 0),
            "down" to arrayOf(0, 1),
        )
        val dirToRightRot = mapOf(
            "left" to "up",
            "up" to "right",
            "right" to "down",
            "down" to "left",
        )
        val dirToLeftRot = mapOf(
            "left" to "down",
            "up" to "left",
            "right" to "up",
            "down" to "right",
        )
    }

    override suspend fun actorBody(msg: IApplMessage) {
        if (msg.msgId() == "dopath") {
            val path = PrologUtils.extractPayload(msg.msgContent())[0]!!
            val validPathPattern = Regex("[wlr]+")
            val reply = if (!validPathPattern.matches(path)) {
                MsgUtil.buildReply(name, "dopathfail", "dopathfail(wrongpath)", msg.msgSender())
            } else {
                applyPath(path)
                MsgUtil.buildReply(name, "dopathdone", "dopathdone(ok)", msg.msgSender())
            }

            sendMessageToActor(reply, msg.msgSender())
        }
    }

    fun applyPath(path: String) {
        var currentPath = path
        while (currentPath.length > 0) {
            val currentStep = currentPath.first()
            currentPath = currentPath.substring(1)

            when (currentStep) {
                'w' -> robotPosition = arrayOf(
                    robotPosition[0] + dirToOffset[robotDir]!![0],
                    robotPosition[1] + dirToOffset[robotDir]!![1],
                )
                'r' -> robotDir = dirToRightRot[robotDir]!!
                'l' -> robotDir = dirToLeftRot[robotDir]!!
            }
        }

        LogUtils.threadOut("PathExecDummyVerifyPath $name | after path <$path> position is ${robotPosition.contentToString()}", ColorsOut.MAGENTA)
    }

    fun resetPosition() {
        robotPosition = arrayOf(0, 0)
        robotDir = "down"
        LogUtils.threadOut("PathExecDummyVerifyPath $name | reset position", ColorsOut.MAGENTA)
    }
}