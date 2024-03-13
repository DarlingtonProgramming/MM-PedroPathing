package com.noahbres.meepmeep.pedropathing

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.core.colorscheme.ColorScheme
import com.noahbres.meepmeep.pedropathing.entity.PedroPathingBotEntity
import com.noahbres.meepmeep.pedropathing.lib.pathgeneration.PathChain

class DefaultBotBuilder(private val meepMeep: MeepMeep) {
    private var constraints = FollowerConstraints(
        70.0, -53.6, -23.5, -61.5, 3.0
    )

    private var width = 18.0
    private var height = 18.0

    private var startPose = Pose2d(0.0, 0.0, 0.0)
    private var colorScheme: ColorScheme? = null
    private var opacity = 0.8

    fun setDimensions(width: Double, height: Double): DefaultBotBuilder {
        this.width = width
        this.height = height

        return this
    }

    fun setStartPose(pose: Pose2d): DefaultBotBuilder {
        this.startPose = pose

        return this
    }

    fun setConstraints(constraints: FollowerConstraints): DefaultBotBuilder {
        this.constraints = constraints

        return this
    }

    fun setConstraints(
        xMovement: Double,
        yMovement: Double,
        forwardZeroPowerAcceleration: Double,
        lateralZeroPowerAcceleration: Double,
        zeroPowerAccelerationMultiplier: Double
    ): DefaultBotBuilder {
        constraints = FollowerConstraints(xMovement, yMovement, forwardZeroPowerAcceleration, lateralZeroPowerAcceleration, zeroPowerAccelerationMultiplier)

        return this
    }

    fun setColorScheme(scheme: ColorScheme): DefaultBotBuilder {
        this.colorScheme = scheme

        return this
    }

    fun build(): PedroPathingBotEntity {
        return PedroPathingBotEntity(
            meepMeep,
            constraints,
            width, height,
            startPose, colorScheme ?: meepMeep.colorManager.theme, opacity,
            false
        )
    }

    fun followPath(pathChain: PathChain): PedroPathingBotEntity {
        val bot = this.build()
        bot.followPath(pathChain)

        return bot
    }
}