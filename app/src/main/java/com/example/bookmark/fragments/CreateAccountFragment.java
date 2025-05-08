package com.example.bookmark.fragments;

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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.example.bookmark.utils.PasswordValidator;

public class CreateAccountFragment extends Fragment {
    private EditText nameET;
    private ProgressBar progressBar;
    private EditText emailET;
    private EditText passwordET;
    private EditText confirmPassET;
    private TextView loginTV;
    private Button signUpBtn;
    private FirebaseAuth auth;

//    public static final String EMAIL_REGEX = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
    public static final String EMAIL_REGEX = "^(.+)@(.+)$";
    public CreateAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        clickListener();
    }
    public void init(View view){
        nameET = view.findViewById(R.id.nameET);
        emailET = view.findViewById(R.id.emailET);
        passwordET = view.findViewById(R.id.passwordET);
        confirmPassET = view.findViewById(R.id.confirmPassET);
        loginTV = view.findViewById(R.id.loginTV);
        signUpBtn = view.findViewById(R.id.signUpBtn);
        progressBar = view.findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();

    }

    private void clickListener(){
        loginTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ReplacerActivity) getActivity()).setFragment(new LoginFragment());
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               String name = nameET.getText().toString();
               String email = emailET.getText().toString();
               String password = passwordET.getText().toString();
               String confirmPass = confirmPassET.getText().toString();
                // String name = "abc";
                // String email = "omer@gmail.com";
                // String password = "aaaaaa";
                // String confirmPass = "aaaaaa";

                if (name.isEmpty() || name.equals(""))
                {
                    nameET.setError("Please enter name");
                    return;
                }
                if (email.isEmpty() || !email.matches(EMAIL_REGEX))
                {
                    emailET.setError("Please enter a valid email");
                    return;
                }
                
                PasswordValidator.ValidationResult passwordValidation = PasswordValidator.validatePassword(password);
                if (!passwordValidation.isValid()) {
                    passwordET.setError(passwordValidation.getErrorMessage());
                    return;
                }
                
                if (!password.equals(confirmPass))
                {
                    confirmPassET.setError("Passwords do not match");
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                createAccount(name, email, password);

            }
        });
    }

    private void createAccount(String name, String email, String password)
    {
        //firebase auth automatically creates the user 
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    FirebaseUser user = auth.getCurrentUser();
//                    UserProfileChangeRequest.Builder request = new UserProfileChangeRequest.Builder();
//                    request.setDisplayName(name);
//                    user.updateProfile(request.build());
                    user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(getContext(), "Email verification sent", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    if (user != null)
                    {
                        uploadUser(user, name, email);
                    }
                }
                else
                {
                    progressBar.setVisibility(View.GONE);
                    String exception = task.getException().getMessage();
                    Toast.makeText(getContext(),"error"+exception, Toast.LENGTH_SHORT ).show();
                }

            }
        });
    }

    private void uploadUser(FirebaseUser user, String name, String email)
    {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("email", email);
        map.put("profileImage", "");
        map.put("uid", user.getUid());
        map.put("following", new ArrayList<String>());
        map.put("followers", new ArrayList<String>());
        map.put("bio", "");


        FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    assert getActivity() != null;
                    progressBar.setVisibility(View.GONE);
                    startActivity(new Intent(getContext().getApplicationContext(), MainActivity.class));
                    getActivity().finish();
                }
                else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error: "+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
}