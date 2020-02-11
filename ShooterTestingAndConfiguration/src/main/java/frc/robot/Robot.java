
package frc.robot;

//imports
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.kauailabs.navx.frc.AHRS;
import com.revrobotics.ColorMatch;
import com.revrobotics.ColorMatchResult;
import com.revrobotics.ColorSensorV3;

import edu.wpi.first.wpilibj.ADXL345_I2C;
import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.interfaces.Accelerometer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
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
	WPI_TalonSRX WinchMotor = new WPI_TalonSRX(6);
	WPI_TalonSRX SwifferMotor = new WPI_TalonSRX(7);
	WPI_TalonSRX BeltMotor = new WPI_TalonSRX(8);
	DoubleSolenoid SwifferPiston = new DoubleSolenoid(9, 1, 2);
	DoubleSolenoid GearShift = new DoubleSolenoid(10, 3, 4);
	DoubleSolenoid CollectionDoor = new DoubleSolenoid(11, 5, 6);
	I2C.Port i2cPort = I2C.Port.kOnboard;
	ColorSensorV3 colorSensor = new ColorSensorV3(i2cPort);
	AHRS ahrs;

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
	boolean groundCollection;
	boolean ballShooter;
	boolean eject;
	boolean winchForwards;
	boolean winchReverse;
	int POVhook;
	boolean stationCollection;
	boolean colorRotate;
	boolean colorEndgame;
	boolean gearShift;
	boolean resetButton1;
	boolean resetButton2;

	//Additional Values
	double ColorMotorVal = 0.5;
	int color = 0;
	int fieldColor = 0;
	int endgameTargetColor;
	int halfRotation = 0;
	boolean endRotation;
	boolean allRotationsDone = false;
	String gameData;
	Timer TestDelta = new Timer();
	double X;
	double Y;
	double Z;
	double distance;

	BuiltInAccelerometer accel2;

	Accelerometer accel;
	double AccelerometerX;

	// This function is called once at the beginning during operator control
	public void robotInit() {
		TestDelta.start();

		try {
            ahrs = new AHRS(SPI.Port.kMXP); 
        } catch (RuntimeException ex ) {
            DriverStation.reportError("Error instantiating navX MXP:  " + ex.getMessage(), true);
		}
		m_colorMatcher.addColorMatch(kBlueTarget);
		m_colorMatcher.addColorMatch(kGreenTarget);
		m_colorMatcher.addColorMatch(kRedTarget);
		m_colorMatcher.addColorMatch(kYellowTarget);
	}

	// This function is called periodically during operator control
	public void robotPeriodic() {
		double currentDeltaTIme = TestDelta.get();
		SmartDashboard.putNumber("deltaTime", currentDeltaTIme);
		double distanceSinceReset = ahrs.getVelocityX() * currentDeltaTIme;
		distance = distanceSinceReset + distance;
		TestDelta.reset();
		SmartDashboard.putNumber("distance", distance);
		SmartDashboard.putNumber("meters/sec", ahrs.getVelocityX());

		//accel2.getX();

		//gyro
		/*X = ahrs.getRoll();
		SmartDashboard.putNumber("Roll", X);
		Y = ahrs.getPitch();
		SmartDashboard.putNumber("Pitch", Y);
		Z = ahrs.getYaw();
		SmartDashboard.putNumber("Yaw", Z);*/

		
		//trying out the displacement thingy
		SmartDashboard.putBoolean("isCalibrating", ahrs.isCalibrating());

		//get joystick values and buttons and such
		RightVal = joyR.getY();
		LeftVal = joyL.getY();
		groundCollection = joyE.getRawButton(2);
		ballShooter = joyE.getRawButton(1);
		eject = joyE.getRawButton(5);
		winchForwards = joyE.getRawButton(11);
		winchReverse = joyE.getRawButton(12);
		POVhook = joyE.getPOV();
		stationCollection = joyE.getRawButton(3);
		colorRotate = joyE.getRawButton(4);
		colorEndgame = joyE.getRawButton(6);
		gearShift = joyR.getRawButton(1);
		resetButton1 = joyE.getRawButton(7);
		resetButton2 = joyE.getRawButton(8);
		//reset button 2 for the various pistons of the robot
		
		//DriveTrain
		FRMotor.set(RightVal);
		BRMotor.set(RightVal);
		FLMotor.set(LeftVal);
		BLMotor.set(LeftVal);

		//Ground Collection
		if(groundCollection) {
			CollectionDoor.set(DoubleSolenoid.Value.kForward);
			SwifferPiston.set(DoubleSolenoid.Value.kForward);
			SwifferMotor.set(.5);
			BeltMotor.set(.5);
			System.out.println("Collecting from ground");
		}

		//Human player station collection
		if(stationCollection) {
			CollectionDoor.set(DoubleSolenoid.Value.kReverse);
			SwifferPiston.set(DoubleSolenoid.Value.kReverse);
			BeltMotor.set(-0.5);
			System.out.println("Collecting from the human player");
		}

		//Ball shooter
		if(ballShooter) {
			CollectionDoor.set(DoubleSolenoid.Value.kReverse);
			SwifferPiston.set(DoubleSolenoid.Value.kReverse);
			BeltMotor.set(1);
			System.out.println("Lobbing the balls from the cannon thingy");
		}

		//Ball eject
		if(eject) {
			CollectionDoor.set(DoubleSolenoid.Value.kForward);
			SwifferPiston.set(DoubleSolenoid.Value.kForward);
			BeltMotor.set(-1);
			SwifferMotor.set(-1);
			System.out.println("Ejecting balls from collector");
		}
	
		//Resetting the color wheel values and whatnot
		if (resetButton1) {
			allRotationsDone = false;
			halfRotation = 0;
			endRotation = false;
		}
		//Resetting the pistons to default positions
		if (resetButton2) {
			CollectionDoor.set(DoubleSolenoid.Value.kForward);
			SwifferPiston.set(DoubleSolenoid.Value.kReverse);
		}

		//turning the motors off if they are not in use
		if (!eject && !ballShooter && !stationCollection && !groundCollection) {
			BeltMotor.set(0);
			SwifferMotor.set(0);
		}
	
		//Classic endgame question of "what color do we need to get again???"
		gameData = DriverStation.getInstance().getGameSpecificMessage();
		if(gameData.length() > 0) {
			switch (gameData.charAt(0))
			{
				case 'R': endgameTargetColor = 1;
					break;
				case 'G': endgameTargetColor = 2;
					break;
				case 'B': endgameTargetColor = 3;
					break;
				case 'Y': endgameTargetColor = 4;
					break;
				default: endgameTargetColor = 0;
					break;
			}
		}
		
		//Color Sensor functions
		if (colorRotate || colorEndgame) {
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
		}	
		

		//Normal Color wheel functions
		if (halfRotation == 7) {
			allRotationsDone = true;
		}
		
		//Rotating the Color wheel 3 ish rotations
		if (colorRotate && !allRotationsDone) {
			ColorMotor.set(ControlMode.PercentOutput, ColorMotorVal);
		}

		//endgame color wheel, Jonathan's design, Erin's execution, and Jacob's incredible clean-up skillz.
		if(colorEndgame && endgameTargetColor!=fieldColor){
			ColorMotor.set(ControlMode.PercentOutput, ColorMotorVal);
		}
	}
}