package cz.nuc.gw2lfg;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

/**
 * Created with IntelliJ IDEA.
 * User: mist
 * Date: 13.4.13
 * Time: 14:44
 * To change this template use File | Settings | File Templates.
 */
public final class HelpActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
