package servicedesk.control;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;

public class FXMLPieChartWindowController implements Initializable {

    @FXML
    private PieChart pieChart;
    @FXML
    private ComboBox employeeBox;
    @FXML
    private ComboBox stateBox;
    @FXML
    private TextArea totalArea;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ObservableList<String> stateList = FXCollections.observableArrayList();
        stateList.addAll("Все","Открытые","Закрытые");
        stateBox.setItems(stateList);
        
        employeeBoxFill();
        totalSearch((String) stateBox.getValue()); 

    }
    
    public void totalSearch(String state){
        pieChart.getData().clear();
        totalArea.clear();
        try {
            Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ServiceDesk", "postgres", "");
            PreparedStatement ps = null;
            if ( (state == null)||(stateBox.getValue().equals("Все")) ) {
                ps = con.prepareStatement("SELECT category,COUNT(*) FROM task group by category");
            }
            else if (state.equals("Открытые")){
                ps = con.prepareStatement("SELECT category,COUNT(*) FROM task Where state = 'Открыта' Group by category");
            }
            else if (state.equals("Закрытые")){
                ps = con.prepareStatement("SELECT category,COUNT(*) FROM task Where state = 'Закрыта' Group by category");
            }
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                PieChart.Data slice = new PieChart.Data(resultSet.getString(1), resultSet.getInt(2));
                pieChart.getData().add(slice);
                totalArea.appendText(resultSet.getString(1) + " - " + resultSet.getInt(2)+"\n");
            }
            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    public void searchOnEmployee(int id, String state){
        pieChart.getData().clear();
        totalArea.clear();
        try {
            Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ServiceDesk", "postgres", "");
            
            PreparedStatement ps = null;
            if ( (state == null)||(stateBox.getValue().equals("Все")) ) {
                ps = con.prepareStatement("SELECT category,COUNT(*) FROM task  where idmaster=? group by category");
                ps.setInt(1, id);
            }
            else if (state.equals("Открытые")){
                ps = con.prepareStatement("SELECT category,COUNT(*) FROM task Where idmaster=? and state = 'Открыта' Group by category");
                ps.setInt(1, id);
            }
            else if (state.equals("Закрытые")){
                ps = con.prepareStatement("SELECT category,COUNT(*) FROM task Where idmaster=? and state = 'Закрыта' Group by category");
                ps.setInt(1, id);
            }
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                PieChart.Data slice = new PieChart.Data(resultSet.getString(1), resultSet.getInt(2));
                pieChart.getData().add(slice);
                totalArea.appendText(resultSet.getString(1) + " - " + resultSet.getInt(2)+"\n");
            }
            
            
            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    public void employeeBoxFill(){
        ObservableList<String> employeeList = null;
        try {
            Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ServiceDesk", "postgres", "");
            PreparedStatement ps = con.prepareStatement("SELECT id,initials FROM employee ORDER BY id");
            ResultSet resultSet = ps.executeQuery();
            employeeList = FXCollections.observableArrayList();
            employeeList.add("0: Общий обзор");
            while (resultSet.next()) {
                int id = resultSet.getInt(1);
                String initials = resultSet.getString(2);
                employeeList.add(id + ": " + initials);
            }
            con.close();
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        employeeBox.setItems(employeeList);
        employeeBox.setValue(employeeList.get(0));
    }
    
    @FXML
    public void employeeBoxAction() {
        String sub = (String)employeeBox.getValue();
        sub = sub.substring(0, sub.indexOf(":"));
        
        if (sub.equals("0")) {
            totalSearch((String)stateBox.getValue());
        } else {
            searchOnEmployee(Integer.parseInt(sub),(String)stateBox.getValue());
        }
    }
    
    @FXML
    public void stateBoxAction() {
        String sub = (String)employeeBox.getValue();
        sub = sub.substring(0, sub.indexOf(":"));
        
        if (sub.equals("0")) {
            totalSearch((String)stateBox.getValue());
        } else {
            searchOnEmployee(Integer.parseInt(sub),(String)stateBox.getValue());
        }
    }
}
