# AlwaysOnline Minecraft Plugin

A Bukkit/Spigot plugin that helps keep your Minecraft server running 24/7 with:
- Periodic health monitoring (memory, TPS)
- Optional `/stop` command blocking
- Admin status commands
- Auto-restart shell script with Aikar's optimized JVM flags

---

## Installation

### Option A: Build from Source (Maven)
```bash
mvn clean package
```
Copy `target/AlwaysOnline-1.0.0.jar` into your server's `plugins/` folder.

### Option B: Drop in Source Files
If you don't have Maven, a Minecraft plugin developer can compile the `.java` files with the Spigot API on the classpath.

---

## Auto-Restart Script

Place `start.sh` in your server root and run it instead of launching the JAR directly:

```bash
chmod +x start.sh
./start.sh
```

Edit the top of `start.sh` to set your RAM and JAR filename. The script uses **Aikar's JVM flags** for best performance and will **automatically restart** the server if it crashes.

---

## Commands

| Command | Description |
|---|---|
| `/alwaysonline status` | Show uptime, players, and memory |
| `/alwaysonline meminfo` | Detailed memory breakdown |
| `/alwaysonline tps` | Show TPS (1m / 5m / 15m) |
| `/alwaysonline reload` | Reload config.yml |
| `/alwaysonline help` | List all commands |

Aliases: `/aol`, `/online`

**Permission:** `alwaysonline.admin` (default: OP)

---

## config.yml Options

| Option | Default | Description |
|---|---|---|
| `keep-alive-interval-minutes` | `5` | How often health checks run |
| `log-keep-alive` | `false` | Log each keep-alive ping |
| `warn-high-memory` | `true` | Warn when memory > 85% |
| `warn-low-tps` | `true` | Warn when TPS < 15 |
| `block-stop-command` | `false` | Block `/stop` from console |
| `warn-on-stop` | `true` | Warn when `/stop` is used |
| `log-empty-server` | `false` | Log when server is empty |
| `show-uptime-on-join` | `true` | Show uptime to admins on join |

---

## Compatibility

- **Minecraft:** 1.13 – 1.20+
- **Server software:** Spigot, Paper, Purpur (any Bukkit-based)
- **Java:** 8+
