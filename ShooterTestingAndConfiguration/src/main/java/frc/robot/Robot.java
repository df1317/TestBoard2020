
package frc.robot;

//imports
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Joystick;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.*;
//import com.analog.adis16448.frc.*;
//for some reason, com.analog.adis16448.* seems to prevent the build from being successful
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import com.revrobotics.*;
import edu.wpi.first.wpilibj.util.Color;

public class Robot extends TimedRobot {
	

//_______________Declarations_______________

	I2C.Port i2cPort = I2C.Port.kOnboard;

	//Talon declaration (WPI)
	WPI_TalonSRX FRMotor = new WPI_TalonSRX(1);
	WPI_TalonSRX BRMotor = new WPI_TalonSRX(2);
	WPI_TalonSRX FLMotor = new WPI_TalonSRX(3);
	WPI_TalonSRX BLMotor = new WPI_TalonSRX(4);
	WPI_TalonSRX ColorMotor = new WPI_TalonSRX(5);

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
	Joystick joyR = new Joystick(2);

	//Joystick Function declarations
	boolean joyETRigger;
	double ExtraVal;
	double TestVal;
	boolean joyERunColorWheel;
	boolean joyEResetColorWheel;
	boolean joyEAddBottom;
	boolean joyESubractBottom;
	boolean joyEEndgame;

	//Additional Values
	double TopMotorVal;
	double BottomMotorVal;
	double ColorMotorVal;
	Color currentColor;
	int color;
	int fieldColor;
	int endgameColor;
	int halfRotation;
	boolean endRotation;
	boolean allRotationsDone;
	String gameData;
	boolean endColorR;
	boolean endColorG;
	boolean endColorY;
	boolean endColorB;

	// This function is called once at the beginning during operator control
	public void robotInit() {

		TopMotorVal = 0;
		BottomMotorVal = 0;
		halfRotation = 0;
		allRotationsDone = false;
		color = 0;
		fieldColor = 0;
		m_colorMatcher.addColorMatch(kBlueTarget);
		m_colorMatcher.addColorMatch(kGreenTarget);
		m_colorMatcher.addColorMatch(kRedTarget);
		m_colorMatcher.addColorMatch(kYellowTarget);
		boolean endColorB = false;
		boolean endColorG = false;
		boolean endColorY = false;
		boolean endColorR = false;

	}

	// This function is called periodically during operator control
	public void robotPeriodic() {

		//get joystick values and buttons and such
		ExtraVal = joyR.getY();
		TestVal = joyL.getY();
		joyETRigger = joyE.getRawButton(1);
		joyERunColorWheel = joyE.getRawButton(5);
		joyEResetColorWheel = joyE.getRawButtonPressed(3);
		joyEEndgame = joyE.getRawButton(2);
		ColorMotorVal = 0.5;

		//DriveTrain
		FRMotor.set(ExtraVal);
		BRMotor.set(ExtraVal);
		FLMotor.set(-TestVal);
		BLMotor.set(-TestVal);

		gameData = DriverStation.getInstance().getGameSpecificMessage();
		
		//Color Detection Part 1
		currentColor = colorSensor.getColor();
		if (gameData == "R") {
			endgameColor = 1;
			endColorR = true;
		}
		if (gameData == "G") {
			endgameColor = 2;
			endColorG = true;
		}
		if (gameData == "B") {
			endgameColor = 3;
			endColorB = true;
		}
		if (gameData == "Y") {
			endgameColor = 4;
			endColorY = true;
		} 
		String colorString;
		final ColorMatchResult match = m_colorMatcher.matchClosestColor(currentColor);

	/*
		4 is yellow
		3 is blue
		2 is green
		1 is red	
					*/
	
		//Color Detection Part 2
		if (match.color == kGreenTarget) {
		  colorString = "Green";
		  color = 2;
		  fieldColor = 4;
		  endRotation = true;
		} else if (match.color == kYellowTarget) {
		  colorString = "Yellow";
		  color = 4;
		  fieldColor = 2;
		} else if (match.color == kBlueTarget) {
		  colorString = "Blue";
		  color = 3;
		  fieldColor = 1;
		} else if (match.color == kRedTarget) {
		colorString = "Red";
		color = 1;
		fieldColor = 3;
		} else {
			colorString = "Unknown";
			color = 0;
			fieldColor = 0;
		  }
		if (fieldColor == 1 && endRotation == true) {
			halfRotation = halfRotation + 1;
			endRotation = false;
		}	
    	SmartDashboard.putNumber("Confidence", match.confidence);
		SmartDashboard.putString("Detected Color", colorString);		

		//Normal Color wheel
		if (halfRotation == 7) {
			allRotationsDone = true;
		}
		if (joyERunColorWheel == true && allRotationsDone == false) {
			ColorMotor.set(ControlMode.PercentOutput, ColorMotorVal);
		}
		if (joyEResetColorWheel == true) {
			allRotationsDone = false;
			halfRotation = 0;
		}

		//endgame color wheel, Jonathan's design, Erin's execution.
		// :D
		if (joyEEndgame == true && endColorR == true && fieldColor != 1) {
			ColorMotor.set(ControlMode.PercentOutput, ColorMotorVal);
		} 
		if (joyEEndgame == true && endColorY == true && fieldColor != 4) {
			ColorMotor.set(ControlMode.PercentOutput, ColorMotorVal);
		}
		if (joyEEndgame == true && endColorB == true && fieldColor != 3) {
			ColorMotor.set(ControlMode.PercentOutput, ColorMotorVal);
		}
		if (joyEEndgame == true && endColorG == true && fieldColor != 2) {
			ColorMotor.set(ControlMode.PercentOutput, ColorMotorVal);
		}
		System.out.println("color = " + color + (" field detected-color " + fieldColor + " half rotations" + halfRotation));
		SmartDashboard.putNumber("color", color);
		SmartDashboard.putNumber("field detected color", fieldColor);
		SmartDashboard.putNumber("half rotations", halfRotation);
	}
}