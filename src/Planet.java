public class Planet {
    private String name;
    private double radius;
    private double semiMajorAxis;
    private double eccentricity;
    private double rotationSpeed;
    private double orbitSpeed;
    private String imagePath;

    //nothing fancy, just something to create planet objects and pull the data of each planet when needed.
    public Planet(String name, double radius, double semiMajorAxis, double eccentricity, double rotationSpeed, double orbitSpeed, String imagePath) {
        this.name = name;
        this.radius = radius;
        this.semiMajorAxis = semiMajorAxis;
        this.eccentricity = eccentricity;
        this.rotationSpeed = rotationSpeed;
        this.orbitSpeed = orbitSpeed;
        this.imagePath = imagePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getSemiMajorAxis() {
        return semiMajorAxis;
    }

    public void setSemiMajorAxis(double semiMajorAxis) {
        this.semiMajorAxis = semiMajorAxis;
    }

    public double getEccentricity() {
        return eccentricity;
    }

    public void setEccentricity(double eccentricity) {
        this.eccentricity = eccentricity;
    }

    public double getRotationSpeed() {
        return rotationSpeed;
    }

    public void setRotationSpeed(double rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    public double getOrbitSpeed() {
        return orbitSpeed;
    }

    public void setOrbitSpeed(double orbitSpeed) {
        this.orbitSpeed = orbitSpeed;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
}