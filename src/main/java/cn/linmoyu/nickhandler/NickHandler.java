package cn.linmoyu.nickhandler;

import dev.iiahmed.disguise.Disguise;
import dev.iiahmed.disguise.DisguiseManager;
import dev.iiahmed.disguise.DisguiseProvider;
import dev.iiahmed.disguise.DisguiseResponse;
import io.loyloy.nicky.Nick;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Pattern;

public class NickHandler extends JavaPlugin implements Listener {

    private final DisguiseProvider provider = DisguiseManager.getProvider();

    public NickHandler() {
        provider.allowOverrideChat(false);
        provider.setNameLength(16);
        provider.setNamePattern(Pattern.compile("[\\u4e00-\\u9fa5_a-zA-Z0-9]*"));
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        DisguiseManager.initialize(this, false);

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.setDisplayName(player.getName()); // 防止奇怪的后遗症?
        String nickyName = new Nick(player).get();
        if (nickyName == null || nickyName.isEmpty()) return;
        // Nicky默认会在名字后面加两个§r§r 包括默认前缀也有 直接处理掉吧~
        nickyName = nickyName.replaceAll("§.", "");

        Disguise disguise = Disguise.builder()
                .setName(nickyName)
                .build();
        DisguiseResponse response = provider.disguise(player, disguise);
        switch (response) {
            case SUCCESS:
                player.setDisplayName(nickyName);
                break;
            case FAIL_NAME_INVALID:
                player.sendMessage("§c你的匿名昵称中包含无效字符, 请更换其他匿名后重试. §f(v" + getDescription().getVersion() + "§f)");
                break;
            case FAIL_NAME_TOO_LONG:
                player.sendMessage("§c你的匿名昵称过长. §f(v" + getDescription().getVersion() + "§f)");
                break;
            case FAIL_NAME_ALREADY_ONLINE:
                player.sendMessage("§c当前子服已有一名与你匿名昵称相同的玩家. §f(v" + getDescription().getVersion() + "§f)");
                break;
            default:
                player.sendMessage("§c在当前子服对你进行匿名时出错. 上报管理员时请提供以下错误, 并附带游玩模式: " + response + ". §f(v" + getDescription().getVersion() + "§f)");
                break;
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        player.setDisplayName(provider.getInfo(player).getName());
        provider.undisguise(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String fullCommand = event.getMessage();
        if (fullCommand.isEmpty()) return;
        String baseCommand = fullCommand.split(" ")[0];

        if (!baseCommand.equalsIgnoreCase("/nickhandler")) return;
        event.setCancelled(true);
        Player player = event.getPlayer();
        player.sendMessage("§fNickHandler By §b@LinMoyu_ | YukiEnd §av" + getDescription().getVersion());
    }


}
