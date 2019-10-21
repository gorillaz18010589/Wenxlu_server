package tw.org.iii.appps.androidwenxlufood;
//1.FoodList.xml配置新增購物車
//2.Food Model配置
//3.food_item.xnl配置
//4.FoodViewHolder 複製MenuViewHolder
//5.讀取foodList方法
//6.新增layout: add_new_food_layout
//7.showAddFoodDialog()
//8.上傳檔案方法(Storage)
//9.選擇檔案方法
//10.onActivityResult接受Itente的code
//11.當按下螢幕兩秒以上出現item/Updat/Delete事件
//12.更新FoodList事件
//13.更新圖片
//14.刪除方法
//putFile(@NonNull Uri uri):(回傳值UploadTask)

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import info.hoang8f.widget.FButton;
import tw.org.iii.appps.androidwenxlufood.Common.Common;
import tw.org.iii.appps.androidwenxlufood.Interface.ItemClickListener;
import tw.org.iii.appps.androidwenxlufood.Model.Category;
import tw.org.iii.appps.androidwenxlufood.Model.Food;
import tw.org.iii.appps.androidwenxlufood.ViewHolder.FoodViewHolder;

public class FoodListActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RelativeLayout root_Layout;

    FloatingActionButton food_lisTfab;

    //firebase
    FirebaseDatabase database;
    DatabaseReference foodList;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    String categoryId = "";//從Food_home來的參數
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

    //add new food
    EditText edtName, edtDescription, edtPrice, edtDiscount;
    FButton btnSelect, btnUpload;

    Food newFood;//新的newFood容器

    Uri saveUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        //1.init
        recyclerView = findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        root_Layout = findViewById(R.id.root_layout);

        //2.firebase
        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Foods");
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        //3.按下按鈕新增商品
        food_lisTfab = findViewById(R.id.food_lisTfab);
        food_lisTfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddFoodDialog();
            }
        });

        //4.如果有itent有來,且不為空的話,讀取FoodList,抓取catrgoryId
        if (getIntent() != null)
            categoryId = getIntent().getStringExtra("CategoryId");
        Log.v("brad", "有intente:" + categoryId);
        if (!categoryId.isEmpty()) {
            loadFoodList(categoryId);
            Log.v("brad", "有catrgoryId:" + categoryId);
        }
    }

    //5.讀取foodList方法
    private void loadFoodList(String categoryId) {
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodList.orderByChild("menuId").equalTo(categoryId)
        ) {
            @Override
            //設定food_name,food_image
            protected void populateViewHolder(FoodViewHolder foodViewHolder, Food food, int i) {
                foodViewHolder.food_name.setText(food.getName());
                Picasso.with(getBaseContext())
                        .load(food.getImage())
                        .into(foodViewHolder.food_image);

                foodViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //6.
                        showAddFoodDialog();
                    }
                });
            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    //7.顯示DiaLog輸入框
    private void showAddFoodDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Add new Food");//設定標題
        alertDialog.setMessage("Please fill full information");//設定訊息

        //因為add_new_menu_layout沒有指定的activity所以用inflater
        LayoutInflater inflater = this.getLayoutInflater();//從這個頁面取得LayoutInflater物件實體
        View add_menu_layout = inflater.inflate(R.layout.add_new_food_layout, null);//擴充抓到View(1.Virw的資源檔,2.根視圖)

        edtName = add_menu_layout.findViewById(R.id.editName);
        edtPrice = add_menu_layout.findViewById(R.id.editPrice);
        edtDescription = add_menu_layout.findViewById(R.id.editDescription);
        edtDiscount = add_menu_layout.findViewById(R.id.editDiscount);
        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);
        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);

        //配置btnSelect按鈕事件,呼叫檔案選擇方法
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();// 讓使用者選擇圖片從 Gallery和 Uri圖片
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {//當按下upload按鈕上傳圖片檔案
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

                //將新得到的Foodlist灌到newFood裡
                if (newFood != null) {
                    foodList.push().setValue(newFood);//將newFood(Storage的檔,設置在firebase上)
                    Snackbar.make(root_Layout, "New Food:" + newFood.getName() + "was added", Snackbar.LENGTH_SHORT).show();
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

    //8.上傳檔案方法(Storage)
    private void uploadImage() {
        final ProgressDialog mDoalog = new ProgressDialog(this);
        mDoalog.setMessage("Uploading...");
        mDoalog.show();

        String imageName = UUID.randomUUID().toString(); //隨機產生一個唯一的key值,再轉回到字串,當作節點名稱

        final StorageReference imageFolder = storageReference.child("images/" + imageName); //取得分類裡面的imaes資料庫節點,images/idname
        imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            //當檔案上傳成功時
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mDoalog.dismiss();
                Toast.makeText(FoodListActivity.this, "Uploaded!!!", Toast.LENGTH_SHORT).show();
                imageFolder.getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //將使用者輸入的資訊,set到New food上
                                newFood = new Food();
                                newFood.setName(edtName.getText().toString());
                                newFood.setDescription(edtDescription.getText().toString());
                                newFood.setDiscount(edtDiscount.getText().toString());
                                newFood.setMenuId(categoryId);
                                newFood.setImage(saveUri.toString());
                                Log.v("brad", "onSuccess=>" + "uri:" + saveUri.toString());
                            }
                        });
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mDoalog.dismiss();
                        Toast.makeText(FoodListActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.v("brad", "onFailure=>" + "e.Message:" + e.getMessage());
                    }
                })
                //當檔案下載時
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        mDoalog.setMessage("Uploaded" + progress + "%");
                        Log.v("brad", "onProgress=>" + "UploadTask:" + taskSnapshot);
                    }
                });


    }

    //9.選擇檔案方法
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");//設定itntet種類(所有圖片類型)
        intent.setAction(Intent.ACTION_GET_CONTENT);//設定呼叫哪些程式(ACTION_GET_CONTENT== 有哪些程式可以會自己跳出來)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Common.PICK_IMAGE_REQUEST);//設定要跳轉的頁面(1.跳轉的頁面intent,2.回應馬對應要接收的itent)
    }

    //10.onActivityResult接受Itente的code
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK //如果code== 71 而且結果送出Code== ok
                && data != null && data.getData() != null) {//而且data資料有近來
            saveUri = data.getData();//取得data資料,灌到uri裡
            btnSelect.setText("Image Selected");
        }
    }

    //11.當按下螢幕兩秒以上出現item/Updat/Delete事件
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        //按下更新按鈕
        if(item.getTitle().equals(Common.UPDATE)){

            showUpdateDialog(adapter.getRef(item.getOrder()).getKey()//1.取得這個節點的String id
            ,adapter.getItem(item.getOrder()));//取得這個選到的id的物件節點(要用在預設上)
            Log.v("brad","更新了:" + adapter.getRef(item.getOrder()).getKey());

        //按下刪除按鈕
        }else if(item.getTitle().equals(Common.DELETE)){
            deleteFood(adapter.getRef(item.getOrder()).getKey());
            Log.v("brad","刪除了:" + adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    //14.刪除方法
    private void deleteFood(String key) {
        foodList.child(key).removeValue();
    }

    //12.更新FoodList事件
    private void showUpdateDialog(final String key, final Food item) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Edit Food");//設定標題
        alertDialog.setMessage("Please fill full information");//設定訊息

        //因為add_new_menu_layout沒有指定的activity所以用inflater
        LayoutInflater inflater = this.getLayoutInflater();//從這個頁面取得LayoutInflater物件實體
        View add_menu_layout = inflater.inflate(R.layout.add_new_food_layout, null);//擴充抓到View(1.Virw的資源檔,2.根視圖)

        edtName = add_menu_layout.findViewById(R.id.editName);
        edtPrice = add_menu_layout.findViewById(R.id.editPrice);
        edtDescription = add_menu_layout.findViewById(R.id.editDescription);
        edtDiscount = add_menu_layout.findViewById(R.id.editDiscount);

        //顯示為預設定好的資訊
        edtName.setText(item.getName());//原本的填寫姓名
        edtPrice.setText(item.getPrice());//原本的填寫價格
        edtDescription.setText(item.getDescription());//原本的填寫敘述
        edtDiscount.setText(item.getDiscount());//原本的填折扣


        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);
        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);

        //配置btnSelect按鈕事件,呼叫檔案選擇方法
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();// 讓使用者選擇圖片從 Gallery和 Uri圖片
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {//當按下upload按鈕上傳圖片檔案
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

                    //更新資訊
                    item.setName(edtName.getText().toString()); //更改為你輸入的姓名
                    item.setPrice(edtPrice.getText().toString());//更改為你輸入的價格
                    item.setDescription(edtDescription.getText().toString());//更改為你輸入的敘述
                    item.setDiscount(edtDiscount.getText().toString());//更改為你輸入的折扣

                    foodList.child(key).setValue(item);//在這節點選到的id商品,設置修改好的資訊item船上去database
                    Snackbar.make(root_Layout, "Food:" + item.getName() + "was edited", Snackbar.LENGTH_SHORT).show();

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

    //13.更新圖片
    private void changeImage(final Food item) {
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
                Toast.makeText(FoodListActivity.this,"Uploaded!!!",Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(FoodListActivity.this,"" +e.getMessage(),Toast.LENGTH_SHORT ).show();
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

    }}
