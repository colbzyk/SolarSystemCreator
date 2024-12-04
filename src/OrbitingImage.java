import javafx.scene.image.ImageView;
import javafx.scene.transform.Rotate;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.image.Image;

public class OrbitingImage {

    //creates variables for build in javafx image view to store the image
    //the specific images group, its random angle, radius, tilt angle and x,y,z
    private ImageView imageView;
    private Group group;
    private double angle = Math.random() * 360; 
    private double orbitRadius;
    private double tiltAngle; 
    private double x, y, z;

    //assigns the values to a specifc image object
    //applies the tilt on the z_axis so that its path is different than another image, then adds it to the group
    public OrbitingImage(Image image, double orbitRadius) {
        this.orbitRadius = orbitRadius;

        this.tiltAngle = Math.random() * 360;

        this.imageView = new ImageView(image);
        this.imageView.setFitWidth(20);
        this.imageView.setFitHeight(20);

        this.group = new Group(imageView);

        Rotate tilt = new Rotate(tiltAngle, Rotate.Z_AXIS);
        this.group.getTransforms().add(tilt);
    }

    public Group getGroup() {
        return group;
    }

    public ImageView getImageView() {
        return imageView;
    }

    //calculates the 3d coordinates of image based on its orbit radius and assigned angle using rotation matrix
    //using 2d rotation formula, the tilt is calcuated and applied
    public void updatePosition() {
        this.angle += 0.01;

        x = orbitRadius * Math.cos(angle);
        y = orbitRadius * Math.sin(angle);
        z = orbitRadius * Math.sin(angle);

        double xTilted = x * Math.cos(Math.toRadians(tiltAngle)) - z * Math.sin(Math.toRadians(tiltAngle));
        double zTilted = x * Math.sin(Math.toRadians(tiltAngle)) + z * Math.cos(Math.toRadians(tiltAngle));

        group.setTranslateX(xTilted);
        group.setTranslateY(y);
        group.setTranslateZ(zTilted);



    }

    //gets yaw and pictch from the delta, 0.2 is just a sensitivity factor
    //rotates the objects for the amount specified in yaw and pitch variables on the specified axis.
    //clears the transforms because it will not remain stable adding values constantly
    //add the yaw and pitch to group to orient the images towards the camera
    public void updateOrientationToFaceCamera(double deltaX, double deltaY) {
        double yaw = deltaX * 0.2;  
        double pitch = deltaY* 0.2;
    
        Rotate rotateYaw = new Rotate(yaw, Rotate.Y_AXIS);
        Rotate rotatePitch = new Rotate(pitch, Rotate.X_AXIS);
    
        group.getTransforms().clear();
        group.getTransforms().addAll(rotateYaw, rotatePitch);
    }
    
}
