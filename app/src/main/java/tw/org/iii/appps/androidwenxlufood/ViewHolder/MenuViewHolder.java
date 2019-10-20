package tw.org.iii.appps.androidwenxlufood.ViewHolder;
//1.新增刪除更新按鈕
//2.實作View.OnCreateContextMenuListener :類似按下右鍵出現選項
//3.在Common新增
//4.實做onCreateContextMenu,並且在loadMenu裡設置itemOnclik事件,並且在MenuViewHolder裡建構式加上itemView.setOnCreateContextMenuListener(this);//讓item下去時ContextMenu有反應

import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import tw.org.iii.appps.androidwenxlufood.Common.Common;
import tw.org.iii.appps.androidwenxlufood.Interface.ItemClickListener;
import tw.org.iii.appps.androidwenxlufood.R;

public class MenuViewHolder extends RecyclerView.ViewHolder implements
        View.OnClickListener,
        View.OnCreateContextMenuListener
{
    public TextView txtMenuName; //自己定義的要得標提名
    public ImageView imageView; //自己定義的圖片名

    private ItemClickListener itemClickListener; //自己寫好的介面

    //1.當取得itemView時,要先創一個item view(Card View)讓並抓到這個id
    public MenuViewHolder(View itemView) {
        super(itemView);
        txtMenuName =  itemView.findViewById(R.id.menu_name);
        imageView= itemView.findViewById(R.id.menu_image);

        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);//讓item下去時ContextMenu有反應
    }

    //2.創建一個ItemClickListener的事件(丟入寫號的介面)
    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    //1.要實作的方法之一,當下按鈕呼叫自己寫好的介面建構式
    @Override
    public void onClick(View view) {
        //3.呼叫自己定義的介面
        itemClickListener.onClick(view,getAdapterPosition(),false);//1.view,2.取得點擊的itemo位置
    }

    //add(int var1, int var2, int var3, CharSequence var4):(回傳MenuItem)
    //當常按時出現ContextMenu
    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle("Select the action");//設定標頭檔

        //設置Menu選單
        contextMenu.add(0,0,getAdapterPosition(), Common.UPDATE);
        contextMenu.add(0,1,getAdapterPosition(), Common.DELETE);
        Log.v("brad","onCreateContextMenu:" +getAdapterPosition());



    }
}
