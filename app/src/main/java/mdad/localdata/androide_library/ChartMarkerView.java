package mdad.localdata.androide_library;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

// Used to display tooltip when graph data point is clicked
public class ChartMarkerView extends MarkerView {

    private final TextView tvContent;
    private MPPointF mOffset;

    public ChartMarkerView(Context context) {
        super(context, R.layout.marker_view);
        tvContent = findViewById(R.id.tvContent);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        // Update the marker content
        tvContent.setText(String.format("Books: %.0f", e.getY()));
        super.refreshContent(e, highlight);
    }

    // Text offset
    @Override
    public MPPointF getOffset() {
        if (mOffset == null) {
            mOffset = new MPPointF(-(getWidth() / 2f), -getHeight());
        }
        return mOffset;
    }
}
