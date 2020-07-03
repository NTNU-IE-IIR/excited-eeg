package no.ntnu.stud.avikeyb;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.ntnu.stud.avikeyb.backend.InputInterface;
import no.ntnu.stud.avikeyb.backend.InputType;
import no.ntnu.stud.avikeyb.backend.Keyboard;
import no.ntnu.stud.avikeyb.backend.Layout;
import no.ntnu.stud.avikeyb.backend.OutputDevice;
import no.ntnu.stud.avikeyb.backend.core.BackendLogger;
import no.ntnu.stud.avikeyb.backend.core.CoreKeyboard;
import no.ntnu.stud.avikeyb.backend.core.Logger;
import no.ntnu.stud.avikeyb.backend.core.WordUpdater;
import no.ntnu.stud.avikeyb.backend.dictionary.DictionaryEntry;
import no.ntnu.stud.avikeyb.backend.dictionary.DictionaryHandler;
import no.ntnu.stud.avikeyb.backend.dictionary.InMemoryDictionary;
import no.ntnu.stud.avikeyb.backend.dictionary.LinearEliminationDictionaryHandler;
import no.ntnu.stud.avikeyb.backend.dictionary.ResourceHandler;
import no.ntnu.stud.avikeyb.backend.layouts.AdaptiveLayout;
import no.ntnu.stud.avikeyb.backend.layouts.BinarySearchLayout;
import no.ntnu.stud.avikeyb.backend.layouts.ETOSLayout;
import no.ntnu.stud.avikeyb.backend.layouts.LayoutWithSuggestions;
import no.ntnu.stud.avikeyb.backend.layouts.MobileLayout;
import no.ntnu.stud.avikeyb.gui.AdaptiveLayoutGUI;
import no.ntnu.stud.avikeyb.gui.BinarySearchLayoutGUI;
import no.ntnu.stud.avikeyb.gui.ChessLayoutGUI;
import no.ntnu.stud.avikeyb.gui.ETOSLayoutGUI;
import no.ntnu.stud.avikeyb.gui.LayoutGUI;
import no.ntnu.stud.avikeyb.gui.MobileLayoutGUI;
import no.ntnu.stud.avikeyb.gui.core.AndroidResourceLoader;
import no.ntnu.stud.avikeyb.iointerface.WebSocketInterface;
import no.ntnu.stud.avikeyb.gui.core.AsyncSuggestions;
import no.ntnu.stud.avikeyb.gui.core.ChessLayout;
import no.ntnu.stud.avikeyb.gui.core.TabSwitchInterceptor;

public class MainActivity extends AppCompatActivity {

    private ViewGroup layoutWrapper;

    private final DictionaryHandler dictionaryHandler = new DictionaryHandler();
    private Keyboard keyboard;

    private Layout currentLayout;
    private LayoutGUI currentLayoutGUI;
    private Layout.LayoutListener currentLayoutListener;
    private List<String> cachedSuggestions = new ArrayList<>();
    private boolean isBroadcasting = false;
    private Button broadcastButton;
    private TabSwitchInterceptor tabSwitcherInterceptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BackendLogger.setLogger(new Logger() {
            @Override
            public void log(String s) {
                Log.d("BackendLogInfo", s);
            }
        });

        keyboard = new CoreKeyboard();
        keyboard.addOutputDevice(new ToastOutput());

        final TabLayout layoutTabs = (TabLayout) findViewById(R.id.layoutTabs);
        layoutTabs.setTabMode(TabLayout.MODE_SCROLLABLE);

//        layoutTabs.addTab(layoutTabs.newTab().setText("Simple"));
        layoutTabs.addTab(layoutTabs.newTab().setText("ETOS"));
        layoutTabs.addTab(layoutTabs.newTab().setText("Adaptive"));
        layoutTabs.addTab(layoutTabs.newTab().setText("Mobile"));
        layoutTabs.addTab(layoutTabs.newTab().setText("Binary"));
        layoutTabs.addTab(layoutTabs.newTab().setText("Chess"));


        tabSwitcherInterceptor = new TabSwitchInterceptor(layoutTabs);

        layoutWrapper = (ViewGroup) findViewById(R.id.layoutWrapper);

        final LinearEliminationDictionaryHandler mobileDictionary = new LinearEliminationDictionaryHandler();

        final ETOSLayout etosLayout = new ETOSLayout(keyboard);
        final BinarySearchLayout binLayout = new BinarySearchLayout(keyboard);
        final AdaptiveLayout adaptiveLayout = new AdaptiveLayout(keyboard);

        // Asynchronously load the dictionary enties from a file and set the entries to the
        // two in memory dictionaries.
        loadDictionaryFromFile(Arrays.asList(dictionaryHandler, mobileDictionary), R.raw.dictionary);

        final TabLayout.OnTabSelectedListener tabSwitcher = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 1: {
                        switchLayout(adaptiveLayout, new AdaptiveLayoutGUI(MainActivity.this, adaptiveLayout));
                        break;
                    }
                    case 2: {
                        MobileLayout l = new MobileLayout(keyboard, mobileDictionary);
                        MobileLayoutGUI mobileGUI = new MobileLayoutGUI(MainActivity.this, l, R.layout.layout_mobile_dictionary, R.layout.layout_mobile);
                        switchLayout(l, mobileGUI);
                        mobileGUI.firstUpdate(); // We need to update gui right after it has been set to mark first mobile layout row.
                        break;
                    }
                    case 3: {
                        switchLayout(binLayout, new BinarySearchLayoutGUI(MainActivity.this, binLayout));
                        break;
                    }
                    case 4: {
                        ChessLayout chessLayout = new ChessLayout(keyboard);
                        switchLayout(chessLayout, new ChessLayoutGUI(MainActivity.this, chessLayout));
                        break;
                    }
                    default: {
                        switchLayout(etosLayout, new ETOSLayoutGUI(MainActivity.this, etosLayout));
                        break;
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        };

         final EditText bufferText = ((EditText) MainActivity.this.findViewById(R.id.currentBuffer));

        bufferText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager methodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                methodManager.hideSoftInputFromWindow(bufferText.getWindowToken(), 0);
                return true;
            }
        });


        final AsyncSuggestions suggestions = new AsyncSuggestions(dictionaryHandler);

        // Update the buffer view
        keyboard.addStateListener(new Keyboard.KeyboardListener() {
            @Override
            public void onOutputBufferChange(String oldBuffer, String newBuffer) {
                bufferText.setText(newBuffer);
                bufferText.setSelection(bufferText.getText().length());
                suggestions.findSuggestionsFor(keyboard.getCurrentWord(), new AsyncSuggestions.ResultCallback() {
                    @Override
                    public void onResult(List<String> suggestions) {
                        cachedSuggestions = suggestions.subList(0, Math.min(20, suggestions.size()));
                        if(currentLayout != null && currentLayout instanceof LayoutWithSuggestions){
                            ((LayoutWithSuggestions) currentLayout).setSuggestions(cachedSuggestions);
                        }
                    }
                });
            }
        });

        // Update user word usage count
        keyboard.addOutputDevice(new WordUpdater(dictionaryHandler));


        keyboard.addOutputDevice(new OutputDevice() {
            @Override
            public void sendOutput(String output) {
                WebSocketInterface.getInstance().sendOutput(output);
            }
        });
        keyboard.addSettingsListener(new Keyboard.SettingsListener() {
            @Override
            public void onSettingsRequestd() {
                tabSwitcherInterceptor.activate();
            }
        });

        layoutTabs.addOnTabSelectedListener(tabSwitcher);

        // Trigger the creation of the layout in the first tab
        tabSwitcher.onTabSelected(layoutTabs.getTabAt(0));

        broadcastButton = (Button)findViewById(R.id.buttonBroadcast);

        // If the phone is rotated or something else causes the activity to be recreated
        // while broadcasting the button will be reactivated and the user is able to
        // start two broadcast threads. Running two broadcasts at the same time shouldn't case any
        // problems (except for a temporary memory leak) so at the moment nothing is done to prevent
        // this.
        broadcastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startBroadCasting();
            }
        });

    } // end of on create


    @Override
    protected void onResume() {
        super.onResume();
        WebSocketInterface.getInstance().start();
        WebSocketInterface.getInstance().setInputInterface(new Handler(), tabSwitcherInterceptor.interceptInput(new InputInterface() {
            @Override
            public void sendInputSignal(InputType inputType) {
                if(currentLayout != null){
                    currentLayout.sendInputSignal(inputType);
                }
            }
        }));
    }

    @Override
    protected void onPause() {
        super.onPause();

        WebSocketInterface.getInstance().setInputInterface(new Handler(), null);
        WebSocketInterface.getInstance().stop(); // The client will have to reconnect when the activity resumes

        // Set save location.
        String folderPath = this.getFilesDir().getPath();
        String fileName = "dictionary.txt";
        String filePath = folderPath + "/" + fileName;
//        System.out.println(filePath);

        // Store the dictionary to file.
        try {
            ResourceHandler.storeDictionaryToFile(dictionaryHandler.getDictionary(), filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void switchLayout(Layout layout, LayoutGUI layoutGui) {
        layoutGui.setLayoutContainer(layoutWrapper);
        layoutGui.onLayoutActivated();

        if(layout instanceof LayoutWithSuggestions){
            ((LayoutWithSuggestions) layout).setSuggestions(cachedSuggestions);
        }

        layoutGui.updateGUI();
        setupInputButtons(layout);
        setupLayoutListener(layout, layoutGui);
    }

    private void setupLayoutListener(Layout layout, LayoutGUI layoutGui){

        if(currentLayout != null){
            // Remove the old listener when the layout changes to prevent memory leaks
            currentLayout.removeLayoutListener(currentLayoutListener);
        }

        // Update the gui when the layouts changes.
        currentLayoutListener = new Layout.LayoutListener() {
            @Override
            public void onLayoutChanged() {
                currentLayoutGUI.updateGUI();
            }
        };
        currentLayout = layout;
        currentLayoutGUI = layoutGui;
        currentLayout.addLayoutListener(currentLayoutListener);
    }

    private void setupInputButtons(final InputInterface input) {

        final InputInterface wrappedInput = tabSwitcherInterceptor.interceptInput(input);

        findViewById(R.id.buttonInput1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wrappedInput.sendInputSignal(InputType.INPUT1);
            }
        });
        findViewById(R.id.buttonInput2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wrappedInput.sendInputSignal(InputType.INPUT2);
            }
        });
        findViewById(R.id.buttonInput3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wrappedInput.sendInputSignal(InputType.INPUT3);
            }
        });
        findViewById(R.id.buttonInput4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wrappedInput.sendInputSignal(InputType.INPUT4);
            }
        });
    }


    // Fill the in memory dictionaryHandler from a file
    private void loadDictionaryFromFile(final List<InMemoryDictionary> dictionaries, final int resourceId) {
        new AsyncTask<Void, Void, List<DictionaryEntry>>() {
            @Override
            protected List<DictionaryEntry> doInBackground(Void... voids) {
                return AndroidResourceLoader.loadDictionaryFromResource(MainActivity.this, resourceId);
            }

            @Override
            protected void onPostExecute(List<DictionaryEntry> dictionaryEntries) {
                for (InMemoryDictionary dict : dictionaries) {
                    dict.setDictionary(dictionaryEntries);
                }

                // This is a hack to trigger an initial search in the dictionary for an empty
                // string. This is used to show suggestions of the most used words before the
                // user starts typing
                keyboard.clearCurrentBuffer();
            }
        }.execute();
    }

    // Shows the keyboard output in a toast message
    private class ToastOutput implements OutputDevice {
        @Override
        public void sendOutput(String output) {
            Toast.makeText(MainActivity.this, output, Toast.LENGTH_SHORT).show();
        }
    }




    private InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) (broadcast >> (k * 8));
        return InetAddress.getByAddress(quads);
    }


    private void startBroadCasting(){
        if(isBroadcasting){
            return;
        }
        isBroadcasting = true;
        broadcastButton.setEnabled(false);
        broadcastIp();
    }

    private void stopBroadcasting(){
        isBroadcasting = false;
        broadcastButton.setEnabled(true);
    }

    private void broadcastIp() {

        final InetAddress addr;
        try {
            addr = getBroadcastAddress();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("BROADCAST", "Failed to get broadcast address");
            synchronized (MainActivity.this){
                stopBroadcasting();
            }
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramSocket socket = null;
                try {
                    socket = new DatagramSocket();
                    socket.setBroadcast(true);
                    Log.d("BROADCAST", "Starting to broadcast");

                    // Broadcast for approximately 30 seconds
                    for(int i=0; i < 30; i++){
                        String data = "AVIKEYB";
                        DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), addr, WebSocketInterface.INTERFACE_TCP_PORT);
                        try {
                            socket.send(packet);
                            Log.d("BROADCAST", "Sent broadcast message " + i);
                        } catch (IOException e) {
                            Log.d("BROADCAST", "Failed to send broadcast");
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    Log.d("BROADCAST", "Finished braodcasting");
                } catch (SocketException e) {
                    e.printStackTrace();
                    Log.d("BROADCAST", "Failed to start broadcasting");
                }
                finally {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stopBroadcasting();
                        }
                    });
                }
            }
        }).start();
    }

}
