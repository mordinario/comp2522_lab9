import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main
    extends Application
{
    private static final Path                QUIZ_PATH = Paths.get("src", "res", "quiz.txt");
    private static final Map<String, String> QUESTION_MAP;
    private static final List<String>        QUESTION_LIST;
    private static final Random              RAND = new Random();

    static
    {
        if(Files.notExists(QUIZ_PATH))
        {
            throw new RuntimeException("Quiz question file not found");
        }

        try
        {
            final List<String> quizList = Files.readAllLines(QUIZ_PATH);
            QUESTION_MAP = new HashMap<>();
            quizList.forEach(s -> QUESTION_MAP.put(s.substring(0, s.indexOf("|")),
                                                   s.substring(s.indexOf("|") + 1)));
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
     *
     * @param primaryStage
     * @throws Exception
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
     *
     *
     * @param question
     * @param answer
     * @param submitButton
     * @return
     */
    private static Task<String> askQuestion(final Label question,
                                            final TextField answer,
                                            final Button submitButton)
    {
        return new Task<>() {
            @Override
            public String call()
            {
                final int randQuestionValue;
                final String randQuestion;
                final String randAnswer;

                System.out.println("Task called!");

                // Instantiate rand, get question and answer
                randQuestionValue = RAND.nextInt(QUESTION_LIST.size());
                randQuestion = QUESTION_LIST.get(randQuestionValue);
                randAnswer = QUESTION_MAP.get(randQuestion);

                System.out.println("Question: " + randQuestion);
                System.out.println("Answer: " + randAnswer);

                question.setText(randQuestion);
                answer.setPromptText(randAnswer);

                submitButton.setOnAction(e -> {
                    if(answer.getText().equals(randAnswer))
                    {
                        System.out.println("Correct!");
                        question.setText("Correct!");
                    }
                    else {
                        System.out.println("Incorrect!");
                        question.setText("Incorrect!");
                    }
                });

                return "HEEEELP";
            }
        };
    }

    private static Scene questionScene(final Stage primaryStage,
                                       final int   height,
                                       final int   width)
    {
        // QUESTIONS
        int questionNum = 1;
        final Label     question = new Label("Question " + questionNum);
        final TextField answer   = new TextField();
        final Button    submit   = new Button("Submit");

        final Thread questionThread;
        final Thread countdownThread;

        final VBox questionBox = new VBox(question, answer, submit);
        questionBox.setSpacing(10);

        final Task<String> questionTask = askQuestion(question, answer, submit);
        System.out.println("Task object created");

        questionThread = new Thread(questionTask);
        questionThread.setDaemon(true);
        questionThread.start();

        question.setText(questionTask.getValue());

        return new Scene(questionBox, height, width);
    }

    private static Scene menuScene(final Stage primaryStage,
                                   final int height,
                                   final int width)
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
                questionScene(primaryStage, height, width)));

        return new Scene(menuBox, height, width);
    }
}