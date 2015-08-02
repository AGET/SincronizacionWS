package com.example.workstation.sincronizacionws;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends Activity {

    ListView lstv;
    String namespace = "http://tempuri.org/";
    //String url = "http://10.0.2.2/WSAgenda/Service1.asmx";			//Local
    String url = "http://192.168.56.1/Service1.asmx";            //Local
//	String url = "http://192.168.43.103/WSAgenda/Service1.asmx";	//Intranet
//    String url = "http://192.168.2.59/WSAgenda/Service1.asmx";	//Intranet

    SQLHelper sqlhelper;
    SQLiteDatabase db;
    HttpTransportSE transporte;
    SoapObject request;
    SoapSerializationEnvelope sobre;
    SoapPrimitive resultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lstv = (ListView) findViewById(R.id.lista);

        lstv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(
                    AdapterView<?> arg0,
                    View arg1,
                    int posicion,
                    long arg3) {
                // TODO Auto-generated method stub
                String valor = (String) lstv.getItemAtPosition(posicion);
                StringTokenizer st = new StringTokenizer(valor, "-");
                String clave = st.nextToken();
                String nombre = st.nextToken();
                String telefono = st.nextToken();
                String email = st.nextToken();
                String pais = st.nextToken();
                lanzarAlerta(clave, nombre, telefono, email, pais);
            }
        });
        actualiza();
    }

    public void lanzarAlerta(
            final String cve, final String nom, final String tel,
            final String email, final String pais) {
        AlertDialog.Builder dialog =
                new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("Que deseas realizar con el Contacto");
        dialog.setMessage(nom);
        dialog.setCancelable(true);
        dialog.setPositiveButton(
                "Eliminar",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        borrar(cve);
                    }
                });
        dialog.setNegativeButton("Modificar",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        Intent intent = new Intent(
                                getApplicationContext(),
                                Insertar.class);
                        intent.putExtra("id", cve);
                        intent.putExtra("nom", nom);
                        intent.putExtra("tel", tel);
                        intent.putExtra("mail", email);
                        intent.putExtra("pais", pais);
                        intent.putExtra("boton", "Modificar");
                        startActivity(intent);
                    }
                });
        dialog.show();
    }

    protected void borrar(String id) {
        sqlhelper = new SQLHelper(this);
        db = sqlhelper.getWritableDatabase();
        db.execSQL("delete from contacto where _id ='" + id + "'");
        db.close();
        actualiza();
    }

    protected void actualiza() {
        sqlhelper = new SQLHelper(this);
        db = sqlhelper.getWritableDatabase();

        Cursor c = db.rawQuery(
                "select "
                        + "c._id, "
                        + "c.nombre, "
                        + "c.telefono, "
                        + "c.correo, "
                        + "p.nombre "
                        + " from contacto c "
                        + " inner join pais p "
                        + " on c.pais=p._id", null);

//		Cursor c = db.rawQuery("select c.id, c.nombre, c.telefono, c.correo, c.pais from contacto c",null);

        if (c.moveToFirst()) {
            ArrayList<String> arreglo =
                    new ArrayList<String>(c.getCount());
            do {
                String id = c.getString(0);
                String nom = c.getString(1);
                String tel = c.getString(2);
                String mail = c.getString(3);
                String pais = c.getString(4);
                arreglo.add(id + "-" + nom + "-" + tel + "-" + mail + "-" + pais);
            } while (c.moveToNext());
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<String>(
                            this,
                            android.R.layout.simple_list_item_1,
                            arreglo);
            lstv.setAdapter(adapter);
        } else {
            ArrayList<String> arreglo = new ArrayList<String>(1);
            arreglo.add("Sin Datos");
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<String>(
                            this,
                            android.R.layout.simple_list_item_1,
                            arreglo
                    );
            lstv.setAdapter(adapter);
        } // if

        db.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.insertar:
                Intent intent =
                        new Intent(getApplicationContext(),
                                Insertar.class);
                intent.putExtra("id", "");
                intent.putExtra("nom", "");
                intent.putExtra("tel", "");
                intent.putExtra("mail", "");
                intent.putExtra("pais", "");
                intent.putExtra("boton", "Insertar");
                startActivity(intent);
                break;
            case R.id.actualizar:
                actualiza();
                break;
            case R.id.descargarCatalogo:
                descargarCatalogo();
                break;
            case R.id.cargarDatos:
                cargarDatos();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void descargarCatalogo() {
        sqlhelper = new SQLHelper(this);
        db = sqlhelper.getWritableDatabase();
        db.execSQL("DELETE FROM pais");
        db.close();
        Log.e("JMMC", "SOAP-1");
        String accionSoap = "http://tempuri.org/consultaPais";
        String metodo = "consultaPais";
        String cadena = "";
        try {
            Log.e("JMMC", "SOAP-2");
            request = new SoapObject(namespace, metodo);
            Log.e("JMMC", "SOAP-3");
            sobre = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            Log.e("JMMC", "SOAP-4");
            sobre.dotNet = true;
            Log.e("JMMC", "SOAP-5");
            sobre.setOutputSoapObject(request);
            Log.e("JMMC", "SOAP-6");
            // Habilitar la comunicacion con el
            // Web Services desde el Activity Principal
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            transporte = new HttpTransportSE(url);
            Log.e("JMMC", "SOAP-7");
            transporte.call(accionSoap, sobre);
            Log.e("JMMC", "SOAP-8");
            resultado = (SoapPrimitive) sobre.getResponse();
            Log.e("JMMC", "SOAP-9");
            cadena = resultado.toString();
            Log.e("JMMC", "SOAP-10");
            //Toast.makeText(this, cadena, Toast.LENGTH_LONG).show();
            StringTokenizer st1 = new StringTokenizer(cadena, "*");
            db = sqlhelper.getWritableDatabase();

            while (st1.hasMoreElements()) {
                Log.e("JMMC", "SOAP-11");
                StringTokenizer st2 = new StringTokenizer(st1.nextToken(), ",");
                Log.e("JMMC", "SOAP-12");
                String id = st2.nextToken().toString();
                String nombre = st2.nextToken().toString();
                Log.e("JMMC", "SOAP-13-" + id + "-" + nombre);
                db.execSQL("insert into pais (_id, nombre) "
                        + "values ('" + id + "','" + nombre + "')");
            }

            Cursor c = db.rawQuery("select * from pais", null);
            if (c.moveToFirst()) {
                do {
                    String id = c.getString(0);
                    String nom = c.getString(1);
                    System.out.println("DESPUES PAIS- id: " + id + " nombre: " + nom);
//					Log.e("JMMC","SOAP-2b-"+id+"-"+
                } while (c.moveToNext());
            }

            db.close();
            Toast.makeText(this, "Datos del Catalogo \n Descargados", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("JMMC", "SOAP-" + e.toString());
        }
    }

    protected void cargarDatos() {
        sqlhelper = new SQLHelper(this);
        String accionSoap = "http://tempuri.org/InsertarContacto";

        String metodo = "InsertarContacto";
        String cadena = "";
        try {
            db = sqlhelper.getWritableDatabase();
            Cursor c = db.rawQuery("select * from contacto", null);
            if (c.moveToFirst()) {
                do {
                    String id = c.getString(0);
                    String nom = c.getString(1);
                    String tel = c.getString(2);
                    String mail = c.getString(3);
                    String pais = c.getString(4);
//					Log.e("JMMC","SOAP-2b-"+id+"-"+nom+"-"+tel+"-"+mail+"-"+pais);

                    request = new SoapObject(namespace, metodo);
                    Log.e("JMMC", "SOAP-3b");
                    request.addProperty("id", id);
                    request.addProperty("nombre", nom);
                    request.addProperty("telefono", tel);
                    request.addProperty("correo", mail);
                    request.addProperty("pais", pais);
                    sobre = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    Log.e("JMMC", "SOAP-4b");
                    sobre.dotNet = true;
                    Log.e("JMMC", "SOAP-5b");
                    sobre.setOutputSoapObject(request);
                    Log.e("JMMC", "SOAP-6b");
                    // Habilitar la comunicacion con el Web Services desde el Activity Principal
                    StrictMode.ThreadPolicy policy =
                            new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    transporte = new HttpTransportSE(url);
                    Log.e("JMMC", "SOAP-7b");
                    transporte.call(accionSoap, sobre);
                    Log.e("JMMC", "SOAP-8b");
                    resultado = (SoapPrimitive) sobre.getResponse();
                    Log.e("JMMC", "SOAP-9b");
                    cadena = resultado.toString();
                    Log.e("JMMC", "SOAP-10b");
                } while (c.moveToNext());
                Toast.makeText(this, "Datos Cargados", Toast.LENGTH_LONG).show();
            }
            db.close();
        } catch (Exception e) {
            Log.e("JMMC", "SOAP-b-" + e.toString());
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            actualiza();
        }
    }
}