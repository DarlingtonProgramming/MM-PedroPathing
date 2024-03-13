package com.noahbres.meepmeep.pedropathing

data class FollowerConstraints(
    val xMovement: Double, val yMovement: Double,
    val forwardZeroPowerAcceleration: Double, val lateralZeroPowerAcceleration: Double,
    val zeroPowerAccelerationMultiplier: Double
)
