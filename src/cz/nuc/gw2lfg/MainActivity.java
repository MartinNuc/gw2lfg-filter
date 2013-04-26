package cz.nuc.gw2lfg;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MainActivity extends ListActivity {

    private List<String> filter = new ArrayList<String>();
    private List<String> events = new ArrayList<String>();
    private List<LookingForGroupItem> notInterested = new ArrayList<LookingForGroupItem>();
    private Integer minLevel = null;
    private Integer maxLevel = null;
    private int continent = 0;
    private Boolean sounds = true;
    private long delay = 10;
    private int limit = 25;
    private Boolean filterSwitch = false;
    private MenuItem refreshButton;
    private AtomicBoolean keepRunning = new AtomicBoolean(false);
    private ArrayAdapter<LookingForGroupItem> adapter;
    private List<LookingForGroupItem> lookingForGroupItems = new ArrayList<LookingForGroupItem>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        keepRunning.set(false);

        adapter = new LookingForGroupAdapter(this, lookingForGroupItems);
        setListAdapter(adapter);

        getListView().setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                                   int position, long id) {
                        LookingForGroupItem i;
                        try {
                            i = lookingForGroupItems.get(position);
                        }
                        catch (Exception e)
                        {
                            return false;
                        }
                        if (notInterested.contains(i) == false) {
                            notInterested.add(i);
                        }
                        lookingForGroupItems.remove(position);
                        adapter.notifyDataSetChanged();
                        return false;
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSettings();
        refresh();
    }

    public void refresh() {
        new BackgroundAsyncTask().execute();
    }

    private void loadSettings() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        if (prefs == null) {
            return;
        }
        Map<String, ?> all = prefs.getAll();
        Iterator it = all.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            if ("filter_preference".equals(pairs.getKey()) == true) {
                String entry = (String) pairs.getValue();
                filter.clear();
                if ("".equals(entry) == false) {
                    String[] split = entry.split(",");
                    for (String s : split) {
                        filter.add(s.trim());
                    }
                }
            } else if ("filter_switch".equals(pairs.getKey()) == true) {
                filterSwitch = (Boolean) pairs.getValue();
                if (filterSwitch == null) {
                    filterSwitch = false;
                }
            } else if ("min-level".equals(pairs.getKey()) == true) {
                try {
                    minLevel = Integer.parseInt((String) pairs.getValue());
                } catch (Exception e) {
                    minLevel = null;
                }
            } else if ("max-level".equals(pairs.getKey()) == true) {
                try {
                    maxLevel = Integer.parseInt((String) pairs.getValue());
                } catch (Exception e) {
                    minLevel = null;
                }
            } else if ("sounds".equals(pairs.getKey()) == true) {
                sounds = (Boolean) pairs.getValue();
                if (sounds == null) {
                    sounds = true;
                }
            } else if ("limit".equals(pairs.getKey()) == true) {
                try {
                    limit = Integer.parseInt((String) pairs.getValue());
                } catch (Exception e) {
                    limit = 25;
                }
            } else if ("delay".equals(pairs.getKey()) == true) {
                try {
                    delay = Long.parseLong((String) pairs.getValue());
                } catch (Exception e) {
                    delay = 10;
                }
            } else if ("continent".equals(pairs.getKey()) == true) {
                if ((Boolean) pairs.getValue() == true) {
                    continent = 1;
                } else {
                    continent = 0;
                }
            } else if ("list_preference".equals(pairs.getKey()) == true) {
                HashSet<String> events = (HashSet<String>) pairs.getValue();
                this.events.clear();
                for (String s : events) {
                    this.events.add(s);
                }
            }

            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    private void playSound() {
        if (sounds == false) {
            return;
        }

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            Log.getStackTraceString(e);
            return;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.refresh_option:
                refreshButton = item;
                if (keepRunning.get() == true) {
                    disableRefresh();
                } else {
                    enableRefresh();
                }
                return (true);
            case R.id.action_settings:
                intent = new Intent(this, PreferencesFromXml.class);
                startActivity(intent);
                return (true);
            case R.id.action_help:
                intent = new Intent(this, HelpActivity.class);
                startActivity(intent);
                return (true);
        }
        return super.onOptionsItemSelected(item);
    }

    private void disableRefresh() {
        keepRunning.set(false);

        if (refreshButton != null && refreshButton.getActionView() != null) {
            refreshButton.getActionView().clearAnimation();
            refreshButton.setActionView(null);
        }
    }

    private void enableRefresh() {
        keepRunning.set(true);

        if (refreshButton == null) {
            refreshButton = (MenuItem) findViewById(R.id.refresh_option);
        }
        LayoutInflater inflater = (LayoutInflater) getApplication()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) inflater.inflate(R.layout.action_refresh, null);

        Animation rotation = AnimationUtils.loadAnimation(getApplication(),
                R.anim.refresh_rotate);
        rotation.setRepeatCount(Animation.INFINITE);
        iv.startAnimation(rotation);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableRefresh();
            }
        });
        refreshButton.setActionView(iv);

        refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        refreshButton = (MenuItem) findViewById(R.id.refresh_option);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableRefresh();
    }

    public final class BackgroundAsyncTask extends AsyncTask<Void, String, List<LookingForGroupItem>> {

        @Override
        protected List<LookingForGroupItem> doInBackground(Void... params) {

            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();

            HttpGet httpGet = new HttpGet("http://www.gw2lfg.com");
            HttpResponse response;
            try {
                response = httpClient.execute(httpGet, localContext);
            } catch (Exception e) {
                return null;
            }

            ArrayList<LookingForGroupItem> ret = new ArrayList<LookingForGroupItem>();

            String html = "";
            int responseCode = response.getStatusLine().getStatusCode();
            switch (responseCode) {
                case 200:
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        try {
                            html = EntityUtils.toString(entity);
                        } catch (Exception e) {
                            Log.getStackTraceString(e);
                            return new ArrayList<LookingForGroupItem>();
                        }
                    }
                    break;
                default:
                    return new ArrayList<LookingForGroupItem>();
            }

            try {
                Document doc = Jsoup.parse(html);

                Element tab;
                if (continent == 0) {
                    tab = doc.getElementById("tab_EU");
                } else {
                    tab = doc.getElementById("tab_NA");
                }
                Element body = tab.getElementsByTag("tbody").first();
                Elements rows = body.children();
                int cnt = 0;
                for (Element row : rows) {
                    boolean interesting = false;
                    if (events.size() > 0) {
                        String event = row.child(1).text();
                        for (String word : events) {
                            if (event.contains(word) == true) {
                                interesting = true;
                                break;
                            }
                        }
                        if (interesting == false) {
                            continue;
                        }
                    }
                    // apply word filter
                    if (filter.size() > 0 && filterSwitch == true) {
                        interesting = false;
                        String text = row.child(3).text();
                        for (String word : filter) {
                            if (text.toLowerCase().contains(word.toLowerCase()) == true) {
                                interesting = true;
                                break;
                            }
                        }
                        if (interesting == false) {
                            continue;
                        }
                    }

                    LookingForGroupItem i = new LookingForGroupItem();
                    i.setName(row.child(0).text());
                    i.setLevel(row.child(2).text());
                    if (minLevel != null && i.getLevel() != null && i.getLevel() < minLevel) {
                        continue;
                    }
                    if (maxLevel != null && i.getLevel() != null && i.getLevel() > maxLevel) {
                        continue;
                    }
                    i.setText(row.child(3).text());
                    i.setEvent(row.child(1).text());
                    i.setUpdated(row.child(4).text());

                    if (notInterested.contains(i)) {
                        continue;
                    }

                    ret.add(i);
                    cnt++;

                    if (cnt >= limit) {
                        break;
                    }
                }
            } catch (Exception e) {
                Log.e("cz.nuc.gw2lfg", "Error during loading data", e);
                return new ArrayList<LookingForGroupItem>();
            }

            return ret;
        }

        @Override
        protected void onPostExecute(List<LookingForGroupItem> result) {
            super.onPostExecute(result);

            if (result != null) {
                lookingForGroupItems.clear();
                if (result.size() > 0) {
                    for (LookingForGroupItem i : result) {
                        lookingForGroupItems.add(i);
                    }
                }
                adapter.notifyDataSetChanged();
                if (keepRunning.get() == true) {
                    if (result.size() > 0) {
                        playSound();
                    }
                    Toast.makeText(getBaseContext(), "Autorefresh found: " + result.size(), Toast.LENGTH_SHORT).show();

                    ScheduledExecutorService scheduler =
                            Executors.newSingleThreadScheduledExecutor();
                    scheduler.schedule(new Runnable() {

                        public void run() {
                            refresh();
                        }
                    }, delay, TimeUnit.SECONDS);
                }
            }
        }
    }
}