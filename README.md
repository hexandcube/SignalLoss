# **SignalLoss**

**Get notified whenever your server connection gets interrupted**

SignalLoss is a lightweight, configurable Fabric mod that displays a simple toast notification when your connection to a Minecraft server is interrupted.

![SignalLoss toast animation](https://cdn.modrinth.com/data/cached_images/61a5f1033cc57639d894f89afa28c86ddfa23484.gif)

## Configuration
You can configure the mod in-game using commands or by editing `/config/signalloss.json`.

**Commands:**
* `/signalloss config <option> <value>` - sets a config value 
* `/signalloss config reset` - resets the config file to default settings
* `/signalloss reload` - reloads the config from `signalloss.json`

**Options:**

| Option | Command | Default     | Description                                                                                                 |
| :--- | :--- |:------------|:------------------------------------------------------------------------------------------------------------|
| **Enabled** | `enabled` | `true`      | Enable the SignalLoss mod                                                                                   |
| **Timeout Threshold** | `timeoutThreshold` | `2000` (2s) | How long to wait since the last packet was successfully received, before displaying the toast notification. |
| **Min Warning Time** | `minWarningTime` | `2000` (2s) | Minimum time the toast stays on screen after being triggered, to prevent flickering.                        |
| **Linger Time** | `lingerTime` | `1000` (1s) | How long the toast stays on screen after recovery (shows final lag duration).                               |
| **Draw Background** | `drawBackground` | `true`      | Should a background for the toast be rendered                                                               |
| **Singleplayer** | `showInSingleplayer`| `false`     | Enable the SignalLoss mod for the Singleplayer internal server                                              |
| **Position** | `position` | `CENTER`    | Screen position (`LEFT`, `CENTER`, `RIGHT`).                                                                |
| **Text Color** | `textColor` | `#FFFF5555` | ARGB Hex color for the warning text.                                                                        |
| **Background Color** | `backgroundColor` | `#A0000000` | ARGB Hex color for the background box.                                                                      |
