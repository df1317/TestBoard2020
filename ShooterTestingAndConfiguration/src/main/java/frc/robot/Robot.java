
package frc.robot;

//imports
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.revrobotics.ColorMatch;
import com.revrobotics.ColorMatchResult;
import com.revrobotics.ColorSensorV3;
import com.analog.adis16448.frc.*;
//for some reason, com.analog.adis16448.* seems to prevent the build from being successful
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Color;

public class Robot extends TimedRobot {
	

//_______________Declarations_______________

	//Hardware Declarations
	WPI_TalonSRX FRMotor = new WPI_TalonSRX(1);
	WPI_TalonSRX BRMotor = new WPI_TalonSRX(2);
	WPI_TalonSRX FLMotor = new WPI_TalonSRX(3);
	WPI_TalonSRX BLMotor = new WPI_TalonSRX(4);
	WPI_TalonSRX ColorMotor = new WPI_TalonSRX(5);
	I2C.Port i2cPort = I2C.Port.kOnboard;
	ColorSensorV3 colorSensor = new ColorSensorV3(i2cPort);

	//Color Sensor Declaration/Values
	final Color kBlueTarget = ColorMatch.makeColor(0.143, 0.427, 0.429);
	final Color kGreenTarget = ColorMatch.makeColor(0.197, 0.561, 0.240);
	final Color kRedTarget = ColorMatch.makeColor(0.561, 0.232, 0.114);
  	final Color kYellowTarget = ColorMatch.makeColor(0.361, 0.524, 0.113);
	ColorMatch m_colorMatcher = new ColorMatch();
	ColorMatchResult match;
	Color currentColor;
	String colorString;

	//Joystick declarations
	Joystick joyE = new Joystick(0);
	Joystick joyL = new Joystick(1);
	Joystick joyR = new Joystick(2);
	boolean joyETRigger;
	double RightVal;
	double LeftVal;
	boolean joyERunColorWheel;
	boolean joyEResetColorWheel;
	boolean joyEAddBottom;
	boolean joyESubractBottom;
	boolean joyEEndgame;

	//Additional Values
	double ColorMotorVal = 0.5;
	int color = 0;
	int fieldColor = 0;
	int endgameColor;
	int halfRotation = 0;
	boolean endRotation;
	boolean allRotationsDone = false;
	String gameData;

	// This function is called once at the beginning during operator control
	public void robotInit() {
		m_colorMatcher.addColorMatch(kBlueTarget);
		m_colorMatcher.addColorMatch(kGreenTarget);
		m_colorMatcher.addColorMatch(kRedTarget);
		m_colorMatcher.addColorMatch(kYellowTarget);
	}

	// This function is called periodically during operator control
	public void robotPeriodic() {
		//get joystick values and buttons and such
		RightVal = joyR.getY();
		LeftVal = joyL.getY();
		joyETRigger = joyE.getRawButton(1);
		joyERunColorWheel = joyE.getRawButton(5);
		joyEResetColorWheel = joyE.getRawButtonPressed(3);
		joyEEndgame = joyE.getRawButton(2);

		//DriveTrain
		FRMotor.set(RightVal);
		BRMotor.set(RightVal);
		FLMotor.set(-LeftVal);
		BLMotor.set(-LeftVal);

		//Classic endgame question of "what color do we need to get again???"
		gameData = DriverStation.getInstance().getGameSpecificMessage();
		if(gameData.length() > 0) {
			switch (gameData.charAt(0))
			{
				case 'R': endgameColor = 1;
					break;
				case 'G': endgameColor = 2;
					break;
				case 'B': endgameColor = 3;
					break;
				case 'Y': endgameColor = 4;
					break;
				default:
					break;
			}
		}
		//Color Sensor functions
		currentColor = colorSensor.getColor();
		match = m_colorMatcher.matchClosestColor(currentColor);

		if (match.color == kGreenTarget) {
			colorString = "Green";
			color = 2;
			endRotation = true;
		} else if (match.color == kYellowTarget) {
			colorString = "Yellow";
			color = 4;
		} else if (match.color == kBlueTarget) {
			colorString = "Blue";
			color = 3;
		} else if (match.color == kRedTarget) {
			colorString = "Red";
			color = 1;
		} else {
			colorString = "Unknown";
			color = 0;
			fieldColor = 0;
		}

		if(color != 0) fieldColor = (color+2)%4;
		
		if (fieldColor == 1 && endRotation) {
			halfRotation = halfRotation + 1;
			endRotation = false;
		}	

    	SmartDashboard.putNumber("Confidence", match.confidence);
		SmartDashboard.putString("Detected Color", colorString);		

		//Normal Color wheel functions
		if (halfRotation == 7) {
			allRotationsDone = true;
		}

		if (joyERunColorWheel  && !allRotationsDone) {
			ColorMotor.set(ControlMode.PercentOutput, ColorMotorVal);
		}

		if (joyEResetColorWheel) {
			allRotationsDone = false;
			halfRotation = 0;
			endRotation = false;
		}

		//endgame color wheel, Jonathan's design, Erin's execution, and Jacob's incredible clean-up skillz.
		// :D
		if(joyEEndgame && endgameColor != fieldColor){
			ColorMotor.set(ControlMode.PercentOutput, ColorMotorVal);
		}
	}
}