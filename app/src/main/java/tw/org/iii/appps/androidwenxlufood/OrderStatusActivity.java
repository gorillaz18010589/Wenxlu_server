package tw.org.iii.appps.androidwenxlufood;
//1.Order Status Activity
//2.Order_layout
// 3.OrderViewHolder
// 4.創建OrderModel,RequestModel
// 5.到OrderActivity寫code
//6.Common新增CodeStatus狀態
//7.新增api i     maven { url 'https://jitpack.io' }
// implementation 'com.github.jaredrummler:material-spinner:1.0.8'下拉選單api
//8.update_order_layout
//9.Update按鈕跟Delete按鈕設定
//10.打開Wenlu客戶端
//11.在客戶端創建一個SerVice當關閉時,這個Server會處理推播


//Spinner:
//setItems(@NonNull T... items):設定多個欄位(多個欄位字串)(Spinner_void方法)
//getSelectedIndex()://取得被選取到的item(回傳Int)

//String:
//String.valueOf(int i)://轉型成String,且如果值為空不會抱錯

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.zip.Inflater;

import tw.org.iii.appps.androidwenxlufood.Common.Common;
import tw.org.iii.appps.androidwenxlufood.Interface.ItemClickListener;
import tw.org.iii.appps.androidwenxlufood.Model.Request;
import tw.org.iii.appps.androidwenxlufood.ViewHolder.OrderViewHolder;

public class OrderStatusActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;
    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    MaterialSpinner spinner; //下拉選單


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        //1.Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        //2.init
        recyclerView = findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        loadOrders();

    }
    //3.讀取顯示訂單Database("requests")
    private void loadOrders() {
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                requests
        ) {
            @Override
            protected void populateViewHolder(OrderViewHolder orderViewHolder, Request request, int i) {
                    orderViewHolder.txtOrderId.setText(adapter.getRef(i).getKey());
                    orderViewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(request.getStatus()));//設定送餐狀態(code)
                    orderViewHolder.txtOrderAddress.setText(request.getAddress());
                    orderViewHolder.txtOrderPhone.setText(request.getPhone());
                Log.v("brad","Stautus:" + request.getStatus());

                    orderViewHolder.setItemClickListener(new ItemClickListener() {
                        @Override
                        public void onClick(View view, int position, boolean isLongClick) {

                        }
                    });
            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    //4.常按兩秒時叫出現UPDATE跟DELET按鈕
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if(item.getTitle().equals("UPDATE")){//按下UPDATE按鈕時
            showUpdateDialog(
                    adapter.getRef(item.getOrder()).getKey(),
                    adapter.getItem(item.getOrder())); //顯示更新DiaLog(1.資料庫節點id 2.節點物件實體)

        }else  if(item.getTitle().equals("DELETE")){//按下DELETE按鈕時
            deleteDialog(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    //6.按下刪除按鈕,刪掉Firebase節點資料
    private void deleteDialog(String key) {//key == 點選到的Requset-id
        requests.child(key).removeValue();
    }

    //5.顯示更新DiaLog方法(1.節點資料庫id, 2.你點選的id物件)
    private void showUpdateDialog(String key, final Request item) {
       final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
       alertDialog.setTitle("Update Order");
       alertDialog.setMessage("Please chooser status");

        LayoutInflater inflater =  this.getLayoutInflater();//從這頁面取得Intflater(回傳到LayoutInflater)
        final View view = inflater.inflate(R.layout.update_order_layout,null);//擴充抓取layout(1.layout資源區,2.)

        //設定可以選擇三種欄位按鈕
        spinner = view.findViewById(R.id.statusSpinner);
        spinner.setItems("Placed","On my way","Shipped");//設定多個欄位(多個欄位字串)(Spinner_void方法)

        alertDialog.setView(view);

        final String localKey = key;//將Request的id存到localkey

        //按下Yes時將新的Requests資料,上傳到firebase
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();//關閉dialog
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));//將Requests的狀態欄位設定為(你選到sppiner的item轉為String),0,1,2等

                //將Request裡,被選取到id,上傳firbase上去(item新的已設定Request物件)
                requests.child(localKey).setValue(item);
            }
        });

        //按下NO時
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();//Dialog顯示執行出來
    }


}
