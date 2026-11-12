package com.example.cocobol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Adaptador sencillo para los ListView de la aplicación.
 * Cada renglón de "datos" llena, en orden, los TextView indicados en "ids".
 * Los botones de cada fila guardan su posición en el tag para saber qué renglón se tocó.
 */
public class FilaAdapter extends BaseAdapter {

    private final Context contexto;
    private final int layout;
    private final int[] ids;
    private final ArrayList<String[]> datos;
    private View.OnClickListener oyente;
    private int[] idsBoton = new int[0];

    public FilaAdapter(Context contexto, int layout, int[] ids, ArrayList<String[]> datos) {
        this.contexto = contexto;
        this.layout = layout;
        this.ids = ids;
        this.datos = datos;
    }

    /** Indica qué botones de la fila responden y quién atiende el clic (normalmente la Activity). */
    public void setOyente(View.OnClickListener oyente, int... idsBoton) {
        this.oyente = oyente;
        this.idsBoton = idsBoton;
    }

    /** Reemplaza los renglones y vuelve a dibujar la lista. */
    public void cambiarDatos(ArrayList<String[]> nuevos) {
        datos.clear();
        datos.addAll(nuevos);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return datos.size();
    }

    @Override
    public Object getItem(int posicion) {
        return datos.get(posicion);
    }

    @Override
    public long getItemId(int posicion) {
        return posicion;
    }

    @Override
    public View getView(int posicion, View vista, ViewGroup padre) {
        if (vista == null) {
            vista = LayoutInflater.from(contexto).inflate(layout, padre, false);
        }
        llenar(contexto, vista, ids, datos.get(posicion));

        for (int idBoton : idsBoton) {
            View boton = vista.findViewById(idBoton);
            if (boton != null) {
                boton.setTag(posicion);
                boton.setOnClickListener(oyente);
            }
        }
        return vista;
    }

    /** Llena una vista con un renglón. También se usa para tarjetas que no están en un ListView. */
    public static void llenar(Context contexto, View vista, int[] ids, String[] fila) {
        for (int i = 0; i < ids.length && i < fila.length; i++) {
            View campo = vista.findViewById(ids[i]);
            if (campo instanceof TextView) {
                TextView texto = (TextView) campo;
                texto.setText(fila[i]);
                if (ids[i] == R.id.txt_estado) {
                    texto.setVisibility(fila[i].isEmpty() ? View.GONE : View.VISIBLE);
                    pintarEstado(contexto, texto, fila[i]);
                }
            }
        }
    }

    /** Colorea la etiqueta de estado igual que la página web (success, warning, danger, neutral). */
    public static void pintarEstado(Context contexto, TextView texto, String estado) {
        String e = estado.toLowerCase(Locale.ROOT);
        int nivel = 0; // 0 neutro, 1 ok, 2 aviso, 3 peligro
        if (e.startsWith("-") || e.equals("adeudo") || e.equals("no listo") || e.equals("inactivo")) {
            nivel = 3;
        } else if (e.equals("pendiente") || e.equals("en producción")) {
            nivel = 2;
        } else if (e.equals("pagado") || e.equals("listo") || e.equals("sin adeudo") || e.equals("activo")
                || e.equals("entregado")) {
            nivel = 1;
        } else if (e.matches("\\d+/10")) {
            int cal = Integer.parseInt(e.substring(0, e.indexOf('/')));
            nivel = cal >= 8 ? 1 : (cal >= 6 ? 2 : 3);
        }

        int fondo;
        int color;
        if (nivel == 3) {
            fondo = R.drawable.fondo_estado_peligro;
            color = R.color.estado_peligro_texto;
        } else if (nivel == 2) {
            fondo = R.drawable.fondo_estado_aviso;
            color = R.color.estado_aviso_texto;
        } else if (nivel == 1) {
            fondo = R.drawable.fondo_estado_ok;
            color = R.color.estado_ok_texto;
        } else {
            fondo = R.drawable.fondo_estado_neutro;
            color = R.color.estado_neutro_texto;
        }
        texto.setBackgroundResource(fondo);
        texto.setTextColor(ContextCompat.getColor(contexto, color));
    }
}
