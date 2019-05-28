package sample;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Controller  extends Application{
    private static ConexionMySQL objConexion;
    private Alert resultado;
    private Connection conexion;
    private static final int MAX_DATA_POINTS = 20;
    private int xSeriesData = 0;
    private XYChart.Series<Number, Number> series1 = new XYChart.Series<>();
    private ExecutorService executor;
    private ConcurrentLinkedQueue<Number> dataQ1 = new ConcurrentLinkedQueue<>();
    private SerialPort comPort;
    private int voltage = 0;

    @FXML
    LineChart<Number, Number> lc_intake;
    @FXML
    private NumberAxis xAxis;
    @FXML
    Button btnActivate;
public void initialize(){

    String estado = conectarSQL();
    System.out.println(estado);

    makeLineChart();
//    getValueSerial();

    executor = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    });

    AddToQueue addToQueue = new AddToQueue();
    executor.execute(addToQueue);
    //-- Prepare Timeline
    prepareTimeline();
}


    private void makeLineChart(){
        xAxis = new NumberAxis(0, MAX_DATA_POINTS, MAX_DATA_POINTS / 10);
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarkVisible(false);
        xAxis.setMinorTickVisible(false);



        lc_intake.setTitle("Consumo diario");
        lc_intake.setHorizontalGridLinesVisible(true);

        // Set Name for Series
        series1.setName("Agua en litros");

        // Add Chart Series
        lc_intake.getData().addAll(series1);
    }
    private void getValueSerial(){
                comPort = SerialPort.getCommPorts()[0];
        comPort.openPort();
        boolean b = comPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                    return;
                InputStream in = comPort.getInputStream();
                byte[] buffer = new byte[3];
                String message = "";
                int len = 0;
                try {
                    len = in.read(buffer);
                } catch (IOException e) {
                }
                if (len > 0) {
                    message = new String(buffer);
                }
                try {
                    voltage = Integer.parseInt(message.trim());
//                    System.out.println(voltage);
                } catch (NumberFormatException ignored) {
                }
            }
        });
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

    private class AddToQueue implements Runnable {
        public void run() {
            try {
//                dataQ1.add(voltage);
                int randomN= (int) (100 * Math.random() - 5);
          dataQ1.add(randomN);

                Thread.sleep(300);
                executor.execute(this);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    //-- Timeline gets called in the JavaFX Main thread
    private void prepareTimeline() {
        // Every frame to take any data from queue and add to chart
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                addDataToSeries();
            }
        }.start();
    }

    private void addDataToSeries() {
        for (int i = 0; i < 20; i++) { //-- add 20 numbers to the plot+
            if (dataQ1.isEmpty()) break;
            series1.getData().add(new XYChart.Data<>(xSeriesData++, dataQ1.remove()));
        }
        // remove points to keep us at no more than MAX_DATA_POINTS
        if (series1.getData().size() > MAX_DATA_POINTS) {
            series1.getData().remove(0, series1.getData().size() - MAX_DATA_POINTS);
        }
        // update
        xAxis.setLowerBound(xSeriesData - MAX_DATA_POINTS);
        xAxis.setUpperBound(xSeriesData - 1);
    }


    @Override
    public void start(Stage primaryStage) {

    }

    public void stop() {
//        comPort.closePort();
    }


}
