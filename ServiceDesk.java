package servicedesk;

import java.io.IOException;
import java.sql.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ServiceDesk extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("view/FXMLMainWindow.fxml"));

        Scene scene = new Scene(root);
        stage.setTitle("Service Desk");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    public static ObservableList<String> getTasks(Connection con) {
        ObservableList<String> activeTasks = null;
        try {
            PreparedStatement ps = con.prepareStatement("SELECT id,headline FROM task ORDER BY id");
            ResultSet resultSet = ps.executeQuery();
            activeTasks = FXCollections.observableArrayList();

            while (resultSet.next()) {
                int id = resultSet.getInt(1);
                String head = resultSet.getString(2);
                if (head.length() <= 13) {
                    activeTasks.add("Заявка № " + String.valueOf(id) + ": " + head);
                }
                else{
                    activeTasks.add("Заявка № " + String.valueOf(id) + ": " + head.substring(0, 12) + "...");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return activeTasks;
    }
    public static ObservableList<String> getSubTasks(Connection con, int taskId) {
        ObservableList<String> activeTasks = null;
        try {
            PreparedStatement ps = con.prepareStatement("SELECT id,headline FROM sub_task where relatedtask_id=? ORDER BY id");
            ps.setInt(1, taskId);
            ResultSet resultSet = ps.executeQuery();
            activeTasks = FXCollections.observableArrayList();

            while (resultSet.next()) {
                int id = resultSet.getInt(1);
                String head = resultSet.getString(2);
                if (head.length() <= 12) {
                    activeTasks.add("Задание № " + String.valueOf(id) + ": " + head);
                }
                else{
                    activeTasks.add("Задание № " + String.valueOf(id) + ": " + head.substring(0, 11) + "...");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return activeTasks;
    }
    
    public static void setUpWindow(Parent root, ActionEvent event, String name) throws IOException{
        Stage stage = new Stage();
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(name);
        
        stage.initModality(Modality.NONE);
        stage.initOwner(((Node)event.getSource()).getScene().getWindow());
        stage.show();
    }
    public static void setUpWindow(Parent root, ActionEvent event, String name, int id) throws IOException{
        Stage stage = new Stage();
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(name);
        
        stage.initModality(Modality.NONE);
        stage.initOwner(((Node)event.getSource()).getScene().getWindow());
        stage.setUserData(id);
        
        stage.show();
    }
    
    public static String getMonth (int monthNum){
        switch (monthNum){
            case 1: return (" январь '");
            case 2: return (" февраль '");
            case 3: return (" март '");
            case 4: return (" апрель '");
            case 5: return (" май '");
            case 6: return (" июнь '");
            case 7: return (" июль '");
            case 8: return (" август '");
            case 9: return (" сентябрь '");
            case 10: return (" октябрь '");
            case 11: return (" ноябрь '");
            case 12: return (" декабрь '");
            default: return (" Invalid month ");
        }
    }
}
