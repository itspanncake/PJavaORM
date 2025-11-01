# PJavaORM 🥞

A lightweight and flexible **Java ORM (Object-Relational Mapping) library** designed for Minecraft plugins (Paper/Spigot).

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![](https://jitpack.io/v/itspanncake/PJavaORM.svg)](https://jitpack.io/#itspanncake/PJavaORM)

---

## ✨ Features and Advantages

* **Zero User Dependency:** PancakeORM is distributed as a **Fat JAR (Uber JAR)**. All required JDBC drivers for **SQLite**, **MySQL**, and **PostgreSQL** are **integrated and safely relocated** within the library. Users don't need to configure ShadowJar or add extra dependencies for drivers.
* **Multi-Database Support:** Seamlessly switch between SQLite, MySQL/MariaDB, and PostgreSQL.
* **Automatic Schema Generation:** Generates `CREATE TABLE IF NOT EXISTS` queries on startup, ensuring your database schema matches your Java Entities.
* **Simple API:** Provides a generic `Repository<T>` for basic CRUD operations.

---

## 🚀 Installation

PancakeORM is published via **JitPack**.

### 1. Configure JitPack Repository

Add the JitPack repository to your plugin's `settings.gradle.kts`:

```kotlin
// settings.gradle.kts

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2. Add Dependency

Add the library to your plugin's ``build.gradle.kts``. Since the library is a Fat JAR, no separate JDBC driver dependencies are needed!

```kotlin
// build.gradle.kts (of your Plugin)

dependencies {
    // Core Plugin API (e.g., Paper)
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

    implementation("com.github.itspanncake:PJavaORM:1.0.0")
}
```

## ✍️ Usage Guide

### A. Define your Entity Model

```java
// src/main/java/com/yourplugin/data/PlayerStats.java

package com.yourplugin.data;

import fr.panncake.pjavaorm.annotations.*;

@Entity(name = "player_stats")
public class PlayerStats {

    @Id
    @Column(name = "player_uuid", length = 36)
    private String playerUUID;

    @Column(nullable = false)
    private int kills;

    private long lastSeen;

    public PlayerStats() {}

    public PlayerStats(String uuid, int kills, long lastSeen) {
        this.playerUUID = uuid;
        this.kills = kills;
        this.lastSeen = lastSeen;
    }

    // Getters and Setters... (omitted for brevity)
    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }
    public void setLastSeen(long lastSeen) { this.lastSeen = lastSeen; }
}
```

### B. Initialization and Operation Example

This example demonstrates initialization and a simple CRUD operation on a player join event.

```java
// src/main/java/com/yourplugin/MyPlugin.java

package com.yourplugin;

import fr.panncake.pjavaorm.PJavaORM;
import fr.panncake.pjavaorm.core.Repository;
import fr.panncake.pjavaorm.enums.DatabaseType;
import com.yourplugin.data.PlayerStats;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Optional;

public class MyPlugin extends JavaPlugin implements Listener {

    private Repository<PlayerStats> statsRepository;

    @Override
    public void onEnable() {
        // --- Initialization Example (Using SQLite) ---
        String url = "jdbc:sqlite:" + getDataFolder().getAbsolutePath() + "/database.db";

        try {
            // 1. Initialize ORM
            // Parameters: URL, User, Password, DB_Type, Entity Classes
            PancakeORM.init(url, null, null, DatabaseType.SQLITE, PlayerStats.class);
            
            // 2. Get the working Repository instance
            this.statsRepository = PancakeORM.getRepository(PlayerStats.class);
            
            getServer().getPluginManager().registerEvents(this, this);
            getLogger().info("PancakeORM initialized successfully.");

        } catch (SQLException e) {
            getLogger().severe("Failed to initialize PancakeORM database. Disabling plugin.");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String uuid = event.getPlayer().getUniqueId().toString();

        try {
            // READ: Find existing stats by UUID
            Optional<PlayerStats> optionalStats = statsRepository.findById(uuid);

            PlayerStats stats;
            if (optionalStats.isPresent()) {
                // UPDATE: Modify the existing object
                stats = optionalStats.get();
                stats.setKills(stats.getKills() + 1); // Simple counter update for example
                stats.setLastSeen(System.currentTimeMillis());
            } else {
                // CREATE: New object
                stats = new PlayerStats(uuid, 0, System.currentTimeMillis());
            }

            // SAVE: Persist the object (Insert or Update)
            statsRepository.save(stats); 
            
            getLogger().info(event.getPlayer().getName() + " loaded stats. Total kills: " + stats.getKills());

        } catch (SQLException e) {
            getLogger().severe("Database error during player join for " + uuid);
            e.printStackTrace();
        }
    }
}
```

## 🤝 Contributing

Contributions, bug reports, and feature suggestions are welcome! Feel free to open an **Issue** or submit a **Pull Request**.

## 📜 License

PancakeORM is distributed under the [**MIT License**](https://opensource.org/license/MIT).