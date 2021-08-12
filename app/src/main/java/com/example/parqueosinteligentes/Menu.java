package com.example.parqueosinteligentes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Menu extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button btnMapa;
    private Button cerrarSesion;
    DatabaseReference db_reference;
    FirebaseUser user;
    FirebaseDatabase root;
    private String email;
    private String usuario;
    private String tipo="comun";//Temporal solo para pruebas

    private TextView textViewNombre;
    private TextView textViewPrioridad;

    private TextView textViewEstac1;
    private TextView textViewEstac2;
    private TextView textViewEstac3;
    private TextView textViewEstac4;

    private LinearLayout layoutEstac1;
    private LinearLayout layoutEstac2;
    private LinearLayout layoutEstac3;
    private LinearLayout layoutEstac4;


    String ArrayIDEstacionamiento[]  = {"P1","P2","P3","P4"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        root=FirebaseDatabase.getInstance();
        user = mAuth.getCurrentUser();

        email=user.getEmail();
        usuario=email.split("@")[0];
        iniciarBaseDeDatosUsuarios();
        InitializateComponents();

        textViewNombre.setText(usuario);
        //textViewPrioridad.setText(tipo);
        cerrarSesion = (Button) findViewById(R.id.btnCerrarSesion);

        cerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(Menu.this,MainActivity.class));
                finish();
            }
        });

        notificacion();
        actualizarPrioridadEstacionamientoUI();
        actualizarEstadoEstacionamientoUI();
        //ocuparEstacionamiento("P1");
        }

    private void InitializateComponents(){
        textViewNombre = (TextView) findViewById(R.id.textViewNombre);
        textViewPrioridad = (TextView) findViewById(R.id.textViewPrioridad);

        textViewEstac1 = (TextView) findViewById(R.id.parqueo1);
        textViewEstac2 = (TextView) findViewById(R.id.parqueo2);
        textViewEstac3 = (TextView) findViewById(R.id.parqueo3);
        textViewEstac4 = (TextView) findViewById(R.id.parqueo4);

        layoutEstac1 = (LinearLayout) findViewById(R.id.layoutEstac1);
        layoutEstac2 = (LinearLayout) findViewById(R.id.layoutEstac2);
        layoutEstac3 = (LinearLayout) findViewById(R.id.layoutEstac3);
        layoutEstac4 = (LinearLayout) findViewById(R.id.layoutEstac4);


        db_reference.child(usuario).child("tipo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                tipo = snapshot.getValue(String.class);
            }
            @Override
            public void onCancelled(DatabaseError error) {

            }
        });


        }
    public void revisarMapa(View v) {
        Intent mapa = new Intent(this, Parqueadero.class);
        startActivity(mapa);
    }
    public void iniciarBaseDeDatosUsuarios(){
        db_reference = root.getReference("usuarios");
    }


    public void notificacion(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                db_reference.child(usuario).child("tipo").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        while(tipo.isEmpty()) {
                            tipo = snapshot.getValue(String.class);
                            if(tipo==null){//TODO:Se debe implementar registrar los datos en la base, por ahora solo estan quemados
                                tipo="privilegiado";//Si un usuario que no esta quemado inicia sesion, se le setea el tipo "privilegiado"
                            }
                        }
                        Menu.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textViewPrioridad.setText(tipo);//TODO: SE DEBE LEER ESTO AFUERA DE LA FUNCION NOTIFICACION
                                Toast.makeText(getApplicationContext(), "Usuario tipo:" + tipo, Toast.LENGTH_SHORT).show();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });

            }
        }).start();

    }

    /*
    public void ocuparEstacionamiento(String estacionamiento){
        //DatabaseReference db_reference_estacionamiento = root.getReference("Parkeo").child(estacionamiento);
        DatabaseReference db_reference_general = root.getReference();
        db_reference_general.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                DataSnapshot db_reference_estacionamiento = snapshot.child("Parkeo").child(estacionamiento);
                DataSnapshot db_reference_usuario = snapshot.child("usuarios");

                double estado_parqueo = db_reference_estacionamiento.child("estado").getValue(Double.class);
                String prioridad_parqueo = db_reference_estacionamiento.child("tipo").getValue(String.class);

                String prioridad_usuario = db_reference_usuario.child(usuario).child("tipo").getValue(String.class);

                //Validaciones para poder parquear
                if(estado_parqueo == 0) {

                    if (prioridad_usuario.equals(prioridad_parqueo) || prioridad_usuario.equals("privilegiado")) {
                        setEstadoEstacionamientoDB(estacionamiento,1);
                    }else {
                        Toast.makeText(getApplicationContext(), "No se puede estacionar en "+ estacionamiento+", es PRIVILEGIADO", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(getApplicationContext(), "El puesto " + estacionamiento +" se encuentra OCUPADO", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

    }

     */

    public void setEstadoEstacionamientoDB(String estacionamiento, int estado){
        DatabaseReference db_reference_estacionamiento = root.getReference("Parkeo").child(estacionamiento);

        db_reference_estacionamiento.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                db_reference_estacionamiento.child("estado").setValue(estado);
                Toast.makeText(getApplicationContext(), "El estacionamiento " + estacionamiento + " fue cambiado a " + Integer.toString(estado) , Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

    }

    public void actualizarEstadoEstacionamientoUI(){

        for(int i = 0; i< ArrayIDEstacionamiento.length; i++){
            String estacionamiento = ArrayIDEstacionamiento[i];

            DatabaseReference db_reference_estacionamiento = root.getReference("Parkeo").child(estacionamiento).child("estado");

            db_reference_estacionamiento.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Double estado_estacionamiento = snapshot.getValue(Double.class);
                    setEstadoEstacionamientoUI(estacionamiento,estado_estacionamiento);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                }
            });

        }
    }

    public void setEstadoEstacionamientoUI(String estacionamiento,double estado){
        String ArrayIDEstacionamiento[]  = {"P1","P2","P3","P4"};
        TextView[] textViewEstacionamientos ={textViewEstac1,textViewEstac2,textViewEstac3,textViewEstac4};
        int indexEstacionamiento = -1;
        for(int i = 0;i < ArrayIDEstacionamiento.length;i++){//Esto es muy ineficiente, pero es temporal para hacer pruebas
            if(ArrayIDEstacionamiento[i]==estacionamiento){
                indexEstacionamiento = i;
            }
        }

        TextView txtViewestacionamiento = textViewEstacionamientos[indexEstacionamiento];
        //txtViewestacionamiento.setText(estado == 0 ? "DISPONIBLE":"OCUPADO");
        if(estado == 0){
            txtViewestacionamiento.setBackgroundColor(Color.parseColor("#6BF870"));
            txtViewestacionamiento.setText("DISPONIBLE");
        }
        else{
            txtViewestacionamiento.setBackgroundColor(Color.RED);
            txtViewestacionamiento.setText("OCUPADO");
        }
    }

    public void actualizarPrioridadEstacionamientoUI(){
        for(int i = 0; i< ArrayIDEstacionamiento.length; i++){
            String estacionamiento = ArrayIDEstacionamiento[i];

            DatabaseReference db_reference_estacionamiento = root.getReference("Parkeo").child(estacionamiento).child("tipo");

            db_reference_estacionamiento.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String prioridad_estac = snapshot.getValue(String.class);
                    if(tipo.equals("comun")){//Solo para el usuario comun, se aplica el filtro de ocultar los privilegiados
                        if(prioridad_estac.equals("privilegiado")){
                            visibilidadEstacionamientoUI(estacionamiento,0);
                        }
                        else{
                            visibilidadEstacionamientoUI(estacionamiento,1);
                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError error) {
                }
            });

        }
    }

    public void visibilidadEstacionamientoUI(String estacionamiento, int activado){
        String ArrayIDEstacionamiento[]  = {"P1","P2","P3","P4"};
        LinearLayout[] layoutEstacionamientos ={layoutEstac1,layoutEstac2,layoutEstac3,layoutEstac4};

        int indexEstacionamiento = -1;
        for(int i = 0;i < ArrayIDEstacionamiento.length;i++){//Esto es muy ineficiente, pero es temporal para hacer pruebas
            if(ArrayIDEstacionamiento[i]==estacionamiento){
                indexEstacionamiento = i;
            }
        }

        LinearLayout layoutEstacionamiento = layoutEstacionamientos[indexEstacionamiento];
        if(activado == 0){
            layoutEstacionamiento.setVisibility(View.INVISIBLE);//
        }
        else{
            layoutEstacionamiento.setVisibility(View.VISIBLE);
        }
    }

}