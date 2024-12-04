import java.beans.VetoableChangeSupport;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class SolarSystem extends Application {
    private double anchorX, anchorY, anchorAngleX, anchorAngleY;

    //this needs to be an object becaused its accessed in a "closed loop", it can't be final because it changes
    private DoubleProperty speedMultiplier = new SimpleDoubleProperty(1.0);

    //stores current angle for each planet
    private Map<String, Double> angles = new HashMap<>();

    //holds the speed values for rotating on the y axis for each planet
    private Map<String, Double> rotations = new HashMap<>();

    private Map<String, Rotate> planetRotations = new HashMap<>();
    Map<String, Sphere> planetSpheres = new HashMap<>();
    Map<String, Text> planetLabels = new HashMap<>();

    //refers to the image when importing a planet
    private String selectedFilePath = null;

    //pointer to the csv path
    private String csvFilePath;
    
    
        @Override
        public void start(Stage primaryStage) {
            final int WIDTH = 1000;
            final int HEIGHT = 643;
    
            //calls loadPlanetsFromFile method to open the solar system using the data specified in the csv given
            List<Planet> planets = loadPlanetsFromFile(csvFilePath);
            System.out.println(planets);
    
            final int sunRadius = 200;
    
            Sphere sun = new Sphere(sunRadius);
            PhongMaterial sunMaterial = new PhongMaterial();
            sunMaterial.setDiffuseMap(new Image("file:earth/sun.jpg"));
            sunMaterial.setSelfIlluminationMap(new Image("file:earth/sun.jpg"));
            sun.setMaterial(sunMaterial);
    
            Group planetsGroup = new Group();
    
            //loops through the planets in the list and creates spheres, maps the images to spheres, adds name label
            //puts the sphere and label in hashmaps
            //adds a rotation object to the sphere and puts the object in a hashmap
            //adds the planet sphere and label to the planetsgroup
            for (Planet planet : planets) {
                Sphere planetSphere = new Sphere(planet.getRadius());
    
                PhongMaterial material = new PhongMaterial();
                material.setDiffuseMap(new Image(planet.getImagePath()));
                planetSphere.setMaterial(material);
    
                Text planetLabel = new Text(planet.getName());
                double fontSize = calculateFontSize(planet.getSemiMajorAxis());
                planetLabel.setFont(Font.font("Arial", fontSize));
                planetLabel.setFill(Color.WHITE);
    
                planetSpheres.put(planet.getName(), planetSphere);
                planetLabels.put(planet.getName(), planetLabel);

                Rotate planetRotate = new Rotate(0, Rotate.Y_AXIS);
                planetSphere.getTransforms().add(planetRotate);
                planetRotations.put(planet.getName(), planetRotate);
    
                planetsGroup.getChildren().addAll(planetSphere, planetLabel);
            }

            //rotates the sun
            Rotate sunRotate = new Rotate(0, Rotate.Y_AXIS);
            sun.getTransforms().add(sunRotate);
    
            AnimationTimer planetAnimation = new AnimationTimer() {
                //initalizes the sun rotation and speed, the sun is unique so i just hard coded its way to rotation
                private double sunRotation = 0;
                private double sunRotationSpeed;

                @Override
                public void handle(long now) {
                    sunRotationSpeed = 6 * speedMultiplier.get();
                    sunRotation += sunRotationSpeed;
                    sunRotate.setAngle(sunRotation);

                    for (Planet planet : planets) {
                        //grabs the specific planet values from the hashmaps
                        Sphere sphere = planetSpheres.get(planet.getName());
                        Rotate rotate = planetRotations.get(planet.getName());
                        Text label = planetLabels.get(planet.getName());
                        if (sphere == null){
                            System.out.println("the sphere is null");
                        }
                        if (rotate == null){
                            System.out.println("rotate is null");
                        }
                        if (sphere != null && rotate != null) {
                            //figures out the orbit/rotation speed in real time because the speed multiplier changes based on the slider
                            double orbitSpeed = planet.getOrbitSpeed() * speedMultiplier.get();
                            double rotationSpeed = planet.getRotationSpeed() * speedMultiplier.get();

                            //angle is grabbed from a planet if its in the hashmap. its not not in there use 0.0. add orbitspeed
                            //this basically is just the speed of the rotation, it plays a part in calculating where the planet needs to go
                            angles.put(planet.getName(), angles.getOrDefault(planet.getName(), 0.0) + orbitSpeed);

                            //grabs angle and updates position using updateOrbitalPosition method
                            double angle = angles.get(planet.getName());
                            updateOrbitalPosition(sphere, angle, planet.getSemiMajorAxis(), planet.getEccentricity());

                            //same with angle, gets it from hashmap. its not not in there use 0.0. add rotationspeed
                            //this just changes the speed in real time
                            rotations.put(planet.getName(), rotations.getOrDefault(planet.getName(), 0.0) + rotationSpeed);
                            rotate.setAngle(rotations.get(planet.getName()));

                            //ensures the label exists and then attaches it to wherever the planet is. offsets it by a bit above the sphere for better viewing
                            if (label != null) {
                                label.setTranslateX(sphere.getTranslateX());
                                label.setTranslateY(-planet.getRadius() - 20);
                                label.setTranslateZ(sphere.getTranslateZ());
                            }
                        }
                    }
                }
                //This is how the planets are updated
                //It uses keplers formula for calculating orbit called "The Polar Equation of an Ellipse"
                //javafx uses cartesion coordinates so i translate it using cartesion conversion
                //then set the planets to their respective x and z coordinates. doing this every frame gives it an orbiting look
                private void updateOrbitalPosition(Sphere planet, double angle, double semiMajorAxis, double eccentricity) {
                    double focusOffset = semiMajorAxis * eccentricity;
            
                    double r = semiMajorAxis * (1 - eccentricity * eccentricity) /
                            (1 + eccentricity * Math.cos(angle));
            
                    double x = r * Math.cos(angle) - focusOffset;
                    double z = r * Math.sin(angle);
            
                    planet.setTranslateX(x);
                    planet.setTranslateZ(z);
                }
            };
            planetAnimation.start();
    
            //how light is made in the scene to give it a realistic look. nothing fancy, just group the light and sun
            PointLight sunLight = new PointLight(Color.WHITE);
    
            sunLight.setTranslateX(0);
            sunLight.setTranslateY(0);
            sunLight.setTranslateZ(0);
    
            Group sunGroup = new Group(sun, sunLight);
    
            //add the sungroup and planetsgroup to the main group
            //creates the subscene for the 3d part of the main menu
            Group group = new Group(sunGroup, planetsGroup);
            SubScene threeDSubScene = new SubScene(group, WIDTH, HEIGHT, true, SceneAntialiasing.DISABLED);
            Camera camera = new PerspectiveCamera(true);
            threeDSubScene.setCamera(camera);
    
            //set the camera for viewing in the 3d space
            camera.setTranslateZ(-10000);
            camera.setNearClip(1);
            camera.setFarClip(100000);
    
            //the stack pane is over all the panes.
            //it first adds the background
            //second adds the subscene
            //third ads all of the labels, buttons, and vboxes
            StackPane root = new StackPane();
    
            Image galaxyBackground = loadImage("file:earth/galaxy4.jpg", "Galaxy background");
            ImageView galaxyImageView = new ImageView(galaxyBackground);
            galaxyImageView.setFitWidth(WIDTH);
            galaxyImageView.setFitHeight(HEIGHT);
            root.getChildren().add(galaxyImageView);
    
            root.getChildren().add(threeDSubScene);
    

            //buttons for resuming/pausing the rotation animation timer
            Button playButton = new Button("Resume");
            Button pauseButton = new Button("Pause");
    
            playButton.setPadding(new Insets(10));
            pauseButton.setPadding(new Insets(10));
    
            StackPane.setAlignment(playButton, Pos.BOTTOM_RIGHT);
            StackPane.setAlignment(pauseButton, Pos.BOTTOM_LEFT);
    
            StackPane.setMargin(pauseButton, new Insets(0, 0, 50, 10));
            StackPane.setMargin(playButton, new Insets(0, 10, 50, 0));
    
            //slider to dynamically change the speed of the scene
            Slider speedSlider = new Slider(0.01, 1, 0.3);
            speedSlider.setShowTickMarks(true);
            speedSlider.setShowTickLabels(true);
            speedSlider.setMajorTickUnit(1);
            speedSlider.setBlockIncrement(0.1);
            speedMultiplier.bind(speedSlider.valueProperty());

            StackPane.setAlignment(speedSlider, Pos.BOTTOM_CENTER);
    
            root.getChildren().addAll(playButton, speedSlider, pauseButton);
    
            //viewable buttons when your looking at the 3d interface
            Button importButton = new Button("Import Planet");
            importButton.setPadding(new Insets(10));
            StackPane.setAlignment(importButton, Pos.TOP_LEFT);
            StackPane.setMargin(importButton, new Insets(10, 0, 0, 10));
            root.getChildren().add(importButton);
    
            Button deleteButton = new Button("Delete Planet");
            deleteButton.setPadding(new Insets(10));
            StackPane.setAlignment(deleteButton, Pos.TOP_LEFT);
            StackPane.setMargin(deleteButton, new Insets(60, 0, 0, 10));
            root.getChildren().add(deleteButton);

            Button mainMenuButton = new Button("Main Menu");
            mainMenuButton.setPadding(new Insets(10));
            StackPane.setAlignment(mainMenuButton, Pos.TOP_RIGHT);
            StackPane.setMargin(mainMenuButton, new Insets(10, 10, 0, 0));
            root.getChildren().add(mainMenuButton);

            // Button viewDataButton = new Button("View Planet Data");
            // viewDataButton.setPadding(new Insets(10));
            // StackPane.setAlignment(viewDataButton, Pos.CENTER_LEFT);
            // StackPane.setMargin(viewDataButton, new Insets(10, 10, 0, 0));
            // root.getChildren().add(viewDataButton);
    
            VBox inputForm = new VBox(10);
            inputForm.setAlignment(Pos.CENTER);
            inputForm.setPadding(new Insets(20));
            inputForm.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-border-color: white; -fx-border-width: 2;");
    
            TextField nameField = new TextField();
            nameField.setMaxWidth(200);
            nameField.setPromptText("Planet Name");
    
            TextField radiusField = new TextField();
            radiusField.setMaxWidth(200);
            radiusField.setPromptText("Radius");
    
            TextField semiMajorAxisField = new TextField();
            semiMajorAxisField.setMaxWidth(200);
            semiMajorAxisField.setPromptText("Semi-Major Axis");
    
            TextField eccentricityField = new TextField();
            eccentricityField.setMaxWidth(200);
            eccentricityField.setPromptText("Eccentricity");
    
            TextField rotationSpeedField = new TextField();
            rotationSpeedField.setMaxWidth(200);
            rotationSpeedField.setPromptText("Rotation Speed");
    
            TextField orbitSpeedField = new TextField();
            orbitSpeedField.setMaxWidth(200);
            orbitSpeedField.setPromptText("Orbit Speed");
    
            Button imageChooserButton = new Button("Choose Image...");
            imageChooserButton.setStyle("-fx-min-width: 150;");
            Label selectedImageLabel = new Label("");
    
            ComboBox<String> colorDropdown = new ComboBox<>();
            colorDropdown.setStyle("-fx-min-width: 150;");
            colorDropdown.setValue("Choose Color...");
    
            //creates a file array for the colors and adds it to the dropdon above
            File colorsFolder = new File("colors");
            if (colorsFolder.exists() && colorsFolder.isDirectory()) {
                File[] colorFiles = colorsFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg"));
                if (colorFiles != null) {
                    for (File file : colorFiles) {
                        colorDropdown.getItems().add(file.getName());
                    }
                }
            }
    
            //if a color is selected, it will be applied to selectedFilePath, it sets the imagelabel text to "" 
            colorDropdown.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (!"Choose Color...".equals(newValue)) {
                    selectedFilePath = "file:colors/" + newValue;
                    selectedImageLabel.setText("");
                }
            });
    
            //Opens a filechooser and puts it to selectedfilePath, changes the color dropdown back to "Choose Color..."
            imageChooserButton.setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select Image for Planet");
                File chosenFile = fileChooser.showOpenDialog(primaryStage);
                if (chosenFile != null) {
                    selectedFilePath = "file:" + chosenFile.getAbsolutePath();
                    selectedImageLabel.setText("Selected: " + chosenFile.getName());
                    colorDropdown.setValue("Choose Color...");
                }
            });
            
            //Made an hbox so the image chooser and file chooser are next to each other with an orLabel 
            HBox imageAndColorSelector = new HBox(10);
            imageAndColorSelector.setAlignment(Pos.CENTER);
    
            Label orLabel = new Label("or");
            orLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14;");
    
            imageAndColorSelector.getChildren().addAll(imageChooserButton, orLabel, colorDropdown);
    
            Button submitButton = new Button("Add Planet");
            Button cancelButton = new Button("Cancel");
    
            submitButton.setStyle("-fx-min-width: 100;");
            cancelButton.setStyle("-fx-min-width: 100;");
    
            HBox buttonsBox = new HBox(10, submitButton, cancelButton);
            buttonsBox.setAlignment(Pos.CENTER);
    
            inputForm.getChildren().addAll(nameField, radiusField, semiMajorAxisField, eccentricityField,
                    rotationSpeedField, orbitSpeedField, imageAndColorSelector, selectedImageLabel, buttonsBox);
    
            inputForm.setVisible(false);
            root.getChildren().add(inputForm);
    
            importButton.setOnAction(e -> inputForm.setVisible(true));
            cancelButton.setOnAction(e -> inputForm.setVisible(false));
    
            submitButton.setOnAction(e -> {
                try {
                    //gets all the values from the text fields and checks if the name already exists and that the file path is not null
                    String name = nameField.getText().trim();
                    double radius = Double.parseDouble(radiusField.getText().trim());
                    double semiMajorAxis = Double.parseDouble(semiMajorAxisField.getText().trim());
                    double eccentricity = Double.parseDouble(eccentricityField.getText().trim());
                    double rotationSpeed = Double.parseDouble(rotationSpeedField.getText().trim());
                    double orbitSpeed = Double.parseDouble(orbitSpeedField.getText().trim());
            
                    if (selectedFilePath == null) {
                        throw new IllegalArgumentException("You must select an image or color!");
                    }
            
                    if (planetSpheres.containsKey(name)) {
                        throw new IllegalArgumentException("Planet with this name already exists!");
                    }

                    //creates a new planet object using the values, adds it to the planets list
                    Planet newPlanet = new Planet(name, radius, semiMajorAxis, eccentricity, rotationSpeed, orbitSpeed, selectedFilePath);
                    planets.add(newPlanet);
            
                    //adds the planet to the scene
                    addPlanetToScene(newPlanet, planetsGroup);
            
                    //creates an array list of all of the values
                    //creates another and adds commas
                    //appends it to the csv file
                    ArrayList<String> csvRow = new ArrayList<>();
                    csvRow.add(name);
                    csvRow.add(String.valueOf(radius));
                    csvRow.add(String.valueOf(semiMajorAxis));
                    csvRow.add(String.valueOf(eccentricity));
                    csvRow.add(String.valueOf(rotationSpeed));
                    csvRow.add(String.valueOf(orbitSpeed));
                    csvRow.add(selectedFilePath);
            
                    ArrayList<String> csvRowFormatted = new ArrayList<>();
                    csvRowFormatted.add(String.join(",", csvRow));
                    MyFile.appendToFile(csvRowFormatted, csvFilePath);
            
                    //clears all the fields for the next use
                    nameField.clear();
                    radiusField.clear();
                    semiMajorAxisField.clear();
                    eccentricityField.clear();
                    rotationSpeedField.clear();
                    orbitSpeedField.clear();
                    selectedFilePath = null;
                    selectedImageLabel.setText("");
                    colorDropdown.setValue("Choose Color...");
                    inputForm.setVisible(false);            
                } catch (Exception ex) {
                    System.err.println("Error adding planet: " + ex.getMessage());
                }
            });
            
            VBox deleteForm = new VBox(10);
            deleteForm.setAlignment(Pos.CENTER);
            deleteForm.setPadding(new Insets(20));
            deleteForm.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-border-color: white; -fx-border-width: 2;");
            deleteForm.setVisible(false);
    
            Button cancelDeleteButton = new Button("Cancel");
            cancelDeleteButton.setOnAction(e -> deleteForm.setVisible(false));
            deleteForm.getChildren().add(cancelDeleteButton);
    
            deleteButton.setOnAction(e -> {
                //clears the form
                //loops through the planets list and adds buttons for each planet
                //once a button is selected, it sends a confirmation
                //once confirmed it will just remove the planets from the list, call deletePlanet method and remove the row from the csv
                //it also removes it from the delete form, then sets visibility to false
                deleteForm.getChildren().clear();
                for (Planet planet : planets) {
                    Button planetButton = new Button(planet.getName());
                    planetButton.setMaxWidth(300);
                    deleteForm.getChildren().add(planetButton);
            
                    planetButton.setOnAction(event -> {
                        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
                        confirmationDialog.setTitle("Delete Confirmation");
                        confirmationDialog.setHeaderText("Are you sure you want to delete " + planet.getName() + "?");
                        confirmationDialog.setContentText("This action cannot be undone.");
            
                        Optional<ButtonType> result = confirmationDialog.showAndWait();
                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            deletePlanet(planet, planetsGroup);
                            planets.remove(planet);
                            MyFile.removeRowByName(planet.getName(), csvFilePath);
                            deleteForm.getChildren().remove(planetButton);
                        }
                        deleteForm.setVisible(false);
                    });
                }
                deleteForm.getChildren().add(cancelDeleteButton);
                deleteForm.setVisible(true);
            });
            
    
            root.getChildren().add(deleteForm);
    
            //switches the stage back to the mainmenu class
            mainMenuButton.setOnAction(e -> {
                MainMenu mainMenu = new MainMenu();
                mainMenu.start(primaryStage);
            });
    
            Scene mainScene = new Scene(root, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED);
    
            mainScene.setFill(new ImagePattern(galaxyBackground));
    
            initMouseControl(mainScene, camera, group);
    
            //functionality for play/pause buttons
            playButton.setOnAction(e -> {
                planetAnimation.start();
            });
    
            pauseButton.setOnAction(e -> {
                planetAnimation.stop();
            });
    
            //changes the cameras position (forward or backward using w/d)
            primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                switch (event.getCode()) {
                    case W:
                        camera.setTranslateZ(camera.getTranslateZ() + 100);
                        break;
                    case S:
                        camera.setTranslateZ(camera.getTranslateZ() - 100);
                        break;
                }
            });
    
            primaryStage.setTitle("Solar System");
            primaryStage.setScene(mainScene);
            primaryStage.show();
    
        }
    
        private void addPlanetToScene(Planet planet, Group planetsGroup) {
            //creates a new sphere, material, label etc. everything the planet needs
            //sets its initial position within the scene
            //adds it to the group
            //adds the sphere and label to the hashmap

            //BUG FIX, fixed a bug where the planet was not spinning upon import, it just needed to added to rotate hashmap
            Sphere planetSphere = new Sphere(planet.getRadius());
        
            PhongMaterial material = new PhongMaterial();
            material.setDiffuseMap(new Image(planet.getImagePath()));
            planetSphere.setMaterial(material);
        
            Text planetLabel = new Text(planet.getName());
            double fontSize = calculateFontSize(planet.getSemiMajorAxis());
            planetLabel.setFont(Font.font("Arial", fontSize));
            planetLabel.setFill(Color.WHITE);
        
            double initialX = planet.getSemiMajorAxis();
            double initialZ = 0;
            planetSphere.setTranslateX(initialX);
            planetSphere.setTranslateY(0);
            planetSphere.setTranslateZ(initialZ);
        
            planetLabel.setTranslateX(initialX);
            planetLabel.setTranslateY(-planet.getRadius() - 20);
            planetLabel.setTranslateZ(initialZ);

            Rotate planetRotate = new Rotate(0, Rotate.Y_AXIS);
            planetSphere.getTransforms().add(planetRotate);
        
            planetsGroup.getChildren().addAll(planetSphere, planetLabel);
    
            planetSpheres.put(planet.getName(), planetSphere);
            planetLabels.put(planet.getName(), planetLabel);
            planetRotations.put(planet.getName(), planetRotate);

        }
    
        private void deletePlanet(Planet planet, Group planetsGroup) {
            //retrieves thes sphere and label
            //if its not null then remove it from the planets group, and spheres/labels group
            //removes the it from the angles and rotation hashmaps
            Sphere sphere = planetSpheres.get(planet.getName());
            Text label = planetLabels.get(planet.getName());
    
            if (sphere != null) {
                planetsGroup.getChildren().remove(sphere);
                planetSpheres.remove(planet.getName());
            }
    
            if (label != null) {
                planetsGroup.getChildren().remove(label);
                planetLabels.remove(planet.getName());
            }
    
            angles.remove(planet.getName());
            rotations.remove(planet.getName());
        }
    
        public void setCsvFilePath(String csvFilePath) {
            this.csvFilePath = csvFilePath;
        }

    private List<Planet> loadPlanetsFromFile(String fileName) {
        //creates an arraylist for the planets
        //creates an array list of all the things in the specified csv file
        List<Planet> planets = new ArrayList<>();
        ArrayList<String> lines = MyFile.readFile(fileName);
    
        //loops through the arrraylist of lines and pulls the data, separates the line by commas and puts the indivual data in coresponding variables
        //then create a new planet object for each and then add that to the planets array. return the array
        for (int i = 1; i < lines.size(); i++) {
            String[] data = lines.get(i).split(",");
            String name = data[0];
            double radius = Double.parseDouble(data[1]);
            double semiMajorAxis = Double.parseDouble(data[2]);
            double eccentricity = Double.parseDouble(data[3]);
            double rotationSpeed = Double.parseDouble(data[4]);
            double orbitSpeed = Double.parseDouble(data[5]);
            String imagePath = data[6];
    
            Planet planet = new Planet(name, radius, semiMajorAxis, eccentricity, rotationSpeed, orbitSpeed, imagePath);
            planets.add(planet);
        }
        return planets;
    }

    //formula i made so when people add planets, the planets far away will have a big enough label to see it
    private double calculateFontSize(double semiMajorAxis) {
        return 130 + (semiMajorAxis / 5000) * 100;
    }    


    //just creates an image object using the path given.
    //if the image is corrupted or something it will stop it from breaking the program and just skip it
    private Image loadImage(String path, String description) {
        try {
            Image image = new Image(path);
            return image;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //just creates a sphere and adds the color
    private Sphere createSphere(int radius, Color color) {
        Sphere sphere = new Sphere(radius);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(color);
        sphere.setMaterial(material);
        return sphere;
    }

    //takes in the 3d scene, camera, 3d scenes group and the list of orbiting images
    private void initMouseControl(Scene mainScene, Camera camera, Group group) {
        //creates a rotate object for x and y
        Rotate rotateX = new Rotate();
        Rotate rotateY = new Rotate();

        //applies them to the group so it affects all of the images
        group.getTransforms().addAll(rotateX, rotateY);

        //sets the axis
        rotateX.setAxis(Rotate.X_AXIS);
        rotateY.setAxis(Rotate.Y_AXIS);

        //when you click your mouse it stores where you clicked as well and the angles of the rotate objects
        mainScene.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = rotateX.getAngle();
            anchorAngleY = rotateY.getAngle();
        });

        //once you drag it, it will find the delta by recording the current x,y values minus the x,y where you initially clicked
        //changes the angle by adding or minusing the delta from the initial angle. + or - just determines if its inverted. 0.3 is just the intensity factor
        mainScene.setOnMouseDragged(event -> {
        double deltaX = event.getSceneX() - anchorX;
        double deltaY = event.getSceneY() - anchorY;

        rotateX.setAngle(anchorAngleX + deltaY * 0.3);
        rotateY.setAngle(anchorAngleY - deltaX * 0.3);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
