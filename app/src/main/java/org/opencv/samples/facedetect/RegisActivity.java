package org.opencv.samples.facedetect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisActivity extends Activity {
    EditText ID,Password,email,phone;
    Button signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regis);
        ID=(EditText)findViewById(R.id.username);
        Password=(EditText)findViewById(R.id.password);
        email=(EditText)findViewById(R.id.email);
        phone=(EditText)findViewById(R.id.phone);
        signup=(Button)findViewById(R.id.sign_up);
        signup.setOnClickListener(calcResult);

    }

    private OnClickListener calcResult=new OnClickListener() {
        @Override
        public void onClick(View v) {
            String ID_str=ID.getText().toString();
            String Pa_str=Password.getText().toString();
            String email_str=email.getText().toString();
            String phone_str=phone.getText().toString();
            if (ID_str.equals("Robert") ) {
                Toast.makeText(RegisActivity.this, "ID has been used!", Toast.LENGTH_SHORT).show();
                return;
            }
            else if (Pa_str.length()< 6) {
                Toast.makeText(RegisActivity.this, "Password is too short!", Toast.LENGTH_SHORT).show();
                return;
            }
            else if (email_str.equals("robert@gmail.com")) {
                Toast.makeText(RegisActivity.this, "Email has been used!", Toast.LENGTH_SHORT).show();
                return;
            }
            else{
                Toast.makeText(RegisActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                DBfunction.create_account(ID_str,Pa_str,email_str);
                startActivity(new Intent().setClass(RegisActivity.this, MainActivity.class));
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
