
package com.freyr.app.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.freyr.app.R;
import com.freyr.app.data.model.Payment;
import com.freyr.app.data.model.User;
import com.freyr.app.data.repository.FreyrRepository;

public class PagosRecycler extends RecyclerView.Adapter<PagosRecycler.PaymentViewHolder> {

    // Define la acción que se ejecuta al eliminar un pago.
    public interface OnDeletePaymentListener {
        void onDeletePayment(Payment payment);
    }

    private final Context context;
    private final OnDeletePaymentListener deleteListener;

    // Arreglos que almacenan los pagos y los usuarios.
    private Payment[] payments = new Payment[0];
    private User[] allUsers = new User[0];

    public PagosRecycler(Context context, OnDeletePaymentListener deleteListener) {
        this.context = context;
        this.deleteListener = deleteListener;
    }

    // Actualiza el arreglo de pagos y refresca la pantalla.
    public void setPayments(Payment[] payments) {
        this.payments = payments != null
                ? payments : new Payment[0];

        notifyDataSetChanged();
    }

    // Actualiza el arreglo de usuarios y refresca la pantalla.
    public void setUsers(User[] users) {
        this.allUsers = users != null
                ? users : new User[0];

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        // Carga el diseño XML de cada pago.
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_payment, parent, false);

        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PaymentViewHolder holder, int position) {

        // Obtiene el pago correspondiente a esta posición.
        Payment payment = payments[position];

        // Muestra el valor del pago con formato de moneda.
        holder.tvAmount.setText(
                FreyrRepository.formatCurrency(payment.getAmount())
        );

        // Busca el nombre del usuario que realizó el pago.
        String userName = "Usuario";

        for (User user : allUsers) {
            if (user.getId().equals(payment.getUserId())) {
                userName = user.getNomCompl();
                break;
            }
        }

        // Muestra quién pagó y la fecha del pago.
        holder.tvInfo.setText(
                "Pagado por " + userName + " · " + payment.getDate()
        );

        // Muestra la descripción solo si existe y no está vacía.
        if (payment.getDescription() != null
                && !payment.getDescription().trim().isEmpty()) {

            holder.tvDesc.setText(
                    "\"" + payment.getDescription() + "\""
            );

            holder.tvDesc.setVisibility(View.VISIBLE);

        } else {
            // Oculta el campo cuando el pago no tiene descripción.
            holder.tvDesc.setVisibility(View.GONE);
        }

        // Ejecuta la acción de eliminar cuando se pulsa el botón.
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeletePayment(payment);
            }
        });
    }

    @Override
    public int getItemCount() {
        // Devuelve la cantidad de pagos almacenados.
        return payments.length;
    }

    // Mantiene las referencias a las vistas de cada pago.
    static class PaymentViewHolder extends RecyclerView.ViewHolder {

        TextView tvAmount, tvInfo, tvDesc;
        ImageButton btnDelete;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);

            tvAmount = itemView.findViewById(R.id.tv_payment_amount);
            tvInfo = itemView.findViewById(R.id.tv_payment_info);
            tvDesc = itemView.findViewById(R.id.tv_payment_desc);
            btnDelete = itemView.findViewById(R.id.btn_delete_payment);
        }
    }
}
