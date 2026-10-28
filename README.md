# CleanInsta (v1.0.0)

<p align="center">
  <b>A clean, malware-free, privacy-hardened alternative to Honista and modified Instagram clients.</b><br>
  Built via binary reverse engineering, dynamic loader neutralization, and clean bytecode synthesis.
</p>

---

## 🔍 Background & Reverse Engineering Analysis

Popular modified Instagram clients like **Honista** promise privacy features (Ghost Mode, Anti-Delete Messages, Media Downloading), but our comprehensive teardown of `Honista_v13.0__x64.apk` revealed severe security risks and bloat:

1. **Dynamic Remote Code Execution (Fairy/Shadow Framework)**:
   - Honista inserted an unauthorized 7.4 MB DEX payload (`classes19.dex`, 5,702 classes).
   - It embedded the Fairy / Shadow plugin virtualization framework (`com.lib.app.core.*`, `com.lib.app.manager.*`) which dynamically hooks `ClassLoader`, `Instrumentation`, and `ActivityThread` to fetch and execute secondary unsigned APKs (`base-1.apk`, `/notification_res.apk`) at runtime from remote servers.
2. **Third-Party Telemetry & Tracking**:
   - Integrated AppMetrica and persistent beaconing to remote command-and-control servers (`nakheelteam.cc`).
3. **Evasion & Camouflage**:
   - Cloaked settings and injected features behind legitimate business namespaces (`com.instagram.business.activity.*` such as `As92`, `Ai5w`, `Abk3`, etc.) to evade heuristic malware scanners.
4. **Ad Injection & Force Redirects**:
   - Injected promotional hooks and external redirect intents (`android.intent.action.VIEW`) targeting third-party ad channels.

---

## 🛡️ CleanInsta Architecture

**CleanInsta** completely purges every dynamic loader, remote plugin engine, spyware beacon, and advertisement injection from the application while reimplementing all desired features cleanly, locally, and transparently.

```
┌────────────────────────────────────────────────────────┐
│               Host Instagram Engine (v426)             │
│            (classes.dex through classes18.dex)         │
└───────────────────────────┬────────────────────────────┘
                            │ Hook Gateway
┌───────────────────────────▼────────────────────────────┐
│                    CleanInsta Core                     │
│  ┌───────────────────────┐   ┌──────────────────────┐  │
│  │   HookManager.java    │   │   CleanPrefs.java    │  │
│  └───────────────────────┘   └──────────────────────┘  │
│  ┌───────────────────────┐   ┌──────────────────────┐  │
│  │    GhostMode.java     │   │  MessageVault.java   │  │
│  │ (Seen / Heartbeat)    │   │ (SQLite Anti-Unsend) │  │
│  └───────────────────────┘   └──────────────────────┘  │
│  ┌───────────────────────┐   ┌──────────────────────┐  │
│  │    AdBlocker.java     │   │ MediaDownloader.java │  │
│  │ (JSON / Telemetry)    │   │ (Direct CDN Engine)  │  │
│  └───────────────────────┘   └──────────────────────┘  │
│  ┌───────────────────────┐   ┌──────────────────────┐  │
│  │ DevOptionsManager.java│   │CleanSettingsActivity │  │
│  │  (QE / MobileConfig)  │   │   (Pure AMOLED UI)   │  │
│  └───────────────────────┘   └──────────────────────┘  │
└────────────────────────────────────────────────────────┘
```

### Key Modules

- **Zero-Telemetry Policy**: All calls to `nakheelteam.cc`, AppMetrica, Facebook telemetry (`logging_client_events`), and ad networks are dropped before leaving the socket layer.
- **Dynamic Loaders Neutralized**: The `Fairy` dynamic plugin system and all associated content providers (`PluginManagerProvider`, `PluginShadowProvider`, `ServiceProvider`, `AppMetricaService`) are replaced with empty, inert stubs.
- **Native AMOLED Settings**: Clean, dark-mode native UI (`CleanSettingsActivity`) directly accessible without external web views or ads.

---

## ✨ Features

### 👻 Ghost Mode & Privacy Shield
- **Ghost Stories**: Read and watch Instagram Stories without triggering `seen` receipts.
- **Ghost Direct Messages**: View incoming messages without sending `mark_seen` read receipts to chat partners.
- **Ghost Live Streams**: Watch live broadcasts anonymously without appearing in the viewer count or broadcasting presence heartbeats.
- **Typing Indicator Suppression**: Prevents real-time typing indicators from notifying the other party.

### 🛡️ Anti-Delete Message Vault
- Uses a local encrypted SQLite database (`clean_message_vault.db`) to log direct messages.
- If a sender un-sends or revokes a message, CleanInsta preserves the message text and media locally with a deleted indicator.

### 📥 High-Speed Media Downloader
- Multi-threaded direct CDN downloader for:
  - High-resolution Reels with audio intact
  - Stories and Highlights
  - Multi-photo carousel posts
  - Profile pictures in full resolution

### 🚫 AdBlocker & Clean Feed
- Automatically sanitizes timeline responses by filtering out sponsored feed posts, sponsored reels, and suggested carousel items.
- Suppresses client telemetry pings and analytics collectors.

### ⚙️ Developer Options & Experiment Flags
- Unlocks internal Instagram Developer Options and MobileConfig overrides (`is_employee`, internal build flags, UI experiment toggles).

---

## 📦 Downloads & Releases

Pre-compiled, signed APK packages are available under [Releases](https://github.com/phyroosh/CleanInsta/releases):

| Asset | Version | Signing Scheme | SHA-256 Checksum |
| :--- | :--- | :--- | :--- |
| `CleanInsta_v1.0.apk` | v1.0.0 | Android APK Signature v3 | `1a52110f8d373f6dbff784eda0dc38c44df7a8f761cace50566c91ba5336feb2` |

---

## 🛠️ Building from Source

### Prerequisites
- Linux / macOS
- OpenJDK 17 or higher
- Android SDK Build-Tools (version `34.0.0` or `35.0.0` with `d8`, `zipalign`, `apksigner`)
- Android platform SDK (`platforms/android-34/android.jar`)
- Python 3.8+

### Build Steps

1. Clone this repository:
   ```bash
   git clone https://github.com/phyroosh/CleanInsta.git
   cd CleanInsta
   ```

2. Compile the clean bytecode and repack the APK:
   ```bash
   ./build.sh
   ```

3. The final aligned and signed APK will be generated at:
   ```
   out/CleanInsta_v1.0.apk
   ```

---

## 📜 License & Disclaimer

This project is created strictly for educational, security auditing, and reverse-engineering research purposes under fair use. All trademarks, service marks, and company names belong to their respective owners.
