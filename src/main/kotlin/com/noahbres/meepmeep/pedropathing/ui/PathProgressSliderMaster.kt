package com.noahbres.meepmeep.pedropathing.ui

import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.pedropathing.entity.PedroPathingBotEntity
import java.awt.event.ActionEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import javax.swing.AbstractAction
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.KeyStroke
import kotlin.math.max
import kotlin.math.min

class PathProgressSliderMaster(
    private val meepMeep: MeepMeep,
    private val sliderWidth: Int,
    private val sliderHeight: Int
) : JPanel(), MouseMotionListener, MouseListener {
    private val botList = mutableListOf<Pair<PedroPathingBotEntity, PathProgressSubSlider>>()

    private var maxPathDisplacement = 0.0
    private var maxPathIndex = 0

    private var internalIsPaused = false
    private var wasPausedBeforeMouseDown = false

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        addMouseListener(this)
        addMouseMotionListener(this)

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SPACE"), "space_pressed")

        actionMap.put("space_pressed", object : AbstractAction() {
            override fun actionPerformed(p0: ActionEvent?) {
                internalIsPaused = !internalIsPaused

                for ((bot, _) in botList) {
                    if (internalIsPaused)
                        bot.pause()
                    else
                        bot.unpause()
                }
            }
        })
    }

    fun addPedroPathingBot(bot: PedroPathingBotEntity) {
        if (botList.indexOfFirst { it.first == bot } != -1) throw Exception("PedroPathingBotEntity instance has already been added")

        val currPathLength = bot.currentPathChain?.length() ?: 0.0
        if (currPathLength >= maxPathDisplacement) {
            maxPathDisplacement = currPathLength
            maxPathIndex = botList.size
        }

        maxPathDisplacement = max(bot.currentPathChain?.length() ?: 0.0, maxPathDisplacement)
        for ((_, slider) in botList) {
            slider.maxPathDisplacement = maxPathDisplacement
        }

        if (botList.isEmpty())
            internalIsPaused = bot.pathPaused
        else {
            if (internalIsPaused) bot.pause() else bot.unpause()
            botList[0].first.looping = false
            bot.looping = false
        }

        val subSlider = PathProgressSubSlider(
            bot,
            maxPathDisplacement,
            sliderWidth,
            sliderHeight,
            bot.colorScheme.TRAJECTORY_SLIDER_FG,
            bot.colorScheme.TRAJECTORY_SLIDER_BG,
            bot.colorScheme.TRAJECTORY_TEXT_COLOR,
            MeepMeep.FONT_CMU_BOLD
        )

        bot.setTrajectoryProgressSliderMaster(this, botList.size)

        botList.add(Pair(bot, subSlider))
        add(subSlider)

        meepMeep.windowFrame.pack()
    }

    fun removePedroPathingBot(bot: PedroPathingBotEntity) {
        val indexOfBot = botList.indexOfFirst { it.first == bot }
        if (indexOfBot != -1) {
            remove(botList[indexOfBot].second)

            botList.removeAt(indexOfBot)
            meepMeep.windowFrame.pack()
        } else {
            throw Exception("PedroPathingBotEntity instance not found")
        }
    }

    fun reportDone(index: Int) {
        if (index == maxPathIndex) {
            for ((bot, _) in botList) {
                bot.start()
            }
        }
    }

    fun reportProgress(index: Int, dispTraveled: Double) {
        if (index != -1) {
            botList[index].second.progress = dispTraveled / maxPathDisplacement
        }
    }

    override fun mouseReleased(me: MouseEvent?) {
        for ((bot, slider) in botList) {
            if (!wasPausedBeforeMouseDown) bot.unpause()

            bot.resume()

            slider.redraw()
        }
    }

    override fun mousePressed(me: MouseEvent?) {
        wasPausedBeforeMouseDown = internalIsPaused

        val clippedInputPercentage = min(max(me!!.x.toDouble() / width.toDouble(), 0.0), 1.0)
        val clippedInputTime = clippedInputPercentage * maxPathDisplacement

        for ((bot, slider) in botList) {
            bot.pause()

            bot.setPathProgressDisplacement(min(clippedInputTime, bot.currentPathChain?.length() ?: 0.0))

            slider.redraw()
        }
    }

    override fun mouseDragged(me: MouseEvent?) {
        val clippedInputPercentage = min(max(me!!.x.toDouble() / width.toDouble(), 0.0), 1.0)
        val clippedInputTime = clippedInputPercentage * maxPathDisplacement

        for ((bot, slider) in botList) {
            bot.setPathProgressDisplacement(min(clippedInputTime, bot.currentPathChain?.length() ?: 0.0))

            slider.redraw()
        }
    }

    override fun mouseMoved(me: MouseEvent?) {}

    override fun mouseClicked(me: MouseEvent?) {}

    override fun mouseEntered(me: MouseEvent?) {}

    override fun mouseExited(me: MouseEvent?) {}
}