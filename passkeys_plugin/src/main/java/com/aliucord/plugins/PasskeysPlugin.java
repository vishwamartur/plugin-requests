package com.aliucord.plugins;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.aliucord.Plugin;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.PluginManifest;
import com.aliucord.patcher.Hook;
import com.aliucord.utils.ReflectUtils;

import java.util.HashMap;
import java.util.Map;

@AliucordPlugin
public class PasskeysPlugin extends Plugin {

    private Map<String, String> passkeys = new HashMap<>();

    @Override
    public void start(Context context) {
        patcher.patch(
                "com.discord.api.ApiClient",
                "login",
                new Class<?>[]{String.class, String.class},
                new Hook(callFrame -> {
                    String username = (String) callFrame.args[0];
                    String passkey = (String) callFrame.args[1];

                    if (passkeys.containsKey(username) && passkeys.get(username).equals(passkey)) {
                        callFrame.setResult(true);
                    } else {
                        callFrame.setResult(false);
                    }
                })
        );
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }

    @NonNull
    @Override
    public PluginManifest getManifest() {
        return new PluginManifest(
                "PasskeysPlugin",
                "1.0.0",
                "A plugin to backport passkeys/webauthn support to Aliucord.",
                "Aliucord",
                new String[]{"USE_BIOMETRIC", "USE_FINGERPRINT", "USE_FACE_RECOGNITION"}
        );
    }

    public void registerPasskey(String username, String passkey) {
        passkeys.put(username, passkey);
    }

    public void authenticatePasskey(String username, String passkey) {
        if (passkeys.containsKey(username) && passkeys.get(username).equals(passkey)) {
            Toast.makeText(context, "Authentication successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show();
        }
    }

    public View createPasskeyView(Context context) {
        View view = View.inflate(context, R.layout.plugin_passkeys, null);

        EditText usernameInput = view.findViewById(R.id.username_input);
        EditText passkeyInput = view.findViewById(R.id.passkey_input);
        Button registerButton = view.findViewById(R.id.register_button);
        Button authenticateButton = view.findViewById(R.id.authenticate_button);

        registerButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString();
            String passkey = passkeyInput.getText().toString();
            registerPasskey(username, passkey);
            Toast.makeText(context, "Passkey registered", Toast.LENGTH_SHORT).show();
        });

        authenticateButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString();
            String passkey = passkeyInput.getText().toString();
            authenticatePasskey(username, passkey);
        });

        return view;
    }
}
