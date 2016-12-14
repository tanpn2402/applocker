package com.tanpn.applocker.user;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tanpn.applocker.MainActivity;
import com.tanpn.applocker.R;
import com.tanpn.applocker.utils.PreUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Signin extends AppCompatActivity implements SignInFragment.onSignInCompletion {
    private ViewPager mViewPager;
    private Button btnFunction;

    private Snackbar signinStatus;

    private PreUtils pref;

    private void init(){

        pref = new PreUtils(this);

        mViewPager = (ViewPager) findViewById(R.id.myViewPager);
        mViewPager.setAdapter(new SignInAdapter(getSupportFragmentManager()));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0: // signin view:
                        btnFunction.setText("Quên mật khẩu");
                        break;

                    default: // forgot view
                        btnFunction.setText("Đăng nhập");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        btnFunction = (Button) findViewById(R.id.btnFunction);
        btnFunction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (mViewPager.getCurrentItem()){
                    case 0: // signin view:
                        mViewPager.setCurrentItem(1);
                        btnFunction.setText("Đăng nhập");
                        break;

                    default: // forgot view
                        mViewPager.setCurrentItem(0);
                        btnFunction.setText("Quên mật khẩu");
                }
            }
        });



        signinStatus = Snackbar.make(
                findViewById(R.id.activity_login),
                "",
                Snackbar.LENGTH_INDEFINITE);

        signinStatus.setAction("Ẩn", new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                signinStatus.dismiss();
            }
        });
    }



    private FirebaseDatabase root;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;


    private void initFirebase(){
        root = FirebaseDatabase.getInstance();
        userRef = root.getReference("user");

        mAuth = FirebaseAuth.getInstance();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        init();

        initFirebase();

    }


    private EditText edtUsername, edtPassword, edtFullname;

    @Override
    public void onSignIn(EditText edt1, EditText edt2) {
        edtUsername = edt1;
        edtPassword = edt2;

        signin();
    }

    @Override
    public void onSignUp(EditText edt1, EditText edt2, EditText edt3) {
        edtFullname = edt1;
        edtUsername = edt2;
        edtPassword = edt3;

        signup();
    }

    @Override
    public void onForgot(EditText edt1) {
        edtUsername = edt1;

        forgot();
    }


    /**
     * sign in
     *
     * */
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private boolean validateEmail(EditText input){
        String text = input.getText().toString();
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(text);

        if(!matcher.matches()){
            // validate by using pattern
            // sai qui dinh
            input.setError("sai định dạng");
            return false;
        }
        return true;

    }

    private void signin(){


        // dang nhap
        // valadite email
        /**
         * Tai sao k de firebase validate luon?
         * lam nhu the nay se tiet kiem 1 buoc neu user nhap email sai -> do ton dung luong
         * */
        if(/*!validateEmail(edtUsername) ||*/ edtPassword.getText().toString().isEmpty())
            return;

        if(signinStatus.isShown())
            signinStatus.dismiss();
        signinStatus.setText("đang đăng nhập...").setDuration(Snackbar.LENGTH_INDEFINITE).show();

        // dang nhap k can network: mat khau va username da duoc luu truoc do
        if(!pref.getString(R.string.pref_key_user_name, "null").equals("null") &&
                !pref.getString(R.string.pref_key_user_password, "null").equals("null")){

            if(edtUsername.getText().toString().equals(pref.getString(R.string.pref_key_user_name)) &&
                    edtPassword.getText().toString().equals(pref.getString(R.string.pref_key_user_password))){

                signinStatus.setText("đăng nhập thành công").setDuration(Snackbar.LENGTH_LONG).show();

                // chuyen den dashboard activity
                Intent in = new Intent(this, Dashboard.class);
                startActivity(in);

                return;
            }
        }



        mAuth.signInWithEmailAndPassword(edtUsername.getText().toString(), edtPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            // sign in fail
                            if(signinStatus.isShown())
                                signinStatus.dismiss();
                            //signinStatus.setText("đăng nhập thất bại, vui lòng thử lại.").setDuration(Snackbar.LENGTH_INDEFINITE).show();
                            signinStatus.setText(task.getException().toString()).setDuration(Snackbar.LENGTH_INDEFINITE).show();

                        }
                        else {
                            // User is signed in
                            if(signinStatus.isShown())
                                signinStatus.dismiss();
                            signinStatus.setText("đăng nhập thành công").setDuration(Snackbar.LENGTH_LONG).show();
                            
                            // chuyen layout
                            gotoDashboard(task.getResult().getUser());
                        }

                    }
                });
    }

    private void gotoDashboard(FirebaseUser user) {
        // luu 1 so thong tin can thiet
        pref.put(R.string.pref_key_user_email, user.getEmail() );
        pref.put(R.string.pref_key_user_password, edtPassword.getText().toString());
        pref.put(R.string.pref_key_user_id, user.getUid());
        pref.apply();




        // chuyen den dashboard activity
        Intent in = new Intent(this, Dashboard.class);
        startActivity(in);

        //////
        this.finish();
    }


    /**
     * sign up
     * */

    private static final String PASSWORD_PATTERN =
            "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,20})";

    private boolean validatePassword(EditText input){
        String text = input.getText().toString();
        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher(text);

        if(!matcher.matches()){
            // validate by using pattern
            // sai qui dinh
            input.setError("sai định dạng");
            return false;
        }
        return true;
    }

    private String fullname;
    private String email;
    private String password;


    private void signup(){
        // dang ki

        if(!validateEmail(edtUsername))
            return;

        if(!validatePassword(edtPassword))
            return;


        if(signinStatus.isShown())
            signinStatus.dismiss();
        signinStatus.setText("đang tài khoản mới...").show();

        email = edtUsername.getText().toString();
        password = edtPassword.getText().toString();
        fullname = edtFullname.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            signinStatus.setText(task.getResult().toString()).setDuration(Snackbar.LENGTH_LONG).show();

                        }
                        else {

                            // sign up success
                            signinStatus.setText(task.getResult().toString()).setDuration(Snackbar.LENGTH_LONG).show();

                            // back to sign in layout
                            backtoSignInLayout();

                        }


                    }
                });
    }

    private void backtoSignInLayout() {
        // quay tro lại man hinh dang nhap
        mViewPager.setCurrentItem(0);
        btnFunction.setText("Quên mật khẩu");
    }


    /**
     * forget password
     * */
    private void forgot(){

    }


    @Override
    public void onBackPressed() {
        // quay ve Main
        //Intent in = new Intent(this , MainActivity.class);
        //startActivity(in);


        finish(); // thoat ung dung luon
    }

    @Override
    protected void onPause() {
        super.onPause();

        finish();
    }
}
