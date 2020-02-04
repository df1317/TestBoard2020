
package frc.robot;

//imports
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.kauailabs.navx.frc.AHRS;
import com.revrobotics.ColorMatch;
import com.revrobotics.ColorMatchResult;
import com.revrobotics.ColorSensorV3;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.Timer;

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
	boolean colorRotations;
	boolean colorEndgame;
	boolean toggleGearShift;
	boolean gearShift;

	//Additional Values
	double ColorMotorVal = 0.5;
	int color = 0;
	int fieldColor = 0;
	int endgameColor;
	int halfRotation = 0;
	boolean endRotation;
	boolean allRotationsDone = false;
	String gameData;
	Timer timeTest = new Timer();
	double X;
	double Y;
	double Z;

	// This function is called once at the beginning during operator control
	public void robotInit() {
		timeTest.start();

		try {
            ahrs = new AHRS(SPI.Port.kMXP); 
        } catch (RuntimeException ex ) {
            DriverStation.reportError("Error instantiating navX MXP:  " + ex.getMessage(), true);
		}
		//assinging the values of XYZ for the gyroscope

		
		m_colorMatcher.addColorMatch(kBlueTarget);
		m_colorMatcher.addColorMatch(kGreenTarget);
		m_colorMatcher.addColorMatch(kRedTarget);
		m_colorMatcher.addColorMatch(kYellowTarget);
	}

	// This function is called periodically during operator control
	public void robotPeriodic() {
		System.out.println(timeTest.get());

		//gyro
		X = ahrs.getRoll();
		Y = ahrs.getPitch();
		Z = ahrs.getYaw();

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
		colorRotations = joyE.getRawButton(4);
		colorEndgame = joyE.getRawButton(6);
		gearShift = joyR.getRawButton(1);

		
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
			System.out.println("Collecting from the human player")
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
		if (colorRotations && !allRotationsDone) {
			ColorMotor.set(ControlMode.PercentOutput, ColorMotorVal);
		}
	/*	if (joyEResetColorWheel) {
			allRotationsDone = false;
			halfRotation = 0;
			endRotation = false;
		} */

		//endgame color wheel, Jonathan's design, Erin's execution, and Jacob's incredible clean-up skillz.
		if(colorEndgame && endgameColor != fieldColor){
			ColorMotor.set(ControlMode.PercentOutput, ColorMotorVal);
		}
	}
}