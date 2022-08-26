package it.unibo.lenziguerra.wasteservice.sonar

import it.unibo.kactor.MsgUtil
import it.unibo.lenziguerra.wasteservice.SystemConfig
import unibo.comm22.interfaces.Interaction2021
import unibo.comm22.tcp.TcpClientSupport
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.time.Duration
import java.time.LocalTime
import javax.swing.*


fun main() {
    SystemConfig.setConfiguration()
    SonarGui.createGui()
}

object SonarGui {
    val button = JButton("Stop")
    lateinit var tcpConn : Interaction2021
    lateinit var tcpConnTime: LocalTime

    fun createGui() {
        val frame = JFrame("Sonar Stop Test GUI")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(500, 500)

        val fontName = getFontWithPreference("Stop", "Roboto")
        System.out.println("Using font $fontName")

        button.font = Font(fontName, Font.BOLD, 100)
        button.addMouseListener(object : MouseListener {
            override fun mousePressed(e: MouseEvent?) {
                onButtonPress()
            }

            override fun mouseReleased(e: MouseEvent?) {
                onButtonRelease()
            }

            override fun mouseEntered(p0: MouseEvent?) {}
            override fun mouseExited(p0: MouseEvent?) {}
            override fun mouseClicked(p0: MouseEvent?) {}
        })
        frame.contentPane.add(button) // Adds Button to content pane of frame

        frame.isVisible = true
    }

    private fun onButtonPress() {
        val distance = SystemConfig.DLIMIT - 5
        sendSonarEvent(distance)
    }

    private fun onButtonRelease() {
        val distance = SystemConfig.DLIMIT + 5
        sendSonarEvent(distance)
    }

    private fun sendSonarEvent(distance: Int) {
        checkConnect()
        val event = MsgUtil.buildEvent("sonarGui", "sonarDistance", "sonarDistance($distance)")
        tcpConn.forward(event.toString())
    }

    private fun checkConnect() {
        if (!this::tcpConn.isInitialized
            || Duration.between(tcpConnTime, LocalTime.now()) > Duration.ofSeconds(10)
        ) {
            tcpConn = TcpClientSupport.connect(
                SystemConfig.hosts["sonarinterrupter"]!!,
                SystemConfig.ports["sonarinterrupter"]!!,
                5
            )
            tcpConnTime = LocalTime.now()
        }
    }

    private fun getFontWithPreference(forText: String, vararg preferredFonts: String): String {
        val fallbackFont = "Arial"
        val available = GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames
        return preferredFonts.firstOrNull { it in available } ?: fallbackFont
    }
}