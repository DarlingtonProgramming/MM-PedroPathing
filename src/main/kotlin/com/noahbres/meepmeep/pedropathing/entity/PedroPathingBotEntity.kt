package com.noahbres.meepmeep.pedropathing.entity

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.core.colorscheme.ColorScheme
import com.noahbres.meepmeep.core.entity.BotEntity
import com.noahbres.meepmeep.core.entity.EntityEventListener
import com.noahbres.meepmeep.core.exhaustive
import com.noahbres.meepmeep.pedropathing.DriveShim
import com.noahbres.meepmeep.pedropathing.FollowerConstraints
import com.noahbres.meepmeep.pedropathing.lib.pathgeneration.MathFunctions
import com.noahbres.meepmeep.pedropathing.lib.pathgeneration.Path
import com.noahbres.meepmeep.pedropathing.lib.pathgeneration.PathChain
import com.noahbres.meepmeep.pedropathing.lib.pathgeneration.Vector
import com.noahbres.meepmeep.pedropathing.ui.PathProgressSliderMaster
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sqrt


class PedroPathingBotEntity(
    meepMeep: MeepMeep,
    private var constraints: FollowerConstraints,

    width: Double, height: Double,
    pose: Pose2d,

    val colorScheme: ColorScheme,
    opacity: Double,

    var listenToSwitchThemeRequest: Boolean = false
) : BotEntity(meepMeep, width, height, pose, colorScheme, opacity), EntityEventListener {
    companion object {
        const val SKIP_LOOPS = 5
    }

    override val tag = "PP_BOT_ENTITY"

    override var zIndex: Int = 0

    var drive = DriveShim(constraints, pose)

    var currentPathChain: PathChain? = null
    var currentPath: Path? = null
    var closestPose: Pose2d? = null;

    private var pathEntity: PathEntity? = null

    var looping = true
    private var running = false

    var pathPaused = false

    private var skippedLoops = 0

    private var displacementTraveled = 0.0
        set(value) {
            pathEntity?.pathProgress = value
            field = value
        }

    private var sliderMaster: PathProgressSliderMaster? = null
    private var sliderMasterIndex: Int? = null

    override fun update(deltaTime: Long) {
        if (!running) return

        if (skippedLoops++ < SKIP_LOOPS) return

        if (!pathPaused) {
            closestPose = currentPath?.getClosestPoint(drive.poseEstimate, 10)
//            val xvel = drive.constraints.xMovement * (deltaTime / 1e9)
//            val yvel = drive.constraints.yMovement * (deltaTime / 1e9)
//            displacementTraveled += MathFunctions.distance(drive.poseEstimate, closestPose)
            val vel = getDriveVelocityError()
            displacementTraveled += vel * (deltaTime / Math.pow(10.0, 9.0))
        }

        when {
            currentPathChain != null && !currentPathChain!!.isAtParametricEnd(displacementTraveled) -> {
                var remainingDisplacement = displacementTraveled

                for (i in 0 until currentPathChain!!.size()) {
                    val path = currentPathChain!!.getPath(i)

                    if (remainingDisplacement <= path.length()) {
                        val tValue = remainingDisplacement / path.length()
                        pose = path.getPoint(tValue)?.let { Pose2d(it.x, it.y, path.getHeadingGoal(tValue)) } ?: pose
                        drive.update(pose)
                        currentPath = path
                        pathEntity!!.markerEntityList.forEach {
                            if (currentPath != null && currentPathChain!!.getIndex(currentPath) == it.callback.index && currentPath!!.closestPointTValue >= it.callback.startCondition)
                                it.passed()
                        }
                        sliderMaster?.reportProgress(sliderMasterIndex ?: -1, displacementTraveled)
                        return
                    } else {
                        remainingDisplacement -= path.length()
                    }
                }
            }

            looping -> {
                pathEntity!!.markerEntityList.forEach {
                    it.reset()
                }
                displacementTraveled = 0.0
                sliderMaster?.reportDone(sliderMasterIndex ?: -1)
            } else -> {
                displacementTraveled = 0.0
                running = false
                sliderMaster?.reportDone(sliderMasterIndex ?: -1)
            }
        }.exhaustive
    }

    private fun getDriveVelocityError(): Double {
        if (currentPath == null) {
            return 0.0
        }
        val distanceToGoal: Double = if (!currentPath!!.isAtParametricEnd()) {
            currentPath!!.length() * (1 - currentPath!!.closestPointTValue)
        } else {
            val offset = Vector()
            offset.setOrthogonalComponents(
                drive.poseEstimate.x - currentPath!!.lastControlPoint.x,
                drive.poseEstimate.y - currentPath!!.lastControlPoint.y
            )
            MathFunctions.dotProduct(currentPath!!.endTangent, offset)
        }
        val distanceToGoalVector: Vector = MathFunctions.scalarMultiplyVector(
            MathFunctions.normalizeVector(currentPath!!.closestPointTangentVector),
            distanceToGoal
        )
        val velocity = Vector(
            MathFunctions.dotProduct(
                getVelocity(),
                MathFunctions.normalizeVector(currentPath!!.closestPointTangentVector)
            ), currentPath!!.closestPointTangentVector.theta
        )
        val forwardHeadingVector = Vector(1.0, drive.poseEstimate.heading)
        val forwardVelocity = MathFunctions.dotProduct(forwardHeadingVector, velocity)
        val forwardDistanceToGoal = MathFunctions.dotProduct(forwardHeadingVector, distanceToGoalVector)
        val lateralHeadingVector = Vector(1.0, drive.poseEstimate.heading - Math.PI / 2)
        val lateralVelocity = MathFunctions.dotProduct(lateralHeadingVector, velocity)
        val lateralDistanceToGoal = MathFunctions.dotProduct(lateralHeadingVector, distanceToGoalVector)
        val forwardVelocityError = Vector(
            MathFunctions.getSign(forwardDistanceToGoal) * sqrt(Math.abs(-2 * drive.constraints.zeroPowerAccelerationMultiplier * drive.constraints.forwardZeroPowerAcceleration * forwardDistanceToGoal)) - forwardVelocity,
            forwardHeadingVector.theta
        )
        val lateralVelocityError = Vector(
            MathFunctions.getSign(lateralDistanceToGoal) * sqrt(Math.abs(-2 * drive.constraints.zeroPowerAccelerationMultiplier * drive.constraints.lateralZeroPowerAcceleration * lateralDistanceToGoal)) - lateralVelocity,
            lateralHeadingVector.theta
        )
        val velocityErrorVector: Vector = MathFunctions.addVectors(forwardVelocityError, lateralVelocityError)
        return MathFunctions.clamp(velocityErrorVector.magnitude, 0.0, hypot(drive.constraints.xMovement, drive.constraints.yMovement))
    }

    fun getVelocity(): Vector {
        val velocity = Vector()
        velocity.setOrthogonalComponents(drive.poseEstimate.x - drive.previousPose.x, drive.poseEstimate.y - drive.previousPose.y)
        velocity.magnitude = MathFunctions.distance(drive.poseEstimate, drive.previousPose) / ((drive.currentPoseTime - drive.previousPoseTime) / Math.pow(10.0, 9.0))
        return velocity
    }

    fun start() {
        running = true
    }

    fun resume() {
        running = true
    }

    fun pause() {
        pathPaused = true
    }

    fun unpause() {
        pathPaused = false
    }

    fun setPathProgressDisplacement(disp: Double) {
        if (currentPathChain != null)
            displacementTraveled = min(disp, currentPathChain!!.length())
    }

    fun followPath(path: PathChain) {
        currentPathChain = path
        currentPath = path.getPath(0)
        pathEntity = PathEntity(meepMeep, path, colorScheme)
    }

    fun setConstraints(constraints: FollowerConstraints) {
        this.constraints = constraints

        drive = DriveShim(constraints, pose)
    }

    override fun switchScheme(scheme: ColorScheme) {
        if (listenToSwitchThemeRequest)
            super.switchScheme(scheme)
    }

    fun setTrajectoryProgressSliderMaster(master: PathProgressSliderMaster, index: Int) {
        sliderMaster = master
        sliderMasterIndex = index
    }

    override fun onAddToEntityList() {
        if (pathEntity != null)
            meepMeep.requestToAddEntity(pathEntity!!)
    }

    override fun onRemoveFromEntityList() {
        if (pathEntity != null)
            meepMeep.requestToRemoveEntity(pathEntity!!)
    }
}