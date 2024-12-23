package mdad.localdata.androide_library;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StaffAccountAdapter extends RecyclerView.Adapter<StaffAccountAdapter.AccountViewHolder> {

    public interface OnAccountActionListener {
        void onEdit(StaffAccount account);
        void onDelete(StaffAccount account);
    }

    private final List<StaffAccount> staffList;
    private final OnAccountActionListener listener;

    public StaffAccountAdapter(List<StaffAccount> staffList, OnAccountActionListener listener) {
        this.staffList = staffList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_staff_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        StaffAccount account = staffList.get(position);
        holder.tvUsername.setText(account.getUsername());
        holder.tvRole.setText("Role: " + account.getRole());

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(account));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(account));
    }

    @Override
    public int getItemCount() {
        return staffList.size();
    }

    static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvRole;
        Button btnEdit, btnDelete;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvRole = itemView.findViewById(R.id.tvRole);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
