// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.lib.aupirates3291.lib.subsystems.lighting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.motorcontrol.Spark;  
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.lib.aupirates3291.constants.Ports.PORTLIST;
import frc.lib.aupirates3291.helper.ShuffleboardHelper;
import frc.lib.aupirates3291.lib.subsystems.lighting.constants.BlinkenLightingConstants;
import frc.lib.aupirates3291.lib.subsystems.lighting.constants.BlinkenLightingConstants.Colors;

public class LightingSubsystem extends SubsystemBase {
  private Spark lighting;

  private final SendableChooser<Colors> lightingChooser = new SendableChooser<>();
  private GenericEntry lightingChooserEntry;
  private GenericEntry onOffToggle;

  /**
   * Creates a new LightingSubsystem.
   */
  public LightingSubsystem() {
    // Empty constructor
  }

  public void init() {
    ShuffleboardTab tab = Shuffleboard.getTab("Lighting");

    // Define Blinken module, currently as a Spark motor controller Servo
    lighting = new Spark(PORTLIST.LIGHTING_PORT.getPort());

    // Set the default color
    lighting.set(BlinkenLightingConstants.startingColor.getColorValue());

    // We need to return the added toggle in order to see if it is enabled or not
    onOffToggle = ShuffleboardHelper.addOnOffToggle(
      tab, 
      "Switch", 
      false, 
      List.of(0, 0),  // Column, Row
      List.of(2, 1)  // Width, Height
    );

    addColorSelector(tab);
  }

  public void addColorSelector(ShuffleboardTab tab) {
    List<Colors> colors;
    List<String> colorList = new ArrayList<>();

    // Colors to be displayed
    colorList.add("Solid Colors");
    colorList.add("Fixed Palette Patterns");

    colors = Colors.getColorsByTypeNames(colorList);

    // Add the colors to the SmartDashboard
    lightingChooser.setDefaultOption(BlinkenLightingConstants.startingColor.getColorName(), BlinkenLightingConstants.startingColor);

    for (Colors c : colors) {
      lightingChooser.addOption(c.getColorName(), c);
    }

    // Get the colors by the type names
    lightingChooserEntry = ShuffleboardHelper.addChooser(
      tab, 
      "Color Selector", 
      lightingChooser, 
      List.of(0, 1),  // Column, Row
      List.of(2, 1)  // Width, Height
    );
  }

  /**
   * Disables the lighting
   */
  public void disable() {
    lighting.set(Colors.OFF.getColorValue());
  }

  /**
   * Sets the lighting to the alliance color
   * 
   * NOTE: Selected alliance color only displays when the robot is enabled
   */
  public void setAllianceColor() {
    Optional<Alliance> alliance = DriverStation.getAlliance();

    // Set the lighting to the alliance color
    if (alliance.isPresent()) {
      if (alliance.get() == Alliance.Blue) {
        lighting.set(Colors.BLUE.getColorValue());
        BlinkenLightingConstants.setDefaultColor(lightingChooser, lightingChooserEntry, Colors.BLUE);
      } else if (alliance.get() == Alliance.Red) {
        lighting.set(Colors.RED.getColorValue());
        BlinkenLightingConstants.setDefaultColor(lightingChooser, lightingChooserEntry, Colors.RED);
      } else {
        lighting.set(Colors.OFF.getColorValue());
        BlinkenLightingConstants.setDefaultColor(lightingChooser, lightingChooserEntry, Colors.OFF);
      }
    } else {
      lighting.set(Colors.OFF.getColorValue());
      BlinkenLightingConstants.setDefaultColor(lightingChooser, lightingChooserEntry, Colors.OFF);
    }
  }

  public void setLighting(String colorName) {
    Colors selectedColor = Colors.getColorByName(colorName);

    if (selectedColor != null) {
      lighting.set(selectedColor.getColorValue());

      if (lightingChooserEntry != null) {
        BlinkenLightingConstants.setDefaultColor(lightingChooser, lightingChooserEntry, selectedColor);
      }
    }
  }

  @Override
  public void periodic() {
    // Default color
    Colors selectedColor = Colors.OFF;

    // Check if the lighting is disabled
    if (!onOffToggle.getBoolean(false)) {
      // Turn off lights
      lighting.set(selectedColor.getColorValue());

      // Update display
      BlinkenLightingConstants.setDefaultColor(lightingChooser, lightingChooserEntry, selectedColor);
    } else {
      // This method will be called once per scheduler run
      selectedColor = lightingChooser.getSelected();

      // Set the lighting to the selected color
      if (selectedColor.getColorName().equals("Alliance")) {
        // Set the lighting to the alliance color
        setAllianceColor();
      } else {
        // Set the lighting to the selected color
        lighting.set(selectedColor.getColorValue());
        BlinkenLightingConstants.setDefaultColor(lightingChooser, lightingChooserEntry, selectedColor);
      }
    }
  }
}
