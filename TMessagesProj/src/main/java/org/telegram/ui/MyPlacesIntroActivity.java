/*
 * This is the source code of Telegram for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.telegram.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

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

import java.util.ArrayList;

public class MyPlacesIntroActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private ListAdapter listAdapter;

    private int placesSectionRow;
    private int homeRow;
    private int workRow;
    private int entertainmentRow;
    private int weekSectionRow;
    private int rowCount;

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
            actionBar.setTitle(LocaleController.getString("PrivacySettings", R.string.PrivacySettings));
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
                    ((HeaderCell) view).setText("My Weekly Routine");
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
}
