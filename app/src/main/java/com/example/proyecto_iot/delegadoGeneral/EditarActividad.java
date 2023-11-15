package com.example.proyecto_iot.delegadoGeneral;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.alumno.Entities.Alumno;
import com.example.proyecto_iot.databinding.ActivityEditarActividadBinding;
import com.example.proyecto_iot.delegadoGeneral.adapter.ListaDelegadosAdapter;
import com.example.proyecto_iot.delegadoGeneral.entity.Actividades;
import com.example.proyecto_iot.delegadoGeneral.entity.Usuario;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class EditarActividad extends AppCompatActivity {
    ActivityEditarActividadBinding binding;
    FirebaseFirestore db;
    TextInputLayout nombreAntiguo;
    EditText delegadoAntiguo;
    List<Alumno> listaAct;
    private static  boolean delegadosCargados = false;
    Alumno user_delegado;
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditarActividadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //Toolbar
        Toolbar toolbar = binding.toolbarEditarActividadesDg;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //====================================


        Actividades actividadAEditar = (Actividades) getIntent().getSerializableExtra("actividadActual");

        nombreAntiguo = binding.textFieldNombreActividadAntiguo;
        nombreAntiguo.getEditText().setText(actividadAEditar.getNombre());
        delegadoAntiguo = binding.editTextNombreDelegadoAntiguo;
        delegadoAntiguo.setText(actividadAEditar.getDelegadoActividad().getNombre()+' '+actividadAEditar.getDelegadoActividad().getApellidos());

        delegadoAntiguo.setOnClickListener(view -> {
            showDialog();
        });

        //Butom para actualizar
        binding.buttonEditarActDg.setOnClickListener(view -> {
            String nombreActividad = nombreAntiguo.getEditText().getText().toString();
            Actividades actividad = new Actividades();

            if( !nombreActividad.equals("") && user_delegado!=null){
                Intent intent = new Intent();
                actividad.setNombre(nombreActividad);
                actividad.setEstado("abierto");
                actividad.setId(actividadAEditar.getId());

                intent.putExtra("nombreActividad",actividad);
                intent.putExtra("delegado",user_delegado);
                setResult(RESULT_OK,intent);
                finish();
            }else {
                Toast.makeText(this,"Complete los campos",Toast.LENGTH_SHORT).show();
            }


        });


    }

    //Flecha para regrasar al inicio
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    public void showDialog() {


        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.buttomsheetlayout_dg);

        db = FirebaseFirestore.getInstance();
        db.collection("alumnos")
                .whereEqualTo("estado","activo")
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        QuerySnapshot queryDocumentSnapshots = task.getResult();
                        listaAct = new ArrayList<>(); // Inicializa la lista

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Alumno alumno = document.toObject(Alumno.class);
                            listaAct.add(alumno);
                        }

                        ListaDelegadosAdapter adapter = new ListaDelegadosAdapter();
                        adapter.setContext(this);
                        adapter.setListaUsuarios(listaAct);
                        adapter.setOnItemClickListener(new ListaDelegadosAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(Alumno alumno) {
                                user_delegado = alumno;
                                delegadoAntiguo.setText(alumno.getNombre()+' '+alumno.getApellidos()); // Actualiza el EditText con el nombre del usuario seleccionado
                                dialog.dismiss(); // Cierra el diálogo después de la selección

                            }
                        });

                        recyclerView = dialog.findViewById(R.id.recycleViewAsignarDelegado);
                        recyclerView.setAdapter(adapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(this));




                    }
                    dialog.show();
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    dialog.getWindow().setGravity(Gravity.BOTTOM);

                });






    }

}