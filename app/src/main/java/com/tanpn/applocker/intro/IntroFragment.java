package com.tanpn.applocker.intro;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.tanpn.applocker.MainActivity;
import com.tanpn.applocker.R;
import com.tanpn.applocker.lockservice.LockPreferences;
import com.tanpn.applocker.lockservice.LockService;
import com.tanpn.applocker.utils.PreUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 */
public class IntroFragment extends Fragment {

    private static final String BACKGROUND_COLOR = "backgroundColor";
    private static final String PAGE = "page";

    private int mBackgroundColor, mPage;

    public static IntroFragment newInstance(int backgroundColor, int page) {
        IntroFragment frag = new IntroFragment();
        Bundle b = new Bundle();
        b.putInt(PAGE, page);
        b.putInt(BACKGROUND_COLOR, backgroundColor);
        frag.setArguments(b);
        return frag;
    }
    private ViewPager viewPager;

    public static IntroFragment newInstance(int backgroundColor, int page, ViewPager vi) {
        IntroFragment frag = new IntroFragment();
        frag.viewPager = vi;
        Bundle b = new Bundle();
        b.putInt(PAGE, page);
        b.putInt(BACKGROUND_COLOR, backgroundColor);
        frag.setArguments(b);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!getArguments().containsKey(BACKGROUND_COLOR))
            throw new RuntimeException("Fragment must contain a \"" + BACKGROUND_COLOR + "\" argument!");
        mBackgroundColor = getArguments().getInt(BACKGROUND_COLOR);

        if (!getArguments().containsKey(PAGE))
            throw new RuntimeException("Fragment must contain a \"" + PAGE + "\" argument!");
        mPage = getArguments().getInt(PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Select a layout based on the current page
        int layoutResId;
        switch (mPage) {
            case 0:
                layoutResId = R.layout.layout_intro_0;
                break;
            case 1:
                layoutResId = R.layout.layout_intro_1;
                break;
            case 2:
                layoutResId = R.layout.layout_intro_2;
                break;
            case 3:
                layoutResId = R.layout.layout_intro_3;
                break;
            case 4:
                layoutResId = R.layout.layout_intro_4;
                break;
            default:
                layoutResId = R.layout.layout_intro_5;
        }

        // Inflate the layout resource file
        View view = getActivity().getLayoutInflater().inflate(layoutResId, container, false);

        // Set the current page index as the View's tag (useful in the PageTransformer)
        view.setTag(mPage);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the background color of the root view to the color specified in newInstance()
        View background = view.findViewById(R.id.intro_background);
        background.setBackgroundColor(mBackgroundColor);

        if(mPage == 4){
            initLayout4(view);
        }
        else if(mPage == 5){
            initLayout5(view);
        }

    }

    private Button btn1, btn2;
    private EditText edt1, edt2, edt3;
    private FirebaseAuth mAuth;

    private void initLayout4(View v) {
        btn1 = (Button) v.findViewById(R.id.btnNext);
        edt1 = (EditText) v.findViewById(R.id.edtUsername);
        edt2 = (EditText) v.findViewById(R.id.edtPassword);
        edt3 = (EditText) v.findViewById(R.id.edtComfirm);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goNext();
            }
        });

        mAuth = FirebaseAuth.getInstance();
    }

    private void goNext() {
        if(!validateEmail(edt1))
            return;

        if(!validatePassword(edt2))
            return;

        if(!edt2.getText().toString().equals(edt3.getText().toString())){
            edt3.setError("mật khẩu không trùng khớp");

            return;
        }


        createFirebaseAccount();
    }

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

    private static final String USERNAME_PATTERN =
            "^[a-z0-9_-]{6,15}$";

    /**

     ^                      # Start of the line
     [a-z0-9_-]	            # Match characters and symbols in the list, a-z, 0-9, underscore, hyphen
     {6,15}                 # Length at least 3 characters and maximum length of 15
     $                      # End of the line


     * */

    private boolean validateEmail(EditText input){
        String text = input.getText().toString();
        Pattern pattern = Pattern.compile(USERNAME_PATTERN);
        Matcher matcher = pattern.matcher(text);

        if(!matcher.matches()){
            // validate by using pattern
            // sai qui dinh
            input.setError("sai định dạng");
            return false;
        }
        return true;

    }

    private void createFirebaseAccount(){

        Toast.makeText(getContext(),
                "Vui lòng đợi.",
                Toast.LENGTH_LONG).show();

        String signinName = edt1.getText().toString();
        String pass = edt2.getText().toString();
        String email = signinName + "@gmail.com";
        // create user
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(getContext(),
                                    "Lỗi khi tạo tài khoản, hãy thử lại.",
                                    Toast.LENGTH_SHORT).show();

                        }
                        else {

                            Toast.makeText(getContext(),
                                    "Thành công.",
                                    Toast.LENGTH_SHORT).show();

                            // back to sign in layout
                            saveUserAccount();

                        }


                    }
                });



    }

    private void saveUserAccount() {
        // luu vao pref
        PreUtils pre = new PreUtils(getContext());

        pre.put(R.string.pref_key_user_name, edt1.getText().toString());
        pre.put(R.string.pref_key_user_password, edt2.getText().toString());
        pre.put(R.string.pref_key_user_email, edt1.getText().toString() + "@gmail.com");

        pre.apply();

        // chuyen qua layout tiep theo
        //getFragmentManager().beginTransaction().replace(R.layout.layout_intro_4, R.layout.layout_intro_5).commit();
        viewPager.setCurrentItem(5, true);
    }


    /**
         * -----------------------------------------------------------------------
         * */
    private void initLayout5(View v) {
        btn1 = (Button) v.findViewById(R.id.btnPasswordLock);
        btn2 = (Button) v.findViewById(R.id.btnPatternLock);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPasswordLock();
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPatternLock();
            }
        });
    }

    private void setPatternLock() {
        if(!checkSetUser()){

            Toast.makeText(getContext(),
                    "Vui lòng điền đầy đủ thông tin trước đó",
                    Toast.LENGTH_LONG).show();

            return;
        }


        LockService.showCreate(getContext(), LockPreferences.TYPE_PASSWORD);

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(setLock, new IntentFilter("SET_DEFAULT_PASSWORD"));

    }

    private void setPasswordLock() {
        if(!checkSetUser()){

            Toast.makeText(getContext(),
                    "Vui lòng điền đầy đủ thông tin trước đó",
                    Toast.LENGTH_LONG).show();

            return;
        }

        LockService.showCreate(getContext(), LockPreferences.TYPE_PASSWORD);

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(setLock, new IntentFilter("SET_DEFAULT_PASSWORD"));
    }

    private boolean checkSetUser(){
        PreUtils pre = new PreUtils(getContext());
        if(pre.getString(R.string.pref_key_user_name, "null").equals("null"))
            return false;
        else
            return true;
    }

    private BroadcastReceiver setLock = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onSetLock();
        }
    };

    private void onSetLock(){
        // kiem tra lock duoc dat hay chua
        PreUtils pre = new PreUtils(getContext());
        if(pre.isCurrentPasswordEmpty())
            return;


        // dat mat khau thanh cong
        // set first use != null;
        pre.put(R.string.pref_key_first_use, "abc");
        pre.apply();



        // chuyen qua Main Activity
        Intent in = new Intent(getContext(), MainActivity.class);
        startActivity(in);

        // finish
        getActivity().finish();

    }
}
