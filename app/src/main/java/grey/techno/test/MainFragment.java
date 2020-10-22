package grey.techno.test;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

public class MainFragment extends Fragment {

    private Button scanBut;
    private Button validateBut;
    private MainVM mainVM;
    private RecyclerView mRecyclerView;
    private URLadapter mAdapter;
    private ProgressBar mProgress;
    private LinearLayout mEmptyList;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainVM = new ViewModelProvider(requireActivity()).get(MainVM.class);
        mainVM.validateBtnState.observe(this, state -> {
            if (state) {
                mAdapter.update();
//                validateBut.setEnabled(true);
                mEmptyList.setVisibility(View.GONE);
            } else {
//                validateBut.setEnabled(false);
            }
        });
        mainVM.progress.observe(this, progress -> {
            if (progress) {
                mProgress.setVisibility(View.VISIBLE);
                mRecyclerView.setAlpha(0.3f);
            } else {
                mProgress.setVisibility(View.GONE);
                mRecyclerView.setAlpha(1);
                mAdapter.update();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        mRecyclerView = v.findViewById(R.id.recycler);
        mAdapter = new URLadapter(mainVM);
        mRecyclerView.setAdapter(mAdapter);
        LinearLayoutManager lm = new LinearLayoutManager(requireActivity());
        mRecyclerView.setLayoutManager(lm);
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), lm.getOrientation()));
        mRecyclerView.setItemAnimator(null);
        mProgress = v.findViewById(R.id.progressBar);
        mEmptyList = v.findViewById(R.id.emptyList);
//        scanBut = (Button) v.findViewById(R.id.button_scan);
//        scanBut.setOnClickListener(x -> {
//            mainVM.buttonScanListener();
//        });
//        validateBut = (Button) v.findViewById(R.id.button_validate);
//        validateBut.setOnClickListener(x -> {
//            mainVM.buttonValidateListener();
//        });
        return v;
    }
}
