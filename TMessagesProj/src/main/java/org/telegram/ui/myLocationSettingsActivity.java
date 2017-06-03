/*
 * This is the source code of Telegram for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.telegram.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.android.AndroidUtilities;
import org.telegram.android.ContactsController;
import org.telegram.android.LocaleController;
import org.telegram.android.MediaController;
import org.telegram.android.MessagesController;
import org.telegram.android.NotificationCenter;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ConnectionsManager;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.messenger.RPCRequest;
import org.telegram.messenger.TLObject;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.HTTPHandler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static org.telegram.android.volley.VolleyLog.TAG;

public class myLocationSettingsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private ListAdapter listAdapter;

    private int placesSectionRow;
    private int homeRow;
    private int workRow;
    private int entertainmentRow;
    private int weekSectionRow;
    private int rowCount;
    String lat;
    String lon;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        ContactsController.getInstance().loadPrivacySettings();

        rowCount = 0;
        placesSectionRow = rowCount++;
        homeRow = rowCount++;
        workRow = rowCount++;
        entertainmentRow = rowCount++;
        weekSectionRow = rowCount++;


        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    @Override
    public View createView(LayoutInflater inflater) {
        if (fragmentView == null) {
            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            actionBar.setAllowOverlayTitle(true);
            actionBar.setTitle("My Places");
            actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
                @Override
                public void onItemClick(int id) {
                    if (id == -1) {
                        finishFragment();
                    }
                }
            });

            listAdapter = new ListAdapter(getParentActivity());

            fragmentView = new FrameLayout(getParentActivity());
            FrameLayout frameLayout = (FrameLayout) fragmentView;
            frameLayout.setBackgroundColor(0xfff0f0f0);

            ListView listView = new ListView(getParentActivity());
            listView.setDivider(null);
            listView.setDividerHeight(0);
            listView.setVerticalScrollBarEnabled(false);
            listView.setDrawSelectorOnTop(true);
            frameLayout.addView(listView);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) listView.getLayoutParams();
            layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
            layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
            layoutParams.gravity = Gravity.TOP;
            listView.setLayoutParams(layoutParams);
            listView.setAdapter(listAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                    if(i == homeRow){
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle("Home");
                        final EditText input = new EditText(getParentActivity());
                        input.setHint("Insert city");
                        builder.setView(input);
                        builder.setPositiveButton("confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(input.getText().toString().trim().length() > 0){
                                    String city = input.getText().toString().trim();
                                    try {
                                        Bundle locationHome = new getCoordinates()
                                                .execute(city)
                                                .get();
                                        String latHome = locationHome.getString("lat");
                                        String lonHome = locationHome.getString("lon");
                                        SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("latHome",latHome);
                                        editor.putString("longHome",lonHome);
                                        editor.commit();
                                        Log.i(TAG,"fine! " + lonHome);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    }
                                } else{
                                    Toast.makeText(getParentActivity(),"That is empty :(", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                        builder.setNegativeButton("Why?", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getParentActivity(),"To website", Toast.LENGTH_SHORT).show();
                            }
                        });
                        showAlertDialog(builder);
                    } else if(i == workRow){
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle("Work");
                        final EditText input = new EditText(getParentActivity());
                        input.setHint("Insert city");
                        builder.setView(input);
                        builder.setPositiveButton("confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(input.getText().toString().trim().length() > 0){
                                    String city = input.getText().toString().trim();
                                    try {
                                        Bundle locationWork = new getCoordinates()
                                                .execute(city)
                                                .get();
                                        String latWork = locationWork.getString("lat");
                                        String lonWork = locationWork.getString("lon");
                                        SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("latWork",latWork);
                                        editor.putString("longWork",lonWork);
                                        editor.commit();
                                        Log.i(TAG,"fine! " + latWork);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    }
                                } else{
                                    Toast.makeText(getParentActivity(),"That is empty :(", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                        builder.setNegativeButton("Why?", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getParentActivity(),"To website", Toast.LENGTH_SHORT).show();
                            }
                        });
                        showAlertDialog(builder);
                    } else if (i == entertainmentRow){
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle("Entertainment");
                        final EditText input = new EditText(getParentActivity());
                        input.setHint("Insert city");
                        builder.setView(input);
                        builder.setPositiveButton("confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(input.getText().toString().trim().length() > 0){
                                    String city = input.getText().toString().trim();
                                    try {
                                        Bundle locationEntertainment = new getCoordinates()
                                                .execute(city)
                                                .get();
                                        String latEntertainment = locationEntertainment.getString("lat");
                                        String lonEntertainment = locationEntertainment.getString("lon");
                                        SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("latEntertainment",latEntertainment);
                                        editor.putString("longEntertainment",lonEntertainment);
                                        editor.commit();
                                        Log.i(TAG,"fine! " + latEntertainment);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    }
                                } else{
                                    Toast.makeText(getParentActivity(),"That is empty :(", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                        builder.setNegativeButton("Why?", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getParentActivity(),"To website", Toast.LENGTH_SHORT).show();
                            }
                        });
                        showAlertDialog(builder);
                    }
                    //TODO find a solution for commuteRow as well!!
                }
            });
        } else {
            ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }
        return fragmentView;
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.privacyRulesUpdated) {
            if (listAdapter != null) {
                listAdapter.notifyDataSetChanged();
            }
        }
    }

    private String formatRulesString() {
        ArrayList<TLRPC.PrivacyRule> privacyRules = ContactsController.getInstance().getPrivacyRules();
        if (privacyRules.size() == 0) {
            return LocaleController.getString("LastSeenNobody", R.string.LastSeenNobody);
        }
        int type = -1;
        int plus = 0;
        int minus = 0;
        for (TLRPC.PrivacyRule rule : privacyRules) {
            if (rule instanceof TLRPC.TL_privacyValueAllowUsers) {
                plus += rule.users.size();
            } else if (rule instanceof TLRPC.TL_privacyValueDisallowUsers) {
                minus += rule.users.size();
            } else if (rule instanceof TLRPC.TL_privacyValueAllowAll) {
                type = 0;
            } else if (rule instanceof TLRPC.TL_privacyValueDisallowAll) {
                type = 1;
            } else {
                type = 2;
            }
        }
        if (type == 0 || type == -1 && minus > 0) {
            if (minus == 0) {
                return LocaleController.getString("LastSeenEverybody", R.string.LastSeenEverybody);
            } else {
                return LocaleController.formatString("LastSeenEverybodyMinus", R.string.LastSeenEverybodyMinus, minus);
            }
        } else if (type == 2 || type == -1 && minus > 0 && plus > 0) {
            if (plus == 0 && minus == 0) {
                return LocaleController.getString("LastSeenContacts", R.string.LastSeenContacts);
            } else {
                if (plus != 0 && minus != 0) {
                    return LocaleController.formatString("LastSeenContactsMinusPlus", R.string.LastSeenContactsMinusPlus, minus, plus);
                } else if (minus != 0) {
                    return LocaleController.formatString("LastSeenContactsMinus", R.string.LastSeenContactsMinus, minus);
                } else if (plus != 0) {
                    return LocaleController.formatString("LastSeenContactsPlus", R.string.LastSeenContactsPlus, plus);
                }
            }
        } else if (type == 1 || type == -1 && plus > 0) {
            if (plus == 0) {
                return LocaleController.getString("LastSeenNobody", R.string.LastSeenNobody);
            } else {
                return LocaleController.formatString("LastSeenNobodyPlus", R.string.LastSeenNobodyPlus, plus);
            }
        }
        return "unknown";
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private class ListAdapter extends BaseFragmentAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int i) {
            return true;
        }

        @Override
        public int getCount() {
            return rowCount;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            int type = getItemViewType(i);
            if (type == 0) {
                if (view == null) {
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(0xffffffff);
                }
                TextSettingsCell textCell = (TextSettingsCell) view;
                if (i == homeRow) {
                    textCell.setText("Home", true);
                } else if (i == workRow) {
                    textCell.setText("Work", true);
                } else if (i == entertainmentRow) {
                    textCell.setText("Entertainment",true);
                }
            } else if (type == 1) {
                if (view == null) {
                    view = new TextInfoPrivacyCell(mContext);
                }

            } else if (type == 2) {
                if (view == null) {
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(0xffffffff);
                }
                if (i == placesSectionRow) {
                    ((HeaderCell) view).setText("My Places");
                } else if (i == weekSectionRow) {
                    //((HeaderCell) view).setText("My Weekly Routine");
                }
            }  else if (type == 3) {
                if (view == null) {
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(0xffffffff);
                }
            }
            return view;
        }

        @Override
        public int getItemViewType(int i) {
            if (i == homeRow || i == workRow || i == entertainmentRow) {
                return 0;
            }else if (i == placesSectionRow || i == weekSectionRow) {
                return 2;
            }
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 4;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    private class getCoordinates extends AsyncTask<String, Void, Bundle>{

        @Override
        protected Bundle doInBackground(String... params) {
            String url = "https://maps.googleapis.com/maps/api/geocode/json?address="+params[0]+"&key=AIzaSyBRl6TIz43ZR9kuW_sjos6NdAWb557V2wQ";


            HTTPHandler sh = new HTTPHandler();
            String jsonStr = null;
            try {
                jsonStr = sh.MakeServiceCall(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            if(jsonStr != null){
                try{
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    JSONArray resultsArray = jsonObject.getJSONArray("results");
                    JSONObject resultsObject = resultsArray.getJSONObject(0);
                    JSONObject b = resultsObject.getJSONObject("geometry");
                    JSONObject c = b.getJSONObject("location");
                    lat = c.getString("lat");
                    lon = c.getString("lng");
                    Log.i("lat", lat);
                    Log.i("lon", lon);
                    Bundle location = new Bundle();
                    location.putString("lat",lat);
                    location.putString("lon",lon);
                    return location;

                }catch (final JSONException e){
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    Bundle error = new Bundle();
                    error.putString("Json parsing error: ", e.getMessage());
                    return error;

                }
            }else {
                Log.e(TAG, "Couldn't get json from server.");
                Bundle error = new Bundle();
                error.putString(TAG, "Couldn't get json from server.");
                return error;
            }
        }

        @Override
        protected void onPostExecute(Bundle result){
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute(){}

        @Override
        protected void onProgressUpdate(Void... Values){}
    }
}
