# MeepMeep (PedroPathing)

Fork of [MeepMeep](https://github.com/NoahBres/MeepMeep) that supports [PedroPathing](https://github.com/brotherhobo/10158-Centerstage), developed by 10158 Scott's Bots.

*P.S. very unstable*, deprecated.

<img src="/images/readme/screen-recording.gif" width="500" height="500"/>

# Table of Contents

- [Installing (Android Studio)](#installing-android-studio)
- [Misc](#misc)
    - [Poor Performance?](#poor-performance)
    - [Adding a second bot](#adding-a-second-bot)

# Installing (Android Studio)
1.  In Android Studio, click on the "FtcRobotController" Module, then right click on the FtcRobotController folder and click `New > Module`
    <img src="/images/readme/installationStep1.png" width="751" height="287"/>
2.  On the left part of this window, select "Java or Kotlin Library"
    <img src="/images/readme/installationStep2.png" width="544" height="382"/>

3.  From here, remove the `:ftcrobotcontroller:lib` in the "Library Name" section, and rename it to `MeepMeepTesting`. You may use whatever name you wish but the rest of the instructions will assume you have chosen the name `MeepMeepTesting`. Ensure that you also change the "class name" section to match.

4.  Hit "Finish" at the bottom right of the Module Create window.

5.  Open up the `build.gradle` file for the MeepMeepTesting module (or whatever you chose to name it prior). In this file, change all instances `JavaVersion.VERSION_1_7` to `JavaVersion.VERSION_1_8`
    <img src="/images/readme/installationStep5.png" width="566" height="274"/>

6.  At the bottom of the file add the following gradle snippet:

        repositories {
            maven { url = 'https://jitpack.io' }
            maven { url = 'https://maven.brott.dev/' }
        }

        dependencies {
            implementation 'com.github.DarlingtonProgramming:MM-PedroPathing:0.0.1'
        }

7.  When Android Studio prompts you to make a gradle sync, click "Sync Now".
    <img src="/images/readme/installationStep7.png" width="644" height="20"/>

8.  Create a class for your MeepMeepTesting java module if it does not yet exist. Paste the following sample in it. Feel free to change this later.

```java
package com.example.meepmeeptesting;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.noahbres.meepmeep.MeepMeep;
import com.noahbres.meepmeep.pedropathing.DefaultBotBuilder;
import com.noahbres.meepmeep.pedropathing.entity.PedroPathingBotEntity;
import com.noahbres.meepmeep.pedropathing.lib.pathgeneration.*;

public class MeepMeepTesting {
    public static Pose2d startingPose = new Pose2d(15.5, 61.75, Math.toRadians(270));
    public static PathChain middleSpikeMark;

    public static void main(String[] args) {
        MeepMeep meepMeep = new MeepMeep(700);

        PedroPathingBotEntity robot = new DefaultBotBuilder(meepMeep)
                // Set this to the length and width of your robot
                .setDimensions(15, 17.5)
                // Set this based on your follower constants for PedroPathing
                // (xMovement, yMovement, forwardZeroPowerAcceleration, lateralZeroPowerAcceleration, zeroPowerAccelerationMultiplier)
                .setConstraints(70, -50, -25, -60, 3)
                .build();

        middleSpikeMark = robot.getDrive().pathBuilder()
                .addPath(new Path(new BezierLine(new Point(startingPose), new Point(12, 37.5, Point.CARTESIAN))))
                .setLinearHeadingInterpolation(startingPose.getHeading(), Math.toRadians(220))
                .addPath(new Path(new BezierLine(new Point(12, 37.5, Point.CARTESIAN), new Point(52.5, 27, Point.CARTESIAN))))
                .setLinearHeadingInterpolation(Math.toRadians(220), Math.PI, 0.7)
                .addPath(new BezierLine(
                        new Point(52.5, 27, Point.CARTESIAN),
                        new Point(30, 10.5, Point.CARTESIAN)
                ))
                .addParametricCallback(0.5, () -> {})
                .setConstantHeadingInterpolation(Math.PI)
                .addPath(new BezierLine(
                        new Point(30, 10.5, Point.CARTESIAN),
                        new Point(-65, 11.5, Point.CARTESIAN)
                ))
                .setConstantHeadingInterpolation(Math.PI)
                .addPath(new BezierCurve(
                        new Point(-65, 11.5, Point.CARTESIAN),
                        new Point(-40, 10.5, Point.CARTESIAN),
                        new Point(37, 10.5, Point.CARTESIAN),
                        new Point(52.2, 28, Point.CARTESIAN)
                ))
                .setConstantHeadingInterpolation(Math.PI)
                .addPath(new BezierLine(
                        new Point(52.2, 28, Point.CARTESIAN),
                        new Point(30, 10.5, Point.CARTESIAN)
                ))
                .setConstantHeadingInterpolation(Math.PI)
                .addPath(new BezierLine(
                        new Point(30, 10.5, Point.CARTESIAN),
                        new Point(-65, 11.5, Point.CARTESIAN)
                ))
                .setConstantHeadingInterpolation(Math.PI)
                .addPath(new BezierCurve(
                        new Point(-65, 11.5, Point.CARTESIAN),
                        new Point(-40, 10.5, Point.CARTESIAN),
                        new Point(37, 10.5, Point.CARTESIAN),
                        new Point(52.2, 28, Point.CARTESIAN)
                ))
                .setConstantHeadingInterpolation(Math.PI)
                .addPath(new BezierLine(
                        new Point(52.2, 28, Point.CARTESIAN),
                        new Point(30, 10.5, Point.CARTESIAN)
                ))
                .setConstantHeadingInterpolation(Math.PI)
                .addPath(new BezierLine(
                        new Point(30, 10.5, Point.CARTESIAN),
                        new Point(-65, 11.5, Point.CARTESIAN)
                ))
                .setConstantHeadingInterpolation(Math.PI)
                .addParametricCallback(0.8, () -> {
                })
                .addPath(new BezierCurve(
                        new Point(-65, 11.5, Point.CARTESIAN),
                        new Point(-40, 10.5, Point.CARTESIAN),
                        new Point(37, 10.5, Point.CARTESIAN),
                        new Point(52.2, 28, Point.CARTESIAN)
                ))
                .setConstantHeadingInterpolation(Math.PI)
                .addPath(new Path(new BezierLine(new Point(52.2, 28, Point.CARTESIAN), new Point(47, 35, Point.CARTESIAN))))
                .setConstantHeadingInterpolation(Math.PI)
                .build();

        robot.followPath(middleSpikeMark);

        meepMeep.setBackground(MeepMeep.Background.FIELD_CENTERSTAGE_JUICE_DARK)
                .setDarkMode(true)
                .setBackgroundAlpha(0.95f)
                .addEntity(robot)
                .start();
    }
}
```

9. Create a run configuration for Android Studio.
    1. First, click on the drop down menu on the top bar of Android Studio, where it says "TeamCode" with a little Android logo next to it.
    2. Click `Edit Configurations`
    3. Click on the "+" symbol in the top left of the window, and when it prompts you, select "Application".
    4. Change the name to your liking (ex. meepmeep-run)
    5. Where it says "module not specified", click to open the dropdown, then select your JRE.
    6. Where it says "cp <no module>" click it to open the dropdown, and then select FtcRobotController.MeepMeepTesting.main
    7. Where it says "Main Class", click the little "file" icon to the right of the text and then select the name of the main class for your MeepMeepTesting module.
    8. From here, in the bottom right of the window, press "Apply" then "Ok".
    9. It will now automatically switch to that Run/Debug Configuration profile.
10. If at any point you would like to build code onto your Control Hub or Phone, then click the Run/Debug configuration profile at the top to open the dropdown menu and select TeamCode. Perform the same steps to switch back to MeepMeepRun.

# Misc

### Poor Performance?

On some systems, hardware acceleration may not be enabled by default.
To enable hardware acceleration use the cli flag: `-Dsun.java2d.opengl=true`.

Or, enable it _before_ initializing your `MeepMeep` instance with the following snippet:
`System.setProperty("sun.java2d.opengl", "true");`

### Adding a second bot:

Declare a new `PedroPathingBotEntity` and add it via `MeepMeep#addEntity(Entity)`.
