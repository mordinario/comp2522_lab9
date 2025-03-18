import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Main.java. It's a quiz! TODO; this. later
 *
 * @author Marcy Ordinario
 * @author Ryan Chu
 * @version 1.0
 */
public class Main
    extends Application
{
    private static final Path                QUIZ_PATH = Paths.get("src", "res", "quiz.txt");
    private static final Map<String, String> QUESTION_MAP;
    private static final List<String>        QUESTION_LIST;
    private static final Random              RAND = new Random();

    static
    {
        // Throw if quiz questions don't exist
        if(Files.notExists(QUIZ_PATH))
        {
            throw new RuntimeException("Quiz question file not found");
        }

        // Try to create questions from the .txt file;
        // throw if exception occurs
        try
        {
            final List<String> quizList = Files.readAllLines(QUIZ_PATH);

            // Instantiate HashMap
            QUESTION_MAP = new HashMap<>();

            // ONLY add questions/answers to the HashMap
            // if there's a "|" character that separates them -
            // if not, then that line's in an invalid format
            // and will be ignored
            quizList.stream()
                    .filter(s->s.contains("|"))
                    .forEach(s -> QUESTION_MAP
                            .put(s.substring(0, s.indexOf("|")),
                                 s.substring(s.indexOf("|") + 1)));

            // Get an array list from the keys
            QUESTION_LIST = new ArrayList<>(QUESTION_MAP.keySet());
        } catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Runs the program.
     *
     * @param args unused for this program
     */
    public static void main(String[] args)
    {
        launch(args);
    }

    /**
     * Starts. the. uh. ngl idk what to put here TODO;
     *
     * @param primaryStage Stage to show
     * @throws Exception If an exception occurs
     */
    @Override
    public void start(final Stage primaryStage)
    throws Exception
    {
        primaryStage.setScene(menuScene(primaryStage,
                                        400, 300));
        primaryStage.setTitle("COMP2522 - Lab 9");
        primaryStage.show();
    }

    /**
     * Returns a task that asks a question.
     *
     * @param question     Label object to display question to
     * @param answer       TextField object to display answer to
     * @param submitButton Button object to use as event
     *
     * @return             TODO; not too sure on what Object to return here
     */
    private static Task<Void> askQuestion(final Label question,
                                            final TextField answer,
                                            final Button submitButton)
    {
        return new Task<>() {
            @Override
            public Void call()
            {
                final int randQuestionValue;
                final String randQuestion;
                final String randAnswer;

                System.out.println("Task called!");

                // Instantiate rand, get question and answer
                randQuestionValue = RAND.nextInt(QUESTION_LIST.size());
                randQuestion = QUESTION_LIST.get(randQuestionValue);
                randAnswer = QUESTION_MAP.get(randQuestion);

                // Print to console to show I'm not insane
                System.out.println("Question: " + randQuestion);
                System.out.println("Answer: " + randAnswer);

                // Display question
                question.setText(randQuestion);
                // REMOVE LATER - display answer to text field
                // Yes, said text immediately gets overwritten -
                // I'm keeping it like this though
                answer.setPromptText(randAnswer);

                // When button is clicked:
                submitButton.setOnAction(e -> {
                    // If answer is correct
                    if(answer.getText().equals(randAnswer))
                    {
                        // Tell user they're correct!
                        System.out.println("Correct!");
                        question.setText("Correct!");
                    }
                    // Else
                    else {
                        // Shame them.
                        System.out.println("Incorrect!");
                        question.setText("Incorrect!");
                    }
                });

                // Return nothing - don't need to return a value
                return null;
            }
        };
    }

    /*
     * Returns the scene where the questions are asked.
     *
     * @param width  int width of the scene
     * @param height int height of the scene
     *
     * @return       Scene object
     */
    private static Scene questionScene(final int width,
                                       final int height)
    {
        // Variable
        int questionNum = 1; // Displays what question the user's on -
                             // Currently redundant
        final Label     questionLabel    = new Label();
        final Label     timerLabel       = new Label();
        final TextField answerBox        = new TextField();
        final Button    submitButton     = new Button("Submit");

        // Using an animation to make the timer
        final Timeline questionTimer = new Timeline();
        questionTimer.getKeyFrames()
                     .addAll(Stream.iterate(10.0, d -> d >= 0, d -> d - 0.01)
                                   .map( i -> new KeyFrame(Duration.seconds(10.0 - i),
                                                        e -> timerLabel.setText(String.format("%.2f", i))))
                                   .toList());

        // Set to actually do stuff if the timer reaches 0
        questionTimer.setOnFinished(e -> System.out.println("Countdown complete!"));

        // Use .play to play the timer from its current state
        // Use .stop to stop the timer and restart it (hidden)
        // Use .pause to stop the timer at it's current state
        // Use .playFromStart to play the timer from the start
        questionTimer.playFromStart();



        final Thread questionThread;
        final Thread countdownThread; // Used later, for countdown

        final VBox questionBox = new VBox(questionLabel,
                                          answerBox,
                                          submitButton, timerLabel);
        questionBox.setSpacing(10);

        final Task<Void> questionTask = askQuestion(questionLabel,
                                                      answerBox,
                                                      submitButton);
        System.out.println("Question task created");

        // Not too sure what this all is yet,
        // but it *is* necessary for the program to run
        questionThread = new Thread(questionTask);
        questionThread.setDaemon(true);
        questionThread.start();

        return new Scene(questionBox, width, height);
    }

    /*
     * Returns the beginning scene to enter the game.
     *
     * @param primaryStage Stage to show the scene to;
     *                       required to set button action
     * @param width        int width of the scene
     * @param height       int height of the scene
     *
     * @return The proper Scene object
     */
    private static Scene menuScene(final Stage primaryStage,
                                   final int width,
                                   final int height)
    {
        // INTRO MENU
        final Label menuLabel;
        final Button playButton;

        menuLabel = new Label("Welcome! play my game");
        playButton = new Button("Play my game. you know you wanna");

        final VBox menuBox = new VBox(menuLabel, playButton);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setSpacing(10);

        playButton.setOnAction(e -> primaryStage.setScene(
                questionScene(width, height)));

        return new Scene(menuBox, width, height);
    }
}