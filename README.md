<p align="center">
    <h1>mini18n</h1>
    <a href="https://github.com/ShatteredSoftware/mini18n/blob/master/LICENSE"><img alt="License" src="https://img.shields.io/github/license/ShatteredSoftware/mini18n?style=for-the-badge&logo=github"/></a>
    <a href="https://github.com/ShatteredSoftware/mini18n/issues"><img alt="GitHub Issues" src="https://img.shields.io/github/issues/ShatteredSoftware/mini18n?style=for-the-badge&logo=github" /></a>
    <a href="https://discord.gg/zUbNX9t"><img alt="Discord" src="https://img.shields.io/badge/Get%20Help-On%20Discord-%237289DA?style=for-the-badge&logo=discord" /></a>
    <a href="ko-fi.com/uberpilot"><img alt="Ko-Fi" src="https://img.shields.io/badge/Support-on%20Ko--fi-%23F16061?style=for-the-badge&logo=ko-fi" /></a>
</p>

---

A tiny (12kb) internationalization library.


## General Usage

```java
public class Example {
    public static void main(String[] args) {
        // Create a new MessageSet
        var messageSet = new MessageSet();

        // Add a message
        messageSet.addMessage(Locale.ENGLISH, "world_greeting", "Hello, world!");
        // And in another language
        messageSet.addMessage(Locale.forLanguageTag("es"), "world_greeting", "¡Hola Mundo!");

        messageSet.get("greeting", Locale.US); // "Hello, world!"

        // Add a message with a placeholder
        messageSet.addMessage(Locale.ENGLISH, "greeting", "Hello, %name%!");
        messageSet.addMessage(Locale.forLanguageTag("es"), "greeting", "¡Hola %name%!");

        // Get the message, replacing placeholders:
        System.out.println(messageSet.get(Locale.ENGLISH, "greeting", new LinkedHashMap<>() {{
            put("name", "Hunter");
        }})); // "Hello, Hunter!"

        // Get a message, pluralized:
        messageSet.addMessage(Locale.ENGLISH, "dollars.one", "You have %count% dollar.");
        messageSet.addMessage(Locale.ENGLISH, "dollars.other", "You have %count% dollars.");
        System.out.println(messageSet.get(Locale.ENGLISH, "dollars", new LinkedHashMap<>() {{
            put("count", 5);
        }})); // "You have 5 dollars."
        
        // Get a message, ordinal:
        messageSet.addMessage(Locale.ENGLISH, "pages.one", "%count%st page");
        messageSet.addMessage(Locale.ENGLISH, "pages.two", "%count%nd page");
        messageSet.addMessage(Locale.ENGLISH, "pages.few", "%count%rd page");
        messageSet.addMessage(Locale.ENGLISH, "pages.other", "%count%th page");
        System.out.println(messageSet.get(Locale.ENGLISH, "pages", new LinkedHashMap<>() {{
            put("count", 10);
            put("ordinal", true);
        }})); // "10th Page"
        
        // Get a message and combine it with another message:
        messageSet.addMessage(Locale.ENGLISH, "home", "Welcome to the home page! %message:greeting%");
        System.out.println(messageSet.get(Locale.ENGLISH, "home", new LinkedHashMap<>() {{
            put("name", "Hunter");
        }})); // "Welcome to the home page! Hello, Hunter!"
    }
}
```

## Use it in a Minecraft Plugin

### Gradle:
**Add JitPack and the dependency:**
```gradle
repositories {
    maven {
        url = "https://jitpack.io"
        name = "JitPack"
    }
}

dependencies {
    implementation 'com.github.shatteredsoftware:mini18n:1.0.0'
}
```

### General Usage:
```java
import java.io.File;
import org.bukkit.plugin.java.JavaPlugin;
import software.shattered.mini18n.MessageSet;

public class MyPlugin extends JavaPlugin {
    private final MessageSet messageSet = new MessageSet();

    @Override
    public void onLoad() {
        loadMessages();
    }
    
    private void loadMessages() {
        // Save the default messages
        saveResource("en.yml", false);
        
        // Load the default messages
        var config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "en.yml"));
        config.getConfigurationSection("messages").getKeys(true).forEach(key -> {
            var message = config.getString("messages." + key);
            messageSet.addMessage(Locale.ENGLISH, key, message);
        });
    }
}
```