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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class FXMLKnowledgeBaseController implements Initializable {

    @FXML
    private ListView categoryListView;
    @FXML
    private ListView tipsListView;
    @FXML
    private TextField searchField;
    @FXML
    private TextField headField;
    @FXML
    private TextArea tipArea;
    
    private String currentCategory;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        ObservableList<String> categoryList = null;
        try {
            Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ServiceDesk","postgres","");
            PreparedStatement ps = con.prepareStatement("SELECT categoryname FROM categories ORDER BY id");
            ResultSet resultSet = ps.executeQuery();
            categoryList = FXCollections.observableArrayList();
            
            categoryList.add("Все");
            while (resultSet.next()) {
                categoryList.add(resultSet.getString(1));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        categoryListView.setItems(categoryList);
        
        
        MultipleSelectionModel<String> categorySelectionModel = categoryListView.getSelectionModel();
        categorySelectionModel.selectedItemProperty().addListener(new ChangeListener<String>(){
             
            @Override
            public void changed(ObservableValue<? extends String> changed, String oldValue, String newValue){
                
                currentCategory = newValue;
                ObservableList<String> tipsList = null;
                try {
                    Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ServiceDesk", "postgres", "");
                    PreparedStatement ps = null;
                    if (currentCategory.equals("Все")) {
                        ps = con.prepareStatement("SELECT id,incident_name FROM knowledge_base ORDER BY id");
                    } else {
                        ps = con.prepareStatement("SELECT id,incident_name FROM knowledge_base where categoryname=? ORDER BY id");
                        ps.setString(1, newValue);
                    }
                    ResultSet resultSet = ps.executeQuery();
                    tipsList = FXCollections.observableArrayList();

                    while (resultSet.next()) {
                        tipsList.add(resultSet.getString(1)+": "+resultSet.getString(2));
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                tipsListView.setItems(tipsList);
            }
        });
        
        MultipleSelectionModel<String> tipsSelectionModel = tipsListView.getSelectionModel();
        tipsSelectionModel.selectedItemProperty().addListener(new ChangeListener<String>(){
             
            @Override
            public void changed(ObservableValue<? extends String> changed, String oldValue, String newValue) {
                
                try {
                    Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ServiceDesk", "postgres", "");
                    PreparedStatement ps = con.prepareStatement("SELECT incident_name,solution FROM knowledge_base where id=? ORDER BY id");
                    ps.setInt(1, Integer.parseInt(newValue.substring(0,newValue.indexOf(":"))));
                    ResultSet resultSet = ps.executeQuery();
                    while (resultSet.next()) {   
                        headField.setText(resultSet.getString(1));
                        tipArea.setText(resultSet.getString(2));
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    
    @FXML
    public void tipSearch(ActionEvent event) throws Exception {
        
        String searchValue = searchField.getText();

        ObservableList<String> searchedTipsList = null;
        try {
            Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ServiceDesk", "postgres", "");
            PreparedStatement ps = null;
            if ( (searchValue.equals("")) && ( currentCategory.equals("Все") ) ){
                ps = con.prepareStatement("SELECT id,incident_name FROM knowledge_base ORDER BY id");
            }
            else if ( (searchValue.equals("") == false) && ( currentCategory.equals("Все") ) ){
                ps = con.prepareStatement("SELECT id,incident_name FROM knowledge_base where incident_name like '%" + searchValue + "%' ORDER BY id");
            } else {
                ps = con.prepareStatement("SELECT id,incident_name FROM knowledge_base where categoryname=? and incident_name like '%" + searchValue + "%' ORDER BY id");
                ps.setString(1, currentCategory);
            }
            ResultSet resultSet = ps.executeQuery();
            searchedTipsList = FXCollections.observableArrayList();

            while (resultSet.next()) {
                searchedTipsList.add(resultSet.getString(1) + ": " + resultSet.getString(2));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        tipsListView.setItems(searchedTipsList);

    }
}
