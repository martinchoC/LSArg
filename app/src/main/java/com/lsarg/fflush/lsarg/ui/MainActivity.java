package com.lsarg.fflush.lsarg.ui;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lsarg.fflush.lsarg.PinnedHeaderAdapter;
import com.lsarg.fflush.lsarg.R;
import com.lsarg.fflush.lsarg.Seña;

import org.json.JSONArray;
import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

// Activity that display customized list view and search box
public class MainActivity extends Activity {

	private ArrayList<String> words;
	private JSONArray array;

	// unsorted list items
	ArrayList<String> mItems;

	// array list to store section positions
	ArrayList<Integer> mListSectionPos;

	// array list to store listView data
	ArrayList<String> mListItems;

	// custom adapter
	PinnedHeaderAdapter mAdaptor;

	// search box
	@BindView(R.id.search_view) EditText mSearchView;
	// loading view
	@BindView(R.id.loading_view) ProgressBar mLoadingView;
	// custom list view with pinned header
	@BindView(R.id.list_view) PinnedHeaderListView mListView;
	// empty view
	@BindView(R.id.empty_view) TextView mEmptyView;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// UI elements
		setupViews();

		listWords();

		mItems = new ArrayList<String>();
		mItems = words;//(Arrays.asList(ITEMS));
		mListSectionPos = new ArrayList<Integer>();
		mListItems = new ArrayList<String>();

		// for handling configuration change
		if (savedInstanceState != null) {
			mListItems = savedInstanceState.getStringArrayList("mListItems");
			mListSectionPos = savedInstanceState.getIntegerArrayList("mListSectionPos");

			if (mListItems != null && mListItems.size() > 0 && mListSectionPos != null && mListSectionPos.size() > 0) {
				setListAdaptor();
			}

			String constraint = savedInstanceState.getString("constraint");
			if (constraint != null && constraint.length() > 0) {
				mSearchView.setText(constraint);
				setIndexBarViewVisibility(constraint);
			}
		} else {
			new Poplulate().execute(mItems);
		}
	}

	private void setupViews() {
		setContentView(R.layout.main_act);
		ButterKnife.bind(this);

	}

	private void listWords(){
		words = new ArrayList<>();
		BufferedReader br = null;

		try{
			br = new BufferedReader(new InputStreamReader(getAssets().open("senias")));
			System.out.println("Archivo vacio? "+br.equals(null));

			String json = org.apache.commons.io.IOUtils.toString(br);

			array = new JSONArray(json);
			Log.d("LENGTH","Largo del arreglo: "+array.length());
			String palabra="";
			for(int i=0; i<array.length(); i++){
				palabra = array.getJSONObject(i).getString("nombre");
				words.add(palabra);
			}
		}
		catch (FileNotFoundException e){System.out.println("No se pudo obtener el archivo 1");}
		catch (IOException e){System.out.println("No se pudo obtener el archivo 2");}
		catch(JSONException e){ System.out.println("No se pudo obtener el archivo 3");}

	}

	// I encountered an interesting problem with a TextWatcher listening for
	// changes in an EditText.
	// The afterTextChanged method was called, each time, the device orientation
	// changed.
	// An answer on Stackoverflow let me understand what was happening: Android
	// recreates the activity, and
	// the automatic restoration of the state of the input fields, is happening
	// after onCreate had finished,
	// where the TextWatcher was added as a TextChangedListener.The solution to
	// the problem consisted in adding
	// the TextWatcher in onPostCreate, which is called after restoration has
	// taken place
	//
	// http://stackoverflow.com/questions/6028218/android-retain-callback-state-after-configuration-change/6029070#6029070
	// http://stackoverflow.com/questions/5151095/textwatcher-called-even-if-text-is-set-before-adding-the-watcher
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		mSearchView.addTextChangedListener(filterTextWatcher);
		super.onPostCreate(savedInstanceState);
	}

	private void setListAdaptor() {
		// create instance of PinnedHeaderAdapter and set adapter to list view
		mAdaptor = new PinnedHeaderAdapter(this, mListItems, mListSectionPos);
		mListView.setAdapter(mAdaptor);

		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		// set header view
		View pinnedHeaderView = inflater.inflate(R.layout.section_row_view, mListView, false);
		mListView.setPinnedHeaderView(pinnedHeaderView);

		// set index bar view
		IndexBarView indexBarView = (IndexBarView) inflater.inflate(R.layout.index_bar_view, mListView, false);
		indexBarView.setData(mListView, mListItems, mListSectionPos);
		mListView.setIndexBarView(indexBarView);

		// set preview text view
		View previewTextView = inflater.inflate(R.layout.preview_view, mListView, false);
		mListView.setPreviewView(previewTextView);

		// for configure pinned header view on scroll change
		mListView.setOnScrollListener(mAdaptor);
	}

	private TextWatcher filterTextWatcher = new TextWatcher() {
		public void afterTextChanged(Editable s) {
			String str = s.toString();
			if (mAdaptor != null && str != null)
				mAdaptor.getFilter().filter(str);
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}
	};

	public class ListFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			// NOTE: this function is *always* called from a background thread,
			// and
			// not the UI thread.
			String constraintStr = constraint.toString().toLowerCase(Locale.getDefault());
			FilterResults result = new FilterResults();

			if (constraint != null && constraint.toString().length() > 0) {
				ArrayList<String> filterItems = new ArrayList<String>();

				synchronized (this) {
					for (String item : mItems) {
						if (item.toLowerCase(Locale.getDefault()).startsWith(constraintStr)) {
							filterItems.add(item);
						}
					}
					result.count = filterItems.size();
					result.values = filterItems;
				}
			} else {
				synchronized (this) {
					result.count = mItems.size();
					result.values = mItems;
				}
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			ArrayList<String> filtered = (ArrayList<String>) results.values;
			setIndexBarViewVisibility(constraint.toString());
			// sort array and extract sections in background Thread
			new Poplulate().execute(filtered);
		}

	}

	private void setIndexBarViewVisibility(String constraint) {
		// hide index bar for search results
		if (constraint != null && constraint.length() > 0) {
			mListView.setIndexBarVisibility(false);
		} else {
			mListView.setIndexBarVisibility(true);
		}
	}

	// sort array and extract sections in background Thread here we use
	// AsyncTask
	private class Poplulate extends AsyncTask<ArrayList<String>, Void, Void> {

		private void showLoading(View contentView, View loadingView, View emptyView) {
			contentView.setVisibility(View.GONE);
			loadingView.setVisibility(View.VISIBLE);
			emptyView.setVisibility(View.GONE);
		}

		private void showContent(View contentView, View loadingView, View emptyView) {
			contentView.setVisibility(View.VISIBLE);
			loadingView.setVisibility(View.GONE);
			emptyView.setVisibility(View.GONE);
		}

		private void showEmptyText(View contentView, View loadingView, View emptyView) {
			contentView.setVisibility(View.GONE);
			loadingView.setVisibility(View.GONE);
			emptyView.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPreExecute() {
			// show loading indicator
			showLoading(mListView, mLoadingView, mEmptyView);
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(ArrayList<String>... params) {
			mListItems.clear();
			mListSectionPos.clear();
			ArrayList<String> items = params[0];
			if (mItems.size() > 0) {

				// NOT forget to sort array
				Collections.sort(items, new SortIgnoreCase());

				String prev_section = "";
				for (String current_item : items) {
					String current_section = current_item.substring(0, 1).toUpperCase(Locale.getDefault());

					if (!prev_section.equals(current_section)) {
						mListItems.add(current_section);
						mListItems.add(current_item);
						// array list of section positions
						mListSectionPos.add(mListItems.indexOf(current_section));
						prev_section = current_section;
					} else {
						mListItems.add(current_item);
					}
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (!isCancelled()) {
				if (mListItems.size() <= 0) {
					showEmptyText(mListView, mLoadingView, mEmptyView);
				} else {
					setListAdaptor();
					showContent(mListView, mLoadingView, mEmptyView);
				}
			}
			super.onPostExecute(result);
		}
	}

	public class SortIgnoreCase implements Comparator<String> {
		public int compare(String s1, String s2) {
			return s1.compareToIgnoreCase(s2);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (mListItems != null && mListItems.size() > 0) {
			outState.putStringArrayList("mListItems", mListItems);
		}
		if (mListSectionPos != null && mListSectionPos.size() > 0) {
			outState.putIntegerArrayList("mListSectionPos", mListSectionPos);
		}
		String searchText = mSearchView.getText().toString();
		if (searchText != null && searchText.length() > 0) {
			outState.putString("constraint", searchText);
		}
		super.onSaveInstanceState(outState);
	}

	@OnItemClick(R.id.list_view) void onItemClick(int position) {
		String palabra = mListView.getItemAtPosition(position).toString();
		Seña toSee = findWord(palabra);

		Intent i = new Intent(this, WordActivity.class);
		i.putExtra("nombre", toSee.getNombre());
		i.putExtra("configuracion", toSee.getConfiguracion());
		i.putExtra("foto1", toSee.getFoto1());
		i.putExtra("foto2", toSee.getFoto2());
		i.putExtra("foto3", toSee.getFoto3());
		i.putExtra("foto4", toSee.getFoto4());
		i.putExtra("movimiento", toSee.getMovimiento());
		i.putExtra("observacion", toSee.getObservacion());
		i.putExtra("rasgosNoManuales", toSee.getRasgosNoManuales());
		i.putExtra("videoFrente", toSee.getVideoFrente());
		i.putExtra("videoPerfil", toSee.getVideoPerfil());
		i.putExtra("orientacion", toSee.getOrientacion());
		i.putExtra("ubicacion", toSee.getUbicacion());
		startActivity(i);

		Log.d("seleccionado","Palabra seleccionada: "+toSee.getNombre());

	}

	private Seña findWord(String word){
		boolean found = false;
		int start = 0;
		while(start<array.length() && found == false){
			try {
				if (array.getJSONObject(start).getString("nombre") == word){
					Seña seña = new Seña(array.getJSONObject(start).getString("nombre"));
					seña.setNombre(array.getJSONObject(start).getString("nombre"));
					seña.setConfiguracion(array.getJSONObject(start).getString("configuracion"));
					seña.setFoto1(array.getJSONObject(start).getString("foto1"));
					seña.setFoto2(array.getJSONObject(start).getString("foto2"));
					seña.setFoto3(array.getJSONObject(start).getString("foto3"));
					seña.setFoto4(array.getJSONObject(start).getString("foto4"));
					seña.setMovimiento(array.getJSONObject(start).getString("movimiento"));
					seña.setObservacion(array.getJSONObject(start).getString("observacion"));
					seña.setRasgosNoManuales(array.getJSONObject(start).getString("rasgosNoManuales"));
					seña.setVideoFrente(array.getJSONObject(start).getString("videoFrente"));
					seña.setVideoPerfil(array.getJSONObject(start).getString("videoPerfil"));
					seña.setOrientacion(array.getJSONObject(start).getString("orientacion"));
					seña.setUbicacion(array.getJSONObject(start).getString("ubicacion"));
					found = true;
					return seña;
				}
			}
			catch (JSONException e){}
			start++;
		}
		return null;
	}
}
