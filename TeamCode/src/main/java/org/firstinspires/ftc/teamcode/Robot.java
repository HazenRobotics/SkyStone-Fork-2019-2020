
package org.firstinspires.ftc.teamcode;

import android.app.SharedElementCallback;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.UltrasonicSensor;

import org.firstinspires.ftc.robotcontroller.external.samples.SensorMRRangeSensor;
import org.firstinspires.ftc.robotcontroller.internal.FtcRobotControllerActivity;
import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.android.AndroidGyroscope;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import java.util.List;
import java.util.ArrayList;

public class Robot
{
    //======================================================
    DcMotor backLeftWheel;
    DcMotor backRightWheel;

    //======================================================
    DcMotor lift;
    final double MAX_LIFT_SPEED = 0.8;

    //======================================================
    Servo leftHook;
    Servo rightHook;
    final double LEFT_HOOK_HOME = 0.75;
    final double RIGHT_HOOK_HOME = 0.2;
    final double LEFT_HOOK_EXTENDED = 0;
    final double RIGHT_HOOK_EXTENDED = 1;
    double leftHookPosition = LEFT_HOOK_HOME;
    double rightHookPosition =  RIGHT_HOOK_HOME;

    //======================================================
    Servo leftClapper;
    Servo rightClapper;
    final double LEFT_CLAPPER_HOME = 0.0;
    final double RIGHT_CLAPPER_HOME = 1.0;
    final double LEFT_CLAPPER_EXTENDED = 0.38;
    final double RIGHT_CLAPPER_EXTENDED = 0.61;
    double leftClapperPosition = LEFT_CLAPPER_HOME;
    double rightClapperPosition = RIGHT_CLAPPER_HOME;

    //======================================================
    GyroSensor gyro;

    //======================================================
    final int tickPerRevlolution = 1440;
    final double linearWheelDistance = (Math.PI) * 4;//.314961;
    final double linearSpoolDistance = (Math.PI) * 1.5748;

    //======================================================
    enum Position{none,left,right};

    //======================================================
    int shuffleCount = 1;

    HardwareMap hardwareMap;
    OpMode opMode;
    //LinearOpMode opMode;
    Telemetry telemetry;

    TensorFlow tensorFlow;

    //==============================================================================================   Robot method
    public Robot(HardwareMap hMap, OpMode opMode)
    {
        hardwareMap = hMap;
        this.opMode = opMode;
        //this.opMode = (LinearOpMode) opMode;
        telemetry = opMode.telemetry;

        //Map Hardware
        lift = hardwareMap.dcMotor.get("lift");

        backLeftWheel = hardwareMap.dcMotor.get("back_left_motor");
        backRightWheel = hardwareMap.dcMotor.get("back_right_motor");

        leftHook = hardwareMap.servo.get("left_hook");
        rightHook = hardwareMap.servo.get("right_hook");

        leftClapper = hardwareMap.servo.get("left_clapper");
        rightClapper = hardwareMap.servo.get("right_clapper");

        gyro = hardwareMap.gyroSensor.get("gyro");
        gyro.calibrate();

        tensorFlow = new TensorFlow(hardwareMap, opMode);

        tensorFlow.initVuforia();

        telemetry.addData("Robot", "finished setting up hardware");
        telemetry.update();
    }
    //==============================================================================================   hardwareChoose
    public void hardwareChoose(double hi)
    {

    }
    //==============================================================================================   Lift
    public void lift(double distance,double liftPower, boolean isRight)
    {
        telemetry.addData("lift", "running");
        telemetry.update();

        /*if (gamepad2.left_stick_y > 0)
        {
            lift.setPower(gamepad2.left_stick_y * MAX_LIFT_SPEED * 0.4);
            telemetry.addData("Lift", "down");
        }
        else if (gamepad2.left_stick_y < 0)
        {
            lift.setPower(gamepad2.left_stick_y * MAX_LIFT_SPEED);
            telemetry.addData("Lift", "up");
        }
        else
        {
            lift.setPower(0);
        }*/

        lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        if (isRight)
        {
            lift.setDirection(DcMotor.Direction.FORWARD);
        }
        else
        {
            lift.setDirection(DcMotor.Direction.REVERSE);
        }
        // set left motor to run to 5000 encoder counts.
        lift.setTargetPosition(convertDistTicks(5.5, linearSpoolDistance));

        // set both motors to 25% power. Movement will start.
        lift.setPower(liftPower);

        // set left motor to run to target encoder position and stop with brakes on.
        lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        while (lift.isBusy())
        {
            telemetry.addData("encoder-fwd", lift.getCurrentPosition()
                    + "  busy=" + lift.isBusy());
            telemetry.update();
        }

        // set motor power to zero to turn off motors. The motors stop on their own but
        // power is still applied so we turn off the power.
        lift.setPower(0.0);
    }
    //==============================================================================================   clapper
    public void clapper(boolean clappersHome)
    {
        telemetry.addData("clappers", "running");
        telemetry.update();

        //set clappers position to their positions
        if(clappersHome)
        {
            leftClapperPosition = LEFT_CLAPPER_HOME;
            rightClapperPosition = RIGHT_CLAPPER_HOME;
        }
        else
        {
            leftClapperPosition = LEFT_CLAPPER_EXTENDED;
            rightClapperPosition = RIGHT_CLAPPER_EXTENDED;
        }
        leftClapper.setPosition(leftClapperPosition);
        rightClapper.setPosition(rightClapperPosition);
    }
    //==============================================================================================   hooks
    public void hooks(boolean hooksHome)
    {
        telemetry.addData("hooks", "running");
        telemetry.update();

        //set hooks positions to positions
        if(hooksHome)
        {
            leftHookPosition = LEFT_HOOK_HOME;
            rightHookPosition = RIGHT_HOOK_HOME;
        }
        else
        {
            leftHookPosition = LEFT_HOOK_EXTENDED;
            rightHookPosition = RIGHT_HOOK_EXTENDED;
        }
        leftHook.setPosition(leftHookPosition);
        rightHook.setPosition(rightHookPosition);
    }
    //==============================================================================================   convertDistTicks
    //method takes in 2nd parameter for circumfrence of spinning object
    public int convertDistTicks(double distanceToTravel, double circumfrence)
    {
        //1440 revolutions = 1 rotation
        //1 rotation = 4

        double revolutions = distanceToTravel / circumfrence;
        int totalTicks = (int) Math.round(revolutions * tickPerRevlolution);

        return totalTicks;
    }
    //==============================================================================================   move
    public void move(double distanceToTravel,double power, boolean isForward)
    {
        telemetry.addData("move method", "running");
        telemetry.update();

        // reset encoder count kept by left motor.
        backLeftWheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backRightWheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        if (isForward)
        {
            backRightWheel.setDirection(DcMotor.Direction.REVERSE);
            backLeftWheel.setDirection(DcMotor.Direction.FORWARD);
        }
        else
        {
            backRightWheel.setDirection(DcMotor.Direction.FORWARD);
            backLeftWheel.setDirection(DcMotor.Direction.REVERSE);
        }
        // set left motor to run to 5000 encoder counts.
        backLeftWheel.setTargetPosition(convertDistTicks(distanceToTravel, linearWheelDistance));
        backRightWheel.setTargetPosition(convertDistTicks(distanceToTravel, linearWheelDistance));
        // set both motors to 25% power. Movement will start.
        backLeftWheel.setPower(power);
        backRightWheel.setPower(power);

        // set left motor to run to target encoder position and stop with brakes on.
        backLeftWheel.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        backRightWheel.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        // wait while opmode is active and motors are busy running to position.

        while (backLeftWheel.isBusy())
        {
            telemetry.addData("encoder-fwd", backLeftWheel.getCurrentPosition()
                    + "  busy=" + backLeftWheel.isBusy());
            telemetry.update();
        }
        while (backRightWheel.isBusy())
        {
            telemetry.addData("encoder-fwd", backRightWheel.getCurrentPosition()
                    + "  busy=" + backRightWheel.isBusy());
            telemetry.update();
        }

        // set motor power to zero to turn off motors. The motors stop on their own but
        // power is still applied so we turn off the power.
        backLeftWheel.setPower(0.0);
        backRightWheel.setPower(0.0);
    }
    //==============================================================================================   turnOnSpot
    public void turnOnSpot(double turningDegrees, double power, boolean turnLeft)
    {
        telemetry.addData("turnOnSpot", "running");
        telemetry.update();

        double turningNumber = (turningDegrees/180) * 16.4 * (Math.PI);
        double onSpotTurningNumber = turningNumber/2;

        // reset encoder count kept by left motor.
        backLeftWheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backRightWheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        //forward direction switching both wheels for turning in spot
        if(turnLeft)
        {
            backRightWheel.setDirection(DcMotor.Direction.FORWARD);
            backLeftWheel.setDirection(DcMotor.Direction.FORWARD);
        }
        else
        {
            backRightWheel.setDirection(DcMotor.Direction.REVERSE);
            backLeftWheel.setDirection(DcMotor.Direction.REVERSE);
        }
    }
    //==============================================================================================   turn
    public void turn(double turningDegrees, double power, boolean isForward, boolean leftWheel)
    {
        telemetry.addData("turn", "running");
        telemetry.update();

        // calculations from degrees to motor distance

        // 90* arc length = (radius/2) * pi
        // angle/180 * radius * pi
        // (angle/180) * 16.5 * (Math.PI)
        // (turningDegrees/180) * 16.5 * (Math.PI)

        double turningNumber = (turningDegrees/180) * 16.4 * (Math.PI);
        double onSpotTurningNumber = turningNumber/2;

        if (isForward)
        {
            backRightWheel.setDirection(DcMotor.Direction.REVERSE);
            backLeftWheel.setDirection(DcMotor.Direction.FORWARD);
        }
        else
        {
            backRightWheel.setDirection(DcMotor.Direction.FORWARD);
            backLeftWheel.setDirection(DcMotor.Direction.REVERSE);
        }

        //if wheel is left:
        if(leftWheel)
        {
            // reset encoder count kept by left motor.
            backLeftWheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

            // set left motor to run to 5000 encoder counts.
            backLeftWheel.setTargetPosition(convertDistTicks(turningNumber,linearWheelDistance));

            // set both motors to 25% power. Movement will start.
            backLeftWheel.setPower(power);

            // set left motor to run to target encoder position and stop with brakes on.
            backLeftWheel.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // wait while opmode is active and left motor is busy running to position.
            /*while (leftMotor.isBusy()) {
                telemetry.addData("encoder-fwd", leftMotor.getCurrentPosition()
                        + "  busy=" + leftMotor.isBusy());
                telemetry.update();
            }*/

            // set motor power to zero to turn off motors. The motors stop on their own but
            // power is still applied so we turn off the power.
            backLeftWheel.setPower(0.0);
        }

        //if wheel is right:
        else
        {
            // reset encoder count kept by left motor.
            backRightWheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

            // set left motor to run to 5000 encoder counts.
            backRightWheel.setTargetPosition(convertDistTicks(turningNumber,linearWheelDistance));

            // set both motors to 25% power. Movement will start.
            backRightWheel.setPower(power);

            // set left motor to run to target encoder position and stop with brakes on.
            backRightWheel.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // wait while opmode is active and left motor is busy running to position.
            /*while (rightMotor.isBusy())
            {
                telemetry.addData("encoder-fwd", rightMotor.getCurrentPosition()
                        + "  busy=" + rightMotor.isBusy());
                telemetry.update();
            }*/

            // set motor power to zero to turn off motors. The motors stop on their own but
            // power is still applied so we turn off the power.
            backLeftWheel.setPower(0.0);
            backRightWheel.setPower(0.0);
        }
    }
    //==============================================================================================   turnGyro
    public void turnGyro(double turningDegrees, double power, boolean turnRight)
    {
        telemetry.addData("turnGyro", "running");
        telemetry.update();

        backRightWheel.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backLeftWheel.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        gyro.resetZAxisIntegrator();
        if(turnRight)
        {
            backRightWheel.setDirection(DcMotor.Direction.FORWARD);
            backLeftWheel.setDirection(DcMotor.Direction.FORWARD);

            backRightWheel.setPower(power);
            backLeftWheel.setPower(power);

            while(gyro.getHeading() + 180 < 180 - turningDegrees) {}
        }
        else
        {
            backRightWheel.setDirection(DcMotor.Direction.REVERSE);
            backLeftWheel.setDirection(DcMotor.Direction.REVERSE);

            backRightWheel.setPower(power);
            backLeftWheel.setPower(power);

            while(gyro.getHeading() + 180 < 180 + turningDegrees) {}
        }

        backRightWheel.setPower(0);
        backLeftWheel.setPower(0);

        backRightWheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backLeftWheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }
    //==============================================================================================   skystoneNone
    public void skystoneNone()
    {
        telemetry.addData("skystoneNone", "running");
        telemetry.update();
        //Position to pick up skystone

        //Dive and pick up skystone
        move(12, 1, true);

        clapper(false);

        //Back up with skystone and rotate
        move(12, 1, false);

        //move to the preplanned position
        move(12, 1, true);

        //Run method to return place skystone on foundation
        skystoneReturn();
    }
    //==============================================================================================   skystoneLeft
    public void skystoneLeft()
    {
        telemetry.addData("skystoneLeft", "running");
        telemetry.update();
        //Position to pick up skystone

        //Dive and pick up skystone
        turnOnSpot(90, 0.1, true);
        move(8, 1, true);
        turnOnSpot(90, 0.1, false);


        move(10, 1, true);


        clapper(false);

        //Back up with skystone and rotate
        move(12, 1, false);

        //move to the preplanned position
        turnOnSpot(90, 0.1, false);

        //Run method to return place skystone on foundation
        skystoneReturn();
    }
    //==============================================================================================   skystoneRight
    public void skystoneRight()
    {
        telemetry.addData("skystoneRight", "running");
        telemetry.update();
        //Position to pick up skystone

        //Dive and pick up skystone
        move(10, 1, true);
        clapper(false);

        //Back up with skystone and rotate
        move(6, 1, false);

        //move to the preplanned position
        turn(90,1,false,false);

        move(6, 1, true);

        //Run method to return place skystone on foundation
        skystoneReturn();
    }
    //==============================================================================================   skystoneReturn
    public void skystoneReturn()
    {
        move(36, 1, true);
    }
    //==============================================================================================   shuffle
    public void shuffle()
    {
        //Move Shuffle
        if (shuffleCount <= 3)
        {
            move(shuffleCount*2, 0.1, true);
            move(shuffleCount*2, 0.1, false);
        }
        else if (shuffleCount <= 5)
        {
            move(shuffleCount, 0.1, false);
            move(shuffleCount, 0.1, true);
        }

        //Turn Shuffle
        if (shuffleCount == 6)
        {
            turn(10, 0.1, true, false);
            turn(10, 0.1, false, false);
        }
        else if (shuffleCount == 8)
        {
            turn(15, 0.1, true, false);
            turn(15, 0.1, false, false);
        }
        else if (shuffleCount == 10)
        {
            turn(20, 0.1, true, true);
            turn(20, 0.1, false, true);
        }
        else if (shuffleCount == 12)
        {
            turn(25, 0.1, true, true);
            turn(25, 0.1, false, true);
        }


        telemetry.addData("Shuffle Count", shuffleCount);
        telemetry.update();
        shuffleCount++;
    }
    //==============================================================================================   tensorFlowDrive
    public void tensorFlowDrive()
    {
        do
        {
            tensorFlow.tensorFlow();
            shuffle();
        }while(tensorFlow.needsShuffle);

        if (tensorFlow.getSkystonePosition() == TensorFlow.Position.none)
        {
            telemetry.addData("move to the skystone offscreen", "");
            skystoneNone();
        }
        else if (tensorFlow.getSkystonePosition() == TensorFlow.Position.left)
        {
            telemetry.addData("move to the left skystone position", "");
            skystoneLeft();
        }
        else if (tensorFlow.getSkystonePosition() == TensorFlow.Position.right)
        {
            telemetry.addData("move to the right skystone position", "");
            skystoneRight();
        }
        else
            telemetry.addData("Error: ", "No Move");
        telemetry.update();
    }

    public boolean needsShuffle()
    {
        return tensorFlow.needsShuffle();
    }
}

