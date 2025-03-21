package ca.bcit.comp2522.lab9;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A simple JavaFX quiz program. Pulls questions from quiz.txt in the format question|answer.
 *
 * @author Ryan Chu
 * @author Marcy Ordinario
 * @version 1.0
 */
public class QuizApp
        extends Application
{
    private static final int    STAGE_WIDTH_PX      = 500;
    private static final int    STAGE_HEIGHT_PX     = 400;
    private static final int    EDGE_PADDING_PX     = 50;
    private static final int    QUESTION            = 0;
    private static final int    ANSWER              = 1;
    private static final int    QUESTIONS_TO_ASK    = 10;
    private static final int    STD_SPACING_PX      = 10;
    private static final int    NOTHING             = 0;
    private static final double TIMER_START_SECONDS = 10;

    private static final Path                QUIZ_PATH;
    private static final Map<String, String> QUESTION_MAP;
    private static final List<String>        QUESTION_LIST;
    private static final Random              RAND;
    private static final String              STYLES;
    private static       Stage               PRIMARY_STAGE;

    static
    {
        RAND   = new Random();
        STYLES = QuizApp.class.getResource("/Styles.css")
                              .toExternalForm();


        QUIZ_PATH = Paths.get("src", "res", "quiz.txt");

        if(Files.notExists(QUIZ_PATH))
        {
            throw new RuntimeException("Quiz question file not found");
        }

        try
        {
            final List<String> quizList = Files.readAllLines(QUIZ_PATH);

            QUESTION_MAP = quizList.stream()
                                   .filter(s->s.matches("[^|]*\\|[^|]*"))
                                   .collect(Collectors.toMap(s->s.split("\\|")[QUESTION],
                                                             s->s.split("\\|")[ANSWER]));

            QUESTION_LIST = new ArrayList<>(QUESTION_MAP.keySet());
        }
        catch(IOException e)
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
     * Entry point for JavaFX.
     *
     * @param primaryStage Stage to show
     */
    @Override
    public void start(final Stage primaryStage)
    {
        PRIMARY_STAGE = primaryStage;
        primaryStage.setScene(menuScene(primaryStage, STAGE_WIDTH_PX, STAGE_HEIGHT_PX));
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

        playButton.setOnAction(e->
                               {
                                   primaryStage.setScene(
                                           questionSceneHandler.QUESTION_SCENE);
                                   questionSceneHandler.askQuestions();
                               });

        return new Scene(menuBox,
                         width,
                         height);
    }

    /*
     * Stores functionality relating to the questions screen.
     * Use questionSceneHandler.QUESTION_SCENE to get the scene
     * Use askQuestion(num) to ask num questions
     */
    static private abstract class questionSceneHandler
    {
        private static final double TIMER_CHANGE_SECONDS = 1.0 / 60;
        private static final double TIMER_END_SECONDS    = 0;
        private static final int    BUTTON_MIN_WIDTH_PX  = 65;


        private static final Label     QUESTION_LABEL;
        private static final Label     TIMER_LABEL;
        private static final Label     CORRECT_LABEL;
        private static final Label     SCORE_LABEL;
        private static final Timeline  TIMER;
        private static final TextField ANSWER_BOX;
        private static final Button    SUBMIT_BUTTON;
        public static final  Scene     QUESTION_SCENE;

        private static final List<String> questionQueue;

        private static StringBuilder playerAnswers;
        private static int           score;
        private static int           questionIndex;

        static
        {
            score         = NOTHING;
            questionIndex = NOTHING;
            questionQueue = new ArrayList<>();
            playerAnswers = new StringBuilder();

            QUESTION_LABEL = new Label();
            QUESTION_LABEL.setWrapText(true);

            TIMER_LABEL   = new Label();
            CORRECT_LABEL = new Label();
            SCORE_LABEL   = new Label("Score: " + score);

            TIMER = new Timeline(createCountdownFrames());
            TIMER.setOnFinished(e->handleTimeout());

            ANSWER_BOX = new TextField();
            ANSWER_BOX.setOnAction(e->answerQuestion());

            SUBMIT_BUTTON = new Button("Submit");
            SUBMIT_BUTTON.setOnAction(e->answerQuestion());

            HBox scoreHBox;
            VBox questionVBox;
            VBox timerVBox;
            HBox contentHBox;
            VBox mainVBox;

            scoreHBox = new HBox(STD_SPACING_PX, SCORE_LABEL, CORRECT_LABEL);
            questionVBox = new VBox(STD_SPACING_PX, QUESTION_LABEL, ANSWER_BOX);
            timerVBox = new VBox(STD_SPACING_PX, TIMER_LABEL, SUBMIT_BUTTON);
            contentHBox = new HBox(questionVBox, timerVBox);
            mainVBox = new VBox(STD_SPACING_PX, contentHBox, scoreHBox);

            scoreHBox.setAlignment(Pos.CENTER);

            questionVBox.setAlignment(Pos.BOTTOM_LEFT);
            HBox.setHgrow(questionVBox, Priority.ALWAYS);

            timerVBox.setAlignment(Pos.BOTTOM_CENTER);
            timerVBox.setMinWidth(BUTTON_MIN_WIDTH_PX);

            contentHBox.setAlignment(Pos.CENTER);

            mainVBox.setPadding(new Insets(NOTHING, EDGE_PADDING_PX, NOTHING, EDGE_PADDING_PX));
            mainVBox.setAlignment(Pos.CENTER);


            QUESTION_SCENE = new Scene(mainVBox, STAGE_WIDTH_PX, STAGE_HEIGHT_PX);
            QUESTION_SCENE.getStylesheets()
                          .add(STYLES);
        }

        /**
         * Asks QUESTION_TO_ASK questions on the question scene. Throws an exception if the current
         * scene isn't the question scene.
         */
        public static void askQuestions()
        {
            if(PRIMARY_STAGE.getScene() != QUESTION_SCENE)
            {
                throw new IllegalStateException("Primary stage is not in QUESTION_SCENE");
            }

            questionQueue.addAll(QUESTION_LIST.stream()
                                              .sorted(Comparator.comparing(i->RAND.nextInt()))
                                              .limit(QUESTIONS_TO_ASK)
                                              .toList());

            getQuestionFromQueue();
        }

        /*
         * Displays the first question on the queue and starts the timer
         */
        private static void getQuestionFromQueue()
        {
            QUESTION_LABEL.setText(questionQueue.getFirst());
            TIMER.playFromStart();
        }

        /*
         * Handles when a question gets answered normally.
         */
        private static void answerQuestion()
        {
            TIMER.stop();

            String question;
            String playerAnswer;
            String realAnswer;
            String result;

            question     = questionQueue.getFirst();
            playerAnswer = ANSWER_BOX.getText();
            realAnswer   = QUESTION_MAP.get(question);

            playerAnswers.append((questionIndex + 1))
                         .append(". ")
                         .append(question)
                         .append(" - ");

            if(realAnswer.equalsIgnoreCase(playerAnswer.strip()))
            {
                result = "Correct";
                CORRECT_LABEL.setText(result);
                CORRECT_LABEL.setStyle("-fx-text-fill: green");

                score++;
                SCORE_LABEL.setText("Score: " + score);

                playerAnswers.append(result)
                             .append("\n\tCorrect Answer: ")
                             .append(realAnswer)
                             .append("\n\n");
            }
            else
            {
                result = "Incorrect";
                CORRECT_LABEL.setText(result);
                CORRECT_LABEL.setStyle("-fx-text-fill: red");

                playerAnswers.append(result)
                             .append("\n\tCorrect Answer: ")
                             .append(realAnswer)
                             .append("\n\tPlayer Answer: ")
                             .append(playerAnswer)
                             .append("\n\n");
            }


            removeQuestionFromQueue();
        }

        /*
         * Handles when a question gets timed out.
         */
        private static void handleTimeout()
        {
            CORRECT_LABEL.setText("Timed out");
            CORRECT_LABEL.setStyle("-fx-text-fill: red");

            playerAnswers.append((questionIndex + 1))
                         .append(". ")
                         .append(questionQueue.getFirst())
                         .append(" - Timed Out\n\tCorrect Answer: ")
                         .append(QUESTION_MAP.get(questionQueue.getFirst()))
                         .append("\n\n");

            removeQuestionFromQueue();
        }

        /*
         * Handles removing question from the queue.
         */
        private static void removeQuestionFromQueue()
        {
            questionQueue.removeFirst();
            questionIndex++;

            if(!questionQueue.isEmpty())
            {
                ANSWER_BOX.clear();
                ANSWER_BOX.requestFocus();
                getQuestionFromQueue();
            }
            else
            {
                PRIMARY_STAGE.setScene(endScreenHandler.END_SCENE);
                endScreenHandler.display(score, playerAnswers.toString());
                cleanUpScene();
            }
        }

        /*
         * Cleans up the fields in the question scene
         */
        private static void cleanUpScene()
        {
            questionQueue.clear();
            TIMER.stop();
            ANSWER_BOX.clear();
            QUESTION_LABEL.setText("");
            SCORE_LABEL.setText("");
            CORRECT_LABEL.setText("");
            score         = NOTHING;
            questionIndex = NOTHING;
            playerAnswers = new StringBuilder();
        }

        /*
         * Used to set up the question timer
         * separated for readability
         */
        private static KeyFrame[] createCountdownFrames()
        {
            // Separated for readability
            Function<Double, KeyFrame> keyFrameMaker =
                    i->new KeyFrame(Duration.seconds(TIMER_START_SECONDS - i),
                                    e->TIMER_LABEL.setText(String.format("%.2f", i)));

            return Stream.concat(Stream.iterate(TIMER_START_SECONDS,
                                                d->d > TIMER_END_SECONDS,
                                                d->d - TIMER_CHANGE_SECONDS),
                                 Stream.of(TIMER_END_SECONDS)
                                )
                         .map(keyFrameMaker)
                         .toArray(KeyFrame[]::new);
        }
    }

    /*
     * Stores functionality related to the end screen
     * Use endScreenHandler.END_SCENE to access the scene and
     * .display(int, String) to display a given score and text
     */
    static private abstract class endScreenHandler
    {
        static final TextArea ANSWER_BOX;
        static final Label    SCORE_LABEL;
        static final Button   RETRY_BUTTON;
        static final Scene    END_SCENE;

        static
        {
            SCORE_LABEL  = new Label("Score: ");

            ANSWER_BOX   = new TextArea();
            ANSWER_BOX.setEditable(false);

            RETRY_BUTTON = new Button("Retry");
            RETRY_BUTTON.setOnAction(e->
                                     {
                                         PRIMARY_STAGE.setScene(
                                                 questionSceneHandler.QUESTION_SCENE);
                                         questionSceneHandler.askQuestions();
                                     });

            Label resultsLabel;
            VBox  mainVBox;

            resultsLabel = new Label("Results");
            mainVBox = new VBox(STD_SPACING_PX, resultsLabel, SCORE_LABEL, ANSWER_BOX,
                                RETRY_BUTTON);

            resultsLabel.setStyle("-fx-font-weight: bold");
            mainVBox.setPadding(new Insets(0, EDGE_PADDING_PX, 0, EDGE_PADDING_PX));

            END_SCENE = new Scene(mainVBox, STAGE_WIDTH_PX, STAGE_HEIGHT_PX);
            END_SCENE.getStylesheets()
                     .add(STYLES);
        }

        /**
         * Sets SCORE_LABEL to the given score and ANSWER_BOX to the given string.
         *
         * @param score the score to set
         * @param answers the answers to set
         */
        public static void display(final int score,
                                   final String answers)
        {
            SCORE_LABEL.setText("Score: " + score);
            ANSWER_BOX.setText(answers);
        }
    }

}