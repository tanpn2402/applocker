package com.tanpn.applocker.lockservice;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.tanpn.applocker.R;

/**
 * Created by phamt_000 on 11/9/16.
 * Hiển thị màn hình khóa dạng passwork 4 kí tự
 */
public class PasswordView extends ViewGroup implements View.OnClickListener, View.OnLongClickListener {



    // Tạo 1 interface để làm sự kiện
    public interface OnNumberListener {

        public void onStart();

        public void onNumberButton(String newNumber);

        public void onOkButton();

        public void onOkButtonLong();

        public void onBackButton();

        public void onBackButtonLong();
    }

    // DEFINE
    // drawable id của 2 nút chức năng
    private final int mOkImageResource = R.drawable.ic_action_accept;
    private final int mBackImageResource = R.drawable.ic_action_cancel;

    // ATTRIBUTES

    private OnNumberListener mListener;

    // private TextView mTextView;

    public void setListener(OnNumberListener listener) {
        this.mListener = listener;
    }

    private Button[] mButtons; // Danh sach các phím số
    private ImageButton mBackButton; // nút trở về
    private ImageButton mOkButton;   // nút OK


    private int mHorizontalSpacing;     // khoảng cách giữa các nút theo chiều ngang
    private int mVerticalSpacing;       // khoảng cách giữa các nút theo chiều dọc
    private int mChildWidth;            // chiều rộng của nút
    private int mChildHeight;           // chiều dài của nút

    private int mRows = 3;                  // số hàng
    private int mCols = 3;                  // số cột

    private int mWidth;                 //
    private int mHeight;                //
    private int mMaxHeight;
    private int mMaxWidth;

    // padding --> make sure các nút nhìn cho đẹp, giữa màn hình
    private int mPaddingLeft = 0;
    private int mPaddingTop = 0;
    private int mPaddingRight = 0;
    private int mPaddingBottom = 0;

    /** How many times may the view be taller than wide? */
    private float mMaxVScale = 1.2f;    // số lần scale theo chiều dọc
    /** How many times may the view be wider than tall? */
    private float mMaxHScale = 1.2f;   // số lần scale theo chiều ngang

    private String mPassword = "";

    // FUNCTIONS


    public PasswordView(Context context){
        super(context);
    }

    public PasswordView(Context context, AttributeSet attr){
        super(context, attr);
        TypedArray typedArray = context.obtainStyledAttributes(attr,
                R.styleable.PasswordView);


        boolean mSquareChildren;
        try {
            mHorizontalSpacing = typedArray.getDimensionPixelSize(
                    R.styleable.PasswordView_horizontalSpacing, 0);
            mVerticalSpacing = typedArray.getDimensionPixelSize(
                    R.styleable.PasswordView_verticalSpacing, 0);
            mRows = typedArray.getInteger(R.styleable.PasswordView_rows, mRows);
            mCols = typedArray.getInteger(R.styleable.PasswordView_cols, mCols);

            // Avoid Arithmetic Exceptions
            if (mRows <= 0)
                mRows = 1;
            if (mCols <= 0)
                mCols = 1;

            mMaxWidth = typedArray.getDimensionPixelSize(
                    R.styleable.PasswordView_maxWidth, 0);
            mMaxHeight = typedArray.getDimensionPixelSize(
                    R.styleable.PasswordView_maxHeight, 0);
            mMaxHScale = typedArray.getFloat(R.styleable.PasswordView_maxHScale, 1F);
            mMaxVScale = typedArray.getFloat(R.styleable.PasswordView_maxVScale, 1F);

            mSquareChildren = typedArray.getBoolean(
                    R.styleable.PasswordView_squareChildren, false);

        } finally {
            typedArray.recycle();
        }

        // This prevents a bug with children not being measured correctly
        mPaddingLeft = getPaddingLeft();
        mPaddingRight = getPaddingRight();
        mPaddingTop = getPaddingTop();
        mPaddingBottom = getPaddingBottom();
        setPadding(0, 0, 0, 0);

        if (mSquareChildren && mHorizontalSpacing == mVerticalSpacing) {
            mMaxHScale = (float) mCols / mRows;
            mMaxVScale = (float) mRows / mCols;
        } else if (mSquareChildren) {
            Log.w("TAG",
                    "To get square children, horizontal and vertical spacing should be set to equal!");
        }



    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // lấy tất cả các phím số
        mButtons = new Button[] {
                (Button) findViewById(R.id.numlock_b0),
                (Button) findViewById(R.id.numlock_b1),
                (Button) findViewById(R.id.numlock_b2),
                (Button) findViewById(R.id.numlock_b3),
                (Button) findViewById(R.id.numlock_b4),
                (Button) findViewById(R.id.numlock_b5),
                (Button) findViewById(R.id.numlock_b6),
                (Button) findViewById(R.id.numlock_b7),
                (Button) findViewById(R.id.numlock_b8),
                (Button) findViewById(R.id.numlock_b9)
        };

        // sét tất cả các nút có sự kiện onCLick

        for (Button b : mButtons)
            b.setOnClickListener(this);


        // khởi tạo 2 nút chức năng: back & ok
        mBackButton = (ImageButton) findViewById(R.id.numlock_bLeft);
        mOkButton = (ImageButton) findViewById(R.id.numlock_bRight);

        mBackButton.setImageResource(mBackImageResource);
        mBackButton.setOnClickListener(this);
        mBackButton.setOnLongClickListener(this);

        mOkButton.setImageResource(mOkImageResource);
        mOkButton.setOnClickListener(this);
        mOkButton.setOnLongClickListener(this);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // xử lý lại width và height
        mWidth = resolveSize(MeasureSpec.getSize(widthMeasureSpec),
                widthMeasureSpec);
        mHeight = resolveSize(MeasureSpec.getSize(heightMeasureSpec),
                heightMeasureSpec);

        // căn chỉnh lại các thông số
        correctViewSize(mWidth, mHeight, mMaxWidth, mMaxHeight, mMaxHScale,mMaxVScale);

        // Đo lại chiều dài và chiều rộng, vì có khả năng mất 1 số chỗ
        int childMeasureWidth = MeasureSpec.makeMeasureSpec(mChildWidth,
                MeasureSpec.EXACTLY);
        int childMeasureHeight = MeasureSpec.makeMeasureSpec(mChildHeight,
                MeasureSpec.EXACTLY);

        final int childCount = getChildCount();         // lấy tất cả các nút
        for(int i = 0 ; i < childCount; i++){
            View view   = getChildAt(i);
            measureChild(view, childMeasureWidth, childMeasureHeight);
        }

        // đặt lại dimension sau khi đã measure
        setMeasuredDimension(mWidth, mHeight);
    }


    private void correctViewSize(int width, int height, int maxWidth, int maxHeight, float maxHScale, float maxVScale){

        if (maxWidth != 0)
            width = Math.min(width, maxWidth);
        if (maxHeight != 0)
            height = Math.min(height, maxHeight);
        float hScale = (float) width / height;
        float vScale = (float) height / width;

        // Vertical stretch
        if (hScale <= maxHScale) {
            int desiredHeight = (int) ((float) width * maxVScale);
            height = Math.min(height, desiredHeight);
        }

        // Horizontal stretch
        else if (vScale <= maxVScale) {
            int desiredWidth = (int) ((float) height * maxHScale);
            width = Math.min(width, desiredWidth);
        }

        int horizontalSpacing = mHorizontalSpacing * (mCols - 1);
        int verticalSpacing = mVerticalSpacing * (mRows - 1);

        mChildWidth = (width - mPaddingLeft - mPaddingRight - horizontalSpacing)
                / mCols;
        mChildHeight = (height - mPaddingTop - mPaddingBottom - verticalSpacing)
                / mRows;

        // Set the correct values
        mWidth = mPaddingLeft + mPaddingRight + (mChildWidth * mCols)
                + (mHorizontalSpacing * (mCols - 1));
        mHeight = mPaddingTop + mPaddingBottom + (mChildHeight * mRows)
                + (mVerticalSpacing * (mRows - 1));
    }

    // implemment từ OnClickListener
    private boolean mStarted;
    @Override
    public void onClick(View v) {
        if (!mStarted) {
            mListener.onStart();
            mStarted = true;
        }

        if (v.getId() == mOkButton.getId()) {
            onOkButtonImpl();
        } else if (v.getId() == mBackButton.getId()) {
            onBackButtonImpl();
        } else {
            onNumberButtonImpl(v);
        }

        // rung khi nhấn nút
        if (mEnableHapticFeedback) {
            performHapticFeedback(
                    HapticFeedbackConstants.VIRTUAL_KEY,
                    HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                            | HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
    }

    private void onBackButtonLongImpl() {
        clearPassword();
        if (mListener != null) {
            mListener.onBackButtonLong();
        }
    }

    private void onOkButtonLongImpl() {
        if (mListener != null) {
            mListener.onOkButtonLong();
        }
    }

    private void onOkButtonImpl() {
        if (mListener != null) {
            mListener.onOkButton();
        }
    }

    private void onBackButtonImpl() {
        if (mPassword.length() != 0) {
            // StringBuilder sb = new StringBuilder(mPassword);
            // sb.deleteCharAt(sb.length() - 1);
            // setPassword(sb.toString());
            clearPassword();
        }
        if (mListener != null) {
            mListener.onBackButton();
        }
    }

    private void onNumberButtonImpl(View v) {
        Button b = (Button) v;
        final String newPassword = new StringBuilder().append(mPassword)
                .append(b.getText()).toString();
        setPassword(newPassword);
        // post instead of executing, so that the
        // last dot in the password gets displayed
        post(new Runnable() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.onNumberButton(newPassword);
                }
            }
        });
    }


    // implemment từ OnLongClickListener
    @Override
    public boolean onLongClick(View v) {
        if (!mStarted) {
            mListener.onStart();
            mStarted = true;
        }

        if (v.getId() == mOkButton.getId()) {
            onOkButtonLongImpl();
        } else if (v.getId() == mBackButton.getId()) {
            onBackButtonLongImpl();
        }

        // rung khi nhấn nút
        if (mEnableHapticFeedback) {
            performHapticFeedback(
                    HapticFeedbackConstants.LONG_PRESS,
                    HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                            | HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
        return true;
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        final int count = getChildCount();
        int childL, childT;
        childL = childT = 0;

        for (int k = 0; k < count; k++) {
            final View child = getChildAt(k);
            childL = mPaddingLeft
                    + ((mHorizontalSpacing + mChildWidth) * (k % mCols));
            childT = mPaddingTop
                    + ((mVerticalSpacing + mChildHeight) * (k / mCols));

            child.layout(childL, childT, childL + mChildWidth, childT
                    + mChildHeight);
        }
    }


    public void setPassword(String password) {
        this.mPassword = (password != null) ? password : "";
    }
    public void clearPassword() {
        setPassword(null);
    }

    public String getPassword() {
        return mPassword;
    }


    public void setOkButtonVisibility(int visibility) {
        if (mOkButton != null) {
            mOkButton.setVisibility(visibility);
        }
    }

    /**
     * Set whether the view will use tactile feedback. If true, there will be
     * tactile feedback as the user enters the pattern.
     *
     * @param tactileFeedbackEnabled
     *            Whether tactile feedback is enabled
     */
    private boolean mEnableHapticFeedback = false;

    public void setTactileFeedbackEnabled(boolean tactileFeedbackEnabled) {
        mEnableHapticFeedback = tactileFeedbackEnabled;
    }

    /**
     * @param swap
     *            True if the buttons should be swapped
     */
    public void setSwitchButtons(boolean swap) {
        int okVisibility = mOkButton.getVisibility();
        int backVisibility = mBackButton.getVisibility();
        if (swap) {
            mBackButton = (ImageButton) findViewById(R.id.numlock_bRight);
            mOkButton = (ImageButton) findViewById(R.id.numlock_bLeft);
        } else {
            mBackButton = (ImageButton) findViewById(R.id.numlock_bLeft);
            mOkButton = (ImageButton) findViewById(R.id.numlock_bRight);
        }
        mOkButton.setImageResource(mOkImageResource);
        mBackButton.setImageResource(mBackImageResource);
        mOkButton.setVisibility(okVisibility);
        mBackButton.setVisibility(backVisibility);

    }
}
