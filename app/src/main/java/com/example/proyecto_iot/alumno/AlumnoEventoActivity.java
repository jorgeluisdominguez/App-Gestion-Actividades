package com.example.proyecto_iot.alumno;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.alumno.Entities.Evento;
import com.example.proyecto_iot.alumno.Entities.Foto;
import com.example.proyecto_iot.alumno.Fragments.AlumnoApoyandoButtonFragment;
import com.example.proyecto_iot.alumno.Fragments.AlumnoApoyarButtonFragment;
import com.example.proyecto_iot.alumno.RecyclerViews.ListaFotosEventoAdapter;
import com.example.proyecto_iot.databinding.ActivityAlumnoEventoBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AlumnoEventoActivity extends AppCompatActivity {

    private ActivityAlumnoEventoBinding binding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage;
    private String userUid = FirebaseAuth.getInstance().getUid();
    private Evento evento;
    private Uri imageUri;
    private String fotoSubidaUrl;
    private BottomSheetDialog bottomSheetDialog;
    private ArrayList<Foto> fotoList = new ArrayList<>();
    private ListaFotosEventoAdapter adapter = new ListaFotosEventoAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAlumnoEventoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        evento = (Evento) getIntent().getSerializableExtra("evento");
        cargarInfoEvento();
        cargarFotos();
        insertarFragmentButtons(savedInstanceState);

        binding.buttonSubirFotos.setOnClickListener(view -> {
            Intent galleryIntent = new Intent(MediaStore.ACTION_PICK_IMAGES);
            openImageLauncher.launch(galleryIntent);
        });

        binding.buttonEventoBack.setOnClickListener(view -> {
            finish();
        });
        binding.buttonEventoLugar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtén el nombre del lugar desde tu evento o de donde sea que lo tengas
                String nombreLugar = evento.getLugar(); // Asegúrate de tener el nombre del lugar

                // Realiza la consulta en la colección "lugares" para obtener las coordenadas
                CollectionReference lugaresRef = db.collection("lugares");
                Query query = lugaresRef.whereEqualTo("nombre", nombreLugar);

                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                GeoPoint coordenadas = document.getGeoPoint("coordenadas");

                                if (coordenadas != null) {
                                    // Accede a la latitud y longitud desde coordenadas
                                    double latitud = coordenadas.getLatitude();
                                    double longitud = coordenadas.getLongitude();

                                    // Crea un Intent para abrir la nueva actividad (en este caso, un mapa)
                                    Intent intent = new Intent(AlumnoEventoActivity.this, MapaEventoActivityAlumno.class);
                                    // Pasa los datos de latitud y longitud a la nueva actividad
                                    intent.putExtra("latitud", latitud);
                                    intent.putExtra("longitud", longitud);
                                    startActivity(intent); // Inicia la nueva actividad (mapa)
                                } else {
                                    // Si no se encontraron coordenadas, puedes mostrar un mensaje de error o tomar otra acción.
                                    Toast.makeText(AlumnoEventoActivity.this, "No se encontraron coordenadas para este lugar", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                                // Si hubo un error en la consulta, puedes mostrar un mensaje de error o tomar otra acción.
                            Toast.makeText(AlumnoEventoActivity.this, "Error al buscar coordenadas: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void insertarFragmentButtons(Bundle savedInstanceState) {
        db.collection("alumnos")
                .document(userUid)
                .collection("eventos")
                .document("evento" + evento.getFechaHoraCreacion().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (savedInstanceState == null) {
                                if (document.exists()) { // evento en lista de eventos de alumno (evento apoyado)
                                    getSupportFragmentManager().beginTransaction()
                                            .setReorderingAllowed(true)
                                            .add(R.id.fragmentEventoButtons, AlumnoApoyandoButtonFragment.class, null)
                                            .commit();

                                    binding.buttonSubirFotos.setVisibility(View.VISIBLE);
                                } else { // evento no apoyado
                                    getSupportFragmentManager().beginTransaction()
                                            .setReorderingAllowed(true)
                                            .add(R.id.fragmentEventoButtons, AlumnoApoyarButtonFragment.class, null)
                                            .commit();
                                }
                            }
                        } else {
                            Log.d("msg-test", "AlumnoEventoActivity: error al buscar evento");
                        }
                    }
                });

    }

    private ActivityResultLauncher<Intent> openImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    imageUri = result.getData().getData();
                    abrirDialogSubirFoto();
                }
            }
    );

    private void cargarFotos() {
        db.collection("eventos")
                .document("evento" + evento.getFechaHoraCreacion().toString())
                .collection("fotos")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Foto foto = document.toObject(Foto.class);
                            fotoList.add(foto);
                            Log.d("msg-test", "foto: "+foto.getDescripcion());
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.d("msg-test", "error al cargar fotos");
                    }
                });

        adapter.setContext(AlumnoEventoActivity.this);
        adapter.setFotoList(fotoList);

        binding.rvFotos.setAdapter(adapter);
        /*LinearLayoutManager linearLayoutManager = new LinearLayoutManager(AlumnoEventoActivity.this){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };*/
        binding.rvFotos.setLayoutManager(new LinearLayoutManager(AlumnoEventoActivity.this));
    }

    private void abrirDialogSubirFoto() {
        bottomSheetDialog = new BottomSheetDialog(AlumnoEventoActivity.this);
        View bottomSheetView = LayoutInflater.from(AlumnoEventoActivity.this).inflate(R.layout.dialog_alumno_subir_foto, (ConstraintLayout) findViewById(R.id.bottomSheetSubirFoto));

        ImageView imagenFoto = bottomSheetView.findViewById(R.id.imageFoto);
        imagenFoto.setImageURI(imageUri);
        Button botonSubirFoto = bottomSheetView.findViewById(R.id.buttonDialogSubirFoto);
        botonSubirFoto.setOnClickListener(view -> {

            // subir foto a firestore y storage
            EditText inputDescripcion = bottomSheetView.findViewById(R.id.inputDescripcion);
            subirFoto(inputDescripcion.getText().toString());

        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void subirFoto(String descripcion) {
        Foto fotoNueva = new Foto();
        fotoNueva.setFechaHoraSubida(com.google.firebase.Timestamp.now());
        fotoNueva.setDescripcion(descripcion);

        String userUid = FirebaseAuth.getInstance().getUid();
        fotoNueva.setAlumnoID(userUid);

        storage = FirebaseStorage.getInstance();
        StorageReference reference = storage.getReference().child("evento" + evento.getFechaHoraCreacion().toString() + "/" + imageUri.getLastPathSegment() + ".jpg");
        reference.putFile(imageUri).continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return reference.getDownloadUrl();
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("msg-test", "foto de evento agregada en storage");
                        fotoNueva.setFotoUrl(task.getResult().toString());
                        subirFotoFirestore(fotoNueva);
                    } else {
                        Log.d("msg-test", "error");
                    }
                });

    }

    private void subirFotoFirestore(Foto fotoNueva){
        db.collection("eventos")
                .document("evento"+ evento.getFechaHoraCreacion().toString())
                .collection("fotos")
                .add(fotoNueva)
                .addOnSuccessListener(documentReference -> {
                    Log.d("msg-test", "foto guardada en firestore exitosamente");
                    finish();
                    startActivity(getIntent());
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });
    }

    private void cargarInfoEvento() {
        binding.textEventoTitulo.setText(evento.getTitulo());
        binding.textEventoActividad.setText(evento.getActividad());
        binding.textEventoDescripcion.setText(evento.getDescripcion());
        binding.buttonEventoFecha.setText(evento.getFecha());
        binding.buttonEventoHora.setText(evento.getHora());
        binding.buttonEventoLugar.setText(evento.getLugar());


        RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL); // Almacenamiento en cache
        Glide.with(AlumnoEventoActivity.this)
                .load(evento.getFotoUrl())
                .apply(requestOptions)
                .into(binding.imageEvento);
    }

    // funcion necesaria para chat
    public Evento getEvento() {
        return evento;
    }
}