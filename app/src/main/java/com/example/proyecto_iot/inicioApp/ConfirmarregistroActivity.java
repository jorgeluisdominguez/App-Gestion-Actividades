package com.example.proyecto_iot.inicioApp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.databinding.ActivityConfirmarregistroBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ConfirmarregistroActivity extends AppCompatActivity {
    ActivityConfirmarregistroBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConfirmarregistroBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        FirebaseAuth.getInstance().signOut();

        binding.backButton2.setOnClickListener(view -> {
            Intent intent = new Intent(ConfirmarregistroActivity.this, IngresarActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Intent intent = new Intent(ConfirmarregistroActivity.this, IngresarActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        return super.onKeyDown(keyCode, event);
    }
}