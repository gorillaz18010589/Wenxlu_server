package tw.org.iii.appps.androidwenxlufood;
//主頁
//1.設定使用者名稱,存sigin帶cuuretUser過來顯示
//1.創建layout=> menu_item 一樣複製
// 2.創建intetface : ItemClickListener
//3.MenuViewHolder
//4.loadMenu處理
//5.add_new_menu_layout
//6.寫好AlertDialog布局,當按下yse根no反映
//7.取得item資訊,創建新的分類
//Storage設定規則改為true


//Builder(@NonNull Context context):創建AlertDiaLog
//getLayoutInflater():取得LayoutInflater物件實體(回傳LayoutInflater )
//inflate(int resource, @Nullable ViewGroup root)://擴充抓到View(1.Virw的資源檔,2.根視圖)(回傳View)
//setView(View view):配置View(回傳Builder)

//setType(String type):(回傳值Intent)
//setAction(@Nullable String action):(回傳值Intent)
//startActivityForResult(Intent intent,int requestCode)://設定要跳轉的頁面(1.跳轉的頁面intent,2.回應馬對應要接收的itent)
//createChooser(Intent target, CharSequence title):(回傳值Intent):建立選擇器(1.需要intent的物件,2.提示名)

//DatabaseReference.removeValue()://刪除Firebase資料庫節點
//getOrder();將字串轉成index

//刪除/更新
//1.新增刪除更新按鈕
//2.實作View.OnCreateContextMenuListener :類似按下右鍵出現選項
//3.在Common新增
//4.實做onCreateContextMenu,並且在loadMenu裡設置itemOnclik事件,並且在MenuViewHolder裡建構式加上itemView.setOnCreateContextMenuListener(this);//讓item下去時ContextMenu有反應

//loadMenu 增/刪/修/顯示
//1.伺服器端的Database先匯出修改一下
//2.Foods裡欄位的第一個字全部改小寫
//3.loadMenu =>
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

import info.hoang8f.widget.FButton;
import tw.org.iii.appps.androidwenxlufood.Common.Common;
import tw.org.iii.appps.androidwenxlufood.Interface.ItemClickListener;
import tw.org.iii.appps.androidwenxlufood.Model.Category;
import tw.org.iii.appps.androidwenxlufood.ViewHolder.MenuViewHolder;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    TextView txtFullName; //搗爛列名

    //firebase
    FirebaseDatabase database;
    DatabaseReference categories;
    FirebaseStorage storage;
    StorageReference storageReference;
    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter ;

    //view
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    //Add New Menu Layout
    EditText edtName;
    FButton btnUpload,btnSelect;

    Category newCatrgory;

    Uri saveUri; //網路圖片檔
    private  final  int PICK_IMAGE_REQUEST = 71;

    DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Menu Management");
        setSupportActionBar(toolbar);

        //4.firebase init Storage init
        database = FirebaseDatabase.getInstance();
        categories = database.getReference("Category");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("images/");

        //5.Fab icon按鈕按下去新增檔案上傳
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              showDialog();
            }
        });

         drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //1.設定User Name
        View headerView = navigationView.getHeaderView(0);//從navigationView裡取得,標頭View(index) 回傳(View)
        txtFullName = headerView.findViewById(R.id.txtFullName);
        txtFullName.setText(Common.currentUser.getName());//取得現在使用者的名稱

        //2.init RecycleView
        recyclerView = findViewById(R.id.recycler_menu);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        loadMenu();

    }

    //6.顯示Dialog方法
    private void showDialog() {
       final AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
       alertDialog.setTitle("Add new Category");//設定標題
       alertDialog.setMessage("Plase fill full information");//設定訊息

       //因為add_new_menu_layout沒有指定的activity所以用inflater
        LayoutInflater inflater = this.getLayoutInflater();//從這個頁面取得LayoutInflater物件實體
        View add_menu_layout = inflater.inflate(R.layout.add_new_menu_layout,null);//擴充抓到View(1.Virw的資源檔,2.根視圖)

        edtName = add_menu_layout.findViewById(R.id.editName);
        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);
        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);

        //配置btnSelect按鈕事件,呼叫檔案選擇方法
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();// 讓使用者選擇圖片從 Gallery和 Uri圖片
            }
        });

        //當按下upload按鈕上傳圖片檔案
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

        alertDialog.setView(add_menu_layout);//配置View為add_new_menu_layout
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);//設定icon

        //設定按下Yes後按鈕事件
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();

                //創造先的catgory
                if(newCatrgory != null){
                    categories.push().setValue(newCatrgory);//將newCatrgory(Storage的檔,設置在firebase上)
                    Snackbar.make(drawer,"New category:" + newCatrgory.getName() +"was added",Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        //設定按下NO後事件
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();//關閉dialog
            }

        });
        alertDialog.show();//顯示DiaLog


    }

    //9.上傳圖片檔方法
    //randomUUID()://隨機產生一個唯一的key值(回傳值UUID)
    private void uploadImage() {
        final ProgressDialog mDoalog = new ProgressDialog(this);
        mDoalog.setMessage("Uploading...");
        mDoalog.show();

        String imageName = UUID.randomUUID().toString(); //隨機產生一個唯一的key值,再轉回到字串,當作id

        final StorageReference imageFolder = storageReference.child("images/"+ imageName); //取得分類裡面的imaes資料庫節點,images/idname
        imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            //當檔案上傳成功時
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mDoalog.dismiss();
                Toast.makeText(HomeActivity.this,"Uploaded!!!",Toast.LENGTH_SHORT).show();
                imageFolder.getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //set value for newCategory if image upload and we can get download Link
                        newCatrgory = new Category(uri.toString(),edtName.getText().toString());//設定新分類(1.選擇的Uri圖檔,2.你輸入的檔案名,)
                        Log.v("brad","onSuccess=>"+"uri:" + uri);
                    }
                });
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mDoalog.dismiss();
                        Toast.makeText(HomeActivity.this,"" +e.getMessage(),Toast.LENGTH_SHORT ).show();
                        Log.v("brad","onFailure=>" +"e.Message:"+ e.getMessage());
                    }
                })
                //當檔案下載時
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        mDoalog.setMessage("Uploaded" + progress +"%");
                        Log.v("brad","onProgress=>" +"UploadTask:" + taskSnapshot);
                    }
                });


    }

    //8.準備接受intent的事件
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
        && data != null && data.getData() != null){
            saveUri = data.getData();
            btnSelect.setText("Image Selected");
        }
    }

    //7.按下Select選擇圖片檔案
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");//設定itntet種類(所有圖片類型)
        intent.setAction(Intent.ACTION_GET_CONTENT);//設定呼叫哪些程式(ACTION_GET_CONTENT== 有哪些程式可以會自己跳出來)
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);//設定要跳轉的頁面(1.跳轉的頁面intent,2.回應馬對應要接收的itent)
    }

    //3.用FirebaseRecyclerAdapter讀取category灌到RecycleView
    private void loadMenu() {
        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(
                Category.class,
                R.layout.menu_item,
                MenuViewHolder.class,
                categories
        ) {
            @Override
            protected void populateViewHolder(MenuViewHolder menuViewHolder, Category category, int i) {
                    menuViewHolder.txtMenuName.setText(category.getName());
                    Picasso.with(HomeActivity.this)//設定頁面
                            .load(category.getImage())//讀取圖片
                            .into(menuViewHolder.imageView);//圖片顯示

                //按下item圖片時叫出menu的選項
                menuViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //sned Category id and start new activity
                        Log.v("brad","menuViewHolder.ItemOnClicik,"+"postion:" + position);
                    }
                });
            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }


    //按下返回鍵時
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //漢堡按鈕產生時
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    //漢堡按鈕導覽列,有點選item時
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();



        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Update / Delete
    //10.當menu選單被選擇時,刪除,更新等按鈕
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {//Menu Item 類似UPDATE按鈕根DElETE按鈕Item

        //點選UPADE按鈕時
        if(item.getTitle().equals(Common.UPDATE)){//如果這個item的欄位名稱等於UPDATE的話
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),
                   adapter.getItem(item.getOrder()));
            Log.v("brad","MenuItem=>UPDATE:" + item.getOrder());

        //點選DELETE按鈕時
        }else if(item.getTitle().equals(Common.DELETE)){
            deleteDialog(adapter.getRef(item.getOrder()).getKey());
            Log.v("brad","MenuItem=>DELETE:" + item.getOrder() +"item:" +item);
        }

        return super.onContextItemSelected(item);

    }
    //13.當按下DELETE按鈕時刪除
    private void deleteDialog(String key) {
        Log.v("brad","key:" +key);
        categories.child(key).removeValue();
        Toast.makeText(HomeActivity.this,"Item deleted",Toast.LENGTH_SHORT).show();

    }


    //11.當按下更新Update時方法
    private void showUpdateDialog(final String key, final Category item) {
        //複製ShowAddDiaLog 並且修改
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
        alertDialog.setTitle("UPDATE Category");//設定標題
        alertDialog.setMessage("Plase fill full information");//設定訊息

        //因為add_new_menu_layout沒有指定的activity所以用inflater
        LayoutInflater inflater = this.getLayoutInflater();//從這個頁面取得LayoutInflater物件實體
        View add_menu_layout = inflater.inflate(R.layout.add_new_menu_layout,null);//擴充抓到View(1.Virw的資源檔,2.根視圖)

        edtName = add_menu_layout.findViewById(R.id.editName);
        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);
        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);

        //設定為Category 裡面的名字
        edtName.setText(item.getName());

        //配置btnSelect按鈕事件,呼叫檔案選擇方法
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();// 讓使用者選擇圖片從 Gallery和 Uri圖片
            }
        });

        //當按下upload按鈕上傳圖片檔案
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeImage(item);
            }
        });

        alertDialog.setView(add_menu_layout);//配置View為add_new_menu_layout
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);//設定icon

        //設定按下Yes後按鈕事件
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();

                //Update information
                item.setName(edtName.getText().toString());//設置新的名字
                categories.child(key).setValue(item);
            }
        });

        //設定按下NO後事件
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();//關閉dialog
            }

        });
        alertDialog.show();//顯示DiaLog


    }

    //12.圖片更新方式,直接複製uploadImage去修改
    private void changeImage(final Category item) {
        final ProgressDialog mDoalog = new ProgressDialog(this);
        mDoalog.setMessage("Uploading...");
        mDoalog.show();

        String imageName = UUID.randomUUID().toString(); //隨機產生一個唯一的key值,再轉回到字串,當作id

        final StorageReference imageFolder = storageReference.child("images/"+ imageName); //取得分類裡面的imaes資料庫節點,images/idname
        imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            //當檔案上傳成功時
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mDoalog.dismiss();
                Toast.makeText(HomeActivity.this,"Uploaded!!!",Toast.LENGTH_SHORT).show();
                imageFolder.getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //set value for newCategory if image upload and we can get download Link
                                item.setImage(uri.toString());//將這個Catrgory設定新的圖片(uri圖片網址,轉成String)
                                Log.v("brad","Update:onSuccess=>"+"uri:" + uri);
                            }
                        });
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mDoalog.dismiss();
                        Toast.makeText(HomeActivity.this,"" +e.getMessage(),Toast.LENGTH_SHORT ).show();
                        Log.v("brad","onFailure=>" +"e.Message:"+ e.getMessage());
                    }
                })
                //當檔案下載時
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        mDoalog.setMessage("Uploaded" + progress +"%");
                        Log.v("brad","onProgress=>" +"UploadTask:" + taskSnapshot);
                    }
                });

    }


}
