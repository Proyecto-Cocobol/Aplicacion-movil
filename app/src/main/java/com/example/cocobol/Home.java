package com.example.cocobol;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Pantalla para PROFESOR, RESPONSABLE y ALUMNO (la web todavía no tiene vistas para estos roles). */
public class Home extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        final View barra = findViewById(R.id.barra_superior);
        final int dp8 = (int) (8 * getResources().getDisplayMetrics().density);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), new OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                Insets b = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                barra.setPadding(dp8, dp8 + b.top, dp8, dp8);
                v.setPadding(b.left, 0, b.right, b.bottom);
                return insets;
            }
        });

        if (Sesion.id(this) < 0) {
            cerrarSesion();
            return;
        }
        ((TextView) findViewById(R.id.txt_bienvenida)).setText("Bienvenido, " + Sesion.nombre(this));
        ((TextView) findViewById(R.id.txt_rol)).setText("Rol: " + Util.rolLegible(Sesion.rol(this))
                + "\nEsta sección estará disponible próximamente.");

        findViewById(R.id.boton_cerrar_sesion).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.boton_cerrar_sesion) {
            cerrarSesion();
        }
    }

    private void cerrarSesion() {
        Sesion.cerrar(this);
        Intent intent = new Intent(Home.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
