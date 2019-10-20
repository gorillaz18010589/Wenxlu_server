package tw.org.iii.appps.androidwenxlufood;
//登入頁面
//1.準備UserModel
//2.登入帳號密碼驗證

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import info.hoang8f.widget.FButton;
import tw.org.iii.appps.androidwenxlufood.Common.Common;
import tw.org.iii.appps.androidwenxlufood.Model.User;

public class SignInActivity extends AppCompatActivity {
    EditText editPhone,editPassword;
    FButton btnSignIn;

    FirebaseDatabase database;
    DatabaseReference Users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        //1.init
        editPhone = findViewById(R.id.editPhone);
        editPassword = findViewById(R.id.editPassword);

        btnSignIn = findViewById(R.id.signin_sign);

        //2.firebase init
        database = FirebaseDatabase.getInstance();
        Users = database.getReference("User");

        //3.按下Sigin in按鈕intenet到home頁面
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sigInUser(editPhone.getText().toString(),editPassword.getText().toString());
            }
        });

    }

    //3.登入帳密方法(1.使用者輸入的電話,2.使用者輸入的密碼)
    private void sigInUser(String phone ,String password) {
        final ProgressDialog dialog = new ProgressDialog(SignInActivity.this);
        dialog.setMessage("Please waiting...");
        dialog.show();

        //3.1
        final String localPhone = phone;//使用者輸入的電話
        final String localPassWord = password;//使用者輸入的密碼

        Users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(localPhone).exists()){//當Users裡(使用者輸入的電話),存在時

                    dialog.dismiss();
                    User user = dataSnapshot.child(localPhone).getValue(User.class);//取得此電話的使用者欄位User資料,存入User
                    user.setPhone(localPhone);//使用者輸入的電話,設置上去

                    if(Boolean.parseBoolean(user.getIsStaff())){//檢查員工狀態true/false

                        if(user.getPassword().equals(localPassWord)){//user資料庫密碼,是否使用者輸入的一樣
                            //login 員工帳號true,密碼正確
                            Intent homeIntent = new Intent(SignInActivity.this,HomeActivity.class);
                            Common.currentUser = user; //將使用者現在的登入user存到currentUser
                            Log.v("brad","會員登入成功手機:" +user.getName()+",密碼:" + user.getPassword()+",使用者名稱"+ user.getName());
                            startActivity(homeIntent);
                            finish();

                        }else{//密碼錯誤
                            Toast.makeText(SignInActivity.this,"Wrong PassWord",Toast.LENGTH_SHORT).show();
                        }

                    }else{//員工帳號為flase
                        Toast.makeText(SignInActivity.this,"Plase Login With Staff Account",Toast.LENGTH_SHORT).show();
                    }

                }else{//使用者電話帳號不存在
                    Toast.makeText(SignInActivity.this,"User no exists in Database",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
