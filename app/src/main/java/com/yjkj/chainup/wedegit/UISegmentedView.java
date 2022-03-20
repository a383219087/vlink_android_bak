package com.yjkj.chainup.wedegit;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.yjkj.chainup.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class UISegmentedView extends RadioGroup {

    //Helpers
    private int mSdk;
    private Context mCtx;

    //Interaction
    private OnSelectionChangedListener mListener;

    //UI
    private int selectedColor = Color.parseColor("#0099CC");
    private int unselectedColor = Color.TRANSPARENT;
    private int unselectedTextColor = Color.parseColor("#0099CC");
    private int defaultSelection = -1;
    private boolean stretch = false;
    private int selectedTextColor = Color.WHITE;
    private float textSize;
    private float stroke;
    private boolean equalWidth = false;
    private String identifier = "";
    private ColorStateList textColorStateList;
    private int unit = TypedValue.COMPLEX_UNIT_SP;

    //Item organization
    private LinkedHashMap<String, String> itemMap = new LinkedHashMap<String, String>();
    private ArrayList<RadioButton> options;

    public UISegmentedView(Context context) {
        super(context, null);
        //Initialize
        init(context);
        // Setup the view
        update();
    }

    public UISegmentedView(Context context, AttributeSet attrs) throws Exception {
        super(context, attrs);

        //Initialize
        init(context);

        //Here's where overwrite the defaults with the values from the xml attributes
        TypedArray attributes = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.UISegmentedView,
                0, 0);

        try {

            selectedColor = attributes.getColor(R.styleable.UISegmentedView_selectedColor, selectedColor);
            selectedTextColor = attributes.getColor(R.styleable.UISegmentedView_selectedTextColor, selectedTextColor);
            unselectedColor = attributes.getColor(R.styleable.UISegmentedView_unselectedColor, unselectedColor);
            unselectedTextColor = attributes.getColor(R.styleable.UISegmentedView_unselectedTextColor, selectedColor);

            //Set text selectedColor state list
            textColorStateList = new ColorStateList(new int[][]{
                    {-android.R.attr.state_checked}, {android.R.attr.state_checked}},
                    new int[]{unselectedTextColor, selectedTextColor}
            );

            textSize = attributes.getFloat(R.styleable.UISegmentedView_svTextSize, 12);
            stroke = attributes.getFloat(R.styleable.UISegmentedView_stroke, 0.5f);
            defaultSelection = attributes.getInt(R.styleable.UISegmentedView_defaultSelection, defaultSelection);
            equalWidth = attributes.getBoolean(R.styleable.UISegmentedView_equalWidth, equalWidth);
            stretch = attributes.getBoolean(R.styleable.UISegmentedView_stretch, stretch);
            identifier = attributes.getString(R.styleable.UISegmentedView_identifier);

            CharSequence[] itemArray = attributes.getTextArray(R.styleable.UISegmentedView_items);
            CharSequence[] valueArray = attributes.getTextArray(R.styleable.UISegmentedView_values);

            // TODO: Need to look into better setting up the item for the preview view
            if (this.isInEditMode()) {
                itemArray = new CharSequence[]{"YES", "NO", "MAYBE", "DON'T KNOW"};
            }

            //Item and value arrays need to be of the same length
            if (itemArray != null && valueArray != null) {
                if (itemArray.length != valueArray.length) {
                    throw new Exception("Item labels and value arrays must be the same size");
                }
            }

            if (itemArray != null) {

                if (valueArray != null) {
                    for (int i = 0; i < itemArray.length; i++) {
                        itemMap.put(itemArray[i].toString(), valueArray[i].toString());
                    }

                } else {

                    for (CharSequence item : itemArray) {
                        itemMap.put(item.toString(), item.toString());
                    }

                }

            }

        } finally {
            attributes.recycle();
        }

        //Setup the view
        update();
    }

    private void init(Context context) {
        mCtx = context;
        //Needed for calling the right "setbackground" method
        mSdk = Build.VERSION.SDK_INT;
        //Provide a tad bit of padding for the view
        this.setPadding(0, 0, 0, 0);
    }

    public void clear() {
        this.clearCheck();
    }

    /**
     * Does the setup and re-setup of the view based on the currently set options
     */
    @TargetApi(16)
    private void update() {

        //Remove all views...
        this.removeAllViews();

        //Get size of two DP (size of stroke)
        int twoDP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, stroke, getResources().getDisplayMetrics());

        //Ensure orientation is horizontal
        this.setOrientation(RadioGroup.HORIZONTAL);


        float textWidth = 0;
        options = new ArrayList<RadioButton>();

        Iterator itemIterator = itemMap.entrySet().iterator();
        int i = 0;
        while (itemIterator.hasNext()) {

            Map.Entry<String, String> item = (Map.Entry) itemIterator.next();
            RadioButton rb = new RadioButton(mCtx);
            rb.setTextColor(textColorStateList);
            rb.setTextSize(unit, textSize);
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            if (stretch) {
                params.weight = 1.0f;
            }
            if (i > 0) {
                params.setMargins(-twoDP, 0, 0, 0);
            }

            rb.setLayoutParams(params);


            //Clear out button drawable (text only)
            rb.setButtonDrawable(new StateListDrawable());

            //Create state list for background
            if (i == 0) {
                //Left
                GradientDrawable leftUnselected = (GradientDrawable) mCtx.getResources().getDrawable(R.drawable.segmented_left_option).mutate();
                leftUnselected.setStroke(twoDP, unselectedTextColor);

                leftUnselected.setColor(unselectedColor);
                GradientDrawable leftSelected = (GradientDrawable) mCtx.getResources().getDrawable(R.drawable.segmented_left_option_selected).mutate();
                leftSelected.setColor(selectedColor);
                leftSelected.setStroke(twoDP, unselectedTextColor);
                StateListDrawable leftStateListDrawable = new StateListDrawable();
                leftStateListDrawable.addState(new int[]{-android.R.attr.state_checked}, leftUnselected);
                leftStateListDrawable.addState(new int[]{android.R.attr.state_checked}, leftSelected);
                if (mSdk < Build.VERSION_CODES.JELLY_BEAN) {
                    rb.setBackgroundDrawable(leftStateListDrawable);
                } else {
                    rb.setBackground(leftStateListDrawable);
                }


            } else if (i == (itemMap.size() - 1)) {
                //Right
                GradientDrawable rightUnselected = (GradientDrawable) mCtx.getResources().getDrawable(R.drawable.segmented_right_option).mutate();
                rightUnselected.setStroke(twoDP, unselectedTextColor);

                rightUnselected.setColor(unselectedColor);
                GradientDrawable rightSelected = (GradientDrawable) mCtx.getResources().getDrawable(R.drawable.segmented_right_option_selected).mutate();
                rightSelected.setColor(selectedColor);
                rightSelected.setStroke(twoDP, unselectedTextColor);

                StateListDrawable rightStateListDrawable = new StateListDrawable();
                rightStateListDrawable.addState(new int[]{-android.R.attr.state_checked}, rightUnselected);
                rightStateListDrawable.addState(new int[]{android.R.attr.state_checked}, rightSelected);
                if (mSdk < Build.VERSION_CODES.JELLY_BEAN) {
                    rb.setBackgroundDrawable(rightStateListDrawable);
                } else {
                    rb.setBackground(rightStateListDrawable);
                }

            } else {
                //Middle
                GradientDrawable middleUnselected = (GradientDrawable) mCtx.getResources().getDrawable(R.drawable.segmented_middle_option).mutate();
                middleUnselected.setStroke(twoDP, unselectedTextColor);
                middleUnselected.setDither(true);
                middleUnselected.setColor(unselectedColor);
                GradientDrawable middleSelected = (GradientDrawable) mCtx.getResources().getDrawable(R.drawable.segmented_middle_option_selected).mutate();
                middleSelected.setColor(selectedColor);
                middleSelected.setStroke(twoDP, unselectedTextColor);

                StateListDrawable middleStateListDrawable = new StateListDrawable();
                middleStateListDrawable.addState(new int[]{-android.R.attr.state_checked}, middleUnselected);
                middleStateListDrawable.addState(new int[]{android.R.attr.state_checked}, middleSelected);
                if (mSdk < Build.VERSION_CODES.JELLY_BEAN) {
                    rb.setBackgroundDrawable(middleStateListDrawable);
                } else {
                    rb.setBackground(middleStateListDrawable);
                }

            }

            rb.setLayoutParams(params);
            rb.setMinWidth(twoDP * 10);
            rb.setGravity(Gravity.CENTER);
            rb.setTypeface(null, Typeface.NORMAL);
            rb.setText(item.getKey());
            textWidth = Math.max(rb.getPaint().measureText(item.getKey()), textWidth);
            options.add(rb);

            i++;
        }

        //We do this to make all the segments the same width
        for (RadioButton option : options) {
            if (equalWidth) {
                //option.setTextSize(12);
                option.setGravity(Gravity.CENTER);
                option.setWidth((int) (textWidth + (twoDP * 20)));
            }
            this.addView(option);
        }

        this.setOnCheckedChangeListener(selectionChangedListener);

        if (defaultSelection > -1) {
            this.check(((RadioButton) getChildAt(defaultSelection)).getId());
        }
//        int size = this.getChildCount();
//        if (size > 0) {
//            for (int j = 0; j < size; j++) {
//                this.getChildAt(j).setId(j);
//            }
//        }
        this.setGravity(Gravity.CENTER);
    }

    /**
     * TypedValue
     *
     * @param unitType 参考TypedValue
     */
    public void setUnitType(int unitType) {
        this.unit = unitType;
    }

    public void setTexSize(float texSize) {
        this.textSize = texSize;
    }

    public void setTexSize(int unit, float texSize) {
        setUnitType(unit);
        this.textSize = texSize;
    }

    /**
     * Get currently selected segment and the view identifier
     *
     * @return string array of identifier [0] value of currently selected segment [1]
     */
    public String[] getCheckedWithIdentifier() {
        return new String[]{identifier, itemMap.get(((RadioButton) this.findViewById(this.getCheckedRadioButtonId())).getText().toString())};
    }

    /**
     * Get currently selected segment
     *
     * @return value of currently selected segment
     */
    public String getChecked() {
        return itemMap.get(((RadioButton) this.findViewById(this.getCheckedRadioButtonId())).getText().toString());
    }

    /**
     * Used to pass along the selection change event
     * Calls onSelectionChangedListener with identifier and value of selected segment
     */
    private OnCheckedChangeListener selectionChangedListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (mListener != null) {
                try {
                    mListener.onSelectionChanged(group.indexOfChild(group.findViewById(checkedId)));
                } catch (Exception e) {

                }
            }

        }
    };

    /**
     * Interface for for the selection change event
     */
    public interface OnSelectionChangedListener {
        public void onSelectionChanged(int position, String value);
        public void onSelectionChanged(int position);
    }

    /**
     * Sets the items and vaules for each segements.
     *
     * @param itemArray
     * @param valueArray
     * @throws Exception
     */
    public void setItems(String[] itemArray, String[] valueArray) throws Exception {

        itemMap.clear();

        if (itemArray != null && valueArray != null) {
            if (itemArray.length != valueArray.length) {
                throw new Exception("Item labels and value arrays must be the same size");
            }
        }

        if (itemArray != null) {

            if (valueArray != null) {
                for (int i = 0; i < itemArray.length; i++) {
                    itemMap.put(itemArray[i].toString(), valueArray[i].toString());
                }

            } else {

                for (CharSequence item : itemArray) {
                    itemMap.put(item.toString(), item.toString());
                }

            }


        }

        update();
    }

    /**
     * Sets the items and vaules for each segements. Also provides a helper to setting the
     * default selection
     *
     * @param items
     * @param values
     * @param defaultSelection
     * @throws Exception
     */
    public void setItems(String[] items, String[] values, int defaultSelection) throws Exception {

        if (defaultSelection > (items.length - 1)) {
            throw new Exception("Default selection cannot be greater than the number of items");
        } else {
            this.defaultSelection = defaultSelection;
            setItems(items, values);
        }
    }

    /**
     * Sets the item that is selected by default. Must be greater than -1.
     *
     * @param defaultSelection
     * @throws Exception
     */
    public void setDefaultSelection(int defaultSelection) throws Exception {
        if (defaultSelection > (itemMap.size() - 1)) {
            throw new Exception("Default selection cannot be greater than the number of items");
        } else {
            this.defaultSelection = defaultSelection;
            update();
        }
    }

    /**
     * Sets the colors used when drawing the view. The primary color will be used for selected color
     * and unselected text color, while the secondary color will be used for unselected color
     * and selected text color.
     *
     * @param primaryColor
     * @param secondaryColor
     */
    public void setColors(int primaryColor, int secondaryColor) {
        this.selectedColor = primaryColor;
        this.selectedTextColor = secondaryColor;
        this.unselectedColor = secondaryColor;
        this.unselectedTextColor = primaryColor;

        //Set text selectedColor state list
        textColorStateList = new ColorStateList(new int[][]{
                {-android.R.attr.state_checked}, {android.R.attr.state_checked}},
                new int[]{unselectedTextColor, selectedTextColor}
        );

        update();
    }

    /**
     * Sets the colors used when drawing the view
     *
     * @param selectedColor
     * @param selectedTextColor
     * @param unselectedColor
     * @param unselectedTextColor
     */
    public void setColors(int selectedColor, int selectedTextColor, int unselectedColor, int unselectedTextColor) {
        this.selectedColor = selectedColor;
        this.selectedTextColor = selectedTextColor;
        this.unselectedColor = unselectedColor;
        this.unselectedTextColor = unselectedTextColor;

        //Set text selectedColor state list
        textColorStateList = new ColorStateList(new int[][]{
                {-android.R.attr.state_checked}, {android.R.attr.state_checked}},
                new int[]{unselectedTextColor, selectedTextColor}
        );

        update();
    }

    /**
     * Used to set the selected value based on the value (not the visible text) provided in the
     * value array provided via xml or code
     *
     * @param value
     */
    public void setByValue(String value) {
        String buttonText = "";
        if (this.itemMap.containsValue(value)) {
            for (String entry : itemMap.keySet()) {
                if (itemMap.get(entry).equalsIgnoreCase(value)) {
                    buttonText = entry;
                }
            }
        }

        for (RadioButton option : options) {
            if (option.getText().toString().equalsIgnoreCase(buttonText)) {
                this.check(option.getId());
            }
        }

    }

    /**
     * Sets the mListener that gets called when a selection is changed
     *
     * @param listener
     */
    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.mListener = listener;
    }

    /**
     * For use with multiple views. This identifier will be provided in the
     * onSelectionChanged mListener
     *
     * @param identifier
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Set to true if you want each segment to be equal width
     *
     * @param equalWidth
     */
    public void setEqualWidth(boolean equalWidth) {
        this.equalWidth = equalWidth;
        update();
    }

    /**
     * Set to true if the view should be stretched to fill it's parent view
     *
     * @param stretch
     */
    public void setStretch(boolean stretch) {
        this.stretch = stretch;
        update();
    }

}
