package com.example.expiration_date_control;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import java.lang.Character. *;

public class LoginActivity extends AppCompatActivity {

       // two edit text one for enter phone number other for enter OTP code
    Button sendCode,verify;    // sent_ button to request for verification and verify is for to verify code
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private String mVerificationId;
    String phoneNumber,code;

    int counterFor;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users");

        preferences = getDefaultSharedPreferences(getBaseContext());

        if(mAuth.getCurrentUser() != null){
            Intent intent = new Intent(LoginActivity.this,MainActivity.class);

            startActivity(intent);
        }

        sendCode =(Button)findViewById(R.id.sendCode);
        verify =(Button)findViewById(R.id.signIn);

        callbackVerification();

        sendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber =((EditText) findViewById(R.id.phone)).getText().toString();
                if (phoneNumber.equals("")){

                    Toast.makeText(LoginActivity.this,"Вы не ввели телефон",Toast.LENGTH_LONG).show();
                }else{
                    if (phoneNumber.charAt(0)== '+'){
                        if(phoneNumber.length() == 12){
                            startPhoneNumberVerification(phoneNumber);    // call function for receive OTP 6 digit code
                        }else{
                            Toast.makeText(LoginActivity.this,"Неверный формат телефона, номер должен состоять из 11 цифр",Toast.LENGTH_LONG).show();

                        }
                    }else{
                        Toast.makeText(LoginActivity.this,"Неверный формат телефона, номер должен начинаться с +",Toast.LENGTH_LONG).show();

                    }

                }

            }
        });
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code =((EditText) findViewById(R.id.code)).getText().toString();
                if (code.equals("")) {
                    Toast.makeText(LoginActivity.this, "Вы не ввели код подтверждения", Toast.LENGTH_LONG).show();
                }else{
                    verifyPhoneNumberWithCode(mVerificationId,code);            //call function for verify code
                }


            }
        });
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // MyProductsForRecyclerView number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks


    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("phoneNumber",phoneNumber);
                            editor.apply();

                            counterFor = 1;

                            Toast.makeText(getApplicationContext(), "sign in successfull", Toast.LENGTH_SHORT).show();
                            myRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // This method is called once with the initial value and again
                                    // whenever data at this location is updated.
                                    if (counterFor == 1){
                                        boolean isPhoneNumberAlreadyExists = false;

                                        String c = "null";
                                        try{
                                            c = dataSnapshot.child(phoneNumber).child("allProducts").child("count").getValue(Integer.class)+"";
                                        } catch (Exception e) {
                                            e.printStackTrace();

                                        }
                                        if(!c.equals("null")){
                                            isPhoneNumberAlreadyExists = true;
                                        }


                                        if (!isPhoneNumberAlreadyExists){
                                            myRef.child(phoneNumber).child("allProducts").child("count").setValue(0);
                                        }
                                        counterFor = 0;
                                    }


                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    // Failed to read value

                                }
                            });

                            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                            startActivity(intent);

                        } else {
                            // Sign in failed, display a message and update the UI

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                // [START_EXCLUDE silent]

                                // [END_EXCLUDE]
                            }

                        }
                    }
                });
    }
    private void verifyPhoneNumberWithCode(String verificationId, String code) {

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        signInWithPhoneAuthCredential(credential);
    }


    private void callbackVerification() {

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verificaiton without
                //     user action.
                // [START_EXCLUDE silent]
                // [END_EXCLUDE]

                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
                // [END_EXCLUDE]
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                // [START_EXCLUDE silent]
                // [END_EXCLUDE]

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]

                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]

                    // [END_EXCLUDE]
                }

                // Show a message and update the UI
                // [START_EXCLUDE]

                // [END_EXCLUDE]
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.

                Toast.makeText(LoginActivity.this,"Код отправлен на номер "+phoneNumber,Toast.LENGTH_LONG).show();
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // [START_EXCLUDE]
                // Update UI

                // [END_EXCLUDE]
            }
        };
    }
}
