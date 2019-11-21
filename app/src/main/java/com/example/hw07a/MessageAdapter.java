package com.example.hw07a;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.IOException;
import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder>{
    ArrayList<Message> messages = new ArrayList<>();
    Context context;
    AdapterInterface adapterInterface;
    public MessageAdapter(ArrayList<Message> messages, Context context, AdapterInterface adapterInterface) {
        this.messages = messages;
        this.context = context;
        this.adapterInterface = adapterInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_message, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final Message message = messages.get(i);
        viewHolder.messageDisplay.setText(message.myText);
        viewHolder.firstName.setText(message.firstName);

        PrettyTime prettyTime = new PrettyTime();

        String datetime = prettyTime.format(message.date);
        viewHolder.time.setText(datetime);
        if(message.image.equals("1")){
            viewHolder.imageView.setImageResource(0);
        }else {
            try {
                Bitmap imageBitmap = decodeFromFirebaseBase64(message.image);
                viewHolder.imageView.setImageBitmap(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterInterface.onDelete(message.messageId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageDisplay, firstName, time;
        ImageView imageView;
        ImageButton delete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageDisplay = (TextView) itemView.findViewById(R.id.tv_Message);
            firstName = (TextView) itemView.findViewById(R.id.tv_firstname);
            time = (TextView) itemView.findViewById(R.id.tv_TimeStamp);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            delete = (ImageButton) itemView.findViewById(R.id.ib_Delete);
        }
    }
    public interface AdapterInterface{
        public void onDelete(String messageID);
    }
    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }
}
