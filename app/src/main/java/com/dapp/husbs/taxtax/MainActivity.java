package com.dapp.husbs.taxtax;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dapp.husbs.taxtax.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    Button btnSignIn,btnRegister ;
    RelativeLayout rootlayout ;

    FirebaseAuth auth ;
    FirebaseDatabase db ;
    DatabaseReference users ;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        fonts
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                                        .setDefaultFontPath("fonts/Arkhip_font.ttf")
                                        .setFontAttrId(R.attr.fontPath)
                                        .build());
        setContentView(R.layout.activity_main);

//        init Firebase
        auth = FirebaseAuth.getInstance() ;
        db = FirebaseDatabase.getInstance() ;
        users =  db.getReference("Users") ;


        btnRegister =(Button) findViewById(R.id.btnRegister) ;
        btnSignIn = (Button)  findViewById(R.id.btnSignIn) ;
        rootlayout =(RelativeLayout) findViewById(R.id.rootLayout) ;

//        Event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            showRegisterDialog() ;
            }
        });
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoginDialog();
            }
        });
    }

    private void showLoginDialog() {

        final AlertDialog.Builder dialog =  new AlertDialog.Builder(this) ;
        dialog.setTitle("SIGN IN") ;
        dialog.setMessage("Please use email to SIGN IN ") ;

        LayoutInflater inflater = LayoutInflater.from(this) ;
        View login_layout = inflater.inflate(R.layout.layout_login, null ) ;

        final MaterialEditText edtEmail =  login_layout.findViewById(R.id.editEmail) ;
        final MaterialEditText edtPassword =  login_layout.findViewById(R.id.editPassword) ;


        dialog.setView(login_layout) ;

//        set Button
        dialog.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();


//                click Validation
                if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                    Snackbar.make(rootlayout, "please enter email address", Snackbar.LENGTH_SHORT).show();
                    return;
                }


                if (TextUtils.isEmpty(edtPassword.getText().toString())) {
                    Snackbar.make(rootlayout, "please enter password", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (edtPassword.getText().toString().length() < 6) {
                    Snackbar.make(rootlayout, "password too short !!!", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                final String email =  edtEmail.getText().toString() ;
                final String password =  edtPassword.getText().toString() ;
//                Login
                auth.signInWithEmailAndPassword(email,password)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Intent intent =  new Intent(MainActivity.this,Welcome.class);
                                startActivity(intent);
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(rootlayout, "Failed "+ e.getMessage(),Snackbar.LENGTH_SHORT).show();

                    }
                });

            }
        });


         dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
             }
         });


        dialog.show() ;
    }

    private void showRegisterDialog() {
        final AlertDialog.Builder dialog =  new AlertDialog.Builder(this) ;
        dialog.setTitle("REGISTER") ;
        dialog.setMessage("Please use email to register ") ;

        LayoutInflater inflater = LayoutInflater.from(this) ;
        View Resgister_layout = inflater.inflate(R.layout.layout_register, null ) ;


        final MaterialEditText edtEmail =  Resgister_layout.findViewById(R.id.editEmail);
        final MaterialEditText edtPassword =  Resgister_layout.findViewById(R.id.editPassword) ;
        final MaterialEditText edtName =  Resgister_layout.findViewById(R.id.editName) ;
        final MaterialEditText edtPhone =   Resgister_layout.findViewById(R.id.editPhone) ;


        dialog.setView(Resgister_layout) ;

//        set Button
        dialog.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();



//                click Validation
                if (TextUtils.isEmpty(edtEmail.getText().toString())){
                    Snackbar.make(rootlayout,"please enter email address", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty( edtPhone.getText().toString())){
                    Snackbar.make(rootlayout,"please enter phone number", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edtPassword.getText().toString())){
                    Snackbar.make(rootlayout,"please enter password", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (edtPassword.getText().toString().length() < 6 ){
                    Snackbar.make(rootlayout,"password too short !!!", Snackbar.LENGTH_SHORT).show();
                    return;
                }

//                Register new User
                final String email =  edtEmail.getText().toString() ;
                final String password =  edtPassword.getText().toString() ;
                final String phone =  edtPhone.getText().toString() ;
                final String name =  edtName.getText().toString() ;
                auth.createUserWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
//                                    save user to bd
                                User user =  new User() ;
                                user.setEmail(email);
                                user.setName(name);
                                user.setPassword(password);
                                user.setPhone(phone);
//                                Use email to key
                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Snackbar.make(rootlayout,"Register Successfully  !!!", Snackbar.LENGTH_SHORT).show();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(rootlayout," Failed" +e.getMessage() , Snackbar.LENGTH_LONG).show();

                                    }
                                });

                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(rootlayout," Failed" +e.getMessage() , Snackbar.LENGTH_SHORT).show();

                    }
                });

            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
            }
        }) ;

        dialog.show() ;

    }


}
