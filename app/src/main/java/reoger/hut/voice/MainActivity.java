package reoger.hut.voice;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private Button button2;
    private Button button3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        button = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,VioceActivity.class));
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,DrawbleTestActivity.class));
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PackageManager packageManager = getPackageManager();


                Intent intent = new Intent();

                intent.setAction("reoger.hut.voice.a");
                intent.addCategory("reoger.hut.voice.c");
//                intent.addCategory("reoger.hut.voice.d");
//                intent.addCategory("reoger.hut.voice.e");
                intent.setDataAndType(Uri.parse("file://abs"),"text/plain");


                ResolveInfo resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                ComponentName componentName = intent.resolveActivity(packageManager);

                if(componentName ==null ){
                    Log.d("TAG","******/*/*********************************/*/*/*/*/**/*");
                }

                if(resolveInfo ==null)
                    Toast.makeText(MainActivity.this,"没有找到对应的actvity",Toast.LENGTH_SHORT).show();
                else
                    startActivity(intent);
            }
        });
    }
}
