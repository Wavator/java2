import java.net.URL;
import java.util.*;
import java.time.LocalDate;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.fxml.FXML;
import javafx.collections.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import java.io.*;

public class Controller implements Initializable {
    @FXML public VBox mainVBox;
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;
    @FXML private TableView<Quake> tb;
    @FXML private TableColumn<Quake, String> tbId;
    @FXML private TableColumn<Quake, String> tbDate;
    @FXML private TableColumn<Quake, String> tbLat;
    @FXML private TableColumn<Quake, String> tbLon;
    @FXML private TableColumn<Quake, String> tbDep;
    @FXML private TableColumn<Quake, String> tbMag;
    @FXML private TableColumn<Quake, String> tbReg;
    @FXML private Slider magBar;
    @FXML private Label magLabel;
    @FXML private ChoiceBox<String> regSelect;
    @FXML private Text text;
    @FXML private CategoryAxis magX = new CategoryAxis();
    @FXML private NumberAxis   magY = new NumberAxis();
    @FXML private BarChart<String,Number> magBarChart = new BarChart<>(magX,magY);
    @FXML private CategoryAxis lineChartX = new CategoryAxis();
    @FXML private NumberAxis lineChartY = new NumberAxis();
    @FXML private LineChart<String, Number> lineChart = new LineChart<>(lineChartX, lineChartY);
    @FXML private Canvas pic1;
    @FXML private Canvas pic2;

    private XYChart.Series<String, Number> series1 = new XYChart.Series<>();
    private ObservableList<XYChart.Data<String, Number>> chart1 = FXCollections.observableArrayList();
    private XYChart.Series<String, Number> series2 = new XYChart.Series<>();
    private ObservableList<XYChart.Data<String, Number>> chart2 = FXCollections.observableArrayList();
    private ObservableList<Quake> list = FXCollections.observableArrayList();
    private ObservableList<String> choice = FXCollections.observableArrayList();
    private final String[] magString = {"<=1", "2", "3", "4", "5", "6", "7", "8", ">=9"};

    /*
    * Config the controller.
    * */
    private String defaultLang() {
        Locale _locale = Locale.getDefault();
        String locale = _locale.getLanguage();
        if (locale.equals("zh"))
            locale += "_" + _locale.getCountry();
        return locale;
    }

    private static String langName(String langCode) {
        switch (langCode) {
            case "en": return "English";
            case "zh_CN": return "简体中文";
            default: break;
        }
        return null;
    }

    private static String toLangCode(String name) {
        switch (name) {
            case "English": return "en";
            case "简体中文": return "zh_CN";
            default: break;
        }
        return null;
    }

    /**
     * Use Spider to update the data
     */
    public void upload() {
        Spider spider = new Spider();
        text.setText(langCnf.getProperty("Update") + " " +langCnf.getProperty(spider.updateData()));
    }

    @FXML private Label lFrom;
    @FXML private Label lTo;
    @FXML private Label lMagnitude;
    @FXML private Label lRegion;
    @FXML private Button bSearch;
    @FXML private Button bUpdate;
    @FXML private Tab tMap;
    @FXML private Tab tChart1;
    @FXML private Tab tChart2;
    @FXML private Tab tPref;
    @FXML private Label lLang;
    @FXML private Label lDbDir;
    @FXML private Label lUpdateURL;
    @FXML Button bResetPref;
    @FXML Button bApplyPref;

    private static Properties langCnf = new Properties();
    private static Properties mainCnf = new Properties();

    private static BufferedReader readerMain;
    private static BufferedReader reader;

    private void initLang(String locale) throws Exception {
        try {
            reader = new BufferedReader(new FileReader("res/cnf/lang/" + locale + ".cnf"));
            langCnf.load(reader);
        } catch (FileNotFoundException e) {
            System.out.println("Cannot find the locale file.");
            reader = new BufferedReader(new FileReader("res/cnf/lang/en.cnf"));
            langCnf.load(reader);
        }
        lFrom.setText(langCnf.getProperty("From") + ": ");
        lTo.setText(langCnf.getProperty("To") + ": ");
        lMagnitude.setText(langCnf.getProperty("Magnitude") + ">= ");
        lRegion.setText(langCnf.getProperty("Region") + " ");
        bSearch.setText(langCnf.getProperty("Search"));
        bUpdate.setText(langCnf.getProperty("Update"));
        tbId.setText(langCnf.getProperty("ID"));
        tbDate.setText(langCnf.getProperty("UTC_date"));
        tbLat.setText(langCnf.getProperty("Latitude"));
        tbLon.setText(langCnf.getProperty("Longitude"));
        tbDep.setText(langCnf.getProperty("Depth"));
        tbMag.setText(langCnf.getProperty("Magnitude"));
        tbReg.setText(langCnf.getProperty("Region"));
        tMap.setText(langCnf.getProperty("Map"));
        tChart1.setText(langCnf.getProperty("ChartByMagnitude"));
        tChart2.setText(langCnf.getProperty("ChartByDate"));
        tPref.setText(langCnf.getProperty("Preferences"));
        lLang.setText(langCnf.getProperty("Language"));
        lDbDir.setText(langCnf.getProperty("DatabaseFile"));
        lUpdateURL.setText(langCnf.getProperty("UpdateURL"));
        bResetPref.setText(langCnf.getProperty("Reset"));
        bApplyPref.setText(langCnf.getProperty("Apply"));
    }

    @FXML ChoiceBox<String> lang;
    @FXML TextField dbDir;
    @FXML TextField updateURL;

    private ObservableList<String> langSel = FXCollections.observableArrayList();

    /**
     * Implement the initial preferences according to the cnf file
     * @throws Exception if all the cnf files are missing
     * or failed about the IO (such as privilege)
     */
    public void initPreferences() throws Exception {
        String locale;
        try {
            readerMain = new BufferedReader(new FileReader("res/cnf/main.cnf"));
            mainCnf.load(readerMain);
            locale = mainCnf.getProperty("lang");
        } catch (FileNotFoundException e) {
            System.err.println("Cannot find the main config file.");
            return ;
        }
        readerMain.close();
        if (locale == null)
            locale = defaultLang();
        initLang(locale);
        dbDir.setText(mainCnf.getProperty("location"));
        updateURL.setText(mainCnf.getProperty("web_url"));
        langSel.clear();
        langSel.add("English");
        langSel.add("简体中文");
        lang.setItems(langSel);
        lang.setValue(langName(mainCnf.getProperty("lang")));
    }

    /**
     *Using the URL given to initialize the class.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            initPreferences();
        } catch (Exception e) {
            e.printStackTrace();
        }

        dateFrom.setValue(LocalDate.of(2017, 12, 15));
        dateTo.setValue(LocalDate.of(2017,12, 31));
        magBar.setValue(0.0);
        tbId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tbLat.setCellValueFactory(new PropertyValueFactory<>("latitude"));
        tbLon.setCellValueFactory(new PropertyValueFactory<>("longitude"));
        tbDep.setCellValueFactory(new PropertyValueFactory<>("depth"));
        tbMag.setCellValueFactory(new PropertyValueFactory<>("magnitude"));
        tbReg.setCellValueFactory(new PropertyValueFactory<>("region"));
        tbDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        text.setText("0 " + langCnf.getProperty("itemHasBeenFound"));

        magBarChart.getData().add(series1);
        magX.setLabel("Magnitudes");
        magY.setLabel("Amount");
        magY.setTickLabelRotation(90);
        magBarChart.setLegendVisible(false);
        lineChart.getData().add(series2);
        lineChartX.setLabel("Date");
        lineChartY.setLabel("Amount");
        lineChart.setLegendVisible(false);
        tb.setItems(list);
        DataModel dataModel = new DataModel();
        TreeSet<String> regs = dataModel.getRegions();
        choice.addAll(regs);
        regSelect.setItems(choice);
        regSelect.setValue(Quake.WORLDWIDE);
    }

    /**
     * The Methods are used to confirm that the first date is before the second.
     */
    @FXML
    public void getDateAction1() {
        final Callback<DatePicker, DateCell> dayCellFactory =
                new Callback<>() {
                    @Override
                    public DateCell call(final DatePicker datePicker) {
                        return new DateCell() {
                            @Override
                            public void updateItem(LocalDate item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item.isBefore(
                                        dateFrom.getValue())
                                        ) {
                                    setDisable(true);
                                    setStyle("-fx-background-color: #ffc0cb;");
                                }
                            }
                        };
                    }
                };
        dateTo.setDayCellFactory(dayCellFactory);
        dateTo.show();
    }

    @FXML
    public void getDateAction2() {
        final Callback<DatePicker, DateCell> dayCellFactory =
                new Callback<>() {
                    @Override
                    public DateCell call(final DatePicker datePicker) {
                        return new DateCell() {
                            @Override
                            public void updateItem(LocalDate item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item.isAfter(
                                        dateTo.getValue())
                                        ) {
                                    setDisable(true);
                                    setStyle("-fx-background-color: #ffc0cb;");
                                }
                            }
                        };
                    }
                };
        dateFrom.setDayCellFactory(dayCellFactory);
    }

    /**
     * Update the bar chart of magnitudes.
     */
    private void updateChart1() {
        int[] cnt = new int[10];
        try {
            for (Quake q : list) {
                float m = Float.parseFloat(q.getMagnitude());
                cnt[(int) m]++;
            }
        } catch(FormatFlagsConversionMismatchException e) {
            System.err.println(e.getMessage());
        }
        chart1.clear();
        for(int i = 0; i < magString.length; i++) {
            chart1.add(new XYChart.Data<>(magString[i], cnt[i+1]));
        }
        series1.setData(chart1);
    }

    /**
     * Update the line chart of date.
     */
    private void updateChart2() {
        String tmp = null;
        TreeMap<String, Integer> dateMap = new TreeMap<>();
        Set<Integer> setY = new HashSet<>();
        Set<Integer> setM = new HashSet<>();
        int kase = 1;
        try {
            for (Quake q : list) {
                tmp = q.getDate();
                int y = Integer.parseInt(tmp.substring(0, 4));
                int m = Integer.parseInt(tmp.substring(5, 7));
                setY.add(y);
                if(setY.size() == 2) {
                    kase = 3;
                    break;
                }
                setM.add(m);
                if(setM.size()!=1) {
                    kase = 2;
                }
            }
        } catch (FormatFlagsConversionMismatchException e) {
            System.err.println(e.getMessage());
        }
        for(Quake q: list) {
            switch(kase) {
                case 3: tmp = q.getDate().substring(0, 4);
                        break;
                case 2: tmp = q.getDate().substring(5, 7);
                        break;
                case 1: tmp = q.getDate().substring(8, 10);
            }
            if(dateMap.containsKey(tmp)) {
                dateMap.replace(tmp, dateMap.get(tmp) + 1);
            } else {
                dateMap.put(tmp, 1);
            }
        }
        switch(kase) {
            case 1: lineChartX.setLabel("Day /day");
                break;
            case 2: lineChartX.setLabel("Month /month");
                break;
            case 3: lineChartX.setLabel("Year /year");
        }

        chart2.clear();
        try {
            if(!dateMap.isEmpty())tmp = dateMap.firstKey();
            while (tmp != null) {
                chart2.add(new XYChart.Data<>(tmp, dateMap.get(tmp)));
                tmp = dateMap.higherKey(tmp);
            }
        } catch(RuntimeException e) {
            System.err.println(e.getMessage());
        }

        series2.setData(chart2);
    }

    public void updateMagnitude() {
        double mag = magBar.getValue();
        mag = ((int)(mag * 10)) / 10.0;
        magLabel.setText(Double.toString(mag));
    }

    private static double getPic1X(float lon) {
        double ret;
        double deg = 424 / 360.0;
        if (lon >= 0)
            ret = lon * deg;
        else
            ret = 424 + lon * deg;
        return ret;
    }

    private static double getPic1Y(float lat) {
        double deg = 283 / 164.0;
        return 141.5 - deg * lat;
    }

    private static double getPic2X(float lon, double theta) {
        double R = 106, ret;
        ret = 0.4222382 * R * Math.toRadians(lon) * (1 + Math.cos(theta));
        return ret + 282;
    }

    private static double getPic2Y(double theta) {
        double R = 106, ret;
        ret = 1.3265004 * R * Math.sin(theta);
        return 141.5 - ret;
    }

    private static double Eckert4DeltaF(double x, double phi) {
        return (x + Math.sin(x) * Math.cos(x) + 2 * Math.sin(x) - (2 + Math.PI / 2.0) * Math.sin(phi))
            / (1 + Math.cos(2 * x) + 2 * Math.cos(x));
    }

    /**
     * Draw the locations of the earthquakes in the world map
     * @param call: An ArrayList of Quake
     */
    public void drawPic(ArrayList<Quake> call) {
        GraphicsContext gc1 = pic1.getGraphicsContext2D();
        GraphicsContext gc2 = pic2.getGraphicsContext2D();
        gc1.clearRect(0, 0, 424, 283);
        gc1.setFill(Color.RED);
        gc2.clearRect(0, 0, 564, 283);
        gc2.setFill(Color.RED);
        for (Quake q : call) {
            float lon = q.getLongitude().value();
            float lat = q.getLatitude().value();
            gc1.fillOval(getPic1X(lon), getPic1Y(lat), 5, 5);
            if (lon >= 0)
                lon -= 180;
            else
                lon += 180;
            double theta = Math.toRadians(lat) / 2.0;
            for (int i = 1; i <= 50; ++ i)
                theta = theta - Eckert4DeltaF(theta, Math.toRadians(lat));
            gc2.fillOval(getPic2X(lon, theta), getPic2Y(theta), 5, 5);
        }
    }

    /**
     * Provide the result of searching.
     * Draw the world map.
     * Create two charts.
    */
    @FXML
    public void showText() {
        list.clear();
        chart1.clear();
        chart2.clear();
        try {
            LocalDate from = dateFrom.getValue();
            LocalDate to = dateTo.getValue();
            try {
                if (from.isAfter(to)) {
                    throw new ProjErr("from > to");
                }
            } catch (ProjErr pe) {
                System.err.println(pe.getMessage());
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle(langCnf.getProperty("Error"));
                a.setHeaderText(langCnf.getProperty("DateFormatError"));
                a.setContentText(langCnf.getProperty("FromDateCannotBeLaterThanToDate"));
                a.show();
                return;
            }
            float mag = (float) magBar.getValue();
            mag = (float) (((int) (mag * 10)) / 10.0);
            DataModel dataModel = new DataModel();
            ArrayList<Quake> call = dataModel.getQuakes(
                from.toString(),
                to.toString(),
                mag,
                regSelect.getValue());
            list.addAll(call);
            updateChart1();
            updateChart2();
            drawPic(call);
        } catch(Exception e) {
            if(list.isEmpty()) return;
            updateChart1();
            updateChart2();
        }
        try {
            if (list.size() != 0 && list.size() != 1) text.setText(list.size() + " " + langCnf.getProperty("itemHasBeenFound"));
            else text.setText(list.size() + " " + langCnf.getProperty("itemsHaveBeenFound"));
        } catch(Exception e) {
            text.setText("0 " + langCnf.getProperty("itemHasBeenFound"));
        }
    }

    /**
     * Reset the preferences according to the original cnf file
     */
    @FXML
    private void resetPref() throws Exception {
        initPreferences();
    }

    /**
     * Write the new preferences to the cnf file
     */
    @FXML
    private void applyPref() throws Exception {
        mainCnf.setProperty("lang", toLangCode(lang.getValue()));
        mainCnf.setProperty("location", dbDir.getText());
        mainCnf.setProperty("web_url", updateURL.getText());
        FileOutputStream outputFile = new FileOutputStream("res/cnf/main.cnf");
        mainCnf.store(outputFile, "");
        outputFile.close();
        initPreferences();
    }
}
