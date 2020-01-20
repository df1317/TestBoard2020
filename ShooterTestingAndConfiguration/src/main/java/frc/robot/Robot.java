
package frc.robot;

//imports
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Joystick;

import java.util.concurrent.TimeUnit;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.*;
import com.analog.adis16448.frc.*;
//for some reason, com.analog.adis16448.* seems to prevent the build from being successful
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.Sendable;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.revrobotics.*;
import edu.wpi.first.wpilibj.util.Color;




public class Robot extends TimedRobot {
	

//_______________Declarations_______________

	I2C.Port i2cPort = I2C.Port.kOnboard;

	//Talon declaration (normal)
	//TalonSRX TopMotor = new TalonSRX(5);
	//TalonSRX BottomMotor = new TalonSRX(3);

	//Talon declaration (WPI)
	WPI_TalonSRX TopMotor = new WPI_TalonSRX(4);
	WPI_TalonSRX BottomMotor = new WPI_TalonSRX(3);

	//Color Sensor Declaration/Values
	ColorSensorV3 colorSensor = new ColorSensorV3(i2cPort);
	ColorMatch m_colorMatcher = new ColorMatch();
	Color kBlueTarget = ColorMatch.makeColor(0.143, 0.427, 0.429);
  	Color kGreenTarget = ColorMatch.makeColor(0.197, 0.561, 0.240);
  	Color kRedTarget = ColorMatch.makeColor(0.561, 0.232, 0.114);
	Color kYellowTarget = ColorMatch.makeColor(0.361, 0.524, 0.113);
	

	//Joystick declarations
	Joystick joyE = new Joystick(0);
	Joystick joyL = new Joystick(1);

	//Joystick Function declarations
	boolean joyETRigger;
	double ExtraVal;
	double TestVal;
	boolean joyEAddTop;
	boolean joyESubtractTop;
	boolean joyEAddBottom;
	boolean joyESubractBottom;

	//Additional Values
	double TopMotorVal;
	double BottomMotorVal;

	Color currentColor;
	
	// boolean endRotation
	// int halfRotation
	// int colorNumber


	// This function is called once at the beginning during operator control
	public void robotInit() {

		TopMotorVal = 0;
		BottomMotorVal = 0;

		m_colorMatcher.addColorMatch(kBlueTarget);
		m_colorMatcher.addColorMatch(kGreenTarget);
		m_colorMatcher.addColorMatch(kRedTarget);
		m_colorMatcher.addColorMatch(kYellowTarget);

	}

	// This function is called periodically during operator control
	public void robotPeriodic() {

		currentColor = colorSensor.getColor();

		String colorString;
		ColorMatchResult match = m_colorMatcher.matchClosestColor(currentColor);
	
		if (match.color == kGreenTarget) {
		  colorString = "Green";
		} else if (match.color == kYellowTarget) {
		  colorString = "Yellow";
		} else if (match.color == kBlueTarget) {
		  colorString = "Blue";
		} else if (match.color == kRedTarget) {
		colorString = "Red";
		} else {
			colorString = "Unknown";
		  }

		//get joystick values and buttons and such
		ExtraVal = joyE.getY();
		TestVal = joyL.getY();
		joyETRigger = joyE.getRawButton(1);
		//joyEAddTop = joyE.getRawButtonPressed(5);
		//joyESubtractTop = joyE.getRawButtonPressed(3);
		//joyEAddBottom = joyE.getRawButtonPressed(6);
		//joyESubractBottom = joyE.getRawButtonPressed(4);
		TopMotorVal = ExtraVal;
		BottomMotorVal = ExtraVal;
		//previously Top was + and Bottom was -, but I swapped it for testing purposes

		//adding/subtracting from TopmotorVal with WPI_TalonSRX apposed to normal
	/*	if (joyEAddTop == true) {
			TopMotorVal = TopMotorVal + 0.05;
		}
		if (joyESubtractTop == true) {
			TopMotorVal = TopMotorVal -0.05;
		}
			*/
			
    	SmartDashboard.putNumber("Confidence", match.confidence);
		SmartDashboard.putString("Detected Color", colorString);
		SmartDashboard.putNumber("LeftMostJoystick", ExtraVal);
		SmartDashboard.putNumber("TopVal", TopMotorVal);
		SmartDashboard.putNumber("BottomVal", BottomMotorVal);
		System.out.println("Test thingy" + TopMotor);
		//SmartDashboard.putNumber("TopMotor", TopMotor);




	/*	if (joyEAddBottom == true) {
			BottomMotorVal = BottomMotorVal + 0.05;
		}
		if (joyESubractBottom == true) {
			BottomMotorVal = BottomMotorVal - 0.05;
		}
		TopMotor.set(ExtraVal); */

		//if the trigger is pressed, run the motors
		if (joyETRigger == true) {
			TopMotor.set(ControlMode.PercentOutput, TopMotorVal);
			BottomMotor.set(ControlMode.PercentOutput, BottomMotorVal);
		}
		else {
			TopMotor.set(ControlMode.PercentOutput, 0);
			BottomMotor.set(ControlMode.PercentOutput, 0);
		} 


/* old/unused code

		if (joyETRigger == false) {
			TopMotor.set(0);
			//this just sets the swiffer values for it going up and down to the value of the extra joystick
		}
		else {
			swifferupdown.set(0);
			swifferupdownSlave.set(0);
		}

		//Driving
		// Deadband - within 5% joystick, make it zero
		if (Math.abs(leftVal) < 0.05) {
			leftVal = 0;
		}
		if (Math.abs(rightVal) < 0.05) {
			rightVal = 0;
		}

		//slow the robot whilst driving
		if(joyLTrigger && joyRTrigger) {
			leftVal = leftVal/2;
			rightVal = rightVal/2;
			robotSlowDriveDebug = 1;
		}
		else {
			robotSlowDriveDebug = 0;
		}

		//shoot the ball
		if(joyRballshoot) {
			ballshoot = -1;
			ballshootdebug = 1;
		}
		else if(joyRPOV == 180 && limitVal==false) {
			ballshoot = 0.25;
			ballshootdebug = -1;
		}
		else {
			ballshoot = 0;
			ballshootdebug = 0;
		}
		ballthingy.set(ballshoot);

		//testing limitswitch
		//System.out.println("limitVal " + limitVal);


		//System.out.println("leftVal = " + leftVal + " rightval = " + rightVal);

		//drive the diggity dang robit
		//drive.tankDrive(leftVal, rightVal);
		frontLeftMotor.set(leftVal);
		leftSlave1.set(leftVal);
		frontRightMotor.set(rightVal);
		rightSlave1.set(rightVal);

		//elevator up/down control (reformatted for the sake of testing, just about all of the code for the elevator is here)
		//should it be necessary for debugging, just have otherVal output
		if (joyETRigger == true) {
			elevator.set(otherVal);
		}
		else {
			elevator.set(0);
		}

		//spike hatch control
		if (joyEPOV == 0) {
			hatchCollector.set(-0.5);
			spikedebug = 1;
		}
		else if (joyEPOV == 180) {
			hatchCollector.set(0.5);
			spikedebug = -1;
		}
		else {
			hatchCollector.set(0);
			spikedebug = 0;
		}
		//System.out.println("spikeHatchCollector = " + spiketest);

		//swiffer in/out control w/ limit switch
		//once again, if I need it for debugging, just use swifferVal
		if(joyRPOV == 0) {
			swifferVal = 0.5;
		}
		else if(joyRPOV == 180) {
			swifferVal = -0.5;
		}
		else {
			swifferVal = 0;
		}
		swiffer.set(swifferVal);

		//everything pneumatic
		//button based pneumatic control
		//uncomment the printIn's for debugging purposes
		 if(joyEFrontpneu) {
			 frontpneuToggle = !frontpneuToggle;
		 }
		 if(joyEBackpneu) {
			 backpneuToggle = !backpneuToggle;
		}

		 if(joyEallpneu) {
		 	backpneuToggle = !backpneuToggle;
			 frontpneuToggle = !frontpneuToggle;
		 }

		//uncomment the print ins if debugging is necessary
		 if(frontpneuToggle) {
			solenoidFront.set(DoubleSolenoid.Value.kForward);
			//System.out.println("front solenoids going out");
		 }
		 else {
			solenoidFront.set(DoubleSolenoid.Value.kReverse);
			//System.out.println("front solenoids going back in");
		 }
		 if(backpneuToggle) {
			solenoidBack.set(DoubleSolenoid.Value.kForward);
			//System.out.println("back solenoids going out");
		 }
		 else {
			solenoidBack.set(DoubleSolenoid.Value.kReverse);
			//System.out.println("back solenoids going back in");
		 }	
		

		//print the values for different variables for bugtesting
		//System.out.println("JoyL:" + leftVal + "  joyR:" + rightVal + " joy3: " + otherVal + "elevatorVal: " + elevatorVal + "swifferVal: " + swifferVal);
		*/
	
	}
}
