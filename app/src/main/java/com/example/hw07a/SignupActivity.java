package com.example.hw07a;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {
    private static final String MY_TAG = "Sign Up";

    FirebaseAuth firebaseAuth;
    User user = new User();

    EditText email;
    EditText password;
    EditText comfirmPassword;
    EditText firstName;
    EditText lastName;

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String userId = "";
        if (firebaseUser != null) {
            // intent to contact list
            userId = firebaseUser.getUid();
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            intent.putExtra(MainActivity.MY_KEY, userId);
            startActivity(intent);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        firebaseAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.et_Email);
        password = findViewById(R.id.et_Password);
        comfirmPassword = findViewById(R.id.et_comfirmPassword);
        firstName = findViewById(R.id.et_firstname);
        lastName = findViewById(R.id.et_lastname);
        Button signup = findViewById(R.id.su_btn_signup);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!firstName.getText().toString().isEmpty() && !lastName.getText().toString().isEmpty() && (password.getText().toString().compareTo(comfirmPassword.getText().toString()) == 0)) {
                    firebaseAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                            .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user1 = firebaseAuth.getCurrentUser();
                                        user.firstName = firstName.getText().toString();
                                        user.lastName = lastName.getText().toString();
                                        String userId = user1.getUid();
                                        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
                                        databaseReference.child(userId).setValue(user);
                                        Intent intent = new Intent(getBaseContext(), MainActivity.class);
                                        intent.putExtra(MainActivity.MY_KEY, userId);
                                        startActivity(intent);
                                        Toast.makeText(SignupActivity.this, "SignUp Successful", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.w("", "failure", task.getException());
                                        Toast.makeText(SignupActivity.this, "SignUp Unsuccessful", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }
            }
        });

        Button cancel = findViewById(R.id.bt_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private boolean isValidForm() {
        boolean valid = true;

        String em = email.getText().toString();
        if (TextUtils.isEmpty(em)) {
            email.setError("Required.");
            valid = false;
        } else {
            email.setError(null);
        }

        String pwd = password.getText().toString();
        if (TextUtils.isEmpty(pwd)) {
            password.setError("Required.");
            valid = false;
        } else {
            password.setError(null);
        }

        return valid;
    }
}
