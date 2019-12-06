package fish.timer.com.timer2;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

/**
 * Created by Pantelemon on 11/29/2019.
 */
public class BlockEdit extends AppCompatDialogFragment {
    public View blockToEdit;
    private EditText editTextName;
    private EditText editTextDescription;
    private NumberPicker EditMinuts;
    private BlockEditListener listener;
    public final static int EDIT_BLOCK = 1;
    public final static int ADD_BLOCK = 2;
    public final static int DELETE_BLOCK = 3;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_time_block,null);

        builder.setView(view)
                .setTitle("Edit Block")
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialogAction(DELETE_BLOCK);
                    }
                })
                .setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialogAction(EDIT_BLOCK);
                    }
                })
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialogAction(ADD_BLOCK);
                    }
                });
        editTextName = (EditText) view.findViewById(R.id.edit_block_Name);
        editTextDescription = (EditText) view.findViewById(R.id.edit_block_Description);
        EditMinuts = (NumberPicker) view.findViewById(R.id.numberPicker);
        String[] nums = new String[60];
        for(int i=0; i<nums.length; i++)
            nums[i] = Integer.toString(i+1);

        EditMinuts.setMinValue(1);
        EditMinuts.setMaxValue(60);
        EditMinuts.setWrapSelectorWheel(false);
        EditMinuts.setDisplayedValues(nums);
        EditMinuts.setValue(1);
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (BlockEditListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement BlockEditlistener");
        }

    }

    public interface BlockEditListener{
        void applyInfo(String Name,int Minutes,String Description,int action);
    }
    public void dialogAction(int action){
        String Name = editTextName.getText().toString();
        String Description = editTextDescription.getText().toString();
        int NumMinutes = EditMinuts.getValue();
        listener.applyInfo(Name,NumMinutes,Description,action);
    }
}
