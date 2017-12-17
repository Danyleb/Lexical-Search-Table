package com.cleveroad.sample.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cleveroad.adaptivetablelayout.AdaptiveTableLayout;
import com.cleveroad.adaptivetablelayout.LinkedAdaptiveTableAdapter;
import com.cleveroad.adaptivetablelayout.OnItemClickListener;
import com.cleveroad.adaptivetablelayout.OnItemLongClickListener;
import com.cleveroad.sample.R;
import com.cleveroad.sample.adapter.IntColor;
import com.cleveroad.sample.adapter.SampleLinkedTableAdapter;
import com.cleveroad.sample.datasource.CsvFileDataSourceImpl;
import com.cleveroad.sample.datasource.UpdateFileCallback;
import com.cleveroad.sample.ui.dialogs.EditItemDialog;
import com.cleveroad.sample.ui.dialogs.SettingsDialog;

import com.cleveroad.sample.utils.PermissionHelper;

import com.cleveroad.sample.utils.Word;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TableLayoutFragment
        extends Fragment
        implements OnItemClickListener, OnItemLongClickListener, UpdateFileCallback {
    private static final String TAG = TableLayoutFragment.class.getSimpleName();
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1132;
    private static final String EXTRA_CSV_FILE = "EXTRA_CSV_FILE";
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    static HashMap<String, Integer> colors = new HashMap<String, Integer>();

    private Uri mCsvFile;
    MainLexical mainLexical;
    private LinkedAdaptiveTableAdapter mTableAdapter;
    private CsvFileDataSourceImpl mCsvFileDataSource;
    private AdaptiveTableLayout mTableLayout;
    private ProgressBar progressBar;
    private View vHandler;
    private Snackbar mSnackbar;
    int i;
    int j;
    int y;
    List<Word> words2;
    List wo = new ArrayList();


    public static TableLayoutFragment newInstance(@NonNull String filename) {
        Bundle args = new Bundle();
        args.putString(EXTRA_CSV_FILE, filename);

        TableLayoutFragment fragment = new TableLayoutFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mCsvFile = Uri.parse(getArguments().getString(EXTRA_CSV_FILE));
        mCsvFileDataSource = new CsvFileDataSourceImpl(getContext(), mCsvFile);

        int g;
        int h;
        for (g = 0; g < mCsvFileDataSource.getRowsCount(); g++) {
            for (h = 0; h < mCsvFileDataSource.getColumnsCount(); h++) {
                String itemSource;
                if (mCsvFileDataSource.getItemData(g, h).endsWith("jpg") ||mCsvFileDataSource.getItemData(g, h).endsWith("LULU")) {
                   itemSource = mCsvFileDataSource.getItemData(g, h).replace(mCsvFileDataSource.getItemData(g, h), "x");
                } else {
                    itemSource = mCsvFileDataSource.getItemData(g, h);
                }
                    String[] textParts1 = itemSource.split(" ");


                    int t;

                    for (t = 0; t < textParts1.length; t++) {


                        colors.put(textParts1[t], 1);

                    }

                    Log.d("ColorsInitial", colors.toString());


            }
        }

       Set key = colors.keySet();
        wo.addAll(key);


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_tab_layout, container, false);

        mTableLayout = (AdaptiveTableLayout) view.findViewById(R.id.tableLayout);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        vHandler = view.findViewById(R.id.vHandler);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        toolbar.inflateMenu(R.menu.table_layout);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.actionSave) {
                    applyChanges();
                } else if (item.getItemId() == R.id.actionSettings) {
                    SettingsDialog.newInstance(
                            mTableLayout.isHeaderFixed(),
                            mTableLayout.isSolidRowHeader(),
                            mTableLayout.isRTL(),
                            mTableLayout.isDragAndDropEnabled())
                            .show(getChildFragmentManager(), SettingsDialog.class.getSimpleName());
                }
                return true;
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mTableLayout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

            initAdapter();



        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSnackbar = Snackbar.make(view, R.string.changes_saved, Snackbar.LENGTH_INDEFINITE);
        TextView tv = (TextView) mSnackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        tv.setMaxLines(3);
        mSnackbar.setAction("Close", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSnackbar.dismiss();
            }
        });
    }

    private void applyChanges() {
        if (PermissionHelper.checkOrRequest(
                getActivity(),
                REQUEST_EXTERNAL_STORAGE,
                PERMISSIONS_STORAGE)) {
            showProgress();
            mCsvFileDataSource.applyChanges(
                    getLoaderManager(),
                    mTableLayout.getLinkedAdapterRowsModifications(),
                    mTableLayout.getLinkedAdapterColumnsModifications(),
                    mTableLayout.isSolidRowHeader(),
                    TableLayoutFragment.this);

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EditItemDialog.REQUEST_CODE_EDIT_SONG && resultCode == Activity.RESULT_OK && data != null) {
            int columnIndex = data.getIntExtra(EditItemDialog.EXTRA_COLUMN_NUMBER, 0);
            int rowIndex = data.getIntExtra(EditItemDialog.EXTRA_ROW_NUMBER, 0);
            String value = data.getStringExtra(EditItemDialog.EXTRA_VALUE);
            mCsvFileDataSource.updateItem(rowIndex, columnIndex, value);
            mTableAdapter.notifyItemChanged(rowIndex, columnIndex);
        } else if (requestCode == SettingsDialog.REQUEST_CODE_SETTINGS && resultCode == Activity.RESULT_OK && data != null) {
            mTableLayout.setHeaderFixed(data.getBooleanExtra(SettingsDialog.EXTRA_VALUE_HEADER_FIXED, mTableLayout.isHeaderFixed()));
            mTableLayout.setSolidRowHeader(data.getBooleanExtra(SettingsDialog.EXTRA_VALUE_SOLID_HEADER, mTableLayout.isSolidRowHeader()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mTableLayout.setLayoutDirection(
                        data.getBooleanExtra(SettingsDialog.EXTRA_VALUE_RTL_DIRECTION, mTableLayout.isRTL())
                                ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
            }
        } else if (requestCode == 1 && resultCode == 2 && data != null) {
            try {
                dothejob();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mTableLayout.setDragAndDropEnabled(data.getBooleanExtra(
                SettingsDialog.EXTRA_VALUE_DRAG_AND_DROP_ENABLED, mTableLayout.isDragAndDropEnabled()));
        mTableAdapter.setRtl(mTableLayout.isRTL());
        mTableAdapter.notifyDataSetChanged();
    }


    public void dothejob() throws InterruptedException {

Thread t = new Thread() {
    @Override
    public void run() {
        Looper.prepare();

        for (j = 0; j <= wo.size() - 1; j++) {
            Random rand = new Random();

            int r = rand.nextInt(255);
            int g = rand.nextInt(255);
            int b = rand.nextInt(255);
            int color = new IntColor(r, g, b).getcolor();
            Log.d("XOOOO", wo.toString());
            Log.d("XOOOO SIZE", String.valueOf(wo.size()));


            mainLexical = new MainLexical(wo.get(j).toString(), color, j);
            mainLexical.doSomeWork();


        }


    }

};
            t.start();

    }

    //------------------------------------- adapter callbacks --------------------------------------
    @Override
    public void onItemClick(int row, int column) {
        EditItemDialog.newInstance(
                row,
                column,
                mCsvFileDataSource.getColumnHeaderData(column),
                mCsvFileDataSource.getItemData(row, column))
                .show(getChildFragmentManager(), EditItemDialog.class.getSimpleName());
    }

    @Override
    public void onRowHeaderClick(int row) {
        EditItemDialog.newInstance(
                row,
                0,
                mCsvFileDataSource.getColumnHeaderData(0),
                mCsvFileDataSource.getItemData(row, 0))
                .show(getChildFragmentManager(), EditItemDialog.class.getSimpleName());
    }

    @Override
    public void onColumnHeaderClick(int column) {
        EditItemDialog.newInstance(
                0,
                column,
                mCsvFileDataSource.getColumnHeaderData(column),
                mCsvFileDataSource.getItemData(0, column))
                .show(getChildFragmentManager(), EditItemDialog.class.getSimpleName());
    }

    @Override
    public void onLeftTopHeaderClick() {
        // implement in next version
    }

    @Override
    public void onItemLongClick(int row, int column) {
        // implement in next version
    }


    @Override
    public void onLeftTopHeaderLongClick() {
        // implement in next version
    }

    @Override
    public void onFileUpdated(final String filePath, boolean isSuccess) {
        hideProgress();
        View view = getView();
        if (view == null) {
            return;
        }

        if (isSuccess) { //if data source have been changed

            Log.e("Done", "File path = " + filePath);

            mCsvFile = Uri.parse(filePath);
            mCsvFileDataSource = new CsvFileDataSourceImpl(getContext(), mCsvFile);

            initAdapter();
            if (mSnackbar != null) {
                if (mSnackbar.isShown()) {
                    mSnackbar.dismiss();
                }
                String text = getString(R.string.changes_saved) + " path = " + filePath;
                mSnackbar.setText(text);
                mSnackbar.show();
            }

        } else {
            Snackbar.make(view, R.string.unexpected_error, Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    private void initAdapter() {

        mTableAdapter = new SampleLinkedTableAdapter(getContext(), mCsvFileDataSource, colors);
        mTableAdapter.setOnItemClickListener(this);
        mTableAdapter.setOnItemLongClickListener(this);

        mTableLayout.setAdapter(mTableAdapter);
    }

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
        vHandler.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
        vHandler.setVisibility(View.GONE);
    }

    public static List<Word> getLexicalField(String value) {


        try

        {
            Document document = Jsoup.connect("http://www.rimessolides.com/motscles.aspx?m=" + value).get();
            Log.d("LEXICALSEARCH", value);
            ArrayList<Word> wordList = new ArrayList<>();

            Word word;


            Elements elements = document.getElementsByClass("MotCle");

            for (Element e : elements) {

                word = new Word();

                // On récupère l'image

                word.setWord(e.getElementsByTag("a").first().text());
                wordList.add(word);

            }
            Log.d("NetList", wordList.get(10).getWord());
            return wordList;

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Error", e.getMessage());
        }

        return null;
    }

    public class MainLexical extends AppCompatActivity {


        private static final String TAG = "";
        String value;

        int colo;
        int b;
        int c;



        public MainLexical(String value, int colo, int o) {
            this.value = value;
            this.b = o;
            this.colo = colo;
        }

        public void doSomeWork() {
            getObservable()
                    // Run on a background thread
                    .subscribeOn(Schedulers.io())
                    // Be notified on the main thread
                    .observeOn(AndroidSchedulers.mainThread())

                    .subscribe(getObserver());
        }


        private Observable<List<Word>> getObservable() {
            return Observable.create(new ObservableOnSubscribe<List<Word>>() {
                @Override
                public void subscribe(ObservableEmitter<List<Word>> e) throws Exception {
                    if (!e.isDisposed()) {
                        e.onNext(getLexicalField(value));
                        e.onComplete();
                    }
                }
            });
        }

        private Observer<List<Word>> getObserver() {
            return new Observer<List<Word>>() {

                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onNext(List<Word> wordList) {

                    int y;

                    for (Word word : wordList) {


                            for (y = 0; y <= wo.size()-1; y++) {
                                String compare = wo.get(y).toString();

                                    if (compare.equalsIgnoreCase(word.getWord())) {
                                        //colors.remove(mCsvFileDataSource.getItemData(f,y));
                                        colors.put(wo.get(y).toString(), colo);
                                        //colors.remove(mCsvFileDataSource.getItemData(b,c));
                                        colors.put(wo.get(b).toString(), colo);

                                        mTableAdapter.notifyDataSetChanged();
                                }
                            }


                    }
                    Log.d(TAG, " " + wordList.size());
                }

                @Override
                public void onError(Throwable e) {

                    Log.d(TAG, " onError : " + e.getMessage());
                }

                @Override
                public void onComplete() {
                    Log.d("COLORS", colors.toString());
                    Log.d(TAG, " onComplete");

                }
            };


        }
    }


    void something() {
        //@Override
       // public void run(){
            Looper.prepare();
            for (i = 0; i <= 5; i++) {
                for (j = 1; j <= 5; j++) {
                    Random rand = new Random();

                    int r = rand.nextInt(255);
                    int g = rand.nextInt(255);
                    int b = rand.nextInt(255);
                    int color = new IntColor(r, g, b).getcolor();
                    String principal = mCsvFileDataSource.getItemData(i, j);
                    String[] textParts = principal.split(" ");
                    Log.d("textPart from Fragment:", textParts[0]);

                    int t;

                    for (t = 0; t < textParts.length; t++) {
                        Log.d("textPart from Adapter :", textParts[t]);
                        mainLexical = new MainLexical(textParts[t], color, i);
                        mainLexical.doSomeWork();
                    }

                }
            }


    }

}
