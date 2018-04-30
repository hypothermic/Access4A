package nl.hypothermic.tcaccess4a;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ControlActivity extends AppCompatActivity {

    private Core core;
    private TextView titleField;
    private TextView statusField;
    private EditText commandField;
    private Button commandBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        titleField = (TextView) findViewById(R.id.titleField);
        statusField = (TextView) findViewById(R.id.status);
        commandField = (EditText) findViewById(R.id.commandField);
        commandBtn = (Button) findViewById(R.id.commandBtn);
        core = (Core) getIntent().getSerializableExtra("XCore");
        core.addListener(new ICoreListener() {
            @Override
            public void onStatusUpdate(final Status status) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusField.setText("Status: " + status.toString());
                    }
                });
            }

            @Override
            public void onVerbose(final String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusField.setText("Status: " + msg);
                    }
                });
            }
        });
        titleField.setText(core.toString());
        core.start();
        commandBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!titleField.getText().toString().matches("")) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    // TODO: multithread this and remove StrictMode debug tools
                    core.sendCommand(commandField.getText().toString().trim());
                    core.updateVerbose("Sending command");
                }
            }
        });
    }
}
