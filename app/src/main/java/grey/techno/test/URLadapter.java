package grey.techno.test;


import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.util.List;

public class URLadapter extends Adapter<URLadapter.URLholder>{
    private MainVM mVM;

    private int mExpandedPosition = -1;
    private int lastExpandedPosition = -1;
    private ChainData mChain;
    private Context mContext;

    URLadapter(MainVM vm) {
        mVM = vm;
    }

    static class URLholder extends ViewHolder  {
        private TextView mTitleText;
        private TextView mURLtext;
        private TextView mDetailsText;
        private LinearLayout mDetails;
        private ImageView mIsValid;
        private ImageView mArrow;

        URLholder(View view) {
            super(view);
            mTitleText = view.findViewById(R.id.itemHeaderText);
            mArrow = view.findViewById(R.id.arrowImage);
            mIsValid = view.findViewById(R.id.isValidImage);
            mDetails = view.findViewById(R.id.itemDetails);
            mURLtext = view.findViewById(R.id.fullURLtext);
            mDetailsText = view.findViewById(R.id.itemDetailsText);
        }
    }

    @NonNull
    @Override
    public URLholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chain, parent, false);
        return new URLholder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull URLholder holder, int position) {

        mContext = holder.itemView.getContext();
        mChain = mVM.getChains().get(position);
        List<Boolean> validations = mChain.getValidations();
        holder.mTitleText.setText(parceTitleText(mChain.getBaseURL()));
        holder.mURLtext.setText(mChain.getBaseURL());
        holder.mDetailsText.setText(parceDetailsText(mChain.getSubjects(), validations));
        holder.itemView.setOnClickListener(x->{
            lastExpandedPosition = mExpandedPosition;
            if (mVM.isValidationOver()) {
                if (mExpandedPosition == position) {
                    mExpandedPosition = -1;
                } else {
                    mExpandedPosition = position;
                }
                notifyItemChanged(lastExpandedPosition);
                notifyItemChanged(mExpandedPosition);
            }
        });

        if (mVM.isValidationOver()) {
            holder.mArrow.setVisibility(View.VISIBLE);
            if (mExpandedPosition == position) {
                animationExpand(holder.mDetails, true);
                holder.mArrow.setImageResource(R.drawable.anim_arrow_down_rotate);
                ((Animatable)(holder.mArrow.getDrawable())).start();
            } else if(lastExpandedPosition == position) {
                animationExpand(holder.mDetails, false);
                holder.mArrow.setImageResource(R.drawable.anim_arrow_up_rotate);
                ((Animatable)(holder.mArrow.getDrawable())).start();
            }
            holder.mIsValid.setVisibility(View.VISIBLE);
            if (validations.size() > 0) {
                if (!mChain.getValidations().contains(false)) {
                    holder.mIsValid.setImageResource(R.drawable.ic_valid_24dp);
                } else {
                    holder.mIsValid.setImageResource(R.drawable.ic_invalid_24dp);
                }
            } else {
                holder.mIsValid.setImageResource(R.drawable.ic_invalid_24dp);
            }
        } else {
            holder.mDetails.setVisibility(View.GONE);
            holder.mIsValid.setVisibility(View.INVISIBLE);
            holder.mArrow.setVisibility(View.GONE);
            mExpandedPosition = -1;
            lastExpandedPosition = -1;
        }
    }

    @Override
    public int getItemCount() {
        return mVM.getChains().size();
    }

    void update() {
        notifyDataSetChanged();
    }

    void animationExpand(LinearLayout v, boolean isExpand) {
        int h = getExpandViewHeight(v);
        ValueAnimator animator;
        if (isExpand) {
            v.setVisibility(View.VISIBLE);
            animator = ValueAnimator.ofInt(0, h);
        } else {
            animator = ValueAnimator.ofInt(h, 0);
        }
        animator.addUpdateListener(x -> {
            v.getLayoutParams().height = (Integer) animator.getAnimatedValue();
            v.requestLayout();
        });
        animator.setDuration((int)(1.2F*h));//?
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    int getExpandViewHeight(View v){
        int width = ((View)v.getParent()).getWidth()
                - mContext.getResources().getDimensionPixelSize(R.dimen.list_item_arrow_image_width)
                - mContext.getResources().getDimensionPixelSize(R.dimen.list_item_is_valid_image_width)
                - 4 * mContext.getResources().getDimensionPixelSize(R.dimen.list_item_image_margin);
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec((width), View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(widthMeasureSpec, heightMeasureSpec);
        return v.getMeasuredHeight();
    }

    String parceTitleText(String url){
        if(url.contains("//")){
            url = url.substring(url.indexOf("//")+2);
        }
        if(url.contains("/")) {
            url = url.substring(0, url.indexOf('/'));
        }else if(url.contains("?")){
            url = url.substring(0, url.indexOf('?'));
        }
        return url;
    }

    private SpannableStringBuilder parceDetailsText(List<String> subjects, List<Boolean> validitys) {
        SpannableStringBuilder str = new SpannableStringBuilder();
        SpannableString spn;
        if (subjects.size() == 0) {
            return str.append(mContext.getResources().getString(R.string.https_not_supported));
        }
        for (int i = 0; i < subjects.size(); i++) {
            str.append(subjects.get(i)).append(": ");
            if (validitys.get(i)) {
                spn = new SpannableString(mContext.getResources().getString(R.string.valid));
                spn.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.colorValid)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                spn = new SpannableString(mContext.getResources().getString(R.string.invalid));
                spn.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.colorInvalid)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            str.append(spn);
            if (i != subjects.size() - 1) {
                str.append("\r\n");
            }
        }
        return str;
    }
}
