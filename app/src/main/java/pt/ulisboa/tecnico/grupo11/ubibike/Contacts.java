package pt.ulisboa.tecnico.grupo11.ubibike;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class Contacts extends AppCompatActivity {
    private ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        ListView listView = (ListView) findViewById(R.id.listView);
        ArrayList<String> contacts = new ArrayList<String>() {{
            add("Item 1");
            add("Item 2");
        }};

        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contacts);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        listView.setLongClickable(true);
        registerForContextMenu(listView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.listView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(listAdapter.getItem(info.position));

            menu.add(0, 0, Menu.NONE, "Send points");
            menu.add(0, 1, Menu.NONE, "Send SMS");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        // create the alert box and text view
        final EditText edit = new EditText(this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(edit);
        builder.setPositiveButton("Send", null);
        builder.setNegativeButton("Cancel", null);
        builder.create();
        final AlertDialog dialog;

        switch (item.getItemId()) {
            case 0:
                // set the title and switch the keyboard to numeric
                edit.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                builder.setTitle("Send points");

                dialog = builder.create();

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                Editable value = edit.getText();
                                try {
                                    int points = Integer.parseInt(value.toString());
                                    if (points <= 0 /*|| points > user.points*/) {
                                        Toast.makeText(Contacts.this, "You can't send that amount of points", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    // TODO: try to send points to other user
                                    dialog.dismiss();
                                } catch (NumberFormatException e) {
                                    Toast.makeText(Contacts.this, "'" + value.toString() + "' is not a number.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        button = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        button.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                    }
                });

                dialog.show();
                break;

            case 1:

                // set the title
                builder.setTitle("Send SMS");
                dialog = builder.create();

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                Editable smsText = edit.getText();
                                if (!smsText.toString().isEmpty()) {
                                    // TODO: send text message
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(Contacts.this, "The message is empty. Please write something.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        button = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        button.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                    }
                });
                dialog.show();
                break;
        }
        return true;
    }
}
