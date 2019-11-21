package com.example.hw07a;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    static final String MY_KEY = "GET_ITEM";
    static final String MY_TAG = "LOGIN";


    private FirebaseAuth firebaseAuth;
    Message message = new Message();
    ArrayList<Message> messages = new ArrayList<>();
    private final int PICK_IMAGE_CAMERA = 1, PICK_IMAGE_GALLERY = 2;
    String imageString = "";
    User user = new User();
//Profile activity
    private int imgUrl;


    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(getBaseContext(), LoginActivity.class);
            startActivity(intent);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();
        final EditText addText = (EditText) findViewById(R.id.et_text);
        ImageButton addPhoto = (ImageButton) findViewById(R.id.ib_add);
        ImageButton send = (ImageButton) findViewById(R.id.ib_send);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if(firebaseUser != null){
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String userId = firebaseUser.getUid();
                    user = dataSnapshot.child(userId).getValue(User.class);
                    TextView UserName = (TextView)findViewById(R.id.tv_username);
                    UserName.setText(user.firstName + " " + user.lastName);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("messages");
        ref2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messages.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Message message1 = ds.getValue(Message.class);
                    messages.add(message1);
                }
                try {
                    if(messages.size() > 0){
                        displayMessage();
                    }
                }catch (Exception e){
                    messages.clear();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        ImageButton logout = (ImageButton) findViewById(R.id.ib_logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                FirebaseAuth.getInstance().signOut();
                startActivity(intent);
            }
        });

        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPhoto();
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                final String userId = firebaseUser.getUid();
                message.myText = addText.getText().toString();
                message.date = Calendar.getInstance().getTime();
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        user = dataSnapshot.child(userId).getValue(User.class);
                        Log.d("user",user.toString());
                        message.firstName = user.firstName;
                        message.lastName = user.lastName;
                        namesReceived();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("The read failed: " ,databaseError.toString());
                    }
                });
                final EditText addText = (EditText) findViewById(R.id.et_text);
                addText.setText("");
            }
        });
    }

    public void namesReceived(){
        if(imageString.equals("")){
            message.image = "1";
        }else {
            message.image = imageString;
        }
        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("messages");
        String key = ref2.push().getKey();
        message.messageId = key;
        Log.d("message", message.toString());
        ref2.child(key).setValue(message);
        ref2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messages.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Message message1 = ds.getValue(Message.class);
                    messages.add(message1);
                }
                try {
                    if(messages.size() > 0){
                        displayMessage();
                    }
                }catch (Exception e){
                    messages.clear();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("The read failed: " ,databaseError.toString());
            }
        });
    }



    public void addPhoto(){
        PackageManager pm = getPackageManager();
        final CharSequence[] options = {"Take Photo", "Choose From Gallery","Cancel"};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Select Option");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    dialog.dismiss();
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, PICK_IMAGE_CAMERA);
                } else if (options[item].equals("Choose From Gallery")) {
                    dialog.dismiss();
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickPhoto.setAction(Intent.ACTION_GET_CONTENT);
                    pickPhoto.setType("image/*");
                    startActivityForResult(pickPhoto, PICK_IMAGE_GALLERY);
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_CAMERA) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            encodeBitmapAndSaveToFirebase(imageBitmap);
        } else if (requestCode == PICK_IMAGE_GALLERY) {
            Uri imageUri = data.getData();
            Bitmap imageBitmap = null;
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageBitmap(imageBitmap);
            encodeBitmapAndSaveToFirebase(imageBitmap);
        }
    }


    public void encodeBitmapAndSaveToFirebase(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        imageString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        Log.d("demo", imageString);
    }
    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

    public void displayMessage(){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.RecyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        MessageAdapter.AdapterInterface adapterInterface = new MessageAdapter.AdapterInterface() {
            @Override
            public void onDelete(String messageID) {
                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("messages");
                databaseReference.child(messageID).removeValue();
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        messages.clear();
                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                            Message message1 = ds.getValue(Message.class);
                            messages.add(message1);
                        }
                        try {
                            if(messages.size() > 0){
                                displayMessage();
                            }else{
                                Log.e("The read failed: " ,"no items found");
                            }
                        }catch (Exception e){
                            messages.clear();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("The read failed: " ,databaseError.toString());
                    }
                });
            }
        };
        RecyclerView.Adapter adapter = new MessageAdapter(messages, getApplicationContext(),adapterInterface);
        recyclerView.setAdapter(adapter);
    }
}
