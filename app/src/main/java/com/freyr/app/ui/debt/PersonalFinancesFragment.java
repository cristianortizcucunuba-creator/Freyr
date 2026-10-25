package com.freyr.app.ui.debt;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.freyr.app.FreyrApplication;
import com.freyr.app.R;
import com.freyr.app.data.model.Debt;
import com.freyr.app.data.model.Payment;
import com.freyr.app.data.repository.FreyrRepository;
import com.freyr.app.ui.adapter.DebtAdapter;
import java.util.ArrayList;
import java.util.List;

public class PersonalFinancesFragment extends Fragment {

    private Button btnAddPersonalDebt;
    private RadioGroup rgFilter;
    private RecyclerView rvPersonalDebts;
    private DebtAdapter debtAdapter;

    private FreyrRepository repository;
    private List<Debt> allDebts = new ArrayList<>();
    private List<Payment> allPayments = new ArrayList<>();
    private String currentFilter = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal_finances, container, false);
        repository = ((FreyrApplication) requireActivity().getApplication()).getRepository();

        initViews(view);
        setupListeners();
        setupRecyclerView();
        observeData();

        return view;
    }

    private void initViews(View view) {
        btnAddPersonalDebt = view.findViewById(R.id.btn_add_personal_debt);
        rgFilter = view.findViewById(R.id.rg_debt_filter);
        rvPersonalDebts = view.findViewById(R.id.rv_personal_debts);
    }

    private void setupListeners() {
        btnAddPersonalDebt.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddDebtActivity.class);
            startActivity(intent);
        });

        rgFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_filter_pending) {
                currentFilter = "pending";
            } else if (checkedId == R.id.rb_filter_paid) {
                currentFilter = "paid";
            } else {
                currentFilter = "all";
            }
            updateFilteredList();
        });
    }

    private void setupRecyclerView() {
        debtAdapter = new DebtAdapter(requireContext(), new DebtAdapter.OnDebtClickListener() {
            @Override
            public void onDebtClick(Debt debt) {
                Intent intent = new Intent(getActivity(), DebtDetailActivity.class);
                intent.putExtra("debt_id", debt.getId());
                startActivity(intent);
            }

            @Override
            public void onPayClick(Debt debt) {
                Intent intent = new Intent(getActivity(), AddPaymentActivity.class);
                intent.putExtra("debt_id", debt.getId());
                startActivity(intent);
            }
        });

        rvPersonalDebts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPersonalDebts.setAdapter(debtAdapter);
    }

    private void observeData() {
        repository.getAllDebtsLive().observe(getViewLifecycleOwner(), debts -> {
            if (debts != null) {
                allDebts = debts;
                updateFilteredList();
            }
        });

        repository.getAllPaymentsLive().observe(getViewLifecycleOwner(), payments -> {
            if (payments != null) {
                allPayments = payments;
                debtAdapter.setPayments(payments);
                updateFilteredList();
            }
        });
    }

    private void updateFilteredList() {
        String uid = repository.getCurrentUserId();
        List<Debt> personalDebts = new ArrayList<>();

        for (Debt d : allDebts) {
            if (d.getFamilyGroupId() == null && (uid.equals(d.getAssignedUserId()) || uid.equals(d.getCreatedBy()))) {
                double paid = 0.0;
                for (Payment p : allPayments) {
                    if (p.getDebtId().equals(d.getId())) {
                        paid += p.getAmount();
                    }
                }
                double remaining = Math.max(0.0, d.getAmount() - paid);
                boolean isPaid = remaining == 0.0 || "paid".equalsIgnoreCase(d.getStatus());

                if ("pending".equals(currentFilter) && !isPaid) {
                    personalDebts.add(d);
                } else if ("paid".equals(currentFilter) && isPaid) {
                    personalDebts.add(d);
                } else if ("all".equals(currentFilter)) {
                    personalDebts.add(d);
                }
            }
        }
        debtAdapter.setDebts(personalDebts);
    }
}
