package com.noahbres.meepmeep.pedropathing

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.noahbres.meepmeep.pedropathing.lib.pathgeneration.PathBuilder


class DriveShim(private val _constraints: FollowerConstraints, var poseEstimate: Pose2d) {
    val constraints: FollowerConstraints = _constraints;
    var previousPose: Pose2d = poseEstimate;

    var previousPoseTime: Long = System.nanoTime()
    var currentPoseTime: Long = System.nanoTime()

    fun update(currentPose: Pose2d) {
        previousPose = poseEstimate
        previousPoseTime = currentPoseTime;
        currentPoseTime = System.nanoTime();
        poseEstimate = currentPose
    }

    fun pathBuilder(): PathBuilder {
        return PathBuilder();
    }
}