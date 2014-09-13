package com.xebia.xtime.monthoverview;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.xebia.xtime.R;
import com.xebia.xtime.monthoverview.approve.ApproveConfirmDialog;
import com.xebia.xtime.monthoverview.approve.ApproveTask;
import com.xebia.xtime.monthoverview.loader.MonthOverviewLoader;
import com.xebia.xtime.shared.TimeSheetUtils;
import com.xebia.xtime.shared.model.TimeSheetRow;
import com.xebia.xtime.shared.model.XTimeOverview;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MonthSummaryFragment extends ListFragment implements LoaderManager
        .LoaderCallbacks<XTimeOverview>, ApproveTask.Listener, ApproveConfirmDialog.Listener {

    private static final String ARG_MONTH = "month";
    private XTimeOverview mOverview;
    private List<TimeSheetRow> mRows;
    private Date mMonth;
    private View mFooterView;

    public MonthSummaryFragment() {
        // Required empty public constructor
    }

    /**
     * @param month Date indicating the month to display
     * @return A new instance of fragment MonthSummaryFragment
     */
    public static MonthSummaryFragment newInstance(Date month) {
        MonthSummaryFragment fragment = new MonthSummaryFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_MONTH, month.getTime());
        fragment.setArguments(args);
        return fragment;
    }

    private void showList() {
        if (null == mRows) {
            mRows = new ArrayList<>();
            setListAdapter(new TimeSheetRowAdapter(getActivity(), mRows));
        } else {
            mRows.clear();
        }

        if (null != mOverview && null != mOverview.getTimeSheetRows()) {
            mRows.addAll(mOverview.getTimeSheetRows());
        }
        showGrandTotal();
        showApproveButton();

        ((ArrayAdapter) getListAdapter()).notifyDataSetChanged();
    }

    private void showGrandTotal() {
        mFooterView.setVisibility(null != mRows && mRows.size() > 0 ? View.VISIBLE : View.GONE);
        if (null != mOverview) {
            double grandTotal = TimeSheetUtils.getGrandTotalHours(mOverview);
            ((TextView) mFooterView.findViewById(R.id.grand_total)).setText(NumberFormat
                    .getNumberInstance().format(grandTotal));
        }
    }

    private void showApproveButton() {
        // only show options menu if the monthly data is not approved yet
        setHasOptionsMenu(null != mOverview && !mOverview.isMonthlyDataApproved());
    }

    private void onApproveClick() {
        ApproveConfirmDialog dialog = new ApproveConfirmDialog();
        dialog.setListener(this);
        dialog.show(getFragmentManager(), null);
    }

    @Override
    public void onApproveConfirmed() {
        setListShown(false);
        double grandTotal = TimeSheetUtils.getGrandTotalHours(mOverview);
        new ApproveTask(this).execute(grandTotal, (double) mMonth.getTime());
    }

    @Override
    public void onApproveComplete(Boolean result) {
        if (null != result && result) {
            Toast.makeText(getActivity(), R.string.toast_approve_success, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getActivity(), R.string.toast_approve_failure, Toast.LENGTH_LONG).show();
        }
        setListShown(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mFooterView = inflater.inflate(R.layout.row_grand_total, null, false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getArguments() != null) {
            mMonth = new Date(getArguments().getLong(ARG_MONTH, -1));
        }
        if (mMonth.getTime() < 0) {
            throw new NullPointerException("Missing ARG_MONTH argument");
        }

        getListView().addFooterView(mFooterView);

        setEmptyText(getText(R.string.empty_month_summary));

        // start loading the month overview
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.month_summary, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.approve) {
            onApproveClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<XTimeOverview> onCreateLoader(int id, Bundle args) {
        return new MonthOverviewLoader(getActivity(), mMonth);
    }

    @Override
    public void onLoadFinished(Loader<XTimeOverview> weekOverviewLoader,
                               XTimeOverview XTimeOverview) {
        mOverview = XTimeOverview;
        showList();
    }

    @Override
    public void onLoaderReset(Loader<XTimeOverview> weekOverviewLoader) {
        // nothing to do
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // make sure the options menu does not keep showing the 'approve' item
        setHasOptionsMenu(false);
    }
}
