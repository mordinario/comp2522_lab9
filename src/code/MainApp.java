import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
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
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Main.java. It's a quiz! TODO; this. later
 *
 * @author Ryan Chu
 * @author Marcy Ordinario
 * @version 1.0
 */
public class MainApp
        extends Application
{
    private static final Path                QUIZ_PATH = Paths.get("src", "res", "quiz.txt");
    private static final Map<String, String> QUESTION_MAP;
    private static final List<String>        QUESTION_LIST;
    private static final Random              RAND      = new Random();

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
                    .filter(s -> s.matches("[^|]*\\|[^|]*"))
                    .forEach(s -> QUESTION_MAP
                            .put(s.substring(0, s.indexOf("|")),
                                 s.substring(s.indexOf("|") + 1)));

            // Get an array list from the keys
            QUESTION_LIST = new ArrayList<>(QUESTION_MAP.keySet());
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /*
     * Stores functionality relating to the questions screen.
     * Use questionSceneHandler.QUESTION_SCENE to get the scene
     * Use askQuestion(num) to ask num questions
     */
    private abstract static class questionSceneHandler
    {
        private static final int    SPACING              = 10;
        private static final double TIMER_START_SECONDS  = 10;
        private static final double TIMER_CHANGE_SECONDS = 1.0 / 60;
        private static final double TIMER_END_SECONDS    = 0;
        private static final int    NOTHING              = 0;

        private static final Label     QUESTION_LABEL;
        private static final Label     TIMER_LABEL;
        private static final Label     CORRECT_LABEL;
        private static final Label     SCORE_LABEL;
        private static final Timeline  TIMER;
        private static final TextField ANSWER_BOX;
        private static final Button    SUBMIT_BUTTON;
        private static final VBox      QUESTION_BOX;
        public static final  Scene     QUESTION_SCENE;

        private static int score;

        private static final List<String> questionQueue = new ArrayList<>();

        static
        {
            QUESTION_LABEL = new Label();
            TIMER_LABEL    = new Label();
            CORRECT_LABEL  = new Label();
            SCORE_LABEL    = new Label();
            score = 0;

            TIMER = new Timeline(createCountdownFrames());
            TIMER.setOnFinished(e -> answerQuestion()); //TODO make this a different function?

            ANSWER_BOX = new TextField();
            ANSWER_BOX.setOnAction(e -> answerQuestion());

            SUBMIT_BUTTON = new Button("Submit");
            SUBMIT_BUTTON.setOnAction(e -> answerQuestion());

            QUESTION_BOX   = new VBox(SPACING,
                                      QUESTION_LABEL,
                                      ANSWER_BOX,
                                      SUBMIT_BUTTON,
                                      TIMER_LABEL,
                                      CORRECT_LABEL,
                                      SCORE_LABEL);
            QUESTION_SCENE = new Scene(QUESTION_BOX, 400, 400); //TODO real width and height
        }

        /**
         * Asks num questions on the questions scene
         * @param num the number of questions to ask
         */
        public static void askQuestions(final int num)
        {
            if(num <= NOTHING)
            {
                throw new IllegalArgumentException(
                        "Number of questions must be greater than 0");
            }
            if(num > QUESTION_LIST.size())
            {
                throw new IllegalArgumentException(
                        "Number of questions larger than question bank");
            }

            questionQueue.addAll(QUESTION_LIST.stream()
                                              .sorted(Comparator.comparing(i -> RAND.nextInt()))
                                              .limit(num)
                                              .toList());

            getQuestionFromQueue();
        }

        /*
         * Asks first question on the queue
         */
        private static void getQuestionFromQueue()
        {
            QUESTION_LABEL.setText(questionQueue.getFirst());
            System.out.println("asking question: "); //TODO remove eventually
            System.out.println(questionQueue.getFirst());
            System.out.println(QUESTION_MAP.get(questionQueue.getFirst()) + "\n");
            TIMER.playFromStart();
        }

        /*
         * handles what happens when a question gets answered
         */
        private static void answerQuestion()
        {
            TIMER.stop();
            if(QUESTION_MAP.get(questionQueue.getFirst())
                           .equalsIgnoreCase(ANSWER_BOX.getText()))
            {
                System.out.println("Correct answer"); // TODO save the score
                CORRECT_LABEL.setText("Correct!");
                score++;
            }
            else
            {
                System.out.println("Wrong answer");
                CORRECT_LABEL.setText("Incorrect.");
            }

            questionQueue.removeFirst();

            if (!questionQueue.isEmpty())
            {
                ANSWER_BOX.clear();
                getQuestionFromQueue();
            } else {
                System.out.println("END"); //TODO send to score screen
                QUESTION_LABEL.setText("The end!");
                SCORE_LABEL.setText("Score: " + score);
            }

        }

        /*
         * Used to set up the question timer
         * separated for readability
         */
        private static KeyFrame[] createCountdownFrames()
        {
            // Separated for readability
            Function<Double, KeyFrame> keyFrameMaker =
                    i -> new KeyFrame(Duration.seconds(TIMER_START_SECONDS - i),
                                      e -> TIMER_LABEL.setText(String.format("%.2f", i)));

            return Stream.concat(Stream.iterate(TIMER_START_SECONDS,
                                                d -> d > TIMER_END_SECONDS,
                                                d -> d - TIMER_CHANGE_SECONDS),
                                 Stream.of(TIMER_END_SECONDS)
                                )
                         .map(keyFrameMaker)
                         .toArray(KeyFrame[]::new);
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
            throws
            Exception
    {
        primaryStage.setScene(menuScene(primaryStage, 400, 300));
        primaryStage.setTitle("COMP2522 - Lab 9");
        primaryStage.show();
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
        final Label  menuLabel;
        final Button playButton;

        menuLabel  = new Label("Welcome! play my game");
        playButton = new Button("Play my game. you know you wanna");

        final VBox menuBox = new VBox(menuLabel,
                                      playButton);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setSpacing(10);

        playButton.setOnAction(e -> primaryStage.setScene(
                questionSceneHandler.QUESTION_SCENE));
        questionSceneHandler.askQuestions(10);

        return new Scene(menuBox,
                         width,
                         height);
    }
}