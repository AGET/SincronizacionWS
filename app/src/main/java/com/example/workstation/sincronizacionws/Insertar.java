package com.example.workstation.sincronizacionws;

/* Codigo*/

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

//import android.text.InputType;
//import android.widget.ArrayAdapter;
//import android.widget.ArrayAdapter;

public class Insertar extends Activity {

    EditText id, nom, tel, mail;
    TextView idPaisSel;
    String idPais;
    Spinner pais;
    Button guardar;
    SQLHelper sqlhelper;
    SQLiteDatabase db;
    int indice = 0;
    final String BASEDEDATOS = "BD_Agenda.db";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insertar);

        id = (EditText) findViewById(R.id.id);
        nom = (EditText) findViewById(R.id.nombre);
        tel = (EditText) findViewById(R.id.telefono);
        mail = (EditText) findViewById(R.id.correo);
        guardar = (Button) findViewById(R.id.guardar);
        pais = (Spinner) findViewById(R.id.pais);
        idPaisSel = (TextView) findViewById(R.id.idpaissel);

        Bundle parametros = getIntent().getExtras();
        id.setText(parametros.getString("id"));
        nom.setText(parametros.getString("nom"));
        tel.setText(parametros.getString("tel"));
        mail.setText(parametros.getString("mail"));
        idPais = parametros.getString("pais");
        //mail.setText(parametros.getString("mail"));
        //pais.setText(parametros.getString("pais"));
        Log.e("JMMC", idPais);
        idPaisSel.setText(idPais);

        id.setSelected(false);

        guardar.setText(parametros.getString("boton"));
        //Log.e("JMMC",idPais);

        sqlhelper = new SQLHelper(this);
        //sqlhelper = new SQLHelper(this, BASEDEDATOS, null, 1);

        guardar.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                db = sqlhelper.getWritableDatabase();
                // TODO Auto-generated method stub
                if (guardar.getText().equals("Insertar")) {
                    db.execSQL(" INSERT INTO contacto "
                            + " (_id,nombre,telefono, correo,pais) "
                            + " VALUES ( "
                            + " '" + id.getText().toString() + "', "
                            + " '" + nom.getText().toString() + "', "
                            + " '" + tel.getText().toString() + "', "
                            + " '" + mail.getText().toString() + "', "
                            + " '" + idPais.toString() + "' "
                            + " ) ");

                } else if (guardar.getText().equals("Modificar")) {

                    db.execSQL("UPDATE contacto" +
                            " SET" +
                            " nombre='" + nom.getText().toString() + "'," +
                            "telefono='" + tel.getText().toString() + "'," +
                            "correo='" + mail.getText().toString() + "'," +
                            "pais='" + idPais.toString() + "'" +
                            " WHERE" +
                            " _id='" + id.getText().toString() + "'");

                    /*db = sqlhelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("nombre", nom.getText().toString());
                    values.put("telefono", tel.getText().toString());
                    values.put("correo", mail.getText().toString());
                    values.put("pais", pais.getText().toString());
                    db.insert("contacto", null, values);
                    db.close();*/
                }
                db.close();
                finish();
            }
        });
        Log.e("JMMC", "2");

        //Spinner pais
        pais = (Spinner) this.findViewById(R.id.pais);
        db = sqlhelper.getWritableDatabase();

        Cursor cur = db.rawQuery("SELECT _id, nombre FROM pais", null);


        int[] paisId = new int[]{android.R.id.text1};
        String[] paisNombre = new String[]{"nombre"};

        System.out.println("VERRRRR:"+idPais);

        if (idPais.equalsIgnoreCase("Mexico")) {
            indice = 0;
            System.out.println("1");
        } else if (idPais.equalsIgnoreCase("Rusia")) {
            indice = 3;
            System.out.println("2");
        } else if (idPais.equalsIgnoreCase("Alemania")) {
            indice = 2;
            System.out.println("3");
        } else if (idPais.equalsIgnoreCase("Japon")) {
            System.out.println("4");
            indice = 3;
        }

        SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item
                , cur, paisNombre, paisId, indice); //indice donde debe iniciar el spinner seleccionado
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pais.setAdapter(mAdapter);
        db.close();
        Log.e("JMMC", "3");

        pais.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Long itemId = parent.getItemIdAtPosition(pos);
                Log.e("JMMC", "Listener=" + itemId);
                idPais = itemId + "";
                idPaisSel.setText(idPais);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Log.e("JMMC", "4");
    }


}