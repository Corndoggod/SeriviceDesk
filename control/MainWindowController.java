package servicedesk.control;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import servicedesk.ServiceDesk;

public class MainWindowController implements Initializable {
    
    private static int id;
    private static double progress;
    
    @FXML
    private ListView taskList;
    @FXML
    private Label taskIdLabel;
    @FXML
    private TextArea noteArea;
    @FXML
    private TextArea masterNoteArea;
    @FXML
    private TextField headField;
    @FXML
    private Label openDateLabel;
    @FXML
    private Label closeDateLabel;
    @FXML
    private Label countLabel;
    @FXML
    private ComboBox priorityBox;
    @FXML
    private ComboBox stateBox;
    @FXML
    private ComboBox categoryBox;
    @FXML
    private ProgressBar taskProgressBar;
        
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        taskProgressBar.setProgress(0);
        
        ObservableList<String> priorityValue = FXCollections.observableArrayList();
        priorityValue.addAll("Приоритет: Средний","Приоритет: Высокий","Приоритет: Низкий");
        priorityBox.setItems(priorityValue);
        
        ObservableList<String> categoryValue = FXCollections.observableArrayList();
        categoryValue.addAll("Категория: Ошибки связи","Категория: Неопределено","Категория: Неисправность оборудования","Категория: Программные ошибки","Категория: Запрос на обслуживание");
        categoryBox.setItems(categoryValue);
        
        ObservableList<String> stateValue = FXCollections.observableArrayList();
        stateValue.addAll("Статус: Закрыта","Статус: Открыта");
        stateBox.setItems(stateValue);
        
        try{           
            try{
                Class.forName("org.postgresql.Driver");
            }catch (Exception ex){ex.printStackTrace();}
            
            Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ServiceDesk","postgres","");
            taskList.setItems(ServiceDesk.getTasks(con));
            con.close();
            
        }catch (SQLException ex){ex.printStackTrace();}
        
        MultipleSelectionModel<String> taskSelectionModel = taskList.getSelectionModel();
        taskSelectionModel.selectedItemProperty().addListener(new ChangeListener<String>(){
             
            @Override
            public void changed(ObservableValue<? extends String> changed, String oldValue, String newValue){
                
                taskProgressBar.setProgress(0);

                taskIdLabel.setText(newValue.substring(0,10));
                int id = Integer.parseInt(newValue.substring(9,newValue.indexOf(":")));
                setId(id);
                try {
                    Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ServiceDesk", "postgres", "");
                    
                    PreparedStatement ps = con.prepareStatement("SELECT priority,category,headline,note,masternote,creationdate,closingdate,state FROM task WHERE id=?");
                    ps.setInt(1,id);
                    ResultSet resultSet = ps.executeQuery();

                    while (resultSet.next()) {
                        priorityBox.setValue("Приоритет: " + resultSet.getString(1));
                        categoryBox.setValue("Категория: " + resultSet.getString(2));
                        headField.setText(resultSet.getString(3));
                        noteArea.setText(resultSet.getString(4));
                        masterNoteArea.setText(resultSet.getString(5));                        
                        openDateLabel.setText("Дата создания: " + resultSet.getString(6));
                        closeDateLabel.setText("Дата закрытия: " + resultSet.getString(7));
                        stateBox.setValue("Статус: " + resultSet.getString(8));
                    }
                    
                    con.close();
                } catch (SQLException ex) {ex.printStackTrace();}
                
                try {
                    int count = 0;
                    int closedCount = 0;
                    Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ServiceDesk", "postgres", "");
                    
                    PreparedStatement ps = con.prepareStatement("SELECT count(*) FROM sub_task WHERE relatedtask_id=?");
                    ps.setInt(1,id);
                    ResultSet resultSet = ps.executeQuery();

                    while (resultSet.next()) {
                        count = resultSet.getInt(1);
                    }
                    
                    ps = con.prepareStatement("SELECT count(*) FROM sub_task WHERE relatedtask_id=? and state = true group by relatedtask_id");
                    ps.setInt(1,id);
                    resultSet = ps.executeQuery();

                    while (resultSet.next()) {
                        closedCount = resultSet.getInt(1);
                    }
                    con.close();
                    taskProgressBar.setProgress(1.0/count * closedCount);
                    setProgress(1.0/count * closedCount);
                    countLabel.setText("  " + closedCount + "/" + count + "  ");
                    
                } catch (SQLException ex) {ex.printStackTrace();}
            }
        });
    }
    
    @FXML
    private void diagBtnAction(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/servicedesk/view/FXMLdiagramWindow.fxml"));
        servicedesk.ServiceDesk.setUpWindow(root, event, "Diagram Window");
    }
    @FXML
    private void subTaskBtnAction(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/servicedesk/view/FXMLSubTaskWindow.fxml"));
        servicedesk.ServiceDesk.setUpWindow(root, event, "Sub Task Window");
    }
    @FXML
    private void saveBtnAction(ActionEvent event) throws Exception {
        String head = headField.getText();
        String note = noteArea.getText();
        String masternote = masterNoteArea.getText();
        
        String state = (String)stateBox.getValue();
        state = state.substring(state.indexOf(": ")+2);
        
        String priority = (String)priorityBox.getValue();
        priority = priority.substring(priority.indexOf(": ")+2);
        
        String category = (String)categoryBox.getValue();
        category = category.substring(category.indexOf(": ")+2);
        
        try {
            Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ServiceDesk", "postgres", "");
            PreparedStatement ps = con.prepareStatement("UPDATE task SET priority=? ::priority_level, category=?, headline=?, note=?, masternote=?, state=? ::task_state WHERE id=?");
            ps.setString(1,priority);
            ps.setString(2,category);
            ps.setString(3,head);
            ps.setString(4,note);
            ps.setString(5,masternote);
            ps.setString(6,state);
            ps.setInt(7, id);
            ps.executeUpdate();
            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        
        Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ServiceDesk", "postgres", "");
        taskList.setItems(ServiceDesk.getTasks(con));
        con.close();
    }
    @FXML
    private void knowledgeBtnAction(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/servicedesk/view/FXMLKnowledgeBase.fxml"));
        servicedesk.ServiceDesk.setUpWindow(root, event, "Knowledge Base");
    }
    
    public void setId(int id){
        this.id = id;
    }
    public static int getId(){
        return id;
    }
    
    public void setProgress(double progress){
        this.progress = progress;
    }
    public static double getProgress(){
        return progress;
    }
}
