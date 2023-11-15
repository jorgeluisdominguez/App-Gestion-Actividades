package com.example.proyecto_iot.alumno.RecyclerViews;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.alumno.AlumnoEventoActivity;
import com.example.proyecto_iot.alumno.Entities.Evento;

import java.util.List;

public class ListaEventosAdapter extends RecyclerView.Adapter<ListaEventosAdapter.EventoViewHolder>{
    private List<Evento> eventoList;
    private Context context;

    @NonNull
    @Override
    public EventoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rv_alumno_evento, parent, false);
        return new EventoViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull EventoViewHolder holder, int position) {
        Evento evento = eventoList.get(position);
        holder.evento = evento;

        TextView textTitulo = holder.itemView.findViewById(R.id.textTitulo);
        TextView textActividad = holder.itemView.findViewById(R.id.textActividad);
        TextView textDescripcion = holder.itemView.findViewById(R.id.textDescripcion);
        TextView textFecha = holder.itemView.findViewById(R.id.textFecha);
        TextView textHora = holder.itemView.findViewById(R.id.textHora);
        ImageView imageEvento = holder.itemView.findViewById(R.id.imageEvento);
        TextView lugar = holder.itemView.findViewById(R.id.textLugar);

        lugar.setText(evento.getLugar());
        textTitulo.setText(evento.getTitulo());
        textActividad.setText(evento.getActividad());
        textDescripcion.setText(evento.getDescripcion());
        textFecha.setText(evento.getFecha());
        textHora.setText(evento.getHora());

        String fotoUrl = evento.getFotoUrl();
        RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL); // Almacenamiento en cache
        Glide.with(getContext())
                .load(fotoUrl)
                .apply(requestOptions)
                .into(imageEvento);
    }

    @Override
    public int getItemCount() {
        return eventoList.size();
    }

    public class EventoViewHolder extends RecyclerView.ViewHolder{

        Evento evento;
        public EventoViewHolder(@NonNull View itemView) {
            super(itemView);
            ConstraintLayout constraintLayout = itemView.findViewById(R.id.evento);
            constraintLayout.setOnClickListener(view -> {
                Intent intent = new Intent(itemView.getContext(), AlumnoEventoActivity.class);
                intent.putExtra("evento", evento);
                itemView.getContext().startActivity(intent);
            });
        }
    }

    public List<Evento> getEventoList() {
        return eventoList;
    }

    public void setEventoList(List<Evento> eventoList) {
        this.eventoList = eventoList;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
