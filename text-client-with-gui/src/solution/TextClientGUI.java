package solution;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;


public class TextClientGUI extends Application {
    MenuButton fileSelector;
    String hostName;
    TextArea textInfo;
    ToggleGroup radioToggle = new ToggleGroup();
    String selectedFile;
    TextField saveFileLocation;
    String directory = "src\\resources\\files\\";


    public void start (Stage primaryStage){
        VBox root = new VBox();
        root.setPrefWidth(1000);
        root.setPrefHeight(800);
        root.setStyle("-fx-background-color: black; "
                + "-fx-border-color: black; -fx-border-width:10");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        textInfo = new TextArea("Click \"Get File Index\" to to the index of the directory's contents.\n" +
                "Otherwise, select the file from the menu, and click \"Get File Contents\" to get the contents of the selected file.");
        textInfo.setWrapText(true);

        textInfo.setPrefHeight(root.getPrefHeight()-40);
        textInfo.setFont(Font.font(null, 18));
        textInfo.setEditable(false);
        textInfo.setPrefWidth(root.getPrefWidth()-10);

        root.getChildren().add(textInfo);

        Button getIndexButton = new Button("Get Index");
        fileSelector = new MenuButton("Menu");
        Button getFileContents = new Button("Get Contents");
        Button saveButton = new Button("Save Text");
        saveFileLocation = new TextField("Type Save Location Here");

        String buttonStyle1 = " -fx-text-fill: black; -fx-font-weight:bold; -fx-font-size:14px";

        getIndexButton.setStyle(buttonStyle1);
        getFileContents.setStyle(buttonStyle1);
        fileSelector.setStyle(buttonStyle1);
        saveButton.setStyle(buttonStyle1);
        saveFileLocation.setStyle(buttonStyle1);

        saveButton.setOnAction(actionEvent -> onSaveButtonClicked());
        getIndexButton.setOnAction(actionEvent -> onGetIndexClicked());
        getFileContents.setOnAction(actionEvent -> onGetFileContentsClicked());


        HBox buttonBox = new HBox();
        buttonBox.getChildren().addAll(getIndexButton,getFileContents,fileSelector, saveButton, saveFileLocation);
        buttonBox.setAlignment(Pos.BASELINE_CENTER);
        buttonBox.setSpacing(10);
        buttonBox.setStyle("-fx-background-color: black; "
                + "-fx-border-color: black; -fx-border-width:10");


        root.getChildren().add(buttonBox);


        primaryStage.show();
    }

    private void onGetFileContentsClicked(){
        Socket connection = establishConnection();

        try {
            BufferedReader incoming = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            PrintWriter outgoing = new PrintWriter(connection.getOutputStream());

            System.out.println(selectedFile);
            outgoing.println("GET "+selectedFile);
            outgoing.flush();

            String lineFromServer = incoming.readLine();
            if(lineFromServer==null){
                System.out.println("server returned NULL.");
            }

            String serverInfo = "";
            while(lineFromServer!=null){
                serverInfo = serverInfo.concat(lineFromServer + "\n");
                lineFromServer = incoming.readLine();
            }
            textInfo.setText(serverInfo);

            incoming.close();

        }catch (Exception e){
            System.out.println("ERROR - file printing error.");
        }


    }

    private void onSaveButtonClicked(){
        File newFile = new File(directory+ saveFileLocation.getText()+".txt");
        try{
            if(!newFile.createNewFile()){
                saveFileLocation.setText(saveFileLocation.getText()+" already exists!");
            }else{
                FileWriter writeToFile = new FileWriter(newFile);
                writeToFile.write(textInfo.getText());
                writeToFile.close();
                saveFileLocation.setText("Created.");
                saveFileLocation.requestFocus();
                saveFileLocation.selectAll();
            }
        }catch (IOException e){
            saveFileLocation.setText("Error - Cannot save there!");

        }
    }

    private void onGetIndexClicked(){
        Socket connection = establishConnection();
        try {
            BufferedReader incoming = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            PrintWriter outgoing = new PrintWriter(connection.getOutputStream());
            outgoing.println("INDEX");
            outgoing.flush();

            String lineFromServer = incoming.readLine();
            ArrayList<String> stringArray = new ArrayList<String>();
            String serverInfo = "";
            if(lineFromServer==null){
               textInfo.setText("Server Error. Please contact the server administrator.");
            }
            while(lineFromServer!=null){
                serverInfo = serverInfo.concat(lineFromServer + "\n");
                stringArray.add(lineFromServer + "\n");
                lineFromServer = incoming.readLine();
            }
            textInfo.setText(serverInfo);
            setMenu(stringArray);

            incoming.close();
        }catch (Exception e){
            System.out.println("ERROR - index printing error.");
        }
    }

    private Socket establishConnection(){
        int listeningPort = 3000;
        Socket connection;

        Scanner userInput = new Scanner(System.in);
        System.out.print("Enter computer name or IP address: ");

        try {
            connection = new Socket(hostName, listeningPort);
            textInfo.setText("Connected.");
            System.out.println("Connected.");
        }catch (Exception e) {
            System.out.println("Error: " + e);
            textInfo.setText("Failed to connect. Please try again.");
            return null;
        }
        return connection;
    }

    private void setMenu(ArrayList<String> menuItems) {
        fileSelector.getItems().clear();
        fileSelector.setText("Menu");

        for (String menuItem : menuItems) {
            RadioMenuItem item = new RadioMenuItem(menuItem);
            item.setToggleGroup(radioToggle);
            fileSelector.getItems().add(item);
            item.setOnAction(e -> {
                fileSelector.setText(menuItem);
                selectedFile = menuItem;
            });
        }
    }

    public static void main (String[] args){
        launch(args);
    }


}
