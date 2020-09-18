package grey.techno.test;


import android.content.res.Resources;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.util.List;


public class URLadapter extends Adapter<URLadapter.URLholder> {
    private MainVM mVM;

    private Resources mResources;
    private int mExpandedPosition = -1;

    URLadapter(MainVM vm, Resources resources) {
        mResources = resources;
        mVM = vm;
    }

    @NonNull
    @Override
    public URLholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chain, parent, false);
        return new URLholder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull URLholder holder, int position) {
        (holder).bind(mVM.getChains().get(position), position);
    }

    @Override
    public int getItemCount() {
        return mVM.getChains().size();
    }

    class URLholder extends ViewHolder implements View.OnClickListener {
        private ChainData mChain;
        private int mPosition;
        private TextView mHeaderText;
        private TextView mDetailsText;
        private ImageView mIsValid;

        URLholder(View view) {
            super(view);
            mHeaderText = itemView.findViewById(R.id.itemHeaderText);
            mDetailsText = itemView.findViewById(R.id.itemDetailsText);
            mIsValid = itemView.findViewById(R.id.isValidImage);
            itemView.setOnClickListener(this);
        }

        void bind(ChainData chain, int position) {
            mChain = chain;
            List<Boolean> validations = mChain.getValidations();
            mPosition = position;
            mHeaderText.setText(mChain.getBaseURL());
            mDetailsText.setText(parceDetailsText(mChain.getSubjects(), validations));
            if (mVM.isValidationOver()) {
                if (mExpandedPosition == mPosition) {
                    mDetailsText.setVisibility(View.VISIBLE);
                } else {
                    mDetailsText.setVisibility(View.GONE);
                }
            } else {
                mDetailsText.setVisibility(View.GONE);
                mExpandedPosition = -1;
            }
            if (validations.size() > 0) {
                if (!mChain.getValidations().contains(false)) {
                    mIsValid.setImageResource(R.drawable.ic_valid_24dp);
                } else {
                    mIsValid.setImageResource(R.drawable.ic_invalid_24dp);
                }
                mIsValid.setVisibility(View.VISIBLE);
            } else {
                mIsValid.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            if (mVM.isValidationOver()) {
                if (mExpandedPosition == mPosition) {
                    mExpandedPosition = -1;
                } else {

                    mExpandedPosition = mPosition;
                }
                notifyDataSetChanged();
            }
        }
    }

    void update() {
        notifyDataSetChanged();
    }

    private SpannableStringBuilder parceDetailsText(List<String> subjects, List<Boolean> validitys) {
        SpannableStringBuilder str = new SpannableStringBuilder();
        SpannableString spn;
        if (subjects.size() == 0) {
            return str.append(mResources.getString(R.string.https_not_supported));
        }
        for (int i = 0; i < subjects.size(); i++) {
            str.append(subjects.get(i)).append(": ");
            if (validitys.get(i)) {
                spn = new SpannableString(mResources.getString(R.string.valid));
                spn.setSpan(new ForegroundColorSpan(mResources.getColor(R.color.colorValid, null)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                spn = new SpannableString(mResources.getString(R.string.invalid));
                spn.setSpan(new ForegroundColorSpan(mResources.getColor(R.color.colorInvalid, null)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            str.append(spn);
            if (i != subjects.size() - 1) {
                str.append("\r\n");
            }
        }
        return str;
    }
}
