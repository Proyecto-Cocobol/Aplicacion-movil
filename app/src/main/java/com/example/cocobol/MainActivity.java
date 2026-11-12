package com.example.cocobol;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText Contrasena, Correo;
    Button BtnIniciarSesion;
    TextView MensajeError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), new OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            }
        });

        //Aquí están los ID y los Listeners de los botones
        BtnIniciarSesion = findViewById(R.id.boton_inicio_sesion);
        BtnIniciarSesion.setOnClickListener(this);

        Contrasena = findViewById(R.id.Contrasena);
        Correo = findViewById(R.id.Correo);
        MensajeError = findViewById(R.id.mensajeError);
    }

    @Override
    public void onClick(View v) {
        Datos obtenerDatos = new Datos();

        obtenerDatos.setContrasena(Contrasena.getText().toString());
        obtenerDatos.setCorreo(Correo.getText().toString().trim());

        //Aqui se hace la validacion de login contra las cuentas guardadas en el telefono (SQLite)
        if (v.getId() == R.id.boton_inicio_sesion) {
            MensajeError.setText("");
            if (obtenerDatos.getCorreo().isEmpty() || obtenerDatos.getContrasena().isEmpty()) {
                MensajeError.setText("Ingresa tu usuario y contraseña.");
                return;
            }

            BaseDatos bd = BaseDatos.obtener(this);
            // Igual que la web: se puede entrar con el nombre de usuario o con el número de boleta
            Cursor c = bd.consultar("SELECT id, nombre, paterno, rol, activo FROM cuenta "
                            + "WHERE usuario = ? COLLATE NOCASE OR (boleta = ? AND boleta <> '')",
                    obtenerDatos.getCorreo(), obtenerDatos.getCorreo());

            if (!c.moveToFirst()) {
                c.close();
                MensajeError.setText("Usuario o contraseña incorrectos.");
                return;
            }
            long id = c.getLong(0);
            String nombre = c.getString(1) + " " + c.getString(2);
            String rol = c.getString(3);
            boolean activa = c.getInt(4) == 1;
            c.close();

            if (!bd.contrasenaCorrecta(id, obtenerDatos.getContrasena())) {
                MensajeError.setText("Usuario o contraseña incorrectos.");
            } else if (!activa) {
                MensajeError.setText("Tu cuenta está desactivada. Contacta al administrador.");
            } else {
                Sesion.iniciar(this, id, nombre, rol);
                Contrasena.setText("");
                Intent intento;
                if (rol.equals("ADMINISTRADOR")) {
                    intento = new Intent(this, Inicio_principal_admin.class);
                } else {
                    // La web todavía no tiene vistas para PROFESOR, RESPONSABLE ni ALUMNO
                    intento = new Intent(this, Home.class);
                }
                startActivity(intento);
                finish();
            }
        }
    }
}
