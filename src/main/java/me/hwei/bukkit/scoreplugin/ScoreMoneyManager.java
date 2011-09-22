package me.hwei.bukkit.scoreplugin;

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.PluginManager;

import com.nijikokun.register.payment.Method;
import com.nijikokun.register.payment.Method.MethodAccount;
import com.nijikokun.register.payment.Methods;

public class ScoreMoneyManager extends ServerListener  {

	public ScoreMoneyManager(PluginManager pluginManager, ScoreOutput output) {
		this.pluginManager = pluginManager;
		this.output = output;
	}
	
	public String Format(double amount) {
		if(this.method == null)
			return Double.toString(amount);
		return this.method.format(amount);
	}
	
	public boolean TakeMoney(String name, double amount) {
		if(this.method == null)
			return false;
		if(method.hasAccount(name)) {
			MethodAccount balance = method.getAccount(name);
			if(balance.hasEnough(amount)) {
				balance.subtract(amount);
				return true;
			}
	    }
		return false;
	}
	
	public boolean GiveMoney(String name, double amount) {
		if(this.method == null)
			return false;
		if(method.hasAccount(name)) {
			MethodAccount balance = method.getAccount(name);
			balance.add(amount);
			return true;
	    }
		return false;
	}
	
    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        if (Methods.hasMethod()) {
            Boolean check = Methods.checkDisabled(event.getPlugin());

            if(check) {
                this.method = null;
                this.output.ToConsole("Payment method was disabled. No longer accepting payments.");
            }
        }
    }

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
    	if (!Methods.hasMethod()) {
            if(Methods.setMethod(this.pluginManager)) {
            	this.method = Methods.getMethod();
            	if(this.method == null) {
            		this.output.ToConsole("Payment method not found.");
            	} else {
            		this.output.ToConsole("Payment method found (" + this.method.getName() + " version: " + this.method.getVersion() + ").");
            	}
            }
        }
    }
    
    protected Method method;
    protected PluginManager pluginManager;
    protected ScoreOutput output;
}
