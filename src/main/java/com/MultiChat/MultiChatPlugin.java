package com.MultiChat;

import ch.qos.logback.core.hook.ShutdownHook;
import ch.qos.logback.core.hook.ShutdownHookBase;
import ch.qos.logback.core.joran.action.ShutdownHookAction;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.ClientSessionManager;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ClanManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;

@Slf4j
@PluginDescriptor(
	name = "Dual Chat"
)
public class MultiChatPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private MultiChatConfig config;

	String extension=" (DC)";
	String walkie=null;
	boolean oneTime;
	@Inject
	private ClanManager clanManager;

	@Override
	protected void startUp() throws Exception
	{

	}
	@Override
	protected void shutDown() throws Exception
	{

	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage) throws Exception
	{
		if(walkie==null)
			oneTime=true;
		walkie = RuneLite.RUNELITE_DIR + "\\" + client.getLocalPlayer().getName() + "DualChat.txt";
		if(config.send()) {
			if(chatMessage.getSender()!=null&&!chatMessage.getSender().isEmpty()&&!chatMessage.getSender().contains(extension)) {
				FileWriter talkie = new FileWriter(walkie, true);
				try {
					talkie.write("<Clan>"+client.getClanMemberManager().getClanOwner());
					System.out.println(chatMessage.getName());
					ClanMemberRank rank = client.getClanMemberManager().findByName(chatMessage.getName().replaceAll("<img=\\d+>","")).getRank();
					if(rank.getValue()==-1) {
						talkie.write("<Sender>" + chatMessage.getSender()+extension);
					}else{
						talkie.write("<Sender>" + chatMessage.getSender()+extension+" <img="+clanManager.getIconNumber(rank)+">");
					}
					talkie.write("<Name>" + chatMessage.getName());
					talkie.write("<Message>" + chatMessage.getMessage() + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
				talkie.close();
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if(oneTime){
			oneTime=false;
			File deletion=null;
			for(File file:(RuneLite.RUNELITE_DIR).listFiles())
			{
				if(file.getName().contains("DualChat.txt")&&file.getName().contains(client.getLocalPlayer().getName())) {
					deletion=file;
					break;
				}
			}
			try {
				if (deletion != null)
					Files.delete(deletion.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (config.receive()){
			for(File file:(RuneLite.RUNELITE_DIR).listFiles())
			{
				File other = null;
				FileReader otherWalkie = null;
				if(file.getName().contains("DualChat.txt")&&!file.getName().contains(client.getLocalPlayer().getName())){
					try {
						other=file;
						otherWalkie = new FileReader(other);
					}catch (IOException e){
						e.printStackTrace();
					}
				}
				if(otherWalkie!=null){
					BufferedReader readie = new BufferedReader(otherWalkie);
					try {
						String text = readie.readLine();
						String clan="";
						if(client.getClanMemberManager()!=null){
							clan=client.getClanMemberManager().getClanOwner();
						}
						if(!text.isEmpty()) {
							for (String line : text.split("\n")) {
								if (!line.split("<Clan>")[1].split("<Sender>")[0].equals(clan)) {
									String Sender = line.split("<Sender>")[1].split("<Name>")[0];
									String Name = line.split("<Name>")[1].split("<Message>")[0];
									String Message = line.split("<Message>")[1];
									this.client.addChatMessage(ChatMessageType.FRIENDSCHAT, Name, Message, Sender);
								}
							}
						}
						readie.close();
					}catch (IOException e){
						e.printStackTrace();
					}
					other.delete();
				}
			}
		}
	}
	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged){
		if (gameStateChanged.getGameState() == GameState.HOPPING||gameStateChanged.getGameState() == GameState.LOGGING_IN) {
			walkie=null;
		}

		/*
		Declare this at the top


		Socket socket=null;
		boolean host;
		int port=7273;
		DataInputStream input=null;
		DataOutputStream output=null;

		Attempt to join active connection if that fails then start new socket

		if(gameStateChanged.getGameState() == GameState.LOGGED_IN){
			try
			{
				socket = new Socket("127.0.0.1", port);
				System.out.println("Connected");

				// takes input from terminal
				input  = new DataInputStream(System.in);

				// sends output to the socket
				out    = new DataOutputStream(socket.getOutputStream());
			}
			catch(UnknownHostException u)
			{
				System.out.println(u);
			}
			catch(IOException i)
			{
				System.out.println(i);
			}
		}
		 */

		/*
		if(config.receive()) {
			for (File file : (RuneLite.RUNELITE_DIR).listFiles()) {
				if (file.getName().contains("DualChat.txt") && !file.getName().contains(client.getLocalPlayer().getName())) {
					file.delete();
				}
			}
		}
		 */
	}
	@Provides
	MultiChatConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MultiChatConfig.class);
	}
}
