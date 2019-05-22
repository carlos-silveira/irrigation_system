package sample;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javafx.scene.control.Alert;

public class ConexionMySQL {

    private String usuario, password, nombreBD, servidor;
    private int puerto;
    private Connection conexion = null;

   public ConexionMySQL(String usuario, String password, String nombreBD, String servidor, int puerto) {
        this.usuario = usuario;
        this.password = password;
        this.nombreBD = nombreBD;
        this.servidor = servidor;
        this.puerto = puerto;
    }

    public Connection getConexion() {
        return conexion;
    }

    public String conectar() throws ClassNotFoundException {
        try {
//            Class.forName("com.mysql.jdbc.Driver");
//            conexion = DriverManager.getConnection("jdbc:mysql://" + this.servidor + ":" + this.puerto + "/" + nombreBD, this.usuario, this.password);
            Class.forName("org.mariadb.jdbc.Driver");
            conexion = DriverManager.getConnection("jdbc:mariadb://" + this.servidor + ":" + this.puerto + "/" + nombreBD, this.usuario, this.password);
            if (conexion != null)
                return "Conexion exitosa.";
            else
                return "No se ha podido conectar al servidor.";
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    public String insertModDel(String cons) {
        try {
            java.sql.Statement st = conexion.createStatement();
            st.executeUpdate(cons);
            return "Operacion Exitosa.";
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    java.sql.ResultSet consultar(String query) {
        java.sql.ResultSet resultado;
        try {
            java.sql.Statement st = conexion.createStatement();
            resultado = st.executeQuery(query);
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            System.out.println("Error: " + e.getMessage());
            resultado = null;
        }
        return resultado;
    }
}


