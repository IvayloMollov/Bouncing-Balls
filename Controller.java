package BounsingBallsGame;


import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.*;
import static javafx.scene.paint.Color.*;


public class Controller implements Initializable {

    private static final int numberOfStartingBalls = 1; //Number of starting balls
    private static final boolean randomness = true;
    private static Color mColor = Color.BLUE;
    private boolean stopped = false;                    //Keeps the state of the animation running / stopped
    private static int numberofBalls = 1;
    private static double sChosenradius = 50.0;         //Radius of all balls
    private static int mLapse = 10;
    private ObservableList<Ball> arrayOfBalls = FXCollections.observableArrayList();
    public static ExecutorService threadPool;              //Threadpool for our threads

    private static final Color firstColorFill = BLUEVIOLET;//Colort of the first ball

    @FXML
    public Pane ballPane;        // Bouncing field for all balls
    @FXML
    private Button btnStartBall; // Button to add additional balls to the
    @FXML
    private Label lblBallCount;  //Label to display all balls in play

    @FXML
    private Button btnQuit;       // Button to terminate the game

    @FXML
    private Button btnStop;       // Button to suspend the animation

    @FXML
    private ColorPicker clrPck;   // User-friendly way os setting colors

    @FXML
    private Slider sldSpeed;      //Slider to control the speed of the next ball

    @FXML
    private Rectangle rect;       // Vertical segment, obstacle

    private double clickX;
    private double clickY;          // Stores the coordinates of on-click event in the drawing area

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //override the initialize method for the javaFx Controller class
        threadPool = Executors.newCachedThreadPool();

        clrPck.setValue(YELLOW);

        lblBallCount.setText( String.valueOf(numberofBalls));
        lblBallCount.setVisible(true);

        btnStartBall.setText("Start");
        btnStartBall.addEventHandler(MouseEvent.MOUSE_CLICKED,
                event -> { //Event for the button start to add e new ball
                    if (event.getClickCount() >= 1) {
                        int singleBall = 1;
                        boolean notRandom = false;
                        //Increase the numberofBalls by one, which gets reflected in the label displayed on the toolbar
                        numberofBalls += 1;
                        mColor = clrPck.getValue();
                        Color colorLook = mColor;
                        final double speed = 10 * sldSpeed.getValue();
                        createBalls(notRandom, colorLook, singleBall,
                                speed, clickX, clickY);
                    }
                });


        ballPane.addEventHandler(MouseEvent.MOUSE_CLICKED,
                event -> { // Event which stores the coordinates of a clicked point in the bouncing area
                    if (event.getClickCount() == 1) {
                        if(event.getX() < rect.getBoundsInParent().getMaxX()
                                && event.getX() > rect.getBoundsInParent().getMinX()
                                && event.getY() > rect.getBoundsInParent().getMinX()){
                            clickX = ballPane.getWidth() / 2;
                            clickY = ballPane.getHeight() / 2;
                        } else {
                            clickX = event.getX();
                            clickY = event.getY();
                        }
                    }
                });



        //This method adds the new ball to the pane as soon as the instantiated ball gets added to the arraylist
        arrayOfBalls.addListener(new ListChangeListener<Ball>() {
            @Override
            public void onChanged(Change<? extends Ball> change) {
                while (change.next()) {
                    change.getAddedSubList().stream().forEach((ball) -> {
                        ballPane.getChildren().add(ball.getBallBody());
                    });
                    change.getRemoved().stream().forEach((ball) -> {
                        ballPane.getChildren().remove(ball.getBallBody());
                    });
                }
            }
        });

        createBalls(randomness, firstColorFill, numberOfStartingBalls, 400,
                ballPane.getWidth() / 2, ballPane.getHeight() / 2);
        startAnimation(ballPane);

    }


    AnimationTimer timer;           // Creating out animation timer
    //This method initiates the animation of the bouncing balls
    private void startAnimation(final Pane ballContainer) {
        final LongProperty lastFrameTime = new SimpleLongProperty(0);
         timer = new AnimationTimer() {
            @Override
            public void handle(long timestamp) {
                try{
                    //this code determines the time lapse between animation. extending the duration
                    //results in a delay between animation frames, and thus creates a choppier appearance
                    Thread.sleep(mLapse);
                }
                catch(Exception event){}
                if (lastFrameTime.get() > 0) {
                    long elapsedTime = timestamp - lastFrameTime.get();

                    detectCollision(ballContainer.getWidth(), ballContainer.getHeight());
                    updateFrame(elapsedTime);
                }
                lastFrameTime.set(timestamp);
            }

        };
        timer.start();
    }

    private void updateFrame(long elapsedTime) { // Function which updates the drawing feild every frame
        lblBallCount.setText( String.valueOf(numberofBalls));

        double elapsedSeconds = elapsedTime / 1_000_000_000.0;
        arrayOfBalls.stream().forEach((ball) -> { // Changes the position of a ball according to its velocity along  X and Y axis
            ball.setCenterX(ball.getCenterX() + elapsedSeconds * ball.getXVelocity());
            ball.setCenterY(ball.getCenterY() + elapsedSeconds * ball.getYVelocity());
        });
    }

    private void detectCollision(double maxX, double maxY){
        //Function which detects contact with surfaces
        //detect collision begins by checking for collisions with the pane boundaries, this is what the parameters are used for
        //for loop is preferred to stream as we iterate through each ball in the arraylist
        for (int thisBall = 0; thisBall < arrayOfBalls.size(); thisBall++) { // has a time complexity of n^2 because we compare each individual ball to all other balls
            final int ballNumber = thisBall;
            //Using ExecutorService for managing the pool of threads
            threadPool.execute(() -> {

                Ball ball1 = arrayOfBalls.get(ballNumber);

                //begin by checking balls position relative to the pane's boundaries
                boundariesCollision(ball1, maxX, maxY);
                rectCollision(ball1, rect);
                for (int nextBall = ballNumber + 1; nextBall < arrayOfBalls.size(); nextBall++ ) {

                    Ball ball2 = arrayOfBalls.get(nextBall);

                    //Getting the differences between centers of each of two balls, will be used to check for intersection

                    final double deltaX = ball2.getCenterX() - ball1.getCenterX();
                    final double deltaY = ball2.getCenterY() - ball1.getCenterY();

                    //Check for collision between ball1 and ball2
                    if (colliding(ball1, ball2, deltaX, deltaY)) {
                        bounce(ball1, ball2, deltaX, deltaY);
                    }
                }
            });
        }




    }

    private void rectCollision(Ball ball1, Rectangle rect) { //detects intersection with the vertical segment
        double xVel = ball1.getXVelocity();
        double yVel = ball1.getYVelocity();  //storing the initial velocity of a ball

        double xMinBall = ball1.getMinimumX();
        double yMinBall = ball1.getMinimumY();
        double xMaxBall = ball1.getMaximumX();
        double yMaxBall = ball1.getMaximumY(); // getting most- right, left, up and down point of e circle

        double xMinRect = rect.getBoundsInParent().getMinX();
        double yMinRect = rect.getBoundsInParent().getMinY();
        double xMaxRect = rect.getBoundsInParent().getMaxX();
        double yMaxRect = rect.getBoundsInParent().getMaxY();  // due to the figure being rectangle, max coordinates will always be on one of its borders
        if ((ball1.getCenterY() - ball1.getRadius() <= yMaxRect && yVel < 0) // contact with the lower border
                && ball1.getOtherCenterX() >= xMinRect
                && ball1.getOtherCenterX() <= xMaxRect){
            ball1.setYVelocity(-yVel);
        }
        if ((ball1.getCenterX() + ball1.getRadius() >= xMinRect && xVel > 0) // contact with the left border
                && ball1.getOtherCenterY() < yMaxRect
                && ball1.getOtherCenterX() < xMinRect){
            ball1.setXVelocity(-xVel);
        }
        if ((ball1.getCenterX() - ball1.getRadius() <= xMaxRect && xVel < 0) // contact with the right border
                && ball1.getOtherCenterY() < yMaxRect
                && ball1.getOtherCenterX() > xMaxRect){
            ball1.setXVelocity(-xVel);
        }
    }

    //this method is called by detect collision method above to see if a ball has encountered the frame boundaries
    private void boundariesCollision(Ball ball1, double maxX, double maxY){
        double xVel = ball1.getXVelocity();
        double yVel = ball1.getYVelocity();
        //check the sides
        if ((ball1.getCenterX() - ball1.getRadius() <= 0 && xVel < 0)
                || (ball1.getCenterX() + ball1.getRadius() >= maxX && xVel > 0)) {
            ball1.setXVelocity(-xVel);
        }
        //check the top and bottom of the pane
        if ((ball1.getCenterY() - ball1.getRadius() <= 0 && yVel < 0)
                || (ball1.getCenterY() + ball1.getRadius() >= maxY && yVel > 0)) {
            ball1.setYVelocity(-yVel);
        }

    }

    public boolean colliding(final Ball b1, final Ball b2, final double deltaX, final double deltaY) {

        final double radiusSum = b1.getRadius() + b2.getRadius();
        if (deltaX * deltaX + deltaY * deltaY <= radiusSum * radiusSum) {
            if (deltaX * (b2.getXVelocity() - b1.getXVelocity())
                    + deltaY * (b2.getYVelocity() - b1.getYVelocity()) < 0) {
                return true;
            }
        }
        return false;
    }

    //Function which bounces ball off of one an other
    private void bounce(final Ball ball1, final Ball ball2, final double deltaX, final double deltaY) {
        final double distance = sqrt(deltaX * deltaX + deltaY * deltaY);
        final double unitContactX = deltaX / distance;
        final double unitContactY = deltaY / distance;

        final double xVelocity1 = ball1.getXVelocity();
        final double yVelocity1 = ball1.getYVelocity();
        final double xVelocity2 = ball2.getXVelocity();
        final double yVelocity2 = ball2.getYVelocity();

        final double u1 = xVelocity1 * unitContactX + yVelocity1 * unitContactY;
        final double u2 = xVelocity2 * unitContactX + yVelocity2 * unitContactY;

        //This is an implementation of the Elastic Headon Collision equation
        final double v1 = (2 * u2 + u1);
        final double v2 = (2 * u1 - u2);

        final double u1PerpX = xVelocity1 - u1 * unitContactX;
        final double u1PerpY = yVelocity1 - u1 * unitContactY;
        final double u2PerpX = xVelocity2 - u2 * unitContactX;
        final double u2PerpY = yVelocity2 - u2 * unitContactY;

        // setting random velocity after a rebount
        int v = (int)(Math.random() * 100) + 100;

        // changing the velocity
        ball1.setXVelocity(v * unitContactX + u1PerpX);
        ball1.setYVelocity(v * unitContactY + u1PerpY);
        ball2.setXVelocity(v * unitContactX + u2PerpX);
        ball2.setYVelocity(v * unitContactY + u2PerpY);

    }

    //This method uses the Ball class to add new arrayOfBalls to the arrayOfBalls array. The color, number of arrayOfBalls,
    //speed, and position are all parameters here.
    private void createBalls(boolean multipleBalls, Color colorAppearance, int numBalls,
                             double speed, double initialX, double initialY) {
        // The if statement checks the first boolean parameter. If it's true, it will result in 1 arrayOfBalls being added.
        // The else statement below is called when adding a single ball where color and speed are inputs provided by the user
        if (multipleBalls == true) {
            final Random range = new Random();

            for (int thisBall = 0; thisBall < numBalls; thisBall++) {
                double radius = 40;

                final double angle = 2 * PI * range.nextDouble();
                Ball ball = new Ball(initialX, initialY, radius, speed * cos(angle),
                        speed * sin(angle));//, mass
                ball.getBallBody().setFill(CYAN);
                arrayOfBalls.add(ball);
            }
        } else {
            //this portion of code is called when a single ball gets added by the user
            final Random range = new Random();
            double radius = 40;

            final double angle = 2 * PI * range.nextDouble();
            Ball ball = new Ball(initialX, initialY, radius, speed * cos(angle),
                    speed * sin(angle));
            ball.getBallBody().setFill(colorAppearance);
            arrayOfBalls.add(ball);
        }
    }

    // method which terminates all threads
    public static void shutdownThreads(ExecutorService threadPool) {
        Platform.exit();
        System.exit(0);

        threadPool.shutdown(); // Disable new tasks from being submitted
        try {

            // Wait a while for existing tasks to terminate
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!threadPool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            threadPool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
    // button to halt the movment of all balls and release it on e second click
    public void btnStopOnAction(javafx.event.ActionEvent actionEvent) throws InterruptedException{
        if(stopped){
            timer.start();
            stopped = false;
        } else{
            stopped = true;
            timer.stop();
        }
    }
    // terminates the game
    public void btnQuitOnAction(javafx.event.ActionEvent actionEvent) {
        System.exit(0);
    }
}
















