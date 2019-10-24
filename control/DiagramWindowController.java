
package servicedesk.control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import servicedesk.ganttchart.GanttChart;

public class DiagramWindowController {
    
    @FXML
    private void ganttBtnAction(ActionEvent event) throws Exception {
        Stage stage = new Stage();
        stage.setTitle("Диаграмма Гантта");
        GanttChart.buildChart(stage,0,null,getClass().getResource("/servicedesk/ganttchart/ganttchart.css").toExternalForm());
    }
    
    @FXML
    private void pieChartBtnAction(ActionEvent event) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/servicedesk/view/FXMLPieChartWindow.fxml"));
        servicedesk.ServiceDesk.setUpWindow(root, event, "Круговая диаграмма");
    }
    @FXML
    private void barChartBtnAction(ActionEvent event) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/servicedesk/view/FXMLBarChart.fxml"));
        servicedesk.ServiceDesk.setUpWindow(root, event, "Гистограмма");
    }
    @FXML
    private void areaChartBtnAction(ActionEvent event) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/servicedesk/view/FXMLAreaChart.fxml"));
        servicedesk.ServiceDesk.setUpWindow(root, event, "Диаграмма с областями");
    }
    
}
