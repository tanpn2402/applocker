package com.tanpn.applocker.user;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;

import com.tanpn.applocker.R;
import com.tanpn.applocker.fragments.AppsFragment;

public class AccountActivity extends AppCompatActivity implements OnClickListener {

    private ImageButton ibtBack;

    public interface onButtonClick{
        public void onClick();
    }


    private void init(){
        ibtBack = (ImageButton) findViewById(R.id.ibtBack);
        ibtBack.setOnClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_account);
        //this.setTitle(getString(R.string.nav_user));
        //init
        //init();

        // chuyen den fragment all apps
        LoginFragment login  = new LoginFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.activity_account, login).commit();
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ibtBack:
                onBackPressed();
                break;



        }
    }
}
