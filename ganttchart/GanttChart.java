package servicedesk.ganttchart;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javafx.beans.NamedArg;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class GanttChart<X,Y> extends XYChart<X,Y> {

    public static class ExtraData {

        public long length;
        public String styleClass;


        public ExtraData(long lengthMs, String styleClass) {
            super();
            this.length = lengthMs;
            this.styleClass = styleClass;
        }
        public long getLength() {
            return length;
        }
        public void setLength(long length) {
            this.length = length;
        }
        public String getStyleClass() {
            return styleClass;
        }
        public void setStyleClass(String styleClass) {
            this.styleClass = styleClass;
        }


    }

    private double blockHeight = 10;

    public GanttChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis) {
        this(xAxis, yAxis, FXCollections.<XYChart.Series<X, Y>>observableArrayList());
    }

    public GanttChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis, @NamedArg("data") ObservableList<XYChart.Series<X,Y>> data) {
        super(xAxis, yAxis);
        if (!(xAxis instanceof DateAxis && yAxis instanceof CategoryAxis)) {
            throw new IllegalArgumentException("Axis type incorrect, X and Y should both be NumberAxis");
        }
        setData(data);
    }

    private static String getStyleClass( Object obj) {
        return ((ExtraData) obj).getStyleClass();
    }

    private static double getLength( Object obj) {
        return ((ExtraData) obj).getLength();
    }

    @Override protected void layoutPlotChildren() {

      for (int seriesIndex=0; seriesIndex < getData().size(); seriesIndex++) {

            XYChart.Series<X,Y> series = getData().get(seriesIndex);

            Iterator<XYChart.Data<X,Y>> iter = getDisplayedDataIterator(series);
            while(iter.hasNext()) {
                XYChart.Data<X,Y> item = iter.next();
                double x = getXAxis().getDisplayPosition(item.getXValue());
                double y = getYAxis().getDisplayPosition(item.getYValue());
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }
                Node block = item.getNode();
                Rectangle ellipse;
                if (block != null) {
                    if (block instanceof StackPane) {
                        StackPane region = (StackPane)item.getNode();
                        if (region.getShape() == null) {
                            ellipse = new Rectangle( getLength( item.getExtraValue()), getBlockHeight());
                        } else if (region.getShape() instanceof Rectangle) {
                            ellipse = (Rectangle)region.getShape();
                        } else {
                            return;
                        }
                        ellipse.setWidth( getLength( item.getExtraValue()) * ((getXAxis() instanceof NumberAxis) ? Math.abs(((NumberAxis)getXAxis()).getScale()) : 1));
                        ellipse.setHeight(getBlockHeight() * ((getYAxis() instanceof NumberAxis) ? Math.abs(((NumberAxis)getYAxis()).getScale()) : 1));
                        y -= getBlockHeight() / 2.0;

                        // Note: workaround for RT-7689 - saw this in ProgressControlSkin
                        // The region doesn't update itself when the shape is mutated in place, so we
                        // null out and then restore the shape in order to force invalidation.
                        region.setShape(null);
                        region.setShape(ellipse);
                        region.setScaleShape(false);
                        region.setCenterShape(false);
                        region.setCacheShape(false);

                        block.setLayoutX(x);
                        block.setLayoutY(y);
                    }
                }
            }
        }
    }

    public double getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight( double blockHeight) {
        this.blockHeight = blockHeight;
    }

    @Override protected void dataItemAdded(XYChart.Series<X,Y> series, int itemIndex, XYChart.Data<X,Y> item) {
        Node block = createContainer(series, getData().indexOf(series), item, itemIndex);
        getPlotChildren().add(block);
    }

    @Override protected  void dataItemRemoved(final XYChart.Data<X,Y> item, final XYChart.Series<X,Y> series) {
        final Node block = item.getNode();
            getPlotChildren().remove(block);
            removeDataItemFromDisplay(series, item);
    }

    @Override protected void dataItemChanged(XYChart.Data<X, Y> item) {
    }

    @Override protected  void seriesAdded(XYChart.Series<X,Y> series, int seriesIndex) {
        for (int j=0; j<series.getData().size(); j++) {
            XYChart.Data<X,Y> item = series.getData().get(j);
            Node container = createContainer(series, seriesIndex, item, j);
            getPlotChildren().add(container);
        }
    }

    @Override protected  void seriesRemoved(final XYChart.Series<X,Y> series) {
        for (XYChart.Data<X,Y> d : series.getData()) {
            final Node container = d.getNode();
            getPlotChildren().remove(container);
        }
        removeSeriesFromDisplay(series);

    }


    private Node createContainer(XYChart.Series<X, Y> series, int seriesIndex, final XYChart.Data<X,Y> item, int itemIndex) {

        Node container = item.getNode();

        if (container == null) {
            container = new StackPane();
            item.setNode(container);
        }

        container.getStyleClass().add( getStyleClass( item.getExtraValue()));

        return container;
    }

    @Override protected void updateAxisRange() {
        final Axis<X> xa = getXAxis();
        final Axis<Y> ya = getYAxis();
        List<X> xData = null;
        List<Y> yData = null;
        if(xa.isAutoRanging()) xData = new ArrayList<X>();
        if(ya.isAutoRanging()) yData = new ArrayList<Y>();
        if(xData != null || yData != null) {
            for(XYChart.Series<X,Y> series : getData()) {
                for(XYChart.Data<X,Y> data: series.getData()) {
                    if(xData != null) {
                        xData.add(data.getXValue());
                        xData.add(xa.toRealValue(xa.toNumericValue(data.getXValue()) + getLength(data.getExtraValue())));
                    }
                    if(yData != null){
                        yData.add(data.getYValue());
                    }
                }
            }
            if(xData != null) xa.invalidateRange(xData);
            if(yData != null) ya.invalidateRange(yData);
        }
    }
    
    
    //-----------------------------------отсюда начинается мой код---------------------------------------------------------------------------------------
    static public void buildChart(Stage stage, int employeeId, String cmbValue, String style) throws SQLException {

        Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ServiceDesk", "postgres", "");

        int taskCount = 0;
        int OutOfDateCount = 0;

        ObservableList<String> tasksId = null;
        try {
            PreparedStatement ps = null;
            PreparedStatement psCount = null;
            PreparedStatement psOutOfDateCount = null;

            if (employeeId == 0) {
                ps = con.prepareStatement("SELECT id FROM task ORDER BY id DESC");
                psCount = con.prepareStatement("SELECT count(*) FROM task Where state = 'Открыта' ");
                psOutOfDateCount = con.prepareStatement("SELECT count(*) FROM task Where state = 'Открыта' and closingdate < clock_timestamp()");

            } else {
                ps = con.prepareStatement("SELECT id FROM task WHERE idmaster=" + employeeId + "ORDER BY id DESC");
                psCount = con.prepareStatement("SELECT count(*) FROM task WHERE idmaster=" + employeeId + " and state = 'Открыта'");
                psOutOfDateCount = con.prepareStatement("SELECT count(*) FROM task WHERE idmaster=" + employeeId + " and state = 'Открыта' and closingdate < clock_timestamp()");
            }
            ResultSet resultSet = ps.executeQuery();
            tasksId = FXCollections.observableArrayList();

            while (resultSet.next()) {
                int id = resultSet.getInt(1);
                tasksId.add("Заявка №" + id);
            }

            resultSet = psCount.executeQuery();
            while (resultSet.next()) {
                taskCount = resultSet.getInt(1);
            }

            resultSet = psOutOfDateCount.executeQuery();
            while (resultSet.next()) {
                OutOfDateCount = resultSet.getInt(1);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        ObservableList<String> employeeList = null;
        try {
            PreparedStatement ps = con.prepareStatement("SELECT id,initials FROM employee ORDER BY id");
            ResultSet resultSet = ps.executeQuery();
            employeeList = FXCollections.observableArrayList();
            employeeList.add("0: Общий обзор");
            while (resultSet.next()) {
                int id = resultSet.getInt(1);
                String initials = resultSet.getString(2);
                employeeList.add(id + ": " + initials);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        final DateAxis xAxis = new DateAxis();
        final CategoryAxis yAxis = new CategoryAxis();

        final GanttChart<Date, String> chart = new GanttChart<Date, String>(xAxis, yAxis);
        xAxis.setLabel("");
        xAxis.setTickLabelFill(Color.BLACK);

        yAxis.setLabel("");
        yAxis.setTickLabelFill(Color.BLACK);
        yAxis.setTickLabelGap(10);
        yAxis.setCategories(tasksId);

        chart.setLegendVisible(false);
        chart.setBlockHeight(300 / tasksId.size());

        Date createDate;
        Date closeDate;

        Date maxDate = null;
        Date minDate = null;
        long duration;

        String state;

        GregorianCalendar opcalen = new GregorianCalendar();
        GregorianCalendar clcalen = new GregorianCalendar();

        try {
            PreparedStatement ps = null;
            if (employeeId == 0) {
                ps = con.prepareStatement("SELECT id, creationdate, closingdate, state FROM task ORDER BY id DESC");
            } else {
                ps = con.prepareStatement("SELECT id, creationdate, closingdate, state FROM task WHERE idmaster=" + employeeId + " ORDER BY id DESC");
            }

            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {

                int id = resultSet.getInt(1);
                createDate = resultSet.getDate(2);
                closeDate = resultSet.getDate(3);
                state = resultSet.getString(4);

                opcalen.setTime(createDate);
                clcalen.setTime(closeDate);

                if ((maxDate == null) || (minDate == null)) {
                    maxDate = createDate;
                    minDate = createDate;
                }

                if (createDate.before(minDate)) {
                    minDate = createDate;
                }
                if (closeDate.after(maxDate)) {
                    maxDate = closeDate;
                }

                duration = (clcalen.getTimeInMillis() - opcalen.getTimeInMillis() + 86400000) / 864000;

                XYChart.Series series = new XYChart.Series();
                series.getData().add(new XYChart.Data(opcalen.getTime(), "Заявка №" + id, new GanttChart.ExtraData(duration, getStatus(closeDate, state))));
                clcalen.add(Calendar.DAY_OF_MONTH, 2);
                series.getData().add(new XYChart.Data(clcalen.getTime(), "Заявка №" + id, new GanttChart.ExtraData(0, "status-red")));

                chart.getData().add(series);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        con.close();

        chart.getStylesheets().add(style);
        ScrollPane scrPane = new ScrollPane();

        chart.setPrefWidth(((maxDate.getTime() - minDate.getTime()) / (24 * 60 * 60 * 1000) + 3) * 102);
        //разница между максимальной и минимальной датой, делённая на количество миллисекунд в дне, +3 дня в запас для видимости крайних полос справа, 
        //*102 пикселя на каждое деление полосы

        scrPane.setContent(chart);
        scrPane.setPrefWidth(800);
        scrPane.setPrefHeight(450);
        scrPane.setLayoutX(30);
        scrPane.setLayoutY(30);
        scrPane.getStylesheets().add(style);

        Label masterLabel = new Label("Мастер: ");
        Label activeLabel = new Label("Активных заявок: " + taskCount);
        Label rottenLabel = new Label("Просроченных заявок: " + OutOfDateCount);
        masterLabel.setLayoutX(30);
        masterLabel.setLayoutY(500);
        activeLabel.setLayoutX(30);
        activeLabel.setLayoutY(530);
        rottenLabel.setLayoutX(30);
        rottenLabel.setLayoutY(560);

        activeLabel.getStylesheets().add(style);
        masterLabel.getStylesheets().add(style);
        rottenLabel.getStylesheets().add(style);

        Label fromLabel = new Label("От");
        Label toLabel = new Label("До");
        fromLabel.setStyle("-fx-font-size: 14; -fx-text-fill: white; -fx-font-family: Tahoma");
        fromLabel.setLayoutX(550);
        fromLabel.setLayoutY(500);
        toLabel.setStyle("-fx-font-size: 14; -fx-text-fill: white; -fx-font-family: Tahoma");
        toLabel.setLayoutX(550);
        toLabel.setLayoutY(545);

        DatePicker fromDate = new DatePicker();
        DatePicker toDate = new DatePicker();
        fromDate.getStylesheets().add(style);
        fromDate.setLayoutX(575);
        fromDate.setLayoutY(495);
        fromDate.setPrefWidth(255);
        toDate.getStylesheets().add(style);
        toDate.setLayoutX(575);
        toDate.setLayoutY(540);
        toDate.setPrefWidth(255);

        ComboBox employeeBox = new ComboBox(employeeList);

        employeeBox.getStylesheets().add(style);
        employeeBox.setLayoutX(100);
        employeeBox.setLayoutY(495);
        employeeBox.setPrefWidth(170);
        employeeBox.setValue(cmbValue);

        employeeBox.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                String sub = (String) employeeBox.getValue();
                sub = sub.substring(0, sub.indexOf(":"));
                stage.close();
                try {
                    buildChart(stage, Integer.parseInt(sub), (String) employeeBox.getValue(), style);
                } catch (SQLException ex) {
                }
            }
        });

        AnchorPane root = new AnchorPane();
        root.getChildren().add(scrPane);
        root.getChildren().add(employeeBox);
        root.getChildren().add(masterLabel);
        root.getChildren().add(activeLabel);
        root.getChildren().add(rottenLabel);

        root.getChildren().add(fromLabel);
        root.getChildren().add(toLabel);
        root.getChildren().add(toDate);
        root.getChildren().add(fromDate);

        root.setStyle("-fx-background-color:rgba(42,45,54,1)");

        Scene scene = new Scene(root, 860, 620);
        stage.setScene(scene);
        stage.show();
    }

    static public String getStatus(Date closing, String state) {
        if ((closing.before(new Date())) && (state.equals("Открыта"))) {
            return ("status-red");
        } else if ((closing.after(new Date())) && (state.equals("Открыта"))) {
            return ("status-blue");
        } else if (state.equals("Закрыта")) {
            return ("status-green");
        }
        return null;
    }
    

}
