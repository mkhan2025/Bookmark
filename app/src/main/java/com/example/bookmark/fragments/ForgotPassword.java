package com.example.bookmark.fragments;

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

import com.example.bookmark.R;
import com.example.bookmark.ReplacerActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


public class ForgotPassword extends Fragment {
    private EditText emailET;
    private Button recoverBtn;
    private TextView loginTV;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    public static final String EMAIL_REGEX = "^(.+)@(.+)$";

    public ForgotPassword() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
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
        recoverBtn = view.findViewById(R.id.recoverBtn);
        loginTV = view.findViewById(R.id.loginTV);
        progressBar = view.findViewById(R.id.progressBar);
        auth = FirebaseAuth.getInstance();
    }
    private void clickListener()
    {
        recoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailET.getText().toString();
                if (email.isEmpty() || !email.matches(EMAIL_REGEX))
                {
                    emailET.setError("Please enter a valid email");
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(getContext(), "Password reset email sent", Toast.LENGTH_SHORT).show();
                            emailET.setText("");
                        }
                        else
                        {
                            String errMsg = task.getException().getMessage();
                            Toast.makeText(getContext(), "Error: " +errMsg, Toast.LENGTH_SHORT).show();

                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });
        loginTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ReplacerActivity) getActivity()).setFragment(new LoginFragment());
            }
        });
    }
}