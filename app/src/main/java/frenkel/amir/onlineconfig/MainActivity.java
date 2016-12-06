package frenkel.amir.onlineconfig;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.Map;
import frenkel.amir.onlineconfiglib.ConfigManager;

public class MainActivity extends AppCompatActivity {
    private Map<String, String> map = null;
    private final ConfigManager configManager = new ConfigManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean didUserCloseAppProperly = configManager.loadDidUserLeaveSavedPreference(this);
        boolean isAppCrash = !didUserCloseAppProperly;
        configManager.saveDidUserLeavePreference(this, false);
        configManager.getCachedMap(this);
        //CauseAppToCrash();
        InitButtons(this, isAppCrash);
    }

    private void InitButtons(final Context context, final boolean isWaitSynchronously) {
        Button btnViewJson = (Button)findViewById(R.id.btnViewJson);
        btnViewJson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView1 = (TextView)findViewById(R.id.textView);
                Map<String, String> innerMap = configManager.getMap();
                Map<String, Boolean> innerUsedInMap = configManager.getUsedInMap();
                if (innerMap == null || innerUsedInMap == null) textView1.setText("");
                String str = "Map: " + innerMap.toString() + System.getProperty("line.separator") +
                        "Keys: " + innerUsedInMap.toString();
                textView1.setText(str);
            }
        });

        Button btnUseFirstKey = (Button)findViewById(R.id.btnUseFirstKey);
        btnUseFirstKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> innerMap = configManager.getMap();

                if (innerMap != null && innerMap.size() != 0) {
                    String key = innerMap.keySet().toArray()[0].toString();
                    //configManager.SetUsedItem(key);
                    configManager.toggleUsedItem(key);
                }
            }
        });

        Button btnUseSecondKey = (Button)findViewById(R.id.btnUseSecondKey);
        btnUseSecondKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> innerMap = configManager.getMap();
                if (innerMap != null && innerMap.size() != 0) {
                    String key = innerMap.keySet().toArray()[1].toString();
                    //configManager.SetUsedItem(key);
                    configManager.toggleUsedItem(key);
                }
            }
        });
        Button btnLoadJson = (Button)findViewById(R.id.btnLoadJson);
        btnLoadJson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                configManager.setIsUseFirstUrl(true);
                configManager.getConfiguration(context, isWaitSynchronously);
            }
        });
        Button btnLoadJson2 = (Button)findViewById(R.id.btnLoadJson2);
        btnLoadJson2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                configManager.setIsUseFirstUrl(false);
                configManager.getConfiguration(context, isWaitSynchronously);
            }
        });

        Button btnCrashIt = (Button)findViewById(R.id.btnCrashIt);
        btnCrashIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CauseAppToCrash();
            }
        });
    }
    private void CauseAppToCrash() {
        String ff = null;
        ff.toString();
    }

    @Override
    public void onResume(){
        super.onResume();
        configManager.saveDidUserLeavePreference(this, false);
    }
    @Override
    public void onDestroy(){
        configManager.saveDidUserLeavePreference(this, true);
    }
    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        configManager.saveDidUserLeavePreference(this, true);
    }
    @Override
    public void onUserLeaveHint(){
        configManager.saveDidUserLeavePreference(this, true);
    }
}
