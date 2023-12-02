package com.huffazai.huffazai.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.huffazai.huffazai.R;


public class AccountActivity extends AppCompatActivity {
    public static final   String TAG = AccountActivity.class.getSimpleName();
    private Button btnChangeEmail, btnChangePassword, btnSendResetEmail, btnRemoveUser,
            changeEmail, changePassword, sendEmail, remove, signOut;
    private EditText oldEmail, newEmail, password, newPassword;
    private ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(AccountActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };
        btnChangeEmail = (Button) findViewById(R.id.change_email_button);
        btnChangePassword = (Button) findViewById(R.id.change_password_button);
        btnSendResetEmail = (Button) findViewById(R.id.sending_pass_reset_button);
        btnRemoveUser = (Button) findViewById(R.id.remove_user_button);
        changeEmail = (Button) findViewById(R.id.changeEmail);
        changePassword = (Button) findViewById(R.id.changePass);
        sendEmail = (Button) findViewById(R.id.send);
        remove = (Button) findViewById(R.id.remove);
        signOut = (Button) findViewById(R.id.sign_out);

        oldEmail = (EditText) findViewById(R.id.old_email);
        newEmail = (EditText) findViewById(R.id.new_email);
        password = (EditText) findViewById(R.id.password);
        newPassword = (EditText) findViewById(R.id.newPassword);

        oldEmail.setVisibility(View.GONE);
        newEmail.setVisibility(View.GONE);
        password.setVisibility(View.GONE);
        newPassword.setVisibility(View.GONE);
        changeEmail.setVisibility(View.GONE);
        changePassword.setVisibility(View.GONE);
        sendEmail.setVisibility(View.GONE);
        remove.setVisibility(View.GONE);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        btnChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Make relevant UI elements visible
                oldEmail.setVisibility(View.GONE);
                newEmail.setVisibility(View.VISIBLE);
                password.setVisibility(View.VISIBLE);
                newPassword.setVisibility(View.VISIBLE);
                changeEmail.setVisibility(View.VISIBLE);
                changePassword.setVisibility(View.GONE);
                sendEmail.setVisibility(View.GONE);
                remove.setVisibility(View.GONE);

                changeEmail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressBar.setVisibility(View.VISIBLE);

                        String currentPassword = password.getText().toString().trim();
                        String newEmailAddress = newEmail.getText().toString().trim();
                        String newPasswordText = newPassword.getText().toString().trim();

                        if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newEmailAddress) || TextUtils.isEmpty(newPasswordText)) {
                            Toast.makeText(AccountActivity.this, "All fields must be filled", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            return;
                        }

                        // Reauthenticate the user
                        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
                        user.reauthenticate(credential)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> reauthTask) {
                                        if (reauthTask.isSuccessful()) {
                                            // User reauthenticated successfully, proceed with email verification
                                            user.verifyBeforeUpdateEmail(newEmailAddress)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> verifyBeforeUpdateEmailTask) {
                                                            if (verifyBeforeUpdateEmailTask.isSuccessful()) {
                                                                // Verification email sent, email will be updated upon redemption
                                                                Toast.makeText(AccountActivity.this, "Verification email sent", Toast.LENGTH_SHORT).show();

                                                                // Optionally, update the user's password
                                                                user.updatePassword(newPasswordText)
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> updatePasswordTask) {
                                                                                if (updatePasswordTask.isSuccessful()) {
                                                                                    // Password update successful
                                                                                    Log.d("PasswordUpdate", "Password update successful");
                                                                                } else {
                                                                                    // Handle password update failure
                                                                                    String errorMessage = updatePasswordTask.getException().getMessage();
                                                                                    Log.d("PasswordUpdate", "Failed to update password: " + errorMessage);
                                                                                }
                                                                                progressBar.setVisibility(View.GONE);
                                                                            }
                                                                        });
                                                            } else {
                                                                // Failed to send verification email or update email
                                                                String errorMessage = verifyBeforeUpdateEmailTask.getException().getMessage();
                                                                Log.d("EmailUpdate", "Failed to send verification email: " + errorMessage);
                                                                Toast.makeText(AccountActivity.this, "Failed to send verification email: " + errorMessage, Toast.LENGTH_LONG).show();
                                                                progressBar.setVisibility(View.GONE);
                                                            }
                                                        }
                                                    });
                                        } else {
                                            // Failed to reauthenticate
                                            String errorMessage = reauthTask.getException().getMessage();
                                            Log.d("Reauthentication", "Failed to re-authenticate: " + errorMessage);
                                            Toast.makeText(AccountActivity.this, "Failed to re-authenticate!", Toast.LENGTH_LONG).show();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });
                    }
                });
            }
        });


        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Make relevant UI elements visible
                oldEmail.setVisibility(View.VISIBLE);
                newEmail.setVisibility(View.GONE);
                password.setVisibility(View.VISIBLE);
                newPassword.setVisibility(View.VISIBLE);
                changeEmail.setVisibility(View.VISIBLE);
                changePassword.setVisibility(View.GONE);
                sendEmail.setVisibility(View.GONE);
                remove.setVisibility(View.GONE);

                changeEmail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressBar.setVisibility(View.VISIBLE);

                        String currentPassword = password.getText().toString().trim();
                        String oldEmailAddress = oldEmail.getText().toString().trim();
                        String newPasswordText = newPassword.getText().toString().trim();

                        if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(oldEmailAddress) || TextUtils.isEmpty(newPasswordText)) {
                            Toast.makeText(AccountActivity.this, "All fields must be filled", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            return;
                        }

                        // Update the user's email in Firebase Authentication
                        user.updateEmail(oldEmailAddress)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> updateEmailTask) {
                                        if (updateEmailTask.isSuccessful()) {
                                            // Re-authenticate the user with the updated email
                                            AuthCredential credential = EmailAuthProvider.getCredential(oldEmailAddress, currentPassword);
                                            user.reauthenticate(credential)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> reauthTask) {
                                                            if (reauthTask.isSuccessful()) {
                                                                // Re-authentication successful, continue with your logic
                                                                // ...

                                                                // Optionally, update the user's password
                                                                user.updatePassword(newPasswordText)
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> updatePasswordTask) {
                                                                                if (updatePasswordTask.isSuccessful()) {
                                                                                    // Password update successful
                                                                                    Toast.makeText(AccountActivity.this, "Password update successful!", Toast.LENGTH_LONG).show();
                                                                                    Log.d("PasswordUpdate", "Password update successful");
                                                                                } else {
                                                                                    // Handle password update failure
                                                                                    String errorMessage = updatePasswordTask.getException().getMessage();
                                                                                    Toast.makeText(AccountActivity.this, "Failed to update password!", Toast.LENGTH_LONG).show();
                                                                                    Log.d("PasswordUpdate", "Failed to update password: " + errorMessage);
                                                                                }
                                                                                progressBar.setVisibility(View.GONE);
                                                                            }
                                                                        });
                                                            } else {
                                                                String errorMessage = reauthTask.getException().getMessage();
                                                                Log.d("Reauthentication", "Failed to re-authenticate: " + errorMessage);
                                                                Toast.makeText(AccountActivity.this, "Failed to re-authenticate!", Toast.LENGTH_LONG).show();
                                                                progressBar.setVisibility(View.GONE);
                                                            }
                                                        }
                                                    });
                                        } else {
                                            String errorMessage = updateEmailTask.getException().getMessage();
                                            Log.d("EmailUpdate", "Failed to update email: " + errorMessage);
                                            Toast.makeText(AccountActivity.this, "Failed to update email!", Toast.LENGTH_LONG).show();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });
                    }
                });
            }
        });

        btnSendResetEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldEmail.setVisibility(View.VISIBLE);
                newEmail.setVisibility(View.GONE);
                password.setVisibility(View.GONE);
                newPassword.setVisibility(View.GONE);
                changeEmail.setVisibility(View.GONE);
                changePassword.setVisibility(View.GONE);
                sendEmail.setVisibility(View.VISIBLE);
                remove.setVisibility(View.GONE);
            }
        });
        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (!oldEmail.getText().toString().trim().equals("")) {
                    auth.sendPasswordResetEmail(oldEmail.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(AccountActivity.this, "Reset password. email is sent!", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(AccountActivity.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                } else {
                    oldEmail.setError("Enter email");
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        btnRemoveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an AlertDialog to confirm the account deletion
                AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this);
                builder.setTitle("Confirm Account Deletion");
                builder.setMessage("Are you sure you want to delete your account? This action cannot be undone.");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressBar.setVisibility(View.VISIBLE);
                        if (user != null) {
                            user.delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(AccountActivity.this, "Your account is deleted", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(AccountActivity.this, SignupActivity.class));
                                                finish();
                                                progressBar.setVisibility(View.GONE);
                                            } else {
                                                Toast.makeText(AccountActivity.this, "Failed to delete your account!", Toast.LENGTH_SHORT).show();
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing and close the dialog
                    }
                });
                builder.show();
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an AlertDialog to confirm the sign-out action
                AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this);
                builder.setTitle("Confirm Sign Out");
                builder.setMessage("Are you sure you want to sign out?");
                builder.setPositiveButton("Sign Out", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Call the signOut method when the user confirms
                        signOut();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing and close the dialog
                    }
                });
                builder.show();
            }
        });

    }





    //sign out method
    public void signOut() {
        auth.signOut();
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }
}