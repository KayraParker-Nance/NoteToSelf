package kpn.projects.notetoself.ui;

import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import kpn.projects.notetoself.R;
import kpn.projects.notetoself.backups.BackupManager;

public class SettingsFragment extends Fragment {

    private final ActivityResultLauncher<String> createBackupLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"), uri -> {
                if (uri == null) return;
                BackupManager.export(requireContext(), uri, new BackupManager.Callback() {
                    public void onSuccess() {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Backup saved", Toast.LENGTH_SHORT).show());
                    }
                    public void onError(Exception e) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Backup failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                });
            });

    private final ActivityResultLauncher<String[]> restoreBackupLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;
                BackupManager.restore(requireContext(), uri, new BackupManager.Callback() {
                    public void onSuccess() {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Restore complete", Toast.LENGTH_SHORT).show());
                    }
                    public void onError(Exception e) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Restore failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                });
            });

    Button btnBackup, btnRestore;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnBackup = view.findViewById(R.id.btnBackup);
        btnRestore = view.findViewById(R.id.btnRestore);

        btnBackup.setOnClickListener(v -> createBackupLauncher.launch("notetoself_backup.json"));
        btnRestore.setOnClickListener(v -> restoreBackupLauncher.launch(new String[]{"application/json"}));
    }
}