package org.opencv.samples.facedetect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
    EditText ID,Password;
    Button login,signup,help;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ID=(EditText)findViewById(R.id.login_username_input);
        Password=(EditText)findViewById(R.id.login_password_input);
        help=(Button)findViewById(R.id.parse_login_help);
        signup=(Button)findViewById(R.id.parse_signup_button);
        login=(Button)findViewById(R.id.parse_login_button);
        login.setOnClickListener(calcResult);
        signup.setOnClickListener(calcResult1);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }
    private OnClickListener calcResult1=new OnClickListener() {
        @Override
        public void onClick(View v) {
                startActivity(new Intent().setClass(MainActivity.this, RegisActivity.class));
                return;
        }
    };


    private OnClickListener calcResult=new OnClickListener() {
        @Override
        public void onClick(View v) {
            String ID_str=ID.getText().toString();
            String Pa_str=Password.getText().toString();

            if(DBfunction.logincheck(ID_str,Pa_str)){
                final Bundle bundle = new Bundle();
                bundle.putString("ID", ID_str);
                startActivity(new Intent().setClass(MainActivity.this, RoomActivity.class).putExtras(bundle));
                return;
            }
            else{
                Toast.makeText(MainActivity.this, "LogIn Fail!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash, menu);
        return true;
    }

}
