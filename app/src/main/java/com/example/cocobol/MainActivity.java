package com.example.cocobol;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText Contrasena, Correo;
    Button BtnIniciarSesion, BtnRegistrarse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Aquí están los ID y los Listeners de los botones
        BtnIniciarSesion = findViewById(R.id.boton_inicio_sesion);
        BtnIniciarSesion.setOnClickListener(this);

        BtnRegistrarse = findViewById(R.id.boton_registro);
        BtnRegistrarse.setOnClickListener(this);

        Contrasena = findViewById(R.id.Contrasena);
        Correo = findViewById(R.id.Correo);
    }

    @Override
    public void onClick(View v) {
        Datos obtenerDatos = new Datos();

        obtenerDatos.setContrasena(Contrasena.getText().toString());
        obtenerDatos.setCorreo(Correo.getText().toString());

        //Aqui se hace la validacion de login
        if(v.getId() == R.id.boton_inicio_sesion) {
            if(obtenerDatos.getContrasena().equals("admin") && obtenerDatos.getCorreo().equals("admin")) {
                Intent intento = new Intent(this, Inicio_principal_admin.class);
                startActivity(intento);
            }else if(obtenerDatos.getContrasena().equals("alumno") && obtenerDatos.getCorreo().equals("alumno")){
                Intent intentito = new Intent(this, Home.class);
                startActivity(intentito);
            }else{
                Toast.makeText(this, "Credenciales inválidas", Toast.LENGTH_LONG).show();
            }
        }
        //Aqui se hace la validacion de ir al registro
        else if(v.getId() == R.id.boton_registro) {
            Intent intento2 = new Intent(this, Registro.class);
            startActivity(intento2);
        }
    }
}