package BounsingBallsGame;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        Scene scene = new Scene(root);
        stage.setTitle("Bouncing balls game");
        stage.setScene(scene);
        stage.show();
    }

    //The stop method is the last method to be called in the life-cycle of the Application class. Instead of simply
    //stopping a thread, we have to notify it that it should be terminated. The shutdown termination method
    //defined in the BallController class is doing this for all threads in the thread pool

    @Override
    public void stop() {
        Controller.shutdownThreads(Controller.threadPool);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
