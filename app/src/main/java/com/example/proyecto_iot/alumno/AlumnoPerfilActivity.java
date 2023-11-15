package com.example.proyecto_iot.alumno;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.cometchat.chat.core.AppSettings;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.example.proyecto_iot.AppConstants;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.alumno.Entities.Alumno;
import com.example.proyecto_iot.alumno.Fragments.AlumnoHeader1Fragment;
import com.example.proyecto_iot.databinding.ActivityAlumnoPerfilBinding;
import com.example.proyecto_iot.inicioApp.IngresarActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

public class AlumnoPerfilActivity extends AppCompatActivity {

    private ActivityAlumnoPerfilBinding binding;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance(); // autenticacion
    private FirebaseStorage storage; // storage
    private Alumno alumno = new Alumno();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAlumnoPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle bundle = new Bundle();
        bundle.putString("header", "Perfil");
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragmentHeader, AlumnoHeader1Fragment.class, bundle)
                .commit();

        binding.buttonEditarPerfil.setOnClickListener(view -> {
            Intent intent = new Intent(AlumnoPerfilActivity.this, AlumnoPerfilEditarActivity.class);
            intent.putExtra("alumno", alumno);
            startActivity(intent);
        });

        binding.buttonContrasena.setOnClickListener(view -> {
            Intent intent = new Intent(AlumnoPerfilActivity.this, AlumnoPerfilContrasenaActivity.class);
            intent.putExtra("alumno", alumno);
            startActivity(intent);
        });

        binding.buttonCerrarSesion.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            cerrarCesionCometChat();
            Intent intent = new Intent(AlumnoPerfilActivity.this, IngresarActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            completarPerfilInfo();
        }
    }

    // reemplazar por obtener info desde internal storage
    private void completarPerfilInfo(){
        try (FileInputStream fileInputStream = openFileInput("userData");
             FileReader fileReader = new FileReader(fileInputStream.getFD());
             BufferedReader bufferedReader = new BufferedReader(fileReader)){

            String jsonData = bufferedReader.readLine();
            Gson gson = new Gson();
            alumno = gson.fromJson(jsonData, Alumno.class);

            binding.textNombre.setText(alumno.getNombre()+" "+alumno.getApellidos());
            binding.textRol.setText(alumno.getRol());

            cargarFoto(alumno);

        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private void cargarFoto(Alumno alumno){
        String url = alumno.getFotoUrl().equals("")? "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_640.png": alumno.getFotoUrl();

        RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL); // Almacenamiento en cache
        Glide.with(AlumnoPerfilActivity.this)
                .load(url)
                .apply(requestOptions)
                .into(binding.imagePerfil);
    }

    private void cerrarCesionCometChat(){

        String region = AppConstants.REGION;
        String appID = AppConstants.APP_ID;

        AppSettings appSettings = new AppSettings.AppSettingsBuilder()
                .subscribePresenceForAllUsers()
                .setRegion(region)
                .autoEstablishSocketConnection(true)
                .build();

        CometChat.init(AlumnoPerfilActivity.this, appID, appSettings, new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String s) {
                CometChat.logout(new CometChat.CallbackListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        Log.d("msg-test", "Logout completed successfully");
                    }

                    @Override
                    public void onError(CometChatException e) {
                        Log.d("msg-test", "Logout failed with exception: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onError(CometChatException e) {

            }
        });
    }
}