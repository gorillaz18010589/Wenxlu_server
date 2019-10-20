package tw.org.iii.appps.androidwenxlufood;
//1.Main Screen
// 2.Login
// 3.Register
// 4.Create Database with Firebase
//        *firebase storage連接,跟建案
//        *API加入
//        implementation 'com.google.firebase:firebase-storage:16.0.4'
//        implementation 'com.google.firebase:firebase-auth:16.0.5'
//        implementation 'com.google.firebase:firebase-database:16.0.4'
//        implementation 'info.hoang8f:fbutton:1.0.5'
//        *直接複製主頁的xml,圖片複製,color調整,style,sologan,
//        *自行設計字形
//         *sigin_xml
//         *firebase的json資料輸出 User新增欄位 "IsStaff":"false" 員工狀態,
//伺服器端有龐大資料,故不開放註冊,手動更改員工狀態即可
//Model準備,登入方法
//Home頁面配置
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;

public class MainActivity extends AppCompatActivity {
    Button btnSigIn;
    TextView txtSlogan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //1.init
        btnSigIn = findViewById(R.id.btnSignIn);
        txtSlogan = findViewById(R.id.txtSlogan);

        //2.文字設定字形
        Typeface face = Typeface.createFromAsset(getAssets(),"fonts/Nabila.ttf");
        txtSlogan.setTypeface(face);

        btnSigIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signIn = new Intent(MainActivity.this,SignInActivity.class);
                startActivity(signIn);
                Log.v("brad","btnSigin按鈕 => Sigin頁面");
            }
        });
    }
}
