package com.chainup.contract.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;

public final class CpPreferenceManager {
	
	private static CpPreferenceManager sInstance;
	private final SharedPreferences mPreferences;

	public static final String PREF_THEMEMODE = "themeMode";
	public static final String PREF_LANGUAGE = "Language";


	public static final String PREF_TRADE_CONFIRM = "TradeConfirm";
	public static final String PREF_LOSS_CONFIRM = "LossConfirm";
	public static final String PREF_CONTRACT_UNIT = "ContractUnit";
	public static final String PREF_CONTRACT_USDT = "ContractUnitUSDT";
	public static final String PREF_CONTRACT_PNL_CALCULATE = "ContractPnlCalculate";
	public static final String PREF_TRIGGER_PRICE_TYPE = "TriggerPriceType";
	public static final String PREF_EXECUTION = "Execution";
	public static final String PREF_STRATEGY_EFFECTIVE_TIME = "StrategyEffectiveTime";

	public static final String CONTRACT_JSON_LIST_STR = "contract_json_list_str";
	public static final String CONTRACT_MARGIN_COIN_STR = "contract_margin_coin_str";

	public static final String CONTRACT_CURRENT_SELECTED_ID = "contract_current_selected_id";

	public static final String CONTRACT_POSITION_MODEL = "contract_position_model";

	public static final String CONTRACT_TOKEN = "contract_token";

	public static final String CONTRACT_INVITE_URL = "contract_invite_url";

	public static final String isShowAllContract = "isShowAllContract";

	public static final String isShowAllContractEntrust = "isShowAllContractEntrust";

	public static final String isShowAllContractPlan = "isShowAllContractPlan";

	private CpPreferenceManager(Context context) {
		mPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public static CpPreferenceManager getInstance(Context context) {
		synchronized (CpPreferenceManager.class) {
			if (sInstance == null) {
				sInstance = new CpPreferenceManager(context.getApplicationContext());
			}
		}
		
		return sInstance;
	}
	
	public String getSharedString(final String key, final String defValue) {
		return mPreferences.getString(key, defValue);
	}
	
	public void putSharedString(final String key, final String value) {
		mPreferences.edit().putString(key, value).commit();
	}
	
	public void putSharedStringAsyn(final String key, final String value) {
		new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
            	mPreferences.edit().putString(key, value).commit();
                return null;
            }
		}.execute();
	}
	
	public int getSharedInt(final String key, final int defValue) {
		return mPreferences.getInt(key, defValue);
	}
	
	public void putSharedInt(final String key, final int value) {
		mPreferences.edit().putInt(key, value).commit();
	}
	
	public void putSharedIntAsyn(final String key, final int value) {
		new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
            	mPreferences.edit().putInt(key, value).commit();
                return null;
            }
		}.execute();
	}
	
	public long getSharedLong(final String key, final long defValue) {
		return mPreferences.getLong(key, defValue);
	}
	
	public void putSharedLong(final String key, final long value) {
		mPreferences.edit().putLong(key, value).commit();
	}
	
	public void putSharedLongAsyn(final String key, final long value) {
		new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
            	mPreferences.edit().putLong(key, value).commit();
                return null;
            }
        }.execute();
	}
	
	public boolean getSharedBoolean(final String key, final boolean defValue){
		return mPreferences.getBoolean(key, defValue);
	}
	
	public void putSharedBoolean(final String key, final boolean value) {
		mPreferences.edit().putBoolean(key, value).commit();
	}
	
	public void putSharedBooleanAsyn(final String key, final boolean value) {
		new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
            	mPreferences.edit().putBoolean(key, value).commit();
                return null;
            }
		}.execute();
	}
	
	public void removeShare(final String... keys) {
		Editor editor = mPreferences.edit();
		for (String key : keys) {
			editor.remove(key);
		}
		editor.commit();
	}
	
	public static void remove(final Activity ac, final String... keys) {
		Editor editor = ac.getPreferences(Context.MODE_PRIVATE).edit();
		for (String key : keys) {
			editor.remove(key);
		}
		editor.commit();
	}
	
	public static void removeAsyn(final Activity ac, final String key) {
		new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
            	ac.getPreferences(Context.MODE_PRIVATE)
    				.edit().remove(key).commit();
                return null;
            }
		}.execute();
	}
	
	public static boolean getBoolean(final Activity ac, final String key, final boolean defValue) {
		return ac.getPreferences(Context.MODE_PRIVATE)
				.getBoolean(key, defValue);
	}
	
	public static void putBoolean(final Activity ac, final String key,
                                  final boolean value) {
		SharedPreferences sp = ac.getPreferences(Context.MODE_PRIVATE);
		sp.edit().putBoolean(key, value).commit();
	}
	
	public static void putBooleanAsyn(final Activity ac, final String key, final boolean value) {
		new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
            	SharedPreferences sp = ac.getPreferences(Context.MODE_PRIVATE);
                sp.edit().putBoolean(key, value).commit();
                return null;
            }
		}.execute();
	}
	
	public static long getLong(final Activity ac, final String key, final long defValue) {
		return ac.getPreferences(Context.MODE_PRIVATE)
				.getLong(key, defValue);
	}
	
	public static void putLongAsyn(final Activity ac, final String key, final long value) {
		new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
            	SharedPreferences sp = ac.getPreferences(Context.MODE_PRIVATE);
                sp.edit().putLong(key, value).commit();
                return null;
            }
		}.execute();
	}
	
	public static int getInt(final Activity ac, final String key, final int defValue) {
		if(ac == null){
			return defValue;
		}
		return ac.getPreferences(Context.MODE_PRIVATE)
				.getInt(key, defValue);
	}
	
	public static void putInt(final Activity ac, final String key, final int value) {
    	SharedPreferences sp = ac.getPreferences(Context.MODE_PRIVATE);
        sp.edit().putInt(key, value).commit();
	}
	
	public static void putIntAsyn(final Activity ac, final String key, final int value) {
		new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
            	SharedPreferences sp = ac.getPreferences(Context.MODE_PRIVATE);
                sp.edit().putInt(key, value).commit();
                return null;
            }
		}.execute();
	}
	
	public static String getString(final Activity ac, final String key, final String defValue) {
		return ac.getPreferences(Context.MODE_PRIVATE)
				.getString(key, defValue);
	}
	
	public static void putStringAsyn(final Activity ac, final String key, final String value) {
		new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
            	SharedPreferences sp = ac.getPreferences(Context.MODE_PRIVATE);
                sp.edit().putString(key, value).commit();
                return null;
            }
		}.execute();
	}
}
