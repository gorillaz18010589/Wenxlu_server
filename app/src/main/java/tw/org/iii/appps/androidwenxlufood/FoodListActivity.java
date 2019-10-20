package tw.org.iii.appps.androidwenxlufood;
//1.FoodList.xml配置新增購物車
//2.Food Model配置
//3.food_item.xnl配置
//4.FoodViewHolder 複製MenuViewHolder
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import tw.org.iii.appps.androidwenxlufood.Model.Food;
import tw.org.iii.appps.androidwenxlufood.ViewHolder.FoodViewHolder;

public class FoodListActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FloatingActionButton fab;

    //firebase
    FirebaseDatabase database;
    DatabaseReference foodList;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    String CategoryId = "";
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);
    }
}
