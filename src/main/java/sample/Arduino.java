package sample;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Arduino {
    private SerialPort comPort;
    private ConexionMySQL conexion;

    public Arduino(ConexionMySQL conexionMySQL){
        conexion = conexionMySQL;
        try {
            conexion.conectar();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        getValueSerial();
    }

    private void getValueSerial(){
        comPort = SerialPort.getCommPorts()[1];
        System.out.println(comPort.getSystemPortName());
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
                byte[] buffer = new byte[4];
                String message = "";
                int len = 0;
                try {
                    len = in.read(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (len > 0) {
                    message = new String(buffer).trim();
                }
                try {
//                    voltage = Integer.parseInt(message.trim());
                    if(!message.isEmpty()) {
                        ResultSet resultSet = conexion.consultar("INSERT INTO estado VALUES(1, 80.00, 0, 1) ON DUPLICATE KEY UPDATE humedad=80.00, lluvia=0, encendido=1");
                        System.out.println("Mensaje: " +message.trim());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
