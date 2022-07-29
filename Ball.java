package BounsingBallsGame;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;


public class Ball {
    private final DoubleProperty dx;     //Pixels per second on the X axis
    private final DoubleProperty dy;     //Pixels per second on the Y axis
    private final double radius;         //Radius in pixels

    //Creating ball image
    private final Circle BallBody;
    //Constructor which sets a ball with coordinates for center and velocity
    public Ball(double centerX, double centerY, double radius,
                double xVelocity, double yVelocity) {

        this.BallBody = new Circle(centerX, centerY, radius);
        this.dx = new SimpleDoubleProperty(this, "dx", xVelocity);
        this.dy = new SimpleDoubleProperty(this, "dy", yVelocity);
        this.radius = radius;
        BallBody.setRadius(radius);
    }
    //Getters and Setters
    protected double getRadius() {
        return radius;
    }

    protected final double getXVelocity() {
        return dx.get();
    }

    protected final void setXVelocity(double xVelocity) {
        this.dx.set(xVelocity);
    }

    protected final double getYVelocity() {
        return dy.get();
    }

    protected final void setYVelocity(double yVelocity) {
        this.dy.set(yVelocity);
    }

    protected final double getCenterX() {
        return BallBody.getCenterX();
    }

    protected final void setCenterX(double centerX) {
        BallBody.setCenterX(centerX);
    }

    protected final double getCenterY() {
        return BallBody.getCenterY();
    }

    protected final void setCenterY(double centerY) {
        BallBody.setCenterY(centerY);
    }

    protected final double getMinimumY(){
        return BallBody.getBoundsInParent().getMinY();
    }
    protected final double getMinimumX(){
        return BallBody.getBoundsInParent().getMinX();
    }
    protected final double getMaximumY(){
        return BallBody.getBoundsInParent().getMaxY();
    }
    protected final double getMaximumX(){
        return BallBody.getBoundsInParent().getMaxX();
    }

    protected final double getOtherCenterX(){
        return BallBody.getBoundsInParent().getCenterX();
    }

    protected final double getOtherCenterY(){
        return BallBody.getBoundsInParent().getCenterY();
    }
    protected Shape getBallBody() {
        return BallBody;
    }
}
