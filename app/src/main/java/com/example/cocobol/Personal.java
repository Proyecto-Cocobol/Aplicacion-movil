package com.example.cocobol;

import android.os.Bundle;
import android.view.View;

public class Personal extends ActividadBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.activity_personal, R.id.menu_lateral_personal);

        findViewById(R.id.modulo_profesores).setOnClickListener(this);
        findViewById(R.id.modulo_administrativo).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if (id == R.id.modulo_profesores) {
            abrir(Profesores.class);
        } else if (id == R.id.modulo_administrativo) {
            abrir(Administrativo.class);
        }
    }
}
