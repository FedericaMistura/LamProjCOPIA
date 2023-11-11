package com.example.lamproj;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

public abstract class PermissionUtils {
    /*
    Richiedere un permesso.
    Se l'utente ha negato il permesso in precedenza viene visualizzato un dialog
    Se non è stato richiesto in precedenza, viene richiesto immediatamente.
    Se l'utente nega il permesso, l'activity si chiude
     */
    public static void requestPermission(MainActivity activity, int requestId, String permission, boolean finishActivity){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)){
            PermissionUtils.RationaleDialog.newInstance(requestId, finishActivity).show(activity.getSupportFragmentManager(), "dialog");
        } else {
            ActivityCompat.requestPermissions(activity, new String[] {permission}, requestId);
        }
    }

    /*
    Verifica se un determinato permesso è stato concesso.
     */
    public static boolean isPermissionGranted(String[] grantPermissions, int[] grantResults, String permission){
        for (int i = 0; i < grantPermissions.length; i++){
            if(permission.equals(grantPermissions[i])){
                return grantResults[i] == PackageManager.PERMISSION_GRANTED;
            }
        }
        return false; //Se non è stato concesso il permesso
    }

    public static class PermissionDeniedDialog extends DialogFragment{
        private static final String ARGUMENT_FINISH_ACTIVITY = "finish";
        private boolean finishActivity = false;
        public static PermissionDeniedDialog newInstance(boolean finishActivity){
            Bundle arguments = new Bundle();
            arguments.putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity);

            PermissionDeniedDialog dialog = new PermissionDeniedDialog();
            dialog.setArguments(arguments);
            return dialog;
        }

        /*
        Creazione del dialog per informare l'utente che quel permesso non è stato dato
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            finishActivity = getArguments().getBoolean(ARGUMENT_FINISH_ACTIVITY);

            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.location_permission_denied)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
        }

        /*
        l'activity viene chiusa ma prima si mostra un messaggio Toast
         */
        @Override
        public void onDismiss(DialogInterface dialog){
            super.onDismiss(dialog);
            if (finishActivity) {
                Toast.makeText(getActivity(), R.string.permission_required_toast, Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }

    }

    public static class RationaleDialog extends DialogFragment{
        private static final String ARGUMENT_PERMISSION_REQUEST_CODE = "requestCode";
        private static final String ARGUMENT_FINISH_ACTIVITY = "finish";
        private boolean finishActivity = false;

        /*
        Impostazione codice della richiesta del permesso e chiusura o meno dell'activity
         */
        public static RationaleDialog newInstance(int requestCode, boolean finishActivity){
            Bundle arguments = new Bundle();
            arguments.putInt(ARGUMENT_PERMISSION_REQUEST_CODE, requestCode);
            arguments.putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity);
            RationaleDialog dialog = new RationaleDialog();
            dialog.setArguments(arguments);
            return dialog;
        }

        /*
        Dialog per informare l'utente del perchè quel permesso è richiesto
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstaceState){
            Bundle arguments = getArguments();
            final int requestCode = arguments.getInt(ARGUMENT_PERMISSION_REQUEST_CODE);
            finishActivity = arguments.getBoolean(ARGUMENT_FINISH_ACTIVITY);

            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.permission_rationale_location)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
                            finishActivity = false;
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }

        /*
        Quando si chiude il dialog, se finishActivity è true,
        si visualizza un messaggio Toast. Poi l'activity si chiude.
         */
        @Override
        public void onDismiss(DialogInterface dialog){
            super.onDismiss(dialog);
            if (finishActivity){
                Toast.makeText(getActivity(), R.string.permission_required_toast, Toast.LENGTH_SHORT).show();
                requireActivity().finish();
            }
        }


    }
}
