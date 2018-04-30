package nl.hypothermic.tcaccess4a;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ResourceBundle;

public class AuthActivity extends AppCompatActivity {

    private EditText hostField;
    private EditText portField;
    private EditText uuidField;
    private TextView errorField;
    private Button connectBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        hostField = findViewById(R.id.hostField);
        portField = findViewById(R.id.portField);
        uuidField = findViewById(R.id.uuidField);
        errorField = findViewById(R.id.errorField);
        connectBtn = findViewById(R.id.connectBtn);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launch();
            }
        });
        ActivityManager.register(this);
    }

    private void launch() {
        if (hostField.getText().toString().matches("") || portField.getText().toString().matches("") || uuidField.getText().toString().matches("")) {
            errorField.setText("ERROR: Input fields cannot be blank.");
            return;
        }
        this.setFieldState(false);
        errorField.setText("Connecting to: " + hostField.getText().toString().trim() + ":" + Integer.parseInt(portField.getText().toString().trim()));
        Intent control = new Intent(this, ControlActivity.class);
        control.putExtra("XCore", new Core(hostField.getText().toString().trim(), Integer.parseInt(portField.getText().toString().trim()), uuidField.getText().toString().trim()));
        startActivity(control);
    }

    private void setFieldState(Boolean state) {
        hostField.setEnabled(state);
        portField.setEnabled(state);
        uuidField.setEnabled(state);
        connectBtn.setEnabled(state);
    }
}
