package de.meek.inavmissionplanner;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    ListView m_listView = null;
    ArrayList<BoxMode> m_boxModeList = null;
    private static BoxModeAdapter m_listAdapter;
    int m_selectedAux = 0;
    Handler m_handlerCyclicUpdateUI = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        m_listView = (ListView)findViewById(R.id.listBoxMode);

        // https://www.journaldev.com/10416/android-listview-with-custom-adapter-example-tutorial

        m_boxModeList = new ArrayList<>();
        m_listAdapter = new BoxModeAdapter(m_boxModeList, getApplicationContext());
        m_listView.setAdapter(m_listAdapter);

        Spinner spinner = (Spinner) findViewById(R.id.spAux);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                m_selectedAux = i;
//                String item = adapterView.getItemAtPosition(i).toString();
//                Toast.makeText(adapterView.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        m_handlerCyclicUpdateUI.postDelayed(m_runnableUpdateUI, Const.refreshRateUI);

        m_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            updateAuxSetting(position);
            refreshList();
            }
        });
    }

    Runnable m_runnableUpdateUI = new Runnable() {
        @Override
        public void run() {
        ((TextView)findViewById(R.id.tvAux1Val)).setText(String.format("%d" , App.getInstance().getMsp().rcAux1));
        ((TextView)findViewById(R.id.tvAux2Val)).setText(String.format("%d" , App.getInstance().getMsp().rcAux2));
        m_handlerCyclicUpdateUI.postDelayed(this, Const.refreshRateUI);
        }
    };

    void updateAuxSetting(int index) {
        int start = 0;
        int end = 0;
        int aux = 0;
        switch (m_selectedAux) {
            case 1: // clear
                break;
            case 2: // aux 1
                start = App.getInstance().getMsp().rcAux1;
                break;
            case 3: // aux 2
                start = App.getInstance().getMsp().rcAux2;
                break;
            case 0: // nothing
            default:
                return;
        }

        if ((m_selectedAux >= 2) && (start > 0)) {
            int tmp = start;
            start = (tmp - 100 - 900) / 25;
            end =  (tmp + 100 - 900) / 25;
            aux = m_selectedAux - 2; // aux 1 has index 0
        }

        int permanentId = Const.boxes[index].permanentId;
        synchronized (App.getInstance().getMsp().boxModeList) {
            int i = 0;
            int free = -1;
            for (BoxMode bm : App.getInstance().getMsp().boxModeList) {
                if ((free == -1) && (bm.m_start ==  bm.m_end)) {
                    free = i;
                }
                if (bm.m_permanentId == permanentId) {
                    App.getInstance().getMsp().boxModeList.set(i, new BoxMode(i, permanentId, aux, start, end));
                    return;
                }
                i++;
            }

            if (free != -1) {
                App.getInstance().getMsp().boxModeList.set(free, new BoxMode(free, permanentId, aux, start, end));
            }
        }
    }

    public void onBtnSend(View v)
    {
        Toast.makeText(this, "Sending to FC", Toast.LENGTH_SHORT).show();
        synchronized (App.getInstance().getMsp().boxModeList) {
            int i = 0;
            for (BoxMode bm : App.getInstance().getMsp().boxModeList) {
                byte[] b = App.getInstance().getMsp().serialize_MSP_SET_MODE_RANGE((byte)i, (byte)bm.m_permanentId, (byte)bm.m_aux, (byte)bm.m_start, (byte)bm.m_end);
                App.getInstance().request(b);
                i++;
            }
        }
    }

    public void onBtnReceive(View v)
    {
        Toast.makeText(this, "Receiving from FC", Toast.LENGTH_SHORT).show();
        App.getInstance().getMsp().registerCallbackModeChange(handlerUpdateList);
        byte[] b = App.getInstance().getMsp().serialize_MSP_MODE_RANGES_Request();
        App.getInstance().request(b);
    }

    public BoxMode getBox(ArrayList<BoxMode> list, int permanentId) {
        if (list != null) {
            for (BoxMode b : list) {
                if (b.m_permanentId == permanentId) {
                    return b;
                }
            }
        }
        return null;
    }

    public void refreshList() {
        ArrayList<BoxMode> listData = new ArrayList<>();
        synchronized (App.getInstance().getMsp().boxModeList) {
            int i=0;
            for(Const.Box b : Const.boxes) {
                int start = 0;
                int end = 0;
                int aux = -1;
                BoxMode bm = getBox(App.getInstance().getMsp().boxModeList, b.permanentId);
                if (bm != null) {
                    start = bm.m_start * 25 + 900;
                    end = bm.m_end * 25 + 900;
                    aux = bm.m_aux;
                }

                listData.add(new BoxMode(i, b.permanentId, aux, start, end));
                i++;
            }
        }
        m_listAdapter.refreshEvents(listData);
    }

    public void onBtnSave(View v)
    {
        Toast.makeText(this, "Saving to EEPROM", Toast.LENGTH_SHORT).show();
        byte[] b = App.getInstance().getMsp().serialize_MSP_EEPROM_WRITE();
        App.getInstance().request(b);
    }

    Handler handlerUpdateList = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            refreshList();
        }
    };
}
