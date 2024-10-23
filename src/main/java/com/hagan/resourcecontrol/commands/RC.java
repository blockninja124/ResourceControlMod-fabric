package com.hagan.resourcecontrol.commands;

import com.google.common.collect.ImmutableList;
import com.hagan.resourcecontrol.util.ResourceUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;

import java.util.ArrayList;
import java.util.Collection;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.synchronization.brigadier.StringArgumentSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;

public class RC {

	private static final Logger LOGGER = LogUtils.getLogger();

	//TODO: use a translatable here
	//private static final SimpleCommandExceptionType ERROR_EMPTY_PACKNAME = new SimpleCommandExceptionType(Component.literal("Packname not specified"));


	public static void register(CommandDispatcher<CommandSourceStack> dispatcher_) {
		dispatcher_.register(Commands.literal("rc").requires((player) -> {
			return true;
		}
				).then(Commands.literal("reloadall")
						.executes((player) -> {
							return reloadAll(player.getSource());
						}
								)
						).then(Commands.literal("activate")
								.then(Commands.argument("packname", StringArgumentType.string())
										.executes((player) -> {
											return activatePack(player.getSource(), StringArgumentType.getString(player, "packname"), true);
										}
												).then(Commands.argument("reload", BoolArgumentType.bool())
														.executes((player) -> {
															return activatePack(player.getSource(), StringArgumentType.getString(player, "packname"), BoolArgumentType.getBool(player, "reload"));
														}
																)
														)
										)
								).then(Commands.literal("deactivate")
										.then(Commands.argument("packname", StringArgumentType.string())
												.executes((player) -> {
													return deactivatePack(player.getSource(), StringArgumentType.getString(player, "packname"), true);
												}
														).then(Commands.argument("reload", BoolArgumentType.bool())
																.executes((player) -> {
																	return deactivatePack(player.getSource(), StringArgumentType.getString(player, "packname"), BoolArgumentType.getBool(player, "reload"));
																}
																		)
																)
												)
										).then(Commands.literal("moveup")
												.then(Commands.argument("packname", StringArgumentType.string())
														.then(Commands.argument("amount", IntegerArgumentType.integer())
																.executes((player) -> {
																	return movePackUp(player.getSource(), StringArgumentType.getString(player, "packname"), IntegerArgumentType.getInteger(player, "amount"), true);
																}
																		).then(Commands.argument("reload", BoolArgumentType.bool())
																				.executes((player) -> {
																					return movePackUp(player.getSource(), StringArgumentType.getString(player, "packname"), IntegerArgumentType.getInteger(player, "amount"), BoolArgumentType.getBool(player, "reload"));
																				}
																						)
																				)
																)
														)
												).then(Commands.literal("movedown")
														.then(Commands.argument("packname", StringArgumentType.string())
																.then(Commands.argument("amount", IntegerArgumentType.integer())
																		.executes((player) -> {
																			return movePackDown(player.getSource(), StringArgumentType.getString(player, "packname"), IntegerArgumentType.getInteger(player, "amount"), true);
																		}
																				).then(Commands.argument("reload", BoolArgumentType.bool())
																						.executes((player) -> {
																							return movePackDown(player.getSource(), StringArgumentType.getString(player, "packname"), IntegerArgumentType.getInteger(player, "amount"), BoolArgumentType.getBool(player, "reload"));
																						}
																								)
																						)
																		)
																)
														)
				);

	};

	/**
	 * Reloads the entire resources of the client this is called on. Meant for use in commands with a {@link net.minecraft.commands.CommandSourceStack CommandSourceStack}.
	 * Basically forces a client to do F3 + T. If you are looking for a public function to do this, see {@link com.hagan.resourcecontrol.util.ResourceUtils#reloadAll() ResourceUtils#reloadAll()}
	 * @param commandSource A {@link net.minecraft.commands.CommandSourceStack CommandSourceStack} for sending the success or error message.
	 * @return 1 if succeeded, or 0 if any errors occurred. Errors shouldn't happen unless I've fucked up, so please report them to me.
	 */
	private static int reloadAll(CommandSourceStack commandSource) {

		try {
			ResourceUtils.reloadAll();

			commandSource.sendSuccess(() -> {
				return Component.literal("Reloaded textures of player");
			}, true);

			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			commandSource.sendSuccess(() -> {
				return Component.literal("Command failed with error: " + e.toString() + ". See log for more info");
			}, false);
			return 0;
		}
	}

	/**
	 * Uses a {@link net.minecraft.commands.CommandSourceStack CommandSourceStack} to try and find an available
	 * pack with an id of {@link packId}, using the packRepository. Logs if pack is found
	 * in console and in the source stack {@link net.minecraft.commands.CommandSourceStack#sendSuccess CommandSourceStack#sendSuccess}
	 * @param commandSource
	 * @param packId the pack id as a string
	 * @param packRepository
	 * @return Returns the Pack object if the pack was found, or null
	 */
	private static Pack findPack(CommandSourceStack commandSource, String packId, PackRepository packRepository) {

		// ----- Get packs ----- //
		Collection<Pack> availablePacks = packRepository.getAvailablePacks();

		// ----- Find pack ----- //
		LOGGER.info("Searching for pack with id of: " + packId);


		String foundStatus = "not_found";
		Pack foundPack = null;


		for (Pack pack : availablePacks) {
			if (pack.getId().toString().equals("file/" + packId)) {
				LOGGER.info("Found pack:"+pack.getId().toString());
				foundStatus = "found";
				foundPack = pack;
				break;
			}

			// This should never overtake a situation where there is both a .zip and a folder, since if theres a folder itll just break before getting here.
			if (pack.getId().toString().equals("file/" + packId + ".zip")) {
				foundStatus = "zip";
			}
		}

		// ----- Respond to user ----- //
		switch (foundStatus) {

		case "found":
			LOGGER.info("Pack found!");
			break;

		case "zip":
			// Maybe they meant packId.zip
			commandSource.sendSuccess(() -> {
				return Component.literal("Pack with name of '" + packId + "' wasn't found. Did you maybe mean '"+packId+".zip' instead?");
			}, false);
			return null;

		case "not_found":
			// Just wasn't found
			commandSource.sendSuccess(() -> {
				return Component.literal("Pack with name of '" + packId + "' wasn't found");
			}, false);
			return null;

		default:
			// Something very wrong happened and foundStatus is a wrong string.
			commandSource.sendSuccess(() -> {
				return Component.literal("Something unexpected happened when trying to find the pack");
			}, false);
			return null;
		}

		// ----- Return ----- //
		return foundPack;
	}

	private static int activatePack(CommandSourceStack commandSource, @Nullable String packId, boolean reload) {

		try {

			Minecraft mc = Minecraft.getInstance();
			PackRepository packRepository = mc.getResourcePackRepository(); // Access to available packs

			Pack foundPack = findPack(commandSource, packId, packRepository);

			// We already told the user what happened in findPack so just return failed here
			if (foundPack == null) {
				return 0;
			}


			// ----- Activate pack ----- //

			Collection<String> selectedPacks = packRepository.getSelectedIds();

			// Activate the new pack (if not already active)
			if (selectedPacks.contains(foundPack.getId())) {
				commandSource.sendSuccess(() -> {
					return Component.literal("Pack is already selected");
				}, false);
				return 0;
			}

			// Make a copy of selected ids list, so that we can change it
			Collection<String> mutableSelectedPacks = new ArrayList<>(selectedPacks);

			// Add our pack id
			mutableSelectedPacks.add("file/" + packId);

			// Set the selected packs to our new list with our pack added
			packRepository.setSelected(mutableSelectedPacks);

			packRepository.getAvailablePacks();


			// ----- Reload resources ----- //

			if (reload) {
				// Reload the players resources. If not done, all sorts of weirdness happens.
				ResourceUtils.reloadAll();
			}

			commandSource.sendSuccess(() -> {
				return Component.literal("Activated pack");
			}, true);

			return 1;

			// ----- Error handling ----- //
		} catch (Exception e) {
			e.printStackTrace();
			commandSource.sendSuccess(() -> {
				return Component.literal("Command failed with error: " + e.toString() + ". See log for more info");
			}, false);
			return 0;
		}

	}

	private static int deactivatePack(CommandSourceStack commandSource, @Nullable String packId, boolean reload) {

		try {

			Minecraft mc = Minecraft.getInstance();
			PackRepository packRepository = mc.getResourcePackRepository(); // Access to available packs

			Pack foundPack = findPack(commandSource, packId, packRepository);

			// We already told the user what happened in findPack so just return failed here
			if (foundPack == null) {
				return 0;
			}


			// ----- Deactivate pack ----- //

			Collection<String> selectedPacks = packRepository.getSelectedIds();

			// Deactivate the new pack (if not already)
			if (!selectedPacks.contains(foundPack.getId())) {
				commandSource.sendSuccess(() -> {
					return Component.literal("Pack is already disabled");
				}, false);
				return 0;
			}

			// Make a copy of selected ids list, so that we can change it
			Collection<String> mutableSelectedPacks = new ArrayList<>(selectedPacks);

			// Remove our pack id
			mutableSelectedPacks.remove("file/" + packId);

			// Set the selected packs to our new list with our pack added
			packRepository.setSelected(mutableSelectedPacks);

			packRepository.getAvailablePacks();


			// ----- Reload resources ----- //

			if (reload) {
				// Reload the players resources. If not done, all sorts of weirdness happens.
				ResourceUtils.reloadAll();
			}

			commandSource.sendSuccess(() -> {
				return Component.literal("Deactivated pack");
			}, true);


			return 1;

			// ----- Error handling ----- //
		} catch (Exception e) {
			e.printStackTrace();
			commandSource.sendSuccess(() -> {
				return Component.literal("Command failed with error: " + e.toString() + ". See log for more info");
			}, false);
			return 0;
		}

	}


	/**
	 * Changes a resource packs position in the hierarchy. Will give CommandSourceStack an error if pack isn't enabled.
	 * Same for if pack isn't found or component is given as null.
	 * @param commandSource
	 * @param packId the pack id as a string
	 * @param amount If negative, will move the pack that amount towards index 0. If positive, away
	 * @param reload boolean, if true the clients resources will be reloaded after moving. 
	 * @return returns an int. 0 if something failed or 1 if everything succeeded
	 */
	private static int movePack(CommandSourceStack commandSource, @Nullable String packId, int amount, boolean reload) {
		try {

			Minecraft mc = Minecraft.getInstance();
			PackRepository packRepository = mc.getResourcePackRepository(); // Access to available packs

			Pack foundPack = findPack(commandSource, packId, packRepository);

			// We already told the user what happened in findPack so just return failed here
			if (foundPack == null) {
				return 0;
			}


			Collection<String> selectedPacks = packRepository.getSelectedIds();

			// Make sure pack is enabled
			if (!selectedPacks.contains(foundPack.getId())) {
				commandSource.sendSuccess(() -> {
					return Component.literal("Pack not enabled");
				}, false);
				return 0;
			}

			// Make a copy of selected ids list, so that we can change it
			//TODO: why are we going from collection (selectedPacks) to ArrayList back to Collection (mutableSelectedPacks)
			Collection<String> mutableSelectedPacks = new ArrayList<>(selectedPacks);





			// Let me explain this array manipulation here. 
			// First, we get the current index of the pack.
			// Say our pack is "b" and our list is: [a, b, c, d]. We get an index of 1
			// Then we remove index 1 from the list, leaving us with [a, c, d]
			// Then we add back our pack at oldIndex + 1 (which is 2 in this example).
			// That leaves us with the list [a, c, b, d] which has basically moved b one to the right,
			// Or one lower on the list. Boom we've moved are pack. Then you just gotta clamp
			// to not go out of bounds

			//TODO: IMPORTANT probably need to change this so it wont try to go below the mod resources and vanilla resources in priority. Probably will crash lol

			ArrayList<String> array = new ArrayList<>(selectedPacks);

			LOGGER.info("Current packs: " + selectedPacks);

			int oldIndex = array.indexOf(foundPack.getId());
			int newIndex = oldIndex + amount;

			array.remove(oldIndex);


			// This is just clamping between 0 and array.size()
			newIndex = Math.max(0, Math.min(array.size(), newIndex));


			array.add(newIndex, foundPack.getId());

			LOGGER.info("New packs: " + array);

			//mutableSelectedPacks.
			// Remove our pack id
			//mutableSelectedPacks.remove("file/" + component.getString());

			// Set the selected packs to our new list with our pack added
			packRepository.setSelected(array);

			//TODO: is this needed? Its also in activatePack and deactivatePack
			packRepository.getAvailablePacks();


			// ----- Reload resources ----- //

			if (reload) {
				// If not done, all sorts of weirdness happens.
				ResourceUtils.reloadAll();
			}



			// ----- Send success ----- //

			commandSource.sendSuccess(() -> {
				return Component.literal("Moved pack");
			}, true);


			return 1;

			// ----- Error handling ----- //
		} catch (Exception e) {
			e.printStackTrace();
			commandSource.sendSuccess(() -> {
				return Component.literal("Command failed with error: " + e.toString() + ". See log for more info");
			}, false);
			return 0;
		}
	}

	/**
	 * Moves a pack up in the pack priority hierarchy. If amount is 0, will move it the max amount up.
	 * @param commandSource
	 * @param packId the pack id as a string
	 * @param amount amount of places to move the pack up, or to top if 0.
	 * @return int, 0 if something failed or 1 if everything succeeded.
	 * @see #movePackDown
	 */
	private static int movePackUp(CommandSourceStack commandSource, @Nullable String packId, int amount, boolean reload) {
		//TODO: should this be ===? I saw on SO that == is comparing place in memory, which seems not quite what i want
		if (amount == 0) {
			amount = 24000; //big enough?
		}

		// Maybe should be negative? Not sure if index 0 is top priority or just max index
		return movePack(commandSource, packId, amount, reload);
	}

	/**
	 * Moves a pack down in the pack priority hierarchy. If amount is 0, will move it the max amount down.
	 * It will not / cannot send the pack below the 'mod resources' and 'minecraft' resources.
	 * @param commandSource
	 * @param packId the pack id as a string
	 * @param amount amount of places to move the pack down, or to bottom if 0.
	 * @return int, 0 if something failed or 1 if everything succeeded.
	 * @see #movePackUp
	 */
	private static int movePackDown(CommandSourceStack commandSource, @Nullable String packId, int amount, boolean reload) {

		if (amount == 0) {
			amount = 24000; //small enough?
		}

		return movePack(commandSource, packId, -amount, reload);
	}
}