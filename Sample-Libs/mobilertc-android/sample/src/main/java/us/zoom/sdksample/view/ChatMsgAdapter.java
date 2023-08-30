package us.zoom.sdksample.view;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import us.zoom.sdk.ZoomVideoSDKChatMessage;
import us.zoom.sdksample.R;

public class ChatMsgAdapter extends RecyclerView.Adapter<ChatMsgAdapter.MsgHolder> {

    public interface ItemClickListener {
        void onItemClick();
    }

    ItemClickListener listener;
    private List<CharSequence> list = new ArrayList<>();

    public ChatMsgAdapter() {

    }

    public ChatMsgAdapter(ItemClickListener listener) {

        this.listener = listener;
    }

    public void clear()
    {
        list.clear();
        notifyDataSetChanged();
    }

    public void onReceive(ZoomVideoSDKChatMessage item) {

        String senderName = item.getSenderUser().getUserName() + ":";
        String content = item.getContent();

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(senderName).append(content);

        builder.setSpan(new ForegroundColorSpan(Color.parseColor("#BABACC")), 0, senderName.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE); //设置前面的字体颜色
        builder.setSpan(new ForegroundColorSpan(Color.parseColor("#FFFFFF")), senderName.length(), builder.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE); //设置前面的字体颜色

        list.add(builder);

        notifyItemInserted(list.size());

    }

    @NonNull
    @Override
    public MsgHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_msg, parent, false);
        return new MsgHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MsgHolder holder, int position) {
        CharSequence item = list.get(position);
        holder.chatMsg.setText(item);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MsgHolder extends RecyclerView.ViewHolder {

        TextView chatMsg;
        TextView senderText;

        MsgHolder(View view) {
            super(view);

            chatMsg = view.findViewById(R.id.chat_msg_text);
            senderText = view.findViewById(R.id.chat_sender_text);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (null != listener) {
                        listener.onItemClick();
                    }
                }
            });

        }
    }
}
