package com.example.bookmark.fragments;

import static android.view.View.VISIBLE;
import static com.example.bookmark.fragments.CreateAccountFragment.EMAIL_REGEX;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookmark.MainActivity;
import com.example.bookmark.R;
import com.example.bookmark.ReplacerActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginFragment extends Fragment {

    private EditText emailET;
    private EditText passwordET;
    private TextView forgotTV;
    private TextView signUpTV;
    private Button logInBtn;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        clickListener();
    }

    private void init(View view)
    {
       emailET = view.findViewById(R.id.emailET);
       passwordET = view.findViewById(R.id.passwordET);
       forgotTV = view.findViewById(R.id.forgotTV);
       signUpTV = view.findViewById(R.id.signUpTV);
       logInBtn = view.findViewById(R.id.loginInBtn);
       progressBar = view.findViewById(R.id.progressBar);
       forgotTV = view.findViewById(R.id.forgotTV);
       auth = FirebaseAuth.getInstance();
    }
    private void clickListener()
    {
        forgotTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ReplacerActivity) getActivity()).setFragment(new ForgotPassword());
            }
        });

        logInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailET.getText().toString();
                String password = passwordET.getText().toString();

                if (email.isEmpty() || !email.matches(EMAIL_REGEX))
                {
                    emailET.setError("Please enter a valid email");
                }
                if (password.isEmpty() || password.length() < 6)
                {
                    passwordET.setError("Please enter a valid password");
                }
                progressBar.setVisibility(VISIBLE);
                // firebaseAuth automatically handles authentication.
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            FirebaseUser user = auth.getCurrentUser();
                            if (!user.isEmailVerified())
                            {
                                Toast.makeText(getContext(), "Please verify your email", Toast.LENGTH_SHORT).show();
                            }
                            sendUserToMainActivity();
                        }
                        else
                        {
                            String exception = "Error" + task.getException().getMessage();
                            Toast.makeText(getContext(), exception, Toast.LENGTH_SHORT ). show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
        signUpTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ReplacerActivity) getActivity()).setFragment(new CreateAccountFragment());
            }
        });

    }
    private void sendUserToMainActivity(){
        if (getActivity() == null)
        {
            return;
        }
        progressBar.setVisibility(View.GONE);
        startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
        getActivity().finish();
    }
}