package servicedesk.control;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import servicedesk.ServiceDesk;

public class FXMLSubTaskWindowController implements Initializable {
    
    @FXML
    private ListView subTaskList;
    @FXML
    private Label taskIdLabel;
    @FXML
    private Label subTaskIdLabel;
    @FXML
    private Label stateLabel;
    @FXML
    private Label openDateLabel;
    @FXML
    private Label closeDateLabel;
    @FXML
    private TextArea masterNoteArea;
    @FXML
    private TextField headField;
    @FXML
    private ProgressBar taskProgressBar;
    
    private int parentId = MainWindowController.getId();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        taskProgressBar.setProgress(MainWindowController.getProgress());
        taskIdLabel.setText("Заявка №" + String.valueOf(parentId));
        try{           
            try{
                Class.forName("org.postgresql.Driver");
            }catch (Exception ex){ex.printStackTrace();}
            
            Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ServiceDesk","postgres","");
            subTaskList.setItems(ServiceDesk.getSubTasks(con,parentId));
            con.close();
            
        }catch (SQLException ex){ex.printStackTrace();}
        
        MultipleSelectionModel<String> taskSelectionModel = subTaskList.getSelectionModel();
        taskSelectionModel.selectedItemProperty().addListener(new ChangeListener<String>(){
             
            @Override
            public void changed(ObservableValue<? extends String> changed, String oldValue, String newValue){
                
                subTaskIdLabel.setText(newValue.substring(0,11));
                int id = Integer.parseInt(newValue.substring(10,newValue.indexOf(":")));
                try {
                    Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ServiceDesk", "postgres", "");
                    
                    PreparedStatement ps = con.prepareStatement("SELECT headline,masternote,start,sub_task.end,state FROM sub_task WHERE id=? and relatedtask_id=?");
                    ps.setInt(1,id);
                    ps.setInt(2,parentId);
                    ResultSet resultSet = ps.executeQuery();

                    while (resultSet.next()) {
                        headField.setText(resultSet.getString(1));
                        masterNoteArea.setText(resultSet.getString(2));                        
                        openDateLabel.setText("Дата создания: " + resultSet.getString(3));
                        closeDateLabel.setText("Дата закрытия: " + resultSet.getString(4));
                        if (resultSet.getBoolean(5)){stateLabel.setText("Статус: Закрыта");}
                        else {stateLabel.setText("Статус: Открыта");}
                        
                    }
                    
                    con.close();
                } catch (SQLException ex) {ex.printStackTrace();}
                
                
            }
        });
        
    }    
    
}
