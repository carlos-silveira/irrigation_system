package sample;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

public class Controller  extends Application{
    private static ConexionMySQL objConexion;
    private Alert resultado;
    private Connection conexion;
    final int WINDOW_SIZE = 10;
    private ScheduledExecutorService scheduledExecutorService;

    private static final int MAX_DATA_POINTS = 20;
    private int xSeriesData = 0;
    private XYChart.Series<String, Number> series1 = new XYChart.Series<>();
    private ExecutorService executor;
    private ConcurrentLinkedQueue<Number> dataQ1 = new ConcurrentLinkedQueue<>();

    private int voltage = 0;
    private long lastTimerCall;
    private AnimationTimer timer;
    int randomN;
    public int status,rain;
    private DateFormat dateFormat;
    private boolean comunicacionEncender = false;

    @FXML
    LineChart<String, Number> lc_intake;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    NumberAxis yAxis;
    @FXML
    Button btnActivate;
    @FXML
    Label humiditylb,rainlb,statuslb ;
public void initialize(){

    String estado = conectarSQL();
    System.out.println(estado);

    makeLineChart();
//    getValueSerial();

    getData();
    btnActivate.setOnAction(actionEvent -> activarSistema());
    Thread hiloBoton = new Thread(() -> escucharCambioEnComunicacion());
    hiloBoton.start();

}

    private void getData(){
        String query = "SELECT * FROM estado WHERE ID=1";
        ResultSet data = objConexion.consultar(query);

        try {
            while (data.next()) {
                humiditylb.setText(data.getString("humedad"));
                rain = Integer.parseInt(data.getString("lluvia"));
                if (rain == 0 ){
                    rainlb.setText("No esta lloviendo.");
                }else if (rain == 1){
                    rainlb.setText("Esta lloviendo.");
                }
                status= Integer.parseInt(data.getString("encendido"));
                if (status == 0 ){
                    statuslb.setText("Esta apagado.");
                }else if (status == 1){
                    statuslb.setText("Esta encendido.");
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    static final class Consumption {
        private final double liters;
        private  Date date;

        public Consumption(double liters, Date date) {
            this.liters = liters;
            this.date = date;
        }

        public double getLiters() {
            return liters;
        }

        public Date getDate() {
            return date;
        }
    }
    public List<Consumption> getValues() {
        double lit = 0;
        String d="31/12/1998";
        List<Consumption> respuesta = new ArrayList<>();

        String query2 = "SELECT * FROM consumo ";
        ResultSet data = objConexion.consultar(query2);

        try {
            while (data.next()) {

                lit= Double.parseDouble(data.getString("litros"));
                d= data.getString("fecha");
                Date da = null;
                try {
                    dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
                    da = dateFormat.parse(d);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                respuesta.add(new Consumption(lit, da) );


            }
        } catch (SQLException e) {
            e.printStackTrace();
        }



        return respuesta;
    }
    private void makeLineChart(){
        xAxis = new CategoryAxis();

        yAxis = new NumberAxis();


        lc_intake.setTitle("Consumo diario");
        lc_intake.setHorizontalGridLinesVisible(true);
        lc_intake.setAnimated(false);

        // Set Name for Series
        series1.setName("Agua en litros");

        // Add Chart Series
        lc_intake.getData().addAll(series1);
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

        // setup a scheduled executor to periodically put data into the chart
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        // put dummy data onto graph per second
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            // get a random integer between 0-10
            Integer random = ThreadLocalRandom.current().nextInt(10);

            // Update the chart
            Platform.runLater(() -> {
                // get current time
                Date now = new Date();
                // put random number with current time
                List<Consumption> results = getValues();
                dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
                series1.getData().clear();
                for(Consumption result:results) {

                    System.out.println(result.getLiters() +"___"+  dateFormat.format(result.getDate()));


                    series1.getData().add(new XYChart.Data<>(simpleDateFormat.format(result.getDate()), result.getLiters()));
                }


                if (series1.getData().size() > WINDOW_SIZE)
                    series1.getData().remove(0);
            });
        }, 0, 1, TimeUnit.SECONDS);

    }

    private String conectarSQL() {
        String estado = null;
        /* Guardamos el objeto de la conexion como la conexion en si porque
         * mas adelante necesitamos ambos. */
//        objConexion = new ConexionMySQL("u5818148_carlos", "=jkst[RHJunx", "u5818148_criptas", "62.210.247.90", 3306);
        objConexion = new ConexionMySQL("root", "", "sistemaRiego", "localhost", 3306);
        try {
            estado = objConexion.conectar();
            conexion = objConexion.getConexion();
        } catch (ClassNotFoundException e) {
            resultado = new Alert(Alert.AlertType.ERROR, "Error en la conexion: " + e.getMessage());
            resultado.show();
        }
        return estado;
    }
    @Override
    public void start(Stage primaryStage) {
    }
    public void stop() throws Exception {
//        comPort.closePort();
        super.stop();
        scheduledExecutorService.shutdownNow();
    }

    public void activarSistema() {
        ResultSet data = objConexion.consultar("Select * from comunicacion");
        try {
            if(data.next()) {
                String query = "Update comunicacion set encender = ";
                if(data.getBoolean("encender")) {
                    query += "false";
                } else {
                    query += "true";
                }
                objConexion.consultar(query);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void escucharCambioEnComunicacion() {
        while (true) {
            ResultSet data = objConexion.consultar("Select * from comunicacion");
            try {
                if(data.next()) {
                    if(comunicacionEncender != data.getBoolean("encender")) {
                        if(comunicacionEncender) {
                            Platform.runLater(() -> {
                                btnActivate.setText("Activar el sistema de riego");
                                btnActivate.getStyleClass().remove("buttondanger");
                                btnActivate.getStyleClass().add("buttonadd");
                            });
                            comunicacionEncender = false;
                        } else {
                            Platform.runLater(() -> {
                                btnActivate.setText("Desactivar el sistema de riego");
                                btnActivate.getStyleClass().remove("buttonadd");
                                btnActivate.getStyleClass().add("buttondanger");
                            });
                            comunicacionEncender = true;
                        }
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
