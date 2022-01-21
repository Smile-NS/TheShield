package io.github.smile_ns.theshield;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import static org.bukkit.event.block.Action.*;

public final class TheShield extends JavaPlugin implements Listener {

    static final int TIME = 5;
    static final int PERIOD = 10;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getServer().getPluginManager().registerEvents(this, this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onUseShield(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Action action = e.getAction();
        if (action == LEFT_CLICK_AIR ||
                action == LEFT_CLICK_BLOCK ||
                action == PHYSICAL
        ) return;

        PlayerInventory inv = player.getInventory();
        ItemStack main = inv.getItemInMainHand();
        ItemStack off = inv.getItemInOffHand();
        ItemStack shield = main.getType() == Material.SHIELD ?
                main : off.getType() == Material.SHIELD ? off : null;
        if (shield == null) return;

        ItemMeta meta = shield.getItemMeta();
        assert meta != null;
        int maxDurability = shield.getType().getMaxDurability();
        final boolean[] first = {true};
        Plugin pl = this;

        new BukkitRunnable() {
            public void run() {
                Damageable damageable = ((Damageable) meta);
                int damage = damageable.getDamage();

                if (!first[0] && !player.isBlocking()) {
                    cancel();
                    return;
                }

                if (maxDurability - damage <= 0) {
                    if (inv.getItemInOffHand().equals(shield))
                        inv.setItemInOffHand(null);
                    else inv.remove(shield);
                    cancel();

                    int slot = inv.getHeldItemSlot();
                    new BukkitRunnable() {
                        public void run() {
                            ItemStack newShield = new ItemStack(Material.SHIELD);
                            if (inv.getItem(slot) != null) inv.addItem(newShield);
                            else inv.setItem(slot, newShield);
                            cancel();
                        }
                    }.runTaskTimer(pl, 100, 0);
                    return;
                }

                damageable.setDamage(damage + maxDurability / (TIME * 20 / PERIOD));
                shield.setItemMeta(meta);
                first[0] = false;
            }
        }.runTaskTimer(this, 0, PERIOD);
    }

    @EventHandler
    public void onBlock(BlockDamageEvent e) {
        if (e.getInstaBreak()) e.setInstaBreak(false);
    }
}