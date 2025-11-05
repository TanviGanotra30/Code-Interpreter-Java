// import javafx.application.Application;
// import javafx.geometry.Insets;
// import javafx.scene.Scene;
// import javafx.scene.control.*;
// import javafx.scene.layout.*;
// import javafx.stage.FileChooser;
// import javafx.stage.Stage;
// import java.io.*;

// public class InterpreterUI extends Application {

//     private TextArea codeArea;
//     private TextArea outputArea;

//     @Override
//     public void start(Stage stage) {
//         stage.setTitle("MiniInterpreter - JavaFX UI");

//         // --- Code Input Area ---
//         codeArea = new TextArea();
//         codeArea.setPromptText("Write your code here...");
//         codeArea.setWrapText(true);
//         codeArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14px;");

//         // --- Output Area ---
//         outputArea = new TextArea();
//         outputArea.setEditable(false);
//         outputArea.setWrapText(true);
//         outputArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13px; -fx-control-inner-background: #1e1e1e; -fx-text-fill: white;");

//         // --- Buttons ---
//         Button runButton = new Button("▶ Run");
//         Button clearButton = new Button("🧹 Clear");
//         Button loadButton = new Button("📂 Load File");

//         runButton.setOnAction(e -> runInterpreter());
//         clearButton.setOnAction(e -> {
//             codeArea.clear();
//             outputArea.clear();
//         });
//         loadButton.setOnAction(e -> loadFile(stage));

//         HBox buttonBar = new HBox(10, runButton, clearButton, loadButton);
//         buttonBar.setPadding(new Insets(10));

//         // --- Layout ---
//         VBox layout = new VBox(10,
//                 new Label("MiniInterpreter Code Editor"),
//                 codeArea,
//                 buttonBar,
//                 new Label("Output:"),
//                 outputArea
//         );
//         layout.setPadding(new Insets(10));

//         Scene scene = new Scene(layout, 900, 700);
//         stage.setScene(scene);
//         stage.show();

//         // optional: load the demo program by default
//         codeArea.setText(MiniInterpreter.demoProgram());
//     }

//     private void runInterpreter() {
//         String code = codeArea.getText();
//         if (code.trim().isEmpty()) {
//             outputArea.setText("⚠ Please enter some code to run!");
//             return;
//         }

//         try {
//             String result = MiniInterpreter.runSource(code);
//             outputArea.setText(result);
//         } catch (Exception ex) {
//             outputArea.setText("Runtime Error:\n" + ex.getMessage());
//         }
//     }

//     private void loadFile(Stage stage) {
//         FileChooser fileChooser = new FileChooser();
//         fileChooser.setTitle("Open MiniInterpreter Source File");
//         fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Source Files", "*.txt", "*.mini", "*.code"));
//         File selectedFile = fileChooser.showOpenDialog(stage);
//         if (selectedFile != null) {
//             try {
//                 String content = new String(java.nio.file.Files.readAllBytes(selectedFile.toPath()));
//                 codeArea.setText(content);
//             } catch (IOException e) {
//                 outputArea.setText("Error loading file: " + e.getMessage());
//             }
//         }
//     }

//     public static void main(String[] args) {
//         launch(args);
//     }
// }
