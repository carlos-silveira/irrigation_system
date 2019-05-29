package sample;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class Arduino {
    private SerialPort comPort;
    private ConexionMySQL conexion;

    private  int ultimoVolumen = 0;
    private String ultimaFecha = "";

    private Boolean encender = false;

    public Arduino(ConexionMySQL conexionMySQL){
        conexion = conexionMySQL;
        try {
            conexion.conectar();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        getValueSerial();
        Thread t = new Thread(() -> {
            while (true) {
                revisarComunicacion();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });
        t.start();
    }

    private void revisarComunicacion() {
        ResultSet r  =conexion.consultar("Select * from comunicacion where id = 1");
        try {
            if(r.next()){
                boolean encenderDB = r.getBoolean(2);
                if(encenderDB != encender) {
                    OutputStream out = comPort.getOutputStream();
                    out.write("a".getBytes());
                    out.close();
                    encender = encenderDB;
                }
                System.out.println("Encender: "+encender);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                byte[] buffer = new byte[8];
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
                    String[] datos = message.split("\\|");

                    if(datos.length == 4) {
                        ResultSet resultSet = conexion.consultar("INSERT INTO estado VALUES(1," +datos[1]+ "," +datos[2]+ ","+datos[0]+") ON DUPLICATE KEY UPDATE humedad=" +datos[1]+ ", lluvia=" +datos[2]+ ", encendido="+datos[0]);
                        int volumen = Integer.parseInt(datos[3]) - ultimoVolumen;
                        LocalDateTime fecha = LocalDateTime.now();
                        String fechaStr = fecha.toString().split(":")[0];
                        System.out.println(fecha.toString().split(":")[0]);
                        if(ultimaFecha.equals(fechaStr)) {
                            conexion.consultar("Update consumo set litros = litros + "+volumen+" where fecha = '"+fechaStr+":00'");
                        } else {
                            ResultSet r = conexion.consultar("select * from consumo where fecha = '"+fechaStr+":00'");
                            r.last();
                            if(r.getRow() > 0) {
                                System.out.println("Actualizando");
                                conexion.consultar("Update consumo set litros = litros + "+volumen+" where fecha = '"+fechaStr+":00'");
                            } else {
                                System.out.println("Insertando");
                                conexion.consultar("insert into consumo values (0 , '"+fechaStr+":00', " +volumen+")");

                            }
                        }
                        ultimoVolumen += volumen;
                        ultimaFecha = fechaStr;
                        System.out.println("Mensaje: " +message.trim());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
