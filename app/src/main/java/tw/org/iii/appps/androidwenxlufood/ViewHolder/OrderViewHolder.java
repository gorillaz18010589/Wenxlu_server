package tw.org.iii.appps.androidwenxlufood.ViewHolder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import tw.org.iii.appps.androidwenxlufood.Interface.ItemClickListener;
import tw.org.iii.appps.androidwenxlufood.R;

public class OrderViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener,View.OnCreateContextMenuListener{
    public TextView txtOrderId, txtOrderStatus, txtOrderPhone , txtOrderAddress;
    public ItemClickListener itemClickListener;

    //1.初始化時抓取order_layout裡要玩的原件
    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);
        txtOrderId= itemView.findViewById(R.id.order_id);
        txtOrderStatus = itemView.findViewById(R.id.order_status);
        txtOrderPhone = itemView.findViewById(R.id.order_phone);
        txtOrderAddress = itemView.findViewById(R.id.order_address);

        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);
    }

    //3.寫一個設置ItemClickListener的事件(丟入寫號的介面),當點到這個item時,把你點到的質抓到
    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    //2.設定點及下去的方法為,自己定義的取得id位置,跟這個view
    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);//1.view,2.取得點擊的itemo位置
    }
    //4.當常按兩秒產生右鍵選單
    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle("Select the action");

        contextMenu.add(0,0,getAdapterPosition(),"UPDATE");
        contextMenu.add(0,1,getAdapterPosition(),"DELETE");


    }
}

