package com.example.cocobol;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;

/** Administrar una categoría (equipo): datos del equipo y sus jugadores. Recibe "id_equipo". */
public class Categorias extends ActividadBase implements AdapterView.OnItemClickListener {

    ListView lista;
    long idEquipo;
    String categoria = "";
    ArrayList<Long> ids = new ArrayList<>();
    ArrayList<String> nombres = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.activity_categorias, R.id.menu_lateral_equipos);

        idEquipo = getIntent().getLongExtra("id_equipo", -1);
        lista = prepararLista(R.layout.cabecera_categorias);
        cabecera.findViewById(R.id.boton_agregar).setOnClickListener(this);
        cabecera.findViewById(R.id.boton_editar).setOnClickListener(this);
        cabecera.findViewById(R.id.boton_evaluaciones).setOnClickListener(this);
        lista.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Cursor e = bd.consultar("SELECT e.categoria, e.edad, p.nombre || ' ' || p.paterno, e.torneo FROM equipo e "
                + "LEFT JOIN personal p ON p.id = e.profesor_id WHERE e.id = ?", String.valueOf(idEquipo));
        if (!e.moveToFirst()) {
            e.close();
            toast("El equipo ya no existe.");
            finish();
            return;
        }
        categoria = e.getString(0);
        texto(R.id.txt_subtitulo_cab, "Categoría " + categoria + ".");
        texto(R.id.txt_categoria, categoria);
        texto(R.id.txt_edad, Util.guion(e.getString(1)));
        texto(R.id.txt_profesor, e.isNull(2) ? "Sin asignar" : e.getString(2));
        texto(R.id.txt_torneo, Util.guion(e.getString(3)));
        e.close();
        cargar();
    }

    private void texto(int idVista, String valor) {
        ((TextView) cabecera.findViewById(idVista)).setText(valor);
    }

    void cargar() {
        ArrayList<String[]> datos = new ArrayList<>();
        ids.clear();
        nombres.clear();
        Cursor c = bd.consultar("SELECT id, nombre, paterno, materno, fecha_nac, numero, fecha_ingreso FROM alumno "
                + "WHERE equipo_id = ? ORDER BY numero, nombre", String.valueOf(idEquipo));
        while (c.moveToNext()) {
            String nombre = Util.nombreCompleto(c.getString(1), c.getString(2), c.getString(3));
            int edad = Util.edad(c.getString(4));
            ids.add(c.getLong(0));
            nombres.add(nombre);
            datos.add(new String[]{Util.iniciales(c.getString(1), c.getString(2)), nombre,
                    edad < 0 ? "—" : String.valueOf(edad), Util.guion(c.getString(4)),
                    c.isNull(5) ? "—" : "#" + c.getInt(5), Util.guion(c.getString(6))});
        }
        c.close();

        texto(R.id.txt_jugadores, "Jugadores (" + datos.size() + ")");
        mostrarEnLista(lista, R.layout.item_categorias,
                new int[]{R.id.txt_avatar, R.id.txt_titulo, R.id.txt_c1, R.id.txt_c2, R.id.txt_c3, R.id.txt_c4}, datos);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if (id == R.id.boton_agregar) {
            elegirJugador();
        } else if (id == R.id.boton_editar) {
            abrir(FormEquipo.class, "id", idEquipo);
        } else if (id == R.id.boton_evaluaciones) {
            abrir(Evaluaciones.class, "id_equipo", idEquipo);
        }
    }

    /** Muestra los alumnos que no están en esta categoría para agregarlos. */
    void elegirJugador() {
        final ArrayList<Long> idsLibres = new ArrayList<>();
        ArrayList<String> textos = new ArrayList<>();
        Cursor c = bd.consultar("SELECT a.id, a.nombre || ' ' || a.paterno, e.categoria FROM alumno a "
                + "LEFT JOIN equipo e ON e.id = a.equipo_id WHERE a.equipo_id IS NULL OR a.equipo_id <> ? "
                + "ORDER BY a.nombre", String.valueOf(idEquipo));
        while (c.moveToNext()) {
            idsLibres.add(c.getLong(0));
            textos.add(c.getString(1) + (c.isNull(2) ? " (sin categoría)" : " (actual: " + c.getString(2) + ")"));
        }
        c.close();
        if (idsLibres.isEmpty()) {
            toast("No hay más alumnos para agregar. Registra uno en Alumnos.");
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Agregar jugador a " + categoria)
                .setItems(textos.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int posicion) {
                        agregarJugador(idsLibres.get(posicion));
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    void agregarJugador(long idJugador) {
        String id = String.valueOf(idJugador);
        String numero = bd.valor("SELECT numero FROM alumno WHERE id = ?", id);
        boolean repetido = !numero.isEmpty() && bd.existe("SELECT id FROM alumno WHERE equipo_id = ? AND numero = ?",
                String.valueOf(idEquipo), numero);
        if (repetido) {
            bd.actualizar("alumno", BaseDatos.cv("equipo_id", idEquipo, "numero", null), idJugador);
            toast("Jugador agregado. Su número #" + numero + " ya estaba ocupado: asígnale uno nuevo.");
        } else {
            bd.actualizar("alumno", BaseDatos.cv("equipo_id", idEquipo), idJugador);
            toast("Jugador agregado.");
        }
        bd.registrarBitacora("Equipos", bd.nombreAlumno(idJugador), "Agregado a la categoría " + categoria);
        cargar();
    }

    @Override
    public void onItemClick(AdapterView<?> padre, View vista, int posicion, long id) {
        final int i = posicion - lista.getHeaderViewsCount();
        if (i < 0 || i >= ids.size()) return;
        String[] opciones = {"Ver alumno", "Ver evaluaciones", "Quitar de la categoría"};
        new AlertDialog.Builder(this)
                .setTitle(nombres.get(i))
                .setItems(opciones, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int opcion) {
                        if (opcion == 0) {
                            abrir(Alumno_personal.class, "id_alumno", ids.get(i));
                        } else if (opcion == 1) {
                            abrir(Evaluaciones.class, "id_alumno", ids.get(i));
                        } else {
                            bd.actualizar("alumno", BaseDatos.cv("equipo_id", null), ids.get(i));
                            bd.registrarBitacora("Equipos", nombres.get(i), "Quitado de la categoría " + categoria);
                            toast("Jugador quitado de la categoría.");
                            cargar();
                        }
                    }
                })
                .show();
    }
}
