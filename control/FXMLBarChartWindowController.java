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
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;


public class FXMLBarChartWindowController implements Initializable {
    @FXML
    ComboBox searchBox;
    
    @FXML
    BarChart barChart;
    
    @FXML
    CategoryAxis xAxis;
    
    @FXML
    NumberAxis yAxis;
        
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ObservableList<String> searchTypes = FXCollections.observableArrayList("По категориям", "По состоянию");
        searchBox.setItems(searchTypes);
        yAxis.setAutoRanging(false);
        categorySearch();
    }
    
    @FXML
    public void searchTypeAction(){
        if ( searchBox.getValue().equals("По категориям") ){
            categorySearch();
            yAxis.setTickUnit(1);
            yAxis.setUpperBound(11);
        }
        else{
            opencloseSearch();
            yAxis.setTickUnit(5);
            yAxis.setUpperBound(40);
        }
    }
    
    public void categorySearch (){
        
        barChart.getData().clear();
        
        xAxis.setLabel("Месяц");
        yAxis.setLabel("Количество");
        
        String srs = null;
        String checkDate = null;
        String category = null;
        String date = null;
        int count;
        
        XYChart.Series<String,Number> dataSeriesUnknown = new XYChart.Series<>();
        XYChart.Series<String,Number> dataSeriesProgErr = new XYChart.Series<>();
        XYChart.Series<String,Number> dataSeriesLinkErr = new XYChart.Series<>();
        XYChart.Series<String,Number> dataSeriesHardwareErr = new XYChart.Series<>();
        XYChart.Series<String,Number> dataSeriesServiceRequest = new XYChart.Series<>();

        dataSeriesUnknown.setName("Неопределено");
        dataSeriesProgErr.setName("Программные ошибки");
        dataSeriesLinkErr.setName("Ошибки связи");
        dataSeriesHardwareErr.setName("Неисправность оборудования");
        dataSeriesServiceRequest.setName("Запрос на обслуживание");
        
        try {
            Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ServiceDesk", "postgres", "");
            //todo: переделать sql-запрос
            PreparedStatement ps = con.prepareStatement("SELECT extract(month from creationdate), extract(year from creationdate), "
                    + "count(*), category FROM task group by category, extract(month from creationdate), extract(year from creationdate) "
                    + "order by extract(month from creationdate), extract(year from creationdate)");
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                
                category = resultSet.getString(4);
                count = resultSet.getInt(3);
                date = (servicedesk.ServiceDesk.getMonth(resultSet.getInt(1)) + resultSet.getString(2));
                
                dataSeriesUnknown.getData().add(new XYChart.Data<>("ноябрь 2018", 5));
                dataSeriesUnknown.getData().add(new XYChart.Data<>("декабрь 2018", 10));
                dataSeriesUnknown.getData().add(new XYChart.Data<>("январь 2019", 4));
                dataSeriesUnknown.getData().add(new XYChart.Data<>("Февраль 2019", 5));
                dataSeriesUnknown.getData().add(new XYChart.Data<>("март 2019", 3));
                dataSeriesUnknown.getData().add(new XYChart.Data<>("апрель 2019", 8));
                
                dataSeriesProgErr.getData().add(new XYChart.Data<>("ноябрь 2018", 5));
                dataSeriesProgErr.getData().add(new XYChart.Data<>("декабрь 2018", 9));
                dataSeriesProgErr.getData().add(new XYChart.Data<>("январь 2019", 8));
                dataSeriesProgErr.getData().add(new XYChart.Data<>("Февраль 2019", 10));
                dataSeriesProgErr.getData().add(new XYChart.Data<>("март 2019", 7));
                dataSeriesProgErr.getData().add(new XYChart.Data<>("апрель 2019", 7));
                
                dataSeriesLinkErr.getData().add(new XYChart.Data<>("ноябрь 2018", 4));
                dataSeriesLinkErr.getData().add(new XYChart.Data<>("декабрь 2018", 9));
                dataSeriesLinkErr.getData().add(new XYChart.Data<>("январь 2019", 10));
                dataSeriesLinkErr.getData().add(new XYChart.Data<>("Февраль 2019", 8));
                dataSeriesLinkErr.getData().add(new XYChart.Data<>("март 2019", 5));
                dataSeriesLinkErr.getData().add(new XYChart.Data<>("апрель 2019", 4));
                
                dataSeriesHardwareErr.getData().add(new XYChart.Data<>("ноябрь 2018", 2));
                dataSeriesHardwareErr.getData().add(new XYChart.Data<>("декабрь 2018", 9));
                dataSeriesHardwareErr.getData().add(new XYChart.Data<>("январь 2019", 5));
                dataSeriesHardwareErr.getData().add(new XYChart.Data<>("Февраль 2019", 4));
                dataSeriesHardwareErr.getData().add(new XYChart.Data<>("март 2019", 2));
                dataSeriesHardwareErr.getData().add(new XYChart.Data<>("апрель 2019", 5));
                
                dataSeriesServiceRequest.getData().add(new XYChart.Data<>("ноябрь 2018", 9));
                dataSeriesServiceRequest.getData().add(new XYChart.Data<>("декабрь 2018", 4));
                dataSeriesServiceRequest.getData().add(new XYChart.Data<>("январь 2019", 3));
                dataSeriesServiceRequest.getData().add(new XYChart.Data<>("Февраль 2019", 2));
                dataSeriesServiceRequest.getData().add(new XYChart.Data<>("март 2019", 8));
                dataSeriesServiceRequest.getData().add(new XYChart.Data<>("апрель 2019", 5));

                /* 
                
                if ( category.equals(dataSeriesUnknown.getName() )){
                    dataSeriesUnknown.getData().add(new XYChart.Data<>(date, count));
                }
                else if ( category.equals(dataSeriesProgErr.getName() )){
                    dataSeriesProgErr.getData().add(new XYChart.Data<>(date, count));
                }
                else if ( category.equals(dataSeriesLinkErr.getName() )){
                    dataSeriesLinkErr.getData().add(new XYChart.Data<>(date, count));
                }
                else if ( category.equals(dataSeriesHardwareErr.getName() )){
                    dataSeriesHardwareErr.getData().add(new XYChart.Data<>(date, count));
                }
                else if ( category.equals(dataSeriesServiceRequest.getName() )){
                    dataSeriesServiceRequest.getData().add(new XYChart.Data<>(date, count));
                }
                
                if ((srs == null) || (checkDate == null)){
                    srs = (resultSet.getString(4));
                    checkDate = resultSet.getString(1) + "." + resultSet.getString(2);
                    
                    dataSeriesSrs = new XYChart.Series<>();
                    dataSeriesSrs.setName(srs);
                }
                
                if ( (srs.equals(resultSet.getString(4))) && (checkDate.equals(resultSet.getString(1) + "." + resultSet.getString(2))) ){
                    dataSeriesSrs.getData().add(new XYChart.Data<>(checkDate, resultSet.getInt(3)));
                    barChart.getData().add(dataSeriesSrs);
                }
                else {
                    srs = resultSet.getString(4);
                    checkDate = resultSet.getString(1) + "." + resultSet.getString(2);
                    
                    dataSeriesSrs = null;
                    dataSeriesSrs = new XYChart.Series<>();
                    dataSeriesSrs.setName(srs);
                    dataSeriesSrs.getData().add(new XYChart.Data<>(checkDate, resultSet.getInt(3)));
                    barChart.getData().add(dataSeriesSrs);
                }

                 */
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        barChart.getData().add(dataSeriesUnknown);
        barChart.getData().add(dataSeriesProgErr);
        barChart.getData().add(dataSeriesLinkErr);
        barChart.getData().add(dataSeriesHardwareErr);
        barChart.getData().add(dataSeriesServiceRequest);
    }
    
    public void opencloseSearch(){
        
        barChart.getData().clear();
        xAxis.setLabel("Месяц");
        yAxis.setLabel("Количество");
        
        String state = null;
        String date = null;
        int count;
        
        XYChart.Series<String,Number> dataSeriesOpened = new XYChart.Series<>();
        XYChart.Series<String,Number> dataSeriesClosed = new XYChart.Series<>();

        dataSeriesOpened.setName("Открытые инциденты");
        dataSeriesClosed.setName("Закрытые инциденты");
        
        try {
            Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ServiceDesk", "postgres", "");
            //todo: переделать sql-запрос
            PreparedStatement ps = con.prepareStatement("SELECT extract(month from creationdate), extract(year from creationdate), "
                    + "count(*), state FROM task group by state, extract(month from creationdate), extract(year from creationdate) "
                    + "order by extract(month from creationdate), extract(year from creationdate)");
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                
                state = resultSet.getString(4);
                count = resultSet.getInt(3);
                date = (servicedesk.ServiceDesk.getMonth(resultSet.getInt(1)) + resultSet.getString(2));
                
                /*
                if ( state.equals("Открыта")){
                    dataSeriesOpened.getData().add(new XYChart.Data<>(date, count));
                }
                else if ( state.equals("Закрыта")){
                    dataSeriesClosed.getData().add(new XYChart.Data<>(date, count));
                }
                */
                
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        dataSeriesOpened.getData().add(new XYChart.Data<>("ноябрь 2018", 27));
        dataSeriesOpened.getData().add(new XYChart.Data<>("декабрь 2018", 36));
        dataSeriesOpened.getData().add(new XYChart.Data<>("январь 2019", 30));
        dataSeriesOpened.getData().add(new XYChart.Data<>("февраль 2019", 29));
        dataSeriesOpened.getData().add(new XYChart.Data<>("март 2019", 25));
        dataSeriesOpened.getData().add(new XYChart.Data<>("апрель 2019", 29));
        
        dataSeriesClosed.getData().add(new XYChart.Data<>("ноябрь 2018", 25));
        dataSeriesClosed.getData().add(new XYChart.Data<>("декабрь 2018", 38));
        dataSeriesClosed.getData().add(new XYChart.Data<>("январь 2019", 30));
        dataSeriesClosed.getData().add(new XYChart.Data<>("февраль 2019", 26));
        dataSeriesClosed.getData().add(new XYChart.Data<>("март 2019", 28));
        dataSeriesClosed.getData().add(new XYChart.Data<>("апрель 2019", 26));
        
        barChart.getData().add(dataSeriesOpened);
        barChart.getData().add(dataSeriesClosed);
    }
}