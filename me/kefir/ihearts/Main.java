package me.kefir.ihearts;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagString;

public class Main extends JavaPlugin implements Listener {
	
	ItemStack heartItem = getHeartItem();
	
	@Override
	public void onEnable() {
		ShapedRecipe heartRecipe = new ShapedRecipe(heartItem);
		heartRecipe.shape("BBB","BBB","BBB");
		heartRecipe.setIngredient('B', Material.BEACON);

		Bukkit.addRecipe(heartRecipe);
		Bukkit.getPluginManager().registerEvents(this, this);
		addCommands();
	}
	
	@EventHandler
	public void dmg(EntityDamageByEntityEvent e) {
		if(e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
			Player p = (Player) e.getDamager();
			Player p2 = (Player) e.getEntity();
			if(p2.getHealth()-e.getDamage()<=0) changeHP(p, 2);
		}
	}
	
	@EventHandler
	public void die(PlayerDeathEvent e) {
		changeHP(e.getEntity(), -2);
	}
	
	@EventHandler
	public void rmb(PlayerInteractEvent e) {
		if(e.getItem() != null) {
			ItemMeta im = e.getItem().getItemMeta();
			net.minecraft.server.v1_16_R3.ItemStack item = CraftItemStack.asNMSCopy(e.getItem());
			if(item.hasTag() && item.getTag().getString("ITEM_ID")=="HEART_ITEM" && im.hasDisplayName() && im.getDisplayName().equals("§6Heart")) {
				changeHP(e.getPlayer(), 2);
				e.getItem().setAmount(e.getItem().getAmount()-1);
			}
		}
	}
	
	public void changeHP(Player p, double amt) {
		try {
			p.setMaxHealth(p.getMaxHealth()+amt);
		} catch(IllegalArgumentException e) {
			for(Player p2 : Bukkit.getOnlinePlayers()) {
				p2.sendMessage(p.getName() + " lost all his hearts!");
			}
			p.kickPlayer("You lost all your health!");
		}
	}
	
	public void addCommands() {
		((CraftServer) this.getServer()).getCommandMap().register("", new Command("withdrawheart") {
			
			@Override
			public boolean execute(CommandSender arg0, String arg1, String[] arg2) {
				if(arg0 instanceof Player) {
					Player p = (Player) arg0;
					if(p.getMaxHealth()>2) {
						changeHP(p, -2);
						p.getInventory().addItem(heartItem);
						p.sendMessage("You withdrawn 1hp");
					}  else {
						p.sendMessage("You don't have enough hp to withdraw!");
					}
				} else {
					arg0.sendMessage("This command can be used by players only!");
				}
				return false;
			}
		});
	}
	
	public ItemStack getHeartItem() {
		ItemStack is = new ItemStack(Material.NETHER_STAR);
		
		net.minecraft.server.v1_16_R3.ItemStack heart = CraftItemStack.asNMSCopy(is);
		NBTTagCompound c = new NBTTagCompound();
		
		c.setString("ITEM_ID", "HEART_ITEM");
		
		heart.setTag(c);
		
		is = CraftItemStack.asBukkitCopy(heart);
		
		ItemMeta i = is.getItemMeta();
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("§7This item gives you 1 heart");
		lore.add("§7");
		lore.add("§7Consumes after use");
		
		i.setDisplayName("§6Heart");
		i.setLore(lore);
		
		is.setItemMeta(i);
		
		return is;
	}

}
