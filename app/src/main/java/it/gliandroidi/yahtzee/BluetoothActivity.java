package it.gliandroidi.yahtzee;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class BluetoothActivity extends AppCompatActivity {
    Holder holder;
    ArrayList<BluetoothDevice> arrayListBluetoothDevices = null;

    public static int REQUEST_ENABLE_BT=1;
    ArrayList<String> stringArrayList= new ArrayList<String>();
    ArrayAdapter<String> arrayAdapter;
    BluetoothDevice bdDevice;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        holder= new Holder();

        //RICHIEDE IL PERMESSO DI GEOLOCALIZZAZIONE NECESSARIO PER LO SCANNING BLUETOOTH
        if (ActivityCompat.checkSelfPermission(BluetoothActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    private class Holder implements View.OnClickListener, AdapterView.OnItemClickListener {
        BluetoothAdapter myBTA = BluetoothAdapter.getDefaultAdapter();
        Button btnScan, btnListen;
        ListView listView;
        String name;

        Holder(){
            btnListen=findViewById(R.id.btnListen);
            btnScan=findViewById(R.id.btnScan);
            listView=findViewById(R.id.listView);
            listView.setOnItemClickListener(this);
            btnListen.setOnClickListener(this);
            btnScan.setOnClickListener(this);
            arrayListBluetoothDevices = new ArrayList<BluetoothDevice>();
            Intent data = getIntent();
            name= data.getStringExtra("playerName");


            //VERIFICA BLUETOOTH: SE IL BLUETOOTHADAPTER è NULL ALLORA IL DISPOSITIVO NON è PREDISPOSTO AL BLUETOOTH
            if(myBTA== null) {
                Toast.makeText(getApplicationContext(), R.string.supported_bt, Toast.LENGTH_LONG).show();
            }
            else {
                //RICHIEDE DI ABILITARE IL BLUETOOTH DEL DISPOSITIVO QUALORA NON FOSSE GIà ATTIVO
                if(!myBTA.isEnabled()){
                    Intent enableBTI = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBTI,REQUEST_ENABLE_BT);

                }
                else{
                    Toast.makeText(getApplicationContext(), R.string.enabled_bt, Toast.LENGTH_LONG).show();
                }
            }

            IntentFilter filter = new IntentFilter();
            //SONO I FILTRI DELLE AZIONI CHE DEVONO ESSERE TROVATE DAL BROADCASTRECEIVER
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            //VIENE INIZIALIZZATO IL BROADCASTRECEIVER
            registerReceiver(brReceiver, filter);

            //RICHIEDE DI RENDERE IL DISPOSITIVO VISIBILE PER IL TEMPO DI DEFAULT DI 120 SECONDI
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);

            //CREA LA LISTVIEW CON I DISPOSITIVI TROVATI DAL BROADCASTRECEIVER GIà FILTRATI
            arrayAdapter= new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, stringArrayList);
            listView.setAdapter(arrayAdapter);





        }


        @Override
        public void onClick(View v) {
            if(v.getId()==R.id.btnScan) {

                //SERVONO PER RIPULIRE LA LISTVIEW AD OGNI SCANSIONE
                stringArrayList.clear();
                arrayListBluetoothDevices.clear();
                // COMINCIA LA SCANSIONE DEI DISPOSITIVI NELLE VICINANZE
                myBTA.startDiscovery();
            }

            if(v.getId()==R.id.btnListen){
                //VIENE APERTA LA GAME ACTIVITY PORTANDO CON SE IL VALORE DEL MULTIPLAYER, IL NOME GIOCATORE E IL BDDEVICE NULL;
                //VIENE INSERITO ANCHE UN VALORE BOOLEANO PER DECIDERE CHI GIOCA PER PRIMO (IN QUESTO CASO, SI GIOCA PER SECONDI)
                Intent intent = new Intent(BluetoothActivity.this, GameActivity.class);
                intent.putExtra("device",bdDevice);
                intent.putExtra("playerName",name);
                intent.putExtra("gameType", 2);
                intent.putExtra("goesFirst", false);
                startActivity(intent);
            }

        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            bdDevice = arrayListBluetoothDevices.get(position);
            //SI CANCELLA LA DISCOVERY CHE RALLENTEREBBE L'APPLICAZIONE
            myBTA.cancelDiscovery();
            //VIENE APERTA LA GAME ACTIVITY PORTANDO CON SE IL VALORE DEL MULTIPLAYER, IL NOME GIOCATORE E IL BDDEVICE NON NULL;
            //VIENE INSERITO ANCHE UN VALORE BOOLEANO PER DECIDERE CHI GIOCA PER PRIMO (IN QUESTO CASO, SI GIOCA PER PRIMI)
            Intent intent = new Intent(BluetoothActivity.this, GameActivity.class);
            intent.putExtra("device",bdDevice);
            intent.putExtra("playerName",name);
            intent.putExtra("gameType", 2);
            intent.putExtra("goesFirst", true);
            startActivity(intent);

        }


        private final BroadcastReceiver brReceiver= new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                    Toast.makeText(BluetoothActivity.this, R.string.device_finding, Toast.LENGTH_LONG).show();
                }else if (BluetoothDevice.ACTION_FOUND.equals(action)){
                    //SCOPRE TUTTI I DISPOSITIVI NELLE VICINANZE
                    BluetoothDevice device= intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    if(arrayListBluetoothDevices.size()<1)
                    {
                        // PRENDE SOLO I DISPOSITIVI CON NOME OLTRE ALL'INDIRIZZO MAC, CHE NON SONO GIà STATI PRESI
                        if (device.getName()!= null) {
                            arrayListBluetoothDevices.add(device);
                            stringArrayList.add(device.getName());
                            arrayAdapter.notifyDataSetChanged();
                        }
                    }
                    else
                    {
                        if (device.getName()!= null) {
                            boolean flag = true;    // FLAG CHE INDICA SE IL DISPOTIVIO È GIA PRESO
                            for (int i = 0; i < arrayListBluetoothDevices.size(); i++) {
                                if (device.getAddress().equals(arrayListBluetoothDevices.get(i).getAddress())) {
                                    flag = false;
                                }
                            }
                            if (flag == true) {
                                arrayListBluetoothDevices.add(device);
                                stringArrayList.add(device.getName());
                                arrayAdapter.notifyDataSetChanged();
                            }
                        }
                    }

                }
            }
        };
    }

}