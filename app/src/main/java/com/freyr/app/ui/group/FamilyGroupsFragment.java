package com.freyr.app.ui.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.freyr.app.FreyrApplication;
import com.freyr.app.R;
import com.freyr.app.data.model.FamilyGroup;
import com.freyr.app.data.repository.FreyrRepository;
import com.freyr.app.ui.adapter.FamilyGroupAdapter;
import java.util.ArrayList;
import java.util.List;

public class FamilyGroupsFragment extends Fragment {

    private Button btnCreateGroup;
    private RecyclerView rvFamilyGroups;
    private FamilyGroupAdapter groupAdapter;

    private FreyrRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_family_groups, container, false);
        repository = ((FreyrApplication) requireActivity().getApplication()).getRepository();

        initViews(view);
        setupListeners();
        setupRecyclerView();
        observeData();

        return view;
    }

    private void initViews(View view) {
        btnCreateGroup = view.findViewById(R.id.btn_create_group);
        rvFamilyGroups = view.findViewById(R.id.rv_family_groups);
    }

    private void setupListeners() {
        btnCreateGroup.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateFamilyGroupActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        groupAdapter = new FamilyGroupAdapter(requireContext(), group -> {
            Intent intent = new Intent(getActivity(), FamilyGroupDetailActivity.class);
            intent.putExtra("group_id", group.getId());
            startActivity(intent);
        });

        rvFamilyGroups.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFamilyGroups.setAdapter(groupAdapter);
    }

    private void observeData() {
        String uid = repository.getCurrentUserId();
        groupAdapter.setCurrentUserId(uid);

        repository.getAllGroupsLive().observe(getViewLifecycleOwner(), groups -> {
            if (groups != null) {
                List<FamilyGroup> userGroups = new ArrayList<>();
                for (FamilyGroup g : groups) {
                    if (g.getMemberIds() != null && g.getMemberIds().contains(uid)) {
                        userGroups.add(g);
                    }
                }
                groupAdapter.setGroups(userGroups);
            }
        });

        repository.getAllDebtsLive().observe(getViewLifecycleOwner(), debts -> {
            if (debts != null) {
                groupAdapter.setDebts(debts);
            }
        });

        repository.getAllPaymentsLive().observe(getViewLifecycleOwner(), payments -> {
            if (payments != null) {
                groupAdapter.setPayments(payments);
            }
        });
    }
}
