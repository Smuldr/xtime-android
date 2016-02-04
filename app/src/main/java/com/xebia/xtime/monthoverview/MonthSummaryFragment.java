package com.xebia.xtime.monthoverview;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
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

    private View footerView;
    private FloatingActionButton approveButton;
    private View summaryView;
    private View progressBar;

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
        footerView.setVisibility(null != mRows && mRows.size() > 0 ? View.VISIBLE : View.GONE);
        if (null != mOverview) {
            double grandTotal = TimeSheetUtils.getGrandTotalHours(mOverview);
            String grandTotalTxt = NumberFormat.getNumberInstance().format(grandTotal);
            TextView grandTotalView = (TextView) footerView.findViewById(R.id.grand_total);
            grandTotalView.setText(getString(R.string.hours_label, grandTotalTxt));
        }
    }

    private void showApproveButton() {
        // only show options menu if the monthly data is not approved yet
        approveButton.setVisibility(null != mOverview && !mOverview.isMonthlyDataApproved() ?
                View.VISIBLE : View.GONE);
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
        new ApproveTask(getActivity(), this).execute(grandTotal, (double) mMonth.getTime());
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
        footerView = inflater.inflate(R.layout.row_grand_total, null, false);
        View rootView = inflater.inflate(R.layout.fragment_month_summary, container, false);
        approveButton = (FloatingActionButton) rootView.findViewById(R.id.fab);
        approveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onApproveClick();
            }
        });
        summaryView = rootView.findViewById(R.id.summary);
        progressBar = rootView.findViewById(R.id.progressBar);
        return rootView;
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

        getListView().addFooterView(footerView);

        // start loading the month overview
        showProgressIndicator(true);
        getLoaderManager().initLoader(0, null, this);
    }

    private void showProgressIndicator(final boolean show) {
        summaryView.setVisibility(show ? View.GONE : View.VISIBLE);
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public Loader<XTimeOverview> onCreateLoader(int id, Bundle args) {
        return new MonthOverviewLoader(getActivity(), mMonth);
    }

    @Override
    public void onLoadFinished(Loader<XTimeOverview> monthOverviewLoader,
                               XTimeOverview XTimeOverview) {
        mOverview = XTimeOverview;
        showProgressIndicator(false);
        showList();
    }

    @Override
    public void onLoaderReset(Loader<XTimeOverview> weekOverviewLoader) {
        // nothing to do
    }
}
