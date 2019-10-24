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
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;

public class FXMLAreaChartController implements Initializable {

    @FXML
    AreaChart areaChart;

    @FXML
    CategoryAxis xAxis;

    @FXML
    NumberAxis yAxis;

    @FXML
    ComboBox searchBox;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        xAxis.setLabel("Дата");
        yAxis.setLabel("Количество");
        
        ObservableList<String> searchTypes = FXCollections.observableArrayList("По категориям", "По состоянию");
        searchBox.setItems(searchTypes);
        
        yAxis.setAutoRanging(false);
        
        openCloseSearch();
        

    }

    @FXML
    public void searchTypeAction() {
        if (searchBox.getValue().equals("По категориям")) {
            categorySearch();
            yAxis.setUpperBound(10);
        } else {
            openCloseSearch();
            yAxis.setUpperBound(18);
        }
        yAxis.setTickUnit(1);
    }

    public void openCloseSearch() {
        areaChart.getData().clear();

        XYChart.Series<String, Number> dataSeriesOpened = new XYChart.Series<>();
        XYChart.Series<String, Number> dataSeriesClosed = new XYChart.Series<>();
         
        dataSeriesOpened.setName("Открытые заявки");
        dataSeriesClosed.setName("Закрытые заявки");
        
        String state = null;
        String date = null;
        int count;
        String state1 = null;
        String date1 = null;
        
        try {
            Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ServiceDesk", "postgres", "");
            //todo: переделать sql-запрос
            PreparedStatement ps = con.prepareStatement("SELECT creationdate, count(*), state FROM task group by state, creationdate order by creationdate");
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                state = resultSet.getString(3);
                count = resultSet.getInt(2);
                date = resultSet.getDate(1).toString();
                
//                if (state.equals("Открыта")){
//                    dataSeriesOpened.getData().add(new XYChart.Data<>(date, count));
//                    
//                }
//                else if (state.equals("Закрыта")){
//                    dataSeriesClosed.getData().add(new XYChart.Data<>(date, count));
//                }   
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        String[] dates = new String[]{"1 апр '19", "2 апр '19", "3 апр '19", "4 апр '19", "5 апр '19", "6 апр '19", "7 апр '19", "8 апр '19", "9 апр '19", "10 апр '19"};
        int[] count0 = new int[]{10, 8, 11, 12, 14, 17, 11, 10, 5, 10};
        int[] count1 = new int[]{10, 7, 11, 12, 12, 17, 4, 10, 1, 1};
        
        for (int i = 0; i < 10; i++) {
            dataSeriesOpened.getData().add(new XYChart.Data<>(dates[i], count0[i]));
            dataSeriesClosed.getData().add(new XYChart.Data<>(dates[i], count1[i]));
        }
        
        areaChart.getData().addAll(dataSeriesClosed,dataSeriesOpened);
    }

    public void categorySearch() {
        areaChart.getData().clear();
        
        String category = null;
        String date = null;
        int count;

        XYChart.Series<String, Number> dataSeriesUnknown = new XYChart.Series<>();
        XYChart.Series<String, Number> dataSeriesProgErr = new XYChart.Series<>();
        XYChart.Series<String, Number> dataSeriesLinkErr = new XYChart.Series<>();
        XYChart.Series<String, Number> dataSeriesHardwareErr = new XYChart.Series<>();
        XYChart.Series<String, Number> dataSeriesServiceRequest = new XYChart.Series<>();

        dataSeriesUnknown.setName("Неопределено");
        dataSeriesProgErr.setName("Программные ошибки");
        dataSeriesLinkErr.setName("Ошибки связи");
        dataSeriesHardwareErr.setName("Неисправность оборудования");
        dataSeriesServiceRequest.setName("Запрос на обслуживание");

        try {
            Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ServiceDesk", "postgres", "");
            //todo: переделать sql-запрос
            PreparedStatement ps = con.prepareStatement("SELECT creationdate, count(*), category FROM task group by category, creationdate order by creationdate");
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                category = resultSet.getString(3);
                count = resultSet.getInt(2);
                date = resultSet.getDate(1).toString();

//                if (category.equals(dataSeriesUnknown.getName())) {
//                    dataSeriesUnknown.getData().add(new XYChart.Data<>(date, count));
//                } else if (category.equals(dataSeriesProgErr.getName())) {
//                    dataSeriesProgErr.getData().add(new XYChart.Data<>(date, count));
//                } else if (category.equals(dataSeriesLinkErr.getName())) {
//                    dataSeriesLinkErr.getData().add(new XYChart.Data<>(date, count));
//                } else if (category.equals(dataSeriesHardwareErr.getName())) {
//                    dataSeriesHardwareErr.getData().add(new XYChart.Data<>(date, count));
//                } else if (category.equals(dataSeriesServiceRequest.getName())) {
//                    dataSeriesServiceRequest.getData().add(new XYChart.Data<>(date, count));
//                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        String[] dates = new String[]{"1 апр '19", "2 апр '19", "3 апр '19", "4 апр '19", "5 апр '19", "6 апр '19", "7 апр '19", "8 апр '19", "9 апр '19", "10 апр '19"};
        int[] count0 = new int[]{3,6,8,3,4,7,1,0,5,0};
        int[] count1 = new int[]{0,0,1,3,5,7,4,0,1,1};
        int[] count2 = new int[]{3,5,7,8,2,7,9,1,5,6};
        int[] count3 = new int[]{0,0,0,1,2,5,7,3,8,1};
        int[] count4 = new int[]{1,5,6,7,9,3,5,4,4,2};
        
        for (int i = 0; i < 10; i++) {
            dataSeriesUnknown.getData().add(new XYChart.Data<>(dates[i], count0[i]));
            dataSeriesProgErr.getData().add(new XYChart.Data<>(dates[i], count1[i]));
            dataSeriesLinkErr.getData().add(new XYChart.Data<>(dates[i], count2[i]));
            dataSeriesHardwareErr.getData().add(new XYChart.Data<>(dates[i], count3[i]));
            dataSeriesServiceRequest.getData().add(new XYChart.Data<>(dates[i], count4[i]));
        }

        areaChart.getData().add(dataSeriesUnknown);
        areaChart.getData().add(dataSeriesProgErr);
        areaChart.getData().add(dataSeriesLinkErr);
        areaChart.getData().add(dataSeriesHardwareErr);
        areaChart.getData().add(dataSeriesServiceRequest);
    }
}
