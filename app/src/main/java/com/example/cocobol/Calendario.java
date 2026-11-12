package com.example.cocobol;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

/** Calendario mensual con los eventos guardados y la lista de eventos (y bitácora si se elige). */
public class Calendario extends ActividadBase implements AdapterView.OnItemClickListener,
        AdapterView.OnItemSelectedListener {

    static final String[] DIAS_CORTOS = {"Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"};

    ListView lista;
    Spinner filVista, filBitacora, filEntreno;
    LinearLayout contenedorMes;
    Calendar mes = Calendar.getInstance();
    ArrayList<Long> ids = new ArrayList<>();
    ArrayList<Boolean> esBitacora = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.activity_calendario, R.id.menu_lateral_calendario);

        mes.set(Calendar.DAY_OF_MONTH, 1);
        lista = prepararLista(R.layout.cabecera_calendario);
        filVista = cabecera.findViewById(R.id.fil_vista);
        filBitacora = cabecera.findViewById(R.id.fil_bitacora);
        filEntreno = cabecera.findViewById(R.id.fil_entreno);
        contenedorMes = cabecera.findViewById(R.id.contenedor_mes);

        filVista.setOnItemSelectedListener(this);
        filBitacora.setOnItemSelectedListener(this);
        filEntreno.setOnItemSelectedListener(this);
        cabecera.findViewById(R.id.boton_mes_anterior).setOnClickListener(this);
        cabecera.findViewById(R.id.boton_mes_siguiente).setOnClickListener(this);
        cabecera.findViewById(R.id.boton_agregar).setOnClickListener(this);
        cabecera.findViewById(R.id.boton_ver_bitacora).setOnClickListener(this);
        lista.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    private String mesClave() {
        return String.format(Locale.US, "%02d/%04d", mes.get(Calendar.MONTH) + 1, mes.get(Calendar.YEAR));
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density);
    }

    void cargar() {
        construirMes();
        cargarLista();
    }

    /** Dibuja la cuadrícula del mes con LinearLayout horizontales (una fila por semana). */
    void construirMes() {
        ((TextView) cabecera.findViewById(R.id.txt_mes)).setText(Util.mesAnio(mes));
        contenedorMes.removeAllViews();

        boolean soloEntreno = filEntreno.getSelectedItemPosition() == 1;
        HashMap<Integer, String> eventos = new HashMap<>();
        HashMap<Integer, Integer> cuantos = new HashMap<>();
        Cursor c = bd.consultar("SELECT fecha, tipo FROM evento WHERE substr(fecha, 4, 7) = ? "
                + (soloEntreno ? "AND tipo = 'Entreno' " : "") + "ORDER BY hora", mesClave());
        while (c.moveToNext()) {
            int dia = Integer.parseInt(c.getString(0).substring(0, 2));
            if (!eventos.containsKey(dia)) eventos.put(dia, c.getString(1));
            Integer n = cuantos.get(dia);
            cuantos.put(dia, n == null ? 1 : n + 1);
        }
        c.close();

        LinearLayout encabezado = nuevaFila();
        for (String d : DIAS_CORTOS) {
            TextView t = nuevaCelda(d, 11, R.color.gris, 26);
            t.setTypeface(null, Typeface.BOLD);
            encabezado.addView(t);
        }
        contenedorMes.addView(encabezado);

        Calendar hoy = Calendar.getInstance();
        boolean esMesActual = hoy.get(Calendar.YEAR) == mes.get(Calendar.YEAR)
                && hoy.get(Calendar.MONTH) == mes.get(Calendar.MONTH);
        int primerDia = mes.get(Calendar.DAY_OF_WEEK) - 1; // 0 = domingo
        int totalDias = mes.getActualMaximum(Calendar.DAY_OF_MONTH);

        LinearLayout fila = nuevaFila();
        for (int i = 0; i < primerDia; i++) {
            fila.addView(nuevaCelda("", 12, R.color.negro, 50));
        }
        for (int dia = 1; dia <= totalDias; dia++) {
            String texto = String.valueOf(dia);
            if (eventos.containsKey(dia)) {
                Integer n = cuantos.get(dia);
                texto += "\n" + eventos.get(dia) + (n != null && n > 1 ? " +" + (n - 1) : "");
            }
            TextView celda = nuevaCelda(texto, 10, R.color.negro, 50);
            celda.setId(R.id.celda_dia);
            celda.setTag(dia);
            celda.setOnClickListener(this);
            celda.setBackgroundResource(eventos.containsKey(dia) ? R.drawable.fondo_evento : R.drawable.fondo_celda);
            if (esMesActual && hoy.get(Calendar.DAY_OF_MONTH) == dia) {
                celda.setTextColor(ContextCompat.getColor(this, R.color.magenta));
                celda.setTypeface(null, Typeface.BOLD);
            }
            fila.addView(celda);
            if (fila.getChildCount() == 7) {
                contenedorMes.addView(fila);
                fila = nuevaFila();
            }
        }
        if (fila.getChildCount() > 0) {
            while (fila.getChildCount() < 7) {
                fila.addView(nuevaCelda("", 12, R.color.negro, 50));
            }
            contenedorMes.addView(fila);
        }
    }

    private LinearLayout nuevaFila() {
        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        return fila;
    }

    private TextView nuevaCelda(String texto, int tamano, int color, int alto) {
        TextView t = new TextView(this);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(alto), 1);
        p.setMargins(dp(1), dp(1), dp(1), dp(1));
        t.setLayoutParams(p);
        t.setText(texto);
        t.setTextSize(tamano);
        t.setGravity(Gravity.CENTER);
        t.setTextColor(ContextCompat.getColor(this, color));
        t.setMaxLines(2);
        return t;
    }

    /** Lista de eventos (y bitácora si "Bitácora: Mostrar") según la vista elegida. */
    void cargarLista() {
        int vista = filVista.getSelectedItemPosition(); // 0 mes, 1 próximos, 2 todos
        boolean conBitacora = filBitacora.getSelectedItemPosition() == 1;
        boolean soloEntreno = filEntreno.getSelectedItemPosition() == 1;
        String hoy = Util.clave(Util.hoy());

        ((TextView) cabecera.findViewById(R.id.txt_titulo_lista)).setText(
                vista == 0 ? "Eventos de " + Util.mesAnio(mes) : (vista == 1 ? "Próximos eventos" : "Todos los eventos"));

        // Cada renglón: {clave para ordenar, "E"/"B", id, avatar, título, subtítulo, estado}
        ArrayList<String[]> filas = new ArrayList<>();
        Cursor c = bd.consultar("SELECT ev.id, ev.titulo, ev.tipo, ev.fecha, ev.hora, e.categoria, ev.lugar "
                + "FROM evento ev LEFT JOIN equipo e ON e.id = ev.equipo_id");
        while (c.moveToNext()) {
            String fecha = c.getString(3);
            if (!incluir(fecha, vista, hoy)) continue;
            if (soloEntreno && !c.getString(2).equals("Entreno")) continue;
            String sub = Util.diaCorto(fecha) + " · " + fecha;
            if (!Util.vacio(c.getString(4))) sub += " · " + c.getString(4);
            sub += "\n" + (c.isNull(5) ? "Toda la academia" : "Categoría " + c.getString(5));
            if (!Util.vacio(c.getString(6))) sub += " · " + c.getString(6);
            filas.add(new String[]{Util.clave(fecha) + Util.nulo(c.getString(4)), "E", c.getString(0),
                    fecha.substring(0, 2), c.getString(1), sub, c.getString(2)});
        }
        c.close();

        if (conBitacora) {
            Cursor b = bd.consultar("SELECT id, fecha, tema, alumno, detalle FROM bitacora");
            while (b.moveToNext()) {
                String fecha = b.getString(1);
                if (!incluir(fecha, vista, hoy)) continue;
                String sub = fecha;
                if (!Util.vacio(b.getString(3))) sub += " · Alumno: " + b.getString(3);
                if (!Util.vacio(b.getString(4))) sub += "\n" + b.getString(4);
                filas.add(new String[]{Util.clave(fecha) + "99", "B", b.getString(0), "B",
                        "Tema: " + b.getString(2), sub, "Bitácora"});
            }
            b.close();
        }

        Collections.sort(filas, new Comparator<String[]>() {
            @Override
            public int compare(String[] a, String[] b) {
                return a[0].compareTo(b[0]);
            }
        });

        ArrayList<String[]> datos = new ArrayList<>();
        ids.clear();
        esBitacora.clear();
        for (String[] f : filas) {
            esBitacora.add(f[1].equals("B"));
            ids.add(Long.parseLong(f[2]));
            datos.add(new String[]{f[3], f[4], f[5], f[6]});
        }
        mostrarEnLista(lista, R.layout.item_evento,
                new int[]{R.id.txt_avatar, R.id.txt_titulo, R.id.txt_subtitulo, R.id.txt_estado}, datos);
    }

    private boolean incluir(String fecha, int vista, String hoy) {
        if (vista == 0) return fecha.length() == 10 && fecha.substring(3).equals(mesClave());
        if (vista == 1) return Util.clave(fecha).compareTo(hoy) >= 0;
        return true;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if (id == R.id.boton_mes_anterior) {
            mes.add(Calendar.MONTH, -1);
            cargar();
        } else if (id == R.id.boton_mes_siguiente) {
            mes.add(Calendar.MONTH, 1);
            cargar();
        } else if (id == R.id.boton_agregar) {
            abrir(FormEvento.class);
        } else if (id == R.id.boton_ver_bitacora) {
            abrir(Bitacora.class);
        } else if (id == R.id.celda_dia) {
            Calendar dia = (Calendar) mes.clone();
            dia.set(Calendar.DAY_OF_MONTH, (Integer) v.getTag());
            Intent intent = new Intent(this, FormEvento.class);
            intent.putExtra("fecha", Util.formatear(dia));
            startActivity(intent);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> padre, View vista, int posicion, long id) {
        int i = posicion - lista.getHeaderViewsCount();
        if (i >= 0 && i < ids.size()) {
            abrir(esBitacora.get(i) ? FormBitacora.class : FormEvento.class, "id", ids.get(i));
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> padre, View vista, int posicion, long id) {
        cargar();
    }

    @Override
    public void onNothingSelected(AdapterView<?> padre) {
    }
}
