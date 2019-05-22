package sample;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.sql.Connection;

public class Controller  {
    private static ConexionMySQL objConexion;
    private Alert resultado;
    private Connection conexion;


    public void initialize() {
        // search panel
        String estado = conectarSQL();
        System.out.println(estado);
    }
    private String conectarSQL() {
        String estado = null;
        /* Guardamos el objeto de la conexion como la conexion en si porque
         * mas adelante necesitamos ambos. */
//        objConexion = new ConexionMySQL("u5818148_carlos", "=jkst[RHJunx", "u5818148_criptas", "62.210.247.90", 3306);
        objConexion = new ConexionMySQL("root", "", "prueba", "localhost", 3306);
        try {
            estado = objConexion.conectar();
            conexion = objConexion.getConexion();
        } catch (ClassNotFoundException e) {
            resultado = new Alert(Alert.AlertType.ERROR, "Error en la conexion: " + e.getMessage());
            resultado.show();
        }
        return estado;
    }


}
