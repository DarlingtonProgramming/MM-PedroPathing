package com.noahbres.meepmeep.pedropathing.entity

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.core.colorscheme.ColorScheme
import com.noahbres.meepmeep.core.entity.ThemedEntity
import com.noahbres.meepmeep.core.toScreenCoord
import com.noahbres.meepmeep.core.util.FieldUtil
import com.noahbres.meepmeep.pedropathing.lib.pathgeneration.Path
import com.noahbres.meepmeep.pedropathing.lib.pathgeneration.PathCallback
import com.noahbres.meepmeep.pedropathing.lib.pathgeneration.PathChain
import com.noahbres.meepmeep.roadrunner.entity.MarkerIndicatorEntity
import java.awt.*
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

class PathEntity(
    override val meepMeep: MeepMeep,
    private val pathChain: PathChain,
    private var colorScheme: ColorScheme
) : ThemedEntity {
    companion object {
        const val PATH_INNER_STROKE_WIDTH = 0.5
        const val PATH_OUTER_STROKE_WIDTH = 2.0

        const val PATH_OUTER_OPACITY = 0.4

        const val PATH_UNFOCUSED_OPACTIY = 0.3

        const val SAMPLE_RESOLUTION = 2.0
    }

    private var canvasWidth = FieldUtil.CANVAS_WIDTH
    private var canvasHeight = FieldUtil.CANVAS_HEIGHT

    override val tag = "PATH_ENTITY"

    override var zIndex: Int = 0

    val markerEntityList = mutableListOf<MarkerIndicatorEntity>()

    private lateinit var baseBufferedImage: BufferedImage

    private var currentPathImage: BufferedImage? = null
    private var lastPath: Path? = null
    private var currentPath: Path? = pathChain.getPath(0)

    var pathProgress: Double? = null

    init {
        redrawPath()
    }

    private fun redrawPath() {
        markerEntityList.forEach {
            meepMeep.requestToRemoveEntity(it)
        }
        markerEntityList.clear()

        val environment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val device = environment.defaultScreenDevice
        val config = device.defaultConfiguration

        baseBufferedImage = config.createCompatibleImage(
            canvasWidth.toInt(), canvasHeight.toInt(), Transparency.TRANSLUCENT
        )
        val gfx = baseBufferedImage.createGraphics()

        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        gfx.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

        val drawnPath = Path2D.Double()

        val innerStroke = BasicStroke(
            FieldUtil.scaleInchesToPixel(PATH_INNER_STROKE_WIDTH).toFloat(),
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND
        )

        val currentEndPoint = currentPath?.firstControlPoint
        val currentEndPose = currentEndPoint?.let { currentPath?.let { it1 -> Pose2d(it.x, it.y, it1.getHeadingGoal(0.0)) } }

        val firstVec = currentEndPose?.vec()?.toScreenCoord()
        if (firstVec != null) {
            drawnPath.moveTo(firstVec.x, firstVec.y)
        }

        for (i in 0 until pathChain.size()) {
            when (val path = pathChain.getPath(i)) {
                is Path -> {
                    val displacementSamples = (path.length() / SAMPLE_RESOLUTION).roundToInt()

                    val dispTVals = (0..displacementSamples).map {
                        it / displacementSamples.toDouble()
                    }

                    val poses: List<Pose2d> = dispTVals.map {
                        val point = path.getPoint(it)
                        Pose2d(point.x, point.y, path.getHeadingGoal(it))
                    }

                    for (pose in poses.drop(1)) {
                        val coord = pose.vec().toScreenCoord()
                        drawnPath.lineTo(coord.x, coord.y)
                    }
                }
            }
        }

        for (i in 0 until pathChain.callbacks.size) {
            when (val callback = pathChain.callbacks[i]) {
                is PathCallback -> {
                    var pose: Pose2d
                    if (callback.type == 1) { // parametric
                        val path = pathChain.getPath(callback.index)
                        val point = path.getPoint(callback.startCondition)
                        pose = Pose2d(point.x, point.y, path.getHeadingGoal(callback.startCondition))

                        val markerEntity = MarkerIndicatorEntity(meepMeep, colorScheme, pose, callback)
                        markerEntityList.add(markerEntity)
                        meepMeep.requestToAddEntity(markerEntity)
                    } else {
                        // time
                    }
                }
            }
        }

        gfx.stroke = innerStroke
        gfx.color = colorScheme.TRAJCETORY_PATH_COLOR
        gfx.color = Color(
            colorScheme.TRAJCETORY_PATH_COLOR.red, colorScheme.TRAJCETORY_PATH_COLOR.green,
            colorScheme.TRAJCETORY_PATH_COLOR.blue, (PATH_UNFOCUSED_OPACTIY * 255).toInt()

        )
        gfx.draw(drawnPath)
    }

    private fun redrawCurrentPath() {
        if (currentPath == null) {
            currentPathImage = null
            return
        }

        val environment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val device = environment.defaultScreenDevice
        val config = device.defaultConfiguration

        currentPathImage = config.createCompatibleImage(
            canvasWidth.toInt(), canvasHeight.toInt(), Transparency.TRANSLUCENT
        )
        val gfx = currentPathImage!!.createGraphics()

        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        gfx.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

        val drawnPath = Path2D.Double()

        val outerStroke = BasicStroke(
            FieldUtil.scaleInchesToPixel(PATH_OUTER_STROKE_WIDTH).toFloat(),
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND
        )
        val innerStroke = BasicStroke(
            FieldUtil.scaleInchesToPixel(PATH_INNER_STROKE_WIDTH).toFloat(),
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND
        )

        val firstVec = Vector2d(currentPath!!.firstControlPoint.x, currentPath!!.firstControlPoint.y).toScreenCoord()
        drawnPath.moveTo(firstVec.x, firstVec.y)

        val displacementSamples = (currentPath!!.length() / SAMPLE_RESOLUTION).roundToInt()

        val dispTVals = (0..displacementSamples).map {
            it / displacementSamples.toDouble()
        }

        val poses: List<Pose2d> = dispTVals.map {
            val point = currentPath!!.getPoint(it)
            Pose2d(point.x, point.y, currentPath!!.getHeadingGoal(it))
        }

        for (pose in poses.drop(1)) {
            val coord = pose.vec().toScreenCoord()
            drawnPath.lineTo(coord.x, coord.y)
        }

        gfx.stroke = outerStroke
        gfx.color = Color(
            colorScheme.TRAJCETORY_PATH_COLOR.red, colorScheme.TRAJCETORY_PATH_COLOR.green,
            colorScheme.TRAJCETORY_PATH_COLOR.blue, (PATH_OUTER_OPACITY * 255).toInt()
        )
        gfx.draw(drawnPath)

        gfx.stroke = innerStroke
        gfx.color = colorScheme.TRAJCETORY_PATH_COLOR
        gfx.draw(drawnPath)
    }

    override fun update(deltaTime: Long) {
        if (pathProgress == null) {
            currentPath = null
        } else {
            var currentDisplacement = 0.0

            for (i in 0 until pathChain.size()) {
                val path = pathChain.getPath(i)

                if (currentDisplacement + path.length() > pathProgress!!) {
                    currentPath = path
                    break
                } else {
                    currentDisplacement += path.length()
                }
            }
        }

        if (lastPath != currentPath) {
            redrawCurrentPath()
        }

        lastPath = currentPath
    }

    override fun render(gfx: Graphics2D, canvasWidth: Int, canvasHeight: Int) {
        gfx.drawImage(baseBufferedImage, null, 0, 0)

        if (currentPathImage != null) gfx.drawImage(currentPathImage, null, 0, 0)
    }

    override fun setCanvasDimensions(canvasWidth: Double, canvasHeight: Double) {
        if (this.canvasWidth != canvasWidth || this.canvasHeight != canvasHeight) redrawPath()
        this.canvasWidth = canvasWidth
        this.canvasHeight = canvasHeight
    }

    override fun switchScheme(scheme: ColorScheme) {
        if (this.colorScheme != scheme) {
            this.colorScheme = scheme
            redrawPath()
        }
    }
}