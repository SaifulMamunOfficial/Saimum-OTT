package nemosofts.tamilaudiopro.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class WidthFitSquareLayout extends FrameLayout {
    private boolean forceSquare = true;

    public WidthFitSquareLayout(Context context) {
        super(context);
    }

    public WidthFitSquareLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public WidthFitSquareLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public void forceSquare(boolean z) {
        this.forceSquare = z;
        requestLayout();
    }

    @Override
    protected void onMeasure(int i, int i2) {
        if (this.forceSquare) {
            i2 = i;
        }
        super.onMeasure(i, i2);
    }
}
