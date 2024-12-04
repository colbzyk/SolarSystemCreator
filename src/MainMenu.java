import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.transform.Rotate;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.animation.AnimationTimer;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.util.Duration;

public class MainMenu extends Application {
    //These values are used to store cursor values to move the 3d scene around
    private double anchorX, anchorY, anchorAngleX, anchorAngleY;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        final int EARTH_RADIUS = 100;
        final int WIDTH = 1000;
        final int HEIGHT = 643;
        final double ORBIT_RADIUS = 125;
        


        Sphere earth = new Sphere(EARTH_RADIUS);

        Image diffuseMap = loadImage("file:earth/earth-d.jpg", "Daytime texture");
        Image specularMap = loadImage("file:earth/earth-n.jpg", "Specular map");
        Image normalMap = loadImage("file:earth/earth-s.jpg", "Normal map");
        Image emissiveMap = loadImage("file:earth/earth-l.jpg", "City lights map");
        Image galaxyBackground = loadImage("file:earth/galaxy4.jpg", "Galaxy background");

        //Add the maps to make the sphere look like earth
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(diffuseMap);
        material.setSpecularMap(specularMap);
        material.setBumpMap(normalMap);
        material.setSelfIlluminationMap(emissiveMap);
        earth.setMaterial(material);

        //some lights
        AmbientLight ambientLight = new AmbientLight();
        ambientLight.setColor(Color.rgb(255, 255, 255, 0.3));

        PointLight pointLight = new PointLight();
        pointLight.setColor(Color.rgb(255, 255, 255, 0.3));
        pointLight.setTranslateX(0);
        pointLight.setTranslateY(-50);
        pointLight.setTranslateZ(-300);

        earth.setTranslateX(0);
        earth.setTranslateY(0);
        earth.setTranslateZ(0);

        //makes it it rotate infinetely on its y-axis 360 degrees
        //interpolator just makes it speed linear, so its always rotating at the same speed
        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(10), earth);
        rotateTransition.setAxis(Rotate.Y_AXIS);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(RotateTransition.INDEFINITE);
        rotateTransition.setInterpolator(Interpolator.LINEAR);
        rotateTransition.play();

        //add jpg or png images to a File aray
        //if its not null, loop through them and creates an image object for each using loadimage method
        //if the image is not null, sends it to my orbiting image function
        //add the image to the orbiting images arraylist
        List<OrbitingImage> orbitingImages = new ArrayList<>();
        File imagesFolder = new File("images");
        File[] imageFiles = imagesFolder.listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png"));

        if (imageFiles != null) {
            for (File file : imageFiles) {
                Image image = loadImage("file:" + file.getPath(), "Image " + file.getName());
                if (image != null) {
                    OrbitingImage orbitingImage = new OrbitingImage(image, ORBIT_RADIUS);
                    orbitingImages.add(orbitingImage);
                }
            }
        }

        //combine the earth and lights into a group
        //adds the images which are in their own indivual group to the MAIN group
        Group group = new Group(earth, ambientLight, pointLight);
        orbitingImages.forEach(orbitingImage -> group.getChildren().add(orbitingImage.getGroup()));

        //this is just to make the images face the camera one by one. 
        for (OrbitingImage oi : orbitingImages) {
            oi.updateOrientationToFaceCamera(0, 0);
        }

        //updates the position of each image one by one every frame
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                for (OrbitingImage orbitingImage : orbitingImages) {
                    orbitingImage.updatePosition();
                }
            }
        }.start();

        //creates the subscene for the 3d part of the main menu
        SubScene subScene = new SubScene(group, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED);
        Camera camera = new PerspectiveCamera(true);
        subScene.setCamera(camera);

        //set the camera for viewing in the 3d space
        camera.setTranslateZ(-700);
        camera.setNearClip(1);
        camera.setFarClip(10000);

        //this calls the method which moves the 3d space around
        initMouseControl(subScene, camera, group, orbitingImages);

        //the stack pane is over all the panes.
        //it first adds the background
        //second adds the subscene
        //third ads all of the labels, buttons, and vboxes
        StackPane root = new StackPane();

        ImageView galaxyImageView = new ImageView(galaxyBackground);
        galaxyImageView.setFitWidth(WIDTH);
        galaxyImageView.setFitHeight(HEIGHT);
        root.getChildren().add(galaxyImageView);

        root.getChildren().add(subScene);

        Label titleLabel = new Label("Solar System Creator");
        titleLabel.setStyle("-fx-font-size: 36; -fx-text-fill: white; -fx-font-weight: bold;");
        StackPane.setAlignment(titleLabel, javafx.geometry.Pos.TOP_CENTER);
        StackPane.setMargin(titleLabel, new Insets(20, 0, 0, 0));
        root.getChildren().add(titleLabel);

        Button startButton = new Button("Start");
        startButton.setStyle("-fx-font-size: 16; -fx-padding: 5; -fx-min-width: 150;");
        StackPane.setAlignment(startButton, Pos.CENTER);
        StackPane.setMargin(startButton, new Insets(125, 0, 0, 0));
        root.getChildren().add(startButton);

        Button infoButton = new Button("Controls and Info");
        infoButton.setStyle("-fx-font-size: 16; -fx-padding: 5; -fx-min-width: 150;");
        StackPane.setAlignment(infoButton, Pos.CENTER);
        StackPane.setMargin(infoButton, new Insets(225, 0, 0, 0));
        root.getChildren().add(infoButton);

        Button exitButton = new Button("Exit");
        exitButton.setStyle("-fx-font-size: 16; -fx-padding: 5; -fx-min-width: 150;");
        StackPane.setAlignment(exitButton, Pos.CENTER);
        StackPane.setMargin(exitButton, new Insets(325, 0, 0, 0));
        root.getChildren().add(exitButton);

        Label footerLabel = new Label("Created By Colby");
        footerLabel.setStyle("-fx-font-size: 14; -fx-text-fill: white;");
        StackPane.setAlignment(footerLabel, javafx.geometry.Pos.BOTTOM_CENTER);
        StackPane.setMargin(footerLabel, new Insets(0, 0, 20, 0));
        root.getChildren().add(footerLabel);

        //vbox is used to make another menu appear, it does this by setVisible()
        VBox menuBox = new VBox(10);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(20));
        menuBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-border-color: white; -fx-border-width: 2;");
        menuBox.setVisible(false);

        root.getChildren().add(menuBox);

        startButton.setOnAction(e -> {
            //starts by making it visible and clearing the children that have been added
            menuBox.setVisible(true);
            menuBox.getChildren().clear();

            Label fileLabel = new Label("Select or Create a Data File");
            fileLabel.setStyle("-fx-font-size: 18; -fx-text-fill: white;");
            menuBox.getChildren().add(fileLabel);

            //creates a combobox and takes csv files from "data" folder and adds to it
            ComboBox<String> fileDropdown = new ComboBox<>();
            File dataFolder = new File("data");

            File[] csvFiles = dataFolder.listFiles((dir, name) -> name.endsWith(".csv"));
            if (csvFiles != null) {
                for (File file : csvFiles) {
                    fileDropdown.getItems().add(file.getName());
                }
            }
            fileDropdown.setPromptText("Select existing file");
            fileDropdown.setStyle("-fx-min-width: 150;");
            menuBox.getChildren().add(fileDropdown);

            Button createNewFileButton = new Button("Create New File");
            createNewFileButton.setStyle("-fx-min-width: 150;");
            menuBox.getChildren().add(createNewFileButton);

            Button proceedButton = new Button("Proceed");
            proceedButton.setStyle("-fx-min-width: 150;");
            menuBox.getChildren().add(proceedButton);

            Button cancel = new Button("Cancel");
            cancel.setStyle("-fx-min-width: 150;");
            menuBox.getChildren().add(cancel);

            createNewFileButton.setOnAction(event -> {
                //removes any children in the box and creates a label and textfield
                menuBox.getChildren().clear();
                Label createFileLabel = new Label("Enter a name for the new data file:");
                createFileLabel.setStyle("-fx-font-size: 18; -fx-text-fill: white;");
                menuBox.getChildren().add(createFileLabel);

                TextField fileNameField = new TextField();
                fileNameField.setStyle("-fx-max-width: 200;");
                fileNameField.setPromptText("Enter file name");
                menuBox.getChildren().add(fileNameField);

                Button proceedWithNewFileButton = new Button("Proceed");
                proceedWithNewFileButton.setStyle("-fx-min-width: 150;");
                menuBox.getChildren().add(proceedWithNewFileButton);

                Button backButton = new Button("Back");
                backButton.setStyle("-fx-min-width: 150;");
                menuBox.getChildren().add(backButton);

                //if back button is selected, then it clears the child
                backButton.setOnAction(backEvent -> {
                    menuBox.getChildren().clear();
                    menuBox.getChildren().add(fileLabel);
                    menuBox.getChildren().add(fileDropdown);
                    menuBox.getChildren().add(createNewFileButton);
                    menuBox.getChildren().add(proceedButton);
                    menuBox.getChildren().add(cancel);
                });

                proceedWithNewFileButton.setOnAction(proceedEvent -> {
                    //gets the text from the textfield and trims it
                    //makes sure its a csv if not, it adds .csv
                    //combines the folder and filename and checks if it doesn't exist
                    //calls createnewcsvfile and statsolarsystemscene 
                    //gives alerts if other in the else statements
                    String fileName = fileNameField.getText().trim();
                    if (!fileName.isEmpty()) {
                        if (!fileName.endsWith(".csv")) {
                            fileName += ".csv";
                        }
                        File newFile = new File(dataFolder + "/" + fileName);
                        if (!newFile.exists()) {
                            createNewCsvFile(newFile);
                            startSolarSystemScene(primaryStage, "data/" + fileName);
                        } else {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("File Exists");
                            alert.setHeaderText(null);
                            alert.setContentText("A file with this name already exists.");
                            alert.showAndWait();
                        }
                    } else {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Invalid Name");
                        alert.setHeaderText(null);
                        alert.setContentText("Please enter a valid file name.");
                        alert.showAndWait();
                    }
                });
            });

            proceedButton.setOnAction(event -> {
                //if you click proceed after selecting an existing file, it gets it from the filedropdown
                //then it adds the folder directory to the name of the file and sends it to startsolarsystemscene method
                String selectedFile = fileDropdown.getValue();
                if (selectedFile != null && !selectedFile.isEmpty()) {
                    startSolarSystemScene(primaryStage, "data/" + selectedFile);
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("No File Selected");
                    alert.setHeaderText(null);
                    alert.setContentText("Please select or create a data file.");
                    alert.showAndWait();
                }
            });

            //makes the vbox invisible if you cancel
            cancel.setOnAction(event -> {
                menuBox.setVisible(false);
            });
        });

        //same thing as before, creates a vbox makes it invisible and then creates labels for everything i want the person to see
        VBox infoBox = new VBox(10);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setPadding(new Insets(20));
        infoBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-border-color: white; -fx-border-width: 2;");
        infoBox.setVisible(false);

        root.getChildren().add(infoBox);
        
        infoButton.setOnAction(e -> {
            infoBox.getChildren().clear();
        
            Label controlsLabel = new Label("Controls:");
            controlsLabel.setStyle("-fx-font-size: 16; -fx-text-fill: white; -fx-font-weight: bold;");
        
            Label zoomLabel = new Label(" - Use 'W' to zoom in.");
            zoomLabel.setStyle("-fx-font-size: 14; -fx-text-fill: white;");
        
            Label unzoomLabel = new Label(" - Use 'S' to zoom out.");
            unzoomLabel.setStyle("-fx-font-size: 14; -fx-text-fill: white;");
        
            Label rotateLabel = new Label(" - Click and drag to rotate the 3D space.");
            rotateLabel.setStyle("-fx-font-size: 14; -fx-text-fill: white;");
        
            Label additionalInfoLabel = new Label("Additional Information:");
            additionalInfoLabel.setStyle("-fx-font-size: 16; -fx-text-fill: white; -fx-font-weight: bold;");
        
            Label saveLabel = new Label(" - Changes are automatically saved to the corresponding CSV file in the 'data' folder.");
            saveLabel.setStyle("-fx-font-size: 14; -fx-text-fill: white;");
        
            Button backButton = new Button("back");
            backButton.setStyle("-fx-font-size: 14; -fx-padding: 5;");
            backButton.setOnAction(event -> infoBox.setVisible(false));
        
            infoBox.getChildren().addAll(controlsLabel, zoomLabel, unzoomLabel, rotateLabel, additionalInfoLabel, saveLabel, backButton);
        
            infoBox.setVisible(true);
        });

        exitButton.setOnAction(e ->{
            primaryStage.close();
        });
        
        Scene mainMenuScene = new Scene(root, WIDTH, HEIGHT, true);

        primaryStage.setTitle("Main Menu");
        primaryStage.setScene(mainMenuScene);
        primaryStage.show();


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

    //takes in the 3d scene, camera, 3d scenes group and the list of orbiting images
    private void initMouseControl(SubScene scene, Camera camera, Group group, List<OrbitingImage> orbitingImages) {
        //creates a rotate object for x and y
        Rotate rotateX = new Rotate();
        Rotate rotateY = new Rotate();

        //applies them to the group so it affects all of the images
        group.getTransforms().addAll(rotateX, rotateY);

        //sets the axis
        rotateX.setAxis(Rotate.X_AXIS);
        rotateY.setAxis(Rotate.Y_AXIS);

        //when you click your mouse it stores where you clicked as well and the angles of the rotate objects
        scene.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = rotateX.getAngle();
            anchorAngleY = rotateY.getAngle();

        });

        //once you drag it, it will find the delta by recording the current x,y values minus the x,y where you initially clicked
        //changes the angle by adding or minusing the delta from the initial angle. + or - just determines if its inverted. 0.3 is just the intensity factor
        //calls the updateOrientationToFaceCamera method in orbiting image one by one using the delta values
        scene.setOnMouseDragged(event -> {
            double deltaX = event.getSceneX() - anchorX;
            double deltaY = event.getSceneY() - anchorY;

            rotateX.setAngle(anchorAngleX + deltaY * 0.3);
            rotateY.setAngle(anchorAngleY - deltaX * 0.3);

            for (OrbitingImage oi : orbitingImages) {
                oi.updateOrientationToFaceCamera(deltaX, deltaY);
            }
        });
    }

    //print writer is built in and will create a file if it doesn't exist automatically.
    //then i just write the header to the file
    private void createNewCsvFile(File file) {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("Name,Radius,SemiMajorAxis,Eccentricity,RotationSpeed,OrbitSpeed,ImagePath");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //This just initalizes the solar system class and i pass the csv path to the setCsvFilePath method
    //then i just start with the primarystage
    private void startSolarSystemScene(Stage primaryStage, String csvFilePath) {
        SolarSystem solarSystem = new SolarSystem();
        try {
            solarSystem.setCsvFilePath(csvFilePath);
            solarSystem.start(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    
}
